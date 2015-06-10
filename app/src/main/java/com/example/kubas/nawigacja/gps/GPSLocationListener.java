package com.example.kubas.nawigacja.gps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

public class GPSLocationListener implements LocationListener {
    private boolean isGPSEnabled;
    private LocationManager locationManager;
    private boolean isNetworkEnabled;
    private ActualLocationManager actualLocationManager;
    private String gpsStatus;
    private Location initialLocation;

    public GPSLocationListener(LocationManager locationManager,ActualLocationManager actualLocationManager) {
        this.actualLocationManager = actualLocationManager;
        gpsStatus = "Szukanie satelit";
        this.locationManager = locationManager;
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location location) {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        actualLocationManager.setBetterLocation(location);
        Log.i("GPS loc ch", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
        Log.i("GPS loc ch", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        gpsStatus = "Sledzenie uruchomione";
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        gpsStatus = "Utracono sygna³";
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

    }

    public boolean isGPSEnabled() {
        return isGPSEnabled;
    }

    public boolean isNetworkEnabled() {
        return isNetworkEnabled;
    }

    public Location getActualLocation() {
        if (actualLocationManager.getActualLocation() == null) {
            return initialLocation;
        }
        return actualLocationManager.getActualLocation();
    }

    public GeoPoint getActualGeoPoint() {
        return new GeoPoint(getActualLocation().getLatitude(), getActualLocation().getLongitude());
    }

    public String getGpsStatus() {
        return gpsStatus;
    }

    public void setInitialLocation(Location initialLocation) {
        this.initialLocation = initialLocation;
    }
}