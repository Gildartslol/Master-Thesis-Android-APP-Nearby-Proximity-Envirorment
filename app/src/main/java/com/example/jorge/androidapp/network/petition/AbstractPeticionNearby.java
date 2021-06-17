package com.example.jorge.androidapp.network.petition;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.annotation.CallSuper;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.ui.activities.BaseActivity;
import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.framework.nearby.MyNearbyConnectionService;
import com.example.jorge.androidapp.network.petition.serializeObjects.AbstractInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import timber.log.Timber;

public abstract class AbstractPeticionNearby {


    public static final int TIMEOUT = 99999999;
    public static final int CANCELED = 99999991;

    private int step;
    private boolean isCompleted = false;

    protected boolean isFailure = false;
    protected boolean isTimeOut = false;
    protected SimpleArrayMap<Long, Payload> incomingFilePayloads;
    protected SimpleArrayMap<Long, Payload> outgoingFilePayloads;
    protected MyNearbyConnectionService service;
    protected Endpoint destinationEndpoint;
    protected String errorMessage;
    protected AbstractInfo actualInfo;
    protected boolean isStarted = false;
    private String TAG = "APP_SHARE_FILE";

    public static AbstractPeticionNearby getInstance(byte[] bytes, MyNearbyConnectionService service, Endpoint endpoint) {
        if (bytes == null)
            return null;
        AbstractInfo info = SerializationUtils.deserialize(bytes);
        int type = info.getStatus();
        if (type == RequestFriendPetition.REQUEST_FRIEND_TYPE || type == RequestFriendPetition.REQUEST_FRIEND_END_TIMEOUT)
            return new RequestFriendPetition(service, endpoint);
        if (type == Chatpetition.CHAT_TYPE)
            return new Chatpetition(service, endpoint);
        if (type == FileExchangePetition.FILE_EXCHANGE_TYPE)
            return new FileExchangePetition(service, endpoint);
        if (type == MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE)
            return new MultiFilesExchange(service, endpoint);
        return null;
    }

    protected AbstractPeticionNearby() {

        this.outgoingFilePayloads = new SimpleArrayMap<>();
        this.incomingFilePayloads = new SimpleArrayMap<>();
        this.errorMessage = "";
    }


    public abstract int getType();

    public abstract void startPetition();

    protected abstract String getTag();

    protected abstract void processIncomingPetitionUpdate(PayloadTransferUpdate payload);

    protected abstract void processOutgoingPetitionUpdate(PayloadTransferUpdate payload);

    protected abstract int getInternalErrorCode();

    protected abstract void onTimeOut();

    public abstract void onEndpointLost(Endpoint endpoint);

    public abstract void onEndpointDisconnected(String endpoint);

    public abstract boolean isPayloadFromPetition(long payloadID);

    protected void onFailurePetition(String error) {
        this.errorMessage = error;
        this.isFailure = true;
        sendFailedToActivity();
    }

    public void onCanceled(){
        this.isFailure = true;
        sendEndActivity(CANCELED);
    }


    public String getErrorMessage() {
        return errorMessage;
    }


    protected void setStep(int step) {
        this.step = step;
    }


    public int getPetitionStep() {
        return step;
    }


    public void addPayload(Payload payload) {
        /*Si el payload es de los que estoy mandando no no se añade*/
        if (payload != null && !isFailure && this.outgoingFilePayloads.get(payload.getId()) == null)
            this.incomingFilePayloads.put(payload.getId(), payload);
    }


    protected void sendFile(File file, int status) {
        try {
            String ramdom = String.valueOf(UUID.randomUUID().getLeastSignificantBits()).substring(8);
            ramdom = ramdom.concat(String.valueOf(status));
            Payload.File payFile = Payload.File.zza(file, Long.valueOf(ramdom));
            Payload toSend = Payload.zza(payFile,Long.valueOf(ramdom));
            outgoingFilePayloads.put(toSend.getId(), toSend);
            Log.i(TAG, "Envio fichero con id " +ramdom +" y de tipo " + toSend.getType());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    service.sendOne(toSend, destinationEndpoint.getId());
                }
            });

        } catch (Exception e) {
            sendFailedToActivity();
            e.printStackTrace();
        }
    }

    protected void sendFile(Uri uri, int status) {
        ParcelFileDescriptor pfd = null;
        try {
            pfd = getContext().getContentResolver().openFileDescriptor(uri, "r");
            String ramdom = String.valueOf(UUID.randomUUID().getLeastSignificantBits()).substring(8);
            ramdom = ramdom.concat(String.valueOf(status));
            Payload.File file = Payload.File.zza(pfd);
            Payload toSend = Payload.zza(file, Long.valueOf(ramdom));
            outgoingFilePayloads.put(toSend.getId(), toSend);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    service.sendOne(toSend, destinationEndpoint.getId());
                }
            });

        } catch (Exception e) {
            sendFailedToActivity();
            e.printStackTrace();
            try {
                pfd.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    protected void sendBytesSerializable(int status, Serializable object, String endpointId) {
        byte[] bites = SerializationUtils.serialize(object);
        String ramdom = String.valueOf(UUID.randomUUID().getLeastSignificantBits()).substring(8);
        ramdom = ramdom.concat(String.valueOf(status));
        Payload sendPay = Payload.zza(bites, Long.valueOf(ramdom));
        service.sendOne(sendPay, endpointId);
    }

    protected void sendBytesInfo(AbstractInfo info) {
        sendBytesInfo(info, destinationEndpoint.getId());
    }

    protected void sendBytesInfo(AbstractInfo info, String endpointId) {
        byte[] bites = SerializationUtils.serialize(info);
        String ramdom = String.valueOf(UUID.randomUUID().getLeastSignificantBits()).substring(8);
        ramdom = ramdom.concat(String.valueOf(info.getStatus()));
        Payload sendPay = Payload.zza(bites, Long.valueOf(ramdom));
        service.sendOne(sendPay, endpointId);
    }

    private long generateInternalErrorID() {
        String ramdom = String.valueOf(UUID.randomUUID().getLeastSignificantBits()).substring(8);
        ramdom = ramdom.concat(String.valueOf(getInternalErrorCode()));
        return Long.valueOf(ramdom);
    }


    /**
     * Trata el update, si pertenece a uno que este saliendo (enviamos nosotros) lo tratamos como tal. Si no se trata como payload de entrada.
     *
     * @param update
     */
    public void addUpdatePayload(PayloadTransferUpdate update) {

        if (update != null && Utils.getStatusFromPayloadId(update.getPayloadId()) == getInternalErrorCode()) {
            isFailure = true;
            errorMessage = "Internal Error";
            sendFailedToActivity();
        } else {
            try {
                if (update != null && !isFailure) {
                    if (outgoingFilePayloads.get(update.getPayloadId()) != null) {
                        processOutgoingPetitionUpdate(update);
                    } else {
                        processIncomingPetitionUpdate(update);
                    }
                }
            } catch (Exception e) {
                errorMessage = "Internal Error";
                service.send(Payload.zza(new byte[0], generateInternalErrorID()));
                sendFailedToActivity();
                e.printStackTrace();
            }
        }
    }


    public void onOutgoingPayloadUpdate(PayloadTransferUpdate update) {
        if (update != null && !isFailure) {
            processOutgoingPetitionUpdate(update);
        }
    }

    public void sendEndActivity(int status) {
        setStep(status);
        service.getmNearbyPetitionListener().onPetitionEnd(this);
    }

    public void sendUpdateToActivity(int step, PayloadTransferUpdate update) {
        setStep(step);
        service.getmNearbyPetitionListener().onUpdatePetition(this, update);
    }

    protected void sendFailedToActivity() {
        isFailure = true;
        service.getmNearbyPetitionListener().onPetitionFailure(this);
    }

    protected Context getContext() {
        return (BaseActivity) service.getmNearbyPetitionListener();
    }

    public String getEndpointID() {
        return this.destinationEndpoint.getId();
    }

    public String getEndpointUserId() {
        return this.destinationEndpoint.getName().split("&")[2];
    }

    public String getEndpointUsername() {
        return this.destinationEndpoint.getName().split("&")[1];
    }

    public Endpoint getDestinationEndpoint() {
        return destinationEndpoint;
    }

    public boolean isStarterPetition() {
        return isStarted;
    }


    protected void setIsCompleted() {
        this.isCompleted = true;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    protected void setTimeOut(int milliseconds) {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                if (!isCompleted) {
                    onTimeOut();
                }
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + milliseconds);
        handler.postDelayed(runnable, milliseconds);
    }


    @CallSuper
    public void logV(String msg) {
        Timber.tag(TAG);
        Timber.v(getTag() + msg);
    }

    @CallSuper
    public void logD(String msg) {
        Timber.tag(TAG);
        Timber.d(getTag() + msg);
    }

    @CallSuper
    public void logW(String msg) {
        Timber.tag(TAG);
        Timber.w(getTag() + msg);
    }

    @CallSuper
    public void logI(String msg) {
        Timber.tag(TAG);
        Timber.i(getTag() + msg);
    }

    @CallSuper
    public void logI(String msg, Object... args) {
        Timber.tag(TAG);
        Timber.i(getTag() + msg, args);
    }

    @CallSuper
    public void logW(String msg, Throwable e) {
        Timber.tag(TAG);
        Timber.w(e, getTag() + msg);
    }

    @CallSuper
    public void logE(String msg, Throwable e) {
        Timber.tag(TAG);
        Timber.e(e, getTag() + msg);
    }

    @CallSuper
    public void logI(String msg, Throwable e) {
        Timber.tag(TAG);
        Timber.i(e, getTag() + msg);
    }
}

