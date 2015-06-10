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

public class SendPosition implements Runnable {
    private GPSManager gpsManager = GPSManager.getInstance();
    private boolean stopped = false;
    private Handler handler;
    private List<GeoPoint> route = new ArrayList<>();
    private Trackable trackable;
    private int delayTimeInMilisecounds;
    private GeoPoint point;

    public SendPosition(Trackable trackable, int delayTimeInMilisecounds) {
        this.trackable = trackable;
        this.delayTimeInMilisecounds = delayTimeInMilisecounds;
        handler = new Handler();
        handler.postDelayed(this, delayTimeInMilisecounds);
    }

    private void refresh() {

        //Location location = getLocationToPrint();
        Location location = gpsManager.getBetterActualLocation();
        if (location == null) {
            return;
        }
        if (!stopped) {
            GeoPoint currentLocation;
            if (route.size() == 0) {
                return;
            }
            currentLocation = new GeoPoint(location);
            Log.i("PostPosit", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
            Log.i("PostPosit", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
            if (route.get(route.size() - 1).distanceTo(currentLocation) < 60) {
                route.add(currentLocation);
                Log.i("PostPosit", "Route: " + route.toString());
                trackable.refreshMapPosition(currentLocation);
                trackable.refreshTrackingPosition(route);
            }
            point = currentLocation;
        } else {
            GeoPoint gp = point;
            point = new GeoPoint(location);
            Log.i("PostPosit", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
            Log.i("PostPosit", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
            trackable.clearTrackingPositions();
            if (gp.distanceTo(point) < 60) {
                trackable.refreshMapPosition(point);
            }
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
//		handler.removeCallbacks(this);
    }

    public void clear() {
        route.clear();
    }

    public void add(GeoPoint point) {
        route.add(point);
    }
}

