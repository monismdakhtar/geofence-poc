package com.poc.android.geofencepoc;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    public static Context context;

    /**
     * Here we make a statically scoped public ApplicationContext available.
     */
    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}