package com.example.kubas.nawigacja.data.model;

import org.osmdroid.bonuspack.routing.Road;

import java.util.Calendar;
import java.util.Date;

public class Travel {
    private Date start;
    private double length;
    private Road road;

    public Travel() {
        this.start = Calendar.getInstance().getTime();
    }

    public Date getStart() {
        return start;
    }

    public double getLength() {
        return length;
    }

    public double getAverageSpeed() {
        return getLength() / getDuration();
    }

    public long getDuration() {
        long miliseconds = Calendar.getInstance().getTime().getTime() - getStart().getTime();
        return miliseconds / 1000 / 60;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public Road getRoad() {
        return road;
    }
}
