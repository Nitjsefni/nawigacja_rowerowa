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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import java.util.Timer;

/**
 * Created by KubaS on 2015-04-26.
 */
public class MapActivity extends Activity{
    private MapController myMapController;
    public GPSManager gpsm;
    private PostPosition postposition;
    private Location actualLocation;
    private Location locationToPrint;
    private double avgLatitude;
    private double avgLongitude;
    private int locationCount;
    private float locationWeight;
    int ACCURANCY_LIMIT = 50;
    private Timer timer;
    public boolean tracking=false;
    ArrayList<OverlayItem> overlayItemArray;
    Road road;
    MapView map;
    GeoPoint startPoint,currentLocation, lastKnown=null;
    IMapController mapController;
    ArrayList<GeoPoint> route = new ArrayList<>();
    Polyline roadOverlay;

    private LocationManager locationManager;
    private String provider;
    boolean isGPSEnabled;

    // getting network status
    boolean isNetworkEnabled;


    private ToggleButton tgbtn;

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

        GeoPoint gp = new GeoPoint(52.406184, 16.925098);

        //route.add(currentLocation);
        //roadOverlay.setPoints(route);

        //map.getOverlays().add(roadOverlay);
        mapController.setCenter(gp);
        //mapController.setCenter(currentLocation);
        map.invalidate();
        gpsm = new GPSManager(this, 1000);
        postposition = new PostPosition(this,5000);
       // startTracking();
        tgbtn = (ToggleButton) findViewById(R.id.toggleButton);
        tgbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startTracking();
                } else {
                    stopTracking();
                }
            }
        });



    }

    public void startTracking()
    {
        gpsm.stop();
        postposition.stop();
        map.getOverlays().clear();
        Location loc;

        if(getActualLocation() != null)
        {
        startPoint= new GeoPoint(getActualLocation());
            Log.i("Begin track", "Lat: " + getActualLocation().getLatitude() + " long: " + getActualLocation().getLongitude() + " accuracy: " + getActualLocation().getAccuracy());
            Log.i("Begin track", "Bear: " + getActualLocation().getBearing() + " prov: " + getActualLocation().getProvider() + " speed: " + getActualLocation().getSpeed());
        }
        route.add(startPoint);
        tracking = true;
        //gpsm = new GPSManager(this, 2000);

        gpsm = new GPSManager(this, 1000);
        postposition = new PostPosition(this,5000);


        //Location location = locationManager.getLastKnownLocation(provider);


        //currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());



    }
    public void stopTracking(){
        gpsm.stop();
        postposition.stop();
        map.getOverlays().clear();
        tracking = false;
        route.clear();
        gpsm = new GPSManager(this, 1000);
        postposition = new PostPosition(this,4000);
    }

    public Location getBetterLocation(Location location, Location actualLocation) {
        addAvgLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        if (actualLocation == null) {
            return location;
        }
        if (actualLocation.getAccuracy() >= location.getAccuracy()) {
            return location;
        }
        return actualLocation;
    }
    public Location getLocationToPrint() {
        return locationToPrint;
    }

    public void setLocationToPrint(Location locationToPrint) {
        this.locationToPrint = locationToPrint;
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
