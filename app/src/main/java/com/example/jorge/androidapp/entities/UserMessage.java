package com.example.jorge.androidapp.entities;

import com.example.jorge.androidapp.framework.Utils;

import java.io.Serializable;

public class UserMessage implements Serializable {

    public static  int MESSAGE_SENT = 1;
    public static  int MESSAGE_RECEIVED = 2;

    private byte[] message;
    private String user;
    private long timeStamp;
    private int type;
    private boolean isEncrypted = false;

    public UserMessage(String user, long timeStamp){
        this.timeStamp = timeStamp;
        this.user = Utils.getUserNameFromNetPacket(user);
    }
    public UserMessage(byte[] message , String user, long timeStamp){
        this.message = message;
        this.timeStamp = timeStamp;
        this.user = Utils.getUserNameFromNetPacket(user);
    }


    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] mensaje) {
        this.message = mensaje;
    }

    public String getUser() {
        return user;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

}
