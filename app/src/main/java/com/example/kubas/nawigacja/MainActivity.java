package com.example.kubas.nawigacja;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.kubas.nawigacja.data.DataManager;
import com.example.kubas.nawigacja.gps.GPSManager;


public class MainActivity extends Activity {
    private String user = "testowy";
    private String password = "rrr";
    private GPSManager gpsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            GPSManager.init((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Nie udało się uruchomić GPS", Toast.LENGTH_LONG).show();
        }
        new LoadingTask().execute();

    }
    private class LoadingTask extends AsyncTask<String, Void, String> {
        private ProgressDialog myProgressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myProgressDialog = new ProgressDialog(MainActivity.this);
            myProgressDialog.setTitle("Proszę czekać");
            myProgressDialog.setMessage("Ładowanie aplikacji...");
            myProgressDialog.setCancelable(false);

            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            if (DataManager.login(user,password)){
                DataManager manager = DataManager.getInstance();
                manager.loadRecomendedRoutes();
                manager.loadSavedRoutes();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            myProgressDialog.hide();
            myProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Zakończono ładowanie aplikacji", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, MenuActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
