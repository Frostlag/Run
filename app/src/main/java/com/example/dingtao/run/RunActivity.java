package com.example.dingtao.run;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;


public class RunActivity extends ActionBarActivity {
    int rid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Model model = Model.Get();
        rid = getIntent().getIntExtra("rid", 0);
        Run run = model.runs.get(rid);


        setContentView(R.layout.activity_run);
        TextView name = (TextView) findViewById(R.id.name);
        TextView begin = (TextView) findViewById(R.id.begin);
        TextView distance = (TextView) findViewById(R.id.distance);
        TextView duration = (TextView) findViewById(R.id.duration);
        TextView averageSpeed = (TextView) findViewById(R.id.average_speed);
        name.setText(run.name);
        begin.setText("Started: " + run.StartedToTime());
        distance.setText("Distance:" + run.DistanceToKm());
        duration.setText("Duration:" + run.DurationToTime());
        averageSpeed.setText("Speed:" + run.SpeedToKmPH());

        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.Map)).getMap();

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (LocationJSON locationJSON : run.tracks){
            options.add(new LatLng(locationJSON.latitude,locationJSON.longitude));
        }
        List<LatLng> points = map.addPolyline(options).getPoints();
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (LatLng latLng : points){
            builder.include(latLng);
        }
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
            }
        });

        Log.i("TEST",run.name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_run, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
