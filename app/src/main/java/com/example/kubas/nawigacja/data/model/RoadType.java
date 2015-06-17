package com.example.kubas.nawigacja.data.model;

import java.io.Serializable;

public class RoadType implements Serializable{
    int length = 3;
    int quality = 2;
    int security = 3;
    String name = "Optymalna";

    @Override
    public String toString() {
        return "{\"label\":\"" + name + "\",\"length\":" + length + ",\"quality\":" + quality + ",\"security\":" + security + "}";
    }

}
