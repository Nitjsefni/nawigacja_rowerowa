package com.example.kubas.nawigacja.data.model;

import com.example.kubas.nawigacja.data.interfaces.Listener;
import com.example.kubas.nawigacja.data.interfaces.Notificator;

import java.util.ArrayList;
import java.util.List;

public class ListWithNotification<T> extends ArrayList<T> implements Notificator {
    private List<Listener> listenerList = new ArrayList<>();
    private String name;

    public ListWithNotification(String name) {
        this.name = name;
    }

    @Override
    public void register(Listener listener) {
        listenerList.add(listener);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean add(T object) {
        boolean added = super.add(object);
        notifyListeners();
        return added;
    }

    @Override
    public void clear() {
        super.clear();
    }

    private void notifyListeners() {
        for (Listener listener : listenerList) {
            listener.notify(this);
        }
    }
}
