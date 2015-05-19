package com.example.kubas.nawigacja;

/**
 * Created by KubaS on 2015-05-04.
 */
public class GeoPosition {
    private  String _GP_NAME, _GP_LAT, _GP_LNG;
    public GeoPosition (String GP_NAME, String GP_LAT, String GP_LNG)
    {


        _GP_NAME = GP_NAME;
        _GP_LAT = GP_LAT;
        _GP_LNG = GP_LNG;
    }


    public String getGeoPositionName()
    { return _GP_NAME;}

    public String getGeoPositionLat()
    { return _GP_LAT;}

    public String getGeoPositionLng()
    { return _GP_LNG;}

}
