package com.example.jorge.androidapp.ui.activities;

import android.os.Bundle;

import com.example.jorge.androidapp.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void setServicesParameters() {

    }

    @Override
    public String getTag() {
        return null;
    }
}
