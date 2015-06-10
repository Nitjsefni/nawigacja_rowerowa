package com.example.kubas.nawigacja;

import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.example.kubas.nawigacja.gps.GPSManager;
import com.example.kubas.nawigacja.tracking.Trackable;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.List;
import java.util.Timer;

public class MapActivity extends Activity implements Trackable {
    private IMapController mapController;
    private GPSManager gpsManager = GPSManager.getInstance();
    private SendPosition sendPosition;
    private Location locationToPrint;
    private Timer timer;
    private Road road;
    private MapView map;
    private GeoPoint startPoint;
    private Polyline roadOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actrivity_map);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        roadOverlay = new Polyline(this);
        roadOverlay.setColor(0x800000FF);
        roadOverlay.setWidth(8.0f);

        mapController = map.getController();
        mapController.setZoom(13);

        GeoPoint gp = gpsManager.getActualPosition();
        if (gp == null) {
            gp = new GeoPoint(52.406184, 16.925098);
        }
        setMapStartPoint(gp);
        map.invalidate();
        sendPosition = new SendPosition(this, 5000);
        // startTracking();
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startTracking();
                } else {
                    stopTracking();
                }
            }
        });


    }

    public void startTracking() {
        gpsManager.restart();
        sendPosition.stop();
        map.getOverlays().clear();
        Location loc;

        if (gpsManager.getActualPosition() != null) {
            startPoint = gpsManager.getActualPosition();
            Log.i("Begin track", "Lat: " + gpsManager.getActualLocation().getLatitude() + " long: " + gpsManager.getActualLocation().getLongitude() + " accuracy: " + gpsManager.getActualLocation().getAccuracy());
            Log.i("Begin track", "Bear: " + gpsManager.getActualLocation().getBearing() + " prov: " + gpsManager.getActualLocation().getProvider() + " speed: " + gpsManager.getActualLocation().getSpeed());
        }
        sendPosition.add(startPoint);
    }

    public void stopTracking() {
        gpsManager.stop();
        sendPosition.stop();
        map.getOverlays().clear();
        sendPosition.clear();
    }

    private void setMapStartPoint(GeoPoint actualPoint) {
        Marker startMarker = new Marker(map);
        startMarker.setPosition(actualPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapController.setZoom(17);
        mapController.setCenter(actualPoint);
        map.getOverlays().add(startMarker);
        map.invalidate();
    }

    public Location getLocationToPrint() {
        return locationToPrint;
    }

    public void setLocationToPrint(Location locationToPrint) {
        this.locationToPrint = locationToPrint;
    }

    public void refreshMapPosition(GeoPoint currentLocation) {
        mapController.setCenter(currentLocation);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(currentLocation);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        mapController.setZoom(17);
        mapController.setCenter(currentLocation);
        map.invalidate();
    }
    public void refreshTrackingPosition(List<GeoPoint> route) {
        roadOverlay.setPoints(route);
        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }
    public void clearTrackingPositions() {
        map.getOverlays().clear();
    }
}
