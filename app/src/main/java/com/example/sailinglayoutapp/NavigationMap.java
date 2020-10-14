package com.example.sailinglayoutapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.decimal4j.util.DoubleRounder;

import java.util.ArrayList;

public class NavigationMap extends AppCompatActivity implements ConfirmDialogFragment.ConfirmDialogListener, LocationServicesFragment.LocationServicesListener, LocationConnectingFragment.LocationConnectingListener, LocationNullFragment.LocationNullListener, SharedPreferences.OnSharedPreferenceChangeListener {

    NavMapGLSurfaceView gLView;
    MarkerCoordCalculations course;
    CourseVariablesObject cvObject;
    int courseSize;
    ArrayList<Location> locations;
    TextView textView_distance, textView_bearing;
    ConstraintLayout layoutGL;
    RadioGroup radioGroup;
    ArrayList<AppCompatRadioButton> radioButtons;
    int selectedMark;
    double bearingDirection;
    ImageView img_compass, img_crosshair;
    BottomNavigationView topNavigation;
    String distFormat;
    int width, height;
    boolean locate;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_map);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Course Navigation");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_black_24dp);
        }

        setupSharedPreferences();

        assignFields();

        topNavigation = findViewById(R.id.navMap_top_navigation);


        //set selected page
        topNavigation.setSelectedItemId(R.id.topNav_navigation);

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
                        if (course != null && cvObject != null) {
                            Intent intent = new Intent();
                            intent.putExtra("COURSE", course);
                            intent.putExtra("SELECTED_MARK", selectedMark);
                            intent.putExtra("COURSE_VARIABLES", cvObject);
                            intent.setClass(getApplicationContext(),NavigationCompassActivity.class);
                            startActivity(intent);
                        } else { }
                        return true;
                    case R.id.topNav_navigation:
                        return true;
                }
                return false;
            }
        });

        locate = true;
        // Reset to centre of screen when user clicks
        img_crosshair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locate) {
                    gLView.setUserCentre();
                    locate = false;
                } else {
                    gLView.resetMapCentre();
                    locate = true;
                }
            }
        });

        layoutGL = (ConstraintLayout) findViewById(R.id.cl_nav_map);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(checkLocationPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, locationListener);
        }

        // Set number of radio buttons to number of marks

        Typeface font = ResourcesCompat.getFont(this, R.font.roboto_medium);
        for(int i = 0; i < courseSize; i++){
            radioButtons.add(new AppCompatRadioButton(this));
            radioButtons.get(i).setId(i+1);
            radioGroup.removeView(radioButtons.get(i));
            radioGroup.addView(radioButtons.get(i)); //the RadioButtons are added to the radioGroup instead of the layout
            radioButtons.get(i).setText("MARK " + (i+1));
            radioButtons.get(i).setTextSize(getResources().getDimensionPixelSize(R.dimen.radio_button_text)/ getResources().getDisplayMetrics().density);
            radioButtons.get(i).setTypeface(font);
            radioButtons.get(i).setButtonDrawable(R.drawable.selector_radio);
            radioButtons.get(i).setPadding(7,0,20,0);
        }
        // If selected mark = 0, then that is the final mark
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
                if (locations.size() > 0) {
                    setTexts();
                } else {
                    gLView.setSelectedMark(selectedMark);
                    redrawGLView();
                }
            }
        });

        gLView = new NavMapGLSurfaceView(this, course.getCoords(), locations, selectedMark, bearingDirection);

        getLocation();
        float courseBearing = (float) - rad2deg(course.getCourseVariablesObject().getBearing());
        bearingDirection = -courseBearing;
        img_compass.setRotation(courseBearing);
        gLView.setmAngle(courseBearing);

        layoutGL.addView(gLView);

        gLView.setOnTouchListener(rotationListener);

        img_compass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gLView.getmAngle() != 0) {
                    // Rotate map and compass so north is straight up
                    img_compass.setRotation(0);
                    gLView.setmAngle(0);
                }
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        if (locations.size() > 0) {
            setTexts();
        }
    }

    public void assignFields() {
        Intent intent = getIntent();
        course = intent.getParcelableExtra("COURSE");
        cvObject = intent.getParcelableExtra("COURSE_VARIABLES");
        courseSize = course.getCoords().size();

        selectedMark = intent.getIntExtra("SELECTED_MARK",0);

        locations = new ArrayList<>();
        textView_distance = findViewById(R.id.text_NavMapDist);
        textView_bearing = findViewById(R.id.text_NavMapBear);
        radioGroup = findViewById(R.id.rgNavMap);
        radioButtons = new ArrayList<>();
        img_compass = findViewById(R.id.img_NavMapCompass);
        img_crosshair = findViewById(R.id.img_crosshair);
    }


    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        distFormat = sharedPreferences.getString("distance", "nm");
    }


    View.OnTouchListener rotationListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            // Make the compass rotate with the map
            img_compass.setRotation(gLView.getmAngle());

            return false;
        }
    };

    private void getLocation() {
        if (checkLocationPermission()) {
            if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null) {
                showLocationNullDialog();
            } else {
                updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }
        }
    }

    private void updateLocation(Location location) {
        if (location != null) {
            if (locations.size() >= 2) {
                locations.remove(0);
                locations.add(location);
                bearingDirection = locations.get(0).bearingTo(locations.get(1));
            } else {
                locations.add(location);
            }
            setTexts();
        }

    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            showConnectingDialog();
        }

        @Override
        public void onProviderDisabled(String provider) {
            showLocationServicesDialog();
        }
    };

    boolean mIsStateAlreadySaved = false, mPendingShowDialog = false;
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mIsStateAlreadySaved = false;
        if(mPendingShowDialog){
            mPendingShowDialog = false;
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showLocationServicesDialog();
            } else {
                showConnectingDialog();
            }
        }
    }

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
        topNavigation.setSelectedItemId(R.id.topNav_navigation);

    }

    public void setTexts() {

        setDistText();

        double bearingBetweenPoints = bearingBetweenPoints(locations.get(locations.size()-1).getLatitude(),locations.get(locations.size()-1).getLongitude(),
                course.getCoords().get(selectedMark).getLatitude(),course.getCoords().get(selectedMark).getLongitude());

        // Display updated bearing to newly selected mark
        String bearingText = Math.round(bearingBetweenPoints) + "°";
        textView_bearing.setText(bearingText);

        gLView.setLocations(locations);
        gLView.setBearing(bearingDirection);
        gLView.setSelectedMark(selectedMark);
        redrawGLView();
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

    public void redrawGLView() {
        // Display new glview with new location

        gLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                gLView.redraw();
            }
        });
    }

    private double met2nm(double met) { return DoubleRounder.round(met / 1852,2);}
    private double met2km(double met) { return DoubleRounder.round(met / 1000, 2);}
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
    protected void onResume() {
        super.onResume();
        gLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gLView.onPause();
        mIsStateAlreadySaved = true;
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
                                ActivityCompat.requestPermissions(NavigationMap.this,
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
        if(mIsStateAlreadySaved){
            mPendingShowDialog = true;
        }else{
            DialogFragment dialog = new ConfirmDialogFragment();
            dialog.show(getSupportFragmentManager(),null);
        }
    }
    public void showLocationServicesDialog() {
        if(mIsStateAlreadySaved){
            mPendingShowDialog = true;
        }else{
            DialogFragment dialog = new LocationServicesFragment();
            dialog.show(getSupportFragmentManager(),null);
        }
    }
    public void showConnectingDialog() {
        if(mIsStateAlreadySaved){
            mPendingShowDialog = true;
        }else{
            DialogFragment dialog = new LocationConnectingFragment();
            dialog.show(getSupportFragmentManager(),null);
        }
    }
    public void showLocationNullDialog() {
        if(mIsStateAlreadySaved){
            mPendingShowDialog = true;
        }else{
            DialogFragment dialog = new LocationNullFragment();
            dialog.show(getSupportFragmentManager(),null);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDialogRetryLocation(DialogFragment dialog) {
        if (checkLocationPermission()) {
            updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
        if (locations.size() == 0) {
            showLocationNullDialog();
        }
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

    @Override
    public void onDialogLocationEnable(DialogFragment dialog) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onDialogLocationNotEnabled(DialogFragment dialog) {
        showLocationNullDialog();
    }
}
