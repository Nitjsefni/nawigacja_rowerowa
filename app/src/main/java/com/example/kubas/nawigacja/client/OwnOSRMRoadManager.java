package com.example.kubas.nawigacja.client;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class OwnOSRMRoadManager extends OSRMRoadManager {
    public OwnOSRMRoadManager() {
        setService(ServerAddress.getServerUrl() + "webservices/viaroute?");
    }

    @Override
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        Road road = super.getRoad(waypoints);
        for (GeoPoint point : road.mRouteHigh) {
            point.setCoordsE6(point.getLatitudeE6() * 10, point.getLongitudeE6() * 10);
        }
        return road;
    }
}
