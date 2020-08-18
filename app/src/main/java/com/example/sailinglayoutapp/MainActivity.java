package com.example.sailinglayoutapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView Start;
    private int Request_code = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Start=findViewById(R.id.Start);


    }

    public void Start_onclick(View view) {
        Intent intent=new Intent();
        intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
        startActivityForResult(intent,Request_code);
    }

    public void Navi_onclick(View view) {

    }

    public void Setting_onclick(View view) {
    }
}
