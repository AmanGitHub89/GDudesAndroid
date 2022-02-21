package com.gdudes.app.gdudesapp.GDServices;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.List;

public class GDudesFirebaseMessagingService extends FirebaseMessagingService {

    private static String LogClass = "GDudesFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage == null) {
            return;
        }

        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataNotification(json);
            } catch (Exception ex) {
                GDLogHelper.LogException(ex);
            }
        }
    }

    private void handleDataNotification(JSONObject json) {
        try {
            String NType = json.getString("NType");
            Context context = getApplicationContext();
            if (NType.equals("NM")) {
                MessageAndNotificationDownloader.GetMessagesFromServer(context);
            } else if (NType.equals("MD")) {
                MessageAndNotificationDownloader.GetMessageStatusUpdates(context);
            } else {
                MessageAndNotificationDownloader.GetNotifications(context);
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String activeProcess : processInfo.pkgList) {
                            if (activeProcess.equals(context.getPackageName())) {
                                isInBackground = false;
                            }
                        }
                    }
                }
            } else {
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                if (componentInfo.getPackageName().equals(context.getPackageName())) {
                    isInBackground = false;
                }
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return false;
        }
        return isInBackground;
    }
}
