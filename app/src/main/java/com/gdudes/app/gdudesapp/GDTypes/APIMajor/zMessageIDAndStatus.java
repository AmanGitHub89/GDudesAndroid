package com.gdudes.app.gdudesapp.GDTypes.APIMajor;

public class zMessageIDAndStatus {
    public String MessageID;
    public String MessageStatus;

    public zMessageIDAndStatus() {
        MessageID = "";
        MessageStatus = "";
    }

    public zMessageIDAndStatus(String vMessageID, String vMessageStatus) {
        MessageID = vMessageID;
        MessageStatus = vMessageStatus;
    }
}