package com.example.jorge.androidapp.network.AsyncPetitions;

import android.os.AsyncTask;

import com.example.jorge.androidapp.network.petition.AbstractPeticionNearby;

public class RequestAsyncPetition extends AsyncTask<AbstractPeticionNearby,Void,Void> {

    @Override
    protected Void doInBackground(AbstractPeticionNearby... petition) {

        AbstractPeticionNearby requestPeti = petition[0];

        return null;
    }



}
