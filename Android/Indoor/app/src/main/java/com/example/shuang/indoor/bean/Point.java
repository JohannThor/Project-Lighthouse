package com.example.shuang.indoor.bean;

/**
 * Created by shuang on 20/07/2016.
 */
public class Point {
    private float pointX;
    private float pointY;
    private double latitude;
    private double longitude;

    public Point(float pointX, float pointY) {
        this.pointX = pointX;
        this.pointY = pointY;
    }

    public Point(float pointX, float pointY, double latitude, double longitude) {
        this.pointX = pointX;
        this.pointY = pointY;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getPointX() {
        return pointX;
    }

    public void setPointX(float pointX) {
        this.pointX = pointX;
    }

    public float getPointY() {
        return pointY;
    }

    public void setPointY(float pointY) {
        this.pointY = pointY;
    }
}
