package com.example.jorge.androidapp.ui.activities.home;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.constantes.KConstantesShareContent;
import com.example.jorge.androidapp.entities.Device;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyPetitionListener;
import com.example.jorge.androidapp.framework.notifications.ApplicationNotificationManager;
import com.example.jorge.androidapp.framework.providers.NearbyDownloadsFileProvider;
import com.example.jorge.androidapp.network.petition.AbstractPeticionNearby;
import com.example.jorge.androidapp.network.petition.Chatpetition;
import com.example.jorge.androidapp.network.petition.FileExchangePetition;
import com.example.jorge.androidapp.network.petition.MultiFilesExchange;
import com.example.jorge.androidapp.network.petition.RequestFriendPetition;
import com.example.jorge.androidapp.ui.activities.chat.ChatActivity;
import com.example.jorge.androidapp.ui.dialogs.CustomDialog;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.File;

public abstract class HomePetitionsActivity extends Home_ActionToggleBar_Activity implements MyNearbyPetitionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    public void setServicesParameters() {
        if (service != null)
            service.setMyNearbyPetitionListener(this);
    }

    public abstract void updateFriendStatus(String idUser);

    /***
     ********************************************************************************
     ********************************************************************************
     *****************************  PETITIONS ***************************************
     ********************************************************************************
     ********************************************************************************
     */

    @Override
    public void onUpdatePetition(AbstractPeticionNearby petition, PayloadTransferUpdate update) {

        switch (petition.getType()) {
            case (RequestFriendPetition.REQUEST_FRIEND_TYPE):
                updateDialogRequestFriend(petition, update);
                break;
            case (Chatpetition.CHAT_TYPE):
                updateDialogChat(petition, update);
                break;
            case (FileExchangePetition.FILE_EXCHANGE_TYPE):
                updateDialogFileTransfer(petition, update);
                break;

            case (MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE):
                updateDialogMultiFileTransfer(petition, update);
                break;

        }

    }

    /*
     * Peticiones acabadas correctamente o timeout
     * */
    @Override
    public void onPetitionEnd(AbstractPeticionNearby petition) {
        logI("ON PETITION END ");

        /*Si es timeout y la petiticion no ha sido elimitada*/
        if (petition.getPetitionStep() == AbstractPeticionNearby.TIMEOUT && service.getPetition(petition.getEndpointID(), petition.getType()) != null) {
            dialogManager.updateErrorDialog(getString(R.string.dialog_timeout, petition.getEndpointUsername()));
            service.deletePetition(petition.getEndpointID(), petition.getType());
        } else if (petition.getPetitionStep() == AbstractPeticionNearby.CANCELED) {
            service.deletePetition(petition.getEndpointID(), petition.getType());
        } else {
            switch (petition.getType()) {

                case (RequestFriendPetition.REQUEST_FRIEND_TYPE):
                    updateDialogRequestFriend(petition, null);
                    RequestFriendPetition pet = (RequestFriendPetition) petition;
                    logD("FRIEND_ Se va a desconectar " + pet.isDisconnectionOk());
                    if (pet.isDisconnectionOk())
                        service.disconnect(petition.getDestinationEndpoint());

                    service.deletePetition(petition.getEndpointID(), petition.getType());
                    break;
                case (MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE):
                    getBundle().remove(KConstantesShareContent.BUNDLE.IS_MULTI_FILE);
                    service.deletePetition("0", petition.getType());
                    updateDialogMultiFileTransfer(petition, null);

                    break;
                case (FileExchangePetition.FILE_EXCHANGE_TYPE):
                    updateDialogFileTransfer(petition, null);
                    service.deletePetition(petition.getEndpointID(), petition.getType());
                    break;
                default:
                    service.deletePetition(petition.getEndpointID(), petition.getType());
            }
        }

    }

    @Override
    public void onPetitionFailure(AbstractPeticionNearby petition) {
        logI("ON PETITION FAILURE ");
        switch (petition.getType()) {
            case (RequestFriendPetition.REQUEST_FRIEND_TYPE):
                service.disconnect(petition.getDestinationEndpoint());
                service.deletePetition(petition.getEndpointID(), petition.getType());
                break;
            case (MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE):
                getBundle().remove(KConstantesShareContent.BUNDLE.IS_MULTI_FILE);
                service.disconnect(petition.getDestinationEndpoint());
                service.deletePetition("0", petition.getType());
                break;
            default:
                service.deletePetition(petition.getEndpointID(), petition.getType());
                break;
        }

        String message = petition.getErrorMessage();
        if (message.equals("")) {
            dialogManager.updateErrorDialog(getString(R.string.dialog_failure));
        } else {
            dialogManager.updateErrorDialog(message);
        }

    }


    private void updateDialogRequestFriend(AbstractPeticionNearby
                                                   petition, PayloadTransferUpdate update) {
        String text;
        int key = petition.getPetitionStep();
        switch (key) {
            case RequestFriendPetition.REQUEST_FRIEND_TYPE:
                text = getString(R.string.friend_request_type);
                dialogManager.updateDialogFriendRequest(text, true, false, 0, false, false);
                break;
            case RequestFriendPetition.REQUEST_FRIEND_START:
                text = getString(R.string.friend_request_start);
                dialogManager.updateDialogFriendRequest(text, true, false, 0, false, false);
                break;
            case RequestFriendPetition.REQUEST_FRIEND_IMAGE_PROFILE:
                int percentTransferred =
                        (int) (100.0 * (update.getBytesTransferred() / (double) update.getTotalBytes()));
                text = getString(R.string.friend_request_profile_percent);
                dialogManager.updateDialogFriendRequest(text, false, true, percentTransferred, false, false);
                break;

            case RequestFriendPetition.REQUEST_FRIEND_WAITING:
                text = getString(R.string.friend_request_done_waiting);
                dialogManager.updateDialogFriendRequest(text, true, false, 0, false, false);
                break;

            case RequestFriendPetition.REQUEST_FRIEND_END_OK:
                text = getString(R.string.friend_request_added);
                dialogManager.updateDialogFriendRequest(text, false, false, 0, true, false);
                updateFriendStatus(petition.getEndpointUserId());

                break;
            case RequestFriendPetition.REQUEST_FRIEND_END_TIMEOUT:
                text = getString(R.string.friend_request_timeOut);
                dialogManager.updateErrorDialog(text);

                break;

            case RequestFriendPetition.REQUEST_FRIEND_INTERNAL_ERROR:
                String message = petition.getErrorMessage();
                dialogManager.updateErrorDialog(message);
                break;
        }

    }

    private void updateDialogChat(AbstractPeticionNearby petition, PayloadTransferUpdate update) {
        String text;
        int key = petition.getPetitionStep();
        switch (key) {
            case Chatpetition.CHAT_START:
                dialogManager.createChatDialog();
                break;
            case Chatpetition.CHAT_TYPE:
                text = getString(R.string.chat_type, petition.getEndpointUsername());
                dialogManager.createChatIncomeDialog(text, petition);
                break;
            case Chatpetition.CHAT_ACCEPT:
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("endpointConnected", petition.getEndpointID());
                dialogManager.getActiveDialog().dismiss();
                startActivity(intent);
                break;
            case Chatpetition.CHAT_REJECT:
                dialogManager.updateErrorDialog(getString(R.string.chat_rejected));
                break;
            case Chatpetition.TIMEOUT:
                dialogManager.updateErrorDialog(getString(R.string.chat_timeout));
                break;
        }


    }

    private void updateDialogFileTransfer(AbstractPeticionNearby petition, PayloadTransferUpdate
            update) {
        String text;
        int key = petition.getPetitionStep();
        FileExchangePetition fileExchangePetition = (FileExchangePetition) petition;
        int percentTransferred = 0;
        switch (key) {
            case FileExchangePetition.FILE_EXCHANGE_TYPE:
                if (petition.isStarterPetition())
                    dialogManager.createFileExchangeDialog();
                break;
            case FileExchangePetition.FILE_EXCHANGE_NAME_OK:

                break;
            case FileExchangePetition.FILE_EXCHANGE_IN_PROGRESS:
                /**
                 percentTransferred =
                 (int) (100 * (update.getBytesTransferred() / (double) update.getTotalBytes()));
                 String filename = ((FileExchangePetition) petition).getFileName();
                 filename = ((FileExchangePetition) petition).getFileName().length() > 10 ? filename.substring(0, 10) : filename;

                 text = getString(R.string.file_exchange_receive, filename, String.valueOf(percentTransferred));
                 dialogManager.updateFileExchangeDialog(text, false, true, percentTransferred, false, false);
                 **/
                break;

            case FileExchangePetition.FILE_EXCHANGE_IN_PROGRESS_OUTSIDE:
                percentTransferred =
                        (int) (100 * (update.getBytesTransferred() / (double) update.getTotalBytes()));

                CustomDialog.CustomOnClickListener listener = new CustomDialog.CustomOnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileExchangePetition.onCanceled();
                        mDialog.dismiss();
                    }
                };

                text = getString(R.string.file_exchange_send, percentTransferred);
                dialogManager.updateFileExchangeDialog(text, false, true, percentTransferred, false, true, listener);

                break;

            case FileExchangePetition.FILE_EXCHANGE_END:
                if (!petition.isStarterPetition()) {
                    String nearbyPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Nearby" + "/" + fileExchangePetition.getFileName();
                    File file = new File(nearbyPath);

                    /*Creamos la notificacion*/
                    Uri fileUri = NearbyDownloadsFileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".fileprovider", file);
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, fileUri);
                    myIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myIntent, Intent.FILL_IN_ACTION);
                    Notification notification = ApplicationNotificationManager.generateNotificationFileDownloaded(this, fileExchangePetition.getFileName(), petition.getEndpointUsername(), pendingIntent);
                    notificationManager.notify(ApplicationNotificationManager.ID_MULTIFILE, notification);
                } else {
                    text = getString(R.string.file_exchange_file_send);
                    dialogManager.updateFileExchangeDialog(text, false, false, 0, true, false, null);
                }
                break;
            case FileExchangePetition.FILE_EXCHANGE_ERROR_CANCELED:


                break;
        }
    }

    private void updateDialogMultiFileTransfer(AbstractPeticionNearby petition, PayloadTransferUpdate
            update) {

        String text;
        int key = petition.getPetitionStep();
        MultiFilesExchange multiPetition = (MultiFilesExchange) petition;
        boolean isServer = getBundle().getBoolean(KConstantesShareContent.BUNDLE.IS_MULTI_FILE);


        switch (key) {
            case MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE:
                break;
            case MultiFilesExchange.MULTIFILE_EXCHANGE_ON_CONNECT:
                if (isServer)
                    dialogManager.createMultiFileExchangeDialog(petition.getEndpointUsername());
                break;
            case MultiFilesExchange.MULTIFILE_EXCHANGE_CONNECTED:
                text = getString(R.string.multi_exchange_connected_to, petition.getEndpointUsername());
                if (isServer)
                    dialogManager.updateMultiFileExchangeDialog(text, true, false, 0, false, false);
                break;
            case MultiFilesExchange.MULTIFILE_EXCHANGE_NAME_OK:
                text = getString(R.string.multi_exchange_sending_file);
                if (isServer)
                    dialogManager.updateMultiFileExchangeDialog(text, true, false, 0, false, false);
                break;
            case MultiFilesExchange.MULTIFILE_EXCHANGE_PROCESSING:
                if (isServer) {
                    int percentTransferred =
                            (int) (100.0 * (update.getBytesTransferred() / (double) update.getTotalBytes()));
                    text = getString(R.string.multi_exchange_sending_file_percent, percentTransferred);
                    dialogManager.updateMultiFileExchangeDialog(text, false, true, percentTransferred, false, false);
                }
                break;
            case MultiFilesExchange.MULTIFILE_EXCHANGE_FILE_RECEIVED_END:
                if (isServer) {
                    Device device = new Device(multiPetition.getDestinationEndpoint());
                    text = getString(R.string.multi_exchange_file_send, device.getUser());
                    dialogManager.updateMultiFileExchangeDialog(text, true, false, 0, false, false);
                } else {

                    /*Creamos la notificacion*/
                    String nearbyPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Nearby" + "/" + multiPetition.getFileName();
                    File file = new File(nearbyPath);
                    Uri fileUri = NearbyDownloadsFileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".fileprovider", file);
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, fileUri);
                    myIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myIntent, Intent.FILL_IN_ACTION);
                    Notification notification = ApplicationNotificationManager.generateNotificationFileDownloaded(this, multiPetition.getFileName(), petition.getEndpointUsername(), pendingIntent);
                    notificationManager.notify(ApplicationNotificationManager.ID_MULTIFILE, notification);
                }


                break;
            case MultiFilesExchange.MULTIFILE_EXCHANGE_FILE_END_PETITION:
                getBundle().remove(KConstantesShareContent.BUNDLE.IS_MULTI_FILE);
                text = getString(R.string.multi_exchange_all_send);
                dialogManager.updateMultiFileExchangeDialog(text, false, false, 0, true, false);
                break;
        }
    }

}
