package com.example.sailinglayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    TextView Start;
    private int Request_code = 1;
    BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Start=findViewById(R.id.Start);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);




    }
    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment=null;

                    switch (item.getItemId()) {
                        case R.id.nav_variables:
                            selectedFragment=new VariablesFragment();
                            break;
                        case R.id.nav_bouyCoor:
                            selectedFragment=new BuoyCoorFragment();
                            break;
                        case R.id.nav_layout:
                            selectedFragment=new LayoutFragment();
                            break;
                        case R.id.nav_compass:
                            selectedFragment=new CompassFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,selectedFragment).commit();

                    return true;
                }
            };

    public void Start_onclick(View view) {
        Intent intent=new Intent();
        intent.setClass(getApplicationContext(),MainActivity2.class);
        startActivityForResult(intent,Request_code);
    }

    public void Navi_onclick(View view) {

    }

    public void Setting_onclick(View view) {
    }
}
