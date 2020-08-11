package com.example.sailinglayoutapp;

public class CourseVariablesObject {

    private String shape;
    private double bearing;
    private double distance;
    private int angle;
    private String type;
    private String trapezoidType;

    CourseVariablesObject() {}

    CourseVariablesObject(String shape, double bearing, double distance, int angle, String type, String trapezoidType) {
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

    double getBearingsInRadians(double bearing) {
        return (bearing*Math.PI)/180;
    }
}
