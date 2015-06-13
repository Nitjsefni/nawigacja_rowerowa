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

    protected void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public GeoPoint getLocation() {
        if (roadNode == null) {
            //TODO - do poprawy
            return new GeoPoint(0, 0);
        }
        return roadNode.mLocation;
    }

    public String getInstruction() {
        if (roadNode == null) {
            return "Jesteś u celu";
        }
        return roadNode.mInstructions;
    }

    public int getManeuverType() {
        if (roadNode == null) {
            return 0;
        }
        return roadNode.mManeuverType;
    }

    public List<Instruction> getInstructionsCondition() {
        return instructions;
    }

    public String getInstructionText(Location loc) {

        if (roadNode == null) {
            return "Dojechales do celu";
        }
        float distance = getLocation().distanceTo(new GeoPoint(loc));
        Instruction instruction = getInstruction(distance);
        if (instruction != null) {
            getInstructionsCondition().remove(instruction);
            return instruction.textOfInstruction(distance, getInstruction());
        }
        return null;
    }

    private Instruction getInstruction(float distance) {
        for (Instruction instruction : getInstructionsCondition()) {
            if (instruction.getFrom() < distance && instruction.getTo() > distance) {

                return instruction;
            }
        }
        return null;
    }

    public boolean isSameRoadNode(RoadNode roadNode) {
        return roadNode == this.roadNode;
    }
}
