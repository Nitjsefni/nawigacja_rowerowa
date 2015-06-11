package com.example.kubas.nawigacja.gps;

import android.location.Location;
import android.util.Log;

public class ActualLocationManager {
    private static int ACCURACY_LIMIT = 50;
    private Location actualLocation;
    private double avgLatitude;
    private double avgLongitude;
    private int locationCount;
    private float locationWeight;
    private String gpsStatus;

    public Location getActualLocation() {
        Location location = actualLocation;
        if (location != null && location.getAccuracy() > 7) {
            location = new Location(location);
            location.setLatitude(getAvgLatitude());
            location.setLongitude(getAvgLongitude());
            location.setAccuracy(getAvgAccuracy());
        }
        return location;
    }

    public void clearAvgLocation() {
        locationCount = 0;
        locationWeight = 0;
        avgLatitude = 0;
        avgLongitude = 0;
        actualLocation = null;
    }

    public void setLocation(Location location) {
        addAvgLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        if (actualLocation == null || actualLocation.getAccuracy() >= location.getAccuracy()) {
            this.actualLocation = location;
        }
    }

    private void addAvgLocation(double avgLatitude, double avgLongitude, float accuracy) {
        if (accuracy > ACCURACY_LIMIT) {
            accuracy = ACCURACY_LIMIT;
        }
        accuracy = ACCURACY_LIMIT + 1 - accuracy;
        this.avgLatitude += avgLatitude * accuracy;
        this.avgLongitude += avgLongitude * accuracy;
        this.locationCount++;
        this.locationWeight += accuracy;
    }

    private double getAvgLatitude() {
        return avgLatitude / locationWeight;
    }

    private double getAvgLongitude() {
        return avgLongitude / locationWeight;
    }

    private float getAvgAccuracy() {
        return ((ACCURACY_LIMIT + 1) * locationCount - locationWeight) / locationCount;
    }

    public String getGpsStatus() {
        return gpsStatus;
    }

    public void setGpsStatus(String gpsStatus) {
        this.gpsStatus = gpsStatus;
    }
}
