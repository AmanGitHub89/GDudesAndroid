package com.gdudes.app.gdudesapp.GDTypes.APIMajor;

public class FirebaseNotificationToken {
    public String token;
    public Boolean IsUpdatedToServer;

    public FirebaseNotificationToken(String token) {
        this.token = token;
        IsUpdatedToServer = false;
    }
}
