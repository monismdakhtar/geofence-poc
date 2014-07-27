package com.poc.android.geofencepoc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.poc.android.geofencepoc.model.GeoFence;
import com.poc.android.geofencepoc.model.ModelException;

import static com.poc.android.geofencepoc.contentprovider.GeoFenceContentProvider.GEOFENCE_CONTENT_URI;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_ALL_COLUMNS;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_ID;


/**
 *
 */
public class GeoFenceMapFragment extends Fragment implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraChangeListener {
    private static final String TAG = "GeoFenceMapFragment";

    private SupportMapFragment mapFragment;
    private GeoFenceContentObserver geoFenceContentObserver;
    private Marker currentMaker;

    public GeoFenceMapFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoFenceContentObserver = new GeoFenceContentObserver(new Handler());
        getActivity().getContentResolver().registerContentObserver(GEOFENCE_CONTENT_URI, true, geoFenceContentObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geo_fence_map, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);

        mapFragment = SupportMapFragment.newInstance(options);

        fragmentTransaction.replace(R.id.map, mapFragment);
        fragmentTransaction.commit();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        GoogleMap map = mapFragment.getMap();
        Log.d(TAG, "map:" + map);

        if (map != null) {
            map.setOnMapClickListener(this);
            map.setOnMarkerDragListener(this);
            map.setOnMarkerClickListener(this);
            map.setOnCameraChangeListener(this);

            map.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_google_maps_available), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
        getActivity().getContentResolver().unregisterContentObserver(geoFenceContentObserver);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // start GoogleMap Listeners
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
    // end GoogleMap Listeners

    public class GeoFenceContentObserver extends ContentObserver {
        private static final String TAG = "GeoFenceContentObserver";
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public GeoFenceContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "onChange(" + selfChange + ")");

            GeoFence latestGeoFence = null;
            
            try {
                latestGeoFence = findLatestGeoFence();
            } catch (ModelException e) {
                Log.e(TAG, "unable to update map with latest geofence: " + e.getLocalizedMessage());
            }

            if (latestGeoFence != null) {
                if (mapFragment != null && mapFragment.getMap() != null) {
                    mapFragment.getMap().clear();
                    addMarker(latestGeoFence, mapFragment.getMap());
                    zoomToCurrentMarker();
                } else {
                    Log.e(TAG, "Google map fragment or Google map are null");
                }
            } else {
                Toast.makeText(App.context, App.context.getString(R.string.toast_error_latest_geofence_not_found), Toast.LENGTH_LONG).show();
            }
            
            super.onChange(selfChange);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "onChange(" + selfChange + ", " + uri + ")");
            super.onChange(selfChange, uri);
        }

        private GeoFence findLatestGeoFence() throws ModelException {
            GeoFence geoFence = null;

            Cursor cursor = App.context.getContentResolver().query(
                    GEOFENCE_CONTENT_URI,
                    GEOFENCES_ALL_COLUMNS,
                    null,
                    null,
                    GEOFENCES_COLUMN_ID + " desc"
            );

            if (cursor != null) {
                cursor.moveToFirst();
                geoFence = new GeoFence(cursor);
            }

            return geoFence;
        }
    }

    private void addMarker(GeoFence latestGeoFence, GoogleMap map) {
        LatLng latLng = new LatLng(latestGeoFence.getLatitude(), latestGeoFence.getLongitude());

        currentMaker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(false)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        map.addCircle(new CircleOptions()
                .center(currentMaker.getPosition())
                .radius(latestGeoFence.getRadius())
                .strokeColor(R.color.DimGray)
                .fillColor(R.color.DimGray)
                .strokeWidth(0));
    }

    private void zoomToCurrentMarker() {
        if (currentMaker != null) {
            if (mapFragment != null && mapFragment.getMap() != null) {
                mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentMaker.getPosition().latitude, currentMaker.getPosition().longitude), 13));
//                mapFragment.getMap().animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
            }
        }
    }
}
