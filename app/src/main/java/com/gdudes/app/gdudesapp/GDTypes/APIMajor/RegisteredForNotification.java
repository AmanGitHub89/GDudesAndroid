package com.gdudes.app.gdudesapp.GDTypes.APIMajor;

import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;

public class RegisteredForNotification {
    public String UserID;
    public String DeviceID;
    public String DeviceToken;
    public Boolean IsRegistered;
    public String DeviceType;

    public RegisteredForNotification(String userID, String token) {
        this.UserID = userID;
        this.DeviceID = PersistantPreferencesHelper.GetDeviceID();
        this.DeviceToken = token;
        this.IsRegistered = true;
        this.DeviceType = "FCM";
    }
}
