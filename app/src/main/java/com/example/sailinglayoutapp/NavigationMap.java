package com.example.sailinglayoutapp;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class NavigationMap extends AppCompatActivity {

    GLSurfaceView gLView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_map);

        Intent intent = getIntent();
        MarkerCoordCalculations course = intent.getParcelableExtra("COURSE");

        LinearLayout layoutCourseLayout = (LinearLayout) findViewById(R.id.layout_navigationMap);

        RelativeLayout layoutGL = (RelativeLayout) findViewById(R.id.rl_navigationMap);

        gLView = new NavMapGLSurfaceView(this, course.getCoords(), course.getCourseVariablesObject().getDistance());

        layoutGL.addView(gLView);
    }
}
