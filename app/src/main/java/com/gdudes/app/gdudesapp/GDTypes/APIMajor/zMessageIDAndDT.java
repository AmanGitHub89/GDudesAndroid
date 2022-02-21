package com.gdudes.app.gdudesapp.GDTypes.APIMajor;

public class zMessageIDAndDT {
    public String MessageID;
    public String DT;
    public int ErrorCode;

    public zMessageIDAndDT() {
        MessageID = "";
        DT = "";
        ErrorCode = 0;
    }

    public zMessageIDAndDT(String vMessageID, String vDT) {
        MessageID = vMessageID;
        DT = vDT;
        ErrorCode = 0;
    }

    @Override
    public boolean equals(Object second) {
        if (this.MessageID.equalsIgnoreCase(((zMessageIDAndDT) second).MessageID)) {
            return true;
        }
        return false;
    }
}
