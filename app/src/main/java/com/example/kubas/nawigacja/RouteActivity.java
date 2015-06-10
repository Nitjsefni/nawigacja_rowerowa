package com.example.kubas.nawigacja;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.gps.GPSManager;

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

public class RouteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_route);

        final MapView map = (MapView) findViewById(R.id.map2);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(14);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        final RoutePoints points = (RoutePoints) extras.get("points");
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
        startMarker = new Marker(map);
        startMarker.setPosition(points.getStartPoint().getGeoPoint());
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.marker_departure));
        startMarker.setTitle("Start point");

        endMarker = new Marker(map);
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

                OSRMRoadManager osm =  new OSRMRoadManager();
                //osm.setService("http://beta.wskocznarower.pl/app_dev.php/webservices/viaroute?");

                RoadManager roadManager = osm;

                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(points.getStartPoint().getGeoPoint());

                if (points.isMidPoint()) {
                    waypoints.add(points.getMidPoint().getGeoPoint());
                }
                waypoints.add(points.getEndPoint().getGeoPoint());
                try {
                    final Road road = roadManager.getRoad(waypoints);


                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (road.mStatus != Road.STATUS_OK) {
                                //handle error... warn the user, etc.
                            }

                            Polyline roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 8, RouteActivity.this);
                            map.getOverlays().add(roadOverlay);

                            Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
                            for (int i = 0; i < road.mNodes.size(); i++) {
                                RoadNode node = road.mNodes.get(i);
                                Marker nodeMarker = new Marker(map);
                                nodeMarker.setPosition(node.mLocation);
                                nodeMarker.setIcon(nodeIcon);
                                nodeMarker.setTitle("Step " + i);
                                nodeMarker.setSnippet(node.mInstructions);
                                nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
                                Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
                                nodeMarker.setImage(icon);
                                map.getOverlays().add(nodeMarker);
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast toast = Toast.makeText(RouteActivity.this,e.toString(), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }).start();
        map.getOverlays().add(startMarker);
        map.getOverlays().add(endMarker);
        map.invalidate();
        gpsm = new GPSManager(this, 1000);
        final Handler h = new Handler();
        final int delay = 6000; //milliseconds

        h.postDelayed(new Runnable(){
            public void run(){

                Location location = getActualLocation();
                if (location == null) {
                    return;
                }
                if (location.getAccuracy() > 7) {
                    location = new Location(location);
                    location.setLatitude(getAvgLatitude());
                    location.setLongitude(getAvgLongitude());
                    location.setAccuracy(getAvgAccurancy());

                    setLocationToPrint(location);

                }
                setActualLocation(null);
                clearAvgLocation();
                h.postDelayed(this, delay);
            }
        }, delay);


    }
    public Location getLocationToPrint() {
        return locationToPrint;
    }

    public void setLocationToPrint(Location locationToPrint) {
        this.locationToPrint = locationToPrint;
        Marker viaMarker = new Marker(map);
        viaMarker.setPosition(gp_przez);
        viaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        viaMarker.setIcon(getResources().getDrawable(R.drawable.marker_via));
        viaMarker.setTitle("Actual position");
        map.getOverlays().add(viaMarker);

        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 8, RouteActivity.this);
        map.getOverlays().add(roadOverlay);


        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
        RoadNode node1 = road.mNodes.get(0);
        Marker nodeMarker = new Marker(map);
        nodeMarker.setPosition(new GeoPoint(locationToPrint));
        nodeMarker.setIcon(nodeIcon);
        nodeMarker.setTitle("Pozycja aktualna");
        nodeMarker.setSnippet(node1.mInstructions);
        nodeMarker.setSubDescription(Road.getLengthDurationText(node1.mLength, node1.mDuration));
        Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
        nodeMarker.setImage(icon);
        for (int i = 1; i < road.mNodes.size(); i++) {
            RoadNode node = road.mNodes.get(i);
            Marker nodeMarker1 = new Marker(map);
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.setTitle("Step " + i);
            nodeMarker.setSnippet(node.mInstructions);
            nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
            Drawable icon2 = getResources().getDrawable(R.drawable.ic_continue);
            nodeMarker.setImage(icon2);
            map.getOverlays().add(nodeMarker1);
        }
        map.getOverlays().add(startMarker);
        map.getOverlays().add(endMarker);



        map.invalidate();
    }
    public void addAvgLocation(double avgLatitude, double avgLongitude,
                               float accuracy) {
        if (accuracy > ACCURANCY_LIMIT) {
            accuracy = ACCURANCY_LIMIT;
        }
        accuracy = ACCURANCY_LIMIT + 1 - accuracy;
        this.avgLatitude += avgLatitude * accuracy;
        this.avgLongitude += avgLongitude * accuracy;
        this.locationCount++;
        this.locationWeight += accuracy;
    }

    public double getAvgLatitude() {
        return avgLatitude / locationWeight;
    }

    public double getAvgLongitude() {
        return avgLongitude / locationWeight;
    }

    public float getAvgAccurancy() {
        return ((ACCURANCY_LIMIT + 1) * locationCount - locationWeight)
                / locationCount;
    }

    public void clearAvgLocation() {
        locationCount = 0;
        locationWeight = 0;
        avgLatitude = 0;
        avgLongitude = 0;
    }
    public Location getActualLocation() {
        return actualLocation;
    }

    public void setActualLocation(Location actualLocation) {
        this.actualLocation = actualLocation;
    }

}
