package com.example.jorge.androidapp.network.petition;

import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.framework.nearby.MyNearbyConnectionService;
import com.example.jorge.androidapp.network.petition.serializeObjects.FileInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.util.ArrayList;

public class MultiFilesExchange extends AbstractPeticionNearby {

    public static final int MULTIFILE_EXCHANGE_TYPE = 40;
    public static final int MULTIFILE_EXCHANGE_ON_CONNECT = 41;
    public static final int MULTIFILE_EXCHANGE_CONNECTED = 42;
    public static final int MULTIFILE_EXCHANGE_PROCESSING = 43;
    public static final int MULTIFILE_EXCHANGE_NAME_OK = 44;
    public static final int MULTIFILE_EXCHANGE_FILE_RECEIVED_END = 45;
    public static final int MULTIFILE_EXCHANGE_FILE_RECEIVED_ERROR = 46;
    public static final int MULTIFILE_EXCHANGE_FILE_END_PETITION = 47;
    public static final int MULTIFILE_EXCHANGE_ERROR_NO_ENDPOINTS = 48;
    public static final int MULTIFILE_EXCHANGE_ERROR = 49;


    private ArrayList<Endpoint> destinationEndpoints;
    private String fileName;
    private Uri uri;


    private ParcelFileDescriptor pfd = null;
    private ArrayList<Endpoint> timeouts;


    public MultiFilesExchange(MyNearbyConnectionService service, ArrayList<Endpoint> destinationEndpoints) {
        super();
        this.destinationEndpoints = destinationEndpoints;
        this.service = service;
        this.setStep(MULTIFILE_EXCHANGE_TYPE);
        this.actualInfo = new FileInfo(MULTIFILE_EXCHANGE_TYPE);
        this.timeouts = new ArrayList<>();
    }

    public MultiFilesExchange(MyNearbyConnectionService service, Endpoint destinationEndpoint) {
        super();
        this.destinationEndpoint = destinationEndpoint;
        this.service = service;
        this.setStep(MULTIFILE_EXCHANGE_TYPE);
        this.actualInfo = new FileInfo(MULTIFILE_EXCHANGE_TYPE);
    }

    @Override
    public int getType() {
        return MULTIFILE_EXCHANGE_TYPE;
    }

    @Override
    public void startPetition() {
        isStarted = true;
        if (destinationEndpoints != null) {
            if (destinationEndpoints.size() > 0) {
                if (uri != null) {
                    /*Obtenemos el nombre del fichero para enviarlo*/
                    fileName = getFileName(uri);
                    /*Instanciamos el payload*/
                    try {
                        pfd = getContext().getContentResolver().openFileDescriptor(uri, "r");
                        Payload.File file = Payload.File.zza(pfd);
                        Payload outGoingPayload = Payload.zza(file, (long) MultiFilesExchange.MULTIFILE_EXCHANGE_PROCESSING);
                        outgoingFilePayloads.put(outGoingPayload.getId(), outGoingPayload);

                        Endpoint destination = destinationEndpoints.get(0);
                        destination.setType(Endpoint.TYPE_MULTI_EXCHANGE);
                        destinationEndpoint = destination;
                        logV("MULTI FILE : CONNECTING TO : " + destination.getName());

                        if (service.getConnectedEndpoints().contains(destination)) {
                            onConnectedEndpoint(destination);
                        } else {
                            sendUpdateToActivity(MULTIFILE_EXCHANGE_ON_CONNECT, null);
                            service.connectToEndpoint(destinationEndpoint);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailurePetition("");
                    }
                } else {
                    onFailurePetition("");
                }
            } else {
                onFailurePetition("No near friends to send");
            }
        }
    }

    @Override
    protected String getTag() {
        return "MultiFilesExchange";
    }

    private void sendConnectNext() {
        if (destinationEndpoints.size() > 0) {
            Endpoint destination = destinationEndpoints.get(0);
            destinationEndpoint = destination;
            if (service.getConnectedEndpoints().contains(destination)) {
                onConnectedEndpoint(destination);
            } else {
                sendUpdateToActivity(MULTIFILE_EXCHANGE_ON_CONNECT, null);
                connectTo(destinationEndpoints.get(0));
            }
        } else {
            sendEndActivity(MultiFilesExchange.MULTIFILE_EXCHANGE_FILE_END_PETITION);
        }
    }

    private void connectTo(Endpoint endpoint) {
        endpoint.setType(Endpoint.TYPE_MULTI_EXCHANGE);
        service.connectToEndpoint(endpoint);
        final int interval = 10000;
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                if (!service.getConnectedEndpoints().contains(endpoint)) {
                    timeouts.add(endpoint);
                    sendEndActivity(TIMEOUT);
                    sendConnectNext();
                }
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);
    }

    public void onConnectedEndpoint(Endpoint endpoint) {
        if (timeouts.contains(endpoint)) {
            service.disconnect(endpoint);
        } else {
            String endpointID = endpoint.getId();
            if (destinationEndpoints.size() > 0) {
                if (destinationEndpoints.get(0).getId().equals(endpointID)) {
                    destinationEndpoint = destinationEndpoints.get(0);
                    sendUpdateToActivity(MULTIFILE_EXCHANGE_CONNECTED, null);
                    FileInfo nameInfo = new FileInfo(MULTIFILE_EXCHANGE_TYPE, fileName);
                    sendBytesInfo(nameInfo, endpointID);
                } else {
                    onFailurePetition("Not matching endpoints");
                }
            }
            destinationEndpoints.remove(0);
        }
    }

    @Override
    public boolean isPayloadFromPetition(long payloadID) {
        if (payloadID == MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE || payloadID == MultiFilesExchange.MULTIFILE_EXCHANGE_NAME_OK
                || payloadID == MultiFilesExchange.MULTIFILE_EXCHANGE_PROCESSING || payloadID == MultiFilesExchange.MULTIFILE_EXCHANGE_FILE_RECEIVED_END
                || payloadID == MultiFilesExchange.MULTIFILE_EXCHANGE_FILE_RECEIVED_ERROR || payloadID == MultiFilesExchange.MULTIFILE_EXCHANGE_ERROR_NO_ENDPOINTS
                || payloadID == MultiFilesExchange.MULTIFILE_EXCHANGE_ERROR) {
            return true;
        }
        return false;
    }

    @Override
    protected void processIncomingPetitionUpdate(PayloadTransferUpdate update) {

        int code;
        Payload payload = incomingFilePayloads.get(update.getPayloadId());

        int type = -1;
        if (payload != null)
            type = payload.getType();

        switch (type) {

            case Payload.Type.BYTES:
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    byte[] bytes = payload.asBytes();
                    FileInfo info = SerializationUtils.deserialize(bytes);
                    this.actualInfo = info;
                    code = info.getStatus();
                    this.setStep(code);

                    if (code == MULTIFILE_EXCHANGE_TYPE) {
                        this.fileName = info.getFileName();
                        logV("MULTI_FILE START : nombre archivo : " + fileName);
                        FileInfo nameFileOk = new FileInfo(MULTIFILE_EXCHANGE_NAME_OK);
                        sendBytesInfo(nameFileOk);
                        sendUpdateToActivity(MULTIFILE_EXCHANGE_NAME_OK, update);
                    }

                    if (code == MULTIFILE_EXCHANGE_NAME_OK) {
                        logV("MULTI_FILE NAME OK : envio achivo");
                        sendFile(uri, MultiFilesExchange.MULTIFILE_EXCHANGE_PROCESSING);
                        sendUpdateToActivity(MULTIFILE_EXCHANGE_NAME_OK, update);
                    }

                    if (code == MULTIFILE_EXCHANGE_FILE_RECEIVED_END) {
                        service.disconnect(getDestinationEndpoint());
                        sendUpdateToActivity(MULTIFILE_EXCHANGE_FILE_RECEIVED_END, null);
                        sendConnectNext();
                    }

                    break;
                }

            case Payload.Type.FILE:
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    logV("MULTI_FILE : ARCHIVO RECIBIDO SUCCESS");
                    // Get the received file (which will be in the Downloads folder)
                    File payloadFile = payload.asFile().asJavaFile();
                    // Rename the file.
                    payloadFile.renameTo(new File(payloadFile.getParentFile(), fileName));
                    sendEndActivity(MULTIFILE_EXCHANGE_FILE_RECEIVED_END);
                    /*Terminado*/
                    FileInfo endInfo = new FileInfo(MULTIFILE_EXCHANGE_FILE_RECEIVED_END);
                    sendBytesInfo(endInfo);
                } else if (update.getStatus() == PayloadTransferUpdate.Status.FAILURE) {
                    sendUpdateToActivity(MULTIFILE_EXCHANGE_FILE_RECEIVED_ERROR, update);
                }

        }

    }

    @Override
    protected void processOutgoingPetitionUpdate(PayloadTransferUpdate update) {
        Payload payload = outgoingFilePayloads.get(update.getPayloadId());
        int type = -1;
        if (payload != null)
            type = payload.getType();

        switch (type) {
            case Payload.Type.FILE:
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                } else if (update.getStatus() == PayloadTransferUpdate.Status.IN_PROGRESS) {
                    int percentTransferred =
                            (int) (100.0 * (update.getBytesTransferred() / (double) update.getTotalBytes()));
                    logI("FILE___ Transferido. %s  .%s/%s", String.valueOf(percentTransferred), String.valueOf(update.getBytesTransferred()), String.valueOf(update.getTotalBytes()));
                    sendUpdateToActivity(MULTIFILE_EXCHANGE_PROCESSING, update);
                }
        }

    }

    @Override
    protected int getInternalErrorCode() {
        return MULTIFILE_EXCHANGE_ERROR;
    }

    @Override
    protected void onTimeOut() {

    }

    @Override
    public void onEndpointLost(Endpoint endpoint) {
        sendConnectNext();
    }

    public void onEndpointDisconnected(String endpoint) {
        sendConnectNext();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

}
