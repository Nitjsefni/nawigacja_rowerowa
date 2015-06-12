package com.example.kubas.nawigacja.data.model.travel;

import android.location.Location;

import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoadNodeToSpeak {
    private RoadNode roadNode;
    private List<Instruction> instructions;

    public RoadNodeToSpeak(RoadNode roadNode) {
        this.roadNode = roadNode;
        instructions = new ArrayList<>(Arrays.asList(Instruction.values()));
    }


    public GeoPoint getLocation() {
        return roadNode.mLocation;
    }

    public String getInstruction() {
        return roadNode.mInstructions;
    }

    public int getManeuverType() {
        return roadNode.mManeuverType;
    }

    public List<Instruction> getInstructionsCondition() {
        return instructions;
    }

    public String getInstructionText(Location loc) {

        if (roadNode == null) {
            return "Dojechales do celu podró¿y";
        }
        float distance = getLocation().distanceTo(new GeoPoint(loc));
        for (Instruction instruction : getInstructionsCondition()) {
            if (instruction.getFrom() < distance && instruction.getTo() > distance) {
                return instruction.textOfInstruction(distance, getInstruction());
            }
        }

        return null;
    }

    public boolean isSameRoadNode(RoadNode roadNode) {
        return roadNode == this.roadNode;
    }
}
