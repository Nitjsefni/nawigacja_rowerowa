package com.example.kubas.nawigacja;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.example.kubas.nawigacja.gps.GPSManager;
import com.example.kubas.nawigacja.tracking.Trackable;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class  SendPosition implements Runnable {
    private GPSManager gpsManager = GPSManager.getInstance();
    private boolean stopped = false;
    private Handler handler;
    private List<GeoPoint> route = new ArrayList<>();
    private Trackable trackable;
    private int delayTimeInMilisecounds;

    public SendPosition(Trackable trackable, int delayTimeInMilisecounds) {
        this.trackable = trackable;
        this.delayTimeInMilisecounds = delayTimeInMilisecounds;
        handler = new Handler();
        handler.postDelayed(this, delayTimeInMilisecounds);
    }

    private void refresh() {
        Location location = gpsManager.getBetterActualLocation();
        if (location == null) {
            return;
        }
        GeoPoint currentLocation;
        currentLocation = new GeoPoint(location);
        Log.i("PostPosit", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
        Log.i("PostPosit", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
        if (route.isEmpty() || route.get(route.size() - 1).distanceTo(currentLocation) < 60) {
            route.add(currentLocation);
            Log.i("PostPosit", "Route: " + route.toString());
            trackable.refreshTrackingPosition(route, location);
        }
        gpsManager.clearAvgLocation();
    }

    private String getLocationInfo(Location location) {
        return "Lat:" + location.getLatitude() +
                "\nLon:" + location.getLongitude() +
                "\nDokładność:" + location.getAccuracy() + "m" +
                "\nData:" + new Date(location.getTime()).toLocaleString();
    }

    @Override
    public void run() {
        refresh();
        if (!stopped) {
            handler.postDelayed(this, delayTimeInMilisecounds);
        }
    }

    public void stop() {
        stopped = true;
    }

    public void clear() {
        route.clear();
    }

    public void add(GeoPoint point) {
        route.add(point);
    }
}

