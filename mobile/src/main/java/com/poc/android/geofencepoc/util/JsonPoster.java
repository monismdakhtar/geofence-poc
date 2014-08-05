package com.poc.android.geofencepoc.util;

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

public class JsonPoster {
//    private static final String TAG = "JsonPoster";

    public String postJson(String json, String url) throws JsonPosterException {
        StringBuilder stringBuilder = new StringBuilder();

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        HttpPost httpPost = new HttpPost(url);
        ByteArrayEntity postEntity = new ByteArrayEntity(json.getBytes());
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
                throw new JsonPosterException("Failed on geofence update JSON post: http status = " + statusCode);
            }
        } catch (Exception e) {
            throw new JsonPosterException("Failed on geofence update JSON post: " + e.getLocalizedMessage(), e);
        }

        return stringBuilder.toString();
    }

    public static class JsonPosterException extends Throwable {

        public JsonPosterException(String message) {
            super(message);
        }


        public JsonPosterException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
