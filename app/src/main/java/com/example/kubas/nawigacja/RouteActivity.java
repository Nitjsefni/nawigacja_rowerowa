package com.example.kubas.nawigacja;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

/**
 * Created by KubaS on 2015-05-06.
 */
public class RouteActivity extends Activity {
    private MapController myMapController;

    boolean isGPSEnabled;
    private Location actualLocation;
    private Location locationToPrint;
    private double avgLatitude;
    private double avgLongitude;
    private int locationCount;
    private float locationWeight;
    int ACCURANCY_LIMIT = 50;
    // getting network status
    boolean isNetworkEnabled;
    public GPSManager gpsm;
    private PostPosition postposition;
    ArrayList<OverlayItem> overlayItemArray;
    Road road;
    MapView map;
    GeoPoint gp_od, gp_do, gp_przez, geopoint_start;
    IMapController mapController;
    ArrayList<GeoPoint> route = new ArrayList<>();
    Polyline roadOverlay;
    Marker startMarker, endMarker;
    //public MyLocationListener listener;
    private LocationManager locationManager;
    private String provider;
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
        String gp_od_lat="",gp_od_lng="";
        String gp_do_lat="",gp_do_lng="";
        String gp_przez_lat="",gp_przez_lng="";
        boolean czy_przez = false;
        if (extras != null) {
            if(!extras.getString("gp_od_lat").equals("0")) {
                gp_od_lat = extras.getString("gp_od_lat");
                gp_od_lng = extras.getString("gp_od_lng");
            }
            else
            {
                gpsm = new GPSManager(this, 2000);
                if(geopoint_start != null) {
                    gp_od_lat = String.valueOf(geopoint_start.getLatitude());
                    gp_od_lng = String.valueOf(geopoint_start.getLongitude());
                }
                gpsm.stop();
            }
            gp_od = new GeoPoint(Double.valueOf(gp_od_lat),Double.valueOf(gp_od_lng));
            gp_do_lat = extras.getString("gp_do_lat");
            gp_do_lng = extras.getString("gp_do_lng");
            gp_do = new GeoPoint(Double.valueOf(gp_do_lat),Double.valueOf(gp_do_lng));
            if(extras.getBoolean("czy_przez"))
            {   czy_przez= extras.getBoolean("czy_przez");
                gp_przez_lat = extras.getString("gp_przez_lat");
                gp_przez_lng = extras.getString("gp_przez_lng");
                gp_przez = new GeoPoint(Double.valueOf(gp_przez_lat),Double.valueOf(gp_przez_lng));
            }
        }
        final boolean czy_przez2 = czy_przez;
        mapController.setCenter(gp_od);
        startMarker = new Marker(map);
        startMarker.setPosition(gp_od);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.marker_departure));
        startMarker.setTitle("Start point");

        endMarker = new Marker(map);
        endMarker.setPosition(gp_do);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setIcon(getResources().getDrawable(R.drawable.marker_destination));
        endMarker.setTitle("End point");

        if(czy_przez)
        {
            Marker viaMarker = new Marker(map);
            viaMarker.setPosition(gp_przez);
            viaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            viaMarker.setIcon(getResources().getDrawable(R.drawable.marker_via));
            viaMarker.setTitle("Via point");
            map.getOverlays().add(viaMarker);
        }


         new Thread(new Runnable()
        {
            public void run() {
                IMapController mapController = map.getController();
                mapController.setZoom(14);

                OSRMRoadManager osm =  new OSRMRoadManager();
                //osm.setService("http://beta.wskocznarower.pl/app_dev.php/webservices/viaroute?");

                RoadManager roadManager = osm;

                ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                waypoints.add(gp_od);

                if(czy_przez2)
                {
                    waypoints.add(gp_przez);
                }
                waypoints.add(gp_do);


                // Polyline roadOverlay = RoadManager.buildRoadOverlay(waypoints, Color.RED, 8, MapActivity.this);

                try {
                    road = roadManager.getRoad(waypoints);
                } catch (Exception e) {
                    Toast toast = Toast.makeText(RouteActivity.this,e.toString(), Toast.LENGTH_LONG);
                    toast.show();
                }

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
