package com.gdudes.app.gdudesapp.APICaller.APICalls;

import android.content.Context;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallType;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APIRequestTypes;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.BaseAPITypes;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.FirebaseNotificationToken;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.RegisterForNotification;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.RegisteredForNotification;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.APISuccessCallback;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class LoginAPICalls extends APICalls {

    private static String LogClass = "LoginAPICalls";

    public LoginAPICalls(Context context) {
        super(context);
        mContext = context;
    }

    public void RegisterForNotification(String token, final APISuccessCallback successCallback) {
        if (!IsUserLoggedIn()) {
            return;
        }
        try {

            ArrayList<RegisteredForNotification> RegisteredForNotificationList = new ArrayList<>();
            RegisteredForNotificationList.add(new RegisteredForNotification(LoggedInUser.UserID, token));
            RegisterForNotification registerForNotification = new RegisterForNotification(RegisteredForNotificationList);

            apiCallInfo.BaseAPIType = BaseAPITypes.Login;
            apiCallInfo.APIReqType = APIRequestTypes.RegisterForNotification;
            apiCallInfo.progressDialog = null;
            apiCallInfo.CallType = APICallType.Post;
            apiCallInfo.PostJsonObject = registerForNotification;
            apiCallInfo.apiTimeout = APICallInfo.APITimeouts.SEMILONG;

            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result != null && result.equals("1")) {
                            successCallback.onSuccess(null, null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public void UnRegisterForNotification(String userID) {
        try {
            FirebaseNotificationToken token = SessionManager.GetFirebaseNotificationToken();
            if (token == null || StringHelper.IsNullOrEmpty(token.token)) {
                return;
            }
            GDLogHelper.Log(LogClass, "UnRegisterForNotification", "UnRegistering");

            ArrayList<RegisteredForNotification> RegisteredForNotificationList = new ArrayList<>();
            RegisteredForNotification registeredForNotification = new RegisteredForNotification(userID, token.token);
            registeredForNotification.IsRegistered = false;
            RegisteredForNotificationList.add(registeredForNotification);

            apiCallInfo.BaseAPIType = BaseAPITypes.Login;
            apiCallInfo.APIReqType = APIRequestTypes.RegisterForNotification;
            apiCallInfo.progressDialog = null;
            apiCallInfo.CallType = APICallType.Post;
            apiCallInfo.PostJsonObject = new RegisterForNotification(RegisteredForNotificationList);
            apiCallInfo.apiTimeout = APICallInfo.APITimeouts.SEMILONG;

            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                try {
                    if (result != null && result.equals("1")) {
                        GDLogHelper.Log(LogClass, "UnRegisterForNotification", "UnRegistered");
                    }
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
            }, () -> {
                GDLogHelper.Log(LogClass, "UnRegisterForNotification", "No Network");
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public void TryLogin(final Context context) {
        if (!IsUserLoggedIn()) {
            return;
        }
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_EmailID, LoggedInUser.EmailID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_Password, SessionManager.GetLoginPassword()));
        APICallInfo apiCallInfo = new APICallInfo("Login", "Login", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(context, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    Users LoggedInUser = new GsonBuilder().create().fromJson(result, Users.class);
                    if (LoggedInUser != null && LoggedInUser.UserID != null && !LoggedInUser.UserID.trim().equals("")) {
                        SessionManager.UserLogIn(LoggedInUser);
                        UserObjectsCacheHelper.AddUpdUserToCache(LoggedInUser);
                    } else {
                        SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                        if (successResult != null && successResult.SuccessResult == -101) {
                            GDToastHelper.ShowToast(context, successResult.FailureMessage, GDToastHelper.ERROR, GDToastHelper.SHORT);
                            GDLogHelper.Log(LogClass, "TryLogin", "Login Failure. Logout.");
                            SessionManager.UserLogout(context, LoggedInUser);
                            return;
                        }
                    }
                } catch (JsonSyntaxException e) {
                    //Do nothing
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
            }
        }, null);
    }
}
