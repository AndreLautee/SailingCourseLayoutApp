package com.example.sailinglayoutapp;

import android.graphics.PointF;
import android.location.Location;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NavMapGLRenderer implements GLSurfaceView.Renderer {

    private ArrayList<Location> coordinates;
    List<Circle> circles;
    Triangle triangle;
    Triangle arrow;
    Line northSouth, eastWest;
    private ArrayList<Location> locations;
    private int selectedMark;
    private double bearingDirection;

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }


    public double getBearingDirection() {
        return bearingDirection;
    }

    public void setBearingDirection(double bearing) {
        this.bearingDirection = deg2rad(bearing);
    }


    public int getSelectedMark() {
        return selectedMark;
    }

    public void setSelectedMark(int selectedMark) {
        this.selectedMark = selectedMark;
    }


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
        GLES20.glClearColor(0.6f, 0.8f, 1.0f, 1.0f);
        circles = new ArrayList<>();
        //cursors = new ArrayList<>();
        createMap();
        //arrow = new Triangle(new float[]{0.80f,0.95f,0.0f,0.75f,0.85f,0.0f,0.85f,0.85f,0.0f});
        //northSouth = new Line(new float[]{0.80f,0.9f,0.0f,0.80f,0.6f,0.0f});
        //eastWest = new Line(new float[]{0.65f,0.75f,0.0f,0.95f,0.75f,0.0f});

    }

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] rotationMatrix = new float[16];
    private final float[] scaleMatrix = new float[16];
    private final float[] translationMatrix = new float[16];
    private final float[] intermediateMatrix = new float[16];
    private final float[] scratch = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] compassMatrix = new float[16];
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


        // Create a rotation
        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Create a scale
        Matrix.setIdentityM(scaleMatrix,0);
        Matrix.scaleM(scaleMatrix, 0, mScale, mScale, mScale);

        // Create a translation
        Matrix.setIdentityM(translationMatrix,0);
        Matrix.translateM(translationMatrix, 0, mOffsetX, mOffsetY, 0);


        // Model = scale * rotation * translation
        Matrix.multiplyMM(intermediateMatrix, 0, scaleMatrix, 0, translationMatrix, 0);
        Matrix.multiplyMM(modelMatrix,0,intermediateMatrix,0,rotationMatrix,0);

        // Combine the model matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0,modelMatrix,0);

        for (int i=0; i < circles.size(); i++) {
            circles.get(i).draw(scratch);
        }
      /*  for (int i=0; i < cursors.size(); i++) {
            cursors.get(i).draw(vPMatrix);
        }*/
        triangle.draw(scratch);
        //arrow.draw(compassMatrix);
        //northSouth.draw(compassMatrix);
        //eastWest.draw(compassMatrix);
    }

    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public float mScale;
    public void setScale(float scale) {
        mScale = scale;
    }

    public float mOffsetX;
    public float mOffsetY;
    public void setOffsetX(float offsetX) {
        mOffsetX += offsetX;
    }
    public void setOffsetY(float offsetY) {
        mOffsetY += offsetY;
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

        float length = 0.15f;

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

        if (ratio_x > 1) {
            if (leftmost_coord > locations.get(j).getLongitude()) {
                leftmost_coord = locations.get(j).getLongitude();
            }
            if (rightmost_coord < locations.get(j).getLongitude()) {
                rightmost_coord = locations.get(j).getLongitude();
            }
            max_xy_dispersion = (rightmost_coord - leftmost_coord)/2;
            ratio_x = Math.abs((locations.get(j).getLongitude() - centre_x) / max_xy_dispersion);
            ratio_y = Math.abs((locations.get(j).getLatitude() - centre_y) / max_xy_dispersion);
        }

        if (ratio_y > 1) {
            if (upmost_coord < locations.get(j).getLatitude()) {
                upmost_coord = locations.get(j).getLatitude();
            }
            if (downmost_coord > locations.get(j).getLatitude()) {
                downmost_coord = locations.get(j).getLatitude();
            }
            max_xy_dispersion = (upmost_coord - downmost_coord)/2;
            ratio_x = Math.abs((locations.get(j).getLongitude() - centre_x) / max_xy_dispersion);
            ratio_y = Math.abs((locations.get(j).getLatitude() - centre_y) / max_xy_dispersion);
        }

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

        float x2 = (float) (Math.sin(bearingDirection + (Math.PI/2))*(length/3)) + XmidBase;
        float y2 = (float) (Math.cos(bearingDirection + (Math.PI/2))*(length/3)) + YmidBase;
        float x3 = (float) (Math.sin(bearingDirection - (Math.PI/2))*(length/3)) + XmidBase;
        float y3 = (float) (Math.cos(bearingDirection - (Math.PI/2))*(length/3)) + YmidBase;

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

            //Log.d("ratio x", String.valueOf(ratio_x));
            //Log.d("ratio y", String.valueOf(ratio_y));
            if (coordinates.get(i).getLongitude() - centre_x < 0) {
                x = (float) -(x * ratio_x);
            } else if (coordinates.get(i).getLongitude() - centre_x > 0) {
                x = (float) (x * ratio_x);
            } else {
                x = 0;
            }
            //Log.d("X", String.valueOf(x));


            if (coordinates.get(i).getLatitude() - centre_y < 0) {
                y = (float) -(y * ratio_y);
            } else if (coordinates.get(i).getLatitude() - centre_y > 0) {
                y = (float) (y * ratio_y);
            } else {
                y = 0;
            }
            //Log.d("Y", String.valueOf(y));


            //Log.d("Selected mark", String.valueOf(selectedMark));

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
