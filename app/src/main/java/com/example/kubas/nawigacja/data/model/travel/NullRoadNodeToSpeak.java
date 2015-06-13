package com.example.kubas.nawigacja.data.model.travel;

import java.util.ArrayList;

public class NullRoadNodeToSpeak extends RoadNodeToSpeak {
    public NullRoadNodeToSpeak() {
        super(null);
        setInstructions(new ArrayList<Instruction>());
    }

    public String getInstruction() {
        return "";
    }
}
