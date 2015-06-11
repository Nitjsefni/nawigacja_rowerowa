package com.example.kubas.nawigacja.routing;

public class RoutingUtil {

    public static String getLengthText(double meters) {
        String result;
        meters/=1000;
        if (meters >= 10.0) {
            result = (int) (meters) + " km";
        } else if (meters >= 1.0) {
            result = Math.round(meters * 10) / 10.0 + " km";
        } else {
            result = (int) (meters * 1000) + " m";
        }

        return result;
    }

    public static String getLengthTextToSpeech(double meters) {
        meters/=1000;
        String result;
        if (meters >= 100.0) {
            result = Math.round(meters) + " kilometrów";
        } else if (meters >= 1.0) {
            result = Math.round(meters * 10)  + " kilometrów";
        } else {
            result = (Math.round(meters * 100)) * 10 + " metrów";
        }

        return result;
    }

    public static String getDurationText(double duration) {
        String result = "";

        int totalSeconds = (int) Math.round(duration);
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
