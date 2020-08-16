package com.example.sailinglayoutapp;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.util.Map;

class LayoutGLSurfaceView extends GLSurfaceView {

    private final LayoutGLRenderer renderer;

    public LayoutGLSurfaceView(Context context, Map<String, String> variables) {
        super(context);
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(2);

        renderer = new LayoutGLRenderer(variables);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
    }
}
