package com.example.kubas.nawigacja.data.model;

import android.location.Location;

import com.example.kubas.nawigacja.routing.RoutingUtil;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

public class GeoPosition implements Serializable {
    private String name;
    private String address;
    private PointType type;
    private GeoPoint geoPoint;

    public GeoPosition(String name, GeoPoint geoPoint) {
        this.name = name;
        this.geoPoint = geoPoint;
    }

    public GeoPosition(String name, double lat, double lng) {
        this.name = name;
        geoPoint = new GeoPoint(lat, lng);
    }

    public GeoPosition(String name, double lat, double lng, String address, PointType type) {
        this(name, lat, lng);
        this.address = address;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return geoPoint.getLatitude();
    }

    public double getLng() {
        return geoPoint.getLongitude();
    }

    public String getAddress() {
        return address;
    }

    public PointType getType() {
        return type;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public String getDescription() {
        return getLat() + " " + getLng();
    }

    public Location getLocation() {
        return RoutingUtil.convertToLocation(geoPoint);
    }
}
