package com.example.kubas.nawigacja.tracking;

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
    private int delayTimeInMilliseconds;

    public SendPosition(Trackable trackable, int delayTimeInMilliseconds) {
        this.trackable = trackable;
        this.delayTimeInMilliseconds = delayTimeInMilliseconds;
        handler = new Handler();
        handler.postDelayed(this, delayTimeInMilliseconds);
    }

    private void refresh() {
        Location location = gpsManager.getActualLocation();
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
    }

    @Override
    public void run() {
        refresh();
        if (!stopped) {
            handler.postDelayed(this, delayTimeInMilliseconds);
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

