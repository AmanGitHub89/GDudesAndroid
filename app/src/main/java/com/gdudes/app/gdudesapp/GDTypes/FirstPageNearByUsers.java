package com.gdudes.app.gdudesapp.GDTypes;

public class FirstPageNearByUsers {
    public String LastStoreDT;
    public String UsersJSON;

    public FirstPageNearByUsers() {
        LastStoreDT = "";
        UsersJSON = "";
    }

    public FirstPageNearByUsers(String sLastStoreDT, String sUsersJSON) {
        LastStoreDT = sLastStoreDT;
        UsersJSON = sUsersJSON;
    }
}
