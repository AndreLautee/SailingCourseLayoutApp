package com.example.sailinglayoutapp;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
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
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        assert extras != null;
        variables = setVariableArray(extras);

        gLView = new LayoutGLSurfaceView(this, variables);
        setContentView(gLView);

/*
        Button back = findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= getIntent();
                intent.setClass(getApplicationContext(),CourseVariablesActivity.class);
                startActivity(intent);
            }
        });*/
    }

    public Map<String, String> setVariableArray(Bundle extras) {
        Map<String, String> result = new HashMap<>();

        if (extras.getInt("TYPE") == 2131296485) {
            result.put("TYPE", "starboard");
        } else if (extras.getInt("TYPE") == 2131296484){
            result.put("TYPE", "portboard");
        }

        Log.d("ANGLEEEEEE", String.valueOf(extras.getInt("ANGLE")));
        Log.d("SECOND BEAT", String.valueOf(extras.getInt("SECOND_BEAT")));
        switch (extras.getInt("SHAPE")) {
            case 0:
                result.put("SHAPE", "windward_leeward");
                break;
            case 1:
                result.put("SHAPE", "triangle");
                if (extras.getInt("ANGLE") == 2131296481) {
                    result.put("ANGLE", "60");
                } else if (extras.getInt("ANGLE") == 2131296482) {
                    result.put("ANGLE", "45");
                }
                break;
            case 2:
                result.put("SHAPE", "trapezoid");
                if (extras.getInt("SECOND_BEAT") == 2131296479) {
                    result.put("SECOND_BEAT", "equal");
                } else if (extras.getInt("SECOND_BEAT") == 2131296480) {
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
