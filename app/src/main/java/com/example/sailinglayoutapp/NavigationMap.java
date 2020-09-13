package com.example.sailinglayoutapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.decimal4j.util.DoubleRounder;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class NavigationMap extends AppCompatActivity {

    NavMapGLSurfaceView gLView;
    MarkerCoordCalculations course;
    int courseSize;
    ArrayList<Location> locations;
    TextView textView_distance, textView_bearing;
    LinearLayout layoutCourseLayout;
    RelativeLayout layoutGL;
    RadioGroup radioGroup;
    ArrayList<AppCompatRadioButton> radioButtons;
    int selectedMark;
    double bearingDirection;
    ImageView img_compass;
    BottomNavigationView bottomNavigation;

    @SuppressLint({"ResourceAsColor", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_map);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        //set selected page
        bottomNavigation.setSelectedItemId(R.id.nav_map);

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
                        intent.setClass(getApplicationContext(),CourseLayoutActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_compass:
                        intent.setClass(getApplicationContext(),NavigationMap.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_map:
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
        course = intent.getParcelableExtra("COURSE");
        courseSize = course.getCoords().size();


        locations = new ArrayList<>();
        textView_distance = findViewById(R.id.text_NavMapDist);
        textView_bearing = findViewById(R.id.text_NavMapBear);
        radioGroup = findViewById(R.id.rgNavMap);
        radioButtons = new ArrayList<>();
        float courseBearing = (float) - rad2deg(course.getCourseVariablesObject().getBearing());
        bearingDirection = -courseBearing;
        img_compass = findViewById(R.id.img_NavMapCompass);

        layoutCourseLayout = (LinearLayout) findViewById(R.id.layout_navigationMap);
        layoutGL = (RelativeLayout) findViewById(R.id.rl_navigationMap);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(checkLocationPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, locationListener);
            locations.add(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }

        // Set number of radio buttons to number of marks

        for(int i = 0; i < courseSize; i++){
            radioButtons.add(new AppCompatRadioButton(this));
            radioButtons.get(i).setId(i+1);
            radioGroup.removeView(radioButtons.get(i));
            radioGroup.addView(radioButtons.get(i)); //the RadioButtons are added to the radioGroup instead of the layout
            radioButtons.get(i).setText("Mark " + (i+1));
            radioButtons.get(i).setTextSize(20);
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

        gLView = new NavMapGLSurfaceView(this, course.getCoords(), locations, selectedMark, bearingDirection);

        img_compass.setRotation(courseBearing);
        gLView.setmAngle(courseBearing);

        layoutGL.addView(gLView);

        gLView.setOnTouchListener(rotationListener);

        img_compass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gLView.getmAngle() != 0) {
                    img_compass.setRotation(0);
                    gLView.setmAngle(0);
                }
            }
        });
    }

    View.OnTouchListener rotationListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            img_compass.setRotation(gLView.getmAngle());
            return false;
        }
    };

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

    public void setTexts() {
        double distBetweenPoints = met2nm(course.getCoords().get(selectedMark).distanceTo(locations.get(locations.size()-1)));

        double bearingBetweenPoints = bearingBetweenPoints(locations.get(locations.size()-1).getLatitude(),locations.get(locations.size()-1).getLongitude(),
                course.getCoords().get(selectedMark).getLatitude(),course.getCoords().get(selectedMark).getLongitude());

        // Display new distance to selected mark
        String distText = distBetweenPoints + " Nm";
        textView_distance.setText(distText);


        // Display updated bearing to newly selected mark
        String bearingText = DoubleRounder.round(bearingBetweenPoints, 2) + "°";
        textView_bearing.setText(bearingText);

        // Display new glview with new location

        gLView.setLocations(locations);
        gLView.setBearing(bearingDirection);
        gLView.setSelectedMark(selectedMark);

        gLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                gLView.redraw();
            }
        });
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
    protected void onResume() {
        super.onResume();
        gLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gLView.onPause();
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
}
