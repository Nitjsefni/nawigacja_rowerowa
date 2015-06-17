package com.example.kubas.nawigacja.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoutePoints implements Serializable {
    GeoPosition startPoint;
    GeoPosition endPoint;
    GeoPosition midPoint;
    RoadType type;
    List<GeoPosition> midPoints = new ArrayList<>();

    public GeoPosition getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(GeoPosition startPoint) {
        this.startPoint = startPoint;
    }

    public GeoPosition getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(GeoPosition endPoint) {
        this.endPoint = endPoint;
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

    public void setMidPoint(GeoPosition midPoint) {
        this.midPoint = midPoint;
    }

    public boolean isMidPoint() {
        return midPoint != null;
    }

    public boolean isStartPoint() {
        return startPoint!=null;
    }

    public boolean isEndPoint() {
        return endPoint!=null;
    }

    public enum PointType {
        START,
        MID,
        END
    }
}
