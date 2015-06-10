package com.example.kubas.nawigacja.client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.kubas.nawigacja.model.GeoPosition;
import com.example.kubas.nawigacja.R;

import java.util.List;

/**
 * Created by Krzysztof_Pawlak on 2015-06-09.
 */
public class LocationAdapter extends ArrayAdapter<GeoPosition> implements Filterable {

    protected static final String TAG = "SuggestionAdapter";
    private List<GeoPosition> suggestions;

    public LocationAdapter(Context context, int resource, List<GeoPosition> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            view = vi.inflate(R.layout.listview_ac_position, parent, false);
        }

        GeoPosition currentClient = getItem(position);

        TextView gp_name = (TextView) view.findViewById(R.id.txtV_GP_Name);
        gp_name.setText(currentClient.getGeoPositionName());

        TextView gp_lat = (TextView) view.findViewById(R.id.txtV_GP_Lat);
        gp_lat.setText(currentClient.getGeoPositionLat());

        TextView gp_lng = (TextView) view.findViewById(R.id.txtV_GP_Lng);
        gp_lng.setText(currentClient.getGeoPositionLng());


        return view;
    }


}