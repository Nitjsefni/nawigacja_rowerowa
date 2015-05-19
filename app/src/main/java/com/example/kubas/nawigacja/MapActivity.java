package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
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
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KubaS on 2015-04-26.
 */
public class MapActivity extends Activity{
    private MapController myMapController;



    ArrayList<OverlayItem> overlayItemArray;
    Road road;
    MapView map;
    GeoPoint currentLocation;
    IMapController mapController;
    ArrayList<GeoPoint> route = new ArrayList<>();
    Polyline roadOverlay;
    public MyLocationListener listener;
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actrivity_map);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        listener = new MyLocationListener();
        provider = locationManager.getBestProvider(criteria, false);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, this);


        Location location = locationManager.getLastKnownLocation(provider);


        currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        roadOverlay = new Polyline(this);
        roadOverlay.setColor(0x800000FF);
        roadOverlay.setWidth(8.0f);


        mapController = map.getController();
        mapController.setZoom(14);
        mapController.setCenter(currentLocation);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(currentLocation);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        route.add(currentLocation);
        //roadOverlay.setPoints(route);

        //map.getOverlays().add(roadOverlay);

        //mapController.setCenter(currentLocation);
        map.invalidate();
       /* new Thread(new Runnable()
        {
            public void run() {
                IMapController mapController = map.getController();
                mapController.setZoom(14);

                GeoPoint gPt0 = new GeoPoint(52372116, 16879898);
                GeoPoint gPt1 = new GeoPoint(52373036, 16876454);
                GeoPoint gPt2 = new GeoPoint(52373036, 16876143);
                GeoPoint gPt3 = new GeoPoint(52371729, 16878911);
                mapController.setCenter(gPt0);
                RoadManager roadManager = new OSRMRoadManager();
                ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                waypoints.add(gPt0);
                waypoints.add(gPt1);
                waypoints.add(gPt2);
                waypoints.add(gPt3);


                // Polyline roadOverlay = RoadManager.buildRoadOverlay(waypoints, Color.RED, 8, MapActivity.this);

                try {
                    road = roadManager.getRoad(waypoints);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        if (road.mStatus != Road.STATUS_OK) {
                            //handle error... warn the user, etc.
                        }

                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 8, MapActivity.this);
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





        map.invalidate();
*/


    }
    public class  MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, this);
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 100, this);
            currentLocation = new GeoPoint(location);
            route.add(currentLocation);
            mapController.setCenter(currentLocation);

            roadOverlay.setPoints(route);

            map.getOverlays().add(roadOverlay);

            mapController.setCenter(currentLocation);
            map.invalidate();


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {


        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
