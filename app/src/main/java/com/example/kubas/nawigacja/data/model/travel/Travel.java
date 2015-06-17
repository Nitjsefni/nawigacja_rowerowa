package com.example.kubas.nawigacja.data.model.travel;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;

import com.example.kubas.nawigacja.data.Times;
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
    private static final double DISTANCE_TOLERANCE = 10;
    private static final double DISTANCE_OUTSIDE_ROAD_TOLERANCE = 40;
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
        this.handler.postDelayed(this, Times.HISTORY_REFRESH_FREQUENCY);
    }

    public Travel(Road road, RoutePoints points) {
        this();
        this.road = road;
        this.points = points;
        setNextInstructionNode(road.mNodes.get(0));
        //tymczasowo - poki serwer nie wysyla poczatku i konca
        if (points.getStartPoint() != null) {
            road.mRouteHigh.add(0, points.getStartPoint().getGeoPoint());

        }
        if (points.getEndPoint() != null) {
            road.mRouteHigh.add(points.getEndPoint().getGeoPoint());
        }
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
        return 3.6 * getLength() / (getDuration() / 1000);
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

    public Location bindPointToRoadLine(Location location) {
        if (road.mRouteHigh.size() < 2) {
            return location;
        }
        return getLocationOnLine(road.mRouteHigh.get(0), road.mRouteHigh.get(1), location);
    }

    @Override
    public void run() {
        Location actualLocation = gpsManager.getActualLocation();
        addToTravelHistory(actualLocation);
        if (road != null) {
            calculateRoadPart(actualLocation);
        }
        handler.postDelayed(this, Times.HISTORY_REFRESH_FREQUENCY);
    }

    private void calculateRoadPart(Location actualLocation) {
        List<GeoPoint> removed = removeElements(road.mRouteHigh, getElementsToCutNumber(actualLocation, road.mRouteHigh));
        removeElementsByExample(road.mNodes, removed);
        if (road.mRouteHigh.size() < 1) {
            setNextInstructionNode(null);
            return;
        }
        if (!nextInstructionNode.isSameRoadNode(road.mNodes.get(0))) {
            setNextInstructionNode(road.mNodes.get(0));
        }
    }

    private void removeElementsByExample(List<RoadNode> roadNodes, List<GeoPoint> removed) {
        List<RoadNode> toRemove = new ArrayList<>();
        for (RoadNode roadNode : roadNodes) {
            if (removed.contains(roadNode.mLocation)) {
                toRemove.add(roadNode);
            } else {
                break;
            }
        }
        for (RoadNode roadNode : toRemove) {
            roadNodes.remove(roadNode);
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
        return isPointOnRoad(location, road.mRouteHigh, 0, DISTANCE_OUTSIDE_ROAD_TOLERANCE);
    }

    private List<GeoPoint> removeElements(List<GeoPoint> mRouteHigh, int elementsToCutNumber) {
        List<GeoPoint> removed = new ArrayList<>();
        for (int i = 0; i < elementsToCutNumber; i++) {
            removed.add(mRouteHigh.get(0));
            mRouteHigh.remove(0);
        }
        if (!mRouteHigh.isEmpty()) {
            removed.add(mRouteHigh.get(0));
        }
        return removed;
    }

    private int getElementsToCutNumber(Location actualLocation, List<GeoPoint> mRouteHigh) {
        for (int i = 0; i < mRouteHigh.size() - 1; i++) {
            if (!isPointOnRoad(actualLocation, mRouteHigh, i, DISTANCE_TOLERANCE)) {
                continue;
            }
            return i;
        }
        return 0;
    }

    private boolean isPointOnRoad(Location actualLocation, List<GeoPoint> mRouteHigh, int i, double tolerance) {
        GeoPoint lastPoint = mRouteHigh.get(i);
        GeoPoint nextPoint = mRouteHigh.get(i + 1);
        return getDistanceFromLine(lastPoint, nextPoint, actualLocation) <= tolerance;
    }

    private double getDistanceFromLine(GeoPoint p1, GeoPoint p2, Location point) {
        Location location = getLocationOnLine(p1, p2, point);
        return location.distanceTo(point);
    }

    private Location getLocationOnLine(GeoPoint p1, GeoPoint p2, Location point) {
        double a;
        double c;
        double b;
        // obliczenie współrzędnych prostopadłej prostej
        double bParallel;
        double aParallel;
        double cParallel;
        if (p1.getLatitude() == p2.getLatitude()) {
            a = 0;
            b = 1;
            c = -1 * p1.getLatitude();
            aParallel = 1;
            bParallel = 0;
            cParallel = -1 * point.getLongitude();
        } else if (p1.getLongitude() == p2.getLongitude()) {
            aParallel = 0;
            bParallel = 1;
            cParallel = -1 * point.getLatitude();
            a = 1;
            b = 0;
            c = -1 * p1.getLongitude();
        } else {
            //x=lon
            a = (p1.getLatitude() - p2.getLatitude()) / (p1.getLongitude() - p2.getLongitude());
            b = -1;
            c = p2.getLatitude() - a * p2.getLongitude();
            aParallel = -1 / a;
            cParallel = point.getLatitude() - aParallel * point.getLongitude();
            bParallel = -1;
        }
        double y = (c * aParallel - a * cParallel) / (bParallel * a - aParallel * b);
        double x = (-b * y - c) / a;

        y = getLimit(p1.getLatitude(), p2.getLatitude(), y);
        x = getLimit(p1.getLongitude(), p2.getLongitude(), x);
        Location location = new Location(point);
        location.setLatitude(y);
        location.setLongitude(x);
        return location;
    }

    private double getLimit(double p1Longitude, double p2Longitude, double value) {
        double max = Math.max(p1Longitude, p2Longitude);
        double min = Math.min(p1Longitude, p2Longitude);
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }
        return value;
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

    public GeoPoint getNextRoadPoint() {
        if (road.mRouteHigh.size() > 1) {
            return road.mRouteHigh.get(1);
        }
        return null;
    }

    public float getActualBearing() {
//        double startLat = Math.toRadians(RoutingUtil.convertToLocation(road.mRouteHigh.get(1)).getLatitude());
//        double startLong = Math.toRadians(RoutingUtil.convertToLocation(road.mRouteHigh.get(1)).getLongitude());
//        double endLat = Math.toRadians(RoutingUtil.convertToLocation(road.mRouteHigh.get(0)).getLatitude());
//        double endLong = Math.toRadians(RoutingUtil.convertToLocation(road.mRouteHigh.get(0)).getLongitude());
        if (road.mRouteHigh.size() < 2) {
            return 0;
        }
        //dist = RoutingUtil.convertToLocation(road.mRouteHigh.get(0)).distanceTo(RoutingUtil.convertToLocation(road.mRouteHigh.get(1)));

        // *** Code to calculate where the arrow should point ***
        double angle = Math.atan2((road.mRouteHigh.get(1).getLatitude() - road.mRouteHigh.get(0).getLatitude()), road.mRouteHigh.get(1).getLongitude() - road.mRouteHigh.get(0).getLongitude());
        angle = Math.toDegrees(angle) - 90;

        return (float) angle;
    }

    public List<GeoPoint> getRoadPoints() {
        return road.mRouteHigh;
    }
}
