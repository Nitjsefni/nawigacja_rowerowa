package com.example.kubas.nawigacja.listeners;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.example.kubas.nawigacja.data.model.GeoPosition;
import com.example.kubas.nawigacja.data.model.RoutePoints;

public class AutoCompleteItemClickListener implements AdapterView.OnItemClickListener {
    private final AutoCompleteTextView textView;
    private final RoutePoints points;
    private final RoutePoints.PointType pointType;


    public AutoCompleteItemClickListener(AutoCompleteTextView textView, RoutePoints points, RoutePoints.PointType start) {

        this.textView = textView;
        this.points = points;
        this.pointType = start;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {

        GeoPosition geoPosition = (GeoPosition) parent.getItemAtPosition(position);
        setPoint(geoPosition);
        textView.setText(geoPosition.getName());
    }

    private void setPoint(GeoPosition position) {
        switch (pointType) {
            case START:
                points.setStartPoint(position);
                break;
            case MID:
                points.setMidPoint(position);
                break;
            case END:
                points.setEndPoint(position);
                break;
        }

    }

}
