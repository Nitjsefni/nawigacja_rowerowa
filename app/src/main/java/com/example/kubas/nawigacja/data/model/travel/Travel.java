package com.example.kubas.nawigacja.data.model.travel;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;

import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.gps.GPSManager;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Travel implements Runnable {
    public static final int HISTORY_REFRESH_FREQUENCY = 1000;
    private static final double LAT_TOLERANCE = 0;
    private static final double DISTANCE_TOLERANCE = 0.05;
    private static final double LON_TOLERANCE = 0;
    private Date start;
    private double length;
    private List<RoadElement> history = new ArrayList<>();
    private Road road;
    private RoutePoints points;
    private Handler handler;
    private Location previousLocation;
    private GPSManager gpsManager = GPSManager.getInstance();
    private RoadNodeToSpeak nextInstructionNode;

    public Travel() {
        this.start = Calendar.getInstance().getTime();
        this.handler = new Handler();
        this.handler.postDelayed(this, HISTORY_REFRESH_FREQUENCY);
    }

    public Travel(Road road, RoutePoints points) {
        this();
        this.road = road;
        this.points = points;
        setNextInstructionNode(road.mNodes.get(0));
        //tymczasowo - poki serwer nie wysyla poczatku i konca
        RoadNode roadNode = new RoadNode();
        roadNode.mLocation = points.getStartPoint().getGeoPoint();
        roadNode.mInstructions = "Punkt strartowy";
        road.mNodes.add(0, roadNode);
//        road.mNodes.add(new RoadNode());
    }

    public Date getStart() {
        return start;
    }

    /**
     * @return length in meters
     */
    public double getLength() {
        return length;
    }

    public double getAverageSpeed() {
        if (getDuration() == 0) {
            return 0;
        }
        return getLength() / (getDuration() / 1000);
    }

    /**
     * @return time in milie
     */
    public long getDuration() {
        return Calendar.getInstance().getTime().getTime() - getStart().getTime();
    }


    public List<RoadNode> getInstructionsNodes() {
        if (road == null) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(road.mNodes);
    }

    public RoadNodeToSpeak getNextInstructionNode() {
        if (nextInstructionNode == null) {
            return new NullRoadNodeToSpeak();
        }
        return nextInstructionNode;
    }

    private void setNextInstructionNode(RoadNode nextInstructionNode) {
        this.nextInstructionNode = new RoadNodeToSpeak(nextInstructionNode);
    }

    public double getTotalRoadDuration() {
        if (road == null) {
            return 0;
        }
        return road.mDuration;
    }

    @Override
    public void run() {
        Location actualLocation = gpsManager.getActualLocation();
        addToTravelHistory(actualLocation);
        if (road != null) {
            calculateRoadPart(actualLocation);
        }
        handler.postDelayed(this, HISTORY_REFRESH_FREQUENCY);
    }

    private void calculateRoadPart(Location actualLocation) {
        removeElements(road.mRouteHigh, getElementsToCutNumber(actualLocation, road.mRouteHigh));
        removeElements(road.mNodes, getElementsToCutNumber(actualLocation, road.mNodes));
        if (road.mRouteHigh.size() < 2) {
            setNextInstructionNode(null);
        }
        if (!nextInstructionNode.isSameRoadNode(road.mNodes.get(1))) {
            setNextInstructionNode(road.mNodes.get(1));
        }
    }

    public boolean isOnRoad(Location location) {
        if (road == null) {
            //nie ma drogi wiec nie jest istotna ta informacja
            return true;
        }
        if (road.mRouteHigh.size() < 2) {
            //koniec trasy, czyli jest na drodze
            return true;
        }
        calculateRoadPart(location);
        return isPointOnRoad(location, road.mRouteHigh, 0);
    }

    private void removeElements(List mRouteHigh, int elementsToCutNumber) {
        for (int i = 0; i < elementsToCutNumber; i++) {
            mRouteHigh.remove(i);
        }
    }

    private int getElementsToCutNumber(Location actualLocation, List mRouteHigh) {
        for (int i = 0; i < mRouteHigh.size(); i++) {
            if (!isPointOnRoad(actualLocation, mRouteHigh, i)) {
                continue;
            }
            return i;
        }
        return 0;
    }

    private boolean isPointOnRoad(Location actualLocation, List mRouteHigh, int i) {
        GeoPoint lastPoint = getGeoPoint(mRouteHigh.get(i));
        GeoPoint nextPoint = getGeoPoint(mRouteHigh.get(i + 1));
        return !isOutsideRoadBorder(lastPoint, nextPoint) &&
                getDistanceFromLine(lastPoint, nextPoint, actualLocation) <= DISTANCE_TOLERANCE;
    }

    private boolean isOutsideRoadBorder(GeoPoint lastPoint, GeoPoint nextPoint) {
        double maxLat = Math.max(lastPoint.getLatitude(), nextPoint.getLatitude()) + LAT_TOLERANCE;
        double minLat = Math.min(lastPoint.getLatitude(), nextPoint.getLatitude()) + LAT_TOLERANCE;
        double maxLon = Math.max(lastPoint.getLongitude(), nextPoint.getLongitude() + LON_TOLERANCE);
        double minLon = Math.min(lastPoint.getLongitude(), nextPoint.getLongitude() + LON_TOLERANCE);
        return lastPoint.getLatitude() > maxLat || lastPoint.getLatitude() < minLat ||
                lastPoint.getLongitude() > maxLon || lastPoint.getLongitude() < minLon;
    }

    private GeoPoint getGeoPoint(Object object) {
        if (object.getClass().equals(GeoPoint.class)) {
            return (GeoPoint) object;
        }
        if (object.getClass().equals(RoadNode.class)) {
            return ((RoadNode) object).mLocation;
        }
        return new GeoPoint(0, 0);
    }

    private double getDistanceFromLine(GeoPoint p1, GeoPoint p2, Location point) {
        double a;
        double c;
        double b;
        if (p1.getLongitude() == p2.getLongitude()) {
            a = 1;
            b = 0;
            c = p1.getLongitude();
        } else {
            a = (p1.getLatitude() - p2.getLatitude()) / (p1.getLongitude() - p2.getLongitude());
            c = p2.getLatitude() - a * p2.getLongitude();
            b = -1;
        }
        return (a * point.getLongitude() + b * point.getLatitude() + c) / Math.sqrt(a * a + b * b);
    }

    private void addToTravelHistory(Location actualLocation) {
        float lengthOfPart = 0;
        if (previousLocation != null) {
            lengthOfPart = actualLocation.distanceTo(previousLocation);
        }
        length += lengthOfPart;
        history.add(new RoadElement(new Date(), actualLocation, lengthOfPart));
        previousLocation = actualLocation;
    }

    public List<RoadElement> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * @return length in meters
     */
    public double getTotalRoadLength() {
        if (road == null) {
            return 0;
        }
        return road.mLength * 1000;
    }

    public boolean isRoadChoosen() {
        return road != null;
    }

    public RoutePoints getPoints() {
        return points;
    }

    public Polyline getRoadOverlay(Context context) {
        return RoadManager.buildRoadOverlay(road, Color.RED, 8, context);
    }
}
