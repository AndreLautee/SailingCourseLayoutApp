package com.example.sailinglayoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class WeatherAPIActivity extends AppCompatActivity {

    TextView t1_temp,t2_city,t3_description,t4_date,t5_wind,t6_wind1,t7_humidity,t8_pressure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_api);

        Button btnBack = findViewById(R.id.btn_weatherBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        t1_temp = (TextView)findViewById(R.id.text_weatherTemp);
        t2_city = (TextView)findViewById(R.id.text_weatherName);
        t3_description = (TextView)findViewById(R.id.text_weatherDesc);
        t4_date = (TextView)findViewById(R.id.text_weatherDate);
        t5_wind = (TextView)findViewById(R.id.text_weatherWindSp);
        t6_wind1 = (TextView)findViewById(R.id.text_weatherWindDir);
        t7_humidity = (TextView)findViewById(R.id.text_weatherHumidity);
        t8_pressure = (TextView)findViewById(R.id.text_weatherPressure);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Weather");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_black_24dp);
        }


        find_weather();
    }

    public void find_weather()
    {

        Intent gpsIntent = getIntent();
        Location location = gpsIntent.getParcelableExtra("LOCATION");

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units=imperial&appid=57df42a409e4c7c20a3221979d61174d";;

        if(!isOnline())
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Info");
            alertDialog.setMessage("Internet not available!");
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

            alertDialog.show();
        }
        else
        {

            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try
                {

                    JSONObject main_object = response.getJSONObject("main");
                    JSONObject wind_object = response.getJSONObject("wind");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String temp = String.valueOf(main_object.getDouble("temp"));
                    String humidity = String.valueOf(main_object.getDouble("humidity"));
                    String pressure = String.valueOf(main_object.getDouble("pressure"));
                    String description = object.getString("description");
                    String city = response.getString("name");
                    String wind = String.valueOf(wind_object.getDouble("speed"));
                    String wind1 = String.valueOf(wind_object.getDouble("deg"));


                    // t1_temp.setText(temp);
                    t2_city.setText(city);
                    t3_description.setText(description);
                    t5_wind.setText(wind);
                    t6_wind1.setText(wind1);
                    t7_humidity.setText(humidity);
                    t8_pressure.setText(pressure);

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE d, MMMM");
                    String formatted_date = sdf.format(calendar.getTime());
                    Log.d("date", calendar.getTime()+"");
                    t4_date.setText(formatted_date);

                    double temp_int = Double.parseDouble(temp);
                    double centi = (temp_int -32)/1.8000;
                    centi = Math.round(centi);
                    int i = (int)centi;
                    t1_temp.setText(String.valueOf(i));




                }catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Response Error", error.toString());
            }
        }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);

    }
}

    public boolean isOnline(){
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;


            default:
                return super.onOptionsItemSelected(item);

        }
    }
}