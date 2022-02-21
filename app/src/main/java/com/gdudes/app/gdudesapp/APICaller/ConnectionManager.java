package com.gdudes.app.gdudesapp.APICaller;

import android.content.Context;

import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ConnectionManager {
    private static ArrayList<ContextConnection> Connections = new ArrayList<>();

    public static void RemoveConnectionsForContext(Context context) {
        try {
            ArrayList<ContextConnection> DisconnectedConnections = new ArrayList<>();
            ArrayList<ContextConnection> NullContextConnections = new ArrayList<>();
            for (int i = 0; i < Connections.size(); i++) {
                if (Connections.get(i).context == null) {
                    NullContextConnections.add(Connections.get(i));
                } else {
                    if (Connections.get(i).context.equals(context)) {
                        try {
                            Connections.get(i).connection.disconnect();
                        } catch (Exception e) {
                        }
                        DisconnectedConnections.add(Connections.get(i));
                    }
                }
            }
            Connections.removeAll(DisconnectedConnections);
            Connections.removeAll(NullContextConnections);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void RemoveConnection(ContextConnection connection) {
        try {
            if (connection != null && connection.ConnectionID != null) {
                try {
                    connection.connection.disconnect();
                } catch (Exception e) {
                }
                Connections.remove(connection);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ContextConnection GetNewConnection(Context context, String sURL) {
        ContextConnection contextConnection = null;
        try {
            URL url = new URL(sURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            contextConnection = new ContextConnection(GDGenericHelper.GetNewGUID(), context, connection);
            Connections.add(contextConnection);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return contextConnection;
    }
}
