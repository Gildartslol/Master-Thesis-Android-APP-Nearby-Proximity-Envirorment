package com.example.jorge.androidapp.entities;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.example.jorge.androidapp.constantes.KConstantesShareContent;

import java.io.File;

public class User {

    @NonNull
    private String userID;
    @NonNull
    private String userName;
    @NonNull
    private String userDeviceName;
    @NonNull
    private boolean isBlocked;

    private byte[] userImage;

    public User(@NonNull String userID, @NonNull String userName, @NonNull String userDeviceName, boolean isBlocked) {
        this.userID = userID;
        this.userName = userName;
        this.userDeviceName = userDeviceName;
        this.isBlocked = isBlocked;
    }

    public User(String endPointName) {
        String[] parts = endPointName.split("&");
        this.userDeviceName = (parts[0]);
        this.userName = (parts[1]);
        this.userID = parts[2];
        this.isBlocked = false;
        this.userImage = null;
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDeviceName() {
        return userDeviceName;
    }

    public void setUserDeviceName(String userDeviceName) {
        this.userDeviceName = userDeviceName;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public byte[] getUserImage() {
        return userImage;
    }

    public void setUserImage(byte[] userImage) {
        this.userImage = userImage;
    }
}
