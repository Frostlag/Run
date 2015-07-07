package com.example.dingtao.run;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

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

import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;


public class RunActivity extends Activity {
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

        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("Map");
        tab1.setIndicator("Map");
        tab1.setContent(R.id.map);
        tabHost.addTab(tab1);

        TabHost.TabSpec tab2 = tabHost.newTabSpec("Speed");
        tab2.setIndicator("Speed");
        tab2.setContent(R.id.speed_chart);
        tabHost.addTab(tab2);

        LineChartView speedChart = (LineChartView) findViewById(R.id.speed_chart);
        speedChart.setInteractive(true);
        speedChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        double maxSpeed = 0;
        double maxAccuracy = 0;

        for (LocationJSON locationJSON : run.tracks){
            if (locationJSON.speed > maxSpeed) maxSpeed = locationJSON.speed;
            if (locationJSON.accuracy > maxAccuracy) maxAccuracy = locationJSON.accuracy;
        }
        double speedtoAccuracy = maxSpeed/maxAccuracy;

        List<PointValue> speedValues = new ArrayList<PointValue>();
        List<PointValue> accuracyValues = new ArrayList<PointValue>();
        for (LocationJSON locationJSON : run.tracks){
            long durationtime = locationJSON.time - run.begin;
            //values.add(String.format("%02d:%02d", minute, second));
            speedValues.add(new PointValue((int) durationtime,(float)(locationJSON.speed)));
            accuracyValues.add(new PointValue((int) durationtime,(float)(locationJSON.accuracy*speedtoAccuracy)));
        }


        
        List<Line> lines = new ArrayList<Line>();

        Line speedLine = new Line(speedValues).setColor(Color.BLUE);

        Line accuracyLine = new Line(accuracyValues).setColor(Color.RED);

        lines.add(speedLine);
        lines.add(accuracyLine);
        LineChartData data = new LineChartData().setLines(lines);

        Axis timeAxis = new Axis().setName("Time").setHasLines(true).setTextColor(Color.BLACK);
        data.setAxisXBottom(timeAxis);
        Axis speedAxis = new Axis().setName("Speed").setHasLines(true).setTextColor(Color.BLACK);
        data.setAxisYLeft(speedAxis);
        Axis accuracyAxis = new Axis().setName("Accuracy").setHasLines(true).setTextColor(Color.BLACK).setFormatter(new AccuracyFormatter(speedtoAccuracy));
        data.setAxisYRight(accuracyAxis);

        speedChart.setLineChartData(data);

        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

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


    private class AccuracyFormatter extends SimpleAxisValueFormatter{
        private double speedToAccuracy;

        private AccuracyFormatter(double speedToAccuracy){
            this.speedToAccuracy = speedToAccuracy;
        }

        @Override
        public int formatValueForAutoGeneratedAxis(char[] formattedValue, float value, int autoDecimalDigits) {
            float scaledValue = (float)(value/speedToAccuracy);
            return super.formatValueForAutoGeneratedAxis(formattedValue, scaledValue, autoDecimalDigits);
        }
    }

}
