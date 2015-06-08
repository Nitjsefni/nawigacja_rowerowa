package com.example.kubas.nawigacja;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;

import java.util.Date;

/**
 * Created by KubaS on 2015-06-06.
 */
public class PostPosition implements Runnable{
    private MapActivity activity;
    private long delayTimeInMilisecounds;
    private boolean stopped = false;
    private Handler handler;

    public PostPosition(MapActivity activity, int delayTimeInMilisecounds) {
        this.activity = activity;
        handler = new Handler();
        handler.postDelayed(this, delayTimeInMilisecounds);

    }

    private void refresh() {

        //Location location = activity.getLocationToPrint();
        Location location = activity.getActualLocation();
        if (location == null) {
            return;
        }
        if (location.getAccuracy() > 7) {
            location = new Location(location);
            location.setLatitude(activity.getAvgLatitude());
            location.setLongitude(activity.getAvgLongitude());
            location.setAccuracy(activity.getAvgAccurancy());
        }


        //activity.setLocationToPrint(location);
        if(activity.tracking) {
            if(activity.route.size() != 0)
                activity.currentLocation = new GeoPoint(location);
            Log.i("PostPosit", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
            Log.i("PostPosit", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
            {
                if(activity.route.get(activity.route.size()-1).distanceTo(activity.currentLocation)<60)
                {

                    activity.route.add(activity.currentLocation);
                    Log.i("PostPosit", "Route: " + activity.route.toString());
                    activity.lastKnown = activity.currentLocation;
                    activity.mapController.setCenter(activity.currentLocation);
                    // activity.map.getOverlays().clear();
                    activity.roadOverlay.setPoints(activity.route);
                    Marker startMarker = new Marker(activity.map);
                    startMarker.setPosition(activity.startPoint);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    activity.map.getOverlays().add(startMarker);
                    activity.map.getOverlays().add(activity.roadOverlay);
                    activity.mapController.setZoom(17);
                    activity.mapController.setCenter(activity.currentLocation);
                    activity.map.invalidate();
                }
            }


        }
        else
        {
            GeoPoint gp = activity.startPoint;
            activity.startPoint = new GeoPoint(location);
            Log.i("PostPosit", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
            Log.i("PostPosit", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());
            activity.map.getOverlays().clear();
            if(gp.distanceTo(activity.startPoint)<60) {
                activity.lastKnown = activity.startPoint;
                Marker startMarker = new Marker(activity.map);
                startMarker.setPosition(activity.startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                activity.map.getOverlays().add(startMarker);
                activity.mapController.setZoom(17);
                activity.mapController.setCenter(activity.startPoint);
                activity.map.invalidate();
            }
        }
        activity.setActualLocation(null);
        activity.clearAvgLocation();


    }

    private String getLocationInfo(Location location) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Lat:");
        stringBuilder.append(location.getLatitude());
        stringBuilder.append("\nLon:");
        stringBuilder.append(location.getLongitude());
        stringBuilder.append("\nDok�adno��:");
        stringBuilder.append(location.getAccuracy());
        stringBuilder.append("m \nData:");
        stringBuilder.append(new Date(location.getTime()).toString());
        return stringBuilder.toString();
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

}

