package com.example.kubas.nawigacja.data.model;

import com.example.kubas.nawigacja.routing.RoutingUtil;

public class Route {
    private final int id;
    private final String name;
    private final String description;
    private final double length;
    private final double time;

    public Route(int id, String name, String description, double length, double time) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.length = length;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getLength() {
        return length;
    }

    /**
     * @return times in seconds
     */
    public double getTime() {
        return time;
    }

    public String getFormattedLength() {
        return RoutingUtil.getFormattedDistance(this.length * 1000);
    }

    public String getFormattedTime() {
        return RoutingUtil.getFormattedTime(this.time);
    }
}
