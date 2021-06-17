package com.example.jorge.androidapp.framework.background;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;

public class AppLifeCycleListener implements LifecycleObserver {

    private boolean isAppInBackGround = false;

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        isAppInBackGround = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        isAppInBackGround = true;
    }

    public boolean isAppInBackGround() {
        return isAppInBackGround;
    }
}
