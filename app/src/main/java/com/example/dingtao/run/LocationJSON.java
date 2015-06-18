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
        accuracy = location.getDouble("accuracy");
        latitude = location.getDouble("latitude");
        longitude = location.getDouble("longitude");
        altitude = location.getDouble("altitude");
        bearing = location.getDouble("bearing");
        provider = location.getString("provider");
        time = location.getLong("time");
        speed = location.getDouble("speed");
    }

    public JSONObject ToJSONObject() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accuracy",accuracy);
        jsonObject.put("latitude", latitude);
        jsonObject.put("longitude", longitude);
        jsonObject.put("altitude", altitude);
        jsonObject.put("bearing", bearing);
        jsonObject.put("provider", provider);
        jsonObject.put("time", time);
        jsonObject.put("speed", speed);

        return jsonObject;
    }

    protected boolean IsBetterLocation(LocationJSON location) {

        // Check whether the new location fix is newer or older
        long timeDelta = location.time - time;
        boolean isSignificantlyNewer = timeDelta > Model.Get().max_time;
        boolean isSignificantlyOlder = timeDelta < -Model.Get().max_time;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.accuracy - accuracy);
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = IsSameProvider(location);

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean IsSameProvider(LocationJSON location) {
        return provider.equals(location.provider);
    }

}
