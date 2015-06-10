package com.example.kubas.nawigacja;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;

/**
 * Created by KubaS on 2015-06-06.
 */
public class GPSManager {
    private MapActivity activity;
    private RouteActivity activity2;
    public LocationManager locationManager;
    public LocationListener locationListener;

    public GPSManager(MapActivity activity, int delayTimeInMilisecounds) {
    this.activity = activity;
    locationManager = (LocationManager) activity
            .getSystemService(Context.LOCATION_SERVICE);
    startLocationListener(delayTimeInMilisecounds);
    //activity.setInfo("Szukanie satelit");
}
    public GPSManager(RouteActivity activity, int delayTimeInMilisecounds) {
        this.activity2 = activity;
        locationManager = (LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE);
        startLocationListener2(delayTimeInMilisecounds);
        //activity.setInfo("Szukanie satelit");
    }
    public Location getBetterLocation(Location location, Location actualLocation) {
        activity.addAvgLocation(location.getLatitude(),location.getLongitude(),location.getAccuracy());
        if (actualLocation == null) {
            return location;
        }
        if (actualLocation.getAccuracy() >= location.getAccuracy()) {
            return location;
        }
        return actualLocation;
    }

    public void startLocationListener(int delayTimeInMilisecounds) {

        activity.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        activity.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        locationListener = new LocationListener() {
            // getting GPS status

            @Override
            public void onLocationChanged(Location location) {
                activity.map.getOverlays().clear();
                activity.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                activity.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);




                activity.setActualLocation(getBetterLocation(location,
                        activity.getActualLocation()));
                Log.i("GPS loc ch", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
                Log.i("GPS loc ch", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
                //Toast toast = Toast.makeText(activity, "Dostawca: " + provider + "\nStatus: " + status, Toast.LENGTH_LONG);
               // toast.show();
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,  locationListener);
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                //Toast toast = Toast.makeText(activity, "Sledzenie uruchomione", Toast.LENGTH_LONG);
               // toast.show();
                locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,  locationListener);
                }

            }

            @Override
            public void onProviderDisabled(String provider) {
                //Toast toast = Toast.makeText(activity, "Utracono sygnał", Toast.LENGTH_LONG);
                //toast.show();
                locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,  locationListener);
                }

            }
        };
        if(activity.isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            activity.startPoint = new GeoPoint(loc);

            Log.i("GPS", "Lat: " + loc.getLatitude() + " long: " + loc.getLongitude() + " accuracy: " + loc.getAccuracy());
            Log.i("GPS", "Bear: " + loc.getBearing() + " prov: " + loc.getProvider() + " speed: " + loc.getSpeed());
            locationManager.removeUpdates(locationListener);
            Marker startMarker = new Marker(activity.map);
            startMarker.setPosition(activity.startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            activity.mapController.setZoom(17);
            activity.mapController.setCenter(activity.startPoint);
            activity.map.getOverlays().add(startMarker);
            activity.map.invalidate();
            if(activity.tracking) {
                activity.route.add(activity.startPoint);
            }
        }
        else if(activity.isGPSEnabled)
        {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0,
                    locationListener);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            activity.startPoint = new GeoPoint(loc);
            Log.i("GPS", "Lat: " + loc.getLatitude() + " long: " + loc.getLongitude() + " accuracy: " + loc.getAccuracy());
            Log.i("GPS", "Bear: " + loc.getBearing() + " prov: " + loc.getProvider() + " speed: " + loc.getSpeed());
            locationManager.removeUpdates(locationListener);
            Marker startMarker = new Marker(activity.map);


            startMarker.setPosition(activity.startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            activity.mapController.setZoom(17);
            activity.mapController.setCenter(activity.startPoint);
            activity.map.getOverlays().add(startMarker);
            activity.map.invalidate();
            if(activity.tracking) {
                activity.route.add(activity.startPoint);
            }
        }
        try {
            if (activity.isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0,
                        0, locationListener);
            }
            if (activity.isGPSEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0, 0,
                        locationListener);
            }
        }
        catch(Exception ex)
        {

        }
    }

    public void startLocationListener2(int delayTimeInMilisecounds) {

        activity2.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        activity2.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        locationListener = new LocationListener() {
            // getting GPS status

            @Override
            public void onLocationChanged(Location location) {

                activity.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                activity.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);




                activity.setActualLocation(getBetterLocation(location,
                        activity.getActualLocation()));
                Log.i("GPS loc ch", "Lat: " + location.getLatitude() + " long: " + location.getLongitude() + " accuracy: " + location.getAccuracy());
                Log.i("GPS loc ch", "Bear: " + location.getBearing() + " prov: " + location.getProvider() + " speed: " + location.getSpeed());


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
                Toast toast = Toast.makeText(activity2, "Dostawca: " + provider + "\nStatus: " + status, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast toast = Toast.makeText(activity2, "Sledzenie uruchomione", Toast.LENGTH_LONG);
                toast.show();
                locationManager = (LocationManager) activity2.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);

                } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,  locationListener);
                }

            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast toast = Toast.makeText(activity2, "Utracono sygnał", Toast.LENGTH_LONG);
                toast.show();
                locationManager = (LocationManager) activity2.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);

                } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,  locationListener);
                }

            }
        };
        if(activity2.isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0,
                    locationListener);
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            activity2.geopoint_start = new GeoPoint(loc);

            locationManager.removeUpdates(locationListener);

            activity2.mapController.setZoom(17);
            activity2.mapController.setCenter(activity2.geopoint_start);

            activity2.map.invalidate();

        }
        else if(activity2.isGPSEnabled)
        {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0,
                    locationListener);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            activity2.geopoint_start = new GeoPoint(loc);

            locationManager.removeUpdates(locationListener);

            activity2.mapController.setZoom(17);
            activity2.mapController.setCenter(activity2.geopoint_start);

            activity2.map.invalidate();
        }
        try {
            if (activity2.isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, delayTimeInMilisecounds,
                        0, locationListener);
            }
            if (activity2.isGPSEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, delayTimeInMilisecounds, 0,
                        locationListener);
            }
        }
        catch(Exception ex)
        {

        }
    }




    public void stop() {
        locationManager.removeUpdates(locationListener);
    }

}
