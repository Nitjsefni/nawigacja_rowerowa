package com.example.kubas.nawigacja.client;

import android.os.AsyncTask;
import android.util.Log;

import com.example.kubas.nawigacja.data.DataManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class WebApiClient extends AsyncTask<NameValuePair, String, String> {
    public static final String PASSWORD = "1234";
    public static final String VERSION = "1.0";
    String user;
    String password;

    public WebApiClient() {
        this.user = DataManager.getInstance().getUsername();
        this.password = DataManager.getInstance().getPassword();
    }

    @Override
    protected String doInBackground(final NameValuePair... request) {
        NameValuePair[] preparedRequest = prepareRequest(request);
        try {
            HttpClient hClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(getServiceUri());
            List<NameValuePair> parameters = new ArrayList<>();

            parameters.add(new BasicNameValuePair("p", PASSWORD));
            parameters.add(new BasicNameValuePair("ver", VERSION));
            parameters.add(new BasicNameValuePair("username", user));
            parameters.add(new BasicNameValuePair("password", password));
            Collections.addAll(parameters, preparedRequest);
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            HttpResponse httpResponse = hClient.execute(httpPost);

            InputStream inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) {
                parseResponse(convertInputStreamToString(inputStream));
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
        }
        return null;
    }

    protected abstract NameValuePair[] prepareRequest(NameValuePair[] request);

    protected abstract void parseResponse(String result) throws IOException;

    protected abstract String getServiceUri();

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        inputStream.close();
        return result.toString();
    }

    protected boolean checkStatus(String status) throws JSONException {
        if (!"0".equals(status)) {
            Log.e(this.getClass().getName(), "Błędny status odpowiedzi:" + status);
            return true;
        }
        return false;
    }
}
