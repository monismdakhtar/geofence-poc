package com.poc.android.geofencepoc;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;

import static com.poc.android.geofencepoc.contentprovider.GeoFenceContentProvider.GEOFENCE_CONTENT_URI;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_ALL_COLUMNS;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_CREATE_TIME;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_EXIT_TIME;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_ID;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_LATITUDE;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_LONGITUDE;
import static com.poc.android.geofencepoc.model.dao.DBHelper.GEOFENCES_COLUMN_RADIUS;

public class GeoFenceFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "LocationsFragment";

    private SimpleCursorAdapter adapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_container, container, false);
        fillData();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader()");

        return new CursorLoader(
                getActivity(),
                GEOFENCE_CONTENT_URI,
                GEOFENCES_ALL_COLUMNS,
                null,
                null,
                GEOFENCES_COLUMN_CREATE_TIME + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished()");
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(TAG, "onLoaderReset()");
        adapter.swapCursor(null);
    }

    private void fillData() {
        String[] from = new String[] {
                GEOFENCES_COLUMN_ID,
                GEOFENCES_COLUMN_RADIUS,
                GEOFENCES_COLUMN_LATITUDE,
                GEOFENCES_COLUMN_LONGITUDE,
                GEOFENCES_COLUMN_CREATE_TIME,
                GEOFENCES_COLUMN_EXIT_TIME
        };

        int[] to = new int[] {
                R.id.idTextView,
                R.id.radiusTextView,
                R.id.latitudeTextView,
                R.id.longitudeTextView,
                R.id.createtimeTextView,
                R.id.exittimeTextView
        };

        LoaderManager.enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.listitem_geofence, null, from, to, 0);
        adapter.setViewBinder(new GeoFenceViewBinder());

        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    private class GeoFenceViewBinder implements SimpleCursorAdapter.ViewBinder {
        @SuppressWarnings("UnusedDeclaration")
        private static final String TAG = "GeoFenceViewBinder";

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//            Log.d(TAG, "setViewValue(" + view + ", " + cursor + ", " + columnIndex + ")");

            if (columnIndex == 5 && view instanceof TextView && cursor.getLong(5) > 0) {
                Date date = new Date(cursor.getLong(5));

                ((TextView) view).setText(DateFormat.getInstance().format(date));

                return true;
            }

            if (columnIndex == 6 && view instanceof TextView && cursor.getLong(6) > 0) {
                Date date = new Date(cursor.getLong(6));

                ((TextView) view).setText(DateFormat.getInstance().format(date));

                return true;
            }

            return false;
        }
    }
}
