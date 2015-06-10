package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.listeners.AutoCompleteItemClickListener;
import com.example.kubas.nawigacja.listeners.AutoCompleteLocationListener;

public class PlanerActivity extends Activity {
    private RoutePoints points = new RoutePoints();
    private TextView show_via;
    private ImageView przez;
    private boolean czy_przez;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planer);

        AutoCompleteTextView edTxt_Route_Beg = (AutoCompleteTextView) findViewById(R.id.atcptv_od);
        final AutoCompleteTextView atcptv_Route_Via = (AutoCompleteTextView) findViewById(R.id.atcptv_przez);
        AutoCompleteTextView atcptv_Route_End = (AutoCompleteTextView) findViewById(R.id.atcptv_do);
        ImageButton imgBtn_Show_Via = (ImageButton) findViewById(R.id.imgBtn_Show_Via);
        show_via = (TextView) findViewById(R.id.txtV_show_via);
        przez = (ImageView) findViewById(R.id.imageView5);
        Spinner tryb_drogi = (Spinner) findViewById(R.id.spinner3);
        Button btn_planuj = (Button) findViewById(R.id.btn_planuj);
        czy_przez = false;
        // Creating adapter for spinner
        String formyPl[] = {"Szybka", "Optymalna", "Bezpieczna", "Wygodna"};
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, formyPl);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        // attaching data adapter to spinner
        tryb_drogi.setAdapter(dataAdapter);
        tryb_drogi.setSelection(dataAdapter.getPosition("Optymalna"));
        edTxt_Route_Beg.addTextChangedListener(new AutoCompleteLocationListener(this, edTxt_Route_Beg));
        atcptv_Route_Via.addTextChangedListener(new AutoCompleteLocationListener(this, atcptv_Route_Via));
        atcptv_Route_End.addTextChangedListener(new AutoCompleteLocationListener(this, atcptv_Route_End));
        edTxt_Route_Beg.setOnItemClickListener(new AutoCompleteItemClickListener(edTxt_Route_Beg, points, RoutePoints.PointType.START));
        atcptv_Route_Via.setOnItemClickListener(new AutoCompleteItemClickListener(atcptv_Route_Via, points, RoutePoints.PointType.MID));
        atcptv_Route_End.setOnItemClickListener(new AutoCompleteItemClickListener(atcptv_Route_End, points, RoutePoints.PointType.END));

        imgBtn_Show_Via.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (czy_przez) {
                    przez.setVisibility(View.GONE);
                    atcptv_Route_Via.setVisibility(View.GONE);
                    show_via.setText("dodaj punkt pośredni");
                } else {
                    przez.setVisibility(View.VISIBLE);
                    atcptv_Route_Via.setVisibility(View.VISIBLE);
                    show_via.setText("usuń punkt pośredni");

                }
            }
        });
        btn_planuj.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (points.getStartPoint() == null || points.getEndPoint() == null) {
                    Toast toast = Toast.makeText(PlanerActivity.this, "Uzupełnij punkt początkowy i docelowy", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
                    intent.putExtra("points", points);
                    startActivity(intent);
                }
            }

        });
    }

    public String data;
}
