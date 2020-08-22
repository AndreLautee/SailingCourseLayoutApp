package com.example.sailinglayoutapp;

import android.location.Location;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NavMapGLRenderer implements GLSurfaceView.Renderer {

    private ArrayList<Location> coordinates;
    NavMap navMap;
    List<Circle> circles;
    Circle cursor;
    double x_dispersion;
    double y_dispersion;
    Location currentLocation;


    NavMapGLRenderer(ArrayList<Location> coords, Location location) {
        coordinates = coords;
        currentLocation = location;
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
        GLES20.glClearColor(0.5f, 0.5f, 1.0f, 1.0f);
        circles = new ArrayList<>();
        createMap(currentLocation);

    }

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        for (int i=0; i < circles.size(); i++) {
            circles.get(i).draw(vPMatrix);
        }
        cursor.draw(vPMatrix);
    }

    public void createMap(Location location) {
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

        double max_xy_dispersion;

        if (upmost_coord - centre_y > rightmost_coord - centre_x) {
            max_xy_dispersion = upmost_coord - centre_y;
        } else {
            max_xy_dispersion = rightmost_coord - centre_x;
        }

        double topLeft_x = centre_x - max_xy_dispersion;
        double topLeft_y = centre_y + max_xy_dispersion;

        /*
        Log.d("max dispersion", String.valueOf(max_xy_dispersion));
        Log.d("course distance", String.valueOf(course_distance));
        Log.d("topLeft x", String.valueOf(topLeft_x));
        Log.d("centre x", String.valueOf(centre_x));
        Log.d("coord 0 x", String.valueOf(coordinates.get(0).getLongitude()));
        Log.d("coord 1 x", String.valueOf(coordinates.get(1).getLongitude()));
        //Log.d("coord 2 x", String.valueOf(coordinates.get(2).getLongitude()));
        //Log.d("coord 3 x", String.valueOf(coordinates.get(3).getLongitude()));
        Log.d("topLeft y", String.valueOf(topLeft_y));
        Log.d("centre y", String.valueOf(centre_y));
        Log.d("coord 0 y", String.valueOf(coordinates.get(0).getLatitude()));
        Log.d("coord 1 y", String.valueOf(coordinates.get(1).getLatitude()));
        //Log.d("coord 2 y", String.valueOf(coordinates.get(2).getLatitude()));
        //Log.d("coord 3 y", String.valueOf(coordinates.get(3).getLatitude()));
        Log.d("number of coords", String.valueOf(coordinates.size())); */

        float x = 0.7f;
        float y = 0.7f;
        double ratio_x = Math.abs((location.getLongitude() - centre_x)/(topLeft_x - centre_x));
        double ratio_y = Math.abs((location.getLatitude() - centre_y)/(topLeft_y - centre_y));

        if (location.getLongitude() - centre_x < 0) {
            x = (float) -(x * ratio_x);
        } else if (location.getLongitude() - centre_x > 0) {
            x = (float) (x * ratio_x);
        } else {
            x = 0;
        }

        if (location.getLatitude() - centre_y < 0) {
            y = (float) -(y * ratio_y);
        } else if (location.getLatitude() - centre_y > 0) {
            y = (float) (y * ratio_y);
        } else {
            y = 0;
        }

        cursor = new Circle(x,y,false);

        for (int i = 0; i < coordinates.size(); i++) {
            x = 0.7f;
            y = 0.7f;
            ratio_x = Math.abs((coordinates.get(i).getLongitude() - centre_x)/(topLeft_x - centre_x));
            ratio_y = Math.abs((coordinates.get(i).getLatitude() - centre_y)/(topLeft_y - centre_y));

            //Log.d("ratio x", String.valueOf(ratio_x));
            //Log.d("ratio y", String.valueOf(ratio_y));
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

            circles.add(new Circle(x,y,true));
        }
    }


}
