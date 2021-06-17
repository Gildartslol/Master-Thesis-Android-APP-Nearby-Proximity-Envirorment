package com.example.jorge.androidapp.framework.nearby.interfaces;

import com.example.jorge.androidapp.entities.Endpoint;

public interface MyNearbyEndpointListener {
    void onEndpointDiscovered(Endpoint endpoint);

    void onEndpointConnected(Endpoint endpoint);

    void onEndpointDisconnected(Endpoint endpoint);

    void onEndpointLost(Endpoint endpoint);

    void disconnect(Endpoint endpoint);
}
