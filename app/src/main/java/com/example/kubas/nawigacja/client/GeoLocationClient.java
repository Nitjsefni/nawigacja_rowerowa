package com.example.kubas.nawigacja.client;

import android.app.Activity;
import android.widget.AutoCompleteTextView;

import com.example.kubas.nawigacja.model.GeoPosition;
import com.example.kubas.nawigacja.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeoLocationClient extends WebApiClient {

    private final Activity context;
    private final AutoCompleteTextView autoComplete;

    public GeoLocationClient(Activity context, AutoCompleteTextView autoComplete, String user, String password) {
        super(user, password);
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
            String status = jsonResponse.getString("status");
            if (jsonResponse.length() > 0)
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject array = jsonResponse.getJSONObject(String.valueOf(i));
                    String name = array.getString("name");
                    JSONObject pos = array.getJSONObject("position");
                    String lat = pos.getString("lat");
                    String lng = pos.getString("lng");
                    gps.add(new GeoPosition(name, lat, lng));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.runOnUiThread(new Runnable() {
            public void run() {
                LocationAdapter aAdapter = new LocationAdapter(context, R.layout.listview_ac_position, gps);
                autoComplete.setAdapter(aAdapter);

                aAdapter.notifyDataSetChanged();
            }
        });
    }

    protected String getServiceUri() {
        return "http://beta.wskocznarower.pl/app_dev.php/webservices/findPlace";
    }

}
