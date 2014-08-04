package com.poc.android.geofencepoc;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;

import static com.poc.android.geofencepoc.EscapeeDetectionReceiver.*;
import static com.poc.android.geofencepoc.contentprovider.GeoFenceContentProvider.GEOFENCE_CONTENT_URI;


public class TabsMainActivity extends ActionBarActivity implements
        ActionBar.OnNavigationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "TabsMainActivity";

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "673992645420";

    private GoogleCloudMessaging gcm;
    private String gcmRegistrationId;
    private GeoFenceContentObserver geoFenceContentObserver;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs_main);

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            gcmRegistrationId = App.getGcmRegistrationId();

            if (gcmRegistrationId.isEmpty()) {
                registerInBackground();
            }
        }

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[] {
                                getString(R.string.title_section1),
                                getString(R.string.title_section2)
                        }),
                this);


        geoFenceContentObserver = new GeoFenceContentObserver(new Handler());
        getContentResolver().registerContentObserver(GEOFENCE_CONTENT_URI, true, geoFenceContentObserver);

        // start escapee detection
        sendBroadcast(new Intent(ESCAPEERECEIVER_START_ACTION));
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

//        if (! App.getGcmRegistrationId().isEmpty()) {
//            registerGeoFence();
//        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        getContentResolver().unregisterContentObserver(geoFenceContentObserver);
        super.onDestroy();
    }

    @Override
    public void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar().getSelectedNavigationIndex());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tabs_main, menu);
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
            registerGeoFence();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        Fragment destination;

        switch (position) {
            case 0:
                destination = new GeoFenceMapFragment();
                break;
            case 1:
                destination = new GeoFenceFragment();
                break;
            default:
                throw new IllegalArgumentException("onNavigationItemSelected(" + position + "): invalid selection");
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, destination).commit();

        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API Client onConnected()");

        String gcmRegistrationId = App.getGcmRegistrationId();

        if (gcmRegistrationId.isEmpty()) {
            Log.d(TAG, "unable to register GeoFence without GCM registration ID");
        } else {

            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            Log.d(TAG, "current location: " + location);

            if (location != null) {
                GeoFenceUpdateAsyncTask.GeoFenceUpdateRequest geoFenceUpdateRequest = new GeoFenceUpdateAsyncTask.GeoFenceUpdateRequest();
                geoFenceUpdateRequest.setDeviceId(gcmRegistrationId);
                geoFenceUpdateRequest.setLatitude(location.getLatitude());
                geoFenceUpdateRequest.setLongitude(location.getLongitude());
                geoFenceUpdateRequest.setTimestamp(new Date());

                new GeoFenceUpdateAsyncTask(this).execute(geoFenceUpdateRequest);
            }
        }
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Google API Client onConnectionSuspended(" + cause + ")");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google API Client onConnectionFailed(" + connectionResult + ")");
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerGeoFence() {
        Log.d(TAG, "registerGeoFence()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        } else if (googleApiClient.isConnected()) {
            onConnected(null);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    gcmRegistrationId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + gcmRegistrationId;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    //sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    App.storeRegistrationId(gcmRegistrationId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                super.onPostExecute(msg);
                Log.d(TAG, msg);
            }
        }.execute(null, null, null);


    }

    // start LifeCycle Logging
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        Log.d(TAG, "onPostResume()");
        super.onPostResume();
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }


    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart()");
        super.onRestart();
    }
    // end LifeCycle Logging
}
