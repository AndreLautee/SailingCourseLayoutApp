package com.example.sailinglayoutapp;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LayoutGLRenderer implements GLSurfaceView.Renderer {

    Map<String, String> variables;
    Shape shape;
    List<Circle> circles;

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
        circles = new ArrayList<>();
        switch (Objects.requireNonNull(variables.get("SHAPE"))) {
            case "windward_leeward":
                createWindwardLeewardShape();
                break;
            case "triangle":
                createTriangleShape();
                break;
            case "trapezoid":
                createTrapezoidShape();
                break;
            case "optimist":
                createOptimistShape();
                break;

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
        shape.draw();
        for (int i=0; i < circles.size(); i++) {
            circles.get(i).draw();
        }
    }

    public void createWindwardLeewardShape() {
        shape = new Shape(new float[]{0.0f,-0.5f,0.0f,0.0f,0.5f,0.0f}, 2);
        circles.add(new Circle(0.0f,-0.5f));
        circles.add(new Circle(0.0f,0.5f));
    }

    public void createTriangleShape() {
        if (Objects.requireNonNull(variables.get("TYPE")).equals("starboard")) {
            if (Objects.requireNonNull(variables.get("ANGLE")).equals("45")) {
                shape = new Shape(new float[]{-0.5f,0.55f,0.0f,-0.5f,-0.55f,0.0f,0.5f,0.0f,0.0f}, 2);
                circles.add(new Circle(-0.5f,0.55f));
                circles.add(new Circle(-0.5f,-0.55f));
                circles.add(new Circle(0.5f,0.0f));
            } else if (Objects.requireNonNull(variables.get("ANGLE")).equals("60")) {
                shape = new Shape(new float[]{-0.55f,0.45f,0.0f,-0.55f,-0.45f,0.0f,0.55f,0.0f,0.0f}, 2);
                circles.add(new Circle(-0.55f,0.45f));
                circles.add(new Circle(-0.55f,-0.45f));
                circles.add(new Circle(0.55f,0.0f));
            }
        } else if (Objects.requireNonNull(variables.get("TYPE")).equals("portboard")) {
            if (Objects.requireNonNull(variables.get("ANGLE")).equals("45")) {
                shape = new Shape(new float[]{0.5f,-0.55f,0.0f,0.5f,0.55f,0.0f,-0.5f,0.0f,0.0f}, 2);
                circles.add(new Circle(0.5f,-0.55f));
                circles.add(new Circle(0.5f,0.55f));
                circles.add(new Circle(-0.5f,0.0f));
            } else if (Objects.requireNonNull(variables.get("ANGLE")).equals("60")) {
                shape = new Shape(new float[]{0.55f,-0.45f,0.0f,0.55f,0.45f,0.0f,-0.55f,0.0f,0.0f}, 2);
                circles.add(new Circle(0.55f,-0.45f));
                circles.add(new Circle(0.55f,0.45f));
                circles.add(new Circle(-0.55f,0.0f));
            }
        }
    }

    public void createTrapezoidShape() {
        if (Objects.requireNonNull(variables.get("TYPE")).equals("starboard")) {
            if (Objects.requireNonNull(variables.get("SECOND_BEAT")).equals("short")) {
                shape = new Shape(new float[]{0.5f,-0.1f,0.0f,0.5f,0.5f,0.0f,-0.5f,0.7f,0.0f,-0.5f,-0.3f,0.0f}, 2);
                circles.add(new Circle(0.5f,-0.1f));
                circles.add(new Circle(0.5f,0.5f));
                circles.add(new Circle(-0.5f,0.7f));
                circles.add(new Circle(-0.5f,-0.3f));
            } else if (Objects.requireNonNull(variables.get("SECOND_BEAT")).equals("equal")) {
                shape = new Shape(new float[]{0.5f,-0.5f,0.0f,0.5f,0.5f,0.0f,-0.5f,0.7f,0.0f,-0.5f,-0.3f,0.0f}, 2);
                circles.add(new Circle(0.5f,-0.5f));
                circles.add(new Circle(0.5f,0.5f));
                circles.add(new Circle(-0.5f,0.7f));
                circles.add(new Circle(-0.5f,-0.3f));
            }
        } else if (Objects.requireNonNull(variables.get("TYPE")).equals("portboard")) {
            if (Objects.requireNonNull(variables.get("SECOND_BEAT")).equals("short")) {
                shape = new Shape(new float[]{0.5f,-0.3f,0.0f,0.5f,0.7f,0.0f,-0.5f,0.5f,0.0f,-0.5f,-0.1f,0.0f}, 2);
                circles.add(new Circle(0.5f,-0.3f));
                circles.add(new Circle(0.5f,0.7f));
                circles.add(new Circle(-0.5f,0.5f));
                circles.add(new Circle(-0.5f,-0.1f));
            } else if (Objects.requireNonNull(variables.get("SECOND_BEAT")).equals("equal")) {
                shape = new Shape(new float[]{0.5f,-0.3f,0.0f,0.5f,0.7f,0.0f,-0.5f,0.5f,0.0f,-0.5f,-0.5f,0.0f}, 2);
                circles.add(new Circle(0.5f,-0.3f));
                circles.add(new Circle(0.5f,0.7f));
                circles.add(new Circle(-0.5f,0.5f));
                circles.add(new Circle(-0.5f,-0.5f));
            }
        }
    }

    public void createOptimistShape() {
        if (Objects.requireNonNull(variables.get("TYPE")).equals("starboard")) {
            shape = new Shape(new float[]{0.5f,-0.5f,0.0f,0.5f,0.5f,0.0f,-0.5f,0.7f,0.0f,-0.5f,-0.3f,0.0f}, 3);
            circles.add(new Circle(0.5f,-0.5f));
            circles.add(new Circle(0.5f,0.5f));
            circles.add(new Circle(-0.5f,0.7f));
        } else if (Objects.requireNonNull(variables.get("TYPE")).equals("portboard")) {
            shape = new Shape(new float[]{0.5f,-0.3f,0.0f,0.5f,0.7f,0.0f,-0.5f,0.5f,0.0f,-0.5f,-0.5f,0.0f}, 3);
            circles.add(new Circle(0.5f,0.7f));
            circles.add(new Circle(-0.5f,0.5f));
            circles.add(new Circle(-0.5f,-0.5f));
        }
    }
}
