package com.gdudes.app.gdudesapp.GDServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APICalls.MessageAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.Comparators.MessagesComparator;
import com.gdudes.app.gdudesapp.Database.GDConversationsDBHelper;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zMessageIDAndDT;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDTimer;
import com.gdudes.app.gdudesapp.Helpers.GPSHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Interfaces.GDTimerTaskRun;
import com.gdudes.app.gdudesapp.activities.MainLayout.HomePageFragment;
import com.gdudes.app.gdudesapp.activities.MainLayout.LayoutActivity;
import com.gdudes.app.gdudesapp.activities.MainLayout.MessagesPageFragment;
import com.gdudes.app.gdudesapp.activities.MessageWindow;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GDMessageService extends Service {

    private static String LogClass = "GDMessageService";
    public static GDMessageService ServiceInstance;
    private static String LoggedInUserID;
    private static Users LoggedInUser;
    private static Boolean RestartService = true;
    private GDMessagesDBHelper mGDMessagesDBHelper;
    private GDConversationsDBHelper mGDConversationsDBHelper;

    private GDTimer mTimerSendPending;
    private GDTimer mTimerDeleteMarkedConversations;
    private GDTimer mTimerUploadErrorLog;
    private GDTimer mTimerGetUserServiceUpdates;

    private Boolean IsSendPendingRunning = false;
    private Boolean IsDeleteMarkedConversationsRunning = false;
    private Boolean IsGetUserServiceUpdatesRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Log.d("GDLog", "GDMessageService - onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //Log.d("GDLog", "onStartCommand");
            GDLogHelper.Log(LogClass, "onStartCommand", "onStartCommand");
//            if (ServiceInstance != null) {
//                Log.d("GDLog", "Service instance is not null : return START_NOT_STICKY");
//                return START_NOT_STICKY;
//            }
            ServiceInstance = this;
            if (!SessionManager.IsSessionManagerInitiated) {
                SessionManager.InitSessionManager(getApplicationContext());
            }
            if (!PersistantPreferencesHelper.IsPersistantPreferencesInitiated) {
                PersistantPreferencesHelper.InitPersistantPreferences(getApplicationContext());
            }
            if (HomePageFragment.HomePageInstance != null) {
                GPSHelper.InitGPSHelper(getApplicationContext());
            }

            LoggedInUser = SessionManager.GetLoggedInUser(getApplicationContext());
            if (LoggedInUser == null || LoggedInUser.UserID == null || LoggedInUser.UserID.equals("")) {
                GDLogHelper.Log(LogClass, "onStartCommand",
                        (LoggedInUser == null ? "LoggedInUser is null. "
                                : LoggedInUser.UserID == null ? "LoggedInUser.UserID is null. "
                                : "LoggedInUser.UserID is empty. ")
                                + "DestroyService(false) and return START_NOT_STICKY.");
                RestartService = false;
                DestroyService(false);
                return START_NOT_STICKY;
            }
            LoggedInUserID = LoggedInUser.UserID;
            mGDMessagesDBHelper = new GDMessagesDBHelper(this);
            mGDConversationsDBHelper = new GDConversationsDBHelper(this);
            StopTimers();
            StartTimers();
            MessageAndNotificationDownloader.StartDownloadingDirectMessagePics_IfNotRunning(GDMessageService.this);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            DestroyService(true);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Log.d("GDLog", "Service Destroyed");
        GDLogHelper.Log(LogClass, "onDestroy", "onDestroy");
        LoggedInUser = null;
        LoggedInUserID = "";
        ServiceInstance = null;
        TryRestartService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Log.d("GDLog", "Task removed.");
        GDLogHelper.Log(LogClass, "onTaskRemoved", "onTaskRemoved");
        LoggedInUser = null;
        LoggedInUserID = "";
        ServiceInstance = null;
        TryRestartService();
        super.onTaskRemoved(rootIntent);
    }

    private void TryRestartService() {
        if (RestartService) {
            try {
                //Log.d("GDLog", "Try restart service");
                GDLogHelper.Log(LogClass, "TryRestartService", "TryRestartService");
                Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
                restartServiceIntent.setPackage(getPackageName());
                //restartServiceIntent.putExtra("LoggedInUserID", LoggedInUserID);
                //restartServiceIntent.putExtra("IsPendingIntentStart", "Y");
                PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                alarmService.set(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + 15000,
                        restartServicePendingIntent);
            } catch (Exception ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
            }
        }
        StopTimers();
    }

    public void DestroyService(Boolean vRestartService) {
        GDLogHelper.Log(LogClass, "DestroyService", "vRestartService:" + Boolean.toString(vRestartService));
        RestartService = vRestartService;
        LoggedInUser = null;
        LoggedInUserID = "";
        ServiceInstance = null;
        stopSelf();
    }

    private void StartTimers() {
        try {
            mTimerSendPending = new GDTimer(5000, 10000, new Handler(), new GDTimerTaskRun() {
                @Override
                public void OnTimerElapsed() {
                    SendPendingMessages();
                    //Update read status for messages (r), if opened while offline.
                    ArrayList<String> UnSeenMessages = mGDMessagesDBHelper.GetAllOfflineReadMessageIDList(LoggedInUserID);
                    SendUpdateForInboundMessages(UnSeenMessages, "R");
                }
            });
            mTimerSendPending.Start();

            mTimerDeleteMarkedConversations = new GDTimer(20000, 60000, new Handler(), new GDTimerTaskRun() {
                @Override
                public void OnTimerElapsed() {
                    DeleteMarkedConversations();
                }
            });
            mTimerDeleteMarkedConversations.Start();

            //Start with a delay of 5 mins and run every 40 mins
            mTimerUploadErrorLog = new GDTimer(300000, 2400000, new Handler(), new GDTimerTaskRun() {
                @Override
                public void OnTimerElapsed() {
                    UploadErrorLogsToServer();
                }
            });
            mTimerUploadErrorLog.Start();

            //Messages, sent message status and notifications
            mTimerGetUserServiceUpdates = new GDTimer(10000, 10000, new Handler(), new GDTimerTaskRun() {
                @Override
                public void OnTimerElapsed() {
                    GetUserServiceUpdates();
                }
            });
            mTimerGetUserServiceUpdates.Start();

            GetMessagesFromServer();
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            DestroyService(true);
        }
    }

    private void StopTimers() {
        try {
            if (mTimerSendPending != null) {
                mTimerSendPending.Stop();
            }
            if (mTimerDeleteMarkedConversations != null) {
                mTimerDeleteMarkedConversations.Stop();
            }
            if (mTimerUploadErrorLog != null) {
                mTimerUploadErrorLog.Stop();
            }
            if (mTimerGetUserServiceUpdates != null) {
                mTimerGetUserServiceUpdates.Stop();
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }


    private void SendPendingMessages() {
        if (IsSendPendingRunning) {
            return;
        }
        try {
            IsSendPendingRunning = true;
            ArrayList<GDMessage> PendingMessages = mGDMessagesDBHelper.GetPendingMessages(LoggedInUserID);
            if (PendingMessages.size() == 0) {
                IsSendPendingRunning = false;
                return;
            }
            //Log.d("GDLog", "SendPendingMessages");
            GDMessage.SetDateForMessageList(PendingMessages);
            Collections.sort(PendingMessages, new MessagesComparator());
            ArrayList<GDMessage> UnSuccessfulMessagesList = GDMessage.SetBigImageForPendingForDirectPhotos(PendingMessages, GDMessageService.this);
            mGDMessagesDBHelper.UpdateMessageListStatusForError(UnSuccessfulMessagesList);

            //Messages that were not sent for more than 6 hours need to be marked as error.
            ArrayList<GDMessage> ToMarkErrorMessagesList = GDMessage.GetMessagesUnDeliveredForMoreThan6Hours(PendingMessages);
            mGDMessagesDBHelper.UpdateMessageListStatusForError(ToMarkErrorMessagesList);

            if (PendingMessages.size() > 8) {
                GDLogHelper.Log(LogClass, "SendPendingMessages", "Sending Count : " + Integer.toString(PendingMessages.size()));
            }
            final ArrayList<String> ConvWithUserIDList = new ArrayList<>();
            for (int i = 0; i < PendingMessages.size(); i++) {
                ConvWithUserIDList.add(PendingMessages.get(i).RecieverID);
            }
            StringHelper.RemoveDuplicateEntries(ConvWithUserIDList);

            APICallInfo apiCallInfo = new APICallInfo("Home", "NewUserMessageList", null, "POST", PendingMessages, null, false, null, APICallInfo.APITimeouts.LONG);
            apiCallInfo.CalledFromService = true;
            GDGenericHelper.executeAsyncPOSTAPITask(GDMessageService.this, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            return;
                        }
                        ArrayList<zMessageIDAndDT> SentMessages = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<zMessageIDAndDT>>() {
                        }.getType());
                        if (SentMessages == null || SentMessages.size() == 0) {
                            return;
                        }

                        //Mark error for messages that failed due to status - START
                        ArrayList<zMessageIDAndDT> ToMarkErrorMessagesListForRules = new ArrayList<zMessageIDAndDT>();
                        for (int i = 0; i < SentMessages.size(); i++) {
                            if (SentMessages.get(i).ErrorCode == -100) {
                                ToMarkErrorMessagesListForRules.add(SentMessages.get(i));
                            }
                        }
                        SentMessages.removeAll(ToMarkErrorMessagesListForRules);
                        if (ToMarkErrorMessagesListForRules.size() > 0) {
                            ArrayList<String> ToMarkErrorMessageIDList = new ArrayList<String>();
                            for (int i = 0; i < ToMarkErrorMessagesListForRules.size(); i++) {
                                ToMarkErrorMessageIDList.add(ToMarkErrorMessagesListForRules.get(i).MessageID);
                            }
                            mGDMessagesDBHelper.UpdateMessageListStatusForErrorWithMessageIDList(ToMarkErrorMessageIDList);
                        }
                        //Mark error for messages that failed due to status - START

                        mGDMessagesDBHelper.UpdateMessagesListStatusForSent(LoggedInUserID, SentMessages);
                        SetConvNeedsLocalRefreshIfRunning(ConvWithUserIDList);
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                    } finally {
                        IsSendPendingRunning = false;
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    IsSendPendingRunning = false;
                }
            });
        } catch (Exception ex) {
            IsSendPendingRunning = false;
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private void GetMessageStatusUpdates() {
        MessageAndNotificationDownloader.GetMessageStatusUpdates(getApplicationContext());
    }

    private void GetMessagesFromServer() {
        MessageAndNotificationDownloader.GetMessagesFromServer(getApplicationContext());
    }

    private void SendUpdateForInboundMessages(ArrayList<String> MessageIDList, final String Status) {
        Context context = GDMessageService.this;
        final GDMessagesDBHelper messagesDBHelper = new GDMessagesDBHelper(context);
        MessageAndNotificationDownloader.SendUpdateForInboundMessages(context, LoggedInUserID, messagesDBHelper, MessageIDList, Status);
    }

    private void DeleteMarkedConversations() {
        final ArrayList<String> DeletionMarkedConversations;
        if (IsDeleteMarkedConversationsRunning) {
            return;
        }
        IsDeleteMarkedConversationsRunning = true;
        try {
            DeletionMarkedConversations = mGDConversationsDBHelper.GetDeletionMarkedConversations(LoggedInUserID);
            if (DeletionMarkedConversations.size() == 0) {
                IsDeleteMarkedConversationsRunning = false;
                return;
            }
            for (int i = 0; i < DeletionMarkedConversations.size(); i++) {
                new MessageAPICalls(GDMessageService.this).DeleteUserConversation(DeletionMarkedConversations.get(i));
            }
        } catch (Exception ex) {
            IsDeleteMarkedConversationsRunning = false;
            GDLogHelper.LogException(ex);
        }
    }

    private void GetNotificationsForNotification() {
        MessageAndNotificationDownloader.GetNotifications(getApplicationContext());
    }

    private void UploadErrorLogsToServer() {
        GDLogHelper.UploadErrorLogsToServer(GDMessageService.this,false);
    }

    private void GetUserServiceUpdates() {
        if (IsGetUserServiceUpdatesRunning) {
            return;
        }
        IsGetUserServiceUpdatesRunning = true;
        try {
            List<APICallParameter> pAPICallParameters = new ArrayList<>();
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUserID));
            APICallInfo apiCallInfo = new APICallInfo("Home", "GetUserServiceUpdates", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.LONG);
            apiCallInfo.CalledFromService = true;
            GDGenericHelper.executeAsyncAPITask(GDMessageService.this, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            return;
                        }
                        UserServiceUpdates userServiceUpdates = new GsonBuilder().create().fromJson(result, UserServiceUpdates.class);
                        if (userServiceUpdates != null) {
                            if (userServiceUpdates.DLoad != null && userServiceUpdates.DLoad.equals("1")) {
                                GetMessagesFromServer();
                            }
                            if (userServiceUpdates.Upd != null && userServiceUpdates.Upd.equals("1")) {
                                GetMessageStatusUpdates();
                            }
                            if (userServiceUpdates.Notif != null && userServiceUpdates.Notif.equals("1")) {
                                GetNotificationsForNotification();
                            }
                        }
                    } catch (IllegalStateException ex) {
                        //GDLogHelper.Log(LogClass, "GetUserServiceUpdates", "IllegalStateException - " + result, GDLogHelper.LogLevel.EXCEPTION);
                    } catch (JsonSyntaxException ex) {
                        //GDLogHelper.Log(LogClass, "GetUserServiceUpdates", "JsonSyntaxException - " + result, GDLogHelper.LogLevel.EXCEPTION);
                    } catch (Exception e) {
                        GDLogHelper.LogException(e);
                    } finally {
                        IsGetUserServiceUpdatesRunning = false;
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    IsGetUserServiceUpdatesRunning = false;
                }
            });
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            IsGetUserServiceUpdatesRunning = false;
        }
    }


    private void SetConvNeedsLocalRefreshIfRunning(ArrayList<String> ConvWithUserIDList) {
        if (MessagesPageFragment.MessagesPageInstance != null) {
            MessagesPageFragment.MessagesPageInstance.SetConversationsNeedLocalRefresh(ConvWithUserIDList);
        }
        LayoutActivity.TabIconCountsNeedRefresh = true;
    }

    class UserServiceUpdates {
        public String DLoad;
        public String Upd;
        public String Notif;
    }
}
