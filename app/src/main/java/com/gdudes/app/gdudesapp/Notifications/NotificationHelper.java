package com.gdudes.app.gdudesapp.Notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.gdudes.app.gdudesapp.Comparators.MessagesComparator;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDServices.NotificationIntentCounter;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.GDNotification;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncHelper;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDBackgroundTaskFinished;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.activities.MainLayout.LayoutActivity;
import com.gdudes.app.gdudesapp.activities.MessageWindow;

import java.util.ArrayList;
import java.util.Collections;

import static com.gdudes.app.gdudesapp.Notifications.NotificationUtils.MaxMessagesForInboxStyle;

public class NotificationHelper {
    private static ArrayList<GDNotification> ToShowNotificationList = new ArrayList<>();
    private static ArrayList<GDNotification> ShownNotificationList = new ArrayList<>();
    private static Context NotificationShownContext;


    public static void ShowNotifications(Context context, ArrayList<GDMessage> messages, String LoggedInUserID) {
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            GetNotificationBuilder(gdBackgroundTaskFinished, context, messages, LoggedInUserID);
        }, data -> {

            GDNotificationBuilder notificationBuilder = (GDNotificationBuilder) data;
            Notification notification = notificationBuilder.Build(context).build();

            //Cancel old and show new notification.
            for (int i = 0; i < NotificationIntentCounter.NotificationCount; i++) {
                NotificationManagerCompat.from(context).cancel(i);
            }
            NotificationManagerCompat.from(context).notify(NotificationIntentCounter.NotificationCount, notification);

            NotificationShownContext = context;
            NotificationIntentCounter.NotificationCount = NotificationIntentCounter.NotificationCount + 1;
            ShownNotificationList.addAll(ToShowNotificationList);
            ToShowNotificationList.clear();

        });
    }

    public static void GetNotificationBuilder(final GDBackgroundTaskFinished gdBackgroundTaskFinished, Context context,
                                              ArrayList<GDMessage> messages, String LoggedInUserID) {
        if (!PersistantPreferencesHelper.GetAppSettings().ShowNotifications.equals("1")) {
            return;
        }
        if (messages.size() == 0 && ToShowNotificationList.size() == 0) {
            return;
        }
        try {
            NotificationUtils.RemoveMessagesForOpenConversation(messages);
            if (messages.size() == 0 && ToShowNotificationList.size() == 0) {
                return;
            }

            GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(context);
            GDMessagesDBHelper messagesDBHelper = new GDMessagesDBHelper(context);

            int AllInboundUnreadCount = 0;
            //If it is only Pic notification, Big pic is shown in notification.
            Boolean ToShowPicNotificationOnly = ToShowNotificationList.size() == 1 && messages.size() == 0
                    && NotificationUtils.IsPicNotification(ToShowNotificationList.get(0));
            if (!ToShowPicNotificationOnly) {
                //Show 7 messages in total. If list contains less than 7 messages then get unread messages from DB
                AllInboundUnreadCount = messagesDBHelper.GetAllUnreadInboundMessageCount(LoggedInUserID);
                //The messages currently in MessageList will also be returned in next statement,
                //so get first MaxMessagesForInboxStyle + MessageList.size() messages
                AddUniqueMessages(messagesDBHelper.GetFirstNUnreadInboundMessages(LoggedInUserID, (MaxMessagesForInboxStyle + messages.size())), messages);
                Collections.sort(messages, new MessagesComparator());
            }

            ArrayList<String> UniqueUsers = GDMessage.GetSenderIDList(messages);
            UniqueUsers.addAll(GDNotification.GetUserIDList(ToShowNotificationList));

            int ChatCount = messagesDBHelper.GetChatCountWithUnreadMessages(LoggedInUserID);
            GDNotificationBuilder notificationBuilder = new GDNotificationBuilder();

            //Add back intents
            Intent[] allIntents = GetIntents(context, notificationBuilder, gdImageDBHelper, messages, UniqueUsers, ChatCount, LoggedInUserID);

            if ((messages.size() + ToShowNotificationList.size()) == 1) {
                if (messages.size() == 1) {
                    NotificationUtils.SetupSingleMessageNotification(notificationBuilder, messages.get(0));
                } else {
                    NotificationUtils.SetupSingleGDNotificationNotification(notificationBuilder, gdImageDBHelper, ToShowNotificationList.get(0));
                }
            } else {
                NotificationUtils.SetupInboxStyleNotification(notificationBuilder, messages, ToShowNotificationList, UniqueUsers, ChatCount,
                        Integer.toString(AllInboundUnreadCount));
            }

            notificationBuilder.pendingIntent = PendingIntent.getActivities(context, 0, allIntents, PendingIntent.FLAG_UPDATE_CURRENT);
            gdBackgroundTaskFinished.OnBackgroundTaskFinished(notificationBuilder);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowNotificationsForNotificationList(Context context, ArrayList<GDNotification> NotificationsList,
                                                            String LoggedInUserID) {
        try {
            Boolean NotificationAdded = false;
            for (int i = 0; i < NotificationsList.size(); i++) {
                if (!ToShowNotificationList.contains(NotificationsList.get(i)) && !ShownNotificationList.contains(NotificationsList.get(i))) {
                    ToShowNotificationList.add(NotificationsList.get(i));
                    NotificationAdded = true;
                }
            }
            if (NotificationAdded) {
                ShowNotifications(context, new ArrayList<>(), LoggedInUserID);
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    private static Intent[] GetIntents(Context context, GDNotificationBuilder notificationBuilder, GDImageDBHelper gdImageDBHelper,
                                       ArrayList<GDMessage> messages, ArrayList<String> uniqueUsers, int chatCount,
                                       String loggedInUserID) {
        //Open LayoutActivity on back button press, and open Chat Tab
        Intent backIntent = new Intent(context, LayoutActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent[] allIntents = new Intent[]{backIntent};

        if (messages.size() > 0) {
            backIntent.putExtra("SelectTab", 2);
            if (uniqueUsers.size() == 1 && chatCount == 1 && ToShowNotificationList.size() == 0) {
                if (MessageWindow.PubConvWithUserID.equalsIgnoreCase(messages.get(0).SenderID)) {
                    return allIntents;
                }
                notificationBuilder.includeLargeIcon = true;
                notificationBuilder.picSrc = gdImageDBHelper.GetImageStringByPicID(messages.get(messages.size() - 1).PicID, false);
                allIntents = new Intent[]{backIntent, NotificationUtils.GetSingleChatIntent(context, messages.get(0))};
            } else {
                allIntents = new Intent[]{backIntent};
            }
        } else {
            backIntent.putExtra("SelectTab", 3);
            if (ToShowNotificationList.size() == 1) {
                notificationBuilder.includeLargeIcon = true;
                notificationBuilder.picSrc = gdImageDBHelper.GetImageStringByPicID(ToShowNotificationList.get(ToShowNotificationList.size() - 1).PicID, false);
                Intent intent = NotificationUtils.GetSingleNotificationIntent(context, ToShowNotificationList.get(0), loggedInUserID);
                allIntents = intent == null ? new Intent[]{backIntent} : new Intent[]{backIntent, intent};
            } else {
                allIntents = new Intent[]{backIntent};
            }
        }
        return allIntents;
    }

    public static void CancelAllNotifications() {
        try {
            if (NotificationShownContext != null) {
                NotificationManagerCompat.from(NotificationShownContext).cancelAll();
                NotificationIntentCounter.NotificationCount = 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void MarkNotificationsAsSeen() {
        if (ShownNotificationList != null) {
            ShownNotificationList.clear();
        }
    }

    private static void AddUniqueMessages(ArrayList<GDMessage> FromList, ArrayList<GDMessage> ToList) {
        try {
            ArrayList<GDMessage> ListToAdd = new ArrayList<>();
            for (int i = 0; i < FromList.size(); i++) {
                if (!ToList.contains(FromList.get(i))) {
                    ListToAdd.add(FromList.get(i));
                }
            }
            ToList.addAll(ListToAdd);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }
}
