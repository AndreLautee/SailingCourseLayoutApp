package com.example.sailinglayoutapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button compass;
    private int Request_code = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compass=findViewById(R.id.compassBtn);

        compass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(getApplicationContext(),MainActivity2.class);
                startActivityForResult(intent,Request_code);

            }
        });

        Button start = findViewById(R.id.button_courseVariables);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
                startActivityForResult(intent,Request_code);
            }
        });
    }
}
