package com.example.sailinglayoutapp;


import android.location.Location;
import android.location.LocationManager;
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
        GLES20.glClearColor(0.384314f, 0.9372549f, 1.0f, 1.0f);

        createMap();

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

        triangle.draw(scratch);

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


    double centre_x = 0;
    double centre_y = 0;
    float x;
    float y;
    double ratio_x;
    double ratio_y;
    int j;

    double leftmost_coord;
    double rightmost_coord;
    double upmost_coord;
    double downmost_coord;

    double max_xy_dispersion;

    public void createMap() {

        leftmost_coord = coordinates.get(0).getLongitude();
        rightmost_coord = coordinates.get(0).getLongitude();
        upmost_coord = coordinates.get(0).getLatitude();
        downmost_coord = coordinates.get(0).getLatitude();

        double xTotal = 0;
        double yTotal = 0;
        // Find left, right, up and down most coord
        // These coords are used in calculations to find a ratio for drawing
        for (int i = 0; i < coordinates.size(); i++) {
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
            xTotal += coordinates.get(i).getLongitude();
            yTotal += coordinates.get(i).getLatitude();
        }

        // Find centre coord to use as reference
        centre_x = xTotal / coordinates.size();
        centre_y = yTotal / coordinates.size();


        // Find max dispersion from centre so a ratio can be determined
        max_xy_dispersion = 0;
        max_xy_dispersion = Math.sqrt(Math.pow(coordinates.get(0).getLongitude() - coordinates.get(1).getLongitude(),2)
                + Math.pow(coordinates.get(0).getLatitude() - coordinates.get(1).getLatitude(),2));

        // For trapezoid or optimist courses the max dispersion may not be dispersion between marks 1 and 4
        // Check for this
        if (coordinates.size() == 4) {
            if (max_xy_dispersion < (Math.sqrt(Math.pow(coordinates.get(3).getLongitude() - coordinates.get(1).getLongitude(),2)
                    + Math.pow(coordinates.get(3).getLatitude() - coordinates.get(1).getLatitude(),2)))) {
                max_xy_dispersion = Math.sqrt(Math.pow(coordinates.get(3).getLongitude() - coordinates.get(1).getLongitude(),2)
                        + Math.pow(coordinates.get(3).getLatitude() - coordinates.get(1).getLatitude(),2));
            }
        }
        // Dispersion is from centre of screen so divide length by 2
        max_xy_dispersion = max_xy_dispersion/2;

        positionTriangle();
        positionCircles();
    }

    double prevRatioX = 1;
    double prevRatioY = 1;
    public void positionTriangle() {
        float length = 0.15f;
        x = 0.7f;
        y = 0.7f;

        if (locations.size() == 1) {
            j = 0;
        } else {
            j = 1;
        }

        ratio_x = Math.abs((locations.get(j).getLongitude() - centre_x) / max_xy_dispersion);
        ratio_y = Math.abs((locations.get(j).getLatitude() - centre_y) / max_xy_dispersion);

        // Scale the map so that the user is always visible
        // even when they go outside the perimeters
        if (ratio_x > 1) {
            mScale *= (prevRatioX/ratio_x);
            prevRatioX = ratio_x;
        }

        if (ratio_y > 1) {
            mScale *= (prevRatioY/ratio_y);
            prevRatioY = ratio_y;
        }

        // Make sure scale is 1 when user is within perimeters
        if (ratio_x <= 1 && ratio_y <= 1) {
            mScale = 1;
        }

        // Don't let the object get too small or too large.
        mScale = Math.max(0.2f, Math.min(mScale, 5.0f));

        // Find x ratio to draw user location
        if (locations.get(j).getLongitude() - centre_x < 0) {
            x = (float) -(x * ratio_x);
        } else if (locations.get(j).getLongitude() - centre_x > 0) {
            x = (float) (x * ratio_x);
        } else {
            x = 0;
        }

        // Find y ratio to draw user location
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

        // Draw user location
        triangle = new Triangle(new float[] {x,y,0.0f,x2,y2,0.0f,x3,y3,0.0f});
    }

    public void positionCircles() {
        circles = new ArrayList<>();

        for (int i = 0; i < coordinates.size(); i++) {
            x = 0.7f;
            y = 0.7f;

            ratio_x = Math.abs((coordinates.get(i).getLongitude() - centre_x) / max_xy_dispersion);

            ratio_y = Math.abs((coordinates.get(i).getLatitude() - centre_y) / max_xy_dispersion);


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

            if (i == selectedMark) {
                circles.add(new Circle(x,y,-1, 0.05f));
            } else {
                circles.add(new Circle(x,y,0,0.03f));
            }
        }
    }


    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

}
