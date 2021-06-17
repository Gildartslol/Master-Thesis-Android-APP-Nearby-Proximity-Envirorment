package com.example.jorge.androidapp.network.petition.serializeObjects;

import java.io.Serializable;

public class ChatInfo extends AbstractInfo implements Serializable {

    private byte[] userMessage;

    public ChatInfo(int status) {
        super(status);

    }

    public byte[] getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(byte[] userMessage) {
        this.userMessage = userMessage;
    }
}
