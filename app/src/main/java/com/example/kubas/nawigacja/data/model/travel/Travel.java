package com.example.kubas.nawigacja.data.model.travel;

import android.location.Location;
import android.os.Handler;

import com.example.kubas.nawigacja.gps.GPSManager;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Travel implements Runnable {
    public static final int HISTORY_REFRESH_FREQUENCY = 1000;
    private Date start;
    private double length;
    private List<RoadElement> history = new ArrayList<>();
    private Road road;
    private Handler handler;
    private Location previousLocation;
    private GPSManager gpsManager = GPSManager.getInstance();
    private RoadNodeToSpeak nextInstructionNode;

    public Travel() {
        this.start = Calendar.getInstance().getTime();
        this.handler = new Handler();
        this.handler.postDelayed(this, HISTORY_REFRESH_FREQUENCY);
    }

    public Date getStart() {
        return start;
    }

    public double getLength() {
        return length;
    }

    public double getAverageSpeed() {
        return getLength() / getTotalRoadDuration();
    }

    public long getDuration() {
        long milliseconds = Calendar.getInstance().getTime().getTime() - getStart().getTime();
        return milliseconds / 1000 / 60;
    }

    public void setRoad(Road road) {
        this.road = road;
        setNextInstructionNode(road.mNodes.get(0));
    }

    public List<RoadNode> getInstructionsNodes() {
        return Collections.unmodifiableList(road.mNodes);
    }

    public RoadNodeToSpeak getNextInstructionNode() {
        return nextInstructionNode;
    }

    private void setNextInstructionNode(RoadNode nextInstructionNode) {
        this.nextInstructionNode = new RoadNodeToSpeak(nextInstructionNode);
    }

    public double getTotalRoadDuration() {
        return road.mDuration;
    }

    @Override
    public void run() {
        Location actualLocation = gpsManager.getActualLocation();
        addToTravelHistory(actualLocation);
        calculateRoadPart(actualLocation);
        handler.postDelayed(this, HISTORY_REFRESH_FREQUENCY);
    }

    private void calculateRoadPart(Location actualLocation) {
        //TODO usuwanie minietych instrukcji
        for (int i = 0; i < road.mRouteHigh.size(); i++) {
            GeoPoint lastPoint = road.mRouteHigh.get(i);
            GeoPoint nextPoint = road.mRouteHigh.get(i + 1);
            //jesli miedzy last i next to usun wszystko wczesniejsze niz last i skoncz szukanie
            //jesli ponizej last to nic nie rob,
            // jesli powyzej, to szukaj dalej
        }
        //na podstawie road.mRouteHigh usun¹æ nale¿y road.mNodes, albo tym samym algorytmem co przed chwila.
        if (!nextInstructionNode.isSameRoadNode(road.mNodes.get(0))) {
            setNextInstructionNode(road.mNodes.get(0));
        }

        //TODO jeœli zwiêksza siê odleg³oœæ do nastêpnego punktu, to znaczy ¿e coœ nie tak i duza szansa, ze trzeba przeliczyc trase.
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
        return road.mLength * 1000;
    }
}
