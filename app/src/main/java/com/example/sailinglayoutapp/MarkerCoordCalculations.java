package com.example.sailinglayoutapp;


import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

public class MarkerCoordCalculations implements Parcelable {

    private ArrayList<Location> coords;
    private CourseVariablesObject courseVariablesObject;

    MarkerCoordCalculations(CourseVariablesObject cV) {
        this.courseVariablesObject = cV;

        coords = new ArrayList<>();
        // Coordinates of user are the coords for starting mark
        // coords[0] is starting mark
        Location userLocation = new Location(LocationManager.GPS_PROVIDER);
        userLocation.setLatitude(courseVariablesObject.getLat());
        userLocation.setLongitude(courseVariablesObject.getLon());
        this.coords.add(userLocation);

        if(!createShape(courseVariablesObject.getShape())) {
            coords = null;
        };
    }

    protected MarkerCoordCalculations(Parcel in) {
        coords = in.createTypedArrayList(Location.CREATOR);
        courseVariablesObject = in.readParcelable(CourseVariablesObject.class.getClassLoader());
    }


    public static final Creator<MarkerCoordCalculations> CREATOR = new Creator<MarkerCoordCalculations>() {
        @Override
        public MarkerCoordCalculations createFromParcel(Parcel in) {
            return new MarkerCoordCalculations(in);
        }

        @Override
        public MarkerCoordCalculations[] newArray(int size) {
            return new MarkerCoordCalculations[size];
        }
    };

    ArrayList<Location> getCoords() {
        return coords;
    }

    void setCoords(ArrayList<Location> coords) {
        this.coords = coords;
    }

    public CourseVariablesObject getCourseVariablesObject() {
        return courseVariablesObject;
    }

    public void setCourseVariablesObject(CourseVariablesObject courseVariablesObject) {
        this.courseVariablesObject = courseVariablesObject;
    }

    public boolean createShape(String shape) {
        switch (shape) {
            case "triangle":
                return (createTriangle());
            case "windward_leeward":
                return (createWindwardLeeward());
            case "trapezoid":
                return (createTrapezoid());
            case "optimist":
                return (createOptimist());
            default:
                return false;
        }
    }

    private boolean createTriangle() {
        // Add coordinates to mark A
        if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                courseVariablesObject.getDistance(), courseVariablesObject.getBearing()))) {
            //Return false if cannot add coordinate
            return false;
        }

        // Check for starboard (right side) or portboard (left side)
        if(courseVariablesObject.getType().equals("starboard")) {
            if(courseVariablesObject.getAngle() == 45) {
                // Calculate bearing to mark B from mark C
                double bearingToMarkC = courseVariablesObject.getBearing() + Math.PI/4;
                // Calculate distance to mark B from mark C
                double distanceToMarkC = Math.sqrt(Math.pow(courseVariablesObject.getDistance(),2)/2);

                if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                        distanceToMarkC,bearingToMarkC))) {
                    //Return false if cannot add coordinate
                    return false;
                };
            } else if (courseVariablesObject.getAngle() == 60) {
                // Calculate bearing to mark B from mark C
                double bearingToMarkC = courseVariablesObject.getBearing() + Math.PI/3;

                // Use mark C coordinates to calculate mark B
                if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                        courseVariablesObject.getDistance(),bearingToMarkC))) {
                    //Return false if cannot add coordinate
                    return false;
                };
            } else {
                return false;
            }
        } else if (courseVariablesObject.getType().equals("portboard")) {
            if(courseVariablesObject.getAngle() == 45) {
                // Calculate bearing to mark B from mark C
                double bearingToMarkC = courseVariablesObject.getBearing() + (7*Math.PI)/4;
                // Calculate distance to mark B from mark C
                double distanceToMarkC = Math.sqrt(Math.pow(courseVariablesObject.getDistance(),2)/2);
                if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                        distanceToMarkC,bearingToMarkC))) {
                    //Return false if cannot add coordinate
                    return false;
                }
            } else if (courseVariablesObject.getAngle() == 60) {
                // Calculate bearing to mark B from mark C
                double bearingToMarkC = courseVariablesObject.getBearing() + (5*Math.PI)/3;

                // Use mark C coordinates to calculate mark B
                if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                        courseVariablesObject.getDistance(),bearingToMarkC))) {
                    //Return false if cannot add coordinate
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean createWindwardLeeward() {
        // Add coordinates to mark A
        //Return false if cannot add coordinate
        return this.coords.add(getCoordinates(coords.get(0).getLatitude(), coords.get(0).getLongitude(),
                courseVariablesObject.getDistance(), courseVariablesObject.getBearing()));
    }

    private boolean createTrapezoid() {
        // Add coordinates to mark A
        if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                courseVariablesObject.getDistance(), courseVariablesObject.getBearing()))) {
            //Return false if cannot add coordinate
            return false;
        }

        // Check for starboard (right side) or portboard (left side)
        if(courseVariablesObject.getType().equals("starboard")) {
            if(courseVariablesObject.getAngle() == 60) {
                // Calculate bearing to mark B from mark A
                double bearingToMarkB = courseVariablesObject.getBearing() + (2*Math.PI)/3;
                // Calculate distance to mark B from mark A
                double distanceToMarkB = courseVariablesObject.getReach()*courseVariablesObject.getDistance();

                // Use mark A coordinates to calculate mark B
                if(!coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                        distanceToMarkB,bearingToMarkB))) {
                    //Return false if cannot add coordinate
                    return false;
                }

                // Calculate distance to mark C from mark D
                double distanceToMarkC = courseVariablesObject.getReach()*courseVariablesObject.getDistance();
                // Check for short or long second straight before calculating mark C
                if(courseVariablesObject.getSecondBeat().equals("short")) {
                    // Calculate bearing to mark C from mark D
                    double bearingToMarkC = courseVariablesObject.getBearing() + (Math.PI)/3;

                    // Use mark D coordinates to calculate mark C
                    if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                            distanceToMarkC,bearingToMarkC))) {
                        //Return false if cannot add coordinate
                        return false;
                    }
                } else if(courseVariablesObject.getSecondBeat().equals("long")) {
                    // Calculate bearing to mark C from mark D
                    double bearingToMarkC = courseVariablesObject.getBearing() + (2*Math.PI)/3;

                    // Use mark D coordinates to calculate mark C
                    if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                            distanceToMarkC,bearingToMarkC))) {
                        //Return false if cannot add coordinate
                        return false;
                    }
                }
            } else if (courseVariablesObject.getAngle() == 70) {
                // Calculate bearing to mark B from mark A
                double bearingToMarkB = courseVariablesObject.getBearing() + (11*Math.PI)/18;
                // Calculate distance to mark B from mark A
                double distanceToMarkB = courseVariablesObject.getReach()*courseVariablesObject.getDistance();

                // Use mark A coordinates to calculate mark B
                if(!coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                        distanceToMarkB,bearingToMarkB))) {
                    //Return false if cannot add coordinate
                    return false;
                }

                // Calculate distance to mark C from mark D
                double distanceToMarkC = courseVariablesObject.getReach()*courseVariablesObject.getDistance();
                // Check for short or long second straight before calculating mark C
                if(courseVariablesObject.getSecondBeat().equals("short")) {
                    // Calculate bearing to mark C from mark D
                    double bearingToMarkC = courseVariablesObject.getBearing() + (7*Math.PI)/18;

                    // Use mark D coordinates to calculate mark C
                    if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                            distanceToMarkC,bearingToMarkC))) {
                        //Return false if cannot add coordinate
                        return false;
                    }
                } else if(courseVariablesObject.getSecondBeat().equals("long")) {
                    // Calculate bearing to mark C from mark D
                    double bearingToMarkC = courseVariablesObject.getBearing() + (11*Math.PI)/18;

                    // Use mark D coordinates to calculate mark C
                    if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                            distanceToMarkC,bearingToMarkC))) {
                        //Return false if cannot add coordinate
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else if(courseVariablesObject.getType().equals("portboard")) {
            if(courseVariablesObject.getAngle() == 60) {
                // Calculate bearing to mark B from mark A
                double bearingToMarkB = courseVariablesObject.getBearing() + (4*Math.PI)/3;
                // Calculate distance to mark B from mark A
                double distanceToMarkB = courseVariablesObject.getReach()*courseVariablesObject.getDistance();

                // Use mark A coordinates to calculate mark B
                if(!coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                        distanceToMarkB,bearingToMarkB))) {
                    //Return false if cannot add coordinate
                    return false;
                }

                // Calculate distance to mark C from mark D
                double distanceToMarkC = courseVariablesObject.getReach()*courseVariablesObject.getDistance();

                // Check for short or long second straight before calculating mark C
                if(courseVariablesObject.getSecondBeat().equals("short")) {
                    // Calculate bearing to mark C from mark D
                    double bearingToMarkC = courseVariablesObject.getBearing() + (5*Math.PI)/3;

                    // Use mark D coordinates to calculate mark C
                    if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                            distanceToMarkC,bearingToMarkC))) {
                        //Return false if cannot add coordinate
                        return false;
                    }
                } else if(courseVariablesObject.getSecondBeat().equals("long")) {
                    // Calculate bearing to mark C from mark D
                    double bearingToMarkC = courseVariablesObject.getBearing() + (4*Math.PI)/3;
                    // Use mark B coordinates to calculate mark C
                    if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                            distanceToMarkC,bearingToMarkC))) {
                        //Return false if cannot add coordinate
                        return false;
                    }
                }
            } else if (courseVariablesObject.getAngle() == 70) {
                // Calculate bearing to mark B from mark A
                double bearingToMarkB = courseVariablesObject.getBearing() + (25*Math.PI)/18;
                // Calculate distance to mark B from mark A
                double distanceToMarkB = courseVariablesObject.getReach()*courseVariablesObject.getDistance();

                // Use mark A coordinates to calculate mark B
                if(!coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                        distanceToMarkB,bearingToMarkB))) {
                    //Return false if cannot add coordinate
                    return false;
                }

                // Calculate distance to mark C from mark D
                double distanceToMarkC = courseVariablesObject.getReach()*courseVariablesObject.getDistance();
                // Check for short or long second straight before calculating mark C
                if(courseVariablesObject.getSecondBeat().equals("short")) {
                    // Calculate bearing to mark C from mark B
                    double bearingToMarkC = courseVariablesObject.getBearing() + (29*Math.PI)/18;

                    // Use mark B coordinates to calculate mark C
                    if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                            distanceToMarkC,bearingToMarkC))) {
                        //Return false if cannot add coordinate
                        return false;
                    }
                } else if(courseVariablesObject.getSecondBeat().equals("long")) {
                    // Calculate bearing to mark C from mark B
                    double bearingToMarkC = courseVariablesObject.getBearing() + (25*Math.PI)/18;
                    // Use mark B coordinates to calculate mark C
                    if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                            distanceToMarkC,bearingToMarkC))) {
                        //Return false if cannot add coordinate
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean createOptimist() {
        // Add coordinates to mark A
        if(!coords.add(getCoordinates(coords.get(0).getLatitude(),coords.get(0).getLongitude(),
                courseVariablesObject.getDistance(), courseVariablesObject.getBearing()))) {
            //Return false if cannot add coordinate
            return false;
        }

        if(courseVariablesObject.getType().equals("starboard")) {
            // Calculate bearing to mark B from mark A
            double bearingToMarkB = courseVariablesObject.getBearing() + (2*Math.PI)/3;
            // Calculate distance to mark B from mark A
            double distanceToMarkB = courseVariablesObject.getDistance();

            // Use mark A coordinates to calculate mark B
            if(!coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                    distanceToMarkB,bearingToMarkB))) {
                //Return false if cannot add coordinate
                return false;
            }

            // Calculate bearing to mark C from mark B
            double bearingToMarkC = courseVariablesObject.getBearing() + Math.PI;
            // Calculate distance to mark C from mark B
            double distanceToMarkC = courseVariablesObject.getDistance();

            // Use mark B coordinates to calculate mark C
            if(!coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                    distanceToMarkC,bearingToMarkC))) {
                //Return false if cannot add coordinate
                return false;
            }

        } else if (courseVariablesObject.getType().equals("portboard")) {
            // Calculate bearing to mark B from mark A
            double bearingToMarkB = courseVariablesObject.getBearing() + (4*Math.PI)/3;
            // Calculate distance to mark B from mark A
            double distanceToMarkB = courseVariablesObject.getDistance();

            // Use mark A coordinates to calculate mark B
            if(!coords.add(getCoordinates(coords.get(1).getLatitude(),coords.get(1).getLongitude(),
                    distanceToMarkB,bearingToMarkB))) {
                //Return false if cannot add coordinate
                return false;
            }

            // Calculate bearing to mark C from mark B
            double bearingToMarkC = courseVariablesObject.getBearing() + Math.PI;
            // Calculate distance to mark C from mark B
            double distanceToMarkC = courseVariablesObject.getDistance();

            // Use mark B coordinates to calculate mark C
            if(!coords.add(getCoordinates(coords.get(2).getLatitude(),coords.get(2).getLongitude(),
                    distanceToMarkC,bearingToMarkC))) {
                //Return false if cannot add coordinate
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
        double d = (dist * 1.852) / r; // convert dist to kms then to arc radians


        double resultLat, resultLon;
        resultLat = Math.asin(Math.sin(latRad) * Math.cos(d) +
                Math.cos(latRad) * Math.sin(d) * Math.cos(bearRad));
        double dlon = Math.atan2(Math.sin(bearRad) * Math.sin(d) *
                Math.cos(latRad), Math.cos(d)-Math.sin(latRad) * Math.sin(latRad));
        resultLon = ((lonRad + dlon + Math.PI) % (2 * Math.PI))-Math.PI;
        resultLat = (resultLat * 180) / Math.PI; // back to degrees
        resultLon = (resultLon * 180) / Math.PI;

        Location l = new Location(LocationManager.GPS_PROVIDER);
        l.setLatitude(resultLat);
        l.setLongitude(resultLon);

        return l;
    }

    // Testing for correct coordinate calculations
    public ArrayList<Double> testGetCoords(double latDeg, double lonDeg, double dist, double bearRad) {
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

        ArrayList<Double> result = new ArrayList<>();
        result.add(resultLat);
        result.add(resultLon);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(coords);
        dest.writeParcelable(courseVariablesObject, flags);
    }
}
