package com.poc.android.geofencepoc.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.poc.android.geofencepoc.model.dao.DBHelper;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static com.poc.android.geofencepoc.model.dao.DBHelper.*;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_CREATE_TIME;

public class GeoFenceContentProvider extends ContentProvider {
    private static final String TAG = "GeoFenceContentProvider";
    public GeoFenceContentProvider() { }

    private DBHelper dbHelper;

    // used for the UriMatcher
    private static final int GEOFENCES = 10;
    private static final int GEOFENCES_ID = 20;

    private static final String GEOFENCE_PATH = "geofence";

    public static final String AUTHORITY = "com.poc.android.geofencepoc.contentprovider";
    public static final Uri GEOFENCE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + GEOFENCE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, GEOFENCE_PATH, GEOFENCES);
        sURIMatcher.addURI(AUTHORITY, GEOFENCE_PATH + "/#", GEOFENCES_ID);

    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate()");
        dbHelper = new DBHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        Uri result;

        long id;

        switch (uriType) {
            case GEOFENCES:
                values.put(GEOFENCES_COLUMN_CREATE_TIME, new Date().getTime());
                id = sqlDB.insert(TABLE_GEOFENSES, null, values);
                getContext().getContentResolver().notifyChange(GEOFENCE_CONTENT_URI, null);
                result = Uri.parse("content://" + AUTHORITY + "/" + GEOFENCE_PATH + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor;
        SQLiteDatabase db;

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case GEOFENCES_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(GEOFENCES_COLUMN_ID + "=" + uri.getLastPathSegment());
            case GEOFENCES:
                checkColumnsGeoFence(projection);
                queryBuilder.setTables(TABLE_GEOFENSES);
                db = dbHelper.getWritableDatabase();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsUpdated;

        switch (uriType) {
            case GEOFENCES:
                rowsUpdated = sqlDB.update(TABLE_GEOFENSES, values, selection, selectionArgs);
                break;
            case GEOFENCES_ID:
                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(TABLE_GEOFENSES, values, GEOFENCES_COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(TABLE_GEOFENSES, values, GEOFENCES_COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // make sure that potential listeners are getting notified
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(GEOFENCE_CONTENT_URI, null);

        return rowsUpdated;
    }

    private void checkColumnsGeoFence(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(GEOFENCES_ALL_COLUMNS));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
