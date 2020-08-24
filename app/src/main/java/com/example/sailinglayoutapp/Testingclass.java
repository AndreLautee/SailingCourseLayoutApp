package com.example.sailinglayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Testingclass extends AppCompatActivity {
    TextView Start;
    private int Request_code = 1;
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing2);

        Start = findViewById(R.id.Start);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        //set selected page
        bottomNavigation.setSelectedItemId(R.id.nav_layout);

        //perform ItemSelectedListener
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_variables:
                        startActivity(new Intent(getApplicationContext(),
                                CourseVariablesActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                switch (item.getItemId()){
                    case R.id.nav_compass:
                        startActivity(new Intent(getApplicationContext(),
                                MainActivity2.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                switch (item.getItemId()){
                    case R.id.nav_layout:
                        return true;
                }
                return false;
            }
        });


    }


}
