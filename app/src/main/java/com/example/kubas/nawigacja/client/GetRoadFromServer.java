package com.example.kubas.nawigacja.client;

import android.util.Log;

import com.example.kubas.nawigacja.RouteActivity;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.routing.PrintRoute;

import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class GetRoadFromServer implements Runnable {
    private RouteActivity routeActivity;
    private RoutePoints points;
    private RoadManager roadManager;
    private PrintRoute action;

    public GetRoadFromServer(RouteActivity routeActivity, RoutePoints points, RoadManager roadManager, PrintRoute action) {
        this.routeActivity = routeActivity;
        this.points = points;
        this.roadManager = roadManager;
        this.action = action;
    }

    public void run() {
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        if (points.isStartPoint()) {
            waypoints.add(points.getStartPoint().getGeoPoint());
        }
        if (points.isMidPoint()) {
            waypoints.add(points.getMidPoint().getGeoPoint());
        }
        if (points.isEndPoint()) {
            waypoints.add(points.getEndPoint().getGeoPoint());
        }
        try {
            action.setRoad(roadManager.getRoad(waypoints));
            routeActivity.runOnUiThread(action);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
        }
    }
}
