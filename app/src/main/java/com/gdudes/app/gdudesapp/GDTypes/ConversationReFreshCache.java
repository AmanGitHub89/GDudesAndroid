package com.gdudes.app.gdudesapp.GDTypes;

import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;

public class ConversationReFreshCache {
    public String UserID;
    public String UserName;
    public String PicID;
    public String LastRefreshDT;
    public Boolean OnlineStatus;

    public ConversationReFreshCache(String sUserID, String sUserName, String sPicID, String sLastRefreshDT, Boolean bOnlineStatus) {
        UserID = sUserID;
        UserName = sUserName;
        PicID = sPicID;
        LastRefreshDT = sLastRefreshDT;
        OnlineStatus = bOnlineStatus;
    }

    public Conversations GetConversation() {
        Conversations conversation = new Conversations(UserID);
        conversation.dLastDataRefreshedDT = GDDateTimeHelper.GetDateFromString(LastRefreshDT);
        return conversation;
    }
}
