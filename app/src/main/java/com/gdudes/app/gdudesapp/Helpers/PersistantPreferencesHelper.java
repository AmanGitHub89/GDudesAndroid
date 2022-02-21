package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.gdudes.app.gdudesapp.GDTypes.AppSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PersistantPreferencesHelper {
    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    static int PRIVATE_MODE = 0;
    private static PersistentStates persistentStates;
    private static String LogClass = "PersistentPreferencesHelper";
    private static final String PREF_NAME = "GDPersistantPreferences";

    private static AppSettings mAppSettings = null;

    public static final String KEY_FileWritePermission = "FileWritePermission";
    public static final String KEY_FirebaseNotificationDeviceID = "FirebaseNotificationDeviceID";
    public static final String KEY_AppSettings = "AppSettings";
    public static final String KEY_LoggedOutEmailID = "LoggedOutEmailID";

    public static Boolean IsPersistantPreferencesInitiated = false;

    public static void InitPersistantPreferences(Context context) {
        try {
            if (IsPersistantPreferencesInitiated) {
                return;
            }
            sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = sharedPreferences.edit();
            IsPersistantPreferencesInitiated = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //File write permission
    public static String GetFileWritePermission() {
        try {
            if (sharedPreferences == null) {
                return "0";
            }
            return sharedPreferences.getString(KEY_FileWritePermission, "0");
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return "0";
        }
    }
    public static void SetFileWritePermission(String FileWritePermission) {
        try {
            editor.putString(KEY_FileWritePermission, FileWritePermission);
            editor.commit();
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    //DeviceID for Firebase
    public static String GetDeviceID() {
        if (sharedPreferences == null) {
            return null;
        }
        String deviceID = sharedPreferences.getString(KEY_FirebaseNotificationDeviceID, null);
        if (deviceID == null) {
            deviceID = GDGenericHelper.GetNewGUID();
            SetDeviceID(deviceID);
        }
        return deviceID;
    }
    private static void SetDeviceID(String deviceID) {
        editor.putString(KEY_FirebaseNotificationDeviceID, deviceID);
        editor.commit();
    }

    //AppSettings
    public static AppSettings GetAppSettings() {
        try {
            if (mAppSettings != null) {
                return mAppSettings;
            }
            if (sharedPreferences == null) {
                return new AppSettings();
            }
            String sAppSettings = sharedPreferences.getString(KEY_AppSettings, null);
            if (sAppSettings == null) {
                return new AppSettings();
            } else {
                mAppSettings = new GsonBuilder().create().fromJson(sAppSettings, AppSettings.class);
                return mAppSettings;
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return new AppSettings();
        }
    }
    public static void SetAppSettings(AppSettings appSettings) {
        try {
            editor.putString(KEY_AppSettings, new Gson().toJson(appSettings));
            editor.commit();
            mAppSettings = appSettings;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    //Logged out User Email ID
    public static String GetLoggedOutEmailID() {
        try {
            if (sharedPreferences == null) {
                return "";
            }
            return sharedPreferences.getString(KEY_LoggedOutEmailID, "");
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return "";
        }
    }
    public static void SetLoggedOutEmailID(String emailID) {
        try {
            editor.putString(KEY_LoggedOutEmailID, emailID);
            editor.commit();
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }



    public static void PersistBeforeClear() {
        if (mAppSettings == null) {
            mAppSettings = GetAppSettings();
        }
        persistentStates = new PersistentStates(GetFileWritePermission(), GetLoggedOutEmailID(), mAppSettings);
    }

    public static void PersistPostClear() {
        if (persistentStates == null) {
            GDLogHelper.Log(LogClass, "PersistPostClear", "Persist Object is null.");
            return;
        }
        SetFileWritePermission(persistentStates.FileWritePermission);
        SetAppSettings(mAppSettings);
        SetLoggedOutEmailID(persistentStates.LoggedOutEmailID);
    }

    private static class PersistentStates {
        public String FileWritePermission = "0";
        public String LoggedOutEmailID = "";
        public AppSettings appSettings = null;

        public PersistentStates(String fileWritePermission, String loggedOutEmailID, AppSettings appSettings) {
            this.FileWritePermission = fileWritePermission;
            this.LoggedOutEmailID = loggedOutEmailID;
            this.appSettings = appSettings;
        }
    }
}
