package com.example.kubas.nawigacja.gps;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.example.kubas.nawigacja.data.Times;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class GPSManager {
    private static GPSManager instance;
    private ActualLocationManager actualLocationManager;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private IncreaseAccuracyAlgorithm accuracyAlgorithm;

    private GPSManager(LocationManager locationManager) throws Exception {
        this.locationManager = locationManager;
        this.actualLocationManager = new ActualLocationManager();
        this.locationListener = new GPSLocationListener(actualLocationManager);
        start();
    }

    private static void loadSettings(Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public static void init(Context context) throws Exception {
        if (instance == null) {
            loadSettings(context);
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
        if (getActualLocation() == null) {
            return null;
        }
        return new GeoPoint(getActualLocation());
    }

    public ArrayList<String> getGPSStatus() {
        ArrayList<String> gpsstatus = new ArrayList<String>();
        String text = actualLocationManager.getGpsStatus();
        gpsstatus.add(text);
        text = actualLocationManager.getGpsStatusProvider();
        gpsstatus.add(text);
        text = String.valueOf(actualLocationManager.getGpsStatusStatus());
        gpsstatus.add(text);
        return gpsstatus;
    }

    public void stop() {
        locationManager.removeUpdates(locationListener);
        accuracyAlgorithm.stop();
    }


    private void start() throws Exception {
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Times.GPS_REFRESH_TIME, 0, locationListener);
        }
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Times.GPS_REFRESH_TIME, 0, locationListener);
        }
        accuracyAlgorithm = new IncreaseAccuracyAlgorithm(actualLocationManager);
    }

    private class IncreaseAccuracyAlgorithm implements Runnable {
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        private Handler handler;
        private boolean isActive;
        private ActualLocationManager actualLocationManager;

        public IncreaseAccuracyAlgorithm(ActualLocationManager actualLocationManager) {
            this.actualLocationManager = actualLocationManager;
            isActive = true;
            handler = new Handler();
            handler.postDelayed(this, Times.ALGORITHM_INCREASE_ACCURACY_TIME);
        }

        @Override
        public void run() {
            actualLocationManager.clearActualLocation();
            /*ArrayList<String> gpsstatus = getGPSStatus();
            Location loc = null;
            if(gpsstatus.get(0).contains("Zmieniono status"))
            {
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsRefreshTime, 0, locationListener);
                    loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(loc != null) {
                        actualLocationManager.setLocation(loc);
                    }

                }
                if (loc == null && isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, gpsRefreshTime, 0, locationListener);
                    loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    actualLocationManager.setLocation(loc);

                }

            }
            else if (gpsstatus.get(0).contains("Sledzenie uruchomione z wykorzystaniem"))
            {
                loc = null;
                if (isGPSEnabled && gpsstatus.get(1).equals("gps")) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsRefreshTime, 0, locationListener);
                    loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(loc != null) {
                        actualLocationManager.setLocation(loc);
                    }

                }
                if (loc == null && isNetworkEnabled && gpsstatus.get(1).equals("network")) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, gpsRefreshTime, 0, locationListener);
                    loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    actualLocationManager.setLocation(loc);

                }

            }
            else if( gpsstatus.get(0).contains("Utracono sygnał"))
            {
                loc = null;
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsRefreshTime, 0, locationListener);
                    loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(loc != null) {
                        actualLocationManager.setLocation(loc);
                    }

                }
                if (loc == null && isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, gpsRefreshTime, 0, locationListener);
                    loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    actualLocationManager.setLocation(loc);

                }

            }*/
            if (isActive) {
                handler.postDelayed(this, Times.ALGORITHM_INCREASE_ACCURACY_TIME);
            }
        }

        public void stop() {
            isActive = false;
        }
    }
}
