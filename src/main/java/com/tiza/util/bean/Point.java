package com.tiza.util.bean;

/**
 * Description: Point
 * Author: DIYILIU
 * Update: 2016-04-14 14:34
 */
public class Point {

    private double lng;
    private double lat;

    public Point() {
    }

    public Point(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
