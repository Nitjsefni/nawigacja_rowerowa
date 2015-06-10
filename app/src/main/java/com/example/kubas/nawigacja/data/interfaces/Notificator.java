package com.example.kubas.nawigacja.data.interfaces;

import com.example.kubas.nawigacja.data.interfaces.Listener;

public interface Notificator {
    void register(Listener listener);

    String getName();
}
