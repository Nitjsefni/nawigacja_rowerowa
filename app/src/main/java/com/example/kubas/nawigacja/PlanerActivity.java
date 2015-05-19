package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KubaS on 2015-04-27.
 */
public class PlanerActivity extends Activity {
   AutoCompleteTextView edTxt_Route_Beg, atcptv_Route_Via,atcptv_Route_End;
    ArrayList<GeoPosition> gps = new ArrayList<GeoPosition>();
    GeoPosition gp_od, gp_do, gp_przez;
    ImageButton imgBtn_Show_Via;
    TextView show_via;
    ImageView przez;
    Spinner tryb_drogi;
    Button btn_planuj;
    boolean czy_przez;
    public ArrayAdapter<GeoPosition> aAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planer);

        edTxt_Route_Beg = (AutoCompleteTextView) findViewById(R.id.atcptv_od);
        atcptv_Route_Via = (AutoCompleteTextView) findViewById(R.id.atcptv_przez);
        atcptv_Route_End = (AutoCompleteTextView) findViewById(R.id.atcptv_do);
        imgBtn_Show_Via = (ImageButton) findViewById(R.id.imgBtn_Show_Via);
        show_via = (TextView) findViewById(R.id.txtV_show_via);
        przez = (ImageView) findViewById(R.id.imageView5);
        tryb_drogi = (Spinner) findViewById(R.id.spinner3);
        btn_planuj = (Button) findViewById(R.id.btn_planuj);
        czy_przez = false;
        // Creating adapter for spinner
        String formyPl[] = {"Szybka", "Optymalna", "Bezpieczna", "Wygodna"};
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formyPl);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        // attaching data adapter to spinner
        tryb_drogi.setAdapter(dataAdapter);
        tryb_drogi.setSelection(dataAdapter.getPosition("Optymalna"));
        edTxt_Route_Beg.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
                // TODO Auto-generated method stub

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new getJson().execute(newText, "beg");
            }

        });
        atcptv_Route_Via.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
                // TODO Auto-generated method stub

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new getJson().execute(newText, "via");
            }

        });
        atcptv_Route_End.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
                // TODO Auto-generated method stub

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new getJson().execute(newText, "end");
            }

        });
        edTxt_Route_Beg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                GeoPosition selection = (GeoPosition) parent.getItemAtPosition(position);

                gp_od = selection;
                String place = gp_od.getGeoPositionName().split(",")[0] + ", " + gp_od.getGeoPositionName().split(",")[1];
                edTxt_Route_Beg.setText(place);

            }
        });

        atcptv_Route_Via.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                GeoPosition selection = (GeoPosition) parent.getItemAtPosition(position);

                gp_przez = selection;
                String place = gp_przez.getGeoPositionName().split(",")[0] + ", " + gp_przez.getGeoPositionName().split(",")[1];
                atcptv_Route_Via.setText(place);

            }
        });
        atcptv_Route_End.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                GeoPosition selection = (GeoPosition) parent.getItemAtPosition(position);

                gp_do = selection;
                String place = gp_do.getGeoPositionName().split(",")[0] + ", " + gp_do.getGeoPositionName().split(",")[1];
                atcptv_Route_End.setText(place);

            }
        });
        imgBtn_Show_Via.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
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
                if(gp_od.getGeoPositionLat().equals("") || gp_do.getGeoPositionLat().equals(""))
                {
                   Toast toast = Toast.makeText(PlanerActivity.this, "Uzupełnij punkt początkowy i docelowy", Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    Intent b = new Intent(getApplicationContext(), RouteActivity.class);




                    b.putExtra("gp_od_lat", gp_od.getGeoPositionLat());
                    b.putExtra("gp_od_lng", gp_od.getGeoPositionLng());

                    b.putExtra("gp_do_lat", gp_do.getGeoPositionLat());
                    b.putExtra("gp_do_lng", gp_do.getGeoPositionLng());

                    if(gp_przez == null)
                    {
                        b.putExtra("czy_przez", false);
                    }
                    else
                    {
                        b.putExtra("czy_przez", true);
                        b.putExtra("gp_przez_lat", gp_przez.getGeoPositionLat());
                        b.putExtra("gp_przez_lng", gp_przez.getGeoPositionLng());

                    }

                    //b.putExtra("zm_od_lokal", zm_od_lokal);
                   // b.putExtra("zm_od_miejscowosc", zm_od_miejscowosc);
                    //b.putExtra("zm_od_kodpocz", zm_od_kodpocz);
                   // b.putExtra("zm_mag", magazynySpinner.getSelectedItem().toString());


                    startActivity(b);
                }
            }

        });
    }
    public String data;
    //public List<GeoPosition> suggest;
    public AutoCompleteTextView autoComplete;
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
                    if(key[1].equals("beg")) {
                        edTxt_Route_Beg.setAdapter(aAdapter);
                    }
                    else if(key[1].equals("via")) {
                        atcptv_Route_Via.setAdapter(aAdapter);
                    }
                    else if(key[1].equals("end")) {
                        atcptv_Route_End.setAdapter(aAdapter);
                    }
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
            super(PlanerActivity.this, R.layout.listview_ac_position, gps);
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
