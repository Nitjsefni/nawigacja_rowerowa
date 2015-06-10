package com.example.kubas.nawigacja.client;

import android.app.Activity;
import android.util.Log;

import com.example.kubas.nawigacja.data.model.Route;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class RouteListClient extends WebApiClient {

    public static final String SERVICE_URL = "webservices/getSavedRoutes";
    private final List<Route> routes;

    public RouteListClient(List<Route> routes) {
        this.routes = routes;
    }

    @Override
    protected NameValuePair[] prepareRequest(NameValuePair[] request) {
        return request;
    }

    protected void parseResponse(String result) throws IOException {
        try {
            JSONObject jsonResponse = new JSONObject(result);
            if (checkStatus(jsonResponse.getString("status"))) return;
            JSONArray items = jsonResponse.optJSONArray("items");
            if (items == null) {
                return;
            }
            for (int i = 0; i < items.length(); i++) {
                JSONObject route = items.getJSONObject(i);
                routes.add(new Route(route.getInt("id"), route.getString("name"), route.getString("description"), route.getDouble("length"), route.getDouble("time")));
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
        }
    }


    protected String getServiceUri() {
        return ServerAddress.getServerUrl() + SERVICE_URL;
    }

}
