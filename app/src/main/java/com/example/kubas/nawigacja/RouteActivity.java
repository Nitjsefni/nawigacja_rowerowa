package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kubas.nawigacja.client.OwnOSRMRoadManager;
import com.example.kubas.nawigacja.client.SavedRouteOSMRRoadManager;
import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.gps.GPSManager;
import com.example.kubas.nawigacja.tracking.Trackable;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteActivity extends Activity implements Trackable {
    private MapView map;
    private ShowPosition showPosition;
    private Polyline roadOverlay;
    private Road road_temp;
    private IMapController mapController;
    private ImageView maneuverImg;
    private TextView txtV_Route_DistanceNode, txtV_Route_InstructionNode, txtV_Route_TimeNode, txtV_Route_Speed,txtV_Route_Time, txtV_Route_MaxLength;
    private double maxLength=0.0, maxDuration=0.0;
    private TextToSpeech t1;
    private ArrayList<RoadNode> rNodes = new ArrayList<>();
    private Marker endMarker, startMarker, viaMarker;
    private RoutePoints points;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_route);
        maneuverImg = (ImageView) findViewById(R.id.maneuverImg);
        txtV_Route_DistanceNode = (TextView) findViewById(R.id.txtV_Route_DistanceNode);
        txtV_Route_TimeNode = (TextView) findViewById(R.id.txtV_Route_TimeNode);
        txtV_Route_InstructionNode = (TextView) findViewById(R.id.txtV_Route_InstructionNode);
        txtV_Route_Speed = (TextView) findViewById(R.id.txtV_Route_Speed);
        txtV_Route_Time = (TextView) findViewById(R.id.txtV_Route_Time);
        txtV_Route_MaxLength = (TextView) findViewById(R.id.txtV_Route_MaxLength);
        map = (MapView) findViewById(R.id.map2);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(new Locale("pl"));
                }
            }
        });
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

        if (points.getStartPoint()!=null) {
            mapController.setCenter(points.getStartPoint().getGeoPoint());
            startMarker = new Marker(map);
            startMarker.setPosition(points.getStartPoint().getGeoPoint());
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(getResources().getDrawable(R.drawable.marker_departure));
            startMarker.setTitle("Punkt poczatkowy");
            startMarker.setSubDescription(points.getStartPoint().getGeoPoint().getLatitudeE6()/1000000F + " " + points.getStartPoint().getGeoPoint().getLongitudeE6()/1000000F);            ;
            map.getOverlays().add(startMarker);
        }
        if (points.getEndPoint()!=null) {
            endMarker = new Marker(map);
            endMarker.setPosition(points.getEndPoint().getGeoPoint());
            endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            endMarker.setIcon(getResources().getDrawable(R.drawable.marker_destination));
            endMarker.setTitle("Punkt koncowy");
            endMarker.setSubDescription(points.getEndPoint().getGeoPoint().getLatitudeE6()/1000000F + " " + points.getEndPoint().getGeoPoint().getLongitudeE6()/1000000F);

            map.getOverlays().add(endMarker);
        }
        if (points.getMidPoint() != null) {
            viaMarker = new Marker(map);
            viaMarker.setPosition(points.getMidPoint().getGeoPoint());
            viaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            viaMarker.setIcon(getResources().getDrawable(R.drawable.marker_via));
            viaMarker.setTitle("Punkt posredni");
            map.getOverlays().add(viaMarker);
        }

        RoadManager roadManager;
        int routeId = extras.getInt("routeId",-1);
        if (routeId==-1) {
            roadManager = new OwnOSRMRoadManager();
        }else{
            roadManager = new SavedRouteOSMRRoadManager(routeId);
        }
        new Thread(new GetRoadFromServer(points, roadManager)).start();
        map.invalidate();
        showPosition = new ShowPosition(this, 5000);
    }

    public String getLengthText(double length) {
        String result;
        if (length >= 100.0) {
            result = (int) (length) + " km";
        } else if (length >= 1.0) {
            result = Math.round(length * 10) / 10.0 + " km";
        } else {
            result = (int) (length * 1000) + " m";
        }

        return result;
    }
    public String getLengthTextToSpeech(double length){
        String result;
        if (length >= 100.0){
            result = (int)(length) + " kilometrów";
        } else if (length >= 1.0){
            result = (int)(length*10)/10.0 + " kilometrów";
        } else {
            result = (int)(length*1000) + " metrów";
        }

        return result;
    }
    public String getDurationText(double duration) {
        String result = "";

        int totalSeconds = (int) duration;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds / 60) - (hours * 60);
        int seconds = (totalSeconds / 60);
        if (hours != 0) {
            result += hours + " h";
        }
        if (minutes != 0) {
            result += minutes + " min";
        }
        if (hours == 0 && minutes == 0) {
            result += seconds + " s";
        }
        return result;
    }


    public void refreshMapPosition(Location loc) {
        GeoPoint currentLocation = new GeoPoint(loc);
        TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
        if (loc.hasSpeed()) {
            txtV_Route_Speed.setText(String.valueOf(loc.getSpeed()) + " m/s");
        } else {
            txtV_Route_Speed.setText("0 m/s");
        }


        map.getOverlays().clear();
        Marker actualMarker = new Marker(map);
        actualMarker.setPosition(currentLocation);
        actualMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
        actualMarker.setTitle("Aktualna pozycja");
        actualMarker.setSubDescription(currentLocation.getLatitude() + " " + currentLocation.getLongitude());
        map.getOverlays().add(actualMarker);
        mapController.setZoom(17);
        mapController.setCenter(currentLocation);
        double latitude = rNodes.get(0).mLocation.getLatitude();
        double longtitude = rNodes.get(0).mLocation.getLongitude();
        Location nextNode = new Location("gps");
        nextNode.setLatitude(latitude);
        nextNode.setLongitude(longtitude);
        GeoPoint ep = points.getEndPoint().getGeoPoint();
        double latitude2 = ep.getLatitude();
        double longtitude2 = ep.getLongitude();
        Location endLoc = new Location("gps");
        endLoc.setLatitude(latitude2);
        endLoc.setLongitude(longtitude2);
        if(rNodes.size() == 0 && loc.distanceTo(endLoc) < 25)
        {
            String text = "Dojechales do celu podróży";
            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        else {


            txtV_Route_InstructionNode.setText(rNodes.get(0).mInstructions);
            txtV_Route_DistanceNode.setText(getLengthText(rNodes.get(0).mLength));
            txtV_Route_TimeNode.setText(getDurationText(rNodes.get(0).mDuration));



            if (loc.distanceTo(nextNode) > 25 && loc.distanceTo(nextNode) < 40) {
                String text = "Za " + getLengthTextToSpeech(rNodes.get(0).mLength) + rNodes.get(0).mInstructions;
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else if (loc.distanceTo(nextNode) > 15 && loc.distanceTo(nextNode) < 20) {
                String text = "Za " + getLengthTextToSpeech(rNodes.get(0).mLength) + rNodes.get(0).mInstructions;
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else if ((loc.distanceTo(nextNode) < 10)) {
                String text = getLengthTextToSpeech(rNodes.get(0).mLength) + rNodes.get(0).mInstructions;
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else if ((loc.distanceTo(nextNode) < 5)) {
                rNodes.remove(0);
                int iconId = iconIds.getResourceId(rNodes.get(0).mManeuverType, R.drawable.ic_empty);
                if (iconId != R.drawable.ic_empty) {
                    Drawable icon2 = getResources().getDrawable(iconId);
                    maneuverImg.setImageDrawable(icon2);
                }
                txtV_Route_InstructionNode.setText(rNodes.get(0).mInstructions);
                txtV_Route_DistanceNode.setText(getLengthText(rNodes.get(0).mLength));
                txtV_Route_TimeNode.setText(getDurationText(rNodes.get(0).mDuration));
                String text = "Za " + getLengthTextToSpeech(rNodes.get(0).mLength) + rNodes.get(0).mInstructions;
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }

            if (loc.hasSpeed()) {
                txtV_Route_Speed.setText(String.valueOf(loc.getSpeed()) + " m/s");
            } else {
                txtV_Route_Speed.setText("0 m/s");
            }

            map.getOverlays().add(roadOverlay);
            RouteActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
                    map.getOverlays().add(startMarker);
                    map.getOverlays().add(endMarker);
                    Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
                    maxDuration = 0.0;
                    maxLength = 0.0;
                    for (int i = 0; i < rNodes.size(); i++) {
                        RoadNode node = rNodes.get(i);
                        Marker nodeMarker = new Marker(map);
                        nodeMarker.setPosition(node.mLocation);
                        nodeMarker.setIcon(nodeIcon);
                        nodeMarker.setTitle("Step " + i);
                        nodeMarker.setSnippet(node.mInstructions);
                        nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
                        maxDuration += node.mDuration;
                        maxLength += node.mLength;
                        Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
                        int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
                        if (iconId != R.drawable.ic_empty) {
                            Drawable icon2 = getResources().getDrawable(iconId);
                            nodeMarker.setImage(icon2);
                        }

                        map.getOverlays().add(nodeMarker);
                        map.invalidate();
                    }
                    int iconId = iconIds.getResourceId(rNodes.get(0).mManeuverType, R.drawable.ic_empty);
                    if (iconId != R.drawable.ic_empty) {
                        Drawable icon2 = getResources().getDrawable(iconId);
                        maneuverImg.setImageDrawable(icon2);
                    }
                    txtV_Route_InstructionNode.setText(rNodes.get(0).mInstructions);
                    txtV_Route_DistanceNode.setText(getLengthText(rNodes.get(0).mLength));
                    txtV_Route_TimeNode.setText(getDurationText(rNodes.get(0).mDuration));

                    String lenght = getLengthText(maxLength);
                    txtV_Route_MaxLength.setText(lenght);
                    String duration = getDurationText(maxDuration);
                    txtV_Route_Time.setText(duration);
                    map.invalidate();
                }
            });


        }
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

    private class PrintOnMap implements Runnable {
        private Road road;

        public PrintOnMap(Road road) {
            this.road = road;
        }

        public void run() {
            if (road.mStatus != Road.STATUS_OK) {
                //handle error... warn the user, etc.
            }
            Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
            road_temp = road;
            //Drawable starticon = getResources().getDrawable(R.drawable.ic_empty);
            IMapController mapController = map.getController();
            mapController.setZoom(14);
            txtV_Route_Speed.setText("0 m/s");
            roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 8, RouteActivity.this);
            map.getOverlays().add(roadOverlay);
            TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
            String lenght = getLengthText(road.mLength);
            txtV_Route_MaxLength.setText(lenght);
            String duration = getDurationText(road.mDuration);
            txtV_Route_Time.setText(duration);
            rNodes.addAll(road.mNodes);
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
                if (iconId != R.drawable.ic_empty) {
                    Drawable icon2 = getResources().getDrawable(iconId);
                    nodeMarker.setImage(icon2);
                }

                map.getOverlays().add(nodeMarker);
            }
            String text = "Za " + getLengthTextToSpeech(rNodes.get(0).mLength) + rNodes.get(0).mInstructions;
            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            map.invalidate();
        }
    }

    private class GetRoadFromServer implements Runnable {
        private RoutePoints points;
        private RoadManager roadManager;

        public GetRoadFromServer(RoutePoints points, RoadManager roadManager) {
            this.points = points;
            this.roadManager = roadManager;
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
                runOnUiThread(new PrintOnMap(roadManager.getRoad(waypoints)));
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.getMessage(), e);
            }
        }
    }
}
