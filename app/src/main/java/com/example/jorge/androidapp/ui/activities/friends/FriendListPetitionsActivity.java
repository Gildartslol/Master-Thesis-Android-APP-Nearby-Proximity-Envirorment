package com.example.jorge.androidapp.ui.activities.friends;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.constantes.KConstantesShareContent;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyPetitionListener;
import com.example.jorge.androidapp.framework.notifications.ApplicationNotificationManager;
import com.example.jorge.androidapp.framework.providers.NearbyDownloadsFileProvider;
import com.example.jorge.androidapp.network.petition.AbstractPeticionNearby;
import com.example.jorge.androidapp.network.petition.Chatpetition;
import com.example.jorge.androidapp.network.petition.FileExchangePetition;
import com.example.jorge.androidapp.network.petition.MultiFilesExchange;
import com.example.jorge.androidapp.network.petition.RequestFriendPetition;
import com.example.jorge.androidapp.ui.activities.BaseActivity;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.File;

public abstract class FriendListPetitionsActivity extends BaseActivity implements MyNearbyPetitionListener {


    protected String TAG = "FRIEND_LIST_ACTIVITY ---> ";

    @Override
    public int getLayoutId() {
        return R.layout.activity_friend_list;
    }

    @Override
    public void setServicesParameters() {
        service.setMyNearbyPetitionListener(this);
    }

    @Override
    protected String getTag() {
        return TAG;
    }


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

    @Override
    public void onPetitionEnd(AbstractPeticionNearby petition) {
        logI("ON PETITION END ");
        Notification notification;

        String nearbyPath;
        File file;
        Uri fileUri;
        Intent myIntent;
        PendingIntent pendingIntent;
        if (petition.getPetitionStep() == AbstractPeticionNearby.TIMEOUT) {
            dialogManager.updateErrorDialog(getString(R.string.dialog_timeout, petition.getEndpointUsername()));
            service.deletePetition(petition.getEndpointID(), petition.getType());
        } else {

            switch (petition.getType()) {

                case (RequestFriendPetition.REQUEST_FRIEND_TYPE):
                    RequestFriendPetition pet = (RequestFriendPetition) petition;
                    logD("FRIEND_ Se va a desconectar " + pet.isDisconnectionOk());
                    service.deletePetition(petition.getEndpointID(), petition.getType());

                    notification = ApplicationNotificationManager.generateNotificationFriendRequestEnd(this, petition.getEndpointUsername());
                    notificationManager.notify(ApplicationNotificationManager.ID_REQUEST, notification);
                    break;
                case (MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE):
                    getBundle().remove(KConstantesShareContent.BUNDLE.IS_MULTI_FILE);
                    service.deletePetition("0", petition.getType());

                    MultiFilesExchange multiPetition = (MultiFilesExchange) petition;
                    nearbyPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Nearby" + "/" + multiPetition.getFileName();
                    file = new File(nearbyPath);
                    fileUri = NearbyDownloadsFileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".fileprovider", file);
                    myIntent = new Intent(Intent.ACTION_VIEW, fileUri);
                    myIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pendingIntent = PendingIntent.getActivity(this, 0, myIntent, Intent.FILL_IN_ACTION);

                    notification = ApplicationNotificationManager.generateNotificationFileDownloaded(this, multiPetition.getFileName(), petition.getEndpointUsername(), pendingIntent);
                    notificationManager.notify(ApplicationNotificationManager.ID_REQUEST, notification);
                    break;
                case (FileExchangePetition.FILE_EXCHANGE_TYPE):
                    updateDialogFileTransfer(petition, null);
                    service.deletePetition(petition.getEndpointID(), petition.getType());

                    FileExchangePetition fileExchangePetition = (FileExchangePetition) petition;
                    nearbyPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Nearby" + "/" + fileExchangePetition.getFileName();
                    file = new File(nearbyPath);
                    fileUri = NearbyDownloadsFileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".fileprovider", file);
                    myIntent = new Intent(Intent.ACTION_VIEW, fileUri);
                    myIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pendingIntent = PendingIntent.getActivity(this, 0, myIntent, Intent.FILL_IN_ACTION);

                    notification = ApplicationNotificationManager.generateNotificationFileDownloaded(this, fileExchangePetition.getFileName(), petition.getEndpointUsername(), pendingIntent);
                    notificationManager.notify(ApplicationNotificationManager.ID_REQUEST, notification);
                    break;
                default:
                    service.deletePetition(petition.getEndpointID(), petition.getType());
            }
        }
    }

    @Override
    public void onPetitionFailure(AbstractPeticionNearby petition) {

    }


    private void updateDialogRequestFriend(AbstractPeticionNearby
                                                   petition, PayloadTransferUpdate update) {

    }

    private void updateDialogChat(AbstractPeticionNearby
                                          petition, PayloadTransferUpdate update) {

    }

    private void updateDialogFileTransfer(AbstractPeticionNearby
                                                  petition, PayloadTransferUpdate update) {

    }

    private void updateDialogMultiFileTransfer(AbstractPeticionNearby
                                                       petition, PayloadTransferUpdate update) {

    }

}
