package com.example.jorge.androidapp.framework.nearby;

import android.app.Application;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.res.Configuration;
import android.os.Bundle;

import com.example.jorge.androidapp.BuildConfig;
import com.example.jorge.androidapp.framework.background.AppLifeCycleListener;
import com.example.jorge.androidapp.framework.background.MemoryBoss;
import com.example.jorge.androidapp.framework.timber.CrashReportingTree;

import timber.log.Timber;

public class NearbyApplication extends Application {

    public MyNearbyConnectionService service;
    private Bundle bundle;
    private MemoryBoss mMemoryBoss;
    private AppLifeCycleListener lifecycleListener;

    public NearbyApplication() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        this.service = MyNearbyConnectionService.getInstance(this);
        this.bundle = new Bundle();

        /*registramos las llamadas al memoryBoss*/
        mMemoryBoss = new MemoryBoss();
        registerComponentCallbacks(mMemoryBoss);
        /*LifeCycle*/
        lifecycleListener = new AppLifeCycleListener();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleListener);
        /*tratamos excepciones*/
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                //Catch your exception
                // Without System.exit() this will not work.
                System.exit(2);
            }
        });

        /**/
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }
    

    public Bundle getBundle() {
        return bundle;
    }


    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public MemoryBoss getmMemoryBoss() {
        return mMemoryBoss;
    }

    public AppLifeCycleListener getAppLifeCycleListener() {
        return lifecycleListener;
    }


}


