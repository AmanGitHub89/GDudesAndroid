package com.gdudes.app.gdudesapp.APICaller;

import android.content.Context;

import java.net.HttpURLConnection;

public class ContextConnection {
    public String ConnectionID;
    public Context context;
    public HttpURLConnection connection;

    public ContextConnection(String vConnectionID, Context vcontext, HttpURLConnection vconnection) {
        ConnectionID = vConnectionID;
        context = vcontext;
        connection = vconnection;
    }

    @Override
    public boolean equals(Object second) {
        if (this.ConnectionID.equalsIgnoreCase(((ContextConnection) second).ConnectionID)) {
            return true;
        }
        return false;
    }
}
