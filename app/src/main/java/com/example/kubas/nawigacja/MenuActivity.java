package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by KubaS on 2015-04-26.
 */
public class MenuActivity extends Activity{

    AutoCompleteTextView atcptv_wyznacz_do;
    GeoPosition cel_trasy, poczatek_trasy;
    GeoPoint gp;
    ArrayList<GeoPosition> gps = new ArrayList<GeoPosition>();
    ImageButton img_Btn_Nawiguj;
    public ArrayAdapter<GeoPosition> aAdapter;
    private LocationManager locationManager;
    private String provider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        atcptv_wyznacz_do = (AutoCompleteTextView) findViewById(R.id.atcptv_wyznacz_do);
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
                if(cel_trasy.getGeoPositionLat().equals(""))
                {
                    Toast toast = Toast.makeText(MenuActivity.this, "Uzupełnij punkt docelowy", Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    Intent k = new Intent(MenuActivity.this, RouteActivity.class);
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    // Define the criteria how to select the locatioin provider -> use
                    // default
                    Criteria criteria = new Criteria();
                    //listener = new MyLocationListener();
                    provider = locationManager.getBestProvider(criteria, false);
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, this);


                    Location location = locationManager.getLastKnownLocation(provider);


                    gp = new GeoPoint(location.getLatitude(), location.getLongitude());


                        k.putExtra("gp_od_lat", String.valueOf(gp.getLatitude()));
                        k.putExtra("gp_od_lng", String.valueOf(gp.getLongitude()));

                    k.putExtra("gp_do_lat", cel_trasy.getGeoPositionLat());
                    k.putExtra("gp_do_lng", cel_trasy.getGeoPositionLng());
                    startActivity(k);
                }
            }
        });
        atcptv_wyznacz_do.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
                // TODO Auto-generated method stub

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new getJson().execute(newText);
            }

        });
        atcptv_wyznacz_do.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                GeoPosition selection = (GeoPosition) parent.getItemAtPosition(position);

                cel_trasy = selection;
                String place = cel_trasy.getGeoPositionName().split(",")[0] + ", " + cel_trasy.getGeoPositionName().split(",")[1];
                atcptv_wyznacz_do.setText(place);

            }
        });


    }
    class getJson extends AsyncTask<String,String,String> {
        InputStream inputStream = null;
        String result = "";
        GeoPosition pos=null;
        @Override
        protected String doInBackground(final String... key) {
            String newText = key[0];
            newText = newText.trim();
            newText = newText.replace(" ", "+");
            try {
                HttpClient hClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://beta.wskocznarower.pl/app_dev.php/webservices/findPlace");
                ResponseHandler<String> rHandler = new BasicResponseHandler();
                ArrayList<NameValuePair> nameValuePairs1 = new ArrayList<NameValuePair>();

                nameValuePairs1.add(new BasicNameValuePair("p", "1234"));
                nameValuePairs1.add(new BasicNameValuePair("ver", "1.0"));
                nameValuePairs1.add(new BasicNameValuePair("username", "testowy"));
                nameValuePairs1.add(new BasicNameValuePair("password", "rrr"));
                nameValuePairs1.add(new BasicNameValuePair("name", "" + key[0]));
                // 6. set httpPost Entity
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs1));
                HttpResponse httpResponse = hClient.execute(httpPost);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();
                gps.clear();
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        JSONObject jsonResponse = new JSONObject(result);
                        JSONArray array = jsonResponse.getJSONArray("data");
                        //JSONObject data = jsonResponse.getJSONObject("data");
                        //JSONObject position = data.getJSONObject("position");
                        String status = jsonResponse.getString("status");
                        if(array.length() > 0)
                            for(int i=0;i<array.length();i++){
                                JSONObject points = array.getJSONObject(i);
                                // JSONArray arr = points.getJSONArray("position");
                                String name = points.getString("name");


                                JSONObject pos = points.getJSONObject("position");
                                //String position = pos.getString("lat");
                                String lat= pos.getString("lat");

                                String lng=pos.getString("lng");

                                //JSONObject pos_lng = arr.getJSONObject(1);
                                //String lng = points.getString("lng");
                                gps.add(new GeoPosition(name, lat, lng));
                            }



                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //JSONArray jArray = new JSONArray(data);


            }catch(Exception e){
                //Log.w("Error", e.getMessage());
            }
            runOnUiThread(new Runnable(){
                public void run(){
                    aAdapter = new LocationAdapter();
                        atcptv_wyznacz_do.setAdapter(aAdapter);

                    aAdapter.notifyDataSetChanged();
                }
            });

            return null;
        }

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();

        return result;

    }
    private class LocationAdapter extends ArrayAdapter<GeoPosition> implements Filterable {

        protected static final String TAG = "SuggestionAdapter";
        private List<GeoPosition> suggestions;
        public LocationAdapter() {
            super(MenuActivity.this, R.layout.listview_ac_position, gps);
            //suggestions = gps;
        }

        @Override
        public int getCount() {
            return gps.size();
        }

        @Override
        public GeoPosition getItem(int index) {

            return gps.get(index);

            //return gps.get(index);
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                LayoutInflater vi = LayoutInflater.from(getContext());
                view = vi.inflate(R.layout.listview_ac_position, parent, false);
            }


            GeoPosition currentClient = gps.get(position);

            TextView gp_name = (TextView) view.findViewById(R.id.txtV_GP_Name);
            gp_name.setText(currentClient.getGeoPositionName());

            TextView gp_lat = (TextView) view.findViewById(R.id.txtV_GP_Lat);
            gp_lat.setText(currentClient.getGeoPositionLat());

            TextView gp_lng = (TextView) view.findViewById(R.id.txtV_GP_Lng);
            gp_lng.setText(currentClient.getGeoPositionLng());



            return view;
        }



    }
}
