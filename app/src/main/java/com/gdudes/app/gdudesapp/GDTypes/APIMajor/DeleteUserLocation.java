package com.gdudes.app.gdudesapp.GDTypes.APIMajor;

public class DeleteUserLocation {
    public String UserID;
    public String LocationID;

    public DeleteUserLocation() {
        this.UserID = "";
        this.LocationID = "";
    }

    public DeleteUserLocation(String vUserID, String vLocationID) {
        this.UserID = vUserID;
        this.LocationID = vLocationID;
    }
}
