package com.example.sailinglayoutapp;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

    private GLSurfaceView gLView;
    private Map<String, String> variables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_layout);
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
        });
    }

    public Map<String, String> setVariableArray(Bundle extras) {
        Map<String, String> result = new HashMap<>();

        if (extras.getInt("TYPE") == 2131296484) {
            result.put("TYPE", "starboard");
        } else if (extras.getInt("TYPE") == 2131296483){
            result.put("TYPE", "portboard");
        }

        switch (extras.getInt("SHAPE")) {
            case 0:
                result.put("SHAPE", "windward_leeward");
                break;
            case 1:
                result.put("SHAPE", "triangle");
                if (extras.getInt("ANGLE") == 2131296480) {
                    result.put("ANGLE", "60");
                } else if (extras.getInt("ANGLE") == 2131296481) {
                    result.put("ANGLE", "45");
                }
                break;
            case 2:
                result.put("SHAPE", "trapezoid");
                if (extras.getInt("SECOND_BEAT") == 2131296478) {
                    result.put("SECOND_BEAT", "equal");
                } else if (extras.getInt("SECOND_BEAT") == 2131296479) {
                    result.put("SECOND_BEAT", "short");
                }
                break;
            case 3:
                result.put("SHAPE", "optimist");
                break;
        }


        return result;
    }
}
