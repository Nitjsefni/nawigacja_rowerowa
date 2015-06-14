package com.example.kubas.nawigacja;

import android.location.Location;

import com.example.kubas.nawigacja.data.model.travel.Travel;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.DateFormat;
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
                value = Long.toString(Math.round(travel.getLength()));
                break;
            case HEIGHT:
                value = Long.toString(Math.round(location.getAltitude()));
                break;
            case HOUR:
                Calendar calendar = Calendar.getInstance();
                value = formatDate(calendar.getTimeInMillis());
                break;
            case TIME:
                value = formatDate(travel.getDuration()-1000*60*60);
                break;

        }
        return value;
    }

    private String formatDate(long milis) {
        return DateFormatUtils.format(milis,"HH:mm:ss");
    }


}
