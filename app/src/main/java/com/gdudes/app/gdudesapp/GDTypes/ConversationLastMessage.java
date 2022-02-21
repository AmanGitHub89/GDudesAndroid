package com.gdudes.app.gdudesapp.GDTypes;

public class ConversationLastMessage {
    public String ConverseWithUserID;
    public GDMessage LastMessage;

    public ConversationLastMessage(String vConverseWithUserID, GDMessage vLastMessage) {
        this.ConverseWithUserID = vConverseWithUserID;
        this.LastMessage = vLastMessage;
    }
}
