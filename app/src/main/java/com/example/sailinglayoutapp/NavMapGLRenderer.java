package com.example.sailinglayoutapp;

import android.location.Location;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NavMapGLRenderer implements GLSurfaceView.Renderer {

    private ArrayList<Location> coordinates;
    NavMap navMap;
    List<Circle> circles;
    double x_dispersion;
    double y_dispersion;
    double course_distance;


    NavMapGLRenderer(ArrayList<Location> coords, double dist) {
        coordinates = coords;
        course_distance = dist;
    }

    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        circles = new ArrayList<>();
        createMap();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        for (int i=0; i < circles.size(); i++) {
            circles.get(i).draw();
        }
    }

    public void createMap() {
        double centre_x = 0;
        double centre_y = 0;

        double leftmost_coord = coordinates.get(0).getLongitude();
        double rightmost_coord = coordinates.get(0).getLongitude();
        double upmost_coord = coordinates.get(0).getLatitude();
        double downmost_coord = coordinates.get(0).getLatitude();

        for (int i = 1; i < coordinates.size(); i++) {
            if (leftmost_coord > coordinates.get(i).getLongitude()) {
                leftmost_coord = coordinates.get(i).getLongitude();
            }
            if (rightmost_coord < coordinates.get(i).getLongitude()) {
                rightmost_coord = coordinates.get(i).getLongitude();
            }
            if (upmost_coord < coordinates.get(i).getLatitude()) {
                upmost_coord = coordinates.get(i).getLatitude();
            }
            if (downmost_coord > coordinates.get(i).getLatitude()) {
                downmost_coord = coordinates.get(i).getLatitude();
            }
        }

        centre_x = (rightmost_coord + leftmost_coord) / 2;
        centre_y = (upmost_coord + downmost_coord) / 2;

        double max_xy_dispersion = distanceToDegrees(course_distance/2);
        double topLeft_x = centre_x - max_xy_dispersion;
        double topLeft_y = centre_y + max_xy_dispersion;
/*
        Log.d("max dispersion", String.valueOf(max_xy_dispersion));
        Log.d("course distance", String.valueOf(course_distance));
        Log.d("coord 0 x", String.valueOf(coordinates.get(0).getLongitude()));
        Log.d("coord 0 y", String.valueOf(coordinates.get(0).getLatitude()));
        Log.d("coord 1 x", String.valueOf(coordinates.get(1).getLongitude()));
        Log.d("coord 1 y", String.valueOf(coordinates.get(1).getLatitude()));
        Log.d("centre x", String.valueOf(centre_x));
        Log.d("centre y", String.valueOf(centre_y));
        Log.d("topLeft x", String.valueOf(topLeft_x));
        Log.d("topLeft y", String.valueOf(topLeft_y));*/
        for (int i = 0; i < coordinates.size(); i++) {
            float x = 0.7f;
            float y = 0.7f;
            double ratio_x = Math.abs((coordinates.get(i).getLongitude() - centre_x)/(topLeft_x - centre_x));
            double ratio_y = Math.abs((coordinates.get(i).getLatitude() - centre_y)/(topLeft_y - centre_y));

            /*Log.d("ratio x", String.valueOf(ratio_x));
            Log.d("ratio y", String.valueOf(ratio_y));*/
            if (coordinates.get(i).getLongitude() - centre_x < 0) {
                x = (float) -(x * ratio_x);
            } else if (coordinates.get(i).getLongitude() - centre_x > 0) {
                x = (float) (x * ratio_x);
            } else {
                x = 0;
            }

            if (coordinates.get(i).getLatitude() - centre_y < 0) {
                y = (float) -(y * ratio_y);
            } else if (coordinates.get(i).getLatitude() - centre_y > 0) {
                y = (float) (y * ratio_y);
            } else {
                y = 0;
            }

            circles.add(new Circle(x,y));
        }
    }

    public double distanceToDegrees(double dist_nm) {
        double dist_metres = dist_nm * 1852;
        double dist_degrees = dist_metres * 0.000009009;

        return dist_degrees;

    }

    public void createWindwardLeeward() {
        circles.add(new Circle(0.0f,-0.5f));
        circles.add(new Circle(0.0f,0.5f));

    }
}
