package com.example.sailinglayoutapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

public class CourseLayoutActivity extends AppCompatActivity {

    RadioButton r1, r2, r3, r4;
    RelativeLayout rl;
    DrawView drawView;
    Location currentLocation;
    CourseVariablesObject cvObject;
    MarkerCoordCalculations markerCoordCalculations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_layout);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        cvObject = setCourseVariablesObject(extras);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationServicesEnabled(this);

        currentLocation = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        TextView textView_coords = findViewById(R.id.textView_coords);
        if (currentLocation != null) {
            markerCoordCalculations = new MarkerCoordCalculations(currentLocation, cvObject);
            textView_coords.setText(String.valueOf(markerCoordCalculations.getCoords().get(1).getLatitude()));
        } else {
            textView_coords.setText("Could not get coords");
        }

        r1 = findViewById(R.id.radioButton_layout_1);
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

                drawView = new DrawView(this, r1, r2);
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

                    drawView = new DrawView(this, r1, r2);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r3, r1);
                    rl.addView(drawView);
                    break;
                } else if (extras.getInt("TYPE") == 1) { // portboard

                    break;
                }
            case 2: // trapezoid
                if (extras.getInt("TYPE") == 0) { // if starboard
                    if (extras.getInt("SECOND_BEAT") == 0) { // if equal length second beat

                        break;
                    } else if (extras.getInt("SECOND_BEAT") == 1) { // if unequal length second beat

                        break;
                    }
                } else if (extras.getInt("TYPE") == 1) { // if portboard
                    if (extras.getInt("SECOND_BEAT") == 0) { // if equal length second beat

                        break;
                    } else if (extras.getInt("SECOND_BEAT") == 1) { // if unequal length second beat

                        break;
                    }
                }
            case 3: // optimist
                if (extras.getInt("TYPE") == 0) { // if starboard

                    break;
                } else if (extras.getInt("TYPE") == 1) { // if portboard

                   break;
                }
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
        /*
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        assert extras != null;
        variables = setVariableArray(extras);


        LinearLayout layoutCourseLayout = (LinearLayout) findViewById(R.id.layout_courseLayout);

        Button button_navigate = new Button(this);
        Button button_back = new Button(this);
        button_navigate.setText("Navigate");
        button_back.setText("Back");

        layoutCourseLayout.addView(button_navigate);
        layoutCourseLayout.addView(button_back);

        RelativeLayout layoutGL = (RelativeLayout) findViewById(R.id.layout_GL);

        gLView = new LayoutGLSurfaceView(this, variables);

        layoutGL.addView(gLView);

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= getIntent();
                intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
                startActivity(intent);
            }
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

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

    View.OnClickListener radioButton_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (r1.getId() != v.getId()) {
                r1.setChecked(false);
            }
            if (r2.getId() != v.getId()) {
                r2.setChecked(false);
            }
            if (r3.getId() != v.getId()) {
                r3.setChecked(false);
            }
            if (r4.getId() != v.getId()) {
                r4.setChecked(false);
            }
        }
    };

}

