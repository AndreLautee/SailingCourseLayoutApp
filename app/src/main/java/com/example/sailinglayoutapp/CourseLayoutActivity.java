package com.example.sailinglayoutapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CourseLayoutActivity extends AppCompatActivity implements ConfirmDialogFragment.ConfirmDialogListener, SharedPreferences.OnSharedPreferenceChangeListener {

    RadioButton r1, r2, r3, r4;
    RelativeLayout rl;
    DrawView drawView;
    Location currentLocation;
    CourseVariablesObject cvObject;
    MarkerCoordCalculations markerCoordCalculations;
    TextView textView_lat, textView_lon, markID;
    int courseSize;
    BottomNavigationView topNavigation;
    private LocationManager locationManager;
    int selectedMark;
    String coordFormat;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Course Layout");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_black_24dp);
        }

        topNavigation = findViewById(R.id.courseLayout_top_navigation);


        //set selected page
        topNavigation.setSelectedItemId(R.id.topNav_layout);

        //perform ItemSelectedListener
        topNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                switch (item.getItemId()){

                    case R.id.topNav_layout:
                        return true;
                    case R.id.topNav_compass:
                        if (markerCoordCalculations != null && cvObject != null) {
                            Intent intent = new Intent();
                            intent.putExtra("COURSE", markerCoordCalculations);
                            intent.putExtra("SELECTED_MARK", selectedMark);
                            intent.putExtra("COURSE_VARIABLES", cvObject);
                            intent.setClass(getApplicationContext(),NavigationCompassActivity.class);
                            startActivity(intent);
                        } else { }
                        return true;
                    case R.id.topNav_navigation:
                        if (markerCoordCalculations != null && cvObject != null) {
                            Intent intent = new Intent();
                            intent.putExtra("COURSE", markerCoordCalculations);
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


        r1 = findViewById(R.id.radioButton_layout_1);
        r2 = findViewById(R.id.radioButton_layout_2);
        r3 = findViewById(R.id.radioButton_layout_3);
        r4 = findViewById(R.id.radioButton_layout_4);

        Intent intent = getIntent();

        cvObject = intent.getParcelableExtra("COURSE_VARIABLES");

        selectedMark = intent.getIntExtra("SELECTED_MARK",1);

        markerCoordCalculations = intent.getParcelableExtra("COURSE");
        if (markerCoordCalculations == null) {
            markerCoordCalculations = new MarkerCoordCalculations(cvObject);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        currentLocation = null;
        if(checkLocationPermission()) {
           locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, locationListener);
           currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        setupSharedPreferences();

        textView_lat = findViewById(R.id.courseLayout_lat);
        textView_lon = findViewById(R.id.courseLayout_lon);
        markID = findViewById(R.id.courseLayout_markID);

        if (markerCoordCalculations != null) {
            courseSize = markerCoordCalculations.getCoords().size();
            setText();
            setDisplay();
        } else {
            textView_lat.setText("Could not get coords");
            textView_lon.setText("Could not get coords");

            r1.setVisibility(View.INVISIBLE);
            r2.setVisibility(View.INVISIBLE);
            r3.setVisibility(View.INVISIBLE);
            r4.setVisibility(View.INVISIBLE);
            // Display an error
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        topNavigation.setSelectedItemId(R.id.topNav_layout);

    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        coordFormat = sharedPreferences.getString("coordinates", "deg_min");
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
                intent.putExtra("LOCATION", currentLocation);
                intent.setClass(getApplicationContext(),WeatherAPIActivity.class);
                startActivity(intent);
                return true;

            case R.id.btn_settings:
                intent = new Intent();
                intent.setClass(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void setDisplay() {
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

        switch (cvObject.getShape()) {
            case "windward_leeward": // windward-leeward
                r3.setVisibility(View.INVISIBLE);
                r4.setVisibility(View.INVISIBLE); // Only two points needed so make r3 and r4 invisible

                r1_layoutParams.setMargins((width/2)-70, (height)/20, 0,0);
                r1.setLayoutParams(r1_layoutParams);

                r2_layoutParams.setMargins((width/2)-70, (9*height)/20, 0,0);
                r2.setLayoutParams(r2_layoutParams);

                drawView = new DrawView(this, r1, r2, width);
                rl.addView(drawView);
                break;
            case "triangle": // triangle
                r4.setVisibility(View.INVISIBLE); // only three points needed so make r4 invisible
                if (cvObject.getType().equals("starboard")) { // starboard

                    r1_layoutParams.setMargins(width/6, (height)/20,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                    r2_layoutParams.setMargins((4*width)/6, (5*height)/20, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                    r3_layoutParams.setMargins(width/6,(9*height)/20, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    drawView = new DrawView(this, r1, r2, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r3, r1, width);
                    rl.addView(drawView);
                    break;
                } else if (cvObject.getType().equals("portboard")) { // portboard

                    r1_layoutParams.setMargins((4*width)/6, (height)/20,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                    r2_layoutParams.setMargins(width/6, (5*height)/20, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                    r3_layoutParams.setMargins((4*width)/6, (9*height)/20, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    drawView = new DrawView(this, r1, r2, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r3, r1, width);
                    rl.addView(drawView);
                    break;
                }
            case "trapezoid": // trapezoid
                if (cvObject.getType().equals("starboard")) { // if starboard
                    if (cvObject.getSecondBeat().equals("long")) { // if equal length second beat

                        r1_layoutParams.setMargins(width/6, (height)/20,0,0);
                        r1.setLayoutParams(r1_layoutParams);

                        r2_layoutParams.setMargins((4*width)/6, (3*height)/20, 0,0);
                        r2.setLayoutParams(r2_layoutParams);

                        r3_layoutParams.setMargins((4*width)/6, (11*height)/20, 0, 0);
                        r3.setLayoutParams(r3_layoutParams);

                        r4_layoutParams.setMargins(width/6, (9*height)/20, 0, 0);
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
                    } else if (cvObject.getSecondBeat().equals("short")) { // if unequal length second beat

                        r1_layoutParams.setMargins(width/6, (height)/20,0,0);
                        r1.setLayoutParams(r1_layoutParams);

                        r2_layoutParams.setMargins((4*width)/6, (3*height)/20, 0,0);
                        r2.setLayoutParams(r2_layoutParams);

                        r3_layoutParams.setMargins((4*width)/6, (7*height)/20, 0, 0);
                        r3.setLayoutParams(r3_layoutParams);

                        r4_layoutParams.setMargins(width/6, (9*height)/20, 0, 0);
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
                } else if (cvObject.getType().equals("portboard")) { // if portboard
                    if (cvObject.getSecondBeat().equals("long")) { // if equal length second beat

                        r1_layoutParams.setMargins((4*width)/6, (height)/20,0,0);
                        r1.setLayoutParams(r1_layoutParams);

                        r2_layoutParams.setMargins(width/6, (3*height)/20, 0,0);
                        r2.setLayoutParams(r2_layoutParams);

                        r3_layoutParams.setMargins(width/6, (11*height)/20, 0, 0);
                        r3.setLayoutParams(r3_layoutParams);

                        r4_layoutParams.setMargins((4*width)/6, (9*height)/20, 0, 0);
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
                    } else if (cvObject.getSecondBeat().equals("short")) { // if unequal length second beat

                        r1_layoutParams.setMargins((4*width)/6, (height)/20,0,0);
                        r1.setLayoutParams(r1_layoutParams);

                        r2_layoutParams.setMargins(width/6, (3*height)/20, 0,0);
                        r2.setLayoutParams(r2_layoutParams);

                        r3_layoutParams.setMargins(width/6, (7*height)/20, 0, 0);
                        r3.setLayoutParams(r3_layoutParams);

                        r4_layoutParams.setMargins((4*width)/6, (9*height)/20, 0, 0);
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
            case "optimist": // optimist
                if (cvObject.getType().equals("starboard")) { // if starboard

                    r1_layoutParams.setMargins(width/8, (height)/20,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                    r2_layoutParams.setMargins((6*width)/8, (3*height)/20, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                    r3_layoutParams.setMargins((6*width)/8, (10*height)/20, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    r4_layoutParams.setMargins(width/8, (8*height)/20, 0, 0);
                    r4.setLayoutParams(r4_layoutParams);

                    drawView = new DrawView(this, r1, r2, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3, width);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r4, r1, width);
                    rl.addView(drawView);
                    break;
                } else if (cvObject.getType().equals("portboard")) { // if portboard

                    r1_layoutParams.setMargins((6*width)/8, (height)/20,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                    r2_layoutParams.setMargins(width/8, (3*height)/20, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                    r3_layoutParams.setMargins(width/8, (10*height)/20, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    r4_layoutParams.setMargins((6*width)/8, (8*height)/20, 0, 0);
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


    private String decimalDeg(double decDegree) {
        return "" + String.format("%.4f", decDegree) + "°";
    }

    private String decimalDeg2degMins(double decDegree) {
        int d = (int) decDegree;
        double m = Math.abs(decDegree - d) * 60;
        return "" + d + "°" + String.format("%.3f",m) + "'";
    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
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
                selectedMark = 1;
                setText();

            }
            if (r2.getId() == v.getId()) {
                r1.setChecked(false);
                r3.setChecked(false);
                r4.setChecked(false);
                if (courseSize == 2) {
                    selectedMark = 0;
                } else {
                    selectedMark = 2;
                }
                setText();
            }
            if (r3.getId() == v.getId()) {
                r1.setChecked(false);
                r2.setChecked(false);
                r4.setChecked(false);
                if (courseSize == 3) {
                    selectedMark = 0;
                } else {
                    selectedMark = 3;
                }
                setText();
            }
            if (r4.getId() == v.getId()) {
                r1.setChecked(false);
                r2.setChecked(false);
                r3.setChecked(false);
                selectedMark = 0;
                setText();
            }
        }
    };

    private void setText() {
        int i;
        switch(selectedMark) {
            case 1:
                setLatText(markerCoordCalculations.getCoords().get(1).getLatitude());
                setLonText(markerCoordCalculations.getCoords().get(1).getLongitude());
                markID.setText("MARK 1");
                r1.setChecked(true);
                r2.setChecked(false);
                r3.setChecked(false);
                r4.setChecked(false);
                break;
            case 2:
                if (courseSize == 2) {
                    i = 0;
                } else {
                    i = 2;
                }
                setLatText(markerCoordCalculations.getCoords().get(i).getLatitude());
                setLonText(markerCoordCalculations.getCoords().get(i).getLongitude());
                markID.setText("MARK 2");
                r1.setChecked(false);
                r2.setChecked(true);
                r3.setChecked(false);
                r4.setChecked(false);
                break;
            case 3:
                if (courseSize == 3) {
                    i = 0;
                } else {
                    i = 3;
                }
                setLatText(markerCoordCalculations.getCoords().get(i).getLatitude());
                setLonText(markerCoordCalculations.getCoords().get(i).getLongitude());
                markID.setText("MARK 3");
                r1.setChecked(false);
                r2.setChecked(false);
                r3.setChecked(true);
                r4.setChecked(false);
                break;
            case 0:
                setLatText(markerCoordCalculations.getCoords().get(0).getLatitude());
                setLonText(markerCoordCalculations.getCoords().get(0).getLongitude());
                markID.setText("MARK " + courseSize);
                r1.setChecked(false);
                r2.setChecked(false);
                r3.setChecked(false);
                r4.setChecked(false);
                switch (courseSize) {
                    case 2:
                        r2.setChecked(true);
                        break;
                    case 3:
                        r3.setChecked(true);
                        break;
                    case 4:
                        r4.setChecked(true);
                        break;
                }
                break;
        }
    }

    private void setLatText(double latDeg){
        if (coordFormat.equals("deg")) {
            textView_lat.setText("" + decimalDeg(latDeg));
        } else {
            textView_lat.setText("" + decimalDeg2degMins(latDeg));
        }
    }

    private void setLonText(double lonDeg) {
        if (coordFormat.equals("deg")) {
            textView_lon.setText("" + decimalDeg(lonDeg));
        } else {
            textView_lon.setText("" + decimalDeg2degMins(lonDeg));
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
        if (key.equals("coordinates")) {
            coordFormat = sharedPreferences.getString(key, "deg_min");
            setText();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}

