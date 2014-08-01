package com.poc.android.geofencepoc;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.poc.android.geofencepoc.contentprovider.GeoFenceContentProvider;
import com.poc.android.geofencepoc.model.dao.DBHelper;

import java.util.Date;
import java.util.List;

public class GeoFenceTransitionIntentService extends IntentService {
    private static final String TAG = "GeoFenceTransitionIntentService";

    public GeoFenceTransitionIntentService() {
        super("GeoFenceTransitionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent(" + intent + ")");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null) {
            Log.d(TAG, "geoFencingEvent == null");
            return;
        }

        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + Integer.toString(errorCode));
        } else {
            int transitionType = geofencingEvent.getGeofenceTransition();
            Location location = geofencingEvent.getTriggeringLocation();

            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();

            String[] geofenceIds = new String[geofences.size()];
            for (int index = 0; index < geofences.size() ; index++) {
                geofenceIds[index] = geofences.get(index).getRequestId();
            }
            String ids = TextUtils.join(",", geofenceIds);


            String[] triggerIds = new String[geofences.size()];

            for (int i = 0; i < triggerIds.length; i++) {
                triggerIds[i] = geofences.get(i).getRequestId();
            }

            String transitionString = null;
            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    transitionString = "exited";
                    updateExitTime(geofenceIds);
                    resetNewGeoFence(location);
                    break;
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    transitionString = "entered";
                    updateEnterTime(geofenceIds);
                    break;
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    transitionString = "dwelled";
                    updateDwellTime(geofenceIds);
                    break;
                default:
                    Log.e(TAG, "Geofence transition error: " + Integer.toString(transitionType));
            }

            sendNotification(transitionString, ids);

            Log.d(TAG, "geofence(s) " + transitionString + ", ids " + ids);
        }
    }

    private void updateEnterTime(String[] geofenceIds) {
        for (String id : geofenceIds) {

            Uri uri = Uri.parse(GeoFenceContentProvider.GEOFENCE_CONTENT_URI + "/" + id);
            Date now = new Date();
            ContentValues values = new ContentValues();
            values.put(DBHelper.GEOFENCES_COLUMN_ENTER_TIME, now.getTime());

            Log.d(TAG, "updated geofence enter time for geofence " + id);

            App.context.getContentResolver().update(uri, values, null, null);
        }
    }

    private void updateDwellTime(String[] geofenceIds) {
        for (String id : geofenceIds) {

            Uri uri = Uri.parse(GeoFenceContentProvider.GEOFENCE_CONTENT_URI + "/" + id);
            Date now = new Date();
            ContentValues values = new ContentValues();
            values.put(DBHelper.GEOFENCES_COLUMN_DWELL_TIME, now.getTime());

            Log.d(TAG, "updated geofence dwell time for geofence " + id);

            App.context.getContentResolver().update(uri, values, null, null);
        }
    }

    private void updateExitTime(String[] geofenceIds) {
        for (String id : geofenceIds) {

            Uri uri = Uri.parse(GeoFenceContentProvider.GEOFENCE_CONTENT_URI + "/" + id);
            Date now = new Date();
            ContentValues values = new ContentValues();
            values.put(DBHelper.GEOFENCES_COLUMN_EXIT_TIME, now.getTime());

            Log.d(TAG, "updated geofence exit time for geofence " + id);

            App.context.getContentResolver().update(uri, values, null, null);
        }
    }


    private void sendNotification(String transitionType, String ids) {

        // Create an explicit content Intent that starts the main Activity
        Intent notificationIntent =
                new Intent(getApplicationContext(), TabsMainActivity.class);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(TabsMainActivity.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.ic_fence)
                .setContentTitle(
                        getString(R.string.geofence_transition_notification_title, transitionType, ids)
                )
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    public void resetNewGeoFence(Location location) {
        GeoFenceUpdateAsyncTask.GeoFenceUpdateRequest geoFenceUpdateRequest = new GeoFenceUpdateAsyncTask.GeoFenceUpdateRequest();
        geoFenceUpdateRequest.setDeviceId(App.getGcmRegistrationId());
        geoFenceUpdateRequest.setLatitude(location.getLatitude());
        geoFenceUpdateRequest.setLongitude(location.getLongitude());
        geoFenceUpdateRequest.setTimestamp(new Date());

        new GeoFenceUpdateAsyncTask(this).execute(geoFenceUpdateRequest);
    }
}
