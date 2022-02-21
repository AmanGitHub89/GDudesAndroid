package com.gdudes.app.gdudesapp.GDTypes;

public class DirectPicDownloadingInfo {
    public String MessageID = "";
    public int FailureCount = 0;

    public DirectPicDownloadingInfo(String messageID) {
        this.MessageID = messageID;
        this.FailureCount = 0;
    }

    @Override
    public boolean equals(Object second) {
        if (this.MessageID.equalsIgnoreCase(((DirectPicDownloadingInfo) second).MessageID)) {
            return true;
        }
        return false;
    }
}
