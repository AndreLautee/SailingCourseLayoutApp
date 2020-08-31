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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_map);

        Intent intent = getIntent();
        course = intent.getParcelableExtra("COURSE");
        courseSize = course.getCoords().size();

        locations = new ArrayList<>();
        textView_distance = new TextView(this);
        textView_bearing = new TextView(this);
        radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        radioButtons = new ArrayList<>();
        bearingDirection = 0;

        layoutCourseLayout = (LinearLayout) findViewById(R.id.layout_navigationMap);
        layoutGL = (RelativeLayout) findViewById(R.id.rl_navigationMap);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationServicesEnabled(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, locationListener);

        locations.add(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

        // Set number of radio buttons to number of marks

        for(int i = 0; i < courseSize; i++){
            radioButtons.add(new RadioButton(this));
            radioButtons.get(i).setId(i+1);
            radioGroup.removeView(radioButtons.get(i));
            radioGroup.addView(radioButtons.get(i)); //the RadioButtons are added to the radioGroup instead of the layout
            radioButtons.get(i).setText("Mark " + (i+1));
        }
        radioGroup.check(radioButtons.get(courseSize-1).getId());

        layoutCourseLayout.removeView(radioGroup);
        layoutCourseLayout.addView(radioGroup);

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

                // Display updated distance to newly selected mark
                String distText = "Distance to selected mark: " + distBetweenPoints + "Nm";
                textView_distance.setText(distText);
                layoutCourseLayout.removeView(textView_distance);
                layoutCourseLayout.addView(textView_distance);

                // Display updated bearing to newly selected mark
                String bearingText = "Bearing to selected mark: " + bearingBetweenPoints + "°";
                textView_bearing.setText(bearingText);
                layoutCourseLayout.removeView(textView_bearing);
                layoutCourseLayout.addView(textView_bearing);

                gLView.setSelectedMark(selectedMark);
                layoutGL.removeView(gLView);
                layoutGL.addView(gLView);
            }
        });

        gLView = new NavMapGLSurfaceView(this, course.getCoords(), locations, selectedMark, bearingDirection);

        layoutGL.addView(gLView);

    }

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

            // Display new distance to selected mark
            String distText = "Distance to selected mark: " + distBetweenPoints + "Nm";
            textView_distance.setText(distText);
            layoutCourseLayout.removeView(textView_distance);
            layoutCourseLayout.addView(textView_distance);

            // Display updated bearing to newly selected mark
            String bearingText = "Bearing to selected mark: " + bearingBetweenPoints + "°";
            textView_bearing.setText(bearingText);
            layoutCourseLayout.removeView(textView_bearing);
            layoutCourseLayout.addView(textView_bearing);

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

    public double distanceBetweenPoints(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 0.8684; // convert to nautical miles

        return DoubleRounder.round(dist,2);
    }

    public double bearingBetweenPoints(double lat1, double lon1, double lat2, double lon2){
        double longDiff = deg2rad(lon2) - deg2rad(lon1);
        double latitude1 = deg2rad(lat1);
        double latitude2 = deg2rad(lat2);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);
        double rad = Math.atan2(y,x);
        double result = (rad2deg(rad)+360) % 360;


        return DoubleRounder.round(result,2);
    }

    private double met2nm(float met) { return DoubleRounder.round(met / 1852,2);}
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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
}
