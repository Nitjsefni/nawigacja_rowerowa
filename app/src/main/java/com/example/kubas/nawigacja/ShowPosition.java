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

public class ShowPosition implements Runnable {
    private GPSManager gpsManager = GPSManager.getInstance();
    private Handler handler;
    private Trackable trackable;
    private int delayTimeInMilisecounds;
    private boolean stopped;

    public ShowPosition(Trackable trackable, int delayTimeInMilisecounds) {
        this.trackable = trackable;
        this.delayTimeInMilisecounds = delayTimeInMilisecounds;
        handler = new Handler();
        handler.postDelayed(this, delayTimeInMilisecounds);
    }

    private void refresh() {
        Location location = gpsManager.getActualLocation();
        if (location == null) {
            return;
        }
        Log.i("PostPosit", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
        Log.i("PostPosit", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
        trackable.refreshMapPosition(location);
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

}

