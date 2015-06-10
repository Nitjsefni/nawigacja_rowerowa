package com.example.kubas.nawigacja.tracking;


import org.osmdroid.util.GeoPoint;

import java.util.List;

public interface Trackable {
    void refreshMapPosition(GeoPoint currentLocation);

    void refreshTrackingPosition(List<GeoPoint> route);

    void clearTrackingPositions();
}
