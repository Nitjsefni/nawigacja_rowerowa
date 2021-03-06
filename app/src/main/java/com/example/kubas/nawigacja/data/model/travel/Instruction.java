package com.example.kubas.nawigacja.data.model.travel;

import com.example.kubas.nawigacja.routing.RoutingUtil;

public enum Instruction {
    BEGIN(500, Integer.MAX_VALUE, "Za ", true),
    FAR(300, 500, "Za ", true),
    MID(30, 300, "Za ", true),
    SHORT(0, 30, "", false);

    private final int from;
    private final int to;
    private final String prefix;
    private final boolean showWithDistance;

    Instruction(int from, int to, String prefix, boolean showWithDistance) {
        this.from = from;
        this.to = to;
        this.prefix = prefix;
        this.showWithDistance = showWithDistance;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String textOfInstruction(double distance, String instruction) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        if (showWithDistance) {
            sb.append(RoutingUtil.getDistanceWithFullNames(distance));
        }
        sb.append(instruction);
        return sb.toString();
    }
}
