package com.gdudes.app.gdudesapp.Notifications;

import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import com.gdudes.app.gdudesapp.Comparators.GDMessageAndNotificationComparator;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.GDNotification;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.activities.MessageWindow;
import com.gdudes.app.gdudesapp.activities.Pics.GDPicViewerActivity;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationUtils {

    public static final int MaxMessagesForInboxStyle = 7;

    /**
     * If a message window is open then remove messages sent by that user
     *
     * @param messages
     */
    public static void RemoveMessagesForOpenConversation(ArrayList<GDMessage> messages) {
        if (StringHelper.IsNullOrEmpty(MessageWindow.PubConvWithUserID)) {
            return;
        }
        ArrayList<GDMessage> MessagesToRemove = new ArrayList<>();
        for (GDMessage message : messages) {
            if (message.SenderID.equalsIgnoreCase(MessageWindow.PubConvWithUserID)) {
                MessagesToRemove.add(message);
            }
        }
        messages.removeAll(MessagesToRemove);
    }

    public static Boolean IsPicNotification(GDNotification notification) {
        String notificationType = notification.NotificationType;
        return notificationType.trim().equals("PCA") || notificationType.trim().equals("PC")
                || notificationType.trim().equals("PLD");
    }

    public static Intent GetSingleChatIntent(Context context, GDMessage message) {
        Intent intent = new Intent(context, MessageWindow.class);
        intent.putExtra("ConvWithUserID", message.SenderID);
        intent.putExtra("ConvWithUserName", StringEncoderHelper.decodeURIComponent(message.SenderName));
        intent.putExtra("ConvWithUserPicID", message.PicID);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MessageWindow.class);
        taskStackBuilder.addNextIntent(intent);
        return intent;
    }

    public static Intent GetSingleNotificationIntent(Context context, GDNotification notification, String loggedInUserID) {
        Intent intent = null;
        try {
            switch (notification.NotificationType.trim()) {
                case "PCA":
                    //Owner also commented on his Picture
                    intent = new Intent(context, GDPicViewerActivity.class);
                    intent.putExtra("SinglePicID", notification.LinkID);
                    intent.putExtra("SinglePicOwnerID", notification.SenderID);
                    break;
                case "PC":
                    //Picture Comment
                case "PLD":
                    //Picture Like Dislike
                    intent = new Intent(context, GDPicViewerActivity.class);
                    intent.putExtra("SinglePicID", notification.LinkID);
                    intent.putExtra("SinglePicOwnerID", loggedInUserID);
                    break;
                case "IB":
                    //Ice-Breaker
                case "GPR":
                    //GDudes Popularity Rank
                    intent = new Intent(context, NewProfileViewActivity.class);
                    intent.putExtra("ClickedUserID", notification.SenderID);
                    if (!StringHelper.IsNullOrEmpty(notification.PicID)) {
                        intent.putExtra("ProfilePicID", notification.PicID);
                    }
                    break;
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return intent;
    }


    public static void SetupSingleMessageNotification(GDNotificationBuilder notificationBuilder, GDMessage message) {
        //Show big text type notification
        notificationBuilder.notificationStyle = GDNotificationBuilder.NotificationStyleType.BIG_TEXT;
        notificationBuilder.notificationTitle = "Message from " + DecodeText(message.SenderName);
        notificationBuilder.notificationText = (message.MessageContainsPhoto() ? "(shared photo)" : "")
                + (message.HasLocation() ? "(shared location)" : "")
                + StringEncoderHelper.doubleDecodeURIComponent(message.MessageText);
        notificationBuilder.bigTitle = notificationBuilder.notificationTitle;

        if (message.MessageContainsPhoto() && message.HasLocation()) {
            notificationBuilder.summary = "Message contains photo and location";
        } else if (message.MessageContainsPhoto()) {
            notificationBuilder.summary = "Message contains photo";
        } else if (message.HasLocation()) {
            notificationBuilder.summary = "Message contains location";
        }
    }

    public static void SetupSingleGDNotificationNotification(GDNotificationBuilder notificationBuilder, GDImageDBHelper gdImageDBHelper, GDNotification notification) {
        //Show single notification for notification
        if (NotificationUtils.IsPicNotification(notification)) {
            String BigPicSrc = "";
            notificationBuilder.bigPic = notification.image;
            if (notificationBuilder.bigPic == null) {
                BigPicSrc = gdImageDBHelper.GetImageStringByPicID(notification.LinkID, true);
                if (BigPicSrc != null && !BigPicSrc.equals("")) {
                    notificationBuilder.bigPic = ImageHelper.GetBitmapFromString(BigPicSrc);
                }
            }
            if (notificationBuilder.bigPic != null) {
                notificationBuilder.notificationStyle = GDNotificationBuilder.NotificationStyleType.BIG_PIC;
            } else {
                notificationBuilder.notificationStyle = GDNotificationBuilder.NotificationStyleType.BIG_TEXT;
            }
        } else {
            notificationBuilder.notificationStyle = GDNotificationBuilder.NotificationStyleType.BIG_TEXT;
        }
        notificationBuilder.notificationTitle = "New notification";
        if (notification.NotificationType.trim().equals("IB")) {
            notificationBuilder.notificationText = DecodeText(notification.SenderName) + ": says " + GDGenericHelper.GetIceBreakMessageFromCode(notification.Message);
        } else {
            notificationBuilder.notificationText = DecodeText(notification.SenderName) + ": " + DoubleDecodeText(notification.Message);
        }
        notificationBuilder.bigTitle = notificationBuilder.notificationTitle;
        notificationBuilder.summary = notificationBuilder.notificationText;
    }

    public static void SetupInboxStyleNotification(GDNotificationBuilder notificationBuilder, ArrayList<GDMessage> messages, ArrayList<GDNotification> notifications,
                                                   ArrayList<String> UniqueUsers, int ChatCount, String AllInboundUnreadCount) {
        ArrayList<String> MessageLines = new ArrayList<>();
        ArrayList<Object> GDMessageAndNotificationList = new ArrayList<>();
        GDMessageAndNotificationList.addAll(messages);
        GDMessageAndNotificationList.addAll(notifications);
        Collections.sort(GDMessageAndNotificationList, new GDMessageAndNotificationComparator());
        //show inbox type notification
        int linesCount = (GDMessageAndNotificationList.size() <= MaxMessagesForInboxStyle) ? GDMessageAndNotificationList.size() : MaxMessagesForInboxStyle;
        for (int i = 0; i < linesCount; i++) {
            if (UniqueUsers.size() == 1 && ChatCount == 1) {
                if (GDMessageAndNotificationList.get(i).getClass().equals(GDMessage.class)) {
                    GDMessage message = (GDMessage) GDMessageAndNotificationList.get(i);
                    if (message.MessageContainsPhoto()) {
                        MessageLines.add("(shared photo) " + DoubleDecodeText(message.MessageText));
                    } else if (message.HasLocation()) {
                        MessageLines.add("(shared location) " + DoubleDecodeText(message.MessageText));
                    } else {
                        MessageLines.add(DoubleDecodeText(message.MessageText));
                    }
                } else {
                    GDNotification notification = (GDNotification) GDMessageAndNotificationList.get(i);
                    if (notification.NotificationType.trim().equals("IB")) {
                        MessageLines.add("says " + GDGenericHelper.GetIceBreakMessageFromCode(notification.Message));
                    } else {
                        MessageLines.add(DecodeText(notifications.get(0).SenderName) + ": " + notification.Message);
                    }
                }
            } else {
                if (GDMessageAndNotificationList.get(i).getClass().equals(GDMessage.class)) {
                    GDMessage message = (GDMessage) GDMessageAndNotificationList.get(i);
                    if (message.MessageContainsPhoto()) {
                        MessageLines.add(DecodeText(message.SenderName) + ": (shared photo) " + DoubleDecodeText(message.MessageText));
                    } else if (message.HasLocation()) {
                        MessageLines.add(DecodeText(message.SenderName) + ": (shared location) " + DoubleDecodeText(message.MessageText));
                    } else {
                        MessageLines.add(DecodeText(message.SenderName) + ": " + DoubleDecodeText(message.MessageText));
                    }
                } else {
                    GDNotification notification = (GDNotification) GDMessageAndNotificationList.get(i);
                    if (notification.NotificationType.trim().equals("IB")) {
                        MessageLines.add(DecodeText(notification.SenderName) + ": says " + GDGenericHelper.GetIceBreakMessageFromCode(notification.Message));
                    } else {
                        MessageLines.add(DecodeText(notifications.get(0).SenderName) + ": " + notification.Message);
                    }
                }
            }
        }
        notificationBuilder.inboxTextLines = MessageLines;

        notificationBuilder.notificationStyle = GDNotificationBuilder.NotificationStyleType.INBOX;

        if (messages.size() > 0 && notifications.size() > 0) {
            notificationBuilder.notificationTitle = "New messages & notifications";
        } else if (messages.size() > 0) {
            notificationBuilder.notificationTitle = AllInboundUnreadCount + " unread messages";
        } else if (notifications.size() > 0) {
            notificationBuilder.notificationTitle = Integer.toString(notifications.size()) + " new notifications";
        }

        if (UniqueUsers.size() == 1 && ChatCount == 1) {
            notificationBuilder.notificationText = AllInboundUnreadCount + " unread messages from " + DecodeText(messages.get(0).SenderName);
        } else {
            if (ChatCount == 0) {
                notificationBuilder.notificationText = AllInboundUnreadCount + " unread messages";
            } else {
                notificationBuilder.notificationText = AllInboundUnreadCount + " unread messages in " + Integer.toString(ChatCount) + " chats";
            }
        }
        if (notifications.size() > 0) {
            if (messages.size() == 0) {
                notificationBuilder.notificationText = Integer.toString(notifications.size()) + " notifications";
            } else {
                notificationBuilder.notificationText += ", " + Integer.toString(notifications.size()) + " notifications";
            }
        }
        notificationBuilder.bigTitle = notificationBuilder.notificationTitle;
        notificationBuilder.summary = notificationBuilder.notificationText;
    }

    private static String DecodeText(String text) {
        return StringEncoderHelper.decodeURIComponent(text);
    }

    private static String DoubleDecodeText(String text) {
        return StringEncoderHelper.doubleDecodeURIComponent(text);
    }
}
