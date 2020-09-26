package com.example.sailinglayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CourseSelectionActivity extends AppCompatActivity {

    ImageView imgTriangle, imgTrapezoid, imgStraight, imgOptimist;
    CourseVariablesObject cvObject;
    Location currentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_selection);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Course Selection");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_west_black_24dp);
        }

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        currentLocation = null;

        if(checkLocationPermission()) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        cvObject = new CourseVariablesObject();

        imgTriangle = findViewById(R.id.img_triangle);
        imgTrapezoid = findViewById(R.id.img_trapezoid);
        imgStraight = findViewById(R.id.img_straight);
        imgOptimist = findViewById(R.id.img_optimist);

        imgTriangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvObject.setShape("triangle");
                Intent intent = new Intent(getApplicationContext(), CourseVariablesBackdropActivity.class);
                intent.putExtra("COURSE_VARIABLES", cvObject);
                startActivity(intent);
            }
        });

        imgTrapezoid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvObject.setShape("trapezoid");
                Intent intent = new Intent(getApplicationContext(), CourseVariablesBackdropActivity.class);
                intent.putExtra("COURSE_VARIABLES", cvObject);
                startActivity(intent);
            }
        });

        imgStraight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvObject.setShape("windward_leeward");
                Intent intent = new Intent(getApplicationContext(), CourseVariablesBackdropActivity.class);
                intent.putExtra("COURSE_VARIABLES", cvObject);
                startActivity(intent);
            }
        });

        imgOptimist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvObject.setShape("optimist");
                Intent intent = new Intent(getApplicationContext(), CourseVariablesBackdropActivity.class);
                intent.putExtra("COURSE_VARIABLES", cvObject);
                startActivity(intent);
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
                finish();
                return true;
            case R.id.btn_home:
                // User chose the "Menu" item, show the app menu UI...
                intent = new Intent();
                intent.putExtra("COURSE_VARIABLES", cvObject);
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
                                ActivityCompat.requestPermissions(CourseSelectionActivity.this,
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
