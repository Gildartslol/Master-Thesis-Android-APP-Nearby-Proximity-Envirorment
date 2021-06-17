package com.example.jorge.androidapp.ui.activities.home;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.adapter.DiscoveryDevicesAdapter;
import com.example.jorge.androidapp.constantes.KConstantesShareContent;
import com.example.jorge.androidapp.entities.Device;
import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.entities.User;
import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyAdvertisingListener;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyClientListener;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyConnectionListener;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyDiscoveringListener;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyEndpointListener;
import com.example.jorge.androidapp.framework.notifications.ApplicationNotificationManager;
import com.example.jorge.androidapp.framework.providers.NearbyDownloadsFileProvider;
import com.example.jorge.androidapp.network.petition.Chatpetition;
import com.example.jorge.androidapp.network.petition.FileExchangePetition;
import com.example.jorge.androidapp.network.petition.MultiFilesExchange;
import com.example.jorge.androidapp.network.petition.RequestFriendPetition;
import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.interfaces.BottomSheetHomeListener;
import com.example.jorge.androidapp.users.UsersDataBaseHelper;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;

public class HomeActivity extends HomePetitionsActivity
        implements NavigationView.OnNavigationItemSelectedListener, MyNearbyEndpointListener, MyNearbyConnectionListener, MyNearbyAdvertisingListener, MyNearbyDiscoveringListener, MyNearbyClientListener, BottomSheetHomeListener {


    @BindView(R.id.recycleViewDevices)
    protected RecyclerView recycleViewDevices;

    @BindView(R.id.switch_advertising_home)
    protected SwitchCompat switch_advertising;

    @BindView(R.id.switch_discovery_home)
    protected SwitchCompat switch_discovering;


    protected UsersDataBaseHelper userBbdd;
    protected String TAG = "HOME_ACTIVITY --->";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Vista de dispositivos y configuración de los listeners*/


        recycleViewDevices.setLayoutManager(new LinearLayoutManager(this));
        recycleViewDevices.setAdapter(devicesAdapter);

        /*Base de datos*/
        userBbdd = new UsersDataBaseHelper(this);

        /*Esperan al servicio*/
        /**
         if (!isMyServiceRunning(NearbyConnectionsService.class)) {
         switch_advertising.setEnabled(false);
         switch_discovering.setEnabled(false);
         }
         **/

    }

    @Override
    protected void onStart() {
        super.onStart();
        logI("On Start Home");
        //logI("Service started " + (service != null));
        setServicesParameters();
        loadDevices();
        service.startAdvertising();

        /*pruebas*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (service != null)
            loadDevices();
    }

    @Override
    public String getTag() {
        return TAG;
    }


    private void loadDevices() {
        for (Endpoint endpoint : service.getDiscoveredEndpoints()) {
            addUpdateDevice(endpoint);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    public void setServicesParameters() {
        if (service != null) {
            service.setMyNearbyEndpointListener(this);
            service.setMyNearbyConnectionListener(this);
            service.setMyNearbyAdvertisingListener(this);
            service.setMyNearbyDiscoveringListener(this);
            service.setMyNearbyPetitionListener(this);
        }
    }

    @Override
    public void updateFriendStatus(String idUser) {
        devicesAdapter.updateFriendStatus(idUser);
    }


    /**
     * Llamado cuando un dispositivo es descubierto y se añade a la lista de dispositivos.
     */
    public void addUpdateDevice(Endpoint endpoint) {
        boolean isConnected = service.getConnectedEndpoints().contains(endpoint);
        /*establecemos si esta conectado*/
        Device device = new Device(endpoint);
        device.setIsConnected(isConnected);
        User user = userBbdd.getUser(device.getDeviceID());
        boolean isFriend = user != null;
        if (isFriend) {
            device.setIsFriend(true);
            device.setUser(user);
        }
        int number = devicesAdapter.setDevice(device);
        if (number != -1) {
            RecyclerView.ViewHolder holder = devicesAdapter.onCreateViewHolder(recycleViewDevices, number);
            devicesAdapter.onBindViewHolder(holder, number);
        }
    }


    /**
     * Llamado cuando un dispositivo es  perdido y se elmina de la lista de dispositivos.
     */
    public void deleteDevice(Endpoint endpoint) {

        Device device = new Device(endpoint);
        int index = devicesAdapter.getPositionById(device.getDeviceID());
        if (index != -1) {
            devicesAdapter.removeAt(index);
        }
    }


    /**
     * LLamado cuando la actividad se va a destruir temporalmente. Salvar cosas importantes o necesarias.
     * invoked when the activity may be temporarily destroyed, save the instance state here
     */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * Descubrimiento de un dispositivo
     */
    @Override
    public void onEndpointDiscovered(Endpoint endpoint) {

        addUpdateDevice(endpoint);
    }


    /**
     * Se desconecta un dispositivo.
     *
     * @param endpoint
     */
    @Override
    public void onEndpointDisconnected(Endpoint endpoint) {
        if (service.getConnectedEndpoints().contains(endpoint))
            service.getConnectedEndpoints().remove(endpoint);
        addUpdateDevice(endpoint);
    }

    /**
     * Se pierde un dispositivo.
     *
     * @param endpoint
     */
    @Override
    public void onEndpointLost(Endpoint endpoint) {
        logI("Perdido el endpoint  " + endpoint.getId());
        deleteDevice(endpoint);
    }

    @Override
    public void disconnect(Endpoint endpoint) {
        addUpdateDevice(endpoint);
    }


    /*Conexion y desconexion*/
    @Override
    public void connectTo(Endpoint endpoint, String type) {
        endpoint.setType(type);
        String username = Utils.getUserNameFromEndpoint(endpoint);
        dialogManager.createConnectionDialog();
        if (service.getDiscoveredEndpoints().contains(endpoint)) {
            service.connectToEndpoint(endpoint);
            if (!type.equals(Endpoint.TYPE_SEND_REQUEST_FRIEND) && !getBundle().getBoolean(KConstantesShareContent.BUNDLE.IS_REQUEST_CLIENT)) {
                final int interval = 15000; // 5 Second
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        if (!service.getConnectedEndpoints().contains(endpoint)) {
                            logI("TimeOut de " + endpoint.getId());
                            dialogManager.updateErrorDialog(getString(R.string.connection_timeout, username));
                            punishTimeoutDevice(endpoint);
                        }
                    }
                };
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);
            }
        } else {
            dialogManager.updateErrorDialogConnecting(username);
        }

    }

    protected void punishTimeoutDevice(Endpoint endpoint) {
        String id = endpoint.getId();
        getBundle().putBoolean(KConstantesShareContent.BUNDLE.PUNISHED + id, true);
        logI("Castigado" + endpoint.getId());
        final int interval = 5000;
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                logI("Sin castigo" + endpoint.getId());
                getBundle().remove(KConstantesShareContent.BUNDLE.PUNISHED + id);
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);
    }


    /**
     * Se inicia una conexion
     *
     * @param endpoint
     * @param connectionInfo
     */
    @Override
    public void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        logI("Conexion iniciada " + endpoint.getName());
        String[] arr = endpoint.getName().split("&");
        String type = arr.length == 4 ? arr[3] : "-1";
        /*Case server*/
        switch (type) {
            case (Endpoint.TYPE_SEND_REQUEST_FRIEND):
                if (isAppInBackground()) {
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.putExtra(KConstantesShareContent.BUNDLE.ENDPOINT_DESTINATION, endpoint);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, KConstantesShareContent.ACTIVITY_RESULT_CODE.NOTIFICATION_REQUEST_CODE, intent, 0);
                    Notification notification = ApplicationNotificationManager.generateNotificationFriendRequest(this, Utils.getUserNameFromEndpoint(endpoint), pendingIntent);
                    notificationManager.notify(ApplicationNotificationManager.ID_REQUEST, notification);
                } else {
                    dialogManager.createFriendRequest(service, endpoint);
                }
                break;
            case (Endpoint.TYPE_ESTABLISH_NORMAL):
                Device dv = new Device(endpoint);
                User user = userBbdd.getUser(dv.getDeviceID());
                if (user != null && !user.isBlocked()) {
                    dialogManager.createIncomingConnectionDialog(service, endpoint);
                } else {
                    service.rejectConnection(endpoint);
                }
                break;
            case (Endpoint.TYPE_MULTI_EXCHANGE):
                service.acceptConnection(endpoint);
                break;
            default:
                if (getBundle().getBoolean(KConstantesShareContent.BUNDLE.PUNISHED + endpoint.getId()))
                    service.rejectConnection(endpoint);
                else
                    service.acceptConnection(endpoint);
                break;
        }
    }


    @Override
    public void onEndpointConnected(Endpoint endpoint) {


        if (getBundle().getBoolean(KConstantesShareContent.BUNDLE.PUNISHED + endpoint.getId())) {
            service.disconnect(endpoint);
        } else {

            String[] arr = endpoint.getName().split("&");
            service.stopDiscovering();
            String type = arr.length == 4 ? arr[3] : "-1";
            switch (type) {
                case (Endpoint.TYPE_SEND_REQUEST_FRIEND):
                    getBundle().putBoolean(KConstantesShareContent.BUNDLE.IS_REQUEST_CLIENT, false);
                    break;
                case (Endpoint.TYPE_ESTABLISH_NORMAL):
                    addUpdateDevice(endpoint);
                    break;
                case (Endpoint.TYPE_MULTI_EXCHANGE):
                    break;
                default:
                    /*El caso en que eres tu, el servidor. Solo te conectas en dos ocasiones, cuando conectas como tal y cuando haces un intercambio multiple*/

                    if (getBundle().getBoolean(KConstantesShareContent.BUNDLE.IS_REQUEST_CLIENT)) {
                        RequestFriendPetition petition = (RequestFriendPetition) service.getPetition(endpoint.getId(), RequestFriendPetition.REQUEST_FRIEND_TYPE);
                        if (petition != null) {
                            petition.onConnected();
                            getBundle().putBoolean(KConstantesShareContent.BUNDLE.IS_REQUEST_CLIENT, false);
                        }
                    } else if (!getBundle().getBoolean(KConstantesShareContent.BUNDLE.IS_MULTI_FILE)) {
                        dialogManager.updateOKConnectingDialog();
                        addUpdateDevice(endpoint);
                    } else {
                        /*Intercambio multiple*/
                        MultiFilesExchange peticion = (MultiFilesExchange) service.getPetition(endpoint.getId(), MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE);
                        if (peticion != null)
                            peticion.onConnectedEndpoint(endpoint);
                    }
                    break;
            }
        }
    }


    @Override
    public void onConnectionFailed(Endpoint endpoint) {
        logW("FALLO EN LA CONEXION");
        if (getBundle().getBoolean(KConstantesShareContent.BUNDLE.IS_REQUEST_CLIENT)) {
        } else if (getBundle().getBoolean(KConstantesShareContent.BUNDLE.IS_MULTI_FILE)) {
            dialogManager.updateTextActive(getString(R.string.multi_exchange_reject, Utils.getUserNameFromEndpoint(endpoint)));
            // updateProgressDialog("Connection rejected by " + Utils.getUserNameFromEndpoint(endpoint));
        } else
            dialogManager.updateFailedConnectingDialog();
    }

    /* *******************************************************************************
     * *******************************************************************************
     * ************************* DISCOVERY AND ADVERTISMENT **************************
     * *******************************************************************************
     * *******************************************************************************
     */


    @Override
    public void onAdvertisingStarted() {
        this.switch_advertising.setChecked(true);
    }

    @Override
    public void onAdvertisingFailed() {

    }

    @Override
    public void onAdvertisingStopped() {
        this.switch_advertising.setChecked(false);
    }

    @Override
    public void onDiscoveryStarted() {
        this.switch_discovering.setChecked(true);
    }

    @Override
    public void onDiscoveryFailed() {

    }

    @Override
    public void onDiscoveryStopped() {
        this.switch_discovering.setChecked(false);
    }


    public UsersDataBaseHelper getUserBbdd() {
        return userBbdd;
    }


    /***
     ********************************************************************************
     ********************************************************************************
     *****************************  BOTTOM SHEET ACTIONS ****************************
     ********************************************************************************
     ********************************************************************************
     */


    @Override
    public void onDialogAddFriend(Device device, int position) {

        //updateProgressDialog("Connecting");
        dialogManager.updateDialogFriendRequest(getString(R.string.friend_request_type), true, false, 0, false, false);
        getBundle().putBoolean(KConstantesShareContent.BUNDLE.IS_REQUEST_CLIENT, true);
        runOnUiThread(new Runnable() {
            public void run() {
                service.startPetition(device.getEndpoint(), RequestFriendPetition.REQUEST_FRIEND_TYPE);
            }
        });
    }

    @Override
    public void onDialogConnect(Device device, int position) {
        runOnUiThread(new Runnable() {
            public void run() {
                connectTo(device.getEndpoint(), Endpoint.TYPE_ESTABLISH_NORMAL);
            }
        });
    }

    @Override
    public void onDialogDisconect(Device device) {

        service.disconnect(device.getEndpoint());
    }

    @Override
    public void onDialogSendFile(Device device, int position) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        getBundle().putSerializable(KConstantesShareContent.BUNDLE.ENDPOINT_DESTINATION, device.getEndpoint());
        startActivityForResult(intent, KConstantesShareContent.ACTIVITY_RESULT_CODE.PICKFILE_REQUEST_CODE);
    }

    @Override
    public void onDialogBlock(Device device, int position) {

    }

    @Override
    public void onDialogOpenChat(Device device, int position) {
        service.startPetition(device.getEndpoint(), Chatpetition.CHAT_TYPE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri fileUri = data.getData();
        Endpoint endpoint;
        switch (requestCode) {
            case KConstantesShareContent.ACTIVITY_RESULT_CODE.PICK_IMAGE_REQUEST_CODE:
                try {

                    String profileDirectory = saveToInternalStorage(fileUri);
                    if (!profileDirectory.equals("")) {
                        writeToPreferences(this.getString(R.string.key_profile_uri), profileDirectory);

                        /*Se pone de perfil*/
                        profilePic.setImageURI(data.getData());
                    }

                } catch (IOException e) {
                    Utils.createToastLong(this, "Error al cargar la imagen");
                }
                break;

            case KConstantesShareContent.ACTIVITY_RESULT_CODE.PICKFILE_REQUEST_CODE:
                if (fileUri != null) {
                    AssetFileDescriptor afd = null;
                    long fileSize = 0;
                    try {
                        afd = getContentResolver().openAssetFileDescriptor(fileUri, "r");
                        if (afd != null)
                            fileSize = afd.getLength();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (afd != null) {
                            try {
                                afd.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    long fileSizeInKB = fileSize / 1024;
                    endpoint = (Endpoint) getBundle().getSerializable(KConstantesShareContent.BUNDLE.ENDPOINT_DESTINATION);
                    if (fileSizeInKB > 10000 && service.getStrategy().equals(Strategy.P2P_STAR)) {
                        dialogManager.createWarningDialog(service, endpoint, fileUri);
                    } else {
                        service.startPetition(endpoint, FileExchangePetition.FILE_EXCHANGE_TYPE, fileUri);
                    }
                }
                break;

            case KConstantesShareContent.ACTIVITY_RESULT_CODE.MULTIFILE_REQUEST_CODE:
                Set<Endpoint> endpointSet = service.getDiscoveredEndpoints();
                ArrayList<Endpoint> receivers = new ArrayList<>();
                for (Endpoint end : endpointSet) {
                    Device dev = new Device(end);
                    User user = userBbdd.getUser(dev.getDeviceID());
                    if (userBbdd != null && !user.isBlocked())
                        receivers.add(end);
                }

                getBundle().putBoolean(KConstantesShareContent.BUNDLE.IS_MULTI_FILE, true);
                service.startPetition(null, MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE, receivers, fileUri);
                break;

            case KConstantesShareContent.ACTIVITY_RESULT_CODE.NOTIFICATION_REQUEST_CODE:
                data.getStringExtra(KConstantesShareContent.BUNDLE.ENDPOINT_DESTINATION);
                endpoint = (Endpoint) data.getSerializableExtra(KConstantesShareContent.BUNDLE.ENDPOINT_DESTINATION);
                dialogManager.createFriendRequest(service, endpoint);
                break;

        }


    }

}

