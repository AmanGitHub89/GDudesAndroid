package com.gdudes.app.gdudesapp.APICaller.APICalls;

import android.content.Context;

import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallType;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APIRequestTypes;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.UserIDAndGUIDList;
import com.gdudes.app.gdudesapp.GDTypes.GDFullImage;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.APISuccessCallback;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;

public class PicsAPICalls extends APICalls {
    private static String LogClass = "LoginAPICalls";

    public PicsAPICalls(Context context) {
        super(context);
        mContext = context;
    }

    public void GetUserPicsByPicIDList(UserIDAndGUIDList userIDAndGUIDList,
                                       final APISuccessCallback successCallback) {
        if (!IsUserLoggedIn()) {
            return;
        }
        try {
            final GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(mContext);

            apiCallInfo.APIReqType = APIRequestTypes.GetUserPicsByPicIDList;
            apiCallInfo.CallType = APICallType.Post;
            apiCallInfo.PostJsonObject = userIDAndGUIDList;

            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    ArrayList<GDPic> picsList = new GsonBuilder().create().fromJson(jsonObject.
                            getString("UserPicsList"), new TypeToken<ArrayList<GDPic>>() {
                    }.getType());
                    if (picsList.size() == 0) {
                        return;
                    }

                    for (int i = 0; i < picsList.size(); i++) {
                        gdImageDBHelper.AddImageToCache(picsList.get(i).PicID, picsList.get(i).UserID,
                                picsList.get(i).PicThumbnail, false);
                        picsList.get(i).image = ImageHelper.GetBitmapFromString(picsList.get(i).PicThumbnail);
                        picsList.get(i).PicThumbnail = "";
                    }
                    successCallback.onSuccess(picsList, null);
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
            }, null);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public void GetUserPictures(Boolean getPublic, String userID, Boolean isManagePics,
                                final APICallerResultCallback apiCallerResultCallback) {
        AddParam(APICallParameter.param_UserID, userID);
        AddParam(APICallParameter.param_IsManagePics, isManagePics ? "TRUE" : "FALSE");

        apiCallInfo.APIReqType = getPublic ? APIRequestTypes.GetUserPublicPics : APIRequestTypes.GetUserPrivatePics;

        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (IsResultError(result)) {
                    apiCallerResultCallback.OnError(result, null);
                    return;
                }
                ArrayList<GDPic> picsList = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<GDPic>>() {
                }.getType());

                apiCallerResultCallback.OnComplete(picsList, null);
            } catch (Exception e) {
                GDLogHelper.LogException(e);
            }
        }, () -> apiCallerResultCallback.OnNoNetwork(apiCallInfo.ExtraData));
    }

    public void GetPicsInProfile(String userID, final APICallerResultCallback apiCallerResultCallback) {
        if (!IsUserLoggedIn()) {
            return;
        }
        AddParam(APICallParameter.param_UserID, userID);
        AddParam(APICallParameter.param_RequestingUserID, LoggedInUser.UserID);

        apiCallInfo.APIReqType = APIRequestTypes.GetPicsInProfile;

        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (IsResultError(result)) {
                    apiCallerResultCallback.OnError(result, null);
                    return;
                }
                ArrayList<GDPic> picsList = new GsonBuilder().create().fromJson(new JSONObject(result).getString("PicList"), new TypeToken<ArrayList<GDPic>>() {
                }.getType());

                apiCallerResultCallback.OnComplete(picsList, null);
            } catch (Exception e) {
                GDLogHelper.LogException(e);
            }
        }, () -> apiCallerResultCallback.OnNoNetwork(apiCallInfo.ExtraData));
    }

    public void GetFullUserImage(String picID, final APICallerResultCallback apiCallerResultCallback) {
        if (!IsUserLoggedIn()) {
            return;
        }
        AddParam(APICallParameter.param_PicID, picID);
        AddParam(APICallParameter.param_RequestingUserID, LoggedInUser.UserID);

        apiCallInfo.APIReqType = APIRequestTypes.GetFullUserImage;

        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (IsResultError(result)) {
                    apiCallerResultCallback.OnError(result, null);
                    return;
                }
                JSONObject jsonObject = new JSONObject(result);
                String FullImageObj = jsonObject.getString("FullUserImage");
                FullImageObj = FullImageObj.substring(1, FullImageObj.length() - 1);
                GDFullImage fullImage = new GsonBuilder().create().fromJson(FullImageObj, GDFullImage.class);

                if (fullImage != null && !StringHelper.IsNullOrEmpty(fullImage.FullImage)) {
                    fullImage.image = ImageHelper.GetBitmapFromString(fullImage.FullImage);
                    if (fullImage.image != null) {
                        GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(mContext);
                        gdImageDBHelper.AddImageToCache(fullImage.PicID, fullImage.UserID, fullImage.FullImage, true);
                        fullImage.FullImage = "";
                        apiCallerResultCallback.OnComplete(fullImage, null);
                    }
                }
                apiCallerResultCallback.OnError(result, null);
            } catch (Exception e) {
                apiCallerResultCallback.OnError(result, null);
                GDLogHelper.LogException(e);
            }
        }, () -> apiCallerResultCallback.OnNoNetwork(apiCallInfo.ExtraData));
    }
}
