package com.example.jorge.androidapp.network.petition.serializeObjects;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class AbstractInfo implements Serializable {

    private int status;
    private ArrayList<Long> filePayloads;

    public AbstractInfo(int status) {
        this.status = status;
    }


    public int getStatus() {
        return status;
    }

    public ArrayList<Long> getFilePayloads() {
        return filePayloads;
    }

    public void setFilePayloads(ArrayList<Long> filePayloads) {
        this.filePayloads = filePayloads;
    }
}