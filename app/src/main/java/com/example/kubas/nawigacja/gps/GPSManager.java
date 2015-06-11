package com.example.kubas.nawigacja.gps;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;

import org.osmdroid.util.GeoPoint;

public class GPSManager implements Runnable {
    private static int gpsRefreshTime;
    private static int increaseAccuracyAlgorithmDuration;
    private static GPSManager instance;
    private final ActualLocationManager actualLocationManager;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isActive;
    private Handler handler;

    private GPSManager(LocationManager locationManager) throws Exception {
        this.locationManager = locationManager;
        this.actualLocationManager = new ActualLocationManager();
        this.locationListener = new GPSLocationListener(actualLocationManager);
        start();
    }

    private static void loadSetings(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        gpsRefreshTime = sharedPref.getInt("gpsRefreshTime", 500);
        increaseAccuracyAlgorithmDuration = sharedPref.getInt("increaseAccuracyAlgorithmDuration", 4000);

    }

    public static void init(Context context) throws Exception {
        if (instance == null) {
            loadSetings(context);
            instance = new GPSManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        }
    }

    public static GPSManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("GPS Manager not initialized");
        }
        return instance;
    }

    public Location getActualLocation() {
        return actualLocationManager.getActualLocation();
    }

    public GeoPoint getActualPosition() {
        return new GeoPoint(actualLocationManager.getActualLocation());
    }

    public void stop() {
        locationManager.removeUpdates(locationListener);
        isActive = false;
    }
    public String getGPSStatus(){
        return actualLocationManager.getGpsStatus();
    }

    @Override
    public void run() {
        actualLocationManager.clearAvgLocation();
        if (isActive) {
            handler.postDelayed(this, increaseAccuracyAlgorithmDuration);
        }
    }

    private void start() throws Exception {
        Location loc = null;
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsRefreshTime, 0, locationListener);
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            actualLocationManager.setLocation(loc);
        }
        if (loc == null && isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, gpsRefreshTime, 0, locationListener);
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            actualLocationManager.setLocation(loc);
        } else {
            throw new Exception("Nie można uruchomić GPS");
        }
        isActive = true;
        handler = new Handler();
        handler.postDelayed(this, increaseAccuracyAlgorithmDuration);
    }
}
