package com.example.kubas.nawigacja.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.kubas.nawigacja.R;
import com.example.kubas.nawigacja.data.model.Route;

import java.util.List;

public class RouteListAdapter extends ArrayAdapter<Route> implements Filterable {

    public RouteListAdapter(Context context, int resource, List<Route> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            view = vi.inflate(R.layout.route_list_row, parent, false);
        }

        final Route route = getItem(position);

        TextView name = (TextView) view.findViewById(R.id.pointName);
        name.setText(route.getName());

        TextView description = (TextView) view.findViewById(R.id.description);
        description.setText(route.getDescription());

        TextView time = (TextView) view.findViewById(R.id.time);
        time.setText(route.getFormattedTime());

        TextView length = (TextView) view.findViewById(R.id.length);
        length.setText(route.getFormattedLength());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("test", "" + route.getId());
            }
        });

        return view;
    }


}