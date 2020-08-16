package com.example.sailinglayoutapp;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LayoutGLRenderer implements GLSurfaceView.Renderer {

    Map<String, String> variables;
    Triangle triangle;

    public LayoutGLRenderer(Map<String, String> vars) {
        variables = vars;
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
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (variables.get("TYPE").equals("starboard")) {
            triangle = new Triangle(new float[]{-0.5f,0.5f,0.0f,-0.5f,-0.5f,0.0f,0.5f,0.0f,0.0f});
        } else if (variables.get("TYPE").equals("portboard")) {
            triangle = new Triangle(new float[]{0.5f,0.5f,0.0f,-0.5f,0.0f,0.0f,0.5f,-0.5f,0.0f});
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        triangle.draw();

    }
}
