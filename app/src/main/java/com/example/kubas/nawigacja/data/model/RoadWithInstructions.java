package com.example.kubas.nawigacja.data.model;

import org.osmdroid.bonuspack.routing.Road;

public class RoadWithInstructions {
    private Road road;

    public RoadWithInstructions(Road road) {
        this.road = road;
    }

    public Road getRoad() {
        return road;
    }
}
