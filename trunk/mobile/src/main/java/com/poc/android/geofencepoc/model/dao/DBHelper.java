package com.poc.android.geofencepoc.model.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.poc.android.geofencepoc.App;
import com.poc.android.geofencepoc.GeoFenceCreateAsyncTask;

import static com.poc.android.geofencepoc.contentprovider.GeoFenceContentProvider.GEOFENCE_CONTENT_URI;

/**
 * {@link android.database.sqlite.SQLiteOpenHelper} for our {@link com.poc.android.geofencepoc.model.GeoFence} table
  *  <p>
 * Below are example commands (OSX) to access the database of a device with BloodHound installed
 * <p>
 * <pre>
 * {@code
 * $ adb -s 10.0.1.28:5555 backup -f data.ab -noapk com.poc.android.geofencepoc
 * $ dd if=data.ab bs=1 skip=24 | python -c "import zlib,sys;sys.stdout.write(zlib.decompress(sys.stdin.read()))" | tar -xvf -
 * $ sqlite3 apps/com.poc.android.geofencepoc/db/geofences.db
  * sqlite> select * from geofences;
 * }
 * </pre>
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    private static final String DATABASE_NAME = "geofences";
    private static final int DATABASE_VERSION = 4;

    // geofences table
    public static final String TABLE_GEOFENSES = "geofences";
    public static final String GEOFENCES_COLUMN_ID = "_id";
    public static final String GEOFENCES_COLUMN_NAME = "name";
    public static final String GEOFENCES_COLUMN_LATITUDE = "latitude";
    public static final String GEOFENCES_COLUMN_LONGITUDE = "longitude";
    public static final String GEOFENCES_COLUMN_RADIUS = "radius";
    public static final String GEOFENCES_COLUMN_CREATE_TIME = "createtime";
    public static final String GEOFENCES_COLUMN_ENTER_TIME = "entertime";
    public static final String GEOFENCES_COLUMN_DWELL_TIME = "dwelltime";
    public static final String GEOFENCES_COLUMN_EXIT_TIME = "exittime";

    public static final String[] GEOFENCES_ALL_COLUMNS = {
            GEOFENCES_COLUMN_ID,
            GEOFENCES_COLUMN_NAME,
            GEOFENCES_COLUMN_LATITUDE,
            GEOFENCES_COLUMN_LONGITUDE,
            GEOFENCES_COLUMN_RADIUS,
            GEOFENCES_COLUMN_CREATE_TIME,
            GEOFENCES_COLUMN_ENTER_TIME,
            GEOFENCES_COLUMN_DWELL_TIME,
            GEOFENCES_COLUMN_EXIT_TIME
    };


    public static final String GEOFENCE_DATABASE_CREATE = "create table " + TABLE_GEOFENSES + " (" + GEOFENCES_COLUMN_ID
            + " integer primary key, " + GEOFENCES_COLUMN_NAME + " text not null, " + GEOFENCES_COLUMN_LATITUDE
            + " real not null, " + GEOFENCES_COLUMN_LONGITUDE + " real not null, " + GEOFENCES_COLUMN_RADIUS + " real not null, "
            + GEOFENCES_COLUMN_CREATE_TIME + " integer not null, " + GEOFENCES_COLUMN_ENTER_TIME + " integer, "
            + GEOFENCES_COLUMN_DWELL_TIME + " integer, " + GEOFENCES_COLUMN_EXIT_TIME + " integer);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()");
        db.execSQL(GEOFENCE_DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENSES);
        onCreate(db);
    }

    public void clearGeoFenceData(SQLiteDatabase db) {
        Log.d(TAG, "recreating geofence DB...");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENSES);
        db.execSQL(GEOFENCE_DATABASE_CREATE);
        new GeoFenceCreateAsyncTask(App.context).execute(App.getGcmRegistrationId());
        App.context.getContentResolver().notifyChange(GEOFENCE_CONTENT_URI, null);
    }
}
