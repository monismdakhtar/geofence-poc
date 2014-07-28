package com.poc.android.geofencepoc;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.poc.android.geofencepoc.model.GeoFence;
import com.poc.android.geofencepoc.model.ModelException;

import java.util.ArrayList;

import static com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER;

public class GeoFenceContentObserver extends ContentObserver implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GeoFenceContentObserver";
    private static final long UPDATE_INTERVAL = 60000L;
    private static final float MIN_CHANGE_TO_REPORT = 0.0f;


    private LocationRequest locationRequest;
    private Intent locationUpdateIntent;
    private PendingIntent locationUpdatePendingIntent;
    private Intent geoFenceTransitionIntent;
    private PendingIntent geoFenceTransitionPendingIntent;
    private GoogleApiClient googleApiClient;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public GeoFenceContentObserver(Handler handler) {
        super(handler);
        geoFenceTransitionIntent  = new Intent(App.context, GeoFenceTransitionIntentService.class);
        locationUpdateIntent = new Intent(App.context, LocationUpdateIntentService.class);

        googleApiClient = new GoogleApiClient.Builder(App.context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = LocationRequest.create()
                .setPriority(PRIORITY_LOW_POWER)
                .setInterval(UPDATE_INTERVAL)
                .setSmallestDisplacement(MIN_CHANGE_TO_REPORT)
                .setFastestInterval(UPDATE_INTERVAL);

    }

    @Override
    public boolean deliverSelfNotifications() {
        Log.d(TAG, "deliverSelfNotifications()");
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.d(TAG, "onChange(" + selfChange + ")");

        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        } else if (googleApiClient.isConnected()) {
            onConnected(null);
        }

        super.onChange(selfChange);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG, "onChange(" + selfChange + ", " + uri + ")");
        super.onChange(selfChange, uri);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google Sevices API onConnected()");
        GeoFence latestGeoFence;
        ArrayList<Geofence> geofences = new ArrayList<Geofence>();

        try {
            latestGeoFence = GeoFence.findLatestGeoFence();
        } catch (ModelException e) {
            Log.e(TAG, "unable to update active geofence with latest geofence: " + e.getLocalizedMessage());
//            googleApiClient.disconnect();
            return;
        }

        // first clean out any currently installed fences
        PendingResult<Status> removeGeofencesResult = LocationServices.GeofencingApi.removeGeofences(googleApiClient, createGeoFencePendingIntent());
        removeGeofencesResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "geofences removed successfully");
                } else {
                    Toast.makeText(App.context, "Unable to remove GeoFences: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Geofence googleGeofence = new Geofence.Builder()
                .setCircularRegion(
                        latestGeoFence.getLatitude(),
                        latestGeoFence.getLongitude(),
                        latestGeoFence.getRadius()
                )
                .setLoiteringDelay(30000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setRequestId(String.valueOf(latestGeoFence.getId()))
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();

        Log.d(TAG, "created google Geofence: " + googleGeofence);

        geofences.add(googleGeofence);

        if (! geofences.isEmpty()) {
            PendingResult<Status> addGeofencesResult = LocationServices.GeofencingApi.addGeofences(googleApiClient, geofences, createGeoFencePendingIntent());
            addGeofencesResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "geofence monitoring added successfully");
                    } else {
                        Toast.makeText(App.context, "Unable to monitor GeoFences: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            PendingResult<Status> locationUpdateRequestResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, createLocationUpdatePendingIntent());
            locationUpdateRequestResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "location updates requested successfully");
                    } else {
                        Toast.makeText(App.context, "Unable to get location updates: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

//        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Play Services onConnectionSuspended(" + i + ")");
        Toast.makeText(App.context, "Google Play Services onConnectionSuspended: " + i, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play Services onConnectionFailed(" + connectionResult + ")");
        Toast.makeText(App.context, "Google Play Services onConnectionFailed: " + connectionResult, Toast.LENGTH_LONG).show();
    }

    private PendingIntent createGeoFencePendingIntent() {
        if (null != geoFenceTransitionPendingIntent) {
            return geoFenceTransitionPendingIntent;
        } else {
            geoFenceTransitionPendingIntent = PendingIntent.getService(
                    App.context,
                    0,
                    geoFenceTransitionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            return geoFenceTransitionPendingIntent;
        }
    }

    private PendingIntent createLocationUpdatePendingIntent() {
        // If the PendingIntent already exists
        if (null != locationUpdatePendingIntent) {
            return locationUpdatePendingIntent;
        } else {
            locationUpdatePendingIntent = PendingIntent.getService(
                    App.context,
                    0,
                    locationUpdateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            return locationUpdatePendingIntent;
        }
    }
}
