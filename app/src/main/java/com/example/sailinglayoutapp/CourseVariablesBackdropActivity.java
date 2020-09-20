package com.example.sailinglayoutapp;

import android.Manifest;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import org.decimal4j.util.DoubleRounder;
import org.json.JSONException;
import org.json.JSONObject;

public class CourseVariablesBackdropActivity extends AppCompatActivity implements WeatherDialogFragment.WeatherDialogListener{

    BottomSheetBehavior<LinearLayout> sheetBehavior;
    CourseVariablesObject cvObject;
    String shape;
    View calcButton, stubView, arrow, topBar;
    EditText txtLat, txtLon, txtWind, txtDist;
    RadioGroup rgAngle, rgType, rg2ndBeat, rgReach;
    RadioButton rbAngle1, rbAngle2, rbType1, rbType2, rb2ndBeat1, rb2ndBeat2, rbReach1, rbReach2;
    Location currentLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_variables_backdrop);

        Intent intent = getIntent();
        cvObject = intent.getParcelableExtra("COURSE_VARIABLES");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Course Variables");
            actionBar.setDisplayHomeAsUpEnabled(false);
        }



        LinearLayout contentLayout = findViewById(R.id.contentLayout);
        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);

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

        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillCVObject();
                Intent intent = new Intent(getApplicationContext(), CourseLayoutActivity.class);
                intent.putExtra("COURSE_VARIABLES",cvObject);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.btn_home:
                // User chose the "Menu" item, show the app menu UI...
                intent = new Intent();
                intent.setClass(getApplicationContext(),MainActivity.class);
                startActivity(intent);
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
        txtWind.setText(String.valueOf(cvObject.getBearing()));
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

    private void fillCVObject() {
        cvObject.setLat(Double.parseDouble(txtLat.getText().toString()));
        cvObject.setLon(Double.parseDouble(txtLon.getText().toString()));
        cvObject.setBearing(Double.parseDouble(txtWind.getText().toString()));
        cvObject.setDistance(Double.parseDouble(txtDist.getText().toString()));
        switch (shape) {
            case "triangle":
                if(rgAngle.getCheckedRadioButtonId() == rbAngle1.getId()) {
                    cvObject.setAngle(60);
                } else {
                    cvObject.setAngle(45);
                }
                if(rgType.getCheckedRadioButtonId() == rbType1.getId()) {
                    cvObject.setType("starboard");
                } else {
                    cvObject.setType("portboard");
                }
                break;
            case "trapezoid":
                if(rgAngle.getCheckedRadioButtonId() == rbAngle1.getId()) {
                    cvObject.setAngle(60);
                } else {
                    cvObject.setAngle(70);
                }
                if(rgType.getCheckedRadioButtonId() == rbType1.getId()) {
                    cvObject.setType("starboard");
                } else {
                    cvObject.setType("portboard");
                }
                if(rg2ndBeat.getCheckedRadioButtonId() == rb2ndBeat1.getId()) {
                    cvObject.setSecondBeat("long");
                } else {
                    cvObject.setSecondBeat("short");
                }
                if(rgReach.getCheckedRadioButtonId() == rbReach1.getId()) {
                    cvObject.setReach(0.5);
                } else {
                    cvObject.setReach(0.66666);
                }
                break;
            case "optimist":
                if(rgType.getCheckedRadioButtonId() == rbType1.getId()) {
                    cvObject.setType("starboard");
                } else {
                    cvObject.setType("portboard");
                }
                break;
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
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
