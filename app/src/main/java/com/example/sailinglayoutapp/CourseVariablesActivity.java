package com.example.sailinglayoutapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

public class CourseVariablesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_variables);

        final RadioGroup radioGroup_angle = findViewById(R.id.radioGroup_angle);
        final RadioGroup radioGroup_reach = findViewById(R.id.radioGroup_reach);
        final RadioGroup radioGroup_secondBeat = findViewById(R.id.radioGroup_secondBeat);
        final TextView textView_angle = findViewById(R.id.textView_angle);
        final TextView textView_reach = findViewById(R.id.textView_reach);
        final TextView textView_secondBeat = findViewById(R.id.textView_secondBeat);


        Spinner spinner = findViewById(R.id.spinner_shape);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.course_shapes, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(parent.getItemAtPosition(position).toString()) {
                    case "Triangle":
                        radioGroup_angle.setVisibility(View.VISIBLE);
                        radioGroup_reach.setVisibility(View.INVISIBLE);
                        radioGroup_secondBeat.setVisibility(View.INVISIBLE);
                        textView_angle.setVisibility(View.VISIBLE);
                        textView_reach.setVisibility(View.INVISIBLE);
                        textView_secondBeat.setVisibility(View.INVISIBLE);
                        String[] triangleStrings = getResources().getStringArray(R.array.triangle_angles);
                        for (int i = 0; i < radioGroup_angle.getChildCount(); i++) {
                            ((RadioButton) radioGroup_angle.getChildAt(i)).setText(triangleStrings[i]);
                        }
                        break;
                    case "Trapezoid":
                        radioGroup_angle.setVisibility(View.VISIBLE);
                        radioGroup_reach.setVisibility(View.VISIBLE);
                        radioGroup_secondBeat.setVisibility(View.VISIBLE);
                        textView_angle.setVisibility(View.VISIBLE);
                        textView_reach.setVisibility(View.VISIBLE);
                        textView_secondBeat.setVisibility(View.VISIBLE);
                        String[] trapezoidStrings = getResources().getStringArray(R.array.trapezoid_angles);
                        for (int i = 0; i < radioGroup_angle.getChildCount(); i++) {
                            ((RadioButton) radioGroup_angle.getChildAt(i)).setText(trapezoidStrings[i]);
                        }
                        break;
                    default:
                        radioGroup_angle.setVisibility(View.INVISIBLE);
                        radioGroup_reach.setVisibility(View.INVISIBLE);
                        radioGroup_secondBeat.setVisibility(View.INVISIBLE);
                        textView_angle.setVisibility(View.INVISIBLE);
                        textView_reach.setVisibility(View.INVISIBLE);
                        textView_secondBeat.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        final Button button = findViewById(R.id.button_calculate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //CourseVariablesObject courseVariablesObject = createObject();
                // Need to get location here
                //MarkerCoordCalculations(location, courseVariablesObject);
            }
        });
    }


/*
    public CourseVariablesObject createObject() {
        String shape = null;
        double bearing;
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
        bearing = Double.parseDouble(editText_bearing.getText().toString());

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

 */
}

