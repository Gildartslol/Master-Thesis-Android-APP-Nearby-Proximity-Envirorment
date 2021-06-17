package com.example.jorge.androidapp.entities;

import com.example.jorge.androidapp.constantes.KConstantesShareContent;

public class Device {


    /*Represents the information of the other device*/
    private String deviceID;
    private String deviceName;
    private String deviceUsername;
    private boolean isFriend;
    private boolean isConnected;

    /*Endpoint parameters*/
    private Endpoint endpoint;
    /*User asociated*/
    private User user;


    public Device(Endpoint endpoint) {
        this.isFriend = false;
        this.isConnected = false;
        this.deviceID = endpoint.getId();
        this.endpoint = endpoint;
        String info = endpoint.getName();
        String[] parts = info.split("&");

        if (parts.length >= 3) {
            this.deviceName = (parts[0]);
            this.deviceUsername = (parts[1]);
            this.deviceID = parts[2];
        } else {
            this.deviceName = KConstantesShareContent.DEFAULT.UNKNOWN;
            this.deviceUsername = KConstantesShareContent.DEFAULT.UNKNOWN;
            this.deviceID = KConstantesShareContent.DEFAULT.UNKNOWN;
        }

    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public String getDeviceID() {

        return this.deviceID;
    }

    public String getDeviceName() {

        return this.deviceName;
    }


    public void setDeviceName(String name) {

        this.deviceName = name;
    }

    public String getDeviceUsername() {
        return deviceUsername;
    }

    public void setDeviceUsername(String deviceUsername) {
        this.deviceUsername = deviceUsername;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Boolean isFriend() {
        return isFriend;
    }

    public void setIsFriend(Boolean isFriend) {
        this.isFriend = isFriend;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean connected) {
        isConnected = connected;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public byte[] getUserProfile() {
        byte[] bytes = null;
        if (user != null)
            bytes = user.getUserImage();
        return bytes;
    }
}
