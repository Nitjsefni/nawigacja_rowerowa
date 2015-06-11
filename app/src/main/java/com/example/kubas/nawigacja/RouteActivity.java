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
import android.widget.Toast;

import com.example.kubas.nawigacja.client.GetRoadFromServer;
import com.example.kubas.nawigacja.client.OwnOSRMRoadManager;
import com.example.kubas.nawigacja.client.SavedRouteOSMRRoadManager;
import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.gps.GPSManager;
import com.example.kubas.nawigacja.routing.PrintRoute;
import com.example.kubas.nawigacja.routing.RoutingUtil;
import com.example.kubas.nawigacja.tracking.ShowPosition;
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
    private final GPSManager gpsManager = GPSManager.getInstance();
    private ShowPosition showPosition;
    private Polyline roadOverlay;
    private TextToSpeech textToSpeech;
    private ArrayList<RoadNode> roadNodes = new ArrayList<>();
    private Marker endMarker, startMarker, viaMarker;
    private RoutePoints points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_route);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("pl"));
                }
            }
        });
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        MapView map = (MapView) findViewById(R.id.map2);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(14);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        points = (RoutePoints) extras.get("points");
        if (points.getStartPoint() == null) {
            GeoPoint actualPosition = gpsManager.getActualPosition();
            while (actualPosition == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Wyszukiwanie bieżacej lokalizacji", Toast.LENGTH_SHORT).show();
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                actualPosition = gpsManager.getActualPosition();
            }
            points.setStartPoint(new GeoPosition("Aktualna pozycja", actualPosition));
        }

        if (points.getStartPoint() != null) {
            map.getController().setCenter(points.getStartPoint().getGeoPoint());
            startMarker = printMarker(map, points.getStartPoint().getGeoPoint(), "Punkt poczatkowy", R.drawable.marker_departure, points.getStartPoint().getDescription());
        }
        if (points.getEndPoint() != null) {
            endMarker = printMarker(map, points.getEndPoint().getGeoPoint(), "Punkt koncowy", R.drawable.marker_destination, points.getEndPoint().getDescription());
        }
        if (points.getMidPoint() != null) {
            viaMarker = printMarker(map, points.getMidPoint().getGeoPoint(), "Punkt posredni", R.drawable.marker_via, points.getMidPoint().getDescription());
        }

        RoadManager roadManager;
        int routeId = extras.getInt("routeId", -1);
        if (routeId == -1) {
            roadManager = new OwnOSRMRoadManager();
        } else {
            roadManager = new SavedRouteOSMRRoadManager(routeId);
        }
        new Thread(new GetRoadFromServer(this, points, roadManager, new PrintRouteFromServer(map))).start();
        map.invalidate();
        showPosition = new ShowPosition(this, 5000);
    }

    private Marker printMarker(MapView map, GeoPoint point, String name, int resourceToShow, String description) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(resourceToShow));
        marker.setTitle(name);
        marker.setSubDescription(description);
        map.getOverlays().add(marker);
        return marker;
    }


    public void refreshMapPosition(Location loc) {
        final MapView map = (MapView) findViewById(R.id.map2);
        final ImageView maneuverImg = (ImageView) findViewById(R.id.maneuverImg);
        final TextView txtV_Route_DistanceNode = (TextView) findViewById(R.id.txtV_Route_DistanceNode);
        final TextView txtV_Route_InstructionNode = (TextView) findViewById(R.id.txtV_Route_InstructionNode);
        final TextView txtV_Route_TimeNode = (TextView) findViewById(R.id.txtV_Route_TimeNode);
        final TextView txtV_Route_Time = (TextView) findViewById(R.id.txtV_Route_Time);
        final TextView txtV_Route_MaxLength = (TextView) findViewById(R.id.txtV_Route_MaxLength);
        map.getOverlays().clear();
        GeoPoint currentLocation = new GeoPoint(loc);
        TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
        printSpeed(loc);
        printMarker(map, currentLocation, "Aktualna pozycja", android.R.drawable.arrow_down_float, "");
        map.getController().setZoom(17);
        map.getController().setCenter(currentLocation);
        Location nextNode = getLocation(roadNodes.get(0).mLocation);
        GeoPoint endPoint = points.getEndPoint().getGeoPoint();
        Location endLoc = getLocation(endPoint);
        if (roadNodes.size() == 0 && loc.distanceTo(endLoc) < 25) {
            String text = "Dojechales do celu podróży";
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else {


            txtV_Route_InstructionNode.setText(roadNodes.get(0).mInstructions);
            txtV_Route_DistanceNode.setText(RoutingUtil.getLengthText(roadNodes.get(0).mLength));
            txtV_Route_TimeNode.setText(RoutingUtil.getDurationText(roadNodes.get(0).mDuration));


            if (loc.distanceTo(nextNode) > 25 && loc.distanceTo(nextNode) < 40) {
                String text = "Za " + RoutingUtil.getLengthTextToSpeech(roadNodes.get(0).mLength) + roadNodes.get(0).mInstructions;
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else if (loc.distanceTo(nextNode) > 15 && loc.distanceTo(nextNode) < 20) {
                String text = "Za " + RoutingUtil.getLengthTextToSpeech(roadNodes.get(0).mLength) + roadNodes.get(0).mInstructions;
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else if ((loc.distanceTo(nextNode) < 10)) {
                String text = RoutingUtil.getLengthTextToSpeech(roadNodes.get(0).mLength) + roadNodes.get(0).mInstructions;
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else if ((loc.distanceTo(nextNode) < 5)) {
                roadNodes.remove(0);
                int iconId = iconIds.getResourceId(roadNodes.get(0).mManeuverType, R.drawable.ic_empty);
                if (iconId != R.drawable.ic_empty) {
                    Drawable icon2 = getResources().getDrawable(iconId);
                    maneuverImg.setImageDrawable(icon2);
                }
                txtV_Route_InstructionNode.setText(roadNodes.get(0).mInstructions);
                txtV_Route_DistanceNode.setText(RoutingUtil.getLengthText(roadNodes.get(0).mLength));
                txtV_Route_TimeNode.setText(RoutingUtil.getDurationText(roadNodes.get(0).mDuration));
                String text = "Za " + RoutingUtil.getLengthTextToSpeech(roadNodes.get(0).mLength) + roadNodes.get(0).mInstructions;
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }

            map.getOverlays().add(roadOverlay);
            RouteActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
                    map.getOverlays().add(startMarker);
                    map.getOverlays().add(endMarker);
                    double maxLength = 0.0;
                    double maxDuration = 0.0;
                    for (int i = 0; i < roadNodes.size(); i++) {
                        RoadNode node = roadNodes.get(i);
                        Marker nodeMarker = printMarker(map, node.mLocation, "Step " + i, R.drawable.marker_node, Road.getLengthDurationText(node.mLength, node.mDuration));
                        nodeMarker.setSnippet(node.mInstructions);
                        maxDuration += node.mDuration;
                        maxLength += node.mLength;
                        int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
                        if (iconId != R.drawable.ic_empty) {
                            Drawable icon2 = getResources().getDrawable(iconId);
                            nodeMarker.setImage(icon2);
                        }

                        map.getOverlays().add(nodeMarker);
                        map.invalidate();
                    }
                    int iconId = iconIds.getResourceId(roadNodes.get(0).mManeuverType, R.drawable.ic_empty);
                    if (iconId != R.drawable.ic_empty) {
                        Drawable icon2 = getResources().getDrawable(iconId);
                        maneuverImg.setImageDrawable(icon2);
                    }

                    double dist = gpsManager.getActualPosition().distanceTo(roadNodes.get(0).mLocation);
                    int distance = (int) dist;
                    txtV_Route_InstructionNode.setText(roadNodes.get(0).mInstructions);
                    txtV_Route_DistanceNode.setText(String.valueOf(distance) + " m");

                    double time = dist / 4.0;
                    Log.i("RouteActivity", String.valueOf(time));
                    Log.i("RouteActivity", RoutingUtil.getDurationText(time));
                    //txtV_Route_DistanceNode.setText(getLengthText(rNodes.get(0).mLength));
                    txtV_Route_TimeNode.setText(RoutingUtil.getDurationText(time));

                    String lenght = RoutingUtil.getLengthText(maxLength);
                    txtV_Route_MaxLength.setText(lenght);
                    String duration = RoutingUtil.getDurationText(maxDuration);
                    txtV_Route_Time.setText(duration);
                    map.invalidate();
                }
            });


        }
        map.invalidate();
    }

    private Location getLocation(GeoPoint point) {
        Location nextNode = new Location("gps");
        nextNode.setLatitude(point.getLatitude());
        nextNode.setLongitude(point.getLongitude());
        return nextNode;
    }

    private void printSpeed(Location loc) {
        String value = "0 m/s";
        if (loc.hasSpeed()) {
            value = String.valueOf(loc.getSpeed()) + " m/s";
        }
        ((TextView) findViewById(R.id.txtV_Route_Speed)).setText(value);
    }

    public void refreshTrackingPosition(List<GeoPoint> route, Location loc) {
    }

    public void clearTrackingPositions() {
        MapView map = (MapView) findViewById(R.id.map2);
        map.getOverlays().clear();
    }

    @Override
    protected void onStop() {
        if (showPosition != null) {
            showPosition.stop();
        }
        super.onStop();
    }

    public class PrintRouteFromServer implements PrintRoute {
        private final MapView map;
        private Road road;

        public PrintRouteFromServer(MapView map) {
            this.map = map;
        }

        public void setRoad(Road road) {
            this.road = road;
        }

        public void run() {
            ImageView maneuverImg = (ImageView) findViewById(R.id.maneuverImg);
            TextView txtV_Route_InstructionNode = (TextView) findViewById(R.id.txtV_Route_InstructionNode);
            TextView txtV_Route_TimeNode = (TextView) findViewById(R.id.txtV_Route_TimeNode);
            TextView txtV_Route_Speed = (TextView) findViewById(R.id.txtV_Route_Speed);
            TextView txtV_Route_Time = (TextView) findViewById(R.id.txtV_Route_Time);
            TextView textViewRouteMaxLength = (TextView) findViewById(R.id.txtV_Route_MaxLength);
            TextView txtV_Route_DistanceNode = (TextView) findViewById(R.id.txtV_Route_DistanceNode);
            IMapController mapController = map.getController();
            mapController.setZoom(14);
            txtV_Route_Speed.setText("0 m/s");
            roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 8, RouteActivity.this);
            map.getOverlays().add(roadOverlay);
            TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
            textViewRouteMaxLength.setText(RoutingUtil.getLengthText(road.mLength));
            txtV_Route_Time.setText(RoutingUtil.getDurationText(road.mDuration));
            roadNodes.addAll(road.mNodes);
            for (int i = 0; i < road.mNodes.size(); i++) {
                RoadNode node = road.mNodes.get(i);
                Marker nodeMarker = printMarker(map, node.mLocation, "Step " + i, R.drawable.marker_node, Road.getLengthDurationText(node.mLength, node.mDuration));
                nodeMarker.setSnippet(node.mInstructions);
                int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
                if (iconId != R.drawable.ic_empty) {
                    Drawable icon2 = getResources().getDrawable(iconId);
                    nodeMarker.setImage(icon2);
                }
            }
            int iconId = iconIds.getResourceId(roadNodes.get(0).mManeuverType, R.drawable.ic_empty);
            if (iconId != R.drawable.ic_empty) {
                Drawable icon2 = getResources().getDrawable(iconId);
                maneuverImg.setImageDrawable(icon2);
            }
            int distance = Math.round(gpsManager.getActualPosition().distanceTo(roadNodes.get(0).mLocation));
            txtV_Route_InstructionNode.setText(roadNodes.get(0).mInstructions);
            txtV_Route_DistanceNode.setText(String.valueOf(distance) + " m");

            double time = distance / 4.0;
            Log.i(this.getClass().getName(), RoutingUtil.getDurationText(time));
            txtV_Route_TimeNode.setText(RoutingUtil.getDurationText(time));
            String text = "Za " + distance + "metrów " + roadNodes.get(0).mInstructions;
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            map.invalidate();
        }
    }
}
