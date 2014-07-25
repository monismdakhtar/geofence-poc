package com.poc.android.geofencepoc;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


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

    public GeoFenceMapFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

//            if (myLocation != null) {
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 5));
//                map.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
//            }

        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_google_maps_available), Toast.LENGTH_LONG).show();
        }

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
}
