package com.poc.android.geofencepoc;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

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

            if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) || (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();

                String[] geofenceIds = new String[geofences.size()];
                for (int index = 0; index < geofences.size() ; index++) {
                    geofenceIds[index] = geofences.get(index).getRequestId();
                }
                String ids = TextUtils.join(",", geofenceIds);
                String transitionString = getTransitionString(transitionType);

                String[] triggerIds = new String[geofences.size()];

                for (int i = 0; i < triggerIds.length; i++) {
                    triggerIds[i] = geofences.get(i).getRequestId();
                }

                sendNotification(transitionString, ids);
                resetNewGeoFence(location);

                Log.d(TAG, "geofence(s) " + transitionString + ", ids " + ids);

            } else {
                Log.e(TAG, "Geofence transition error: " + Integer.toString(transitionType));
            }
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
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.ic_fence)
                .setContentTitle(
                        getString(R.string.geofence_transition_notification_title,
                                transitionType, ids)
                )
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "entered";

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exited";

            default:
                return "unknown";
        }
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
