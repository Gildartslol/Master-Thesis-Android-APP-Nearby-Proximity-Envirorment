package com.example.jorge.androidapp.network.petition;

import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.framework.nearby.MyNearbyConnectionService;
import com.example.jorge.androidapp.network.petition.serializeObjects.FileInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileExchangePetition extends AbstractPeticionNearby {


    public static final int FILE_EXCHANGE_TYPE = 30;
    public static final int FILE_EXCHANGE_NAME_OK = 32;
    public static final int FILE_EXCHANGE_IN_PROGRESS = 33;
    public static final int FILE_EXCHANGE_IN_PROGRESS_OUTSIDE = 34;
    public static final int FILE_EXCHANGE_END = 35;
    public static final int FILE_EXCHANGE_ERROR_CANCELED = 39;

    private String fileName;
    private Uri uri;

    private Payload outGoingPayload;
    private ParcelFileDescriptor pfd = null;

    public FileExchangePetition(MyNearbyConnectionService service, Endpoint destinationEndpoint) {
        super();
        this.destinationEndpoint = destinationEndpoint;
        this.service = service;
        this.fileName = "";
        this.setStep(FILE_EXCHANGE_TYPE);
        this.actualInfo = new FileInfo(FILE_EXCHANGE_TYPE);
    }


    @Override
    public int getType() {
        return FILE_EXCHANGE_TYPE;
    }

    @Override
    public void startPetition() {
        isStarted = true;
        if (uri != null) {
            /*Obtenemos el nombre del fichero para enviarlo*/
            fileName = getFileName(uri);

            /*Instanciamos el payload*/
            try {
                pfd = getContext().getContentResolver().openFileDescriptor(uri, "r");
                //outGoingPayload = Payload.fromFile(pfd);
                Payload.File file = Payload.File.zza(pfd);
                outGoingPayload = Payload.zza(file, (long) FileExchangePetition.FILE_EXCHANGE_IN_PROGRESS);
                outgoingFilePayloads.put(outGoingPayload.getId(), outGoingPayload);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                onFailurePetition("");
            }

            FileInfo nameInfo = new FileInfo(FILE_EXCHANGE_TYPE, fileName);
            sendBytesInfo(nameInfo);
            sendUpdateToActivity(FILE_EXCHANGE_TYPE, null);
        } else {
            sendFailedToActivity();
        }
    }

    @Override
    protected String getTag() {
        return "FILE_EXCHANGE____";
    }

    @Override
    public void onFailurePetition(String error) {
        if (pfd != null) {
            try {
                pfd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onFailurePetition(error);
    }


    @Override
    public boolean isPayloadFromPetition(long payloadID) {
        if (payloadID == FileExchangePetition.FILE_EXCHANGE_TYPE || payloadID == FileExchangePetition.FILE_EXCHANGE_IN_PROGRESS
                || payloadID == FileExchangePetition.FILE_EXCHANGE_IN_PROGRESS_OUTSIDE || payloadID == FileExchangePetition.FILE_EXCHANGE_NAME_OK
                || payloadID == FileExchangePetition.FILE_EXCHANGE_END || payloadID == FileExchangePetition.FILE_EXCHANGE_ERROR_CANCELED) {
            return true;
        }
        return false;
    }

    @Override
    protected void processOutgoingPetitionUpdate(PayloadTransferUpdate update) {
        long updateID = update.getPayloadId();
        Payload payload = outgoingFilePayloads.get(updateID);
        int type = -1;
        if (payload != null)
            type = payload.getType();

        switch (type) {
            case Payload.Type.FILE:
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    sendEndActivity(FILE_EXCHANGE_END);
                } else if (update.getStatus() == PayloadTransferUpdate.Status.IN_PROGRESS) {
                    sendUpdateToActivity(FILE_EXCHANGE_IN_PROGRESS_OUTSIDE, update);
                }
                break;
        }
    }

    @Override
    protected int getInternalErrorCode() {
        return FILE_EXCHANGE_ERROR_CANCELED;
    }

    @Override
    public void onCanceled() {
        FileInfo nameInfo = new FileInfo(FILE_EXCHANGE_ERROR_CANCELED, "");
        sendBytesInfo(nameInfo);
        super.onCanceled();
    }

    @Override
    protected void onTimeOut() {
        sendEndActivity(TIMEOUT);
    }


    public void onEndpointLost(Endpoint endpoint) {
        onFailurePetition("Lost connection with device");
    }

    public void onEndpointDisconnected(String endpoint) {
        if (!isCompleted()) {
            isFailure = true;
            onFailurePetition("Request failure. Unexpected disconnection");
        }
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

                    if (code == FILE_EXCHANGE_TYPE) {
                        this.fileName = info.getFileName();
                        logV("START : nombre archivo : " + fileName);
                        FileInfo nameFileOk = new FileInfo(FILE_EXCHANGE_NAME_OK);
                        sendBytesInfo(nameFileOk);
                        sendUpdateToActivity(FILE_EXCHANGE_TYPE, update);
                    }

                    if (code == FILE_EXCHANGE_NAME_OK) {
                        logV("NAME OK : envio achivo");
                        service.sendOne(outGoingPayload, destinationEndpoint.getId());
                        sendUpdateToActivity(FILE_EXCHANGE_NAME_OK, update);
                    }

                    if (code == FILE_EXCHANGE_END) {
                        sendEndActivity(FILE_EXCHANGE_END);
                    }

                    if (code == FILE_EXCHANGE_ERROR_CANCELED) {
                        logI("CANCELED");
                        super.onCanceled();
                    }

                    break;
                }

            case Payload.Type.FILE:
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    logV("FILE : ARCHIVO RECIBIDO SUCCESS");
                    // Get the received file (which will be in the Downloads folder)
                    File payloadFile = payload.asFile().asJavaFile();
                    // Rename the file.
                    payloadFile.renameTo(new File(payloadFile.getParentFile(), fileName));
                    sendUpdateToActivity(FILE_EXCHANGE_END, update);
                    /*Enviamos el terminado al otro*/
                    FileInfo endInfo = new FileInfo(FileExchangePetition.FILE_EXCHANGE_END);
                    sendBytesInfo(endInfo);
                    /*Acabamos nosotros*/
                    sendEndActivity(FILE_EXCHANGE_END);
                } else if (update.getStatus() == PayloadTransferUpdate.Status.IN_PROGRESS) {
                    int percentTransferred =
                            (int) (100.0 * (update.getBytesTransferred() / (double) update.getTotalBytes()));
                    //logI("FILE___ Transferido. %s  .%s/%s", String.valueOf(percentTransferred), String.valueOf(update.getBytesTransferred()), String.valueOf(update.getTotalBytes()));
                    sendUpdateToActivity(FILE_EXCHANGE_IN_PROGRESS, update);
                } else {
                    sendUpdateToActivity(FILE_EXCHANGE_ERROR_CANCELED, update);
                }

        }

    }


    public void setUri(Uri uri) {
        this.uri = uri;
    }


    public String getFileName() {
        return this.fileName;
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

}
