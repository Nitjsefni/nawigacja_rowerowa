package com.example.kubas.nawigacja.client;

import android.app.Activity;

import com.example.kubas.nawigacja.model.GeoPosition;
import com.example.kubas.nawigacja.model.Route;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteListClient extends WebApiClient {

    public static final String SERVICE_URL = "webservices/getSavedRoutes";
    private final Activity context;
    private final List<Route> routes;

    public RouteListClient(Activity context, List<Route> routes, String user, String password) {
        super(user, password);
        this.context = context;
        this.routes = routes;
    }

    @Override
    protected NameValuePair[] prepareRequest(NameValuePair[] request) {
        return request;
    }

    protected void parseResponse(String result) throws IOException {
        final ArrayList<GeoPosition> gps = new ArrayList<GeoPosition>();
        try {
//            JSONObject jsonResponse = new JSONObject(result);
//            String status = jsonResponse.getString("status");
//            if (jsonResponse.length() > 0)
//                for (int i = 0; i < jsonResponse.length(); i++) {
//                    JSONObject array = jsonResponse.getJSONObject(String.valueOf(i));
//                    String name = array.getString("name");
//                    JSONObject pos = array.getJSONObject("position");
//                    String lat = pos.getString("lat");
//                    String lng = pos.getString("lng");
//                    gps.add(new GeoPosition(name, lat, lng));
//                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getServiceUri() {
        return getServerUrl() + SERVICE_URL;
    }

}
