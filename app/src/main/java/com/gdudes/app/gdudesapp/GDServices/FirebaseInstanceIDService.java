package com.gdudes.app.gdudesapp.GDServices;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gdudes.app.gdudesapp.APICaller.APICalls.LoginAPICalls;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.FirebaseNotificationToken;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseInstanceIDService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String var1) {
        super.onNewToken(var1);

        String token = FirebaseInstanceId.getInstance().getToken();
        SessionManager.SetFirebaseNotificationToken(new FirebaseNotificationToken(token));
        SendRegistrationToServer(getApplicationContext(), token);
    }

    private static void CreateTokenIfNeeded() {
        FirebaseNotificationToken token = SessionManager.GetFirebaseNotificationToken();
        if (token == null) {
            String sToken = FirebaseInstanceId.getInstance().getToken();
            if (sToken != null && !sToken.isEmpty()) {
                SessionManager.SetFirebaseNotificationToken(new FirebaseNotificationToken(sToken));
            }
        }
    }

    public static void UpdateTokenToServerIfNeeded(Context context) {
        CreateTokenIfNeeded();
        FirebaseNotificationToken token = SessionManager.GetFirebaseNotificationToken();
        if (token == null) {
            return;
        }
        if (!token.IsUpdatedToServer) {
            SendRegistrationToServer(context, token.token);
        } else {
            String sToken = FirebaseInstanceId.getInstance().getToken();
            //With below condition, if user logs in to another device, he will only receive notifications on that device.
            //With this commented out, when he logs into old device again (this being old device) the token is updated again.
            //So always only current device receives notifications.
            //This is called only on app startup. So only one extra API call.
//            if (sToken != null && !token.token.equalsIgnoreCase(sToken)) {
            SessionManager.SetFirebaseNotificationToken(new FirebaseNotificationToken(sToken));
            SendRegistrationToServer(context, sToken);
//            }
        }
    }

    private static void SendRegistrationToServer(Context context, String token) {
        Users user = SessionManager.GetLoggedInUser();
        if (user == null || StringHelper.IsNullOrEmpty(user.UserID)) {
            return;
        }
        new LoginAPICalls(context).RegisterForNotification(token, (data, ExtraData) -> {
            FirebaseNotificationToken nToken = SessionManager.GetFirebaseNotificationToken();
            if (nToken == null) {
                return;
            }
            nToken.IsUpdatedToServer = true;
            SessionManager.SetFirebaseNotificationToken(nToken);
        });
    }
}
