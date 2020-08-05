package com.example.sailinglayoutapp;

public class CourseVariables {

    private String shape;
    private double bearing;
    private double distance;
    private int angle;
    private String type;
    private String trapezoidType;

    public CourseVariables(String shape, String bearing, double distance, int angle, String type, String trapezoidType) {
        this.shape = shape;
        this.bearing = getBearingsInRadians(bearing);
        this.distance = distance;
        this.angle = angle;
        this.type = type;
        this.trapezoidType = trapezoidType;
    }

    String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String getTrapezoidType() {
        return trapezoidType;
    }

    public void setTrapezoidType(String trapezoidType) {
        this.trapezoidType = trapezoidType;
    }

    private double getBearingsInRadians(String bearing) {
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
}
