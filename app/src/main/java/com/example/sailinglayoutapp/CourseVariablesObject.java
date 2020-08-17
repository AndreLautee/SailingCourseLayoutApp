package com.example.sailinglayoutapp;

public class CourseVariablesObject {

    private String shape;
    private double bearing;
    private double distance;
    private int angle;
    private String type;
    private String reach;
    private String secondBeat;

    CourseVariablesObject() {}

    CourseVariablesObject(String shape, double bearing, double distance, int angle, String type, String reach, String secondBeat) {
        this.shape = shape;
        this.bearing = getBearingsInRadians(bearing);
        this.distance = distance;
        this.angle = angle;
        this.type = type;
        this.reach = reach;
        this.secondBeat = secondBeat;
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

    String getReach() { return reach; }

    public void setReach(String reach) { this.reach = reach; }

    String getSecondBeat() {
        return secondBeat;
    }

    public void setSecondBeat(String secondBeat) {
        this.secondBeat = secondBeat;
    }

    double getBearingsInRadians(double bearing) {
        return (bearing*Math.PI)/180;
    }
}
