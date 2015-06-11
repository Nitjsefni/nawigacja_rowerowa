package com.example.kubas.nawigacja.routing;

import org.osmdroid.bonuspack.routing.Road;

public interface PrintRoute extends Runnable {
    void setRoad(Road road);
}
