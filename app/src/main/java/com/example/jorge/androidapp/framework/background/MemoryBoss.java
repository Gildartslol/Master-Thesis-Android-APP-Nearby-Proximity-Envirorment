package com.example.jorge.androidapp.framework.background;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;

public class MemoryBoss implements ComponentCallbacks2 {

    private boolean isAppBackground = false;

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(final int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isAppBackground = true;
        }
        // you might as well implement some memory cleanup here and be a nice Android dev.
    }

    public boolean isAppBackground() {
        return isAppBackground;
    }

    public void setAppBackground(boolean appBackground) {
        isAppBackground = appBackground;
    }
}
