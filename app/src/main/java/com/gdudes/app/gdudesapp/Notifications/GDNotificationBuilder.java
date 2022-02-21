package com.gdudes.app.gdudesapp.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GDNotificationBuilder {

    private static final String GDChanneID = "gdudesapp";
    private static final String GDChanneName = "GDudes";
    private static final long[] vibratePattern = new long[]{0, 100, 50, 100};

    private NotificationChannel notificationChannel = null;

    public String notificationTitle;
    public String notificationText;
    public boolean includeLargeIcon;
    public boolean vibrate;
    public String picSrc;
    public android.app.PendingIntent pendingIntent;
    public NotificationStyleType notificationStyle = NotificationStyleType.BIG_TEXT;

    public String bigTitle;
    public String bigText;
    public String summary;
    public ArrayList<String> inboxTextLines;
    public Bitmap bigPic;

    private Bitmap largeIcon = null;
    private Uri soundUri = null;

    private final Uri SystemDefaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    public enum NotificationStyleType {
        BASIC,
        BIG_TEXT,
        INBOX,
        BIG_PIC
    }

    public NotificationCompat.Builder Build(Context context) {
        SetOptions(context);
        InitChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GDChanneID);
        builder.setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_gdlogo)
                .setLargeIcon(largeIcon)
                .setLocalOnly(true)
                .setAutoCancel(true);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        if (vibrate) {
            builder.setVibrate(vibratePattern);
        }
        if (soundUri != null) {
            builder.setSound(soundUri);
        }

        SetNotificationStyleOptions(builder);
        return builder;
    }

    private void SetNotificationStyleOptions(NotificationCompat.Builder builder) {
        switch (notificationStyle) {
            case BASIC:
                break;
            case BIG_TEXT:
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                bigTextStyle.setBigContentTitle(bigTitle);
                bigTextStyle.bigText(bigText);
                bigTextStyle.setSummaryText(summary);
                builder.setStyle(bigTextStyle);
                break;
            case INBOX:
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                for (int i = 0; i < inboxTextLines.size(); i++) {
                    inboxStyle.addLine(inboxTextLines.get(i));
                }
                inboxStyle.setBigContentTitle(bigTitle);
                inboxStyle.setSummaryText(summary);
                builder.setStyle(inboxStyle);
                break;
            case BIG_PIC:
                NotificationCompat.BigPictureStyle bigPicStyle = new NotificationCompat.BigPictureStyle();
                bigPicStyle.setBigContentTitle(bigTitle);
                bigPicStyle.setSummaryText(summary);
                bigPicStyle.bigPicture(bigPic);
                builder.setStyle(bigPicStyle);
                break;
        }
    }

    private void SetOptions(Context context) {
        if (includeLargeIcon) {
            largeIcon = !StringHelper.IsNullOrEmpty(picSrc) ? GetScaledBitmap(context, ImageHelper.GetBitmapFromString(picSrc)) :
                    GetScaledBitmap(context, BitmapFactory.decodeResource(context.getResources(), R.drawable.defaultuserprofilepic));
        }
        String UserNotificationTone = PersistantPreferencesHelper.GetAppSettings().NotificationTone;
        if (UserNotificationTone.equals("99")) {    //Default system
            soundUri = SystemDefaultSoundUri;
        } else if (UserNotificationTone.equals("4")) { //Off
            soundUri = null;
        } else {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/raw/gdn" + UserNotificationTone);
        }
    }

    private static Bitmap GetScaledBitmap(Context context, Bitmap UserImage) {
        Resources res = context.getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        return Bitmap.createScaledBitmap(UserImage, width, height, false);
    }

    private void InitChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || notificationChannel != null) {
            return;
        }
        notificationChannel = new NotificationChannel(GDChanneID, GDChanneName, NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);

        if (vibrate) {
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(vibratePattern);
        }

        if (soundUri != null) {
            notificationChannel.setSound(soundUri, null);
        }

        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
