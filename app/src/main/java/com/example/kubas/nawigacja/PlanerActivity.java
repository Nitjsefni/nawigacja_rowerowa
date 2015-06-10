package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.listeners.AutoCompleteItemClickListener;
import com.example.kubas.nawigacja.listeners.AutocompleteLocationListener;

public class PlanerActivity extends Activity {
    private RoutePoints points = new RoutePoints();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planer);
        initRoadTypeSpinner();
        initAutocomplete((AutoCompleteTextView) findViewById(R.id.atcptv_od), RoutePoints.PointType.START);
        initAutocomplete((AutoCompleteTextView) findViewById(R.id.atcptv_przez), RoutePoints.PointType.MID);
        initAutocomplete((AutoCompleteTextView) findViewById(R.id.atcptv_do), RoutePoints.PointType.END);

        findViewById(R.id.imgBtn_Show_Via).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility;
                String text;
                if (findViewById(R.id.atcptv_przez).getVisibility() != View.GONE) {
                    visibility = View.GONE;
                    text = "dodaj punkt pośredni";
                } else {
                    visibility = View.VISIBLE;
                    text = "usuń punkt pośredni";
                }
                ((TextView) findViewById(R.id.txtV_show_via)).setText(text);
                findViewById(R.id.imageView5).setVisibility(visibility);
                findViewById(R.id.atcptv_przez).setVisibility(visibility);

            }
        });
        findViewById(R.id.btn_planuj).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (points.getStartPoint() == null || points.getEndPoint() == null) {
                    Toast.makeText(PlanerActivity.this, "Uzupełnij punkt początkowy i docelowy", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                startActivity(new Intent(getApplicationContext(), RouteActivity.class)
                        .putExtra("points", points));
            }

        });
    }

    private void initAutocomplete(AutoCompleteTextView autoCompleteTextView, RoutePoints.PointType type) {
        autoCompleteTextView.addTextChangedListener(new AutocompleteLocationListener(this, autoCompleteTextView));
        autoCompleteTextView.setOnItemClickListener(new AutoCompleteItemClickListener(autoCompleteTextView, points, type));
    }

    private void initRoadTypeSpinner() {
        // Creating adapter for spinner
        Spinner tryb_drogi = (Spinner) findViewById(R.id.spinner3);
        String formyPl[] = {"Szybka", "Optymalna", "Bezpieczna", "Wygodna"};
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, formyPl);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tryb_drogi.setAdapter(dataAdapter);
        tryb_drogi.setSelection(dataAdapter.getPosition("Optymalna"));
    }

}
