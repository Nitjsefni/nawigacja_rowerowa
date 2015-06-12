package com.example.kubas.nawigacja.data.model.travel;

import android.location.Location;

import java.util.Date;

public class RoadElement {
    private final Date date;
    private final Location location;
    private final float length;

    public RoadElement(Date date, Location location, float length) {

        this.date = date;
        this.location = location;
        this.length = length;
    }

    public Date getDate() {
        return date;
    }

    public Location getLocation() {
        return location;
    }

    public float getLength() {
        return length;
    }
}
