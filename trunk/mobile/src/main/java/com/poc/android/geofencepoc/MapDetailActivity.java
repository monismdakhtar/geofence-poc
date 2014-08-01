package com.poc.android.geofencepoc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.poc.android.geofencepoc.model.GeoFence;
import com.poc.android.geofencepoc.model.ModelException;

public class MapDetailActivity extends ActionBarActivity {
    public static final String TAG = "MapDetailActivity";

    public static final String MAP_ACTION_EXTRA_GEOFENCE_ID = "map_action_extra_geofence_id";

    private long currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long startLocationId;

        startLocationId = getIntent().getExtras().getLong(MAP_ACTION_EXTRA_GEOFENCE_ID);

        getSupportActionBar().setTitle(R.string.action_bar_title_fence_map);

        Button next = (Button) findViewById(R.id.diff_map_next);
        Button prev = (Button) findViewById(R.id.diff_map_prev);

        next.setVisibility(View.VISIBLE);
        prev.setVisibility(View.VISIBLE);

        currentId = startLocationId;

        try {
            drawGeoFence(startLocationId, true);
        } catch (ModelException e) {
            e.printStackTrace();
        }
    }

    private void drawGeoFence(long id, boolean zoom) throws ModelException {

        GeoFence geoFence = new GeoFence(id);

        LatLng latLng = new LatLng(geoFence.getLatitude(), geoFence.getLongitude());

        assert getSupportFragmentManager().findFragmentById(R.id.map) != null;
        GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title(geoFence.getCreateTime().toString()));
        map.addCircle(new CircleOptions()
                .center(currentMarker.getPosition())
                .radius(geoFence.getRadius())
                .strokeColor(R.color.DimGray)
                .fillColor(R.color.DimGray)
                .strokeWidth(0));

        if (zoom) {

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));

            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
        }
    }


    /**
     * Maps the Home (android.R.id.home) selection to ending this {@link android.app.Activity}
     *
     * @param item menu item selected
     * @return where the menu selection was handled or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onPrevDiffClick(View view) {
        Log.d(TAG, "onPrevDiffClick()");

        GeoFence currentGeoFence;
        try {
            currentGeoFence = new GeoFence(currentId);
        } catch (ModelException e) {
            Log.e(TAG, "unable to load current geofence, id = " + currentId);
            return;
        }

        GeoFence prevGeoFence = currentGeoFence.getNext();

        if (prevGeoFence == null) {
            Toast.makeText(getApplicationContext(), "No more Location Diffs", Toast.LENGTH_LONG).show();
            return;
        }

        currentId = prevGeoFence.getId();
        try {
            drawGeoFence(prevGeoFence.getId(), false);
        } catch (ModelException e) {
            Log.e(TAG, "unable to draw current geofence, id = " + currentId);
        }

    }

    public void onNextDiffClick(View view) {
        Log.d(TAG, "onNextDiffClick()");

        GeoFence currentGeoFence;
        try {
            currentGeoFence = new GeoFence(currentId);
        } catch (ModelException e) {
            Log.e(TAG, "unable to load current geofence, id = " + currentId);
            return;
        }

        GeoFence nextGeoFence = currentGeoFence.getPrev();

        if (nextGeoFence == null) {
            Toast.makeText(getApplicationContext(), "No more Location Diffs", Toast.LENGTH_LONG).show();
            return;
        }

        currentId = nextGeoFence.getId();
        try {
            drawGeoFence(nextGeoFence.getId(), false);
        } catch (ModelException e) {
            Log.e(TAG, "unable to draw current geofence, id = " + currentId);
        }
    }
}
