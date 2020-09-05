package com.example.sailinglayoutapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    ArrayList<RadioButton> radioButtons;
    int selectedMark;
    double bearingDirection;
    ImageView img_compass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_map);

        Intent intent = getIntent();
        course = intent.getParcelableExtra("COURSE");
        courseSize = course.getCoords().size();

        locations = new ArrayList<>();
        textView_distance = findViewById(R.id.text_NavMapDist);
        textView_bearing = findViewById(R.id.text_NavMapBear);
        radioGroup = findViewById(R.id.rgNavMap);
        radioButtons = new ArrayList<>();
        bearingDirection = 0;
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
            radioButtons.add(new RadioButton(this));
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

                double distBetweenPoints = met2nm(course.getCoords().get(selectedMark).distanceTo(locations.get(locations.size()-1)));
                double bearingBetweenPoints = DoubleRounder.round(locations.get(locations.size()-1).bearingTo(course.getCoords().get(selectedMark)),2);

                if (bearingBetweenPoints < 0) {
                    bearingBetweenPoints = 360 + bearingBetweenPoints;
                }
                // Display updated distance to newly selected mark
                String distText = distBetweenPoints + " Nm";
                textView_distance.setText(distText);

                // Display updated bearing to newly selected mark
                String bearingText = bearingBetweenPoints + "°";
                textView_bearing.setText(bearingText);

                gLView.setSelectedMark(selectedMark);
                layoutGL.removeView(gLView);
                layoutGL.addView(gLView);
            }
        });

        gLView = new NavMapGLSurfaceView(this, course.getCoords(), locations, selectedMark, bearingDirection);

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
            RelativeLayout layoutGL = (RelativeLayout) findViewById(R.id.rl_navigationMap);
            if (locations.size() >= 2) {
                locations.remove(0);
            }
            locations.add(location);

            if (locations.size() == 2) {
                bearingDirection = locations.get(0).bearingTo(locations.get(1));
            }

            double distBetweenPoints = met2nm(course.getCoords().get(selectedMark).distanceTo(locations.get(locations.size()-1)));

            double bearingBetweenPoints = DoubleRounder.round(locations.get(locations.size()-1).bearingTo(course.getCoords().get(selectedMark)),2);

            if (bearingBetweenPoints < 0) {
                bearingBetweenPoints = 360 + bearingBetweenPoints;
            }
            // Display new distance to selected mark
            String distText = distBetweenPoints + " Nm";
            textView_distance.setText(distText);


            // Display updated bearing to newly selected mark
            String bearingText = bearingBetweenPoints + "°";
            textView_bearing.setText(bearingText);

            // Display new glview with new location

            gLView.setLocations(locations);
            gLView.setBearing(bearingDirection);

            layoutGL.removeView(gLView);
            layoutGL.addView(gLView);

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

    private double met2nm(float met) { return DoubleRounder.round(met / 1852,2);}
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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
