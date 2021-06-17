package com.example.jorge.androidapp.network.petition;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.entities.UserMessage;
import com.example.jorge.androidapp.framework.nearby.MyNearbyConnectionService;
import com.example.jorge.androidapp.network.petition.serializeObjects.ChatInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;

public class Chatpetition extends AbstractPeticionNearby {

    public static final int CHAT_TYPE = 20;

    public static final int CHAT_START = 21;
    public static final int CHAT_ACCEPT = 22;
    public static final int CHAT_REJECT = 23;
    public static final int CHAT_MESSAGE = 24;
    public static final int CHAT_END = 25;
    public static final int CHAT_TIMEOUT = 26;
    public static final int CHAT_ERROR = 29;


    private ChatInfo actualChatInfo;
    private UserMessage actualUserMessage;
    private boolean isConnected = false;
    private int timeOut = 15000;

    public Chatpetition(MyNearbyConnectionService service, Endpoint destinationEndpoint) {
        super();
        this.service = service;
        this.destinationEndpoint = destinationEndpoint;
        actualChatInfo = new ChatInfo(CHAT_TYPE);
    }

    @Override
    public void startPetition() {
        logI("STARTED");
        isStarted = true;
        createByteChatInfo(CHAT_TYPE);
        sendUpdateToActivity(CHAT_START, null);
        setTimeOut(timeOut);
    }

    @Override
    protected String getTag() {
        return "CHAT ____ ";
    }


    @Override
    public boolean isPayloadFromPetition(long payloadID) {
        if (payloadID == Chatpetition.CHAT_TYPE || payloadID == Chatpetition.CHAT_ACCEPT
                || payloadID == Chatpetition.CHAT_REJECT || payloadID == Chatpetition.CHAT_MESSAGE
                || payloadID == Chatpetition.CHAT_END || payloadID == Chatpetition.CHAT_ERROR || payloadID == CHAT_TIMEOUT) {
            return true;
        }
        return false;
    }

    @Override
    protected void processOutgoingPetitionUpdate(PayloadTransferUpdate payload) {

    }

    @Override
    protected int getInternalErrorCode() {
        return CHAT_ERROR;
    }

    @Override
    protected void onTimeOut() {
        if (!isConnected && !isFailure) {
            sendTimeOut();
            isTimeOut = true;
            sendEndActivity(TIMEOUT);
        }
    }

    @Override
    public void onEndpointLost(Endpoint endpoint) {
        logW("ONENDPOINTLOST ");
        onFailurePetition(getContext().getString(R.string.dialog_on_failure));
    }

    public void onEndpointDisconnected(String endpoint) {
        logW("ON DISCONNECTED");
        onFailurePetition(getContext().getString(R.string.dialog_on_disconnection));
    }


    @Override
    public void processIncomingPetitionUpdate(PayloadTransferUpdate update) {

        int code;
        Payload payload = incomingFilePayloads.get(update.getPayloadId());

        int type = -1;
        if (payload != null)
            type = payload.getType();

        switch (type) {

            case Payload.Type.BYTES:
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    byte[] bytes = payload.asBytes();
                    ChatInfo info = SerializationUtils.deserialize(bytes);
                    this.actualChatInfo = info;
                    code = info.getStatus();
                    this.setStep(code);
                    if (code == CHAT_TYPE) {
                        logI("TYPE");
                        sendUpdateToActivity(CHAT_TYPE, update);
                    }
                    if (code == CHAT_REJECT) {
                        logI("REJECT");
                        isFailure = true;
                        sendUpdateToActivity(CHAT_REJECT, update);
                        sendEndActivity(CHAT_TYPE);
                    }
                    if (code == CHAT_ACCEPT) {
                        if (!isTimeOut) {
                            isConnected = true;
                            logI("ACCEPT");
                            sendUpdateToActivity(CHAT_ACCEPT, update);
                        } else {
                            sendTimeOut();
                        }
                    }
                    if (code == CHAT_MESSAGE) {
                        logI("MESSAGE");
                        actualUserMessage = SerializationUtils.deserialize(info.getUserMessage());
                        sendUpdateToActivity(CHAT_MESSAGE, update);
                    }
                    if (code == CHAT_END) {
                        logI("END");
                        sendEndActivity(CHAT_END);
                    }
                    if (code == CHAT_TIMEOUT) {
                        logI("TIMEOUT");
                        sendEndActivity(TIMEOUT);
                    }
                    break;
                }
        }
    }


    public void sendMessage(UserMessage msg) {
        ChatInfo info = new ChatInfo(CHAT_MESSAGE);
        info.setUserMessage(SerializationUtils.serialize(msg));
        sendBytesInfo(info);
    }

    public void sendAccept() {
        createByteChatInfo(CHAT_ACCEPT);
    }

    public void sendReject() {
        createByteChatInfo(CHAT_REJECT);
        sendEndActivity(CHAT_TYPE);
    }

    public void sendEnd() {
        createByteChatInfo(CHAT_END);
    }

    public void sendTimeOut() {
        createByteChatInfo(CHAT_TIMEOUT);
    }


    @Override
    public int getType() {
        return CHAT_TYPE;
    }


    private void createByteChatInfo(int status) {
        ChatInfo info = new ChatInfo(status);
        sendBytesInfo(info);
    }

    public UserMessage getActualUserMessage() {
        return actualUserMessage;
    }
}
