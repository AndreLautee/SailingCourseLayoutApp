package com.example.sailinglayoutapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;

import com.dinuscxj.gesture.MultiTouchGestureDetector;

import java.util.ArrayList;

public class NavMapGLSurfaceView extends GLSurfaceView {

    private final NavMapGLRenderer renderer;
    float mScaleFactor;
    private float mPrevAngle, mAngle, mDeltaAngle;
    ScaleGestureDetector mScaleDetector;
    RotationGestureDetector mRotationDetector;
    private ArrayList<Location> locations;
    private double bearingDirection;
    private int selectedMark;

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public NavMapGLRenderer getRenderer() { return renderer; }

    public void setOffset() {
        renderer.resetOffsetX();
        renderer.resetOffsetY();
        requestRender();
    }

    public float getmAngle() {
        return mAngle;
    }

    public void setmAngle(float angle) {
        mAngle = angle;
        mPrevAngle = mAngle;
        renderer.setAngle(mAngle);
        requestRender();
    }

    public void setLocations(ArrayList<Location> lcts) {
        this.locations = lcts;
        renderer.setLocations(locations);
    }


    public double getBearing() {
        return bearingDirection;
    }

    public void setBearing(double bearing) {
        this.bearingDirection = bearing;
        renderer.setBearingDirection(bearingDirection);
    }

    public int getSelectedMark() {
        return selectedMark;
    }

    public void setSelectedMark(int selectedM) {
        this.selectedMark = selectedM;
        renderer.setSelectedMark(selectedMark);
    }

    public NavMapGLSurfaceView(Context context, ArrayList<Location> coords, ArrayList<Location> lcts, int selectedM, double bearing) {
        super(context);

        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(2);

        locations = lcts;
        bearingDirection = bearing;
        selectedMark = selectedM;

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mRotationDetector = new RotationGestureDetector(new RotateListener());

        renderer = new NavMapGLRenderer(coords, locations, selectedMark, bearingDirection);
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        mPrevAngle = 0;
        // Initialise scale to 1
        mScaleFactor = 1f;
        renderer.setScale(mScaleFactor);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void redraw() {
        renderer.createMap();
        requestRender();
    }

    private final float TOUCH_SCALE_FACTOR = 90.0f / 320; // rate of rotation
    private float previousX;
    private float previousY;


    @Override
    public boolean onTouchEvent(MotionEvent e) {

        mScaleDetector.onTouchEvent(e);
        mRotationDetector.onTouchEvent(e);

        float x = e.getX();
        float y = e.getY();

        switch (e.getActionMasked()) {

            case MotionEvent.ACTION_MOVE:

                // Calculate dx then divide by current scale
                // so movement stays the same speed no matter the scale
                float dx = (x - previousX)/renderer.getScale();
                float dy = ((y - previousY) * -1)/renderer.getScale();

                if(!(mScaleDetector.isInProgress() || mRotationDetector.isInProgress())) {
                    renderer.setOffsetX(dx * 0.0015f);
                    renderer.setOffsetY(dy * 0.0015f);
                }

                requestRender();
                break;
            case MotionEvent.ACTION_UP:

        }

        previousX = x;
        previousY = y;
        return true;
    }




    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor = renderer.getScale();
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.2f, Math.min(mScaleFactor, 2f));

            // Update scale
            renderer.setScale(mScaleFactor);
            requestRender();
            return true;
        }
    }

    private class RotateListener implements RotationGestureDetector.OnRotationGestureListener {

        @Override
        public boolean OnRotation(RotationGestureDetector rotationDetector) {
            mDeltaAngle = rotationDetector.getAngle();
            mAngle = mDeltaAngle + mPrevAngle;
            mPrevAngle = mAngle;
            renderer.setAngle(mAngle);
            requestRender();
            return true;
        }
    }


}


