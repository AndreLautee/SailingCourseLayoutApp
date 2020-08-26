package com.example.sailinglayoutapp;

import android.location.Location;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NavMapGLRenderer implements GLSurfaceView.Renderer {

    private ArrayList<Location> coordinates;
    NavMap navMap;
    List<Circle> circles;
    List<Circle> cursors;
    Triangle triangle;
    ArrayList<Location> locations;
    int selectedMark;
    double bearingDirection;


    NavMapGLRenderer(ArrayList<Location> coords, ArrayList<Location> lct, int sM, double bearing) {
        coordinates = coords;
        locations = lct;
        selectedMark = sM;
        bearingDirection = deg2rad(bearing);
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
        //cursors = new ArrayList<>();
        createMap();
    }

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
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
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 5, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        for (int i=0; i < circles.size(); i++) {
            circles.get(i).draw(vPMatrix);
        }
      /*  for (int i=0; i < cursors.size(); i++) {
            cursors.get(i).draw(vPMatrix);
        }*/
        triangle.draw(vPMatrix);
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

        double max_xy_dispersion;

        if (upmost_coord - downmost_coord > rightmost_coord - leftmost_coord) {
            max_xy_dispersion = (upmost_coord - downmost_coord)/2;
        } else {
            max_xy_dispersion = (rightmost_coord - leftmost_coord)/2;
        }

/*
        Log.d("max dispersion", String.valueOf(max_xy_dispersion));
        //Log.d("course distance", String.valueOf(course_distance));
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
        Log.d("number of coords", String.valueOf(coordinates.size()));
*/

        float x;
        float y;
        double ratio_x;
        double ratio_y;

        float length = 0.1f;

        x = 0.7f;
        y = 0.7f;

        int j;
        if (locations.size() == 1) {
            j = 0;
        } else {
            j = 1;
        }

        ratio_x = Math.abs((locations.get(j).getLongitude() - centre_x) / max_xy_dispersion);

        ratio_y = Math.abs((locations.get(j).getLatitude() - centre_y) / max_xy_dispersion);

        if (locations.get(j).getLongitude() - centre_x < 0) {
            x = (float) -(x * ratio_x);
        } else if (locations.get(j).getLongitude() - centre_x > 0) {
            x = (float) (x * ratio_x);
        } else {
            x = 0;
        }

        if (locations.get(j).getLatitude() - centre_y < 0) {
            y = (float) -(y * ratio_y);
        } else if (locations.get(j).getLatitude() - centre_y > 0) {
            y = (float) (y * ratio_y);
        } else {
            y = 0;
        }

        float XmidBase = (float) (-Math.sin(bearingDirection)*length) + x;
        float YmidBase = (float) (-Math.cos(bearingDirection)*length) + y;

        float x2 = (float) (Math.sin(bearingDirection + (Math.PI/2))*(length/2)) + XmidBase;
        float y2 = (float) (Math.cos(bearingDirection + (Math.PI/2))*(length/2)) + YmidBase;
        float x3 = (float) (Math.sin(bearingDirection - (Math.PI/2))*(length/2)) + XmidBase;
        float y3 = (float) (Math.cos(bearingDirection - (Math.PI/2))*(length/2)) + YmidBase;

        triangle = new Triangle(new float[] {x,y,0.0f,x2,y2,0.0f,x3,y3,0.0f});
/*
        for (int j = 0; j < locations.size(); j++) {
            x = 0.7f;
            y = 0.7f;

            ratio_x = Math.abs((locations.get(j).getLongitude() - centre_x) / max_xy_dispersion);

            ratio_y = Math.abs((locations.get(j).getLatitude() - centre_y) / max_xy_dispersion);

            if (locations.get(j).getLongitude() - centre_x < 0) {
                x = (float) -(x * ratio_x);
            } else if (locations.get(j).getLongitude() - centre_x > 0) {
                x = (float) (x * ratio_x);
            } else {
                x = 0;
            }

            if (locations.get(j).getLatitude() - centre_y < 0) {
                y = (float) -(y * ratio_y);
            } else if (locations.get(j).getLatitude() - centre_y > 0) {
                y = (float) (y * ratio_y);
            } else {
                y = 0;
            }

            cursors.add(new Circle(x,y,locations.size()-j, 0.03f));
        }
*/


        for (int i = 0; i < coordinates.size(); i++) {
            x = 0.7f;
            y = 0.7f;

            ratio_x = Math.abs((coordinates.get(i).getLongitude() - centre_x) / max_xy_dispersion);

            ratio_y = Math.abs((coordinates.get(i).getLatitude() - centre_y) / max_xy_dispersion);

            Log.d("ratio x", String.valueOf(ratio_x));
            Log.d("ratio y", String.valueOf(ratio_y));
            if (coordinates.get(i).getLongitude() - centre_x < 0) {
                x = (float) -(x * ratio_x);
            } else if (coordinates.get(i).getLongitude() - centre_x > 0) {
                x = (float) (x * ratio_x);
            } else {
                x = 0;
            }
            Log.d("X", String.valueOf(x));


            if (coordinates.get(i).getLatitude() - centre_y < 0) {
                y = (float) -(y * ratio_y);
            } else if (coordinates.get(i).getLatitude() - centre_y > 0) {
                y = (float) (y * ratio_y);
            } else {
                y = 0;
            }
            Log.d("Y", String.valueOf(y));


            Log.d("Selected mark", String.valueOf(selectedMark));

            if (i == selectedMark) {
                circles.add(new Circle(x,y,-1, 0.05f));
            } else {
                circles.add(new Circle(x,y,-1,0.03f));
            }
        }
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

}
