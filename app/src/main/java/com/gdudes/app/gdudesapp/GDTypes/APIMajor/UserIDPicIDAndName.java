package com.gdudes.app.gdudesapp.GDTypes.APIMajor;

public class UserIDPicIDAndName {
    public String UserID;
    public String UserName;
    public String PicID;

    public UserIDPicIDAndName() {
        UserID = "";
        UserName = "";
        PicID = "";
    }

    public UserIDPicIDAndName(String vUserID, String vUserName, String vPicID) {
        UserID = vUserID;
        UserName = vUserName;
        PicID = vPicID;
    }
}
