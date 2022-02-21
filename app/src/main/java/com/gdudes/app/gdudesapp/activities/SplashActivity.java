package com.gdudes.app.gdudesapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDCountDownTimer;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.MasterDataHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.LoginRegister.RequestPermissionsActivity;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static String LogClass = "SplashActivity";

    Context mContext;
    Users LoggedInUser;
    CountDownTimer timer;
    Boolean bLogout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = getApplicationContext();
        SessionManager.InitSessionManager(mContext);
        PersistantPreferencesHelper.InitPersistantPreferences(mContext);
        MasterDataHelper.InitMasterDataHelper(mContext);

        LoggedInUser = SessionManager.GetLoggedInUser();
        if (LoggedInUser == null) {
            bLogout = true;
        } else {
            if (StringHelper.IsNullOrEmpty(LoggedInUser.UserID)) {
                GDLogHelper.Log(LogClass, "onCreate",
                        "LoggedInUser.UserID is null or empty. DestroyService(false) and return START_NOT_STICKY.");
                bLogout = true;
            }
        }
        CheckPermissionsAndOpenActivity();
    }

    public void CheckPermissionsAndOpenActivity() {
        if (RequestPermissionsActivity.HasAllPermissions(SplashActivity.this)) {
            OpenActivityForUser();
        } else {
            ShowPermissionsActivity();
        }
    }

    void ShowPermissionsActivity() {
        GDCountDownTimer.StartCountDown(1000, () -> {
            Intent intent = new Intent(mContext, RequestPermissionsActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    void OpenActivityForUser() {
        if (bLogout) {
            GDCountDownTimer.StartCountDown(800, () -> {
                SessionManager.UserLogout(mContext, LoggedInUser);
                finish();
            });
        } else {
            GDCountDownTimer.StartCountDown(500, () -> {
                List<APICallParameter> pAPICallParameters = new ArrayList<>();
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_EmailID, LoggedInUser.EmailID));
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_Password, SessionManager.GetLoginPassword()));
                APICallInfo apiCallInfo = new APICallInfo("Login", "Login", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.SHORT);
                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                    try {
                        Users vLoggedInUser = new GsonBuilder().create().fromJson(result, Users.class);
                        if (vLoggedInUser != null && !StringHelper.IsNullOrEmpty(vLoggedInUser.UserID)) {
                            UserObjectsCacheHelper.AddUpdUserToCache(vLoggedInUser);
                            SessionManager.UserLogIn(vLoggedInUser);
                            SessionManager.RedirectUserToActivity(vLoggedInUser, mContext);
                            finish();
                        } else {
                            SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                            if (successResult != null && successResult.SuccessResult == -101) {
                                GDToastHelper.ShowToast(SplashActivity.this, successResult.FailureMessage, GDToastHelper.ERROR, GDToastHelper.SHORT);
                                bLogout = true;
                                OpenActivityForUser();
                                return;
                            }
                            SessionManager.RedirectUserToActivity(LoggedInUser, mContext);
                            finish();
                        }
                    } catch (Exception e) {
                        GDLogHelper.LogException(e);
                        SessionManager.RedirectUserToActivity(LoggedInUser, mContext);
                        finish();
                    }
                }, () -> {
                    SessionManager.RedirectUserToActivity(LoggedInUser, mContext);
                    finish();
                });
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    OpenActivityForUser();
                } else {
                    CheckPermissionsAndOpenActivity();
                }
                break;
        }
    }
}
