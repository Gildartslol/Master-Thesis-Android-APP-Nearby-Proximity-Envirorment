package com.example.jorge.androidapp.framework.nearby.interfaces;

import com.example.jorge.androidapp.entities.Endpoint;
import com.google.android.gms.nearby.connection.ConnectionInfo;

public interface MyNearbyConnectionListener {
    void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo);
    void onConnectionFailed(Endpoint endpoint);
}
