package com.example.kubas.nawigacja.tracking;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.example.kubas.nawigacja.gps.GPSManager;

public class ShowPosition implements Runnable {
    private GPSManager gpsManager = GPSManager.getInstance();
    private Handler handler;
    private Trackable trackable;
    private int delayTimeInMillisecounds;
    private boolean stopped;

    public ShowPosition(Trackable trackable, int delayTimeInMillisecounds) {
        this.trackable = trackable;
        this.delayTimeInMillisecounds = delayTimeInMillisecounds;
        handler = new Handler();
        handler.postDelayed(this, 0);
    }

    private void refresh() {
        Location location = gpsManager.getActualLocation();
        if (location == null) {
            return;
        }
        Log.i(ShowPosition.class.getName(), "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
        Log.i(ShowPosition.class.getName(), "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
        trackable.refreshMapPosition(location);
    }

    @Override
    public void run() {
        refresh();
        if (!stopped) {
            handler.postDelayed(this, delayTimeInMillisecounds);
        }
    }

    public void stop() {
        stopped = true;
    }

}

