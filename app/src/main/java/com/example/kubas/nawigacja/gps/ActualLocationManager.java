package com.example.kubas.nawigacja.gps;

import android.location.Location;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

public class ActualLocationManager {
    private Location actualLocation;
    private double avgLatitude;
    private double avgLongitude;
    private int locationCount;
    private float locationWeight;
    private static int ACCURANCY_LIMIT = 50;

    public void addAvgLocation(double avgLatitude, double avgLongitude,
                               float accuracy) {
        if (accuracy > ACCURANCY_LIMIT) {
            accuracy = ACCURANCY_LIMIT;
        }
        accuracy = ACCURANCY_LIMIT + 1 - accuracy;
        this.avgLatitude += avgLatitude * accuracy;
        this.avgLongitude += avgLongitude * accuracy;
        this.locationCount++;
        this.locationWeight += accuracy;
    }

    public double getAvgLatitude() {
        return avgLatitude / locationWeight;
    }

    public double getAvgLongitude() {
        return avgLongitude / locationWeight;
    }

    public float getAvgAccurancy() {
        return ((ACCURANCY_LIMIT + 1) * locationCount - locationWeight)
                / locationCount;
    }

    public void clearAvgLocation() {
        locationCount = 0;
        locationWeight = 0;
        avgLatitude = 0;
        avgLongitude = 0;
        actualLocation = null;
    }

    public Location getActualLocation() {
        return actualLocation;
    }

    public GeoPoint getActualGeoPoint() {
        return new GeoPoint(getActualLocation());
    }

    public void setActualLocation(Location actualLocation) {
        this.actualLocation = actualLocation;
    }

    public Location getBetterLocation(Location location, Location actualLocation) {
        addAvgLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        if (actualLocation == null) {
            return location;
        }
        if (actualLocation.getAccuracy() >= location.getAccuracy()) {
            return location;
        }
        return actualLocation;
    }

    public void setBetterLocation(Location location) {
        this.actualLocation = getBetterLocation(location, this.actualLocation);
    }
    public Location getBetterActualLocation() {
        Location location = getActualLocation();
        if (location!=null && location.getAccuracy() > 7) {
            location = new Location(location);
            location.setLatitude(getAvgLatitude());
            location.setLongitude(getAvgLongitude());
            location.setAccuracy(getAvgAccurancy());

        }
        if (location!=null) {
            Log.i("LsitenerLocationManager", String.valueOf(location.getLatitude()) + String.valueOf(location.getLongitude()));
        }
        return location;
    }
}
