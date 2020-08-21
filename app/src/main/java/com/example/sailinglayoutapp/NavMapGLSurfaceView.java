package com.example.sailinglayoutapp;

import android.content.Context;
import android.location.Location;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;

public class NavMapGLSurfaceView extends GLSurfaceView {

    private final NavMapGLRenderer renderer;

    public NavMapGLSurfaceView(Context context, ArrayList<Location> coords, double distance) {
        super(context);

        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(2);

        renderer = new NavMapGLRenderer(coords, distance);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
    }
}
