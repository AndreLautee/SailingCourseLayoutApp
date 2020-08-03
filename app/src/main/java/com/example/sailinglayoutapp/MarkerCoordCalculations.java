package com.example.sailinglayoutapp;


import android.graphics.Point;
import android.location.Location;

import java.util.ArrayList;

public class MarkerCoordCalculations {

    private ArrayList<Location> coords = new ArrayList<>();

    public MarkerCoordCalculations(Location userCoord, double dist, String bearing, String shape) {
        this.coords.add(userCoord);

        createShape(shape);
    }

    public void createShape(String shape) {
        if (shape.equals("triangle")) {

        } else if (shape.equals("straight")) {

        } else if (shape.equals("trapezoid")) {

        } else {
            // error
        }
    }

    public void createTriangle() {

    }

    public void createStraight() {

    }

    public void createTrapezoid() {

    }
    public double getBearingsInRadians(String bearing) {
        double bearRad;
        switch(bearing) {
            case "N":
                bearRad = 0;
                break;
            case "NNE":
                bearRad = Math.PI/8;
                break;
            case "NE":
                bearRad = Math.PI/4;
                break;
            case "ENE":
                bearRad = (3*Math.PI)/8;
                break;
            case "E":
                bearRad = Math.PI/2;
                break;
            case "ESE":
                bearRad = (5*Math.PI)/8;
                break;
            case "SE":
                bearRad = (3*Math.PI)/4;
                break;
            case "SSE":
                bearRad = (7*Math.PI)/8;
                break;
            case "S":
                bearRad = Math.PI;
                break;
            case "SSW":
                bearRad = (9*Math.PI)/8;
                break;
            case "SW":
                bearRad = (5*Math.PI)/4;
                break;
            case "WSW":
                bearRad = (11*Math.PI)/8;
                break;
            case "W":
                bearRad = (3*Math.PI)/2;
                break;
            case "WNW":
                bearRad = (13*Math.PI)/8;
                break;
            case "NW":
                bearRad = (7*Math.PI)/4;
                break;
            case "NNW":
                bearRad = (15*Math.PI)/8;
                break;
            default:
                bearRad = 9;
        }
        return bearRad;
    }

    public Location getCoordinates(double latDeg, double lonDeg, double dist, double bearRad) {
        double latRad = (latDeg * Math.PI) / 180; // in radians
        double lonRad = (lonDeg * Math.PI) / 180; // in radians
        int r = 6371; // radius of earth in km
        double d = dist / r; // convert dist to arc radians


        double resultLat, resultLon;
        resultLat = Math.asin(Math.sin(latRad) * Math.cos(d) +
                Math.cos(latRad) * Math.sin(d) * Math.cos(bearRad));
        double dlon = Math.atan2(Math.sin(bearRad) * Math.sin(d) *
                Math.cos(latRad), Math.cos(d)-Math.sin(latRad) * Math.sin(latRad));
        resultLon = ((lonRad + dlon + Math.PI) % (2 * Math.PI))-Math.PI;
        resultLat = (resultLat * 180) / Math.PI; // back to degrees
        resultLon = (resultLon * 180) / Math.PI;

        Location l = new Location("dummyprovider");
        l.setLatitude(resultLat);
        l.setLongitude(resultLon);

        return l;
    }
}
