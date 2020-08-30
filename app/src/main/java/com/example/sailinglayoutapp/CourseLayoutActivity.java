package com.example.sailinglayoutapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CourseLayoutActivity extends AppCompatActivity {

    RadioButton r1, r2, r3, r4;
    RelativeLayout rl;
    DrawView drawView;
    Location location;
    CourseVariablesObject cvObject;
    MarkerCoordCalculations markerCoordCalculations;
    TextView textView_lat, textView_lon;
    int courseSize;
    BottomNavigationView bottomNavigation;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_layout);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        //set selected page
        bottomNavigation.setSelectedItemId(R.id.nav_variables);

        //perform ItemSelectedListener
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = getIntent();
                switch (item.getItemId()){
                    case R.id.nav_variables:
                        intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_layout:
                        return true;
                    case R.id.nav_compass:
                        intent.putExtra("COURSE", markerCoordCalculations);
                        //intent.setClass(getApplicationContext(),NavigationCompass.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_map:
                        intent.putExtra("COURSE", markerCoordCalculations);
                        intent.setClass(getApplicationContext(),NavigationMap.class);
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        cvObject = setCourseVariablesObject(extras);

        Location previousRefPoint = extras.getParcelable("LOCATION");

        if (previousRefPoint == null) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationServicesEnabled(this);

            location = null;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                final int REQUEST_LOCATION = 2;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            location = previousRefPoint;
        }



        textView_lat = findViewById(R.id.textView_lat);
        textView_lon = findViewById(R.id.textView_lon);
        if (location != null) {
            markerCoordCalculations = new MarkerCoordCalculations(location, cvObject);
            intent.putExtra("LOCATION", location);
            courseSize = markerCoordCalculations.getCoords().size();
            textView_lat.setText("Latitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(1).getLatitude()));
            textView_lon.setText("Longitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(1).getLongitude()));
            setDisplay(extras);
        } else {
            textView_lat.setText("Could not get coords");
            textView_lon.setText("Could not get coords");
            // Display an error
        }



        Button button_back = findViewById(R.id.button_back);
        Button button_navigation = findViewById(R.id.button_navigation);

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
                startActivity(intent);
            }
        });

        button_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("COURSE", markerCoordCalculations);
                intent.setClass(getApplicationContext(),NavigationMap.class);
                startActivity(intent);
            }
        });
    }

    public void setDisplay(Bundle extras) {
        r1 = findViewById(R.id.radioButton_layout_1);
        r1.setChecked(true);
        RelativeLayout.LayoutParams r1_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r1.setOnClickListener(radioButton_listener);

        r2 = findViewById(R.id.radioButton_layout_2);
        RelativeLayout.LayoutParams r2_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r2.setOnClickListener(radioButton_listener);

        r3 = findViewById(R.id.radioButton_layout_3);
        RelativeLayout.LayoutParams r3_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r3.setOnClickListener(radioButton_listener);

        r4 = findViewById(R.id.radioButton_layout_4);
        RelativeLayout.LayoutParams r4_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r4.setOnClickListener(radioButton_listener);

        rl = findViewById(R.id.rl_courseLayout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        switch (extras.getInt("SHAPE")) {
            case 0: // windward-leeward
                r3.setVisibility(View.INVISIBLE);
                r4.setVisibility(View.INVISIBLE); // Only two points needed so make r3 and r4 invisible

                r1_layoutParams.setMargins(width/2, height/5, 0,0);
                r1.setLayoutParams(r1_layoutParams);

                r2_layoutParams.setMargins(width/2, (3*height)/5, 0,0);
                r2.setLayoutParams(r2_layoutParams);

                drawView = new DrawView(this, r1, r2, width);
                rl.addView(drawView);
                break;
            case 1: // triangle
                r4.setVisibility(View.INVISIBLE); // only three points needed so make r4 invisible
                if (extras.getInt("TYPE") == 0) { // starboard

                    r1_layoutParams.setMargins(width/6, height/5,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                    r2_layoutParams.setMargins((4*width)/6, (2*height)/5, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                    r3_layoutParams.setMargins(width/6,(3*height)/5, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    drawView = new DrawView(this, r1, r2, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r3, r1, width);
                    rl.addView(drawView);
                    break;
                } else if (extras.getInt("TYPE") == 1) { // portboard

                    r1_layoutParams.setMargins((4*width)/6, height/5,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                    r2_layoutParams.setMargins(width/6, (2*height)/5, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                    r3_layoutParams.setMargins((4*width)/6, (3*height)/5, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    drawView = new DrawView(this, r1, r2, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r3, r1, width);
                    rl.addView(drawView);
                    break;
                }
            case 2: // trapezoid
                if (extras.getInt("TYPE") == 0) { // if starboard
                    if (extras.getInt("SECOND_BEAT") == 0) { // if equal length second beat

                        r1_layoutParams.setMargins(width/6, (3*height)/20,0,0);
                        r1.setLayoutParams(r1_layoutParams);

                        r2_layoutParams.setMargins((4*width)/6, (5*height)/20, 0,0);
                        r2.setLayoutParams(r2_layoutParams);

                        r3_layoutParams.setMargins((4*width)/6, (13*height)/20, 0, 0);
                        r3.setLayoutParams(r3_layoutParams);

                        r4_layoutParams.setMargins(width/6, (11*height)/20, 0, 0);
                        r4.setLayoutParams(r4_layoutParams);

                        drawView = new DrawView(this, r1, r2, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r2, r3, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r3, r4, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r4, r1, width);
                        rl.addView(drawView);
                        break;
                    } else if (extras.getInt("SECOND_BEAT") == 1) { // if unequal length second beat

                        r1_layoutParams.setMargins(width/6, height/5,0,0);
                        r1.setLayoutParams(r1_layoutParams);

                        r2_layoutParams.setMargins((4*width)/6, (3*height)/10, 0,0);
                        r2.setLayoutParams(r2_layoutParams);

                        r3_layoutParams.setMargins((4*width)/6, (5*height)/10, 0, 0);
                        r3.setLayoutParams(r3_layoutParams);

                        r4_layoutParams.setMargins(width/6, (3*height)/5, 0, 0);
                        r4.setLayoutParams(r4_layoutParams);

                        drawView = new DrawView(this, r1, r2, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r2, r3, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r3, r4, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r4, r1, width);
                        rl.addView(drawView);
                        break;
                    }
                } else if (extras.getInt("TYPE") == 1) { // if portboard
                    if (extras.getInt("SECOND_BEAT") == 0) { // if equal length second beat

                        r1_layoutParams.setMargins((4*width)/6, (3*height)/20,0,0);
                        r1.setLayoutParams(r1_layoutParams);

                        r2_layoutParams.setMargins(width/6, (5*height)/20, 0,0);
                        r2.setLayoutParams(r2_layoutParams);

                        r3_layoutParams.setMargins(width/6, (13*height)/20, 0, 0);
                        r3.setLayoutParams(r3_layoutParams);

                        r4_layoutParams.setMargins((4*width)/6, (11*height)/20, 0, 0);
                        r4.setLayoutParams(r4_layoutParams);

                        drawView = new DrawView(this, r1, r2, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r2, r3, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r3, r4, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r4, r1, width);
                        rl.addView(drawView);
                        break;
                    } else if (extras.getInt("SECOND_BEAT") == 1) { // if unequal length second beat

                        r1_layoutParams.setMargins((4*width)/6, height/5,0,0);
                        r1.setLayoutParams(r1_layoutParams);

                        r2_layoutParams.setMargins(width/6, (3*height)/10, 0,0);
                        r2.setLayoutParams(r2_layoutParams);

                        r3_layoutParams.setMargins(width/6, (5*height)/10, 0, 0);
                        r3.setLayoutParams(r3_layoutParams);

                        r4_layoutParams.setMargins((4*width)/6, (3*height)/5, 0, 0);
                        r4.setLayoutParams(r4_layoutParams);

                        drawView = new DrawView(this, r1, r2, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r2, r3, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r3, r4, width);
                        rl.addView(drawView);
                        drawView = new DrawView(this, r4, r1, width);
                        rl.addView(drawView);
                        break;
                    }
                }
            case 3: // optimist
                if (extras.getInt("TYPE") == 0) { // if starboard

                    r1_layoutParams.setMargins(width/8, height/5,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                    r2_layoutParams.setMargins((6*width)/8, (6*height)/20, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                    r3_layoutParams.setMargins((6*width)/8, (13*height)/20, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    r4_layoutParams.setMargins(width/8, (11*height)/20, 0, 0);
                    r4.setLayoutParams(r4_layoutParams);

                    drawView = new DrawView(this, r1, r2, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r4, r1, width);
                    rl.addView(drawView);
                    break;
                } else if (extras.getInt("TYPE") == 1) { // if portboard

                    r1_layoutParams.setMargins((6*width)/8, height/5,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                    r2_layoutParams.setMargins(width/8, (6*height)/20, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                    r3_layoutParams.setMargins(width/8, (13*height)/20, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    r4_layoutParams.setMargins((6*width)/8, (11*height)/20, 0, 0);
                    r4.setLayoutParams(r4_layoutParams);

                    drawView = new DrawView(this, r1, r2, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r4, r1, width);
                    rl.addView(drawView);
                    break;
                }
        }
    }

    // Extract variables from bundle to create an object
    public CourseVariablesObject setCourseVariablesObject(Bundle extras) {
        String type = null;
        String shape = null;
        double bearing;
        double distance;
        int angle = 0;
        double reach = 0;
        String secondBeat = null;

        if (extras.getInt("TYPE") == 0) {
            type = "starboard";
        } else if (extras.getInt("TYPE") == 1){
            type = "portboard";
        }

        bearing = extras.getDouble("BEARING");
        distance = extras.getDouble("DISTANCE");

        switch (extras.getInt("SHAPE")) {
            case 0:
                shape = "windward_leeward";
                break;
            case 1:
                shape = "triangle";
                if (extras.getInt("ANGLE") == 0) {
                    angle = 60;
                } else if (extras.getInt("ANGLE") == 1) {
                    angle = 45;
                }
                break;
            case 2:
                shape = "trapezoid";
                if (extras.getInt("ANGLE") == 0) {
                    angle = 60;
                } else if (extras.getInt("ANGLE") == 1) {
                    angle = 70;
                }
                if (extras.getInt("REACH") == 0) {
                    reach = 0.5;
                } else if (extras.getInt("REACH") == 1) {
                    reach = 0.66;
                }
                if (extras.getInt("SECOND_BEAT") == 0) {
                    secondBeat = "long";
                } else if (extras.getInt("SECOND_BEAT") == 1) {
                    secondBeat = "short";
                }
                break;
            case 3:
                shape = "optimist";
                break;
        }

        CourseVariablesObject result = new CourseVariablesObject(type, shape, bearing, distance, angle, reach, secondBeat);
        return result;
    }

    public void locationServicesEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) { }
        if ( !gps_enabled ){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();
        }
    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

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

    View.OnClickListener radioButton_listener = new View.OnClickListener() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void onClick(View v) {
            if (r1.getId() == v.getId()) {
                r2.setChecked(false);
                r3.setChecked(false);
                r4.setChecked(false);
                textView_lat.setText("Latitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(1).getLatitude()));
                textView_lon.setText("Longitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(1).getLongitude()));
            }
            if (r2.getId() == v.getId()) {
                r1.setChecked(false);
                r3.setChecked(false);
                r4.setChecked(false);
                int i;
                if (courseSize == 2) {
                    i = 0;
                } else {
                    i = 2;
                }
                textView_lat.setText("Latitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(i).getLatitude()));
                textView_lon.setText("Longitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(i).getLongitude()));
            }
            if (r3.getId() == v.getId()) {
                r1.setChecked(false);
                r2.setChecked(false);
                r4.setChecked(false);
                int i;
                if (courseSize == 3) {
                    i = 0;
                } else {
                    i = 3;
                }
                textView_lat.setText("Latitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(i).getLatitude()));
                textView_lon.setText("Longitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(i).getLongitude()));
            }
            if (r4.getId() == v.getId()) {
                r1.setChecked(false);
                r2.setChecked(false);
                r3.setChecked(false);
                textView_lat.setText("Latitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(0).getLatitude()));
                textView_lon.setText("Longitude: " + String.format("%.5f",markerCoordCalculations.getCoords().get(0).getLongitude()));
            }
        }
    };

}

