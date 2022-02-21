package com.gdudes.app.gdudesapp.Helpers;

import com.gdudes.app.gdudesapp.GDTypes.Users;

import java.util.ArrayList;
import java.util.List;

public class UserObjectsCacheHelper {

    private static ArrayList<Users> CachedUsers = new ArrayList<>();

    public static void AddUpdUserToCache(Users user) {
        try {
            ArrayList<Users> UsersList = new ArrayList<>();
            UsersList.add(user);
            AddUpdUserListToCache(UsersList);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void AddUpdUserListToCache(List<Users> UsersList) {
        Users TempUser = null;
        int index = -1;
        try {
            for (int i = 0; i < UsersList.size(); i++) {
                if (CachedUsers.contains(UsersList.get(i))) {
                    TempUser = UsersList.get(i);
                    index = CachedUsers.indexOf(UsersList.get(i));
                    if (TempUser.image == null && CachedUsers.get(index).image != null) {
                        TempUser.image = CachedUsers.get(index).image;
                    }
                    CachedUsers.remove(index);
                    CachedUsers.add(TempUser);
                } else {
                    CachedUsers.add(UsersList.get(i));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static Users GetUserFromCache(String UserID) {
        Users user = new Users(UserID);
        Users FoundUser = null;
        try {
            if (CachedUsers.contains(user)) {
                int index = CachedUsers.indexOf(user);
                FoundUser = CachedUsers.get(index);
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return FoundUser;
    }

    public static void RemoveUserFromCache(String UserID) {
        try {
            Users user = new Users(UserID);
            if (CachedUsers.contains(user)) {
                int index = CachedUsers.indexOf(user);
                CachedUsers.remove(index);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void SetIBlockedUnBlockedHim(String UserID, Boolean Blocked) {
        Users user = new Users(UserID);
        Users FoundUser = null;
        try {
            if (CachedUsers.contains(user)) {
                int index = CachedUsers.indexOf(user);
                FoundUser = CachedUsers.get(index);
            }
            if (FoundUser != null) {
                FoundUser.IBlockedHim = Blocked ? UserID : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }
}
