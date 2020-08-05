package com.example.sailinglayoutapp;


import android.location.Location;

import java.util.ArrayList;

public class MarkerCoordCalculations {

    private ArrayList<Location> coords;
    private CourseVariables courseVariables;

    public MarkerCoordCalculations(Location userCoord, CourseVariables cV) {
        coords = new ArrayList<Location>();
        // Coordinates of user are the coords for starting mark
        // coords[0] is starting mark
        this.coords.add(userCoord);
        this.courseVariables = cV;

        createShape(courseVariables.getShape());
    }

    public void createShape(String shape) {
        switch (shape) {
            case "triangle":
                if (createTriangle()) {
                    //success
                } else {
                    //failure
                }
                break;
            case "straight":
                if (createStraight()) {
                    //success
                } else {
                    //failure
                }
                break;
            case "trapezoid":
                if (createTrapezoid()) {
                    //success
                } else {
                    //failure
                }
                break;
            default:
                // error
                break;
        }
    }

    private boolean createTriangle() {
        // Add coordinates to mark A
        if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                courseVariables.getDistance(),courseVariables.getBearing()))) {
            //Return false if cannot add coordinate
            return false;
        }

        // Check for starboard (right side) or portboard (left side)
        if(courseVariables.getType().equals("starboard")) {
            if(courseVariables.getAngle() == 45) {
                // Calculate bearing to mark B from mark C
                double bearingToMarkC = courseVariables.getBearing() + Math.PI/4;
                // Calculate distance to mark B from mark C
                double distanceToMarkC = Math.sqrt(Math.pow(courseVariables.getDistance(),2)/2);

                coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                        distanceToMarkC,bearingToMarkC));
            } else if (courseVariables.getAngle() == 60) {
                // Calculate bearing to mark B from mark C
                double bearingToMarkC = courseVariables.getBearing() + Math.PI/3;

                // Use mark C coordinates to calculate mark B
                coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                        courseVariables.getDistance(),bearingToMarkC));
            } else {
                return false;
            }
        } else if (courseVariables.getType().equals("portboard")) {
            if(courseVariables.getAngle() == 45) {
                // Calculate bearing to mark B from mark C
                double bearingToMarkC = courseVariables.getBearing() + (7*Math.PI)/4;
                // Calculate distance to mark B from mark C
                double distanceToMarkC = Math.sqrt(Math.pow(courseVariables.getDistance(),2)/2);

                coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                        distanceToMarkC,bearingToMarkC));
            } else if (courseVariables.getAngle() == 60) {
                // Calculate bearing to mark B from mark C
                double bearingToMarkC = courseVariables.getBearing() + (5*Math.PI)/3;

                // Use mark C coordinates to calculate mark B
                coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                        courseVariables.getDistance(),bearingToMarkC));
            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean createStraight() {
        // Add coordinates to mark A
        //Return false if cannot add coordinate
        return coords.add(getCoordinates(coords.get(0).getLatitude(), coords.get(0).getLongitude(),
                courseVariables.getDistance(), courseVariables.getBearing()));
    }

    private boolean createTrapezoid() {
        // Add coordinates to mark A
        if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                courseVariables.getDistance(),courseVariables.getBearing()))) {
            //Return false if cannot add coordinate
            return false;
        }

        // Check for starboard (right side) or portboard (left side)
        if(courseVariables.getType().equals("starboard")) {
            if(courseVariables.getAngle() == 60) {
                // Calculate bearing to mark B from mark A
                double bearingToMarkB = courseVariables.getBearing() + (2*Math.PI)/3;
                // Calculate distance to mark B from mark A
                double distanceToMarkB = courseVariables.getDistance()/2;

                // Use mark A coordinates to calculate mark B
                coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                        distanceToMarkB,bearingToMarkB));

                // Calculate bearing to mark C from mark B
                double bearingToMarkC = courseVariables.getBearing() + Math.PI;

                // Check for short or long second straight before calculating mark C
                if(courseVariables.getTrapezoidType().equals("short")) {
                    // Calculate distance to mark C from mark B
                    double distanceToMarkC = courseVariables.getDistance()*0.66;

                    // Use mark B coordinates to calculate mark C
                    coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                            distanceToMarkC,bearingToMarkC));
                } else if(courseVariables.getTrapezoidType().equals("long")) {
                    // Use mark B coordinates to calculate mark C
                    coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                            courseVariables.getDistance(),bearingToMarkC));
                }
            } else if (courseVariables.getAngle() == 70) {
                // Calculate bearing to mark B from mark A
                double bearingToMarkB = courseVariables.getBearing() + (11*Math.PI)/18;
                // Calculate distance to mark B from mark A
                double distanceToMarkB = courseVariables.getDistance()/2;

                // Use mark A coordinates to calculate mark B
                coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                        distanceToMarkB,bearingToMarkB));

                // Calculate bearing to mark C from mark B
                double bearingToMarkC = courseVariables.getBearing() + Math.PI;
                // Check for short or long second straight before calculating mark C
                if(courseVariables.getTrapezoidType().equals("short")) {
                    // Calculate distance to mark C from mark B
                    double distanceToMarkC = courseVariables.getDistance()*0.66;

                    // Use mark B coordinates to calculate mark C
                    coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                            distanceToMarkC,bearingToMarkC));
                } else if(courseVariables.getTrapezoidType().equals("long")) {
                    // Use mark B coordinates to calculate mark C
                    coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                            courseVariables.getDistance(),bearingToMarkC));
                }
            } else {
                return false;
            }
        } else if(courseVariables.getType().equals("portboard")) {
            if(courseVariables.getAngle() == 60) {
                // Calculate bearing to mark B from mark A
                double bearingToMarkB = courseVariables.getBearing() + (4*Math.PI)/3;
                // Calculate distance to mark B from mark A
                double distanceToMarkB = courseVariables.getDistance()/2;

                // Use mark A coordinates to calculate mark B
                coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                        distanceToMarkB,bearingToMarkB));

                // Calculate bearing to mark C from mark B
                double bearingToMarkC = courseVariables.getBearing() + Math.PI;
                // Check for short or long second straight before calculating mark C
                if(courseVariables.getTrapezoidType().equals("short")) {
                    // Calculate distance to mark C from mark B
                    double distanceToMarkC = courseVariables.getDistance()*0.66;

                    // Use mark B coordinates to calculate mark C
                    coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                            distanceToMarkC,bearingToMarkC));
                } else if(courseVariables.getTrapezoidType().equals("long")) {
                    // Use mark B coordinates to calculate mark C
                    coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                            courseVariables.getDistance(),bearingToMarkC));
                }
            } else if (courseVariables.getAngle() == 70) {
                // Calculate bearing to mark B from mark A
                double bearingToMarkB = courseVariables.getBearing() + (25*Math.PI)/18;
                // Calculate distance to mark B from mark A
                double distanceToMarkB = courseVariables.getDistance()/2;

                // Use mark A coordinates to calculate mark B
                coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                        distanceToMarkB,bearingToMarkB));

                // Calculate bearing to mark C from mark B
                double bearingToMarkC = courseVariables.getBearing() + Math.PI;
                // Check for short or long second straight before calculating mark C
                if(courseVariables.getTrapezoidType().equals("short")) {
                    // Calculate distance to mark C from mark B
                    double distanceToMarkC = courseVariables.getDistance()*0.66;

                    // Use mark B coordinates to calculate mark C
                    coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                            distanceToMarkC,bearingToMarkC));
                } else if(courseVariables.getTrapezoidType().equals("long")) {
                    // Use mark B coordinates to calculate mark C
                    coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                            courseVariables.getDistance(),bearingToMarkC));
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }



    private Location getCoordinates(double latDeg, double lonDeg, double dist, double bearRad) {
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
