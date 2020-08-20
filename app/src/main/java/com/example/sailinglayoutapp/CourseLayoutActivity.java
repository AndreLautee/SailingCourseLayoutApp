package com.example.sailinglayoutapp;

import android.content.Intent;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseLayoutActivity extends AppCompatActivity {

    RadioButton r1, r2, r3, r4;
    RelativeLayout rl;
    DrawView drawView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_layout);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        r1 = findViewById(R.id.radioButton_layout_1);
        RelativeLayout.LayoutParams r1_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r1.setOnClickListener(radioButton_listener);

        r2 = findViewById(R.id.radioButton_layout_2);
        RelativeLayout.LayoutParams r2_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r2.setOnClickListener(radioButton_listener);

        r3 = findViewById(R.id.radioButton_layout_3);
        RelativeLayout.LayoutParams r3_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r3.setOnClickListener(radioButton_listener);

        r4 = findViewById(R.id.radioButton_layout_4);
        RelativeLayout.LayoutParams r4_layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        r4.setOnClickListener(radioButton_listener);

        rl = findViewById(R.id.rl_line);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Log.d("height", String.valueOf(height));
        Log.d("width", String.valueOf(width));
        switch (extras.getInt("SHAPE")) {
            case 0: // windward-leeward
                r3.setVisibility(View.INVISIBLE);
                r4.setVisibility(View.INVISIBLE); // Only two points needed so make r3 and r4 invisible

                r1_layoutParams.setMargins(width/2, height/5, 0,0);
                r1.setLayoutParams(r1_layoutParams);

                r2_layoutParams.setMargins(width/2, (3*height)/5, 0,0);
                r2.setLayoutParams(r2_layoutParams);

                drawView = new DrawView(this, r1, r2);
                rl.addView(drawView);
                break;
            case 1: // triangle
                r4.setVisibility(View.INVISIBLE); // only three points needed so make r4 invisible
                if (extras.getInt("TYPE") == 0) { // starboard
                    r1_layoutParams.setMargins(width/6, height/5,0,0);
                    r1.setLayoutParams(r1_layoutParams);

                   r2_layoutParams.setMargins((4*width)/6, (2*height)/5, 0,0);
                    r2.setLayoutParams(r2_layoutParams);

                   r3_layoutParams.setMargins(width/6,(3*height)/5, 0, 0);
                    r3.setLayoutParams(r3_layoutParams);

                    drawView = new DrawView(this, r1, r2);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r2, r3);
                    rl.addView(drawView);
                    drawView = new DrawView(this, r3, r1);
                    rl.addView(drawView);
                    break;
                } else if (extras.getInt("TYPE") == 1) { // portboard

                    break;
                }
            case 2: // trapezoid
                if (extras.getInt("TYPE") == 0) { // if starboard
                    if (extras.getInt("SECOND_BEAT") == 0) { // if equal length second beat

                        break;
                    } else if (extras.getInt("SECOND_BEAT") == 1) { // if unequal length second beat

                        break;
                    }
                } else if (extras.getInt("TYPE") == 1) { // if portboard
                    if (extras.getInt("SECOND_BEAT") == 0) { // if equal length second beat

                        break;
                    } else if (extras.getInt("SECOND_BEAT") == 1) { // if unequal length second beat

                        break;
                    }
                }
            case 3: // optimist
                if (extras.getInt("TYPE") == 0) { // if starboard

                    break;
                } else if (extras.getInt("TYPE") == 1) { // if portboard

                   break;
                }
        }

        Button button_back = findViewById(R.id.button_back);
        Button button_navigation = findViewById(R.id.button_navigation);

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
                startActivity(intent);
            }
        });

        button_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add code to go to navigation page
            }
        });

        View.OnClickListener radioButton_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (r1.getId() != v.getId()) {
                    r1.setChecked(false);
                }
                if (r2.getId() != v.getId()) {
                    r2.setChecked(false);
                }
                if (r3.getId() != v.getId()) {
                    r3.setChecked(false);
                }
                if (r4.getId() != v.getId()) {
                    r4.setChecked(false);
                }
            }
        };




        /*
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        assert extras != null;
        variables = setVariableArray(extras);


        LinearLayout layoutCourseLayout = (LinearLayout) findViewById(R.id.layout_courseLayout);

        Button button_navigate = new Button(this);
        Button button_back = new Button(this);
        button_navigate.setText("Navigate");
        button_back.setText("Back");

        layoutCourseLayout.addView(button_navigate);
        layoutCourseLayout.addView(button_back);

        RelativeLayout layoutGL = (RelativeLayout) findViewById(R.id.layout_GL);

        gLView = new LayoutGLSurfaceView(this, variables);

        layoutGL.addView(gLView);

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= getIntent();
                intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
                startActivity(intent);
            }
        });*/
    }

    public CourseVariablesObject setCourseVariablesObject(Bundle extras) {
        String type = null;
        String shape = null;
        double bearing;
        double distance;
        int angle = 0;
        double reach = 0;
        String secondBeat = null;

        if (extras.getInt("TYPE") == 0) {
            type = "starboard";
        } else if (extras.getInt("TYPE") == 1){
            type = "portboard";
        }

        bearing = extras.getDouble("BEARING");
        distance = extras.getDouble("DISTANCE");

        switch (extras.getInt("SHAPE")) {
            case 0:
                shape = "windward_leeward";
                break;
            case 1:
                shape = "triangle";
                if (extras.getInt("ANGLE") == 0) {
                    angle = 60;
                } else if (extras.getInt("ANGLE") == 1) {
                    angle = 45;
                }
                break;
            case 2:
                shape = "trapezoid";
                if (extras.getInt("ANGLE") == 0) {
                    angle = 60;
                } else if (extras.getInt("ANGLE") == 1) {
                    angle = 70;
                }
                if (extras.getInt("REACH") == 0) {
                    reach = 0.5;
                } else if (extras.getInt("REACH") == 1) {
                    reach = 0.66;
                }
                if (extras.getInt("SECOND_BEAT") == 0) {
                    secondBeat = "equal";
                } else if (extras.getInt("SECOND_BEAT") == 1) {
                    secondBeat = "short";
                }
                break;
            case 3:
                shape = "optimist";
                break;
        }

        CourseVariablesObject result = new CourseVariablesObject(type, shape, bearing, distance, angle, reach, secondBeat);
        return result;
    }
    /*
    public Map<String, String> setVariableArray(Bundle extras) {
        Map<String, String> result = new HashMap<>();

        if (extras.getInt("TYPE") == 0) {
            result.put("TYPE", "starboard");
        } else if (extras.getInt("TYPE") == 1){
            result.put("TYPE", "portboard");
        }

        switch (extras.getInt("SHAPE")) {
            case 0:
                result.put("SHAPE", "windward_leeward");
                break;
            case 1:
                result.put("SHAPE", "triangle");
                if (extras.getInt("ANGLE") == 0) {
                    result.put("ANGLE", "60");
                } else if (extras.getInt("ANGLE") == 1) {
                    result.put("ANGLE", "45");
                }
                break;
            case 2:
                result.put("SHAPE", "trapezoid");
                if (extras.getInt("SECOND_BEAT") == 0) {
                    result.put("SECOND_BEAT", "equal");
                } else if (extras.getInt("SECOND_BEAT") == 1) {
                    result.put("SECOND_BEAT", "short");
                }
                break;
            case 3:
                result.put("SHAPE", "optimist");
                break;
        }


        return result;
    }*/

    View.OnClickListener radioButton_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (r1.getId() != v.getId()) {
                r1.setChecked(false);
            }
            if (r2.getId() != v.getId()) {
                r2.setChecked(false);
            }
            if (r3.getId() != v.getId()) {
                r3.setChecked(false);
            }
            if (r4.getId() != v.getId()) {
                r4.setChecked(false);
            }
        }
    };
}

