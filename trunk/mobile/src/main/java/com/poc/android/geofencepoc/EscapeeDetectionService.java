package com.poc.android.geofencepoc;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.poc.android.geofencepoc.model.GeoFence;
import com.poc.android.geofencepoc.model.ModelException;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EscapeeDetectionService extends Service {
    private static final String TAG = "EscapeeDetectionService";

    private static final long TIMER_INTERVAL = 60000L;

    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(" + intent + ")");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(" + intent + ", " + flags + ", " + startId +")");
        startTimer();
        return START_STICKY;
    }

    private void startTimer() {
        if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated()) {
            Log.d(TAG, "thread pool not active, starting new one");
            threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
        }

        if (! isRunning) {
            Log.d(TAG, "isRunning false, scheduling timer thread");
            threadPool.scheduleAtFixedRate(new Thread(){ public void run() {onTimerTick();}}, 1000L, TIMER_INTERVAL, TimeUnit.MILLISECONDS);
            isRunning = true;
        } else {
            Log.d(TAG, "service thread already scheduled");
        }
    }

    private void onTimerTick() {
        Log.d(TAG, "tick");

        GeoFence currentGeoFence;

        try {
            currentGeoFence = GeoFence.findLatestGeoFence();
        } catch (ModelException e) {
            Log.e(TAG, "error finding last geofence: " + e.getLocalizedMessage());
            return;
        }

        Log.d(TAG, "current geofence : " + currentGeoFence);

        if (currentGeoFence.getEnterTime() == null) {

        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged(" + newConfig + ")");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory()");
        super.onLowMemory();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTaskRemoved(" + level + ")");
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind(" + intent + ")");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind(" + intent + ")");
        super.onRebind(intent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved(" + rootIntent + ")");
        super.onTaskRemoved(rootIntent);
    }

}

