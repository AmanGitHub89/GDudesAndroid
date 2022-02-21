package com.gdudes.app.gdudesapp.APICaller.APICalls;

import android.app.ProgressDialog;
import android.content.Context;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallType;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APIRequestTypes;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.UserIDAndGUIDList;
import com.gdudes.app.gdudesapp.GDTypes.GDNotification;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.APIFailureCallback;
import com.gdudes.app.gdudesapp.Interfaces.APISuccessCallback;
import com.gdudes.app.gdudesapp.Notifications.NotificationUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeAPICalls extends APICalls {

    public HomeAPICalls(Context context) {
        super(context);
    }

    public void ShowInMapSearch(ProgressDialog progressDialog, final APISuccessCallback successCallback,
                                final APIFailureCallback failureCallback, final APINoNetwork apiNoNetwork) {
        if (!IsUserLoggedIn()) {
            return;
        }
        AddParam(APICallParameter.param_UserID, LoggedInUser.UserID);
        apiCallInfo.APIReqType = APIRequestTypes.ShowInMapSearch;
        apiCallInfo.progressDialog = progressDialog;
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                if (result != null && result.equals("1")) {
                    successCallback.onSuccess(null, null);
                } else {
                    failureCallback.onFailure(null, null);
                }
            }
        }, new APINoNetwork() {
            @Override
            public void onAPINoNetwork() {
                apiNoNetwork.onAPINoNetwork();
            }
        });
    }

    public void GetNotifications(String lastDownloadedNotificationDateTime, final APISuccessCallback successCallback,
                                 final APIFailureCallback failureCallback) {
        if (!IsUserLoggedIn()) {
            failureCallback.onFailure(null, null);
            return;
        }
        try {

            GDMessagesDBHelper messagesDBHelper = new GDMessagesDBHelper(mContext);
            int ChatCount = messagesDBHelper.GetChatCountWithUnreadMessages(LoggedInUser.UserID);
            List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_GetNext, "0"));
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_OnlyUnseen, "1"));
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_OnlyUnseenMaxNo, Integer.toString(NotificationUtils.MaxMessagesForInboxStyle - ChatCount)));
            if (lastDownloadedNotificationDateTime != null && !lastDownloadedNotificationDateTime.equals("")) {
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_SinceDateTime, lastDownloadedNotificationDateTime));
            }
            APICallInfo apiCallInfo = new APICallInfo("Home", "GetUserNotifications", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.SEMILONG);
            apiCallInfo.CalledFromService = true;
            GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            failureCallback.onFailure(null, null);
                            return;
                        }
                        ArrayList<GDNotification> notificationsList = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<GDNotification>>() {
                        }.getType());
                        successCallback.onSuccess(notificationsList, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                        failureCallback.onFailure(null, null);
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    failureCallback.onFailure(null, null);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            failureCallback.onFailure(null, null);
        }

    }

    public void GetFavorites(int pageNo, final APICallerResultCallback apiCallerResultCallback) {
        if (!IsUserLoggedIn()) {
            apiCallerResultCallback.OnError(null, null);
            return;
        }
        AddParam(APICallParameter.param_UserID, LoggedInUser.UserID);
        AddParam(APICallParameter.param_PageNo, Integer.toString(pageNo));

        apiCallInfo.APIReqType = APIRequestTypes.GetFavorites;

        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (IsResultError(result)) {
                    apiCallerResultCallback.OnError(null, null);
                    return;
                }
                if (result.equals("0")) {
                    apiCallerResultCallback.OnError(result, null);
                    return;
                }
                ArrayList<Users> users = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<Users>>() {
                }.getType());
                UserObjectsCacheHelper.AddUpdUserListToCache(users);
                apiCallerResultCallback.OnComplete(users, null);
            } catch (Exception e) {
                GDLogHelper.LogException(e);
                apiCallerResultCallback.OnError(null, null);
            }
        }, () -> {
            apiCallerResultCallback.OnNoNetwork(null);
        });
    }

    public void GetMiniProfilesForUserIDList(ArrayList<String> userIDList,
                                             final APICallerResultCallback apiCallerResultCallback) {
        if (!IsUserLoggedIn()) {
            apiCallerResultCallback.OnError(null, null);
            return;
        }

        apiCallInfo.APIReqType = APIRequestTypes.GetMiniProfilesForUserIDList;
        apiCallInfo.CallType = APICallType.Post;
        apiCallInfo.PostJsonObject = new UserIDAndGUIDList(LoggedInUser.UserID, StringHelper.RemoveDuplicateEntries(userIDList));

        GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (IsResultError(result)) {
                    apiCallerResultCallback.OnError(null, null);
                    return;
                }
                String MiniViewResults = new JSONObject(result).getString("MiniViewResults");
                ArrayList<Users> users = new GsonBuilder().create().fromJson(MiniViewResults, new TypeToken<ArrayList<Users>>() {
                }.getType());
                UserObjectsCacheHelper.AddUpdUserListToCache(users);

                apiCallerResultCallback.OnComplete(users, null);
            } catch (JSONException e) {
                apiCallerResultCallback.OnError(null, null);
                //Do nothing
            } catch (Exception e) {
                GDLogHelper.LogException(e);
                apiCallerResultCallback.OnError(null, null);
            }
        }, () -> {
            apiCallerResultCallback.OnNoNetwork(null);
        });
    }

    public void BlockUser(String userID, ProgressDialog progressDialog,
                          final APICallerResultCallback apiCallerResultCallback) {
        if (!IsUserLoggedIn()) {
            apiCallerResultCallback.OnError(null, null);
            return;
        }
        AddParam(APICallParameter.param_UserID, userID);
        AddParam(APICallParameter.param_RequestingUserID, LoggedInUser.UserID);

        apiCallInfo.APIReqType = APIRequestTypes.BlockUser;
        apiCallInfo.progressDialog = progressDialog;

        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (result != null && result.trim().equals("1")) {
                    apiCallerResultCallback.OnComplete(null, null);
                    return;
                }
                apiCallerResultCallback.OnError(null, null);
            } catch (Exception e) {
                GDLogHelper.LogException(e);
                apiCallerResultCallback.OnError(null, null);
            }
        }, () -> {
            apiCallerResultCallback.OnNoNetwork(null);
        });
    }

    public void BlockUnBlockUsers(ArrayList<String> userIDList, ProgressDialog progressDialog,
                                  final APICallerResultCallback apiCallerResultCallback) {
        if (!IsUserLoggedIn()) {
            apiCallerResultCallback.OnError(null, null);
            return;
        }
        apiCallInfo.APIReqType = APIRequestTypes.UnblockUsers;
        apiCallInfo.CallType = APICallType.Post;
        apiCallInfo.progressDialog = progressDialog;
        apiCallInfo.PostJsonObject = new UserIDAndGUIDList(LoggedInUser.UserID, userIDList);

        GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (result != null && result.trim().equals("1")) {
                    apiCallerResultCallback.OnComplete(null, null);
                    return;
                }
                apiCallerResultCallback.OnError(null, null);
            } catch (Exception e) {
                GDLogHelper.LogException(e);
                apiCallerResultCallback.OnError(null, null);
            }
        }, () -> {
            apiCallerResultCallback.OnNoNetwork(null);
        });
    }

    public void GetBlockedUserList(int pageNo, String searchPhrase, ProgressDialog progressDialog,
                                   final APICallerResultCallback apiCallerResultCallback) {
        if (!IsUserLoggedIn()) {
            apiCallerResultCallback.OnError(null, null);
            return;
        }

        AddParam(APICallParameter.param_UserID, LoggedInUser.UserID);
        AddParam(APICallParameter.param_PageNo, Integer.toString(pageNo));
        AddParam(APICallParameter.param_SearchPhrase, searchPhrase.length() >= 3 ? searchPhrase : "");

        apiCallInfo.APIReqType = APIRequestTypes.GetBlockedUserList_Mobile;
        apiCallInfo.progressDialog = progressDialog;

        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (!IsUserLoggedIn()) {
                    apiCallerResultCallback.OnError(null, null);
                    return;
                }
                if (result.trim().equals("0")) {
                    apiCallerResultCallback.OnError("0", null);
                    return;
                }
                ArrayList<Users> users = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<Users>>() {
                }.getType());
                UserObjectsCacheHelper.AddUpdUserListToCache(users);
                apiCallerResultCallback.OnComplete(users, null);
            } catch (Exception e) {
                GDLogHelper.LogException(e);
                apiCallerResultCallback.OnError(null, null);
            }
        }, () -> {
            apiCallerResultCallback.OnNoNetwork(null);
        });
    }

}
