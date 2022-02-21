package com.gdudes.app.gdudesapp.GDTypes;

public class GDNewMessage {
    public String MessageID;
    public String SenderID;
    public String RecieverID;
    public String MessageText;
    public String AttachedPicIDs;
    public String Location;
    public String AttachedFilePath;
    public String DirectPhonePic;

    public GDNewMessage(String vMessageID, String vSenderID, String vRecieverID, String vMessageText,
                        String vAttachedPicIDs, String vLocation, String vAttachedFilePath, String vAttachedFileSrc) {
        MessageID = vMessageID;
        SenderID = vSenderID;
        RecieverID = vRecieverID;
        MessageText = vMessageText;
        AttachedPicIDs = vAttachedPicIDs;
        Location = vLocation;
        AttachedFilePath = vAttachedFilePath;
        DirectPhonePic = vAttachedFileSrc;
    }
}
