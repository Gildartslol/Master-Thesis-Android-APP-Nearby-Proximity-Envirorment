package com.example.jorge.androidapp.entities;

import android.support.annotation.NonNull;

import java.io.Serializable;

/** Represents a device we can talk to. */
public class Endpoint implements Serializable {
    @NonNull
    private final String id;
    @NonNull
    private final String name;

    private String type;

    public static final String TYPE_ESTABLISH_NORMAL = "0";
    public static final String TYPE_SEND_REQUEST_FRIEND = "1";
    public static final String TYPE_MULTI_EXCHANGE = "2";

    public Endpoint(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Endpoint) {
            Endpoint other = (Endpoint) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Endpoint{id=%s, name=%s}", id, name);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
