package com.poc.android.geofencepoc;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ServiceThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(@SuppressWarnings("NullableProblems") Runnable runnable) {
        ThreadFactory wrappedFactory = Executors.defaultThreadFactory();

        Thread thread = wrappedFactory.newThread(runnable);

        thread.setName("BloodHoundServiceThread");

        return thread;
    }

}
