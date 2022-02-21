package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.gdudes.app.gdudesapp.APICaller.APICalls.LoginAPICalls;
import com.gdudes.app.gdudesapp.GDServices.GDMessageService;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.FirebaseNotificationToken;
import com.gdudes.app.gdudesapp.GDTypes.FirstPageNearByUsers;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.activities.LoginRegister.LoginActivity;
import com.gdudes.app.gdudesapp.activities.LoginRegister.RegisterProfileDescActivity;
import com.gdudes.app.gdudesapp.activities.MainLayout.LayoutActivity;
import com.gdudes.app.gdudesapp.activities.Pics.UploadPicsActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    static SharedPreferences sharedPreferences;
    static Editor editor;
    static Context context;
    static int PRIVATE_MODE = 0;

    static Users mLoggedInUser = null;

    private static String LogClass = "SessionManager";
    private static final String PREF_NAME = "GDUserSession";
    public static final String KEY_NAME = "LoggedInUser";
    public static final String KEY_MobileLoginDT = "MobileLoginDT";
    public static final String KEY_UseGPS = "UseGPS";
    public static final String KEY_LoginPassword = "LoginPassword";
    public static final String KEY_LastDownloadedNotificationDateTime = "LastDownloadedNotificationDateTime";
    public static final String KEY_FirstPageNearByUsers = "FirstPageNearByUsers";
    public static final String KEY_FirebaseNotificationToken = "FirebaseNotificationToken";

    public static Boolean IsSessionManagerInitiated = false;

    public static void InitSessionManager(Context acontext) {
        try {
            context = acontext;
            sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = sharedPreferences.edit();
            IsSessionManagerInitiated = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    //Logged in user
    public static void UserLogIn(Users LoggedInUser) {
        try {
            editor.putString(KEY_NAME, new Gson().toJson(LoggedInUser));
            editor.commit();
            mLoggedInUser = LoggedInUser;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public static Users GetLoggedInUser(Context acontext) {
        try {
            if (mLoggedInUser != null) {
                return mLoggedInUser;
            }
            if (GetLoggedInUser() == null) {
                UserLogout(acontext, null);
            }
            return mLoggedInUser;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return null;
        }
    }

    //Do not call from anywhere but Layout Activity
    public static Users GetLoggedInUser() {
        try {
            if (mLoggedInUser != null) {
                return mLoggedInUser;
            }
            if (sharedPreferences == null) {
                return null;
            }
            String sLoggedInUserDetails = sharedPreferences.getString(KEY_NAME, null);
            if (sLoggedInUserDetails == null) {
                return null;
            }
            mLoggedInUser = new GsonBuilder().create().fromJson(sLoggedInUserDetails, Users.class);
            return mLoggedInUser;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return null;
        }
    }

    public static void UserLogout(Context acontext, Users LoggedInUser) {
        try {
            GDLogHelper.Log(LogClass, "DestroyService", "UserLogout:" + "User Logged out.");

            if (LoggedInUser != null && !StringHelper.IsNullOrEmpty(LoggedInUser.UserID)) {
                new LoginAPICalls(acontext).UnRegisterForNotification(LoggedInUser.UserID);
                PersistantPreferencesHelper.SetLoggedOutEmailID(LoggedInUser.EmailID);
            }

            PersistantPreferencesHelper.PersistBeforeClear();

            //Clear all
            mLoggedInUser = null;
            if (editor != null) {
                editor.clear();
                editor.commit();
            }
            //Destroy service
            GDMessageService service = GDMessageService.ServiceInstance;
            if (service != null) {
                service.DestroyService(false);
            }

            PersistantPreferencesHelper.PersistPostClear();

            // After logout redirect user to Loing Activity
            Intent i = new Intent(acontext, LoginActivity.class);
            // Closing all the Activities & Add new Flag to start new Activity
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            acontext.startActivity(i);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }


    //Others
    public static void SetLoginPassword(String LoginPassword) {
        try {
            editor.putString(KEY_LoginPassword, LoginPassword);
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static String GetLoginPassword() {
        try {
            if (sharedPreferences == null) {
                return "";
            }
            return sharedPreferences.getString(KEY_LoginPassword, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    public static String GetMobileLoginDT(Context acontext) {
        try {
            if (sharedPreferences == null)
                return "";
            return sharedPreferences.getString(KEY_MobileLoginDT, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    public static void SetMobileLoginDT(String MobileLoginDT) {
        try {
            editor.putString(KEY_MobileLoginDT, MobileLoginDT);
            editor.commit();
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }


    public static void SetLastDownloadedNotificationDateTime(String LastDownloadedNotificationDateTime) {
        try {
            editor.putString(KEY_LastDownloadedNotificationDateTime, LastDownloadedNotificationDateTime);
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static String GetLastDownloadedNotificationDateTime() {
        try {
            if (sharedPreferences == null) {
                return "";
            }
            return sharedPreferences.getString(KEY_LastDownloadedNotificationDateTime, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    public static void SetUseGPS(String UseGPS) {
        try {
            if (UseGPS == null || UseGPS.equals("") || (!UseGPS.equals("1") && !UseGPS.equals("0"))) {
                UseGPS = "1";
            }
            editor.putString(KEY_UseGPS, UseGPS);
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static String GetUseGPS() {
        try {
            if (sharedPreferences == null) {
                return "1";
            }
            return sharedPreferences.getString(KEY_UseGPS, "1");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "1";
        }
    }


    //Nearby and Conversations
    public static void SetFirstPageNearByUsers(FirstPageNearByUsers firstPageNearByUsers) {
        try {
            Gson gson = new Gson();
            String details = gson.toJson(firstPageNearByUsers);
            editor.putString(KEY_FirstPageNearByUsers, details);
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void SetFirstPageNearByUsersShowMyInMap() {
        try {
            FirstPageNearByUsers firstPageNearByUsers = GetFirstPageNearByUsers();
            String usersJson = firstPageNearByUsers.UsersJSON;
            if (firstPageNearByUsers != null && usersJson != null && !usersJson.trim().equals("")) {
                ArrayList<Users> users = new GsonBuilder().create().fromJson(usersJson, new TypeToken<List<Users>>() {
                }.getType());
                if (users != null && users.size() > 0) {
                    for (int i = 0; i < users.size(); i++) {
                        users.get(i).MyShowInMapSearch = true;
                    }
                    Gson gson = new Gson();
                    firstPageNearByUsers.UsersJSON = gson.toJson(users);
                    String details = gson.toJson(firstPageNearByUsers);
                    editor.putString(KEY_FirstPageNearByUsers, details);
                    editor.commit();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static FirstPageNearByUsers GetFirstPageNearByUsers() {
        try {
            if (sharedPreferences == null) {
                return null;
            }
            String sFirstPageNearByUsers = sharedPreferences.getString(KEY_FirstPageNearByUsers, null);
            if (sFirstPageNearByUsers == null || sFirstPageNearByUsers.trim().equals("")) {
                return null;
            } else {
                return new GsonBuilder().create().fromJson(sFirstPageNearByUsers, FirstPageNearByUsers.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return null;
        }
    }

    public static void RedirectUserToActivity(Users GDudesUser, Context context) {
        Intent intent = null;
        try {
            if (GDudesUser != null && GDudesUser.ProfileComplete) {
                intent = new Intent(context, LayoutActivity.class);
            } else {
                if (GDudesUser.HasPicsToBeCategorized == null || GDudesUser.HasPicsToBeCategorized.equals("")
                        || GDudesUser.HasPicsToBeCategorized.equals("0")) {
                    intent = new Intent(context, UploadPicsActivity.class);
                    intent.putExtra("IsRegistrationFirstPic", true);
                } else {
                    intent = new Intent(context, RegisterProfileDescActivity.class);
                }
            }
            if (intent != null) {
                // Closing all the Activities
                // Add new Flag to start new Activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }


    //Notifications
    public static void SetFirebaseNotificationToken(FirebaseNotificationToken token) {
        try {
            String sToken = new Gson().toJson(token);
            editor.putString(KEY_FirebaseNotificationToken, sToken);
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static FirebaseNotificationToken GetFirebaseNotificationToken() {
        try {
            if (sharedPreferences == null) {
                return null;
            }
            String sToken = sharedPreferences.getString(KEY_FirebaseNotificationToken, null);
            if (sToken == null) {
                return null;
            } else {
                return new GsonBuilder().create().fromJson(sToken, FirebaseNotificationToken.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return null;
        }
    }
}
