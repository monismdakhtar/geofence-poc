package com.poc.android.geofencepoc;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
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
import com.google.android.gms.location.LocationServices;
import com.poc.android.geofencepoc.contentprovider.GeoFenceContentProvider;
import com.poc.android.geofencepoc.model.GeoFence;
import com.poc.android.geofencepoc.model.ModelException;

import java.util.ArrayList;

public class GeoFenceContentObserver extends ContentObserver implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GeoFenceContentObserver";

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

        googleApiClient = new GoogleApiClient.Builder(App.context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public boolean deliverSelfNotifications() {
        Log.d(TAG, "deliverSelfNotifications()");
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.d(TAG, "onChange(" + selfChange + ")");

        super.onChange(selfChange);
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG, "onChange(" + selfChange + ", " + uri + ")");

        int uriType = GeoFenceContentProvider.URI_MATCHER.match(uri);

        // this will only update the geofence for inserts.  updates to individual geofences
        // like updating the entertime, will not trigger the registration od the updated geofence
        if (uriType == GeoFenceContentProvider.GEOFENCES) {
            if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            } else if (googleApiClient.isConnected()) {
                onConnected(null);
            }
        }
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
            googleApiClient.disconnect();
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
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
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
                    googleApiClient.disconnect();
                }
            });
        }
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
}
