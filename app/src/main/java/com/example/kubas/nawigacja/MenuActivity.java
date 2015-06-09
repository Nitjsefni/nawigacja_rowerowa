package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.kubas.nawigacja.client.GeoLocationClient;
import com.example.kubas.nawigacja.model.GeoPosition;

import org.apache.http.message.BasicNameValuePair;

public class MenuActivity extends Activity {

    GeoPosition cel_trasy, poczatek_trasy;
    ImageButton img_Btn_Nawiguj;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        final AutoCompleteTextView selectTarget = (AutoCompleteTextView) findViewById(R.id.atcptv_wyznacz_do);
        final GeoLocationClient geoLocationClient = new GeoLocationClient(MenuActivity.this, selectTarget, "testowy", "rrr");
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
                if (cel_trasy.getGeoPositionLat().equals("")) {
                    Toast toast = Toast.makeText(MenuActivity.this, "Uzupełnij punkt docelowy", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Intent k = new Intent(MenuActivity.this, RouteActivity.class);
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    // Define the criteria how to select the locatioin provider -> use
                    // default
                    Criteria criteria = new Criteria();
                    //listener = new MyLocationListener();
//                    provider = locationManager.getBestProvider(criteria, false);
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, this);


                    //Location location = locationManager.getLastKnownLocation(provider);


                    //gp = new GeoPoint(location.getLatitude(), location.getLongitude());


                    k.putExtra("gp_od_lat", String.valueOf(0));
                    k.putExtra("gp_od_lng", String.valueOf(0));

                    k.putExtra("gp_do_lat", cel_trasy.getGeoPositionLat());
                    k.putExtra("gp_do_lng", cel_trasy.getGeoPositionLng());
                    startActivity(k);
                }
            }
        });
        selectTarget.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                geoLocationClient.execute(new BasicNameValuePair("name", s.toString()));
            }

        });
        selectTarget.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                cel_trasy = (GeoPosition) parent.getItemAtPosition(position);
                String place = cel_trasy.getGeoPositionName().split(",")[0] + ", " + cel_trasy.getGeoPositionName().split(",")[1];
                selectTarget.setText(place);
            }
        });
    }

    public void showRecomendRoute(View view) {
        findViewById(R.id.recomendedRoutesTabHeader).setBackground(findViewById(R.id.listView).getBackground());
        findViewById(R.id.savedRoutesTabHeader).setBackground(new ColorDrawable(Color.GRAY));
    }

    public void showSavedRoute(View view) {
        findViewById(R.id.savedRoutesTabHeader).setBackground(findViewById(R.id.listView).getBackground());
        findViewById(R.id.recomendedRoutesTabHeader).setBackground(new ColorDrawable(Color.GRAY));}
}
