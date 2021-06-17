package com.example.jorge.androidapp.framework.nearby;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.example.jorge.androidapp.constantes.KConstantesShareContent;
import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyAdvertisingListener;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyConnectionListener;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyDiscoveringListener;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyEndpointListener;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyPetitionListener;
import com.example.jorge.androidapp.network.PetitionManager;
import com.example.jorge.androidapp.network.petition.AbstractPeticionNearby;
import com.example.jorge.androidapp.network.petition.Chatpetition;
import com.example.jorge.androidapp.network.petition.FileExchangePetition;
import com.example.jorge.androidapp.network.petition.MultiFilesExchange;
import com.example.jorge.androidapp.network.petition.RequestFriendPetition;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class MyNearbyConnectionService {


    private static MyNearbyConnectionService instance;
    private String name = "";

    private static final String TAG = "APP_SHARE_FILE";
    private String SERVICE_ID = "com.example.jorge.androidapp";

    /**
     * Our handler to Nearby Connections.
     */
    private ConnectionsClient mConnectionsClient;
    private Context context;

    private final Map<String, Endpoint> mDiscoveredEndpoints = new HashMap<>();
    private final Map<String, Endpoint> mPendingConnections = new HashMap<>();
    private final Map<String, Endpoint> mEstablishedConnections = new HashMap<>();
    private final PetitionManager manager;
    private boolean mIsConnecting = false;
    private boolean mIsDiscovering = false;
    private boolean mIsAdvertising = false;
    private Strategy strategy = Strategy.P2P_STAR;


    public MyNearbyConnectionService(Context context) {
        this.context = context;
        this.mConnectionsClient = Nearby.getConnectionsClient(context);
        this.manager = new PetitionManager(this);
        Timber.tag(TAG);
    }

    public static MyNearbyConnectionService getInstance(Context context) {
        if (instance == null) {
            synchronized (MyNearbyConnectionService.class) {
                if (instance == null) {
                    instance = new MyNearbyConnectionService(context);
                }
            }
        }
        return instance;

    }

    private MyNearbyAdvertisingListener mNearbyAdvertisingListener;
    private MyNearbyDiscoveringListener mNearbyDiscoveringListener;
    private MyNearbyEndpointListener mNearbyEndpointListener;
    private MyNearbyConnectionListener mNearbyConnectionListener;
    private MyNearbyPetitionListener mNearbyPetitionListener;


    public void setMyNearbyAdvertisingListener(MyNearbyAdvertisingListener myNearbyAdvertisingListener) {
        mNearbyAdvertisingListener = myNearbyAdvertisingListener;
    }

    public void setMyNearbyDiscoveringListener(MyNearbyDiscoveringListener myNearbyDiscoveringListener) {
        mNearbyDiscoveringListener = myNearbyDiscoveringListener;
    }


    public void setMyNearbyEndpointListener(MyNearbyEndpointListener mNearbyEndpointListener) {
        this.mNearbyEndpointListener = mNearbyEndpointListener;
    }

    public void setMyNearbyConnectionListener(MyNearbyConnectionListener mNearbyConnectionListener) {
        this.mNearbyConnectionListener = mNearbyConnectionListener;
    }

    public void setMyNearbyPetitionListener(MyNearbyPetitionListener mNearbyPetitionListener) {
        this.mNearbyPetitionListener = mNearbyPetitionListener;
    }

    public MyNearbyPetitionListener getmNearbyPetitionListener() {
        return mNearbyPetitionListener;
    }


    /**
     * Callbacks for connections to other devices.
     */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String endpointId, ConnectionInfo connectionInfo) {
                    logD(
                            String.format(
                                    "onConnectionInitiated(endpointId=%s, endpointName=%s)",
                                    endpointId, connectionInfo.getEndpointName()));
                    Endpoint endpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());
                    mPendingConnections.put(endpointId, endpoint);

                    mNearbyConnectionListener.onConnectionInitiated(endpoint, connectionInfo);

                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    logD(String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result));

                    // We're no longer connecting
                    mIsConnecting = false;
                    if (!result.getStatus().isSuccess()) {
                        logW(
                                String.format(
                                        "Connection failed. Received status %s.",
                                        MyNearbyConnectionService.toString(result.getStatus())));
                        mNearbyConnectionListener.onConnectionFailed(mPendingConnections.remove(endpointId));
                        return;
                    }

                    connectedToEndpoint(mPendingConnections.remove(endpointId));
                }

                @Override
                public void onDisconnected(String endpointId) {
                    if (!mEstablishedConnections.containsKey(endpointId)) {
                        logW("Unexpected disconnection from endpoint " + endpointId);
                        manager.onEndpointDisconnected(endpointId);
                        return;
                    }
                    disconnectedFromEndpoint(mEstablishedConnections.get(endpointId));

                }
            };


    /**
     * Callbacks for payloads (bytes of data) sent from another device to us.
     */
    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, Payload payload) {
                    //logD(String.format("onPayloadReceived(endpointId=%s, payload=%s type=%s )", endpointId, payload.getId(), payload.getType()));
                    if (mEstablishedConnections.get(endpointId) != null)
                        manager.addPayload(mEstablishedConnections.get(endpointId), payload);
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, PayloadTransferUpdate update) {
                    //logD(
                    //        String.format(
                    //                "onPayloadTransferUpdate(endpointId=%s, update=%s payloadID %s)", endpointId, update.getStatus(), update.getPayloadId()));
                    //logD("Trasmitidos/enviados " + update.getBytesTransferred());
                    /*Teoricamente si no esta en establecidos no es una trasmision, sino lo que estamos transfiriendo*/
                    if (mEstablishedConnections.get(endpointId) != null) {
                        manager.addPayloadUpdate(endpointId, update);
                    } else {
                        manager.addOutgoingPayloadUpdate(endpointId, update);
                    }
                }

            };


    public void startAdvertising() {
        mIsAdvertising = true;
        final String localEndpointName = getName();

        AdvertisingOptions.Builder advertisingOptions = new AdvertisingOptions.Builder();
        advertisingOptions.setStrategy(getStrategy());

        mConnectionsClient
                .startAdvertising(
                        localEndpointName,
                        getServiceId(),
                        mConnectionLifecycleCallback,
                        advertisingOptions.build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                logV("Now advertising endpoint " + localEndpointName + getStrategy().toString());
                                mNearbyAdvertisingListener.onAdvertisingStarted();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mIsAdvertising = false;
                                logW("startAdvertising() failed.", e);
                                mNearbyAdvertisingListener.onAdvertisingFailed();
                            }
                        });
    }

    /**
     * Stops advertising.
     */
    public void stopAdvertising() {
        logV("Stop advertising");
        mIsAdvertising = false;
        mConnectionsClient.stopAdvertising();
        mNearbyAdvertisingListener.onAdvertisingStopped();
    }

    /**
     * Returns {@code true} if currently advertising.
     */
    public boolean isAdvertising() {
        return mIsAdvertising;
    }


    /**
     * Accepts a connection request.
     */
    public void acceptConnection(final Endpoint endpoint) {
        mConnectionsClient
                .acceptConnection(endpoint.getId(), mPayloadCallback)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logW("acceptConnection() failed.", e);
                            }
                        });
    }

    /**
     * Rejects a connection request.
     */
    public void rejectConnection(Endpoint endpoint) {
        mConnectionsClient
                .rejectConnection(endpoint.getId())
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logW("rejectConnection() failed.", e);
                            }
                        });
    }

    /**
     * Sets the device to discovery mode. It will now listen for devices in advertising mode. Either
     * or  will be called once we've found
     * out if we successfully entered this mode.
     */
    public void startDiscovering() {
        mIsDiscovering = true;
        mDiscoveredEndpoints.clear();
        logV("Now Discovering endpoints");
        DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder();
        discoveryOptions.setStrategy(getStrategy());
        mConnectionsClient
                .startDiscovery(
                        getServiceId(),
                        new EndpointDiscoveryCallback() {
                            @Override
                            public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                                logD(
                                        String.format(
                                                "onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                                                endpointId, info.getServiceId(), info.getEndpointName()));

                                if (getServiceId().equals(info.getServiceId())) {
                                    Endpoint endpoint = mDiscoveredEndpoints.get(endpointId);
                                    if (endpoint == null) {
                                        Endpoint discovered = new Endpoint(endpointId, info.getEndpointName());
                                        mDiscoveredEndpoints.put(endpointId, discovered);
                                        mNearbyEndpointListener.onEndpointDiscovered(discovered);
                                    }
                                }
                            }

                            @Override
                            public void onEndpointLost(String endpointId) {
                                logD(String.format("onEndpointLost(endpointId=%s)", endpointId));
                                Endpoint endpointLost = mDiscoveredEndpoints.get(endpointId);
                                /*Paramos las peticiones*/
                                manager.onEndpointLost(endpointLost);
                                mDiscoveredEndpoints.remove(endpointId);
                                mNearbyEndpointListener.onEndpointLost(endpointLost);

                            }
                        },
                        discoveryOptions.build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                mNearbyDiscoveringListener.onDiscoveryStarted();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mIsDiscovering = false;
                                logW("startDiscovering() failed.", e);
                                mNearbyDiscoveringListener.onDiscoveryFailed();
                            }
                        });
    }

    /**
     * Stops discovery.
     */
    public void stopDiscovering() {
        mIsDiscovering = false;
        mConnectionsClient.stopDiscovery();
        mNearbyDiscoveringListener.onDiscoveryStopped();
    }

    /**
     * Returns {@code true} if currently discovering.
     */
    public boolean isDiscovering() {
        return mIsDiscovering;
    }


    /**
     * Disconnects from the given endpoint.
     */
    public void disconnect(Endpoint endpoint) {
        if (endpoint != null && mEstablishedConnections.get(endpoint.getId()) != null) {
            mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
            mEstablishedConnections.remove(endpoint.getId());
            mNearbyEndpointListener.disconnect(endpoint);
        } else {
            logV("SE INTENTA DESCONECTAR Y NO EXISTE");
        }
    }

    /**
     * Disconnects from all currently connected endpoints.
     */
    public void disconnectFromAllEndpoints() {
        for (Endpoint endpoint : mEstablishedConnections.values()) {
            mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
        }
        mEstablishedConnections.clear();
    }

    /**
     * Resets and clears all state in Nearby Connections.
     */
    public void stopAllEndpoints() {
        mIsAdvertising = false;
        mIsDiscovering = false;
        mIsConnecting = false;
        mDiscoveredEndpoints.clear();
        mPendingConnections.clear();
        mEstablishedConnections.clear();
    }


    public void connectToEndpoint(final Endpoint endpoint) {
        logV("Sending a connection request to endpoint " + endpoint);
        // Mark ourselves as connecting so we don't connect multiple times
        mIsConnecting = true;

        // Ask to connect
        logV("Name of the endpoint " + getName() + "&" + endpoint.getType());
        mConnectionsClient
                .requestConnection(getName() + "&" + endpoint.getType(), endpoint.getId(), mConnectionLifecycleCallback)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logW("requestConnection() failed.", e);
                                mIsConnecting = false;
                                mNearbyConnectionListener.onConnectionFailed(endpoint);
                            }
                        });
    }

    /**
     * Returns {@code true} if we're currently attempting to connect to another device.
     */
    public final boolean isConnecting() {
        return mIsConnecting;
    }

    public void connectedToEndpoint(Endpoint endpoint) {
        logD(String.format("connectedToEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.put(endpoint.getId(), endpoint);
        mNearbyEndpointListener.onEndpointConnected(endpoint);
    }

    public void disconnectedFromEndpoint(Endpoint endpointDisconnected) {
        logD(String.format("disconnectedFromEndpoint(endpoint=%s)", endpointDisconnected));
        mEstablishedConnections.remove(endpointDisconnected.getId());
        mNearbyEndpointListener.onEndpointDisconnected(endpointDisconnected);
        manager.onEndpointLost(endpointDisconnected);
    }

    /**
     * Returns a list of currently connected endpoints.
     */
    public Set<Endpoint> getDiscoveredEndpoints() {
        return new HashSet<>(mDiscoveredEndpoints.values());
    }

    /**
     * Returns a list of currently connected endpoints.
     */
    public Set<Endpoint> getConnectedEndpoints() {
        return new HashSet<>(mEstablishedConnections.values());
    }

    /**
     * Sends a {@link Payload} to all currently connected endpoints.
     *
     * @param payload The data you want to send.
     */
    public void send(Payload payload) {
        List<String> keys = new ArrayList<>(mEstablishedConnections.keySet());
        sendMultiple(payload, keys);
    }


    /**
     * Sends a {@link Payload} to target connected endpoints.
     *
     * @param payload The data you want to send.
     */
    public void sendOne(Payload payload, String endPointID) {

        mConnectionsClient
                .sendPayload(endPointID, payload)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logW("sendPayload() failed.", e);
                            }
                        });
    }


    /**
     * Sends a {@link Payload} to target connected endpoints.
     *
     * @param payload The data you want to send.
     */
    public void sendMultiple(Payload payload, List<String> endpoints) {

        mConnectionsClient
                .sendPayload(endpoints, payload)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logW("sendPayload() failed.", e);
                            }
                        });
    }


    /**
     * Transforms a {@link Status} into a English-readable message for logging.
     *
     * @param status The current status
     * @return A readable String. eg. [404]File not found.
     */
    public static String toString(Status status) {
        return String.format(
                Locale.ENGLISH,
                "[%d]%s",
                status.getStatusCode(),
                status.getStatusMessage() != null
                        ? status.getStatusMessage()
                        : ConnectionsStatusCodes.getStatusCodeString(status.getStatusCode()));
    }

    /**
     * Returns {@code true} if the app was granted all the permissions. Otherwise, returns {@code
     * false}.
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @CallSuper
    public void logV(String msg) {
        Timber.tag(TAG);
        Timber.v("MyNearbyConnectionsService ____ " + msg);
    }

    @CallSuper
    public void logI(String msg) {
        Timber.tag(TAG);
        Timber.i("MyNearbyConnectionsService ____ " + msg);
    }

    @CallSuper
    public void logD(String msg) {
        Timber.tag(TAG);
        Timber.d("MyNearbyConnectionsService ____ " + msg);
    }

    @CallSuper
    public void logW(String msg) {

        Timber.tag(TAG);
        Timber.w("MyNearbyConnectionsService ____ " + msg);
    }

    @CallSuper
    public void logW(String msg, Throwable e) {
        Timber.tag(TAG);
        Timber.w(e, "MyNearbyConnectionsService ____ " + msg);
    }

    @CallSuper
    public void logE(String msg, Throwable e) {

        Timber.tag(TAG);
        Timber.e(e, "MyNearbyConnectionsService ____ " + msg);
    }

    public String getName() {
        if (!name.equals("")) {
            return name + "&" + getAndroidId();
        }
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model + "&" + getUserName() + "&" + getAndroidId();
        }
    }

    public String getAndroidId() {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public String getUserName() {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        return shared.getString("key_username", KConstantesShareContent.DEFAULT.DEFAULT_DISPLAY_NAME);
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strat){
        this.strategy = strat;
    }


    protected String
    getServiceId() {
        return SERVICE_ID;
    }


    public void startPetition(Endpoint endpoint, int type, Object... values) {

        switch (type) {
            case (RequestFriendPetition.REQUEST_FRIEND_TYPE):
                RequestFriendPetition request = new RequestFriendPetition(this, endpoint);
                request.setConnect(true);
                manager.startPetition(endpoint, request);
                break;
            case (Chatpetition.CHAT_TYPE):
                Chatpetition chatpetition = new Chatpetition(this, endpoint);
                manager.startPetition(endpoint, chatpetition);
                break;
            case (FileExchangePetition.FILE_EXCHANGE_TYPE):
                FileExchangePetition fileExchangePetition = new FileExchangePetition(this, endpoint);
                fileExchangePetition.setUri((Uri) values[0]);
                manager.startPetition(endpoint, fileExchangePetition);
                break;
            case (MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE):
                MultiFilesExchange multiFilesExchange = new MultiFilesExchange(this, (ArrayList<Endpoint>) values[0]);
                multiFilesExchange.setUri((Uri) values[1]);
                manager.startPetition(null, multiFilesExchange);
                break;
        }

    }

    public AbstractPeticionNearby getPetition(String id, int type) {
        return manager.getPetition(id, type);
    }


    public void deletePetition(String endpointID, int type) {

        manager.deletePetition(endpointID, type);
    }

}