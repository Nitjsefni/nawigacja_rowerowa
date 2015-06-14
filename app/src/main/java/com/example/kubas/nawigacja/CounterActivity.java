package com.example.kubas.nawigacja;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.kubas.nawigacja.data.DataManager;
import com.example.kubas.nawigacja.data.model.travel.Travel;
import com.example.kubas.nawigacja.gps.GPSManager;

import java.util.Calendar;

public class CounterActivity extends Activity {
    private RefreshTask refreshTask;
    private boolean czy_z_menu = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_counter);
        refreshTask = new RefreshTask(DataManager.getInstance().getTravel());
        ImageButton goToMap = (ImageButton) findViewById(R.id.imgBtn_GoMap);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            czy_z_menu = extras.getBoolean("czy_z_menu",false);

        }
        goToMap.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(czy_z_menu) {
                    Intent k = new Intent(CounterActivity.this, MapActivity.class);
                    //k.putExtra("resume", true);
                    startActivity(k);
                }
                else {
                    onBackPressed();
                }
            }
        });
    }

    public void viewMap(View view) {
        startActivity(new Intent(CounterActivity.this, RouteActivity.class).putExtra("resume", true));
    }

    @Override
    protected void onPause() {
        stopRefreshTask();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopRefreshTask();
        super.onDestroy();
    }

    public void stopRefreshTask() {
        refreshTask.stop();
    }

    public void showSpeed(View view) {
        refreshTask.setMode(CounterMode.SPEED);
    }

    public void showHour(View view) {
        refreshTask.setMode(CounterMode.HOUR);
    }

    public void showAvgSpeed(View view) {
        refreshTask.setMode(CounterMode.AVG_SPEED);
    }

    public void showTime(View view) {
        refreshTask.setMode(CounterMode.TIME);
    }

    public void showDistance(View view) {
        refreshTask.setMode(CounterMode.DISTANCE);
    }

    public void showHight(View view) {
        refreshTask.setMode(CounterMode.HEIGHT);
    }

    public class RefreshTask implements Runnable {
        private Travel travel;
        private Handler handler;
        private boolean isActive = true;
        private CounterMode mode = CounterMode.SPEED;

        public RefreshTask(Travel travel) {
            this.travel = travel;
            handler = new Handler();
            handler.postDelayed(this, 0);
        }

        @Override
        public void run() {
            refreshValues();
            refreshMainView();
            if (isActive) {
                handler.postDelayed(this, 1000);
            }
        }

        private void refreshMainView() {
            Location location = GPSManager.getInstance().getActualLocation();
            setText(findViewById(R.id.hugeValue), mode.getValue(location, travel));
        }


        private void refreshValues() {
            Location location = GPSManager.getInstance().getActualLocation();
            setText(findViewById(R.id.speed), location.getSpeed() * 3.6);
            setText(findViewById(R.id.hour), Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE));
            setText(findViewById(R.id.averageSpeed), travel.getAverageSpeed());
            setText(findViewById(R.id.traVelLength), String.valueOf(Math.round(travel.getLength() / 100) / 10));
            setText(findViewById(R.id.travelTime), travel.getDuration() / 1000 / 60);
            setText(findViewById(R.id.height), location.getAltitude());
        }

        private void setText(View view, double value) {
            setText(view, String.valueOf(Math.round(value)));
        }

        private void setText(View view, String text) {
            TextView textView = (TextView) view;
            textView.setText(text);
        }

        public void stop() {
            if (isActive) {
                handler.removeCallbacks(this);
                isActive = false;
            }
        }

        public void setMode(CounterMode mode) {
            this.mode = mode;
            setText(findViewById(R.id.hugeTitle), mode.getTitle());
            setText(findViewById(R.id.hugeJed), mode.getJednostka());
            setText(findViewById(R.id.hugeValue), mode.getValue(GPSManager.getInstance().getActualLocation(), travel));
        }
    }
}