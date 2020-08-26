package com.example.sailinglayoutapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class CourseVariablesActivity extends AppCompatActivity {

    private RadioGroup radioGroup_type, radioGroup_angle, radioGroup_reach, radioGroup_secondBeat;
    private Spinner spinner_shape;
    private EditText editText_wind, editText_distance;
    private TextView textView_angle, textView_reach, textView_secondBeat;
    BottomNavigationView bottomNavigation;
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_variables);
        bottomNavigation = findViewById(R.id.bottom_navigation);


        //set selected page
        bottomNavigation.setSelectedItemId(R.id.nav_variables);

        //perform ItemSelectedListener
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                String errorText = "Please complete course variables";
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, errorText, duration);

                switch (item.getItemId()){
                    case R.id.nav_variables:
                        return true;
                    case R.id.nav_layout:
                        Bundle userInput = passBundle();
                        if(userInput != null) {
                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(), CourseLayoutActivity.class);
                            intent.putExtras(userInput);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        }
                        return true;
                    case R.id.nav_compass:
                    case R.id.nav_map:
                        toast.show();
                        return true;
                }
                switch (item.getItemId()){
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext(),
                                MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        radioGroup_type = findViewById(R.id.radioGroup_type);
        spinner_shape = findViewById(R.id.spinner_shape);
        editText_wind = findViewById(R.id.editText_bearing);
        editText_distance = findViewById(R.id.editText_distance);
        radioGroup_angle = findViewById(R.id.radioGroup_angle);
        radioGroup_reach = findViewById(R.id.radioGroup_reach);
        radioGroup_secondBeat = findViewById(R.id.radioGroup_secondBeat);
        textView_angle = findViewById(R.id.textView_angle);
        textView_reach = findViewById(R.id.textView_reach);
        textView_secondBeat = findViewById(R.id.textView_secondBeat);

        setSpinner();

        // If returning to page, reset previously filled in variables
        Intent intent = getIntent();
        final Bundle userInput = intent.getExtras();
        if (userInput != null) {
            RadioButton rb;
            rb = (RadioButton) radioGroup_type.getChildAt(userInput.getInt("TYPE"));
            if(rb != null) {rb.setChecked(true);}
            spinner_shape.setSelection(userInput.getInt("SHAPE"));
            editText_wind.setText(String.valueOf(userInput.getDouble("BEARING")));
            editText_distance.setText(String.valueOf(userInput.getDouble("DISTANCE")));
            rb = (RadioButton) radioGroup_angle.getChildAt(userInput.getInt("ANGLE"));
            if(rb != null) {rb.setChecked(true);}
            rb = (RadioButton) radioGroup_reach.getChildAt(userInput.getInt("REACH"));
            if(rb!= null) {rb.setChecked(true);}
            rb = (RadioButton) radioGroup_secondBeat.getChildAt(userInput.getInt("SECOND_BEAT"));
            if(rb != null) {rb.setChecked(true);}

        }

        // Change what options are available based on the course triangle
        spinner_shape.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch(parent.getItemAtPosition(position).toString()) {
                    case "Triangle":
                        radioGroup_type.setVisibility(View.VISIBLE);
                        if(userInput == null) {radioGroup_type.clearCheck();}
                        radioGroup_angle.setVisibility(View.VISIBLE);
                        if(userInput == null) {radioGroup_angle.clearCheck();}
                        radioGroup_reach.setVisibility(View.GONE);
                        radioGroup_reach.clearCheck();
                        radioGroup_secondBeat.setVisibility(View.GONE);
                        radioGroup_secondBeat.clearCheck();
                        textView_angle.setVisibility(View.VISIBLE);
                        textView_reach.setVisibility(View.GONE);
                        textView_secondBeat.setVisibility(View.GONE);

                        String[] triangleStrings = getResources().getStringArray(R.array.triangle_angles);
                        for (int i = 0; i < radioGroup_angle.getChildCount(); i++) {
                            ((RadioButton) radioGroup_angle.getChildAt(i)).setText(triangleStrings[i]);
                        }
                        break;
                    case "Trapezoid":
                        radioGroup_type.setVisibility(View.VISIBLE);
                        if(userInput == null) {radioGroup_type.clearCheck();}
                        radioGroup_angle.setVisibility(View.VISIBLE);
                        if(userInput == null) {radioGroup_angle.clearCheck();}
                        radioGroup_reach.setVisibility(View.VISIBLE);
                        if(userInput == null) {radioGroup_reach.clearCheck();}
                        radioGroup_secondBeat.setVisibility(View.VISIBLE);
                        if(userInput == null) {radioGroup_secondBeat.clearCheck();}
                        textView_angle.setVisibility(View.VISIBLE);
                        textView_reach.setVisibility(View.VISIBLE);
                        textView_secondBeat.setVisibility(View.VISIBLE);
                        String[] trapezoidStrings = getResources().getStringArray(R.array.trapezoid_angles);
                        for (int i = 0; i < radioGroup_angle.getChildCount(); i++) {
                            ((RadioButton) radioGroup_angle.getChildAt(i)).setText(trapezoidStrings[i]);
                        }
                        break;
                    case "Windward-Leeward":
                        radioGroup_type.setVisibility(View.GONE);
                        radioGroup_type.clearCheck();
                    default:
                        radioGroup_angle.setVisibility(View.GONE);
                        radioGroup_angle.clearCheck();
                        radioGroup_reach.setVisibility(View.GONE);
                        radioGroup_reach.clearCheck();
                        radioGroup_secondBeat.setVisibility(View.GONE);
                        radioGroup_secondBeat.clearCheck();
                        textView_angle.setVisibility(View.GONE);
                        textView_reach.setVisibility(View.GONE);
                        textView_secondBeat.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        final Button button_weather = findViewById(R.id.button_weather);
        button_weather.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), WeatherAPIActivity.class);
                intent.putExtra("location", currentLocation);
                startActivity(intent);
            }
        });

        final Button button = findViewById(R.id.button_calculate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle userInput = passBundle();
                if(userInput != null) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), CourseLayoutActivity.class);
                    intent.putExtras(userInput);
                    startActivity(intent);
                }
            }
        });


    }

    public Bundle passBundle(){
        boolean completeForm = true;
        String errorText = "";

        // Ensure all fields are filled in
        switch (spinner_shape.getSelectedItemPosition()) {
            case 2:
                if (radioGroup_secondBeat.getCheckedRadioButtonId() == -1) {
                    errorText = errorText + "Please select a second beat length\n";
                    completeForm = false;
                }
                if (radioGroup_reach.getCheckedRadioButtonId() == -1) {
                    errorText = errorText + "Please select a reach length\n";
                    completeForm = false;
                }
            case 1:
                if (radioGroup_angle.getCheckedRadioButtonId() == -1) {
                    errorText = errorText + "Please select an angle\n";
                    completeForm = false;
                }
            case 3:
                if (radioGroup_type.getCheckedRadioButtonId() == -1) {
                    errorText = errorText + "Please select a Starboard or Portboard\n";
                    completeForm = false;
                }
            default:
                if (TextUtils.isEmpty(editText_wind.getText())) {
                    errorText = errorText + "Please enter the wind direction\n";
                    completeForm = false;
                }
                if (TextUtils.isEmpty(editText_distance.getText())) {
                    errorText = errorText + "Please enter the distance\n";
                    completeForm = false;
                }
        }
        // Create a bundle containing the inputted variables, to attach to the intent for future use
        if (completeForm) {
            Bundle userInput = new Bundle();
            int selectedRBID;
            View rb;
            selectedRBID = radioGroup_type.getCheckedRadioButtonId();
            rb = findViewById(selectedRBID);
            userInput.putInt("TYPE", radioGroup_type.indexOfChild(rb));
            userInput.putInt("SHAPE", spinner_shape.getSelectedItemPosition());
            userInput.putDouble("BEARING", Double.parseDouble(editText_wind.getText().toString()));
            userInput.putDouble("DISTANCE", Double.parseDouble(editText_distance.getText().toString()));
            selectedRBID = radioGroup_angle.getCheckedRadioButtonId();
            rb = findViewById(selectedRBID);
            userInput.putInt("ANGLE", radioGroup_angle.indexOfChild(rb));
            selectedRBID = radioGroup_reach.getCheckedRadioButtonId();
            rb = findViewById(selectedRBID);
            userInput.putInt("REACH", radioGroup_reach.indexOfChild(rb));
            selectedRBID = radioGroup_secondBeat.getCheckedRadioButtonId();
            rb = findViewById(selectedRBID);
            userInput.putInt("SECOND_BEAT", radioGroup_secondBeat.indexOfChild(rb));
            return userInput;
        } else {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, errorText, duration);
            toast.show();
            return null;
        }
    }

    public void setSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.course_shapes, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_shape.setAdapter(adapter);
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

  /*  public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_variables:
                            openFragment(VariablesFragment.newInstance("", ""));
                            return true;
                        case R.id.nav_bouyCoor:
                            openFragment(BuoyCoorFragment.newInstance("", ""));
                            return true;
                        case R.id.nav_layout:
                            openFragment(LayoutFragment.newInstance("", ""));
                            return true;
                        case R.id.nav_compass:
                            openFragment(CompassFragment.newInstance("", ""));
                            return true;
                    }
                    return false;
                }
        };
*/
/*
    public CourseVariablesObject createObject() {
        String triangle = null;
        double bearing;
        double distance;
        int angle;
        String type = null;
        String trapezoidType = null;

        // Get triangle
        final RadioGroup radioGroup_shape = findViewById(R.id.radioGroup_shape);
        // get selected radio button from radioGroup
        int selectedId = radioGroup_shape.getCheckedRadioButtonId();

        if(selectedId == -1) {
            // Course triangle not selected
            // Error
        } else {
            // find the radiobutton by returned id
            RadioButton selectedRadioButton = findViewById(selectedId);
            triangle = selectedRadioButton.getText().toString();
        }

        // Get bearing
        final EditText editText_bearing = findViewById(R.id.editText_bearing);
        bearing = Double.parseDouble(editText_bearing.getText().toString());

        // Get bearing
        final EditText editText_distance = findViewById(R.id.editText_distance);
        distance = Double.parseDouble(editText_distance.getText().toString());

        // Get angle
        final EditText editText_angle = findViewById(R.id.editText_angle);
        angle = Integer.parseInt(editText_angle.getText().toString());

        // Get type
        final RadioGroup radioGroup_type = findViewById(R.id.radioGroup_type);
        // get selected radio button from radioGroup
        selectedId = radioGroup_type.getCheckedRadioButtonId();

        if(selectedId == -1) {
            // Course triangle not selected
            // Error
        } else {
            // find the radiobutton by returned id
            RadioButton selectedRadioButton = findViewById(selectedId);
            type = selectedRadioButton.getText().toString();
        }

        return new CourseVariablesObject(triangle, bearing, distance, angle, type, trapezoidType);

    }

 */
}

