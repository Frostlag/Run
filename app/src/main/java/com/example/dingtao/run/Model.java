package com.example.dingtao.run;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dingtao on 6/11/2015.
 */
public class Model implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener{
    public static String TRACK_UPDATED = "TRACK UPDATED";
    public boolean started,paused;
    public int min_time,max_time;
    public List<LocationJSON> track;

    private GoogleApiClient mGoogleApiClient;

    SharedPreferences SP;
    private static Model instance;
    private static Boolean init = false;
    private Boolean connected = false;
    private Context main;
    private List<UpdateableView> views;

    public static Model Model(Context context){
        if (init) {
            return instance;
        }
        init = true;
        instance = new Model(context);
        instance.ReloadPreferences();
        return instance;
    }

    private Model(Context context){
        main = context;
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        views = new ArrayList<UpdateableView>();
        Log.d("Test","Connecting");
        mGoogleApiClient = new GoogleApiClient.Builder(main).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    public void ReloadPreferences(){
        min_time = SP.getInt("min_time", 0);
        max_time = SP.getInt("max_time",300);
    }

    public static Model Get(){
        if (init) {
            return instance;
        }
        return null;
    }

    public void Start(){
        if (started){
            started = false;
            paused = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            //Intent intent = new Intent(main,TrackingService.class);
            //main.stopService(intent);
        }else{
            if (!connected){
                mGoogleApiClient.connect();
                try{wait(500);}catch(InterruptedException e){}
            }
            started = true;
            track = new ArrayList<LocationJSON>();
            //Intent intent = new Intent(main,TrackingService.class);
            //main.startService(intent);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,createLocationRequest(),this);
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            track.add(new LocationJSON(lastLocation));
        }
    }

    public void AddLocation(Location location){
        LocationJSON locationJSON = new LocationJSON(location);
        if (isBetterLocation(locationJSON,track.get(track.size()-1))) {
            track.add(locationJSON);
            Update(TRACK_UPDATED);
        }
    }

    public void Save(){
        if (started) return;
        if (track.isEmpty()) return;
    }

    public void Pause(){
        if (!started) return;

        if (paused){
            paused = false;
            Intent intent = new Intent(main,TrackingService.class);
            main.stopService(intent);
        }else{
            paused = true;
            Intent intent = new Intent(main,TrackingService.class);
            main.startService(intent);
        }
    }

    protected boolean isBetterLocation(LocationJSON location, LocationJSON currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.time - currentBestLocation.time;
        boolean isSignificantlyNewer = timeDelta > max_time*1000;
        boolean isSignificantlyOlder = timeDelta < -max_time*1000;
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
        int accuracyDelta = (int) (location.accuracy - currentBestLocation.accuracy);
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.provider,
                currentBestLocation.provider);

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
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void AddView(UpdateableView view){
        if (views.contains(view)) return;
        views.add(view);
    }

    public void RemoveView(UpdateableView view){
        if (!views.contains(view)) return;
        views.remove(view);
    }

    public void Update(String msg){
        for (UpdateableView view : views){
            view.Update(msg);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        connected = true;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        android.os.Process.killProcess(android.os.Process.myPid());

    }

    protected LocationRequest createLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(min_time);
        locationRequest.setInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;

    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.i("Location Changed", location.toString());
        AddLocation(location);
    }
}
