package com.example.kubas.nawigacja.listeners;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;

import com.example.kubas.nawigacja.client.GeoLocationClient;

import org.apache.http.message.BasicNameValuePair;

public class AutocompleteLocationListener implements TextWatcher {
    private AutoCompleteTextView autoCompleteTextView;
    private Activity context;

    public AutocompleteLocationListener(Activity context,AutoCompleteTextView autoCompleteTextView) {
        this.autoCompleteTextView = autoCompleteTextView;
        this.context = context;
    }

    public void afterTextChanged(Editable editable) {

    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        new GeoLocationClient(context, autoCompleteTextView).execute(new BasicNameValuePair("name", s.toString()));
    }

}

