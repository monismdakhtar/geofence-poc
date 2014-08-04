package com.poc.android.geofencepoc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EscapeeDetectionReceiver extends BroadcastReceiver {
    private static final String TAG = "EscapeeDetectionReceiver";

    public static final String ESCAPEERECEIVER_START_ACTION = "com.poc.android.geofencepoc.EscapeeDetectionReceiver.start";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive(" + context + ", " + intent + ")");

        if (intent.getAction().equalsIgnoreCase(ESCAPEERECEIVER_START_ACTION) || intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, EscapeeDetectionService.class);
            context.startService(serviceIntent);
        }
    }
}
