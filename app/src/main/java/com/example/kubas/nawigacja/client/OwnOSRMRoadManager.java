package com.example.kubas.nawigacja.client;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;

public class OwnOSRMRoadManager extends OSRMRoadManager {
    public OwnOSRMRoadManager() {
        setService(ServerAddress.getServerUrl() + "webservices/viaroute?");
    }
}
