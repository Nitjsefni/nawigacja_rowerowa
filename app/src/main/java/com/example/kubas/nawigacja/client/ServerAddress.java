package com.example.kubas.nawigacja.client;

import com.example.kubas.nawigacja.data.DataManager;

class ServerAddress {
    private static String serverUrl = "http://beta.wskocznarower.pl/app_dev.php/";
    //    private static String serverUrl = "http://192.168.2.10/app_dev.php/";
    private static final String PASSWORD = "1234";
    private static final String VERSION = "1.0";
    private static ServerAddress instance;
    String user;
    String password;

    public ServerAddress() {
        this.user = DataManager.getInstance().getUsername();
        this.password = DataManager.getInstance().getPassword();

    }

    public static ServerAddress getInstance() {
        if (instance == null) {
            instance = new ServerAddress();
        }
        return instance;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        ServerAddress.serverUrl = serverUrl;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public String getVERSION() {
        return VERSION;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
    public String getParameters(){
        return "p="+getPASSWORD()+"&ver="+getVERSION()+"&username="+getUser()+"&password="+getPassword();
    }
}
