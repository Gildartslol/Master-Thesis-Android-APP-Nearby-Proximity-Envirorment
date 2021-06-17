package com.example.jorge.androidapp.framework.nearby.interfaces;

import com.example.jorge.androidapp.network.petition.AbstractPeticionNearby;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public interface MyNearbyPetitionListener {
    void onUpdatePetition(AbstractPeticionNearby petition, PayloadTransferUpdate update);
    void onPetitionEnd(AbstractPeticionNearby petition);
    void onPetitionFailure(AbstractPeticionNearby petition);
}
