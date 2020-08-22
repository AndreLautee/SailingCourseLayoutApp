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

public class MainActivity extends AppCompatActivity {
    TextView Start;
    private int Request_code = 1;
    BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Start=findViewById(R.id.Start);
        bottomNavigation = findViewById(R.id.bottom_navigation);





    }


    public void Start_onclick(View view) {
        Intent intent=new Intent();
        intent.setClass(getApplicationContext(),MainActivity2.class);
        startActivityForResult(intent,Request_code);
    }

    public void Navi_onclick(View view) {

    }

    public void Setting_onclick(View view) {
    }
}
