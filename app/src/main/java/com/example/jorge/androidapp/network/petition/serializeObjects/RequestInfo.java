package com.example.jorge.androidapp.network.petition.serializeObjects;

import java.io.Serializable;

public class RequestInfo extends AbstractInfo implements Serializable {

    private int nFiles;
    private String mensaje;


    public RequestInfo(int status) {
        super(status);
        this.mensaje = "";
    }


    public RequestInfo(int status, int nFiles) {
        super(status);
        this.nFiles = nFiles;
        this.mensaje = "";
    }

    public int getnFiles() {
        return nFiles;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
