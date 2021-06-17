package com.example.jorge.androidapp.framework.nearby.interfaces;

import com.example.jorge.androidapp.entities.Endpoint;

public interface MyNearbyClientListener {
    void connectTo(Endpoint endpoint, String type);
}
