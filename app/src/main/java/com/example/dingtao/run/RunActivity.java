package com.example.dingtao.run;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RunActivity extends ActionBarActivity  {
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
        LineChart speedChart = (LineChart) findViewById(R.id.speed_chart);
        name.setText(run.name);
        begin.setText("Started: " + run.StartedToTime());
        distance.setText("Distance:" + run.DistanceToKm());
        duration.setText("Duration:" + run.DurationToTime());
        averageSpeed.setText("Speed:" + run.SpeedToKmPH());

        YAxis yAxis = speedChart.getAxisLeft();
        XAxis xAxis = speedChart.getXAxis();
        yAxis.setEnabled(true);
        xAxis.setEnabled(true);
        yAxis.setDrawAxisLine(true);
        xAxis.setDrawAxisLine(true);
        yAxis.setDrawLabels(true);
        xAxis.setDrawLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> vals = new ArrayList<Entry>();

        for (LocationJSON locationJSON : run.tracks){
            long durationtime = locationJSON.time - run.begin;
            long second = (durationtime / 1000) % 60;
            long minute = (durationtime / (1000 * 60)) % 60;
            long hour = (durationtime / (1000 * 60 * 60)) % 24;
            xVals.add(String.format("%02d:%02d:%02d", hour, minute, second));

            Entry entry = new Entry((float)locationJSON.speed,(int)durationtime);
            vals.add(entry);
            Log.i("Entry",entry.toString());
        }

        LineDataSet speedDataSet = new LineDataSet(vals,"Speed");
        speedDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSets.add(speedDataSet);
        LineData data = new LineData(xVals,dataSets);
        speedChart.setData(data);

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
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    public void Delete(MenuItem item){
        Model.Get().RemoveRun(rid);
        finish();
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
