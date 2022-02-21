package com.gdudes.app.gdudesapp.APICaller.APICalls;

import android.content.Context;
import android.graphics.Bitmap;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallType;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APIRequestTypes;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.Database.GDConversationsDBHelper;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.UserIDAndGUIDList;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zMessageIDAndDT;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zMessageIDAndStatus;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zMessageStatusUpdate;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zUserID;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zUserIDXIDAndGUIDList;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Interfaces.APIFailureCallback;
import com.gdudes.app.gdudesapp.Interfaces.APISuccessCallback;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageAPICalls extends APICalls {

    public MessageAPICalls(Context context) {
        super(context);
    }

    public void SetStatusForUserMessageList(ArrayList<String> messageIDList, String status,
                                            final APISuccessCallback successCallback,
                                            final APIFailureCallback failureCallback) {
        if (!IsUserLoggedIn()) {
            failureCallback.onFailure(null, null);
            return;
        }
        try {
            zUserIDXIDAndGUIDList oUserIDXIDAndGUIDList = new zUserIDXIDAndGUIDList(LoggedInUser.UserID, status, messageIDList);
            apiCallInfo.APIReqType = APIRequestTypes.SetStatusForUserMessageList;
            apiCallInfo.progressDialog = null;
            apiCallInfo.CallType = APICallType.Post;
            apiCallInfo.PostJsonObject = oUserIDXIDAndGUIDList;

            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            failureCallback.onFailure(null, null);
                            return;
                        }
                        ArrayList<zMessageIDAndDT> messageIDAndDTList = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<zMessageIDAndDT>>() {
                        }.getType());
                        successCallback.onSuccess(messageIDAndDTList, null);
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

    public void GetMessages(final APISuccessCallback successCallback,
                            final APIFailureCallback failureCallback) {
        if (!IsUserLoggedIn()) {
            failureCallback.onFailure(null, null);
            return;
        }
        try {
            String FromDateTime = SessionManager.GetMobileLoginDT(mContext);
            if (FromDateTime.equals("")) {
                FromDateTime = GDDateTimeHelper.GetStringFromDate(new Date(0));
            }
            final GDMessagesDBHelper messagesDBHelper = new GDMessagesDBHelper(mContext);
            //Need to check if this works
            ArrayList<String> MessageIDListAfterTime = messagesDBHelper.GetMessageIDListAfterTime(LoggedInUser.UserID, FromDateTime);
            zUserIDXIDAndGUIDList oUserIDXIDAndGUIDList = null;
            if (MessageIDListAfterTime.size() == 0) {
                MessageIDListAfterTime.add(GDGenericHelper.GetNewGUID());
            }
            oUserIDXIDAndGUIDList = new zUserIDXIDAndGUIDList(LoggedInUser.UserID, FromDateTime, MessageIDListAfterTime);

            apiCallInfo.APIReqType = APIRequestTypes.GetMessagesForService;
            apiCallInfo.progressDialog = null;
            apiCallInfo.CallType = APICallType.Post;
            apiCallInfo.PostJsonObject = oUserIDXIDAndGUIDList;
            apiCallInfo.apiTimeout = APICallInfo.APITimeouts.LONG;

            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                try {
                    if (result == null || result.equals("") || result.equals("-1")) {
                        failureCallback.onFailure(null, null);
                        return;
                    }
                    if (result.startsWith("[") && !result.endsWith("]")) {
                        result = result + "]";
                    }
                    ArrayList<GDMessage> messageList = new GsonBuilder().create().fromJson(result, new TypeToken<List<GDMessage>>() {
                    }.getType());
                    successCallback.onSuccess(messageList, null);
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                    failureCallback.onFailure(null, null);
                }
            }, () -> failureCallback.onFailure(null, null));
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            failureCallback.onFailure(null, null);
        }
    }

    public void GetMessageListStatusUpdates(final APISuccessCallback successCallback,
                                            final APIFailureCallback failureCallback) {
        if (!IsUserLoggedIn()) {
            failureCallback.onFailure(null, null);
            return;
        }
        try {
            final GDMessagesDBHelper messagesDBHelper = new GDMessagesDBHelper(mContext);
            List<GDMessage> UnreadSentMessageList = messagesDBHelper.GetUnreadSentMessages(LoggedInUser.UserID);
            if (UnreadSentMessageList.size() == 0) {
                failureCallback.onFailure(null, null);
                return;
            }
            List<zMessageIDAndStatus> MessageStatusList = new ArrayList<>();
            final ArrayList<String> ConvWithUserIDList = new ArrayList<>();
            if (UnreadSentMessageList.size() > 0) {
                for (int i = 0; i < UnreadSentMessageList.size(); i++) {
                    MessageStatusList.add(new zMessageIDAndStatus(UnreadSentMessageList.get(i).MessageID, UnreadSentMessageList.get(i).MessageStatus));
                    ConvWithUserIDList.add(UnreadSentMessageList.get(i).RecieverID);
                }
            }

            apiCallInfo.APIReqType = APIRequestTypes.GetMessageListStatusUpdates;
            apiCallInfo.progressDialog = null;
            apiCallInfo.CallType = APICallType.Post;
            apiCallInfo.PostJsonObject = MessageStatusList;


            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            failureCallback.onFailure(null, null);
                            return;
                        }
                        ArrayList<zMessageStatusUpdate> MessageStatusUpdateList = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<zMessageStatusUpdate>>() {
                        }.getType());
                        if (MessageStatusUpdateList == null || MessageStatusUpdateList.size() == 0) {
                            failureCallback.onFailure(null, null);
                            return;
                        }
                        messagesDBHelper.UpdateMessagesListStatus(LoggedInUser.UserID, MessageStatusUpdateList);
                        successCallback.onSuccess(ConvWithUserIDList, null);
                    } catch (JsonSyntaxException e) {
                        failureCallback.onFailure(null, null);
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

    public void DeleteUserMessageList(ArrayList<String> messageIDList) {
        if (!IsUserLoggedIn() || messageIDList.size() == 0) {
            return;
        }
        try {
            apiCallInfo.APIReqType = APIRequestTypes.DeleteUserMessageList;
            apiCallInfo.CallType = APICallType.Post;
            apiCallInfo.PostJsonObject = new UserIDAndGUIDList(LoggedInUser.UserID, messageIDList);

            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                try {
                    if (IsResultError(result)) {
                        return;
                    }
                    ArrayList<zUserID> deletedIDList = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<zUserID>>() {
                    }.getType());
                    if (deletedIDList.size() == 0) {
                        return;
                    }
                    ArrayList<String> MessageIDListToDel = new ArrayList<String>();
                    for (int i = 0; i < deletedIDList.size(); i++) {
                        MessageIDListToDel.add(deletedIDList.get(i).UserID);
                    }
                    new GDMessagesDBHelper(mContext).DeleteMessagesByMessageIDList(MessageIDListToDel);
                } catch (JsonSyntaxException e) {
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
            }, null);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public void DeleteUserConversation(String userID) {
        if (!IsUserLoggedIn()) {
            return;
        }
        try {
            AddParam(APICallParameter.param_UserID, LoggedInUser.UserID);
            AddParam(APICallParameter.param_ConvWithUserID, userID);

            apiCallInfo.APIReqType = APIRequestTypes.DeleteUserMessageList;

            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                try {
                    if (IsResultError(result)) {
                        return;
                    }
                    SuccessResult successResult = new GsonBuilder().create().fromJson(result, new TypeToken<SuccessResult>() {
                    }.getType());
                    if (successResult != null && successResult.SuccessResult == 1) {
                        new GDConversationsDBHelper(mContext).DeleteConversation(LoggedInUser.UserID,
                                (String) ExtraData, false);
                    }
                } catch (JsonSyntaxException e) {
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
            }, null);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public void GetDirectPicForMessage(String messageID, final APISuccessCallback successCallback, final APIFailureCallback failureCallback) {
        if (!IsUserLoggedIn()) {
            failureCallback.onFailure(null, null);
            return;
        }
        try {
            final GDMessagesDBHelper messagesDBHelper = new GDMessagesDBHelper(mContext);
            ArrayList<GDMessage> messages = messagesDBHelper.GetMessagesByMessageIDList(LoggedInUser.UserID, StringHelper.ToArrayList(messageID));
            if (messages.size() != 1) {
                failureCallback.onFailure(messageID, null);
                return;
            }
            final GDMessage message = messages.get(0);
            if (StringHelper.IsNullOrEmpty(message.AttachedFilePath)) {
                failureCallback.onFailure(null, null);
                return;
            }

            AddParam(APICallParameter.param_MessageID, messageID);
            apiCallInfo.APIReqType = APIRequestTypes.GetDirectPicForMessage;

            GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                try {
                    if (IsResultError(result)) {
                        failureCallback.onFailure(messageID, null);
                        return;
                    }
                    Bitmap image = ImageHelper.GetBitmapFromString(result, true);
                    if (image != null) {
                        String compressedDirectPicSrc = ImageHelper.SaveImageAndGetCompressPicSrc(image, message.AttachedFilePath);
                        if (!StringHelper.IsNullOrEmpty(compressedDirectPicSrc)) {
                            if (messagesDBHelper.UpdateCompressedDirectPicSource(messageID, compressedDirectPicSrc)) {
                                successCallback.onSuccess(messageID, null);
                                return;
                            }
                        }
                    }
                    failureCallback.onFailure(messageID, null);
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                    failureCallback.onFailure(messageID, null);
                }
            }, () -> failureCallback.onFailure(messageID, null));
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            failureCallback.onFailure(messageID, null);
        }
    }
}
