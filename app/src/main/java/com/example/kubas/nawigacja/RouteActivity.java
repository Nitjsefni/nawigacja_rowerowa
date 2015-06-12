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

import com.example.kubas.nawigacja.client.OwnOSRMRoadManager;
import com.example.kubas.nawigacja.client.SavedRouteOSMRRoadManager;
import com.example.kubas.nawigacja.data.DataManager;
import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.data.model.Travel;
import com.example.kubas.nawigacja.gps.GPSManager;
import com.example.kubas.nawigacja.routing.RoutingUtil;
import com.example.kubas.nawigacja.tracking.ShowPosition;
import com.example.kubas.nawigacja.tracking.Trackable;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteActivity extends Activity implements Trackable {
    public static final double DEFAULT_SPEED = 4.0;
    private final GPSManager gpsManager = GPSManager.getInstance();
    private ShowPosition showPosition;
    private RouteViewManager routeViewManager;
    private RefreshRoute refreshView;
    private StartRoute startView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_route);

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
        RoutePoints points = (RoutePoints) extras.get("points");
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
        routeViewManager = new RouteViewManager(this, points);
        RoadManager roadManager;
        int routeId = extras.getInt("routeId", -1);
        if (routeId == -1) {
            roadManager = new OwnOSRMRoadManager();
            refreshView = new RefreshViewWithRouting();
            startView = new StartViewWithRouting(map);
        } else {
            roadManager = new SavedRouteOSMRRoadManager(routeId);
            refreshView = null;
            startView = new StartViewWithoutRouting(map);
        }
        new Thread(new GetRoadFromServer(points, roadManager)).start();
        showPosition = new ShowPosition(this, 5000);
    }


    public void refreshMapPosition(final Location loc) {
        if (refreshView != null) {
            refreshView.setLoc(loc);
            runOnUiThread(refreshView);
        }
    }


    private Location getLocation(GeoPoint point) {
        Location nextNode = new Location("gps");
        nextNode.setLatitude(point.getLatitude());
        nextNode.setLongitude(point.getLongitude());
        return nextNode;
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


    private class StartViewWithRouting implements StartRoute {
        private final MapView map;
        private Travel travel;

        public StartViewWithRouting(MapView map) {
            this.map = map;
        }


        public void run() {
            Road road = travel.getRoad();
            map.getController().setZoom(14);
            int distance = Math.round(gpsManager.getActualPosition().distanceTo(road.mNodes.get(0).mLocation));
            routeViewManager.refreshOverlays();
            routeViewManager.printSpeed(gpsManager.getActualLocation());
            routeViewManager.setRouteSummary(road.mLength * 1000, road.mDuration);
            routeViewManager.setInstructionView(road.mNodes.get(0), distance, distance / DEFAULT_SPEED);
            routeViewManager.speakInstruction(road.mNodes, gpsManager.getActualLocation(), true);
        }

        @Override
        public void setTravel(Travel travel) {
            this.travel = travel;
        }
    }

    private class StartViewWithoutRouting implements StartRoute {
        private final MapView map;
        private Travel travel;

        public StartViewWithoutRouting(MapView map) {
            this.map = map;
        }


        public void run() {
            Road road = travel.getRoad();
            map.getController().setZoom(14);
            routeViewManager.refreshOverlays();
            routeViewManager.setRouteSummary(road.mLength * 1000, road.mDuration);
        }

        @Override
        public void setTravel(Travel travel) {
            this.travel = travel;
        }
    }

    private class RefreshViewWithRouting implements RefreshRoute {
        private Location loc;

        @Override
        public void run() {
            Travel travel = DataManager.getInstance().getTravel();
            double totalLength = 0.0;
            double totalDuration = 0.0;
            for (RoadNode node : travel.getRoad().mNodes) {
                totalDuration += node.mDuration;
                totalLength += node.mLength * 1000;
            }
            int distance = Math.round(gpsManager.getActualPosition().distanceTo(travel.getRoad().mNodes.get(0).mLocation));
            MapView map = (MapView) findViewById(R.id.map2);
            map.getController().setZoom(17);
            map.getController().setCenter(new GeoPoint(loc));
            Marker currentPositionMarker = routeViewManager.createMarker(new GeoPoint(loc), "Aktualna pozycja", android.R.drawable.arrow_down_float, "");
            routeViewManager.refreshOverlays(currentPositionMarker);
            routeViewManager.printSpeed(loc);
            routeViewManager.setInstructionView(travel.getRoad().mNodes.get(0), distance, distance / DEFAULT_SPEED);
            routeViewManager.setRouteSummary(totalLength, totalDuration);
            routeViewManager.speakInstruction(travel.getRoad().mNodes, loc, false);
        }

        public void setLoc(Location loc) {
            this.loc = loc;
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
                Road road = roadManager.getRoad(waypoints);
                Travel travel = new Travel();
                travel.setRoad(road);
                DataManager.getInstance().setTravel(travel);
                routeViewManager.setRoadOverlay(RoadManager.buildRoadOverlay(road, Color.RED, 8, RouteActivity.this));
                startView.setTravel(travel);
                runOnUiThread(startView);
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    private class RouteViewManager {
        private Activity activity;
        private MapView map;
        private Marker endMarker, startMarker, viaMarker;
        private Polyline roadOverlay;
        private TextToSpeech textToSpeech;
        private Location previousLoc;

        public RouteViewManager(Activity activity, RoutePoints points) {
            this.activity = activity;

            map = (MapView) activity.findViewById(R.id.map2);
            if (points.getStartPoint() != null) {
                map.getController().setCenter(points.getStartPoint().getGeoPoint());
                startMarker = createMarker(points.getStartPoint().getGeoPoint(), "Punkt poczatkowy", R.drawable.marker_departure, points.getStartPoint().getDescription());
            }
            if (points.getEndPoint() != null) {
                endMarker = createMarker(points.getEndPoint().getGeoPoint(), "Punkt koncowy", R.drawable.marker_destination, points.getEndPoint().getDescription());
            }
            if (points.getMidPoint() != null) {
                viaMarker = createMarker(points.getMidPoint().getGeoPoint(), "Punkt posredni", R.drawable.marker_via, points.getMidPoint().getDescription());
            }
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        textToSpeech.setLanguage(new Locale("pl"));
                    }
                }
            });
        }

        public void printInstructionsAsPoints(List<RoadNode> instructions, MapView map) {
            TypedArray iconIds = activity.getResources().obtainTypedArray(R.array.direction_icons);
            for (int i = 0; i < instructions.size(); i++) {
                RoadNode node = instructions.get(i);
                Marker nodeMarker = createMarker(node.mLocation, "Step " + i, R.drawable.marker_node, Road.getLengthDurationText(node.mLength * 1000, node.mDuration));
                nodeMarker.setSnippet(node.mInstructions);
                int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
                if (iconId != R.drawable.ic_empty) {
                    Drawable icon2 = activity.getResources().getDrawable(iconId);
                    nodeMarker.setImage(icon2);
                }
                map.getOverlays().add(nodeMarker);
            }
        }

        private Marker createMarker(GeoPoint point, String name, int resourceToShow, String description) {
            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(activity.getResources().getDrawable(resourceToShow));
            marker.setTitle(name);
            marker.setSubDescription(description);
            return marker;
        }

        private void printSpeed(Location loc) {
            String value = "0 km/h";
            if (loc.hasSpeed()) {
                value = Math.round(loc.getSpeed() * 3.6) + " km/h";
            }
            ((TextView) activity.findViewById(R.id.txtV_Route_Speed)).setText(value);
        }

        private void setInstructionView(RoadNode node, double length, double time) {
            TypedArray iconIds = activity.getResources().obtainTypedArray(R.array.direction_icons);
            ImageView maneuverImg = (ImageView) activity.findViewById(R.id.maneuverImg);
            TextView txtV_Route_DistanceNode = (TextView) activity.findViewById(R.id.txtV_Route_DistanceNode);
            TextView txtV_Route_InstructionNode = (TextView) activity.findViewById(R.id.txtV_Route_InstructionNode);
            TextView txtV_Route_TimeNode = (TextView) activity.findViewById(R.id.txtV_Route_TimeNode);
            txtV_Route_InstructionNode.setText(node.mInstructions);
            txtV_Route_DistanceNode.setText(RoutingUtil.getLengthText(length));
            txtV_Route_TimeNode.setText(RoutingUtil.getDurationText(time));
            int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
            if (iconId != R.drawable.ic_empty) {
                Drawable icon2 = activity.getResources().getDrawable(iconId);
                maneuverImg.setImageDrawable(icon2);
            }
        }

        private void setRouteSummary(double length, double time) {
            TextView txtV_Route_Time = (TextView) activity.findViewById(R.id.txtV_Route_Time);
            TextView textViewRouteMaxLength = (TextView) activity.findViewById(R.id.txtV_Route_MaxLength);
            textViewRouteMaxLength.setText(RoutingUtil.getLengthText(length));
            txtV_Route_Time.setText(RoutingUtil.getDurationText(time));
        }

        private void refreshOverlays(Overlay... additional) {
            MapView map = (MapView) activity.findViewById(R.id.map2);
            map.getOverlays().clear();
            if (roadOverlay != null) {
                map.getOverlays().add(roadOverlay);
            }
            if (startMarker != null) {
                map.getOverlays().add(startMarker);
            }
            if (viaMarker != null) {
                map.getOverlays().add(viaMarker);
            }
            if (endMarker != null) {
                map.getOverlays().add(endMarker);
            }
            for (Overlay overlay : additional) {
                map.getOverlays().add(overlay);
            }
//            printInstructionsAsPoints(roadNodes, map);
            map.invalidate();
        }

        public void setRoadOverlay(Polyline roadOverlay) {
            this.roadOverlay = roadOverlay;
        }

        private void speakInstruction(List<RoadNode> roadNodes, Location loc, boolean forceSpeak) {
            ///jak stoje w miejscu to gada kilka razy. powinno byc zabezpieczenie, ze gada tylko jesli zmienila stan ( stany to zakresy odleglosci od punktu)
            //to rozwiazanie nie starcza:(
            if ((loc.equals(previousLoc) || !loc.hasSpeed()) && !forceSpeak) {
                return;
            }
            previousLoc = loc;
            Location nextNode = getLocation(roadNodes.get(0).mLocation);
            String text;
            float distance = loc.distanceTo(nextNode);
            if (roadNodes.size() == 0 && loc.distanceTo(getLocation(roadNodes.get(roadNodes.size() - 1).mLocation)) < 25) {
                text = "Dojechales do celu podróży";
            } else if ((forceSpeak) || (distance > 25 && distance < 40) || (distance > 15 && distance < 20)) {
                text = "Za " + RoutingUtil.getLengthTextToSpeech(distance) + roadNodes.get(0).mInstructions;
            } else if ((distance < 10)) {
                text = roadNodes.get(0).mInstructions;
            } else if (distance < 5) {
                roadNodes.remove(0);
                nextNode = getLocation(roadNodes.get(0).mLocation);
                text = "Za " + RoutingUtil.getLengthTextToSpeech(loc.distanceTo(nextNode)) + roadNodes.get(0).mInstructions;
            } else {
                return;
            }
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private interface StartRoute extends Runnable {
        void setTravel(Travel travel);
    }

    private interface RefreshRoute extends Runnable {
        void setLoc(Location loc);
    }
}