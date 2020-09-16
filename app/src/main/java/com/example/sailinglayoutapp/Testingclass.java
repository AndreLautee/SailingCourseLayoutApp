package com.example.sailinglayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class Testingclass extends AppCompatActivity {
    TextView Start;
    private int Request_code = 1;
    private BottomSheetBehavior bottomsheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing2);

        LinearLayout windward=findViewById(R.id.bottom_sheet_windward);
        bottomsheet = BottomSheetBehavior.from(windward);

        BottomSheetBehavior.from(windward).setHideable(false);



    }


}
