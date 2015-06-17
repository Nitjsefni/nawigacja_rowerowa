package com.example.kubas.nawigacja.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoutePoints implements Serializable {
    private GeoPosition startPoint;
    private GeoPosition endPoint;
    private GeoPosition midPoint;
    private List<GeoPosition> midPoints = new ArrayList<>();
    private RoadType roadType = new RoadType();

    public GeoPosition getStartPoint() {
        return startPoint;
    }

    public GeoPosition getEndPoint() {
        return endPoint;
    }

    public List<GeoPosition> getMidPoints() {
        return Collections.unmodifiableList(midPoints);
    }

    public void addMidPoints(GeoPosition midPoint) {
        this.midPoints.add(midPoint);
    }

    public GeoPosition getMidPoint() {
        return midPoint;
    }

    public boolean isMidPoint() {
        return midPoint != null;
    }

    public void setMidPoint(GeoPosition midPoint) {
        this.midPoint = midPoint;
    }

    public boolean isStartPoint() {
        return startPoint != null;
    }

    public void setStartPoint(GeoPosition startPoint) {
        this.startPoint = startPoint;
    }

    public boolean isEndPoint() {
        return endPoint != null;
    }

    public void setEndPoint(GeoPosition endPoint) {
        this.endPoint = endPoint;
    }

    public RoadType getRoadType() {
        return roadType;
    }

    public enum PointType {
        START,
        MID,
        END
    }
}
