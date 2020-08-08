package com.example.sailinglayoutapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class CourseVariablesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_variables);

        final Button button = findViewById(R.id.button_calculate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                CourseVariablesObject courseVariablesObject = createObject();
                // Need to get location here
                //MarkerCoordCalculations(location, courseVariablesObject);
            }
        });
    }

    public CourseVariablesObject createObject() {
        String shape = null;
        String bearing;
        double distance;
        int angle;
        String type = null;
        String trapezoidType = null;

        // Get shape
        final RadioGroup radioGroup_shape = findViewById(R.id.radioGroup_shape);
        // get selected radio button from radioGroup
        int selectedId = radioGroup_shape.getCheckedRadioButtonId();

        if(selectedId == -1) {
            // Course shape not selected
            // Error
        } else {
            // find the radiobutton by returned id
            RadioButton selectedRadioButton = findViewById(selectedId);
            shape = selectedRadioButton.getText().toString();
        }

        // Get bearing
        final EditText editText_bearing = findViewById(R.id.editText_bearing);
        bearing = editText_bearing.getText().toString();

        // Get bearing
        final EditText editText_distance = findViewById(R.id.editText_distance);
        distance = Double.parseDouble(editText_distance.getText().toString());

        // Get angle
        final EditText editText_angle = findViewById(R.id.editText_angle);
        angle = Integer.parseInt(editText_angle.getText().toString());

        // Get type
        final RadioGroup radioGroup_type = findViewById(R.id.radioGroup_type);
        // get selected radio button from radioGroup
        selectedId = radioGroup_type.getCheckedRadioButtonId();

        if(selectedId == -1) {
            // Course shape not selected
            // Error
        } else {
            // find the radiobutton by returned id
            RadioButton selectedRadioButton = findViewById(selectedId);
            type = selectedRadioButton.getText().toString();
        }

        return new CourseVariablesObject(shape, bearing, distance, angle, type, trapezoidType);

    }
}
