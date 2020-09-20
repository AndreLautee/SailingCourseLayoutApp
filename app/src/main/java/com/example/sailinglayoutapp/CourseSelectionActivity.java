package com.example.sailinglayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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

public class CourseSelectionActivity extends AppCompatActivity {

    ImageView imgTriangle, imgTrapezoid, imgStraight, imgOptimist;
    CourseVariablesObject cvObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_selection);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Course Selection");
            actionBar.setDisplayHomeAsUpEnabled(false);
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
                Intent intent = new Intent(getApplicationContext(), CourseVariablesActivity.class);
                intent.putExtra("cvObject", cvObject);
                startActivity(intent);
            }
        });

        imgTrapezoid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvObject.setShape("trapezoid");
                Intent intent = new Intent(getApplicationContext(), CourseVariablesActivity.class);
                intent.putExtra("cvObject", cvObject);
                startActivity(intent);
            }
        });

        imgStraight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvObject.setShape("windward_leeward");
                Intent intent = new Intent(getApplicationContext(), CourseVariablesActivity.class);
                intent.putExtra("cvObject", cvObject);
                startActivity(intent);
            }
        });

        imgOptimist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvObject.setShape("optimist");
                Intent intent = new Intent(getApplicationContext(), CourseVariablesActivity.class);
                intent.putExtra("COURSE_VARIABLES", cvObject);
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
                intent.putExtra("COURSE_VARIABLES", cvObject);
                intent.setClass(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.btn_variables:
                return true;

            case R.id.btn_weather:
                intent = new Intent();
                intent.setClass(getApplicationContext(),WeatherAPIActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
