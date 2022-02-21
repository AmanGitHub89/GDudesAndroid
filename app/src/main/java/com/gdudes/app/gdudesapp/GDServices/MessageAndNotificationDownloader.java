package com.gdudes.app.gdudesapp.GDServices;

import android.content.Context;
import android.os.Handler;
import android.util.Pair;

import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APICalls.MessageAPICalls;
import com.gdudes.app.gdudesapp.Comparators.DirectPicDownloadingInfoComparator;
import com.gdudes.app.gdudesapp.Database.GDConversationsDBHelper;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zMessageIDAndDT;
import com.gdudes.app.gdudesapp.GDTypes.DirectPicDownloadingInfo;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.GDNotification;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDTimer;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Notifications.NotificationHelper;
import com.gdudes.app.gdudesapp.activities.MainLayout.LayoutActivity;
import com.gdudes.app.gdudesapp.activities.MainLayout.MessagesPageFragment;
import com.gdudes.app.gdudesapp.activities.MessageWindow;

import java.util.ArrayList;
import java.util.Collections;

public class MessageAndNotificationDownloader {

    private static String LogClass = "MessageAndNotificationDownloader";
    private static String LastDownloadedNotificationDateTime = "";
    private static Boolean IsGetMessagesRunning = false;
    private static Boolean IsSendUpdateForInboundMessagesRunning = false;
    private static Boolean IsGetNotificationsForNotificationsRunning = false;
    private static Boolean IsGetMessageStatusUpdatesRunning = false;

    private static ArrayList<DirectPicDownloadingInfo> DirectPicsMessageIDs = new ArrayList<>();
    private static Boolean DirectPicsDownloadRunning = false;
    private static GDTimer GetDirectPicsTimer = null;

    public static void StartDownloadingDirectMessagePics_IfNotRunning(Context context) {
        if (GetDirectPicsTimer == null) {
            StartDownloadingDirectMessagePics(context);
        }
    }

    public static void StartDownloadingDirectMessagePics(Context context) {
        Users loggedInUser = SessionManager.GetLoggedInUser();
        if (loggedInUser == null || StringHelper.IsNullOrEmpty(loggedInUser.UserID)) {
            return;
        }

        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            ArrayList<Pair<String, String>> messageIDAndPathList = new GDMessagesDBHelper(context).GetMessageIDsAndAttachFilePathsForDirectPicDownload(loggedInUser.UserID);
            for (Pair<String, String> messageIDAndPath : messageIDAndPathList) {
                if (!ImageHelper.FileExists(messageIDAndPath.second)) {
                    DirectPicsMessageIDs.add(new DirectPicDownloadingInfo(messageIDAndPath.first));
                }
            }
            gdBackgroundTaskFinished.OnBackgroundTaskFinished(null);
        }, data -> {
            GetDirectPicsTimer = new GDTimer(3000, 2000, new Handler(), () -> {
                GetDirectPics(context);
            });
            GetDirectPicsTimer.Start();
        });
    }

    public static void StopDownloading() {
        if (GetDirectPicsTimer != null) {
            GetDirectPicsTimer.Stop();
            GetDirectPicsTimer = null;
        }
    }

    public static void GetMessagesFromServer(final Context context) {
        if (IsGetMessagesRunning || context == null) {
            return;
        }
        IsGetMessagesRunning = true;
        try {
            final Users LoggedInUser = SessionManager.GetLoggedInUser(context);
            final GDConversationsDBHelper conversationsDBHelper = new GDConversationsDBHelper(context);
            final GDMessagesDBHelper messagesDBHelper = new GDMessagesDBHelper(context);

            new MessageAPICalls(context).GetMessages((data, ExtraData) -> {
                IsGetMessagesRunning = false;
                if (data == null) {
                    return;
                }
                ArrayList<GDMessage> messageList = (ArrayList<GDMessage>) data;
                if (messageList.size() == 0) {
                    return;
                }
                if (messageList.size() > 20) {
                    GDLogHelper.Log(LogClass, "GetMessagesFromServer", "Service got messages : " + Integer.toString(messageList.size()));
                }
                int LastSuccessfull = -1;
                Boolean CurrentSuccessfull = false;
                ArrayList<String> MarkDeliveredMessageIDList = new ArrayList<String>();
                ArrayList<GDMessage> SuccessfullyDownloadedMessages = new ArrayList<GDMessage>();

                String ConvWithUserID = "";
                for (int i = 0; i < messageList.size(); i++) {
                    GDMessage message = messageList.get(i);
                    if (message.SenderID.equalsIgnoreCase(LoggedInUser.UserID)) {
                        ConvWithUserID = message.RecieverID;
                    } else {
                        ConvWithUserID = message.SenderID;
                    }
                    if (messagesDBHelper.MessageExistsInCache(message.MessageID)) {
                        CurrentSuccessfull = false;
                        LastSuccessfull = i;
                    } else {
                        Boolean containsDirectPicToDownload = message.ContainsDirectPicToDownload();
                        if (!containsDirectPicToDownload || GDMessage.SetImagePath(message)) {
                            conversationsDBHelper.AddConversationToCache(LoggedInUser.UserID, ConvWithUserID, message.SenderName, message.PicID, "",
                                    message.SentDateTime, message.SenderType, "0", message.SentDateTime);
                            CurrentSuccessfull = messagesDBHelper.AddMessageToCache(LoggedInUser.UserID, message.MessageID, ConvWithUserID,
                                    message.SenderID, message.SenderName, message.RecieverID, message.MessageText, message.AttachedPicIDs,
                                    message.Location, message.AttachedFilePath, message.DirectPhonePic, message.MessageStatus, message.SentDateTime,
                                    message.ReadDateTime, true);
                            if (CurrentSuccessfull && containsDirectPicToDownload) {
                                DirectPicsMessageIDs.add(new DirectPicDownloadingInfo(message.MessageID));
                            }
                        } else {
                            CurrentSuccessfull = false;
                        }
                    }
                    if (CurrentSuccessfull && !message.SenderID.equalsIgnoreCase(LoggedInUser.UserID)
                            && !message.MessageStatus.equals("R")) {
                        LastSuccessfull = i;
                        MarkDeliveredMessageIDList.add(message.MessageID);
                        SuccessfullyDownloadedMessages.add(message);
                    }
                }
                if (LastSuccessfull != -1) {
                    SessionManager.SetMobileLoginDT(messageList.get(LastSuccessfull).SentDateTime);
                }
                //NotificationHelper.ShowNewMessageNotification(getApplicationContext(), message);
                NotificationHelper.ShowNotifications(context, SuccessfullyDownloadedMessages, LoggedInUser.UserID);

                ArrayList<String> ConvWithUserIDList = new ArrayList<>();
                for (int i = 0; i < messageList.size(); i++) {
                    GDMessage message = messageList.get(i);
                    ConvWithUserIDList.add(message.RecieverID.equalsIgnoreCase(LoggedInUser.UserID) ? message.SenderID : message.RecieverID);
                }

                MessageWindow.NewMessagesAvailable(ConvWithUserIDList);
                SetConvNeedsLocalRefreshIfRunning(ConvWithUserIDList);
                SendUpdateForInboundMessages(context, LoggedInUser.UserID, messagesDBHelper, MarkDeliveredMessageIDList, "D");
            }, (data, ExtraData) -> IsGetMessagesRunning = false);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            IsGetMessagesRunning = false;
        }
    }

    public static void SendUpdateForInboundMessages(final Context context, final String loggedInUserID,
                                                    final GDMessagesDBHelper messagesDBHelper,
                                                    ArrayList<String> messageIDList, final String status) {
        if (IsSendUpdateForInboundMessagesRunning || messageIDList.size() == 0) {
            return;
        }
        IsSendUpdateForInboundMessagesRunning = true;
        try {
            new MessageAPICalls(context).SetStatusForUserMessageList(messageIDList, status, (data, ExtraData) -> {
                IsSendUpdateForInboundMessagesRunning = false;
                if (data == null) {
                    return;
                }
                ArrayList<zMessageIDAndDT> messageIDAndDTList = (ArrayList<zMessageIDAndDT>) data;
                if (messageIDAndDTList.size() == 0) {
                    return;
                }
                messagesDBHelper.UpdateInboundMessagesListStatus(loggedInUserID, messageIDAndDTList, status);
                MessageWindow.ReceivedMessagesStatusUpdated();
            }, (data, ExtraData) -> IsSendUpdateForInboundMessagesRunning = false);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            IsSendUpdateForInboundMessagesRunning = false;
        }
    }

    private static void SetConvNeedsLocalRefreshIfRunning(ArrayList<String> ConvWithUserIDList) {
        if (MessagesPageFragment.MessagesPageInstance != null) {
            MessagesPageFragment.MessagesPageInstance.SetConversationsNeedLocalRefresh(ConvWithUserIDList);
        }
        LayoutActivity.TabIconCountsNeedRefresh = true;
    }

    public static void GetNotifications(final Context context) {
        if (IsGetNotificationsForNotificationsRunning || context == null) {
            return;
        }
        Users loggedInUser = SessionManager.GetLoggedInUser(context);
        if (loggedInUser == null || loggedInUser.UserID.equalsIgnoreCase("")) {
            return;
        }
        final String loggedInUserID = loggedInUser.UserID;
        IsGetNotificationsForNotificationsRunning = true;
        try {
            LastDownloadedNotificationDateTime = SessionManager.GetLastDownloadedNotificationDateTime();
            new HomeAPICalls(context).GetNotifications(LastDownloadedNotificationDateTime, (data, ExtraData) -> {
                IsGetNotificationsForNotificationsRunning = false;

                if (data == null) {
                    return;
                }
                ArrayList<GDNotification> notificationsList = (ArrayList<GDNotification>) data;
                if (notificationsList.size() == 0) {
                    return;
                }
                LastDownloadedNotificationDateTime = notificationsList.get(0).NotificationDateTime;
                SessionManager.SetLastDownloadedNotificationDateTime(LastDownloadedNotificationDateTime);
                NotificationHelper.ShowNotificationsForNotificationList(context, notificationsList, loggedInUserID);
            }, (data, ExtraData) -> IsGetNotificationsForNotificationsRunning = false);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            IsGetNotificationsForNotificationsRunning = false;
        }
    }

    public static void GetMessageStatusUpdates(final Context context) {
        if (IsGetMessageStatusUpdatesRunning) {
            return;
        }
        IsGetMessageStatusUpdatesRunning = true;

        try {
            new MessageAPICalls(context).GetMessageListStatusUpdates((data, ExtraData) -> {
                IsGetMessageStatusUpdatesRunning = false;
                if (data == null) {
                    return;
                }
                ArrayList<String> ConvWithUserIDList = (ArrayList<String>) data;
                if (ConvWithUserIDList.size() == 0) {
                    return;
                }
                MessageWindow.SentMessagesStatusUpdated(ConvWithUserIDList);
                SetConvNeedsLocalRefreshIfRunning(ConvWithUserIDList);
            }, (data, ExtraData) -> IsGetMessageStatusUpdatesRunning = false);
        } catch (Exception ex) {
            IsGetMessageStatusUpdatesRunning = false;
            GDLogHelper.LogException(ex);
        }
    }


    private static void GetDirectPics(Context context) {
        if (DirectPicsMessageIDs.size() == 0 || DirectPicsDownloadRunning) {
            return;
        }
        RemoveDirectPicsWithMaxedOutErrorCount();
        DirectPicsMessageIDs = GDGenericHelper.RemoveDuplicate(DirectPicsMessageIDs);

        if (DirectPicsMessageIDs.size() == 0) {
            return;
        }
        Collections.sort(DirectPicsMessageIDs, new DirectPicDownloadingInfoComparator());
        GetDirectPic(DirectPicsMessageIDs.get(0).MessageID, context);
    }

    private static void RemoveDirectPicsWithMaxedOutErrorCount() {
        ArrayList<DirectPicDownloadingInfo> toDeleteList = new ArrayList<>();
        for (DirectPicDownloadingInfo pic : DirectPicsMessageIDs) {
            if (pic.FailureCount >= 10) {
                toDeleteList.add(pic);
            }
        }
        DirectPicsMessageIDs.removeAll(toDeleteList);
    }

    private static void GetDirectPic(String messageID, Context context) {
        DirectPicsDownloadRunning = true;
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            new MessageAPICalls(context).GetDirectPicForMessage(messageID, (data, ExtraData) -> {
                DirectPicsDownloadRunning = false;
                int index = DirectPicsMessageIDs.indexOf(new DirectPicDownloadingInfo(messageID));
                if (index != -1) {
                    DirectPicsMessageIDs.remove(index);
                }
                MessageWindow.DirectPicDownloaded();
            }, (data, ExtraData) -> {
                DirectPicsDownloadRunning = false;
                int index = DirectPicsMessageIDs.indexOf(new DirectPicDownloadingInfo(messageID));
                if (index != -1) {
                    DirectPicsMessageIDs.get(index).FailureCount = DirectPicsMessageIDs.get(index).FailureCount + 1;
                }
            });
        }, null);
    }
}
