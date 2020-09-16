package com.example.sailinglayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import org.decimal4j.util.DoubleRounder;

import java.util.ArrayList;


public class NavigationCompassActivity extends AppCompatActivity implements SensorEventListener {

    ImageView compass_img;
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
    int courseSize;
    ArrayList<Location> locations;
    TextView textView_distance, textView_bearing;
    LinearLayout layoutCourseLayout;
    RadioGroup radioGroup;
    ArrayList<RadioButton> radioButtons;
    int selectedMark;
    double bearingDirection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_compass);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Navigation Compass");
            actionBar.setDisplayHomeAsUpEnabled(false);
        }


        Intent intent = getIntent();
        course = intent.getParcelableExtra("COURSE");
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

        for(int i = 0; i < courseSize; i++){
            radioButtons.add(new RadioButton(this));
            radioButtons.get(i).setId(i+1);
            radioGroup.removeView(radioButtons.get(i));
            radioGroup.addView(radioButtons.get(i)); //the RadioButtons are added to the radioGroup instead of the layout
            radioButtons.get(i).setText("Mark " + (i+1));
            radioButtons.get(i).setTextSize(20);
            radioButtons.get(i).setButtonDrawable(R.drawable.selector_radio);
            radioButtons.get(i).setPadding(7,0,20,0);
        }
        radioGroup.check(radioButtons.get(courseSize-1).getId());

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == courseSize) {
                    selectedMark = 0;
                } else {
                    selectedMark = checkedId;
                }

                setTexts();
            }



        });


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_compass = (TextView) findViewById(R.id.txt);

        start();
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


                setTexts();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.btn_home:
                // User chose the "Menu" item, show the app menu UI...
                intent = new Intent();
                intent.putExtra("COURSE_VARIABLES", course.getCourseVariablesObject());
                intent.setClass(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.btn_variables:
                intent = getIntent();
                intent.putExtra("COURSE_VARIABLES", course.getCourseVariablesObject());
                intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
                startActivity(intent);
                return true;

            case R.id.btn_weather:
                intent = getIntent();
                intent.putExtra("LOCATION",locations.get(locations.size() - 1));
                intent.setClass(getApplicationContext(),WeatherAPIActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void setTexts() {
        double distBetweenPoints = met2nm(course.getCoords().get(selectedMark).distanceTo(locations.get(locations.size()-1)));

        double bearingBetweenPoints = bearingBetweenPoints(locations.get(locations.size()-1).getLatitude(),locations.get(locations.size()-1).getLongitude(),
                course.getCoords().get(selectedMark).getLatitude(),course.getCoords().get(selectedMark).getLongitude());

        String bearingString = decimalDeg2degMins(bearingBetweenPoints);
        // Display new distance to selected mark
        String distText = distBetweenPoints + " Nm";
        textView_distance.setText(distText);


        // Display updated bearing to newly selected mark
        String bearingText = "" + bearingString;
        textView_bearing.setText(bearingText);

    }

    private double met2nm(float met) { return DoubleRounder.round(met / 1852,2);}
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    public String decimalDeg2degMins(double decDegree) {
        int d = (int) decDegree;
        double m = (decDegree - d) * 60;
        return "" + d + "°" + String.format("%.3f",m) + "'";
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

        String where = "NW";

        if (degrees >= 350 || degrees <= 10)
            where = "N";
        if (degrees < 350 && degrees > 280)
            where = "NW";
        if (degrees <= 280 && degrees > 260)
            where = "W";
        if (degrees <= 260 && degrees > 190)
            where = "SW";
        if (degrees <= 190 && degrees > 170)
            where = "S";
        if (degrees <= 170 && degrees > 100)
            where = "SE";
        if (degrees <= 100 && degrees > 80)
            where = "E";
        if (degrees <= 80 && degrees > 10)
            where = "NE";


        txt_compass.setText(degrees + "° " + where);
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

}
