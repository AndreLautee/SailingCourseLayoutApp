package com.example.sailinglayoutapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class WeatherDialogFragment extends DialogFragment {

    public interface WeatherDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, double wind);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    WeatherDialogListener listener;

    double wind;

    WeatherDialogFragment(double w) {
        wind = w;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View v;
        if(wind == -1) {
            v = inflater.inflate(R.layout.dialog_weather_error, null);
            builder.setView(v)
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the negative button event back to the host activity
                            listener.onDialogNegativeClick(WeatherDialogFragment.this);
                        }
                    });
        } else {
            v = inflater.inflate(R.layout.dialog_weather, null);
            View vt = v.findViewById(R.id.weather_dialog_wind);
            ((TextView)vt).setText(String.valueOf(wind));
            builder.setView(v)
                    .setPositiveButton("COPY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the positive button event back to the host activity
                            listener.onDialogPositiveClick(WeatherDialogFragment.this, wind);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the negative button event back to the host activity
                            listener.onDialogNegativeClick(WeatherDialogFragment.this);
                        }
                    });
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorSecondary));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorSecondary));
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (WeatherDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(" must implement WeatherDialogListener");
        }
    }
}