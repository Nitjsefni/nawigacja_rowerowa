package com.example.kubas.nawigacja.tracking;


import android.location.Location;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public interface Trackable {
    void refreshMapPosition(Location location);

    void refreshTrackingPosition(List<GeoPoint> route, Location location);

    void clearTrackingPositions();
}
