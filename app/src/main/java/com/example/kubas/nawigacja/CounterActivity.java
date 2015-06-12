package com.example.kubas.nawigacja;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.example.kubas.nawigacja.data.DataManager;
import com.example.kubas.nawigacja.data.model.travel.Travel;
import com.example.kubas.nawigacja.gps.GPSManager;

import java.util.Calendar;

public class CounterActivity extends Activity {
    private RefreshTask refreshTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_counter);
        refreshTask = new RefreshTask(DataManager.getInstance().getTravel());
        refreshTask.refreshValues();
    }

    public void viewMap(View view) {
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

    private void stopRefreshTask() {
        refreshTask.stop();
    }

    private class RefreshTask implements Runnable {
        private Travel travel;
        private Handler handler;
        private boolean isActive = true;

        public RefreshTask(Travel travel) {
            this.travel = travel;
            handler = new Handler();
            handler.postDelayed(this, 1500);
        }

        @Override
        public void run() {
            refreshValues();
            if (isActive) {
                handler.postDelayed(this, 900);
            }
        }

        private void refreshValues() {
            Location location = GPSManager.getInstance().getActualLocation();
            setText(findViewById(R.id.speed), location.getSpeed()*3.6);
            setText(findViewById(R.id.hour), Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE));
            setText(findViewById(R.id.averageSpeed), travel.getAverageSpeed());
            setText(findViewById(R.id.traVelLength), String.valueOf(Math.round(travel.getLength() * 100) / 100));
            setText(findViewById(R.id.travelTime), travel.getTotalRoadDuration());
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
    }
}