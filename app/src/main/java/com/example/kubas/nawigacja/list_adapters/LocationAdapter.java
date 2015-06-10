package com.example.kubas.nawigacja.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.R;

import java.util.List;

public class LocationAdapter extends ArrayAdapter<GeoPosition> implements Filterable {

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

        TextView name = (TextView) view.findViewById(R.id.pointName);
        name.setText(currentClient.getName());

        TextView address = (TextView) view.findViewById(R.id.pointAddress);
        address.setText(currentClient.getAddress());

        ImageView gp_lng = (ImageView) view.findViewById(R.id.pointType);
        gp_lng.setImageResource(currentClient.getType().getResourceId());


        return view;
    }


}