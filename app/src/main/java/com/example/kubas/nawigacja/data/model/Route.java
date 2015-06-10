package com.example.kubas.nawigacja.data.model;

public class Route {
    private final int id;
    private final String name;
    private final String description;
    private final double length;
    private final double time;

    public Route(int id, String name, String description, double length, double time) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.length = length;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getLength() {
        return length;
    }

    /**
     * @return times in seconds
     */
    public double getTime() {
        return time;
    }

    public String getFormattedLength() {
        StringBuilder sb = new StringBuilder();
        double length = this.length;
        if (length >= 1) {
            double floor = Math.floor(length);
            sb.append(Math.round(floor));
            sb.append(" km ");
            length -= floor;
        }
        length *= 1000;
        if (length >= 1) {
            sb.append(Math.round(length));
            sb.append("m");
        }
        return sb.toString();
    }

    public String getFormattedTime() {
        StringBuilder sb = new StringBuilder();
        double time = this.time / 3600;
        if (time >= 1) {
            double floor = Math.floor(time);
            sb.append(Math.round(floor));
            sb.append("h ");
            time -= floor;
        }
        time *= 60;
        if (time >= 1) {
            sb.append(Math.round(time));
            sb.append(" min");
        }
        return sb.toString();
    }
}
