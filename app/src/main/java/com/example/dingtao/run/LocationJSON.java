package com.example.dingtao.run;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dingtao on 6/16/2015.
 */
public class LocationJSON {
    public double latitude;
    public double longitude;
    public double altitude;
    public double accuracy;
    public double bearing;
    public String provider;
    public long time;
    public double speed;

    public LocationJSON(Location location){
        accuracy = location.getAccuracy();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        bearing = location.getBearing();
        provider = location.getProvider();
        time = location.getTime();
        speed = location.getSpeed();

    }

    public Location ToLocation(){
        Location location = new Location(provider);
        location.setAccuracy((float)accuracy);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(altitude);
        location.setBearing((float)bearing);
        location.setTime(time);
        location.setSpeed((float)speed);

        return location;
    }

    public LocationJSON(JSONObject location) throws JSONException{
        accuracy = location.getDouble("Accuracy");
        latitude = location.getDouble("Latitude");
        longitude = location.getDouble("Longitude");
        altitude = location.getDouble("Altitude");
        bearing = location.getDouble("Bearing");
        provider = location.getString("Provider");
        time = location.getLong("Time");
        speed = location.getDouble("Speed");
    }

    public JSONObject ToJSONObject() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Accuracy",accuracy);
        jsonObject.put("Latitude",latitude);
        jsonObject.put("Longitude",longitude);
        jsonObject.put("Altitude",altitude);
        jsonObject.put("Bearing",bearing);
        jsonObject.put("Provider",provider);
        jsonObject.put("Time",time);
        jsonObject.put("Speed",speed);

        return jsonObject;
    }

}
