package com.example.kubas.nawigacja.gps;

import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

public class GPSManager {
    private static GPSManager instance;
    private final ActualLocationManager actualLocationManager;
    private LocationManager locationManager;
    private GPSLocationListener locationListener;

    private GPSManager(LocationManager locationManager) throws Exception {
        this.locationManager = locationManager;
        this.actualLocationManager = new ActualLocationManager();
        this.locationListener = new GPSLocationListener(locationManager, actualLocationManager);
        start();
    }


    public void start() throws Exception {
        if (locationListener.isGPSEnabled()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationListener.setInitialLocation(loc);
        } else if (locationListener.isNetworkEnabled()) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            locationListener.setInitialLocation(loc);
        } else {
            throw new Exception("Nie można uruchomić GPS");
        }
    }


    public GeoPoint getActualPosition() {
        return locationListener.getActualGeoPoint();
    }

    public Location getActualLocation() {
        return locationListener.getActualLocation();
    }

    public static void init(LocationManager locationManager) throws Exception {
        if (instance == null) {
            instance = new GPSManager(locationManager);
        }
    }

    public static GPSManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("GPS Manager not initialized");
        }
        return instance;
    }

    public Location getBetterActualLocation() {
        return actualLocationManager.getBetterActualLocation();
    }

    public void clearAvgLocation() {
        actualLocationManager.clearAvgLocation();
    }
}
