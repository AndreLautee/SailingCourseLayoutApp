package com.example.sailinglayoutapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CourseVariablesActivity extends AppCompatActivity {

    private RadioGroup radioGroup_type, radioGroup_angle, radioGroup_reach, radioGroup_secondBeat;
    private Spinner spinner_shape;
    private EditText editText_wind, editText_distance;
    private TextView textView_angle, textView_reach, textView_secondBeat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_variables);

        radioGroup_type = findViewById(R.id.radioGroup_type);
        spinner_shape = findViewById(R.id.spinner_shape);
        editText_wind = findViewById(R.id.editText_bearing);
        editText_distance = findViewById(R.id.editText_distance);
        radioGroup_angle = findViewById(R.id.radioGroup_angle);
        radioGroup_reach = findViewById(R.id.radioGroup_reach);
        radioGroup_secondBeat = findViewById(R.id.radioGroup_secondBeat);
        textView_angle = findViewById(R.id.textView_angle);
        textView_reach = findViewById(R.id.textView_reach);
        textView_secondBeat = findViewById(R.id.textView_secondBeat);

        setSpinner();

        Intent intent = getIntent();
        Bundle userInput = intent.getExtras();
        if (userInput != null) {
            radioGroup_type.check(userInput.getInt("TYPE"));
            spinner_shape.setSelection(userInput.getInt("SHAPE"));
            editText_wind.setText(userInput.getString("BEARING"));
            editText_distance.setText(userInput.getString("DISTANCE"));
            radioGroup_angle.check(userInput.getInt("ANGLE"));
            radioGroup_reach.check(userInput.getInt("REACH"));
            radioGroup_secondBeat.check(userInput.getInt("SECOND_BEAT"));

        }
         /*   Integer type = savedInstanceState.getInt("TYPE");
            String bearing = savedInstanceState.getString("BEARING");

            if(type != null) {radioGroup_type.check(type);}
            if(bearing != null) {editText_wind.setText(bearing);}*/



        spinner_shape.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

                Bundle userInput = new Bundle();
                userInput.putInt("TYPE", radioGroup_type.getCheckedRadioButtonId());
                userInput.putInt("SHAPE", spinner_shape.getSelectedItemPosition());
                userInput.putString("BEARING", editText_wind.getText().toString());
                userInput.putString("DISTANCE", editText_distance.getText().toString());
                userInput.putInt("ANGLE", radioGroup_angle.getCheckedRadioButtonId());
                userInput.putInt("REACH", radioGroup_reach.getCheckedRadioButtonId());
                userInput.putInt("SECOND_BEAT", radioGroup_secondBeat.getCheckedRadioButtonId());

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),MainActivity2.class);
                intent.putExtras(userInput);
                startActivity(intent);
                //CourseVariablesObject courseVariablesObject = createObject();
                // Need to get location here
                //MarkerCoordCalculations(location, courseVariablesObject);
            }
        });
    }


    public void setSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.course_shapes, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_shape.setAdapter(adapter);
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

