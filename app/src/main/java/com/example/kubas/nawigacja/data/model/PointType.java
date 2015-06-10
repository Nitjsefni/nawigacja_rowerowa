package com.example.kubas.nawigacja.data.model;

import com.example.kubas.nawigacja.R;

public enum PointType {
    NORMAL(1, android.R.drawable.ic_menu_mylocation),
    NONE(0, android.R.drawable.ic_menu_myplaces);

    private int id;
    private int resourceId;

    PointType(int id, int resourceId) {
        this.id = id;
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public static PointType valueFromId(int id) {
        for (PointType pointType : values()) {
            if (pointType.getId() == id) {
                return pointType;
            }
        }
        return NORMAL;
    }

    public int getId() {
        return id;
    }
}
