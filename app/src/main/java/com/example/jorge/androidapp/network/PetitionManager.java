package com.example.jorge.androidapp.network;

import android.support.annotation.NonNull;

import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.framework.nearby.MyNearbyConnectionService;
import com.example.jorge.androidapp.network.petition.AbstractPeticionNearby;
import com.example.jorge.androidapp.network.petition.MultiFilesExchange;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.util.ArrayList;
import java.util.HashMap;

public class PetitionManager {

    private HashMap<String, ArrayList<AbstractPeticionNearby>> petitions;
    private MultiFilesExchange multiPetition;
    private MyNearbyConnectionService service;

    public PetitionManager(MyNearbyConnectionService service) {
        this.petitions = new HashMap<>();
        this.service = service;
    }

    public void startPetition(Endpoint endpoint, @NonNull AbstractPeticionNearby petition) {
        if (endpoint != null) {
            String endpointId = endpoint.getId();
            /*Se inicializa el arraylist si es la primera vez que llega una peticion*/
            initilizeEndpoint(endpointId);

            service.logV("START PETITIONN");

            if (petitions.get(endpointId) != null) {
                petitions.get(endpointId).add(petition);
                petition.startPetition();
            }
        } else {
            multiPetition = (MultiFilesExchange) petition;
            multiPetition.startPetition();
        }
    }

    private void initilizeEndpoint(String endpointId) {
        if (petitions.get(endpointId) == null)
            petitions.put(endpointId, new ArrayList<>());
    }


    public void deletePetition(String endpointId, int type) {
        if (endpointId.equals("0")) {
            this.multiPetition = null;
        } else {
            ArrayList<AbstractPeticionNearby> currentPetitions = petitions.get(endpointId);
            AbstractPeticionNearby found = null;
            if (currentPetitions != null) {
                for (AbstractPeticionNearby pet : currentPetitions) {
                    if (pet.getType() == type)
                        found = pet;
                }
                if (found != null) {
                    service.logI("ELIMINADA PETICION " + found.getType());
                    currentPetitions.remove(found);
                }
            }
        }
    }

    public void addPayload(Endpoint endpoint, Payload payload) {
        String endpointId = endpoint.getId();
        long payloadId = payload.getId();
        boolean processed = false;
        payloadId = Utils.getStatusFromPayloadId(payloadId);
        /*si es multipeticion se procesa ys ino se busca*/
        if (String.valueOf(payloadId).startsWith("4")) {
            if (multiPetition == null) {
                multiPetition = new MultiFilesExchange(service, endpoint);
                multiPetition.addPayload(payload);
            } else {
                multiPetition.addPayload(payload);
            }
        } else {

            ArrayList<AbstractPeticionNearby> currentPetitions = petitions.get(endpointId);
            if (currentPetitions != null) {
                for (AbstractPeticionNearby pet : currentPetitions) {
                    if (!processed && pet.isPayloadFromPetition(payloadId)) {
                        processed = true;
                        pet.addPayload(payload);
                    }
                }
            }

            if (!processed) {
                AbstractPeticionNearby petition = createPetition(endpoint, payload.asBytes());
                if (petition != null) {
                    initilizeEndpoint(endpointId);
                    petitions.get(endpointId).add(petition);
                    //currentPetitions = petitions.get(endpointId);
                    //currentPetitions.add(petition);
                    petition.addPayload(payload);
                }
            }
        }


    }


    public void addPayloadUpdate(String endpointId, PayloadTransferUpdate payloadUpdate) {
        long payloadId = Utils.getStatusFromPayloadId(payloadUpdate.getPayloadId());
        boolean processed = false;
        if (String.valueOf(payloadId).startsWith("4")) {
            if (multiPetition != null)
                multiPetition.addUpdatePayload(payloadUpdate);
        } else {
            ArrayList<AbstractPeticionNearby> currentPetitions = petitions.get(endpointId);
            if (currentPetitions != null) {
                for (AbstractPeticionNearby pet : currentPetitions) {
                    if (!processed && pet.isPayloadFromPetition(payloadId)) {
                        processed = true;
                        pet.addUpdatePayload(payloadUpdate);
                    }
                }
            }
        }
    }


    public void addOutgoingPayloadUpdate(String endpointId, PayloadTransferUpdate payloadUpdate) {
        long payloadId = payloadUpdate.getPayloadId();
        boolean processed = false;
        if (String.valueOf(payloadId).startsWith("4")) {
            if (multiPetition != null)
                multiPetition.onOutgoingPayloadUpdate(payloadUpdate);
        } else {
            ArrayList<AbstractPeticionNearby> currentPetitions = petitions.get(endpointId);
            if (currentPetitions != null) {
                for (AbstractPeticionNearby pet : currentPetitions) {
                    if (!processed && pet.isPayloadFromPetition(payloadId)) {
                        processed = true;
                        pet.onOutgoingPayloadUpdate(payloadUpdate);
                    }
                }
            }
        }

    }


    private AbstractPeticionNearby createPetition(Endpoint endpoint, byte[] bytes) {

        AbstractPeticionNearby pet = AbstractPeticionNearby.getInstance(bytes, service, endpoint);
        service.logI("CREADA PETICION " + (pet != null ? pet.getType() : "Fallo"));
        return pet;
    }


    public AbstractPeticionNearby getPetition(String endpointId, int type) {
        if (type == MultiFilesExchange.MULTIFILE_EXCHANGE_TYPE)
            return multiPetition;
        AbstractPeticionNearby result = null;
        ArrayList<AbstractPeticionNearby> currentPetitions = petitions.get(endpointId);
        if (currentPetitions != null) {
            for (AbstractPeticionNearby pet : currentPetitions) {
                if (pet.getType() == type) {
                    result = pet;
                }
            }
        }
        return result;
    }


    public void onEndpointLost(Endpoint endpoint) {
        ArrayList<AbstractPeticionNearby> currentPetitions = petitions.get(endpoint.getId());
        if (currentPetitions != null)
            for (AbstractPeticionNearby pet : currentPetitions) {
                pet.onEndpointLost(endpoint);
            }
    }

    public void onEndpointDisconnected(String endpointID) {
        ArrayList<AbstractPeticionNearby> currentPetitions = petitions.get(endpointID);
        if (currentPetitions != null)
            for (AbstractPeticionNearby pet : currentPetitions) {
                pet.onEndpointDisconnected(endpointID);
            }

    }
}
