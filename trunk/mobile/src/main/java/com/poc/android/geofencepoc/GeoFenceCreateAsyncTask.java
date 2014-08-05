package com.poc.android.geofencepoc;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.poc.android.geofencepoc.contentprovider.GeoFenceContentProvider;
import com.poc.android.geofencepoc.model.GeoFence;
import com.poc.android.geofencepoc.model.ModelException;
import com.poc.android.geofencepoc.model.json.GeoFenceCreateRequest;
import com.poc.android.geofencepoc.model.json.GeoFenceResponse;
import com.poc.android.geofencepoc.util.JsonPoster;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_CREATE_TIME;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_ID;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_LATITUDE;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_LONGITUDE;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_NAME;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_RADIUS;

public class GeoFenceCreateAsyncTask
        extends
        AsyncTask<String, Integer, GeoFence>
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GeoFenceCreateAsyncTask";

    private Context context;
    private String errorMessage = null;

    public GeoFenceCreateAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected GeoFence doInBackground(String ... params) {
        Log.d(TAG, "doInBackground(" + params[0] + ")");

        if (params[0] == null) {
            errorMessage = "invalid parameter: " + params[0];
            return null;
        }

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        ConnectionResult connectionResult = googleApiClient.blockingConnect();

        if (! connectionResult.isSuccess()) {
            errorMessage = "unable to connect to Google: " + connectionResult;
            Log.e(TAG, errorMessage);

            return null;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);


        GeoFenceCreateRequest geoFenceCreateRequest = new GeoFenceCreateRequest();

        geoFenceCreateRequest.setDeviceId(params[0]);
        geoFenceCreateRequest.setLatitude(location.getLatitude());
        geoFenceCreateRequest.setLongitude(location.getLongitude());
        geoFenceCreateRequest.setCreateTime(new Date());

        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.setDateFormat(DateFormat.LONG);
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
            @Override
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                return src == null ? null : new JsonPrimitive(src.getTime());
            }
        });

        Gson gson = builder.create();

        String requestJson = gson.toJson(geoFenceCreateRequest);

        Log.d(TAG, "requestJson: " + requestJson);

        JsonPoster jsonPoster = new JsonPoster();
        String responseJson;
        try {
            responseJson = jsonPoster.postJson(requestJson, App.context.getString(R.string.geofence_server_url) + "create");
        } catch (JsonPoster.JsonPosterException e) {
            Log.e(TAG, "Error posting geofence creation json: " + e.getLocalizedMessage());
            errorMessage = "Error retrieving GeoFence from server";
            return null;
        }

//        {"id":6,"deviceId":"APA91bGq5BcyGmZTyvpIt7odBRnCcBCp-REgnOfwY6BbNgzT6p2Espics5xxpQgcOWPHkSHvjNy99UNkC6xF4f-3NsdFtz4WDNFhJAAmaZdDXWUmvkgcN3qepc4W1fk9i6imE-_J39b3QgsT5rVXcx5Wqfjec0v70O0kzbU9qaRzAc5YTn5PAkU","latitude":37.380867,"longitude":-122.086945,"radius":1000,"createTime":1406491569784,"exitTime":null,"version":0}
        Log.d(TAG, "JSON returned: " + responseJson);
        GeoFenceResponse response = gson.fromJson(responseJson, GeoFenceResponse.class);
        Log.d(TAG, "response:" + response);

        GeoFence geoFence = null;

        try {
            geoFence = recordGeoFence(response);
        } catch (ModelException e) {
            errorMessage = "Error recording GeoFence:";
            Log.e(TAG, "Error recording GeoFence:" + e.getLocalizedMessage());
        }

        return geoFence;
    }

    @Override
    protected void onPostExecute(GeoFence geoFence) {
        super.onPostExecute(geoFence);

        if (geoFence == null) {
            Log.e(TAG, "geoFence == null");
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private GeoFence recordGeoFence(GeoFenceResponse geoFenceUpdateResponse) throws ModelException {
        ContentValues values = new ContentValues();
        values.put(GEOFENCES_COLUMN_ID, geoFenceUpdateResponse.getId());
        values.put(GEOFENCES_COLUMN_LATITUDE, geoFenceUpdateResponse.getLatitude());
        values.put(GEOFENCES_COLUMN_LONGITUDE, geoFenceUpdateResponse.getLongitude());
        values.put(GEOFENCES_COLUMN_RADIUS, geoFenceUpdateResponse.getRadius());
        values.put(GEOFENCES_COLUMN_CREATE_TIME, geoFenceUpdateResponse.getCreateTime().getTime());
        values.put(GEOFENCES_COLUMN_NAME, "GeoFence " + geoFenceUpdateResponse.getId());

        Uri uri = App.context.getContentResolver().insert(GeoFenceContentProvider.GEOFENCE_CONTENT_URI, values);

        return new GeoFence(uri);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API Client onConnected()");
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Google API Client onConnectionSuspended(" + cause + ")");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google API Client onConnectionFailed(" + connectionResult + ")");
    }
}
