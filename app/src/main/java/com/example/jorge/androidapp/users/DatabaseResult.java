package com.example.jorge.androidapp.users;

public class DatabaseResult {


    private boolean isTransactionOK;
    private String message;


    public DatabaseResult() {
    }


    public boolean isTransactionOK() {
        return isTransactionOK;
    }

    public void setTransactionOK(boolean isTransactionOK) {

        this.isTransactionOK = isTransactionOK;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
