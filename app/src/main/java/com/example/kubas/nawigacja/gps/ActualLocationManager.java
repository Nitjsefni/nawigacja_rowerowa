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
    private String gpsProvider;
    private int gpsStatusStatus;
    private Location previousLocation;

    public Location getActualLocation() {
        Location location = actualLocation;
        if (location != null && location.getAccuracy() > 7) {
            location = getAverageLocation(location);
        }
        if (location == null) {
            location = previousLocation;
        }

        return location;
    }

    private boolean isAverageLocationAvaliable() {
        return getAvgLatitude() != 0 || getAvgLongitude() != 0 || getAvgAccuracy() != 0;
    }

    private Location getAverageLocation(Location location) {
        location = new Location(location);
        location.setLatitude(getAvgLatitude());
        location.setLongitude(getAvgLongitude());
        location.setAccuracy(getAvgAccuracy());
        return location;
    }

    public void clearActualLocation() {
        Location location = getActualLocation();
        if (location != null) {
            previousLocation = location;
        }
        actualLocation = null;
        locationCount = 0;
        locationWeight = 0;
        avgLatitude = 0;
        avgLongitude = 0;
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
    public String getGpsStatusProvider() {
        return gpsProvider;
    }
    public int getGpsStatusStatus() {
        return gpsStatusStatus;
    }

    public void setGpsStatus(String gpsStatus) {
        this.gpsStatus = gpsStatus;


    }
    public void setGpsStatus(String gpsStatus, String provider) {
        this.gpsStatus = gpsStatus;
        this.gpsProvider = provider;

    }
    public void setGpsStatus(String gpsStatus, String provider, int status) {
        this.gpsStatus = gpsStatus;
        this.gpsProvider = provider;
        this.gpsStatusStatus = status;

    }
}
