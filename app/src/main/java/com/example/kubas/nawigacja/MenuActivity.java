package com.example.kubas.nawigacja;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Color;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kubas.nawigacja.data.DataManager;
import com.example.kubas.nawigacja.data.interfaces.Listener;
import com.example.kubas.nawigacja.data.interfaces.Notificator;
import com.example.kubas.nawigacja.data.model.RoutePoints;
import com.example.kubas.nawigacja.list_adapters.LocationAdapter;
import com.example.kubas.nawigacja.listeners.AutoCompleteItemClickListener;
import com.example.kubas.nawigacja.listeners.AutocompleteLocationListener;

public class MenuActivity extends Activity implements Listener {

    private RoutePoints points = new RoutePoints();
    private DataManager dataManager = DataManager.getInstance();
    private String actualList = "saved";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        findViewById(R.id.button4).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent k = new Intent(MenuActivity.this, MapActivity.class);
                startActivity(k);
            }
        });
        findViewById(R.id.button3).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent k = new Intent(MenuActivity.this, PlanerActivity.class);
                startActivity(k);
            }
        });
        findViewById(R.id.img_Btn_Nawiguj).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (points.getEndPoint() == null) {
                    Toast.makeText(MenuActivity.this, "Uzupełnij punkt docelowy", Toast.LENGTH_LONG)
                            .show();
                } else {
                    startActivity(new Intent(MenuActivity.this, RouteActivity.class).putExtra("points", points));
                }
            }
        });
        initAutocomplete();
        reloadListView();
        dataManager.registerRouteListListener(this);
    }

    private void initAutocomplete() {
        AutoCompleteTextView selectTarget = (AutoCompleteTextView) findViewById(R.id.atcptv_wyznacz_do);
        selectTarget.addTextChangedListener(new AutocompleteLocationListener(this, selectTarget));
        selectTarget.setOnItemClickListener(new AutoCompleteItemClickListener(selectTarget, points, RoutePoints.PointType.END));
        showSavedRoute(null);

    }

    public void showRecomendRoute(View view) {
        dataManager.loadRecomendedRoutes();
        actualList = "recommend";
        reloadListView();
    }


    public void showSavedRoute(View view) {
        dataManager.loadSavedRoutes();
        actualList = "saved";
        reloadListView();
    }

    private void reloadListView() {
        if (actualList == "saved") {
            findViewById(R.id.savedRoutesTabHeader).setBackgroundColor(Color.TRANSPARENT);
            findViewById(R.id.recomendedRoutesTabHeader).setBackgroundColor(Color.GRAY);
            ((ListView) findViewById(R.id.routeList)).setAdapter(dataManager.getSavedRouteAdapter(this));
        } else {
            findViewById(R.id.recomendedRoutesTabHeader).setBackgroundColor(Color.TRANSPARENT);
            findViewById(R.id.savedRoutesTabHeader).setBackgroundColor(Color.GRAY);
            ((ListView) findViewById(R.id.routeList)).setAdapter(dataManager.getRecomendedRouteAdapter(this));
        }
    }

    @Override
    public void notify(Notificator notificator) {
        runOnUiThread(new Runnable() {
            public void run() {
                reloadListView();
            }
        });
    }
}