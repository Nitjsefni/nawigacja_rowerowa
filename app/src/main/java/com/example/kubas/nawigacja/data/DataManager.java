package com.example.kubas.nawigacja.data;

import android.content.Context;
import android.widget.ListAdapter;

import com.example.kubas.nawigacja.R;
import com.example.kubas.nawigacja.client.RouteListClient;
import com.example.kubas.nawigacja.data.model.ListWithNotification;
import com.example.kubas.nawigacja.data.interfaces.Listener;
import com.example.kubas.nawigacja.data.model.Route;
import com.example.kubas.nawigacja.exceptions.UnAuthorizedException;
import com.example.kubas.nawigacja.list_adapters.RouteListAdapter;

import org.apache.http.message.BasicNameValuePair;

public class DataManager {
    private static DataManager instance;
    private ListWithNotification<Route> savedRoutes = new ListWithNotification<>("savedRoutes");
    private ListWithNotification<Route> recomendedRoutes = new ListWithNotification<>("recomendedRoutes");
    private String username;
    private String password;


    private DataManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static DataManager getInstance() {
        if (instance == null) {
            throw new UnAuthorizedException();
        }
        return instance;
    }

    public static boolean login(String username, String password) {
        instance = new DataManager(username, password);
        return true;
    }

    public void loadRecomendedRoutes() {
        new RouteListClient(recomendedRoutes).execute(new BasicNameValuePair("type", "public"));

    }

    public void loadSavedRoutes() {
        new RouteListClient(savedRoutes).execute(new BasicNameValuePair("type", "private"));
    }

    public ListAdapter getSavedRouteAdapter(Context context) {
        return new RouteListAdapter(context, R.layout.route_list_row, savedRoutes);
    }

    public ListAdapter getRecomendedRouteAdapter(Context context) {
        return new RouteListAdapter(context, R.layout.route_list_row, recomendedRoutes);
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void registerRouteListListener(Listener listener) {
        recomendedRoutes.register(listener);
        savedRoutes.register(listener);
    }
}
