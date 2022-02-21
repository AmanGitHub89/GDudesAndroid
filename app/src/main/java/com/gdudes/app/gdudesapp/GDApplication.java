package com.gdudes.app.gdudesapp;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.gdudes.app.gdudesapp.Helpers.MasterDataHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;

public class GDApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.InitSessionManager(getApplicationContext());
        PersistantPreferencesHelper.InitPersistantPreferences(getApplicationContext());
        MasterDataHelper.InitMasterDataHelper(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
