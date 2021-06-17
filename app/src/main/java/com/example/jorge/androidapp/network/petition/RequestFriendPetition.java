package com.example.jorge.androidapp.network.petition;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.ui.activities.BaseActivity;
import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.entities.User;
import com.example.jorge.androidapp.framework.nearby.MyNearbyConnectionService;
import com.example.jorge.androidapp.network.petition.serializeObjects.RequestInfo;
import com.example.jorge.androidapp.users.DatabaseResult;
import com.example.jorge.androidapp.users.UsersDataBaseHelper;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.IOException;


public class RequestFriendPetition extends AbstractPeticionNearby {

    public static final int REQUEST_FRIEND_TYPE = 10;
    public static final int REQUEST_FRIEND_START = 11;
    public static final int REQUEST_FRIEND_IMAGE_PROFILE = 12;
    public static final int REQUEST_FRIEND_WAITING = 13;
    public static final int REQUEST_FRIEND_NO_FILE = 14;
    public static final int REQUEST_FRIEND_END_OK = 15;
    public static final int REQUEST_FRIEND_END_TIMEOUT = 16;
    public static final int REQUEST_FRIEND_INTERNAL_ERROR = 17;


    private User user;
    private boolean isDisconnectionOk = false;
    private boolean isConnected = false;
    private boolean connect = false;

    private int timeOutMillisConnection = 30000;
    private int timeOutMillisPetition = 80000;

    private String TAG = "REQUEST_FRIEND -->";


    public RequestFriendPetition(MyNearbyConnectionService service, Endpoint destinationEndpoint) {
        super();
        this.service = service;
        this.user = new User(destinationEndpoint.getName());
        this.destinationEndpoint = destinationEndpoint;
    }

    @Override
    public void startPetition() {
        isStarted = true;
        logI("FRIEND___  START. Soy " + service.getUserName());
        sendUpdateToActivity(REQUEST_FRIEND_TYPE, null);
        if (connect) {
            connectTo(destinationEndpoint);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }


    @Override
    public int getType() {
        return REQUEST_FRIEND_TYPE;
    }


    @Override
    protected void processOutgoingPetitionUpdate(PayloadTransferUpdate payload) {
        //logI(" FRIEND___ : Llega Outgoing payload");
    }

    @Override
    protected int getInternalErrorCode() {
        return REQUEST_FRIEND_INTERNAL_ERROR;
    }

    @Override
    protected void onTimeOut() {
        logI(" FRIEND___  FRIEND___ REQUEST: TIME OUT");
        if (isCompleted() && isDisconnectionOk) {
            sendUpdateToActivity(REQUEST_FRIEND_END_OK, null);
        } else {
            if (!isFailure) {
                logI(" FRIEND___ REQUEST FRIEND : TIMEOUT");
                sendBytesInfo(new RequestInfo(REQUEST_FRIEND_END_TIMEOUT, 0));
                sendEndActivity(TIMEOUT);
            }
        }
    }

    @Override
    public void onEndpointLost(Endpoint endpoint) {
        if (!isCompleted()) {
            isFailure = true;
            onFailurePetition(getContext().getString(R.string.dialog_on_failure));
        }
    }

    @Override
    public void onEndpointDisconnected(String endpoint) {
        if (!isCompleted()) {
            isFailure = true;
            onFailurePetition(getContext().getString(R.string.dialog_on_disconnection));
        }
    }

    @Override
    public boolean isPayloadFromPetition(long payloadID) {
        if (payloadID == REQUEST_FRIEND_TYPE || payloadID == REQUEST_FRIEND_START
                || payloadID == REQUEST_FRIEND_IMAGE_PROFILE
                || payloadID == REQUEST_FRIEND_WAITING || payloadID == REQUEST_FRIEND_NO_FILE || payloadID == REQUEST_FRIEND_END_TIMEOUT
                || payloadID == REQUEST_FRIEND_END_OK || payloadID == REQUEST_FRIEND_INTERNAL_ERROR) {
            return true;
        }
        return false;
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
                byte[] bytes = payload.asBytes();
                RequestInfo info = SerializationUtils.deserialize(bytes);
                this.actualInfo = info;
                code = info.getStatus();
                this.setStep(code);
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    if (code == REQUEST_FRIEND_TYPE) {
                        logI(" FRIEND___ REQUEST-TYPE.");
                        createByteRequestInfo(REQUEST_FRIEND_START, 1);
                        sendProfile();
                    } else if (code == REQUEST_FRIEND_START) {
                        logI(" FRIEND___ START");
                        createByteRequestInfo(REQUEST_FRIEND_START, 1);
                        sendProfile();
                    } else if (code == REQUEST_FRIEND_NO_FILE) {
                        logI(" FRIEND___ NO_FILE");
                        writeUser();
                    } else if (code == REQUEST_FRIEND_END_OK) {
                        isDisconnectionOk = true;
                        logI(" FRIEND___ FRIEND OK : OK");
                        if (isCompleted())
                            sendEndActivity(REQUEST_FRIEND_END_OK);
                    } else if (code == REQUEST_FRIEND_END_TIMEOUT) {
                        logI(" FRIEND___ FRIEND TIMEOUT ON the OTHER DEVICE");
                        onTimeOut();
                        service.disconnect(destinationEndpoint);
                        sendEndActivity(TIMEOUT);
                    }
                }
                break;
            case Payload.Type.FILE:
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    logI("FRIEND___ FILE : ARCHIVO RECIBIDO SUCCESS");
                    File file = payload.asFile().asJavaFile();
                    byte[] totalBytes = null;
                    try {
                        totalBytes = Utils.getBytesFromFile(file);
                    } catch (IOException e) {
                        logE("FRIEND ___ Error en archivo recibido.", e);
                        totalBytes = new byte[0];
                    }
                    user.setUserImage(totalBytes);
                    writeUser();

                } else if (update.getStatus() == PayloadTransferUpdate.Status.IN_PROGRESS) {
                    int percentTransferred =
                            (int) (100.0 * (update.getBytesTransferred() / (double) update.getTotalBytes()));
                    logI("FRIEND___ Transferido. %s  .%s/%s", String.valueOf(percentTransferred), String.valueOf(update.getBytesTransferred()), String.valueOf(update.getTotalBytes()));
                    sendUpdateToActivity(REQUEST_FRIEND_IMAGE_PROFILE, update);
                } else if (update.getStatus() == PayloadTransferUpdate.Status.FAILURE) {
                    sendFailedToActivity();
                }
                break;
        }

    }

    public void onConnected() {
        isConnected = true;
        if (!isTimeOut) {
            createByteRequestInfo(REQUEST_FRIEND_TYPE, 1);
            setTimeOut(timeOutMillisPetition);
        } else {
            onTimeOut();
        }
    }

    private void connectTo(Endpoint endpoint) {
        endpoint.setType(Endpoint.TYPE_SEND_REQUEST_FRIEND);
        if (service.getDiscoveredEndpoints().contains(endpoint)) {
            service.connectToEndpoint(endpoint);
            final int interval = timeOutMillisConnection;
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                public void run() {
                    if (!isConnected && !isCompleted()) {
                        isTimeOut = true;
                        onTimeOut();
                    }
                }
            };
            handler.postAtTime(runnable, System.currentTimeMillis() + interval);
            handler.postDelayed(runnable, interval);
        }
    }


    private void sendProfile() {

        String stringUri = Utils.getUriProfile((BaseActivity) service.getmNearbyPetitionListener());
        if (stringUri.equals("")) {
            createByteRequestInfo(REQUEST_FRIEND_NO_FILE);
        } else {
            File f = new File(stringUri, "profile.jpg");
            logI("Se envia el fichero con tamaño %s", f.length());
            Uri uri = Uri.fromFile(f);
            sendFile(uri, REQUEST_FRIEND_IMAGE_PROFILE);
        }
    }

    private void writeUser() {
        logI(" FRIEND___ ESCRIBO EN BBDD");
        if (!isCompleted()) {
            setIsCompleted();
            sendUpdateToActivity(REQUEST_FRIEND_WAITING, null);
            new WriteUpdateUser(this).execute(getContext());
            //if (isDisconnectionOk)
            sendEndActivity(REQUEST_FRIEND_END_OK);
        }
    }

    public boolean isDisconnectionOk() {
        return isDisconnectionOk;
    }


    private void createByteRequestInfo(int status) {
        createByteRequestInfo(status, -1);
    }

    private void createByteRequestInfo(int status, int nfiles) {
        RequestInfo info = new RequestInfo(status, nfiles);
        sendBytesInfo(info);
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    protected User getUser() {
        return user;
    }

    private static class WriteUpdateUser extends AsyncTask<Object, Object, DatabaseResult> {
        RequestFriendPetition request;

        WriteUpdateUser(RequestFriendPetition pet) {
            request = pet;
        }

        @Override
        protected DatabaseResult doInBackground(Object... params) {
            UsersDataBaseHelper userBBdd = new UsersDataBaseHelper(
                    (Context) params[0]);
            DatabaseResult result = null;
            User user = request.getUser();
            if (userBBdd.getUser(user.getUserID()) == null) {
                result = userBBdd.insertFriend(user);
                request.createByteRequestInfo(REQUEST_FRIEND_END_OK);
            } else {
                result = userBBdd.updateUser(user);
            }
            userBBdd.close();
            return result;
        }

        @Override
        protected void onPostExecute(DatabaseResult result) {
            if (result != null && request != null) {
                //request.sendUpdateToActivity(REQUEST_FRIEND_END_OK,null);
            }
        }
    }

}

