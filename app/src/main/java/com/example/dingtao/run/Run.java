package com.example.dingtao.run;

import android.location.Location;
import android.util.Log;


import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dingtao on 6/14/2015.
 */
public class Run {
    public List<LocationJSON> tracks;
    public String name;
    public double distance;
    public double averageSpeed;
    public long duration;
    public long begin;


    public void Calc(){

    }

    public void ForceAddLocation(Location location){
        LocationJSON locationJSON = new LocationJSON(location);
        LocationJSON lastLocation = tracks.get(tracks.size()-1);
        tracks.add(locationJSON);
        distance += location.distanceTo(lastLocation.ToLocation());
        duration = tracks.get(tracks.size()-1).time - tracks.get(0).time;
    }

    public boolean AddLocation(Location location){
        LocationJSON locationJSON = new LocationJSON(location);
        LocationJSON lastLocation = tracks.get(tracks.size()-1);
        if (!lastLocation.IsBetterLocation(locationJSON)) return false;
        else{
            tracks.add(locationJSON);
            distance += location.distanceTo(lastLocation.ToLocation());
            duration = tracks.get(tracks.size()-1).time - tracks.get(0).time;
            Log.i("OLD LOCATION", lastLocation.toString());
            Log.i("NEW LOCATION", location.toString());
            return true;
        }
    }

    public JSONObject ToJSONObject() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("distance",distance);
        jsonObject.put("averageSpeed",averageSpeed);
        jsonObject.put("duration",duration);
        jsonObject.put("begin",begin);

        JSONArray jsonArray = new JSONArray();
        for (LocationJSON locationJSON : tracks){
            jsonArray.put(locationJSON.ToJSONObject());
        }
        jsonObject.put("tracks",jsonArray);
        return jsonObject;
    }

    public Run(Location location){
        name = "";
        distance = 0;
        averageSpeed = location.getSpeed();
        begin = System.currentTimeMillis();
        duration = 0;
        tracks = new ArrayList<LocationJSON>();
        tracks.add(new LocationJSON(location));
    }

    public Run(JSONObject jsonObject)throws JSONException{
        tracks = new ArrayList<LocationJSON>();
        name = jsonObject.getString("name");
        distance = jsonObject.getDouble("distance");
        averageSpeed = jsonObject.getDouble("averageSpeed");
        duration = jsonObject.getLong("duration");
        begin = jsonObject.getLong("begin");
        JSONArray jsonArray = jsonObject.getJSONArray("tracks");
        for (int i = 0; i < jsonArray.length(); i++) {
            tracks.add(new LocationJSON(jsonArray.getJSONObject(i)));
        }
    }

}
