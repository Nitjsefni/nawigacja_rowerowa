package com.example.kubas.nawigacja;

import android.location.Location;

import com.example.kubas.nawigacja.data.model.travel.Travel;

import java.util.Calendar;

public enum CounterMode {
    HOUR("Godzina", ""),
    AVG_SPEED("Średnia prędkość", "km/h"),
    TIME("Czas podróży", ""),
    DISTANCE("Całkowity dystans", "m"),
    HEIGHT("Wysokość NPM", "m"),
    SPEED("Prędkość", "km/h");

    private String title;
    private String jednostka;

    CounterMode(String title, String jednostka) {
        this.title = title;
        this.jednostka = jednostka;
    }

    public String getTitle() {
        return title;
    }

    public String getJednostka() {
        return jednostka;
    }

    public String getValue(Location location, Travel travel) {
        String value = "";
        switch (this) {
            case SPEED:
                value = Double.toString(Math.round(location.getSpeed() * 3.6 * 100) / 100);
                break;
            case AVG_SPEED:
                value = Double.toString(Math.round(travel.getAverageSpeed() * 100) / 100);
                break;
            case DISTANCE:
                value = Long.toString(Math.round(travel.getLength() * 1000));
                break;
            case HEIGHT:
                value = Long.toString(Math.round(location.getAltitude()));
                break;
            case HOUR:
                Calendar calendar = Calendar.getInstance();
                value = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                break;
            case TIME:
                value = Long.toString(travel.getDuration()) + " min " + Long.toString((travel.getDuration() * 60) % 60) + "s";
                break;

        }
        return value;
    }

}
