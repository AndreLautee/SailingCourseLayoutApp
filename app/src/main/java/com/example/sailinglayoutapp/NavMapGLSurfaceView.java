package com.example.sailinglayoutapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;

import com.dinuscxj.gesture.MultiTouchGestureDetector;

import java.util.ArrayList;

public class NavMapGLSurfaceView extends GLSurfaceView {

    private final NavMapGLRenderer renderer;
    float mScaleFactor;
    float mAngle;
    ScaleGestureDetector mScaleDetector;

    public NavMapGLSurfaceView(Context context, ArrayList<Location> coords, ArrayList<Location> locations, int selectedMark, double bearing) {
        super(context);

        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(2);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        renderer = new NavMapGLRenderer(coords, locations, selectedMark, bearing);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        // Initialise scale to 1
        mScaleFactor = 1f;
        renderer.setScale(mScaleFactor);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 90.0f / 320; // rate of rotation
    private float previousX;
    private float previousY;


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        mScaleDetector.onTouchEvent(e);
        float x = e.getX();
        float y = e.getY();

        switch (e.getActionMasked()) {


            case MotionEvent.ACTION_MOVE:


                float dx = x - previousX;
                float dy = y - previousY;

                if(!mScaleDetector.isInProgress()) {
                    renderer.setOffsetX(dx * 0.001f);
                    renderer.setOffsetY(dy * 0.001f);
                }

/*
                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }
                renderer.setAngle(
                        renderer.getAngle() +
                                ((dx + dy)*TOUCH_SCALE_FACTOR));
                */
                requestRender();
                break;
        }

        previousX = x;
        previousY = y;
        return true;
    }




    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.2f, Math.min(mScaleFactor, 5.0f));

            // Update scale
            renderer.setScale(mScaleFactor);
            requestRender();
            return true;
        }

    }


}


