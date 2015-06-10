package com.example.kubas.nawigacja.client;

import android.app.Activity;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import com.example.kubas.nawigacja.data.DataManager;
import com.example.kubas.nawigacja.data.model.PointType;
import com.example.kubas.nawigacja.list_adapters.LocationAdapter;
import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeoLocationClient extends WebApiClient {

    public static final String SERVICE_URL = "webservices/findPlace";
    private final Activity context;
    private final AutoCompleteTextView autoComplete;

    public GeoLocationClient(Activity context, AutoCompleteTextView autoComplete) {
        this.context = context;
        this.autoComplete = autoComplete;
    }

    @Override
    protected NameValuePair[] prepareRequest(NameValuePair[] request) {
        request[0] = new BasicNameValuePair(request[0].getName(), request[0].getValue().trim().replace(" ", "+"));
        return request;
    }

    protected void parseResponse(String result) throws IOException {
        final List<GeoPosition> gps = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(result);
            if (checkStatus(jsonResponse.getString("status"))) return;
            JSONArray locations = jsonResponse.getJSONArray("locations");
            if (locations == null) {
                return;
            }
            for (int i = 0; i < locations.length(); i++) {
                JSONObject address = locations.getJSONObject(i);
                JSONObject pos = address.getJSONObject("position");
                gps.add(new GeoPosition(address.getString("name"), pos.getDouble("lat"), pos.getDouble("lng"),
                        address.getString("address"), PointType.valueFromId(address.optInt("lng", 0))));
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
        }
        showResponse(gps);
    }

    protected void showResponse(final List<GeoPosition> gps) {
        context.runOnUiThread(new Runnable() {
            public void run() {
                LocationAdapter aAdapter = new LocationAdapter(context, R.layout.listview_ac_position, gps);
                autoComplete.setAdapter(aAdapter);
                aAdapter.notifyDataSetChanged();
            }
        });
    }

    protected String getServiceUri() {
        return ServerAddress.getServerUrl() + SERVICE_URL;
    }

}
