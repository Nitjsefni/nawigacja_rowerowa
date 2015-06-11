package com.example.kubas.nawigacja.gps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

public class GPSLocationListener implements LocationListener {
    private ActualLocationManager actualLocationManager;

    public GPSLocationListener(ActualLocationManager actualLocationManager) {
        this.actualLocationManager = actualLocationManager;
        actualLocationManager.setGpsStatus("Szukanie satelit");
    }

    @Override
    public void onLocationChanged(Location location) {
        actualLocationManager.setLocation(location);
        Log.i("GPS loc ch", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
        Log.i("GPS loc ch", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        actualLocationManager.setGpsStatus("Zmieniono status "+provider+" na "+status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        actualLocationManager.setGpsStatus("Sledzenie uruchomione z wykorzystaniem "+provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        actualLocationManager.setGpsStatus("Utracono sygnał "+provider);
    }

}