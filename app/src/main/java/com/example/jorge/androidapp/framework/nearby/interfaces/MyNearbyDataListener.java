package com.example.jorge.androidapp.framework.nearby.interfaces;

import com.example.jorge.androidapp.entities.Endpoint;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public interface MyNearbyDataListener {
    void onReceive(Endpoint endpoint, Payload payload);
    void onTransferUpdate(String endpointId, PayloadTransferUpdate update);
}
