package com.example.jorge.androidapp.framework.nearby;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import timber.log.Timber;

public class NearbyConnectionsService extends Service{

    private String TAG = "APP_SHARE_FILE";
    private MyNearbyConnectionService service = null;

    @Override
    public void onCreate() {
        Timber.tag(TAG);
        Timber.i("NearbyConnectionsService ___ onCreate called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("NearbyConnectionsService ___ onStartCommand executed");
        this.service = MyNearbyConnectionService.getInstance(this);
        return Service.START_STICKY;
    }


    @Override
    public ComponentName startForegroundService(Intent service) {
        Timber.i("NearbyConnectionsService ___ onStartCommand executed");
        this.service = MyNearbyConnectionService.getInstance(this);
        return super.startForegroundService(service);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new MyNearbyBinder();
    }

    public class MyNearbyBinder extends Binder {
        public NearbyConnectionsService getService() {
            return NearbyConnectionsService.this;
        }
    }

    public MyNearbyConnectionService getConnections() {
        return service;
    }

}
