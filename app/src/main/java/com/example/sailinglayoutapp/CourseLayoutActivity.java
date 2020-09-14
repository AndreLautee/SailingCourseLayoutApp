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
import androidx.core.content.ContextCompat;

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
    private LocationManager locationManager;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_layout);

        r1 = findViewById(R.id.radioButton_layout_1);
        r2 = findViewById(R.id.radioButton_layout_2);
        r3 = findViewById(R.id.radioButton_layout_3);
        r4 = findViewById(R.id.radioButton_layout_4);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        //set selected page
        bottomNavigation.setSelectedItemId(R.id.nav_layout);

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
                        intent.setClass(getApplicationContext(),NavigationCompassActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_map:
                        intent.putExtra("COURSE", markerCoordCalculations);
                        intent.setClass(getApplicationContext(),NavigationMap.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext(),
                                MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        cvObject = setCourseVariablesObject(extras);

        Location previousRefPoint = extras.getParcelable("LOCATION");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (previousRefPoint == null) {

            location = null;
            if(checkLocationPermission()) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

        } else {
            location = previousRefPoint;
        }



        textView_lat = findViewById(R.id.textView_lat);
        textView_lon = findViewById(R.id.textView_lon);
        if (location != null) {
            cvObject.setLat(location.getLatitude());
            cvObject.setLon(location.getLongitude());
            markerCoordCalculations = new MarkerCoordCalculations(cvObject);
            intent.putExtra("LOCATION", location);
            courseSize = markerCoordCalculations.getCoords().size();
            textView_lat.setText("Latitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(1).getLatitude()));
            textView_lon.setText("Longitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(1).getLongitude()));
            setDisplay(extras);
        } else {
            textView_lat.setText("Could not get coords");
            textView_lon.setText("Could not get coords");

            r1.setVisibility(View.INVISIBLE);
            r2.setVisibility(View.INVISIBLE);
            r3.setVisibility(View.INVISIBLE);
            r4.setVisibility(View.INVISIBLE);
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
                if (markerCoordCalculations != null) {
                    Intent intent = getIntent();
                    intent.putExtra("COURSE", markerCoordCalculations);
                    intent.setClass(getApplicationContext(),NavigationMap.class);
                    startActivity(intent);
                } else {
                    new AlertDialog.Builder(CourseLayoutActivity.this)
                            .setMessage("GPS not enabled. Location needed to proceed.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create()
                            .show();
                }

            }
        });
    }

    public void setDisplay(Bundle extras) {
        r1.setChecked(true);
        RelativeLayout.LayoutParams r1_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r1.setOnClickListener(radioButton_listener);

        RelativeLayout.LayoutParams r2_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r2.setOnClickListener(radioButton_listener);

        RelativeLayout.LayoutParams r3_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r3.setOnClickListener(radioButton_listener);

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

    public String decimalDeg2degMins(double decDegree) {
        int d = (int) decDegree;
        double m = (decDegree - d) * 60;
        return "" + d + "°" + String.format("%.3f",m) + "'";
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
                                ActivityCompat.requestPermissions(CourseLayoutActivity.this,
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


    View.OnClickListener radioButton_listener = new View.OnClickListener() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void onClick(View v) {
            if (r1.getId() == v.getId()) {
                r2.setChecked(false);
                r3.setChecked(false);
                r4.setChecked(false);
                textView_lat.setText("Latitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(1).getLatitude()));
                textView_lon.setText("Longitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(1).getLongitude()));
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
                textView_lat.setText("Latitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(i).getLatitude()));
                textView_lon.setText("Longitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(i).getLongitude()));
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
                textView_lat.setText("Latitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(i).getLatitude()));
                textView_lon.setText("Longitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(i).getLongitude()));
            }
            if (r4.getId() == v.getId()) {
                r1.setChecked(false);
                r2.setChecked(false);
                r3.setChecked(false);
                textView_lat.setText("Latitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(0).getLatitude()));
                textView_lon.setText("Longitude: " + decimalDeg2degMins(markerCoordCalculations.getCoords().get(0).getLongitude()));
            }
        }
    };

}

