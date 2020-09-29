package com.example.sailinglayoutapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;

import org.decimal4j.util.DoubleRounder;
import org.json.JSONException;
import org.json.JSONObject;

public class CourseVariablesBackdropActivity extends AppCompatActivity
        implements WeatherDialogFragment.WeatherDialogListener, HelpDialogFragment.HelpDialogListener, ConfirmDialogFragment.ConfirmDialogListener {

    BottomSheetBehavior<LinearLayout> sheetBehavior;
    CourseVariablesObject cvObject;
    String shape;
    View calcButton, stubView, arrow, topBar, helpButton;
    EditText txtLat, txtLon, txtWind, txtDist;
    TextInputLayout txtLayLat, txtLayLon, txtLayWind, txtLayDist;
    RadioGroup rgAngle, rgType, rg2ndBeat, rgReach;
    RadioButton rbAngle1, rbAngle2, rbType1, rbType2, rb2ndBeat1, rb2ndBeat2, rbReach1, rbReach2;
    Location currentLocation;
    int prevActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_variables_backdrop);

        setupUI(findViewById(R.id.ui_behind_backdrop));

        Intent intent = getIntent();
        prevActivity = intent.getIntExtra("PREV_ACTIVITY",0);
        cvObject = intent.getParcelableExtra("COURSE_VARIABLES");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Course Variables");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_black_24dp);
        }



        LinearLayout contentLayout = findViewById(R.id.contentLayout);
        ViewStub stub = findViewById(R.id.layout_stub);

        shape = cvObject.getShape();
        switch(shape){
            case "triangle":
                stub.setLayoutResource(R.layout.bottom_sheet_triangle);
                break;
            case "trapezoid":
                stub.setLayoutResource(R.layout.bottom_sheet_trapezoid);
                break;
            case "windward_leeward":
                stub.setLayoutResource(R.layout.bottom_sheet_windward);
                break;
            case "optimist":
                stub.setLayoutResource(R.layout.bottom_sheet_optimist);
                break;
            default:
        }
        stubView = stub.inflate();

        assignViews();
        if(cvObject.getDistance() != 0.0f) {
            rewriteOldValues();
        }

        sheetBehavior = BottomSheetBehavior.from(contentLayout);
        sheetBehavior.setFitToContents(false);
        sheetBehavior.setHideable(false);//prevents the boottom sheet from completely hiding off the screen
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);//initially state to fully expanded
        sheetBehavior.setHalfExpandedRatio(0.2f);

        arrow = findViewById(R.id.filterIcon);
        topBar = findViewById(R.id.topBar);

        topBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                if(sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        //#3 Listening to State Changes of BottomSheet
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                    arrow.setRotation(180);
                }
                if(newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    arrow.setRotation(0);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        currentLocation = null;

        if(checkLocationPermission()) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        final View button_locate = findViewById(R.id.locate_button);
        button_locate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                if (currentLocation == null) {
                    if(checkLocationPermission()) {
                        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    } else {
                        new AlertDialog.Builder(CourseVariablesBackdropActivity.this)
                                .setMessage("GPS not enabled. Location needed to see weather data.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //Prompt the user once explanation has been shown
                                        ActivityCompat.requestPermissions(CourseVariablesBackdropActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                REQUEST_LOCATION);
                                    }
                                })
                                .create()
                                .show();
                    }
                } else {
                    txtLat.setText(String.valueOf(DoubleRounder.round(currentLocation.getLatitude(),5)));
                    txtLon.setText(String.valueOf(DoubleRounder.round(currentLocation.getLongitude(),5)));
                }

            }
        });

        final View button_weather = findViewById(R.id.weather_button);
        button_weather.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                if (currentLocation == null) {
                    if(checkLocationPermission()) {
                        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    } else {
                        new AlertDialog.Builder(CourseVariablesBackdropActivity.this)
                                .setMessage("GPS not enabled. Location needed to see weather data.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //Prompt the user once explanation has been shown
                                        ActivityCompat.requestPermissions(CourseVariablesBackdropActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                REQUEST_LOCATION);
                                    }
                                })
                                .create()
                                .show();
                    }
                } else {

                    showWeatherDialog();

                }

            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });

        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fillCVObject()) {
                    Intent intent = new Intent(getApplicationContext(), CourseLayoutActivity.class);
                    intent.putExtra("COURSE_VARIABLES",cvObject);
                    startActivity(intent);
                }

            }
        });


        txtLat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtLayLat.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    if (!(s.toString().equals("-"))) {
                        try {
                            if (Double.parseDouble(s.toString()) > 90 || Double.parseDouble(s.toString()) < -90) {
                                txtLayLat.setError("Must be within -90° and 90°");
                            }
                        } catch (NumberFormatException nfe) {
                            txtLayLat.setError("Latitude must be a number");
                        }
                    }
                }
            }
        });

        txtLon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtLayLon.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    if (!(s.toString().equals("-"))) {
                        try {
                            if (Double.parseDouble(s.toString()) > 180 || Double.parseDouble(s.toString()) < -180) {
                                txtLayLon.setError("Must be within -180° and 180°");
                            }
                        } catch (NumberFormatException nfe) {
                            txtLayLon.setError("Longitude must be a number");
                        }
                    }
                }
            }
        });

        txtWind.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtLayWind.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    if (!(s.toString().equals("-"))) {
                        try {
                            if (Double.parseDouble(s.toString()) > 360 || Double.parseDouble(s.toString()) < 0) {
                                txtLayWind.setError("Must be within 0° and 360°");
                            }
                        } catch (NumberFormatException nfe) {
                            txtLayWind.setError("Wind must be a number");
                        }
                    }
                }
            }
        });

        txtDist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtLayDist.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    if (!(s.toString().equals("-"))) {
                        try {
                            if (Double.parseDouble(s.toString()) <= 0) {
                                txtLayDist.setError("Must be greater than 0");
                            }
                        } catch (NumberFormatException nfe) {
                            txtLayDist.setError("Distance must be a number");
                        }
                    }
                }
            }
        });
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
                if(sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    if(prevActivity == 2){
                        finish();
                    } else {
                        showConfirmDialog();
                    }
                }
                return true;
            case R.id.btn_home:
                showConfirmDialog();
                return true;

            case R.id.btn_variables:
                return true;

            case R.id.btn_weather:
                intent = new Intent();
                intent.putExtra("LOCATION", currentLocation);
                intent.setClass(getApplicationContext(),WeatherAPIActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void assignViews() {
        txtLat = findViewById(R.id.editText_lat);
        txtLon = findViewById(R.id.editText_lon);
        txtWind = findViewById(R.id.editText_wind);
        txtDist = findViewById(R.id.editText_dist);

        txtLayLat = findViewById(R.id.latitude_layout);
        txtLayLon = findViewById(R.id.longitude_layout);
        txtLayWind = findViewById(R.id.wind_layout);
        txtLayDist = findViewById(R.id.distance_layout);

        helpButton = findViewById(R.id.help_button);
        calcButton = stubView.findViewById(R.id.img_calculate);

        rgAngle = stubView.findViewById(R.id.radioGroup_angle);
        rgType = stubView.findViewById(R.id.radioGroup_type);
        rg2ndBeat = stubView.findViewById(R.id.radioGroup_secondBeat);
        rgReach = stubView.findViewById(R.id.radioGroup_reach);

        rbAngle1 = stubView.findViewById(R.id.rb_angle_1);
        rbAngle2 = stubView.findViewById(R.id.rb_angle_2);
        rbType1 = stubView.findViewById(R.id.rb_starboard);
        rbType2 = stubView.findViewById(R.id.rb_portboard);
        rb2ndBeat1 = stubView.findViewById(R.id.rb_2ndbeat_1);
        rb2ndBeat2 = stubView.findViewById(R.id.rb_2ndbeat_2);
        rbReach1 = stubView.findViewById(R.id.rb_reach_1);
        rbReach2 = stubView.findViewById(R.id.rb_reach_2);
    }

    private void rewriteOldValues() {
        txtLat.setText(String.valueOf(cvObject.getLat()));
        txtLon.setText(String.valueOf(cvObject.getLon()));
        txtWind.setText(String.format("%.0f",(cvObject.getBearing()*180)/Math.PI));
        txtDist.setText(String.valueOf(cvObject.getDistance()));
        if(rbAngle1 != null) {
            if(cvObject.getAngle() == 60) {
                rbAngle1.setChecked(true);
            } else {
                rbAngle2.setChecked(true);
            }
        }
        if(rbType1 != null) {
            if(cvObject.getType().equals("starboard")) {
                rbType1.setChecked(true);
            } else {
                rbType2.setChecked(true);
            }
        }
        if(rb2ndBeat1 != null) {
            if(cvObject.getSecondBeat().equals("long")) {
                rb2ndBeat1.setChecked(true);
            } else {
                rb2ndBeat2.setChecked(true);
            }
        }
        if(rbReach1 != null) {
            if(cvObject.getReach() == 0.5) {
                rbReach1.setChecked(true);
            } else {
                rbReach2.setChecked(true);
            }
        }


    }

    private boolean fillCVObject() {
        boolean completeForm = true;
        String errorText = "";

        Boolean collapseBttmSheet = false;

        try {
            if (txtLat.getText().toString().isEmpty()) {
                txtLayLat.setError("Please enter latitude");
                completeForm = false;
                collapseBttmSheet = true;
            } else if (Double.parseDouble(txtLat.getText().toString()) <= -90 || Double.parseDouble(txtLat.getText().toString()) >= 90) {
                txtLayLat.setError("Must be within -90° and 90°");
                completeForm = false;
                collapseBttmSheet = true;
            } else {
                cvObject.setLat(Double.parseDouble(txtLat.getText().toString()));
            }
        } catch (NumberFormatException nfe) {
            txtLayLat.setError("Latitude must be a number");
            completeForm = false;
            collapseBttmSheet = true;
        }

        try {
            if (txtLon.getText().toString().isEmpty()) {
                txtLayLon.setError("Please enter longitude");
                completeForm = false;
                collapseBttmSheet = true;
            } else if (Double.parseDouble(txtLon.getText().toString()) <= -180 || Double.parseDouble(txtLon.getText().toString()) >= 180) {
                txtLayLon.setError("Must be within -180° and 180°");
                completeForm = false;
                collapseBttmSheet = true;
            } else {
                cvObject.setLon(Double.parseDouble(txtLon.getText().toString()));
            }
        } catch (NumberFormatException nfe) {
            txtLayLon.setError("Longitude must be a number");
            completeForm = false;
            collapseBttmSheet = true;
        }

        try {
            if (txtWind.getText().toString().isEmpty()) {
                txtLayWind.setError("Please enter wind direction");
                completeForm = false;
                collapseBttmSheet = true;
            } else if (Double.parseDouble(txtWind.getText().toString()) <= 0 || Double.parseDouble(txtWind.getText().toString()) >= 360){
                txtLayWind.setError("Must be within 0° and 360°");
                completeForm = false;
                collapseBttmSheet = true;
            } else {
                cvObject.setBearing(Double.parseDouble(txtWind.getText().toString()));
            }
        } catch (NumberFormatException nfe) {
            txtLayWind.setError("Wind must be a number");
            completeForm = false;
            collapseBttmSheet = true;
        }

        try {
            if (txtDist.getText().toString().isEmpty()) {
                txtLayDist.setError("Please enter distance");
                completeForm = false;
                collapseBttmSheet = true;
            } else if (Double.parseDouble(txtDist.getText().toString()) <= 0) {
                txtLayDist.setError("Must be greater than 0");
                completeForm = false;
                collapseBttmSheet = true;
            }
            else {
                cvObject.setDistance(Double.parseDouble(txtDist.getText().toString()));
            }
        } catch (NumberFormatException nfe) {
            txtLayDist.setError("Distance must be a number");
            completeForm = false;
            collapseBttmSheet = true;
        }

        switch (shape) {
            case "triangle":
                if(rgAngle.getCheckedRadioButtonId() == rbAngle1.getId()) {
                    cvObject.setAngle(60);
                } else if (rgAngle.getCheckedRadioButtonId() == rbAngle2.getId()) {
                    cvObject.setAngle(45);
                } else {
                    errorText = errorText + "Please select an angle\n";
                    completeForm = false;
                }
                if(rgType.getCheckedRadioButtonId() == rbType1.getId()) {
                    cvObject.setType("starboard");
                } else if (rgType.getCheckedRadioButtonId() == rbType2.getId()) {
                    cvObject.setType("portboard");
                } else {
                    errorText = errorText + "Please select Starboard or Portboard\n";
                    completeForm = false;
                }
                break;
            case "trapezoid":
                if(rgAngle.getCheckedRadioButtonId() == rbAngle1.getId()) {
                    cvObject.setAngle(60);
                } else if (rgAngle.getCheckedRadioButtonId() == rbAngle2.getId()){
                    cvObject.setAngle(70);
                } else {
                    errorText = errorText + "Please select an angle\n";
                    completeForm = false;
                }
                if(rgType.getCheckedRadioButtonId() == rbType1.getId()) {
                    cvObject.setType("starboard");
                } else if (rgType.getCheckedRadioButtonId() == rbType2.getId()){
                    cvObject.setType("portboard");
                } else {
                    errorText = errorText + "Please select Starboard or Portboard\n";
                    completeForm = false;
                }
                if(rg2ndBeat.getCheckedRadioButtonId() == rb2ndBeat1.getId()) {
                    cvObject.setSecondBeat("long");
                } else if (rg2ndBeat.getCheckedRadioButtonId() == rb2ndBeat2.getId()){
                    cvObject.setSecondBeat("short");
                } else {
                    errorText = errorText + "Please select a second beat length\n";
                    completeForm = false;
                }
                if(rgReach.getCheckedRadioButtonId() == rbReach1.getId()) {
                    cvObject.setReach(0.5);
                } else if (rgReach.getCheckedRadioButtonId() == rbReach2.getId()){
                    cvObject.setReach(0.66666);
                } else {
                    errorText = errorText + "Please select a reach length\n";
                    completeForm = false;
                }
                break;
            case "optimist":
                if(rgType.getCheckedRadioButtonId() == rbType1.getId()) {
                    cvObject.setType("starboard");
                } else if (rgType.getCheckedRadioButtonId() == rbType2.getId()){
                    cvObject.setType("portboard");
                } else {
                    errorText = errorText + "Please select Starboard or Portboard\n";
                    completeForm = false;
                }
                break;
        }
        if (collapseBttmSheet){
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if (!completeForm) {
            if (!errorText.equals("")) {
                errorText = errorText.trim();
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, errorText, duration);
                toast.show();
            }
            return false;
        } else {
            return true;
        }
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(CourseVariablesBackdropActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
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
                        .setMessage("GPS not enabled")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CourseVariablesBackdropActivity.this,
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
        DialogFragment dialog = new ConfirmDialogFragment();
        dialog.show(getSupportFragmentManager(),null);
    }
    public void showHelpDialog() {
        DialogFragment dialog = new HelpDialogFragment();
        dialog.show(getSupportFragmentManager(),null);
    }
    public void showWeatherDialog() {
        findWeather(currentLocation);
    }
    public void findWeather(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units=imperial&appid=57df42a409e4c7c20a3221979d61174d";


        if(!isOnline())
        {
            DialogFragment dialog = new WeatherDialogFragment(-1);
            dialog.show(getSupportFragmentManager(),null);
        }
        else
        {
            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try
                    {
                        JSONObject wind_object = response.getJSONObject("wind");
                        DialogFragment dialog = new WeatherDialogFragment(wind_object.getDouble("deg"));
                        dialog.show(getSupportFragmentManager(),null);
                    }catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    DialogFragment dialog = new WeatherDialogFragment(-1);
                    dialog.show(getSupportFragmentManager(),null);
                }
            }
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(jor);

        }
    }
    public boolean isOnline(){
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, double wind) {
        txtWind.setText(String.valueOf(wind));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if(prevActivity == 1) {
            // Go back to course selection page
            finish();
        } else {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
