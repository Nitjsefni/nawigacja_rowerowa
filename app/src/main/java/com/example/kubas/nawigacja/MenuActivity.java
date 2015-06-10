package com.example.kubas.nawigacja;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Color;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kubas.nawigacja.data.DataManager;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.listeners.AutoCompleteItemClickListener;
import com.example.kubas.nawigacja.listeners.AutocompleteLocationListener;

public class MenuActivity extends Activity {

    private RoutePoints points = new RoutePoints();
    private ImageButton img_Btn_Nawiguj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        final AutoCompleteTextView selectTarget = (AutoCompleteTextView) findViewById(R.id.atcptv_wyznacz_do);
        img_Btn_Nawiguj = (ImageButton) findViewById(R.id.img_Btn_Nawiguj);
        Button btnMap = (Button) findViewById(R.id.button4);
        btnMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent k = new Intent(MenuActivity.this, MapActivity.class);

                startActivity(k);
            }
        });

        Button btnTrasa = (Button) findViewById(R.id.button3);
        btnTrasa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent k = new Intent(MenuActivity.this, PlanerActivity.class);

                startActivity(k);
            }
        });
        img_Btn_Nawiguj.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (points.getEndPoint() == null) {
                    Toast toast = Toast.makeText(MenuActivity.this, "Uzupełnij punkt docelowy", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Intent k = new Intent(MenuActivity.this, RouteActivity.class);
                    getMyLocation();
                    k.putExtra("points", points);
                    startActivity(k);
                }
            }
        });
        selectTarget.addTextChangedListener(new AutocompleteLocationListener(this,selectTarget));
        selectTarget.setOnItemClickListener(new AutoCompleteItemClickListener(selectTarget, points, RoutePoints.PointType.END));
        showSavedRoute(null);
    }

    private void getMyLocation() {
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//listener = new MyLocationListener();
//                    provider = locationManager.getBestProvider(criteria, false);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, this);


        //Location location = locationManager.getLastKnownLocation(provider);


        //gp = new GeoPoint(location.getLatitude(), location.getLongitude());
    }

    public void showRecomendRoute(View view) {
        ListView routeList = (ListView) findViewById(R.id.routeList);
        findViewById(R.id.recomendedRoutesTabHeader).setBackgroundColor(Color.WHITE);
        findViewById(R.id.savedRoutesTabHeader).setBackgroundColor(Color.GRAY);
        routeList.setAdapter(DataManager.getInstance().getRecomendedRouteAdapter(this));
    }

    public void showSavedRoute(View view) {
        ListView routeList = (ListView) findViewById(R.id.routeList);
        findViewById(R.id.savedRoutesTabHeader).setBackgroundColor(Color.WHITE);
        findViewById(R.id.recomendedRoutesTabHeader).setBackgroundColor(Color.GRAY);
        routeList.setAdapter(DataManager.getInstance().getSavedRouteAdapter(this));

    }
}