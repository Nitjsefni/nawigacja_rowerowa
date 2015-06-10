package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.kubas.nawigacja.client.OwnOSRMRoadManager;
import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.gps.GPSManager;
import com.example.kubas.nawigacja.tracking.Trackable;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends Activity implements Trackable {
    private MapView map;
    private RoutePoints points;
    private Road road;
    private ShowPosition showPosition;
    Polyline roadOverlay;
    IMapController mapController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_route);

        map = (MapView) findViewById(R.id.map2);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mapController = map.getController();
        mapController.setZoom(14);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        points = (RoutePoints) extras.get("points");
        if (points.getStartPoint() == null) {
            GPSManager gpsManager = GPSManager.getInstance();
            GeoPoint actualPosition = gpsManager.getActualPosition();
            while (actualPosition == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                actualPosition = gpsManager.getActualPosition();
            }
            points.setStartPoint(new GeoPosition("Aktualna pozycja", actualPosition));
        }
        mapController.setCenter(points.getStartPoint().getGeoPoint());
        Marker startMarker = new Marker(map);
        startMarker.setPosition(points.getStartPoint().getGeoPoint());
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.marker_departure));
        startMarker.setTitle("Start point");

        Marker endMarker = new Marker(map);
        endMarker.setPosition(points.getEndPoint().getGeoPoint());
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setIcon(getResources().getDrawable(R.drawable.marker_destination));
        endMarker.setTitle("End point");

        if (points.getMidPoint() != null) {
            Marker viaMarker = new Marker(map);
            viaMarker.setPosition(points.getMidPoint().getGeoPoint());
            viaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            viaMarker.setIcon(getResources().getDrawable(R.drawable.marker_via));
            viaMarker.setTitle("Via point");
            map.getOverlays().add(viaMarker);
        }

        new Thread(new Runnable() {
            public void run() {
                IMapController mapController = map.getController();
                mapController.setZoom(14);

                RoadManager roadManager = new OwnOSRMRoadManager();
                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(points.getStartPoint().getGeoPoint());

                if (points.isMidPoint()) {
                    waypoints.add(points.getMidPoint().getGeoPoint());
                }
                waypoints.add(points.getEndPoint().getGeoPoint());
                try {
                    road = roadManager.getRoad(waypoints);


                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (road.mStatus != Road.STATUS_OK) {
                                //handle error... warn the user, etc.
                            }
                            Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);

                            //Drawable starticon = getResources().getDrawable(R.drawable.ic_empty);

                            roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 8, RouteActivity.this);
                            map.getOverlays().add(roadOverlay);
                            TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);


                            for (int i = 0; i < road.mNodes.size(); i++) {
                                RoadNode node = road.mNodes.get(i);
                                Marker nodeMarker = new Marker(map);
                                nodeMarker.setPosition(node.mLocation);
                                nodeMarker.setIcon(nodeIcon);
                                nodeMarker.setTitle("Step " + i);
                                nodeMarker.setSnippet(node.mInstructions);
                                nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
                                Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
                                int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
                                if (iconId != R.drawable.ic_empty){
                                    Drawable icon2 = getResources().getDrawable(iconId);
                                    nodeMarker.setImage(icon2);
                                }

                                map.getOverlays().add(nodeMarker);
                            }
                            map.invalidate();
                        }
                    });
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.getMessage(), e);
                }
            }
        }).start();
        map.getOverlays().add(startMarker);
        map.getOverlays().add(endMarker);
        map.invalidate();


        showPosition = new ShowPosition(this, 5000);


    }


    public void refreshMapPosition(Location loc) {
        GeoPoint currentLocation = new GeoPoint(loc);
        mapController.setCenter(currentLocation);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(currentLocation);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
        map.getOverlays().add(startMarker);
        mapController.setZoom(17);
        mapController.setCenter(currentLocation);
        map.invalidate();
    }
    public void refreshTrackingPosition(List<GeoPoint> route, Location loc) {
        roadOverlay.setPoints(route);
        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }
    public void clearTrackingPositions() {
        map.getOverlays().clear();
    }
}
