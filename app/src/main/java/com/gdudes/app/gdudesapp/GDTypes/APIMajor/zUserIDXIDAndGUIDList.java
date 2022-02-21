package com.gdudes.app.gdudesapp.GDTypes.APIMajor;

import java.util.ArrayList;

public class zUserIDXIDAndGUIDList {
    public String UserID;
    public String XID;
    public ArrayList<String> GUIDList;

    public zUserIDXIDAndGUIDList() {
        UserID = "";
        XID = "";
        GUIDList = new ArrayList<>();
    }

    public zUserIDXIDAndGUIDList(String vUserID, String vXID, ArrayList<String> vGUIDList) {
        UserID = vUserID;
        XID = vXID;
        GUIDList = vGUIDList;
    }
}
