package com.example.jorge.androidapp.network.petition.serializeObjects;

import com.google.android.gms.nearby.connection.Payload;

public class FileInfo extends AbstractInfo {

    private String fileName;


    public FileInfo(int status) {
        super(status);

    }

    public FileInfo(int status, String fileName) {
        super(status);
        this.fileName = fileName;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String messageFromUser) {
        this.fileName = messageFromUser;
    }
}
