package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kubas.nawigacja.client.OwnOSRMRoadManager;
import com.example.kubas.nawigacja.client.SavedRouteOSMRRoadManager;
import com.example.kubas.nawigacja.data.DataManager;
import com.example.kubas.nawigacja.data.Times;
import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.data.model.travel.RoadNodeToSpeak;
import com.example.kubas.nawigacja.data.model.travel.Travel;
import com.example.kubas.nawigacja.gps.GPSManager;
import com.example.kubas.nawigacja.routing.RoutingUtil;
import com.example.kubas.nawigacja.tracking.ShowPosition;
import com.example.kubas.nawigacja.tracking.Trackable;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteActivity extends Activity implements Trackable {
    private final GPSManager gpsManager = GPSManager.getInstance();
    private ShowPosition showPosition;
    private RouteViewManager routeViewManager;
    private RefreshRoute refreshView;
    private StartRoute startView;
    private PowerManager.WakeLock mWakeLock;

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

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
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        RoadManager roadManager;
        int routeId = extras.getInt("routeId", -1);
        boolean resume = extras.getBoolean("resume", false);
        GetData roadFromServer;
        RoutePoints points;
        DataManager dataManager = DataManager.getInstance();

        if (resume) {
            points = dataManager.getTravel().getPoints();
        } else {
            points = (RoutePoints) extras.get("points");
        }
        if (points == null) {
            points = new RoutePoints();
        }
        if (routeId == -1) {
            roadManager = new OwnOSRMRoadManager();
            refreshView = new RefreshViewWithRouting(points);
            startView = new StartViewWithRouting(map);
        } else {
            roadManager = new SavedRouteOSMRRoadManager(routeId);
            refreshView = null;
            startView = new StartViewWithoutRouting();
        }
        if (resume) {
            roadFromServer = new ResumeRoad();
        } else {
            roadFromServer = new GetRoadFromServer(points, roadManager);
        }

        routeViewManager = new RouteViewManager(this);
        routeViewManager.zoomAndCenterMap(14, null);
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        new Thread(roadFromServer).start();
        showPosition = new ShowPosition(this, Times.SHOW_POSITION_FREQUENCY_TIME);
        ImageButton goToCounter = (ImageButton) findViewById(R.id.imgBtn_goToCounter);
        goToCounter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CounterActivity.class));
            }
        });
    }



    public void refreshMapPosition(final Location loc) {
        if (refreshView != null) {
            refreshView.setLoc(loc);
            runOnUiThread(refreshView);
        }
    }

    public void refreshTrackingPosition(List<GeoPoint> route, Location loc) {
    }

    public void clearTrackingPositions() {
        MapView map = (MapView) findViewById(R.id.map2);
        map.getOverlays().clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPosition = new ShowPosition(this, Times.SHOW_POSITION_FREQUENCY_TIME);
    }

    @Override
    protected void onPause() {
        if (showPosition != null) {
            showPosition.stop();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mWakeLock.release();
        super.onDestroy();
    }

    private interface StartRoute extends Runnable {
        void setTravel(Travel travel);
    }

    private interface RefreshRoute extends Runnable {
        void setLoc(Location loc);
    }

    private interface GetData extends Runnable {
    }

    private class StartViewWithRouting implements StartRoute {
        private final MapView map;
        private Travel travel;

        public StartViewWithRouting(MapView map) {
            this.map = map;
        }

        public void run() {
            map.getController().setZoom(14);
            int distance = Math.round(gpsManager.getActualPosition().distanceTo(travel.getNextInstructionNode().getLocation()));
            routeViewManager.refreshOverlays();
            routeViewManager.printSpeed(gpsManager.getActualLocation());
            routeViewManager.setRouteSummary(travel.getTotalRoadLength(), travel.getTotalRoadDuration());
            routeViewManager.setInstructionView(travel.getNextInstructionNode(), distance);
            routeViewManager.speakInstruction(travel.getNextInstructionNode(), gpsManager.getActualLocation());
        }

        @Override
        public void setTravel(Travel travel) {
            this.travel = travel;
        }
    }

    private class StartViewWithoutRouting implements StartRoute {
        private Travel travel;

        public void run() {
            routeViewManager.zoomAndCenterMap(14, null);
            routeViewManager.refreshOverlays();
            routeViewManager.setRouteSummary(travel.getTotalRoadLength(), travel.getTotalRoadDuration());
        }

        @Override
        public void setTravel(Travel travel) {
            this.travel = travel;
        }
    }

    private class RefreshViewWithRouting implements RefreshRoute {

        private Location loc;
        private RoutePoints points;

        public RefreshViewWithRouting(RoutePoints points) {
            this.points = points;
        }

        @Override
        public void run() {
            Travel travel = DataManager.getInstance().getTravel();
            if (!travel.isRoadChoosen()) {
                return;
            }

//            if (!travel.isOnRoad(loc)) {
//                if (RouteActivity.isOnline(RouteActivity.this)) {
//                    routeViewManager.speakNewRoad(RouteActivity.isOnline(RouteActivity.this));
//                    points.setStartPoint(null);
//                    findStartPoint(points);
//                    finish();
//                    getIntent().putExtra("points", points);
//                    startActivity(getIntent());
//                } else {
//                    routeViewManager.speakNewRoad(RouteActivity.isOnline(RouteActivity.this));
//                }
//        }
            double totalLength = 0.0;
            double totalDuration = 0.0;
            for (RoadNode node : travel.getInstructionsNodes()) {
                totalDuration += node.mDuration;
                totalLength += node.mLength * 1000;
            }
            GeoPoint nextNodeGeoPoint = travel.getNextInstructionNode().getLocation();
            int distance = Math.round(gpsManager.getActualPosition().distanceTo(nextNodeGeoPoint));
//            Marker currentPositionMarker = routeViewManager.createMarker(new GeoPoint(loc), "Aktualna pozycja", R.drawable.arrow, "");
            // to jest wersja z dowiązywaniem do aktualnej drogi
            Location locationOnRoad;
            if (travel.isOnRoad(loc)) {
                locationOnRoad = travel.bindPointToRoadLine(loc);
            } else {
                locationOnRoad = loc;
            }
            routeViewManager.zoomAndCenterMap(17, locationOnRoad);

            Marker currentPositionMarker = routeViewManager.createMarker(new GeoPoint(locationOnRoad), "Aktualna pozycja", R.drawable.arrow, "");
            routeViewManager.refreshOverlays(currentPositionMarker);
            routeViewManager.rotateMap(travel);
            routeViewManager.printSpeed(loc);
            routeViewManager.setInstructionView(travel.getNextInstructionNode(), distance);
            routeViewManager.setRouteSummary(totalLength, totalDuration);
            routeViewManager.speakInstruction(travel.getNextInstructionNode(), loc);
        }

        public void setLoc(Location loc) {
            this.loc = loc;
        }
    }

    private class ResumeRoad implements GetData {

        public void run() {
            final Travel travel = DataManager.getInstance().getTravel();
            if (travel == null || !travel.isRoadChoosen()) {
                refreshView = null;
                startView = null;
                return;
            }
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        routeViewManager.setRoadOverlay(travel.getRoadOverlay(RouteActivity.this));
                        startView.setTravel(travel);
                    }


                });
                runOnUiThread(startView);
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.getMessage(), e);
            }
        }
    }


    private class GetRoadFromServer implements GetData {
        private RoutePoints points;
        private RoadManager roadManager;

        public GetRoadFromServer(RoutePoints points, RoadManager roadManager) {
            this.points = points;
            this.roadManager = roadManager;
        }

        public void run() {
            findStartPoint(points);
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
                final Road road = roadManager.getRoad(waypoints);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Travel travel = new Travel(road, points);
                        DataManager.getInstance().setTravel(travel);
                        routeViewManager.setRoadOverlay(RoadManager.buildRoadOverlay(road, Color.RED, 8, RouteActivity.this));
                        startView.setTravel(travel);
                        routeViewManager.setPoints(points);
                    }
                });
                runOnUiThread(startView);
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.getMessage(), e);
            }
        }
        private void findStartPoint(RoutePoints points) {
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
        }
    }

    private class RouteViewManager {
        private Activity activity;
        private Marker endMarker, startMarker, viaMarker;
        private Polyline roadOverlay;
        private TextToSpeech textToSpeech;
        private MapMode mode = MapMode.ROUTING;

        public RouteViewManager(Activity activity) {
            this.activity = activity;

            MapView map = (MapView) activity.findViewById(R.id.map2);
            map.setMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent scrollEvent) {
//                    mode = MapMode.PREVIEW;
                    return false;
                }

                @Override
                public boolean onZoom(ZoomEvent zoomEvent) {
                    //                    mode = MapMode.PREVIEW;
                    return false;
                }
            });
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        textToSpeech.setLanguage(new Locale("pl"));
                    }
                }
            });
        }

        public void setPoints(RoutePoints points) {
            if (points.getStartPoint() != null) {
                zoomAndCenterMap(0, points.getStartPoint().getLocation());
                startMarker = createMarker(points.getStartPoint().getGeoPoint(), "Punkt poczatkowy", R.drawable.marker_departure, points.getStartPoint().getDescription());
            }
            if (points.getEndPoint() != null) {
                endMarker = createMarker(points.getEndPoint().getGeoPoint(), "Punkt koncowy", R.drawable.marker_destination, points.getEndPoint().getDescription());
            }
            if (points.getMidPoint() != null) {
                viaMarker = createMarker(points.getMidPoint().getGeoPoint(), "Punkt posredni", R.drawable.marker_via, points.getMidPoint().getDescription());
            }
        }

        @Deprecated
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

        @Deprecated
        public void printNodesAsPoints(List<GeoPoint> instructions, MapView map) {
            for (int i = 0; i < instructions.size(); i++) {
                GeoPoint node = instructions.get(i);
                Marker nodeMarker = createMarker(node, "Step " + i, R.drawable.marker_node, "");
                map.getOverlays().add(nodeMarker);
            }
        }

        private Marker createMarker(GeoPoint point, String name, int resourceToShow, String description) {
            MapView map = (MapView) activity.findViewById(R.id.map2);
            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            Drawable drawable = activity.getResources().getDrawable(resourceToShow);
            marker.setIcon(drawable);
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

        private void setInstructionView(RoadNodeToSpeak node, double length) {
            TextView txtV_Route_InstructionNode = (TextView) activity.findViewById(R.id.txtV_Route_InstructionNode);
            TextView txtV_Route_DistanceNode = (TextView) activity.findViewById(R.id.txtV_Route_DistanceNode);
            txtV_Route_DistanceNode.setText(RoutingUtil.getFormattedDistance(length));
            if (node.getInstruction() == txtV_Route_InstructionNode.getText()) {
                return;
            }
            TypedArray iconIds = activity.getResources().obtainTypedArray(R.array.direction_icons);
            ImageView maneuverImg = (ImageView) activity.findViewById(R.id.maneuverImg);

            txtV_Route_InstructionNode.setText(node.getInstruction());

            int iconId = iconIds.getResourceId(node.getManeuverType(), R.drawable.ic_empty);
            if (iconId != R.drawable.ic_empty) {
                Drawable icon2 = activity.getResources().getDrawable(iconId);
                maneuverImg.setImageDrawable(icon2);
            }
        }

        private void setRouteSummary(double length, double time) {
            TextView txtV_Route_Time = (TextView) activity.findViewById(R.id.txtV_Route_Time);
            TextView textViewRouteMaxLength = (TextView) activity.findViewById(R.id.txtV_Route_MaxLength);
            textViewRouteMaxLength.setText(RoutingUtil.getFormattedDistance(length));
            txtV_Route_Time.setText(RoutingUtil.getFormattedTime(time));
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
//            printNodesAsPoints(DataManager.getInstance().getTravel().getRoadPoints(), map);
            map.invalidate();
        }

        public void setRoadOverlay(Polyline roadOverlay) {
            this.roadOverlay = roadOverlay;
        }

        private void speakInstruction(RoadNodeToSpeak roadNode, Location loc) {
            String text;
            text = roadNode.getInstructionText(loc);
            if (text == null) {
                return;
            }
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }

        private void speakNewRoad(boolean online) {
            String text;
            if (online) {
                text = "Wyjechałeś poza trasę, wyznaczanie nowej trasy";
            } else {
                text = "Wyjechałeś poza trasę, proszę zawróć";
            }
            if (text == null) {
                return;
            }
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }

        public void rotateMap(Travel travel) {
            MapView map = (MapView) activity.findViewById(R.id.map2);
            map.setMapOrientation(travel.getActualBearing());
        }

        public void zoomAndCenterMap(int zoom, Location location) {
            MapView map = (MapView) findViewById(R.id.map2);
            if (mode == MapMode.PREVIEW) {
                return;
            }
            if (zoom > 0) {
                map.getController().setZoom(zoom);
            }
            if (location != null) {
                map.getController().setCenter(new GeoPoint(location));
            }
        }
    }
}