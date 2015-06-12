package com.example.kubas.nawigacja.routing;

public class RoutingUtil {

    public static String getFormattedDistance(double meters) {
        return getLength(meters / 1000, "km", "m", "m");
    }

    public static String getDistanceWithFullNames(double meters) {
        return getLength(meters / 1000, "kilometrów", "metrów", "metry");
    }

    public static String getFormattedTime(double seconds) {
        double hours = seconds / 3600.0;
        StringBuilder sb = new StringBuilder();
        if (hours >= 1) {
            double floor = Math.floor(hours);
            sb.append(Math.round(floor));
            sb.append("h ");
            hours -= floor;
        }
        hours *= 60;
        if (hours >= 1) {
            sb.append(Math.round(hours));
            sb.append(" min");
        }
        return sb.toString();
    }


    private static String getLength(double kilometers, String km, String m, String m2) {
        if (kilometers >= 20.0) {
            return Math.round(kilometers) + " " + km;
        } else if (kilometers >= 1.0) {
            return Math.round(kilometers * 10) / 10.0 + " " + km;
        } else if (kilometers >= 0.1) {
            return (Math.round(kilometers * 100)) * 10 + " " + m;
        } else if (kilometers >= 0.004) {
            return (Math.round(kilometers * 1000)) + " " + m;
        } else {
            return (Math.round(kilometers * 1000)) + " " + m2;
        }
    }
}
