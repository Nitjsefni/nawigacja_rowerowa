package com.example.kubas.nawigacja.client;

class ServerAddress {
    private static String serverUrl = "http://beta.wskocznarower.pl/app_dev.php/";
//    private static String serverUrl = "http://192.168.2.10/app_dev.php/";

    public static String getServerUrl() {
        return serverUrl;
    }

    public static void setServerUrl(String serverUrl) {
        ServerAddress.serverUrl = serverUrl;
    }

}
