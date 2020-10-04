package com.example.sailinglayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.decimal4j.util.DoubleRounder;

import java.util.ArrayList;


public class NavigationCompassActivity extends AppCompatActivity implements SensorEventListener, ConfirmDialogFragment.ConfirmDialogListener, SharedPreferences.OnSharedPreferenceChangeListener {

    ImageView compass_img;
    ImageView arrow;
    TextView txt_compass;
    int degrees;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    boolean haveSensor = false, haveSensor2 = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    MarkerCoordCalculations course;
    CourseVariablesObject cvObject;
    int courseSize;
    ArrayList<Location> locations;
    TextView textView_distance, textView_bearing;
    LinearLayout layoutCourseLayout;
    RadioGroup radioGroup;
    ArrayList<RadioButton> radioButtons;
    int selectedMark;
    double bearingDirection;
    BottomNavigationView topNavigation;
    String distFormat;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_compass);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Navigation Compass");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_black_24dp);
        }

        setupSharedPreferences();

        topNavigation = findViewById(R.id.navCompass_top_navigation);


        //set selected page
        topNavigation.setSelectedItemId(R.id.topNav_compass);

        //perform ItemSelectedListener
        topNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                switch (item.getItemId()){

                    case R.id.topNav_layout:
                        if (course != null && cvObject != null) {
                            Intent intent = new Intent();
                            intent.putExtra("COURSE", course);
                            intent.putExtra("SELECTED_MARK", selectedMark);
                            intent.putExtra("COURSE_VARIABLES", cvObject);
                            intent.setClass(getApplicationContext(),CourseLayoutActivity.class);
                            startActivity(intent);
                        } else { }
                        return true;
                    case R.id.topNav_compass:
                        return true;
                    case R.id.topNav_navigation:
                        if (course != null && cvObject != null) {
                            Intent intent = new Intent();
                            intent.putExtra("COURSE", course);
                            intent.putExtra("SELECTED_MARK", selectedMark);
                            intent.putExtra("COURSE_VARIABLES", cvObject);
                            intent.setClass(getApplicationContext(),NavigationMap.class);
                            startActivity(intent);
                        } else { }
                        return true;
                }
                return false;
            }
        });

        Intent intent = getIntent();
        course = intent.getParcelableExtra("COURSE");
        cvObject = intent.getParcelableExtra("COURSE_VARIABLES");
        selectedMark = intent.getIntExtra("SELECTED_MARK",1);
        courseSize = course.getCoords().size();

        locations = new ArrayList<>();

        textView_distance = findViewById(R.id.text_NavCompDist);
        textView_bearing = findViewById(R.id.text_NavCompBear);
        radioGroup = findViewById(R.id.rgNavCompass);
        radioButtons = new ArrayList<>();
        float courseBearing = (float) - rad2deg(course.getCourseVariablesObject().getBearing());
        bearingDirection = -courseBearing;
        layoutCourseLayout = (LinearLayout) findViewById(R.id.layout_navigationCompass);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(checkLocationPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, locationListener);
            locations.add(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }


        // Set number of radio buttons to number of marks
        Typeface font = ResourcesCompat.getFont(this, R.font.roboto_medium);
        for(int i = 0; i < courseSize; i++){
            radioButtons.add(new RadioButton(this));
            radioButtons.get(i).setId(i+1);
            radioGroup.removeView(radioButtons.get(i));
            radioGroup.addView(radioButtons.get(i)); //the RadioButtons are added to the radioGroup instead of the layout
            radioButtons.get(i).setText("MARK " + (i+1));
            radioButtons.get(i).setTextSize(14);
            radioButtons.get(i).setTypeface(font);
            radioButtons.get(i).setButtonDrawable(R.drawable.selector_radio);
            radioButtons.get(i).setPadding(7,0,20,0);
        }
        if (selectedMark == 0) {
            radioGroup.check(courseSize);
        } else {
            radioGroup.check(selectedMark);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == courseSize) {
                    selectedMark = 0;
                } else {
                    selectedMark = checkedId;
                }

                setBearText();
                setDistText();
            }



        });


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = (ImageView) findViewById(R.id.img_compass);
        arrow = (ImageView) findViewById(R.id.img_arrow);
        txt_compass = (TextView) findViewById(R.id.text_NavCompHead);

        start();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        distFormat = sharedPreferences.getString("distance", "nm");
    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (locations.size() >= 2) {
                locations.remove(0);
                locations.add(location);
                bearingDirection = locations.get(0).bearingTo(locations.get(1));
            } else {
                locations.add(location);
            }

            setBearText();
            setDistText();

        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.btn_home:
                showConfirmDialog();
                return true;

            case R.id.btn_variables:
                intent = new Intent();
                intent.putExtra("COURSE_VARIABLES", cvObject);
                intent.putExtra("PREV_ACTIVITY",2);
                intent.setClass(getApplicationContext(),CourseVariablesBackdropActivity.class);
                startActivity(intent);
                return true;

            case R.id.btn_weather:
                intent = new Intent();
                intent.putExtra("LOCATION",locations.get(locations.size() - 1));
                intent.setClass(getApplicationContext(),WeatherAPIActivity.class);
                startActivity(intent);
                return true;

            case R.id.btn_settings:
                intent = new Intent();
                intent.setClass(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        topNavigation.setSelectedItemId(R.id.topNav_compass);

    }

    public void setBearText() {
        double bearingBetweenPoints = bearingBetweenPoints(locations.get(locations.size()-1).getLatitude(),locations.get(locations.size()-1).getLongitude(),
                course.getCoords().get(selectedMark).getLatitude(),course.getCoords().get(selectedMark).getLongitude());

        // Display updated bearing to newly selected mark
        String bearingText = Math.round(bearingBetweenPoints) + "°";
        textView_bearing.setText(bearingText);
    }

    public void setDistText() {
        double distBetweenPointsMetres = course.getCoords().get(selectedMark).distanceTo(locations.get(locations.size()-1));

        // Display new distance to selected mark
        if (distFormat.equals("km")) {
            String distText = met2km(distBetweenPointsMetres) + " km";
            textView_distance.setText(distText);
        } else {
            String distText = met2nm(distBetweenPointsMetres) + " Nm";
            textView_distance.setText(distText);
        }
    }

    private double met2nm(double met) { return DoubleRounder.round(met / 1852,2);}
    private double met2km(double met) { return DoubleRounder.round(met / 1000, 2);}
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public double bearingBetweenPoints(double lat1, double lon1, double lat2, double lon2){
        double longDiff = deg2rad(lon2) - deg2rad(lon1);
        double latitude1 = deg2rad(lat1);
        double latitude2 = deg2rad(lat2);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);
        double rad = Math.atan2(y,x);
        double result = (rad2deg(rad)+360) % 360;


        return result;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            degrees = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            degrees = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        degrees = Math.round(degrees);
        compass_img.setRotation(-degrees);

        double bearingBetweenPoints = bearingBetweenPoints(locations.get(locations.size()-1).getLatitude(),locations.get(locations.size()-1).getLongitude(),
                course.getCoords().get(selectedMark).getLatitude(),course.getCoords().get(selectedMark).getLongitude());
        bearingBetweenPoints = Math.round(bearingBetweenPoints);
        arrow.setRotation((float) -(degrees-bearingBetweenPoints));

        txt_compass.setText(degrees + "°");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                noSensorsAlert();
            }
            else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
        else{
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorsAlert(){
        Log.d("Response","working");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }

    public void stop() {
        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this,mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        }
        else{
            if(haveSensor)
                mSensorManager.unregisterListener(this,mRotationV);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }


    final int REQUEST_LOCATION = 2;
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setMessage("GPS not enabled. Please allow location services and then return")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(NavigationCompassActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void showConfirmDialog() {
        DialogFragment dialog = new ConfirmDialogFragment();
        dialog.show(getSupportFragmentManager(),null);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("distance")) {
            distFormat = sharedPreferences.getString(key, "nm");
            setDistText();
        }
    }
}
