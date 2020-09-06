package com.example.sailinglayoutapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
    RelativeLayout layoutGL;
    RadioGroup radioGroup;
    ArrayList<RadioButton> radioButtons;
    int selectedMark;
    double bearingDirection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_compass);

        Intent intent = getIntent();
        course = intent.getParcelableExtra("COURSE");
        courseSize = course.getCoords().size();

        locations = new ArrayList<>();
        textView_distance = new TextView(this);
        textView_bearing = new TextView(this);
        radioGroup = findViewById(R.id.rgNavCompass);
        radioButtons = new ArrayList<>();
        bearingDirection = 0;

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
    public void locationServicesEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) { }
        if ( !gps_enabled ){
            androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(this);
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            androidx.appcompat.app.AlertDialog alert = dialog.create();
            alert.show();
        }
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
}
