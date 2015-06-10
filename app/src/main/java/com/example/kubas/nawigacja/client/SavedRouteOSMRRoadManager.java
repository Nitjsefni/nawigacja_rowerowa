package com.example.kubas.nawigacja.client;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class SavedRouteOSMRRoadManager extends OSRMRoadManager {
    public SavedRouteOSMRRoadManager(int id) {
        setService(ServerAddress.getInstance().getServerUrl() + "webservices/getRoute?id=" + id + "&" + ServerAddress.getInstance().getParameters() + "&");
    }

    @Override
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        Road road = super.getRoad(waypoints);
        return road;
    }
}
