package com.gdudes.app.gdudesapp.GDTypes;

import android.graphics.Bitmap;

import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.util.ArrayList;
import java.util.List;

public class GDNotification {
    public String NotificationID = "";
    public String UserID = "";
    public String SenderID = "";
    public String SenderName = "";
    public String NotificationType = "";
    public String LinkID = "";
    public String Message = "";
    public String NotificationDateTime = "";
    public int NotificationSeen = 0;
    public int NotificationRead = 0;
    public int UnseenCount = 0;
    public String PicID = "";
    public Boolean IsDataRefreshed = false;
    public int PopularityRank = 0;
    public Bitmap image = null;

    public GDNotification() {
    }

    public GDNotification(String notificationID) {
        this.NotificationID = notificationID;
    }

    public static ArrayList<String> GetUserIDList(ArrayList<GDNotification> notifications) {
        ArrayList<String> userIDs = new ArrayList<>();
        for (int i = 0; i < notifications.size(); i++) {
            userIDs.add(notifications.get(i).SenderID);
        }
        return StringHelper.RemoveDuplicateEntries(userIDs);
    }

    public static ArrayList<String> GetPicIDList(ArrayList<GDNotification> notifications) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < notifications.size(); i++) {
            String picID = notifications.get(i).PicID;
            if (!StringHelper.IsNullOrEmpty(picID)) {
                picIDs.add(notifications.get(i).PicID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static ArrayList<String> GetPicIDListForNullImages(ArrayList<GDNotification> notifications) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < notifications.size(); i++) {
            String picID = notifications.get(i).PicID;
            if (!StringHelper.IsNullOrEmpty(picID) && notifications.get(i).image == null) {
                picIDs.add(notifications.get(i).PicID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static void SetDataRefreshed(List<GDNotification> ToList, ArrayList<GDNotification> FromList) {
        for (int i = 0; i < FromList.size(); i++) {
            for (int j = 0; j < ToList.size(); j++) {
                if (FromList.get(i).SenderID.equalsIgnoreCase(ToList.get(j).SenderID)) {
                    ToList.get(j).SenderName = FromList.get(i).SenderName;
                    ToList.get(j).PicID = FromList.get(i).PicID;
                    ToList.get(j).IsDataRefreshed = true;
                }
            }
        }
    }

    public static void SetPicsToNotifications(ArrayList<GDPic> pics, ArrayList<GDNotification> notifications) {
        for (int i = 0; i < pics.size(); i++) {
            for (int j = 0; j < notifications.size(); j++) {
                String nPicID = notifications.get(j).PicID;
                if (!StringHelper.IsNullOrEmpty(nPicID) && nPicID.equalsIgnoreCase(pics.get(i).PicID)) {
                    notifications.get(j).image = pics.get(i).image;
                }
            }
        }
    }

    @Override
    public boolean equals(Object second) {
        if (this.NotificationID.equalsIgnoreCase(((GDNotification) second).NotificationID)) {
            return true;
        }
        return false;
    }
}
