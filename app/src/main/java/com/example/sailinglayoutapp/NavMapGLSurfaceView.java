package com.example.sailinglayoutapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class NavMapGLSurfaceView extends GLSurfaceView {

    private final NavMapGLRenderer renderer;

    public NavMapGLSurfaceView(Context context, ArrayList<Location> coords, Location location) {
        super(context);

        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(2);

        renderer = new NavMapGLRenderer(coords, location);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
    }



}
