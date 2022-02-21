package com.gdudes.app.gdudesapp.GDTypes.APIMajor;

import java.util.ArrayList;

public class UserIDAndGUIDList {
    public String UserID;
    public ArrayList<String> GUIDList;

    public UserIDAndGUIDList(String userID, ArrayList<String> guidList) {
        this.UserID = userID;
        this.GUIDList = guidList;
    }
}
