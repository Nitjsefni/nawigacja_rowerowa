package com.example.kubas.nawigacja.routing;

public class RoutingUtil {

    public static String getLengthText(double length) {
        String result;
        if (length >= 10.0) {
            result = (int) (length) + " km";
        } else if (length >= 1.0) {
            result = Math.round(length * 10) / 10.0 + " km";
        } else {
            result = (int) (length * 1000) + " m";
        }

        return result;
    }

    public static String getLengthTextToSpeech(double length) {
        String result;
        if (length >= 100.0) {
            result = Math.round(length) + " kilometrów";
        } else if (length >= 1.0) {
            result = Math.round(length * 10) / 10.0 + " kilometrów";
        } else {
            result = (Math.round(length * 100)) * 10 + " metrów";
        }

        return result;
    }

    public static String getDurationText(double duration) {
        String result = "";

        int totalSeconds = (int) duration;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds / 60) - (hours * 60);
        int seconds = (totalSeconds / 60);
        if (hours != 0) {
            result += hours + " h";
        }
        if (minutes != 0) {
            result += minutes + " min";
        }
        if (hours == 0 && minutes == 0) {
            result += seconds + " s";
        }
        return result;
    }

}
