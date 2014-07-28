package com.poc.android.geofencepoc;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;

public class LocationUpdateIntentService extends IntentService {
    private static final String TAG = "LocationUpdateIntentService";

    public LocationUpdateIntentService() {
        super("LocationUpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent(" + intent + ")");
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            Location location = bundle.getParcelable(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            Log.d(TAG, "location : " + location);
        }

    }
}
