package com.poc.android.geofencepoc;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.poc.android.geofencepoc.contentprovider.GeoFenceContentProvider;
import com.poc.android.geofencepoc.model.GeoFence;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_CREATE_TIME;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_LATITUDE;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_LONGITUDE;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_NAME;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_RADIUS;

public class GeoFenceUpdateAsyncTask extends AsyncTask<GeoFenceUpdateAsyncTask.GeoFenceUpdateRequest, Integer, GeoFence> {
    private static final String TAG = "GeoFenceUpdateAsyncTask";

    @Override
    protected GeoFence doInBackground(GeoFenceUpdateRequest... params) {
        Log.d(TAG, "doInBackground(" + params[0] + ")");

        if (params[0] == null) {
            return null;
        }

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
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext
                    context) {
                return src == null ? null : new JsonPrimitive(src.getTime());
            }
        });

        Gson gson = builder.create();

        String requestJson = gson.toJson(params[0]);

        Log.d(TAG, "requestJson: " + requestJson);

        StringBuilder stringBuilder = new StringBuilder();

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        HttpClient httpClient = new DefaultHttpClient(httpParams);

//        HttpPost httpPost = new HttpPost("http://199.83.221.130/blood/geofence/update");
        HttpPost httpPost = new HttpPost("http://10.0.2.2:8080/blood/geofence/update");
        ByteArrayEntity postEntity = new ByteArrayEntity(requestJson.getBytes());
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(postEntity);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
            } else {
                Log.d(TAG, "Failed on geofence update JSON post: http status = " + statusCode);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed on geofence update JSON post: " + e.getLocalizedMessage());
        }

//        {"id":6,"deviceId":"APA91bGq5BcyGmZTyvpIt7odBRnCcBCp-REgnOfwY6BbNgzT6p2Espics5xxpQgcOWPHkSHvjNy99UNkC6xF4f-3NsdFtz4WDNFhJAAmaZdDXWUmvkgcN3qepc4W1fk9i6imE-_J39b3QgsT5rVXcx5Wqfjec0v70O0kzbU9qaRzAc5YTn5PAkU","latitude":37.380867,"longitude":-122.086945,"radius":1000,"createTime":1406491569784,"exitTime":null,"version":0}
        Log.d(TAG, "JSON returned: " + stringBuilder.toString());
        GeoFenceUpdateResponse response = gson.fromJson(stringBuilder.toString(), GeoFenceUpdateResponse.class);
        Log.d(TAG, "response:" + response);

        recordGeoFence(response);

        return null;
    }

    @Override
    protected void onPostExecute(GeoFence geoFence) {
        super.onPostExecute(geoFence);
    }

    private void recordGeoFence(GeoFenceUpdateResponse geoFenceUpdateResponse) {
        ContentValues values = new ContentValues();
        values.put(GEOFENCES_COLUMN_LATITUDE, geoFenceUpdateResponse.getLatitude());
        values.put(GEOFENCES_COLUMN_LONGITUDE, geoFenceUpdateResponse.getLongitude());
        values.put(GEOFENCES_COLUMN_RADIUS, geoFenceUpdateResponse.getRadius());
        values.put(GEOFENCES_COLUMN_CREATE_TIME, geoFenceUpdateResponse.getCreateTime().getTime());
        values.put(GEOFENCES_COLUMN_NAME, "GeoFence " + geoFenceUpdateResponse.getId());

        App.context.getContentResolver().insert(GeoFenceContentProvider.GEOFENCE_CONTENT_URI, values);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class GeoFenceUpdateRequest {
        @Expose
        private String deviceId;
        @Expose
        private double latitude;
        @Expose
        private double longitude;
        @Expose
        private Date timestamp;

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "GeoFenceUpdateRequest{" +
                    "deviceId='" + deviceId + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class GeoFenceUpdateResponse extends GeoFenceUpdateRequest {
        @Expose
        private long id;
        @Expose
        private int radius;
        @Expose
        private Date createTime;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        @Override
        public String toString() {
            return "GeoFenceUpdateResponse{" +
                    "request=" + super.toString() +
                    "id=" + id +
                    ", radius=" + radius +
                    ", createTime=" + createTime +
                    '}';
        }
    }
}
