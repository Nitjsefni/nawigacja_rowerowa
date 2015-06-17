package com.example.kubas.nawigacja.client;

import com.example.kubas.nawigacja.data.model.RoadType;
import com.example.kubas.nawigacja.exceptions.RoadException;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class OwnOSRMRoadManager extends OSRMRoadManager {
    public OwnOSRMRoadManager(RoadType params) {
        String encode;
        try {
            encode = "params=" + URLEncoder.encode(params.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encode = "";
            e.printStackTrace();
        }
        setService(ServerAddress.getInstance().getServerUrl() + "webservices/viaroute?" + encode);
    }

    @Override
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        Road road = super.getRoad(waypoints);
        if (road.mStatus == Road.STATUS_TECHNICAL_ISSUE) {
            throw new RoadException("Nie udało się wyznaczyć trasy. Spróbuj ponownie");
        }
        return road;
    }
}
