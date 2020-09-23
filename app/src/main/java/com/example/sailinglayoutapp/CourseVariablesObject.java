package com.example.sailinglayoutapp;

import android.os.Parcel;
import android.os.Parcelable;

public class CourseVariablesObject implements Parcelable {

    private String type;
    private double lat;
    private double lon;
    private String shape;
    private double bearing;
    private double distance;
    private int angle;
    private double reach;
    private String secondBeat;

    CourseVariablesObject() {}

    public CourseVariablesObject(String type, double lat, double lon, String shape, double bearing, double distance, int angle, double reach, String secondBeat) {
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.shape = shape;
        this.bearing = getBearingsInRadians(bearing);
        this.distance = distance;
        this.angle = angle;
        this.reach = reach;
        this.secondBeat = secondBeat;
    }

    public CourseVariablesObject(String type, String shape, double bearing, double distance, int angle, double reach, String secondBeat) {
        this.type = type;
        this.shape = shape;
        this.bearing = getBearingsInRadians(bearing);
        this.distance = distance;
        this.angle = angle;
        this.reach = reach;
        this.secondBeat = secondBeat;
    }

    protected CourseVariablesObject(Parcel in) {
        type = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
        shape = in.readString();
        bearing = in.readDouble();
        distance = in.readDouble();
        angle = in.readInt();
        reach = in.readDouble();
        secondBeat = in.readString();
    }

    public static final Creator<CourseVariablesObject> CREATOR = new Creator<CourseVariablesObject>() {
        @Override
        public CourseVariablesObject createFromParcel(Parcel in) {
            return new CourseVariablesObject(in);
        }

        @Override
        public CourseVariablesObject[] newArray(int size) {
            return new CourseVariablesObject[size];
        }
    };

    double getLat() {return lat;}

    public void setLat(double latitude) {this.lat = latitude;}

    double getLon() {return lon;}

    public void setLon(double longitude) {this.lon = longitude;}

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
        this.bearing = getBearingsInRadians(bearing);
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

    double getReach() { return reach; }

    public void setReach(double reach) { this.reach = reach; }

    String getSecondBeat() {
        return secondBeat;
    }

    public void setSecondBeat(String secondBeat) {
        this.secondBeat = secondBeat;
    }

    double getBearingsInRadians(double bearing) {
        return (bearing*Math.PI)/180;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeString(shape);
        dest.writeDouble(bearing);
        dest.writeDouble(distance);
        dest.writeInt(angle);
        dest.writeDouble(reach);
        dest.writeString(secondBeat);
    }
}
