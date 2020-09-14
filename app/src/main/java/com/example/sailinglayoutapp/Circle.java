package com.example.sailinglayoutapp;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Circle {
    private final String vertexShaderCode =

            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int vPMatrixHandle;
    private FloatBuffer vertexBuffer;
    private final int mProgram;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    //static float[] vertices;
    private int points = 360;

    int vertexCount = 100;
    float radius = 0.03f;
    float center_x;
    float center_y;
    float center_z = 0.0f;

    // Set color with red, green, blue and alpha (opacity) values
    float[] color = { 0.0f, 0.0f, 0.0f, 1.0f };

    public Circle(float cntr_x, float cntr_y, int clr, float r) {
        radius = r;
        switch(clr) {
            case -1:
                color = new float[]{0.39215686f, 0.84705882f, 0.79607843f, 1.0f};
                break;
            case 0:
                color = new float[]{0.03921569f, 0.18039216f, 0.16470588f, 0.6f};
                break;
        }

        center_x = cntr_x;
        center_y = cntr_y;
        // Create a buffer for vertex data
        float buffer[] = new float[vertexCount*3]; // (x,y) for each vertex
        int idx = 0;

// Center vertex for triangle fan
        buffer[idx++] = center_x;
        buffer[idx++] = center_y;
        buffer[idx++] = center_z;

// Outer vertices of the circle
        int outerVertexCount = vertexCount-1;

        for (int i = 0; i < outerVertexCount; ++i){
            float percent = (i / (float) (outerVertexCount-1));
            float rad = (float) (percent * 2*Math.PI);

            //Vertex position
            float outer_x = (float) (center_x + radius * Math.cos(rad));
            float outer_y = (float) (center_y + radius * Math.sin(rad));
            float outer_z = 0.0f;

            buffer[idx++] = outer_x;
            buffer[idx++] = outer_y;
            buffer[idx++] = outer_z;
        }

        //vertexCount = buffer.length/COORDS_PER_VERTEX;
        // initialize vertex byte buffer for triangle coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                buffer.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(buffer);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        int vertexShader = NavMapGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = NavMapGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    private int positionHandle;
    private int colorHandle;

   // private int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, color, 0);
        // Set width of line
        GLES20.glLineWidth(1);

        //Log.d("1", String.valueOf(mvpMatrix[0]));
        //Log.d("2", String.valueOf(mvpMatrix[1]));
        //Log.d("4", String.valueOf(mvpMatrix[2]));

        // get handle to triangle's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
