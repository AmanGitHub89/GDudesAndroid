package com.gdudes.app.gdudesapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APICalls.MessageAPICalls;
import com.gdudes.app.gdudesapp.Adapters.MessagesAdapter;
import com.gdudes.app.gdudesapp.Comparators.MessagesComparator;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.Database.GDConversationsDBHelper;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDServices.MessageAndNotificationDownloader;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zMessageIDAndDT;
import com.gdudes.app.gdudesapp.GDTypes.GDGuidAndGuid;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.GDNewMessage;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.UserLocation;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDTimer;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.GDSubGenericAction;
import com.gdudes.app.gdudesapp.Interfaces.GetFileWritePermission;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.Interfaces.OnGDMessageClickListener;
import com.gdudes.app.gdudesapp.Notifications.NotificationHelper;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDMapActivity;
import com.gdudes.app.gdudesapp.activities.MainLayout.MessagesPageFragment;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;
import com.gdudes.app.gdudesapp.subActivity.ChatAttachment;
import com.google.gson.GsonBuilder;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MessageWindow extends GDAppCompatActivity implements GDSubGenericAction {

    private static String mLogClass = "MessageWindow";
    private Menu mMenu;
    private Boolean AreMessageOptionsVisible = false;
    private int mSelectedMessages = 0;
    private MessageWindow WindowInstance;
    private Drawable DefaultUserImage = null;

    private Boolean bGetClickedUserDetails = false;
    private Users LoggedInUser;
    private Users ConvWithUser;
    private String ConvWithUserID;
    private String ConvWithUserName;
    private String ConvWithUserPicID = "";

    public static String PubConvWithUserID = "";
    private static Boolean NewMessagesAvailable = false;
    private static Boolean SentMessagesStatusUpdatesAvailable = false;
    private static Boolean ReceivedMessagesStatusUpdatesAvailable = false;
    private static Boolean NewDirectPicAvailable = false;

    private MessagesAdapter messagesAdapter = null;
    private GDMessagesDBHelper mGDMessagesDBHelper;
    private GDConversationsDBHelper mGDConversationsDBHelper;
    private GDTimer mUpdateListTimer;
    private CountDownTimer SearchMessagesTimer;
    private Boolean IsSearchTextActive = false;
    private ArrayList<GDMessage> mMessageList;
    ArrayList<GDGuidAndGuid> mSelectedPicIDsAndFolderIDs;
    private ListView messagesList;

    private Context mContext;
    private ImageView sendMessageButton;
    private EditText newMessageText;
    private ImageView Attach;

    private LinearLayout MessageAttachmentLayout;
    private Boolean IsAttachmentsShown = false;

    private String DirectPhonePhoto = "";
    private String DirectPhonePhotoPath = "";
    private GetFileWritePermission CurrentGetFileWritePermission;
    private String CameraPhotoPath = "";
    private GetCameraPermission mGetCameraPermission;

    private ChatAttachment ChatAttachmentInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_window);
        LoadActivityForIntent(getIntent());
        MessageAndNotificationDownloader.StartDownloadingDirectMessagePics_IfNotRunning(MessageWindow.this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AreMessageOptionsVisible = false;
        mSelectedMessages = 0;
        bGetClickedUserDetails = false;
        ConvWithUser = null;
        ConvWithUserID = null;
        ConvWithUserName = null;
        ConvWithUserPicID = "";
        PubConvWithUserID = "";
        messagesAdapter = null;
        IsSearchTextActive = false;
        mMessageList = null;
        mSelectedPicIDsAndFolderIDs = null;
        messagesList = null;
        DirectPhonePhoto = "";
        DirectPhonePhotoPath = "";
        LoadActivityForIntent(intent);
    }

    private void LoadActivityForIntent(Intent intent) {
        WindowInstance = MessageWindow.this;
        mContext = getApplicationContext();
        ChatAttachmentInstance = new ChatAttachment(mContext, WindowInstance);
        LoggedInUser = SessionManager.GetLoggedInUser(WindowInstance);
        if (DefaultUserImage == null) {
            DefaultUserImage = ImageHelper.getDrawableForResource(getResources(), R.drawable.defaultuserprofilepic_sm, getTheme());
        }

        Bundle ExtrasBundle = intent.getExtras();
        ConvWithUserID = ExtrasBundle.getString("ConvWithUserID");
        PubConvWithUserID = ConvWithUserID;
        ConvWithUserName = ExtrasBundle.getString("ConvWithUserName");

        if (intent.hasExtra("ConvWithUser")) {
            ConvWithUser = (Users) intent.getExtras().get("ConvWithUser");
            if (ConvWithUser != null && ConvWithUser.UserID != null && !ConvWithUser.UserID.trim().equals("")) {
                if (ConvWithUser.PicID != null && !ConvWithUser.PicID.trim().equals("")) {
                    ConvWithUserPicID = ConvWithUser.PicID;
                }
            } else {
                //Log.d("GDLog", "Getting User");
                bGetClickedUserDetails = true;
            }
        } else {
            //Log.d("GDLog", "Getting User");
            bGetClickedUserDetails = true;
        }
        if (ConvWithUserPicID == null) {
            ConvWithUserPicID = "";
        }
        if (ConvWithUserPicID.equals("") && intent.hasExtra("ConvWithUserPicID")) {
            ConvWithUserPicID = intent.getExtras().getString("ConvWithUserPicID");
        }

        InitLayout();
        SetEvents();

        //hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        messagesAdapter = new MessagesAdapter(MessageWindow.this, LoggedInUser, ConvWithUserID, ConvWithUserName,
                ConvWithUserPicID, new OnGDMessageClickListener() {
            @Override
            public void onMessageClick(int count, Boolean Selected) {
                if (Selected) {
                    mSelectedMessages = mSelectedMessages + 1;
                } else {
                    mSelectedMessages = mSelectedMessages - 1;
                }
                if (mSelectedMessages <= 0) {
                    ShowActions(false);
                } else {
                    ShowActions(true);
                }
            }

            @Override
            public void onMessageLongClick(int count) {
                mSelectedMessages = 1;
                ShowActions(true);
            }
        }, (text, duration, IsError) -> TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), text, duration, IsError).show());
        messagesList.setAdapter(messagesAdapter);

        messagesList.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (IsSearchTextActive) {
                return;
            }
            if (oldBottom > bottom) {
                messagesList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            } else {
                messagesList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            }
        });

        mGDMessagesDBHelper = new GDMessagesDBHelper(mContext);
        mGDConversationsDBHelper = new GDConversationsDBHelper(mContext);
        mMessageList = new ArrayList<>();

        mMessageList.addAll(mGDMessagesDBHelper.GetMessagesForConversation(LoggedInUser.UserID, ConvWithUserID));
        SortAndUpdateMessages();

        setSupportActionBar(findViewById(R.id.ActivityToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SetOnlineIndicationInTitle();

        SetUserImage(null);
        if (bGetClickedUserDetails) {
            GetClickedUserDetails();
        }
        LoadUserPic();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        PubConvWithUserID = ConvWithUserID;
        NotificationHelper.CancelAllNotifications();

        SetInboundMessagesLocallyRead();
        //Get first inbound unread message and scroll to it.
        SendUpdateForInboundMessages();
        GetMessages();

        mUpdateListTimer = new GDTimer(5000, 3000, new Handler(), () -> {
            if (NewMessagesAvailable) {
                NewMessagesAvailable = false;
                GetMessages();
            }
            if (SentMessagesStatusUpdatesAvailable) {
                SentMessagesStatusUpdatesAvailable = false;
                GetMessageStatusUpdates();
            }
            if (ReceivedMessagesStatusUpdatesAvailable) {
                ReceivedMessagesStatusUpdatesAvailable = false;
                GetUpdatesForInboundMessages();
            }
            if (NewDirectPicAvailable) {
                NewDirectPicAvailable = false;
                LoadAllMessagesDirectPics();
            }
            SendUpdateForInboundMessages();
        });
        mUpdateListTimer.Start();
    }

    @Override
    public void onPause() {
        super.onPause();
        PubConvWithUserID = "";
        if (mUpdateListTimer != null) {
            mUpdateListTimer.Stop();
        }
        if (mMessageList != null && mMessageList.size() > 0) {
            mGDConversationsDBHelper.AddConversationToCache(LoggedInUser.UserID, ConvWithUserID, ConvWithUserName, ConvWithUserPicID, "",
                    mMessageList.get(mMessageList.size() - 1).SentDateTime, "U", "0",
                    mMessageList.get(mMessageList.size() - 1).SentDateTime);
        }
        MarkConversationsNeedLocalRefresh();
    }

    @Override
    public void onDestroy() {
        if (!DirectPhonePhotoPath.equals("")) {
            File file = new File(DirectPhonePhotoPath);
            Boolean deleted = file.delete();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_messages_conversation, menu);
        if (mMenu != null) {
            mMenu.getItem(0).setVisible(false);
            mMenu.getItem(1).setVisible(false);
            mMenu.getItem(2).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }


    private void InitLayout() {
        sendMessageButton = findViewById(R.id.sendMessageButton);
        newMessageText = findViewById(R.id.newMessageText);
        Attach = findViewById(R.id.Attach);
        MessageAttachmentLayout = findViewById(R.id.MessageAttachmentLayout);

        messagesList = findViewById(R.id.Messageslist);
    }

    private void SetEvents() {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage("", "", SendMessageMode.MessageText);
            }
        });

        Attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsAttachmentsShown) {
                    ShowHideAttachmentLayout(false);
                } else {
                    ShowHideAttachmentLayout(true);
                }
            }
        });
        newMessageText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (IsAttachmentsShown) {
                    ShowHideAttachmentLayout(false);
                }
            }
        });
        newMessageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsAttachmentsShown) {
                    ShowHideAttachmentLayout(false);
                }
            }
        });
    }

    enum SendMessageMode {
        MessageText,
        UploadedPhoto,
        DirectPhoto,
        Location
    }

    private void SendMessage(String AttachedPicID, String LocationLatLng, SendMessageMode sendMessageMode) {

        String MessageText = "";
        String sDirectPhonePhoto = "";
        String sDirectPhonePhotoPath = "";
        switch (sendMessageMode.toString()) {
            case "MessageText":
                String text = newMessageText.getText().toString().trim();
                if (text.equals("")) {
                    return;
                }
                MessageText = StringEncoderHelper.encodeURIComponent(text);
                break;
            case "UploadedPhoto":
                if (AttachedPicID.trim().equals("")) {
                    return;
                }
                break;
            case "DirectPhoto":
                if (DirectPhonePhotoPath.equals("")) {
                    return;
                }
                sDirectPhonePhoto = DirectPhonePhoto;
                sDirectPhonePhotoPath = DirectPhonePhotoPath;
                break;
            case "Location":
                if (LocationLatLng.trim().equals("")) {
                    return;
                }
                break;
        }

        if (AreMessageOptionsVisible) {
            messagesAdapter.SetSelectedItemCountZero();
            ShowActions(false);
        }
        String MessageID = GDGenericHelper.GetNewGUID();
        GDMessage listMessage = new GDMessage(MessageID, LoggedInUser.UserID, ConvWithUserID, MessageText, AttachedPicID,
                LocationLatLng, sDirectPhonePhotoPath, sDirectPhonePhoto, "P",
                GDDateTimeHelper.GetCurrentDateTimeAsString(true), null, false);
        GDNewMessage newMessage = new GDNewMessage(MessageID, LoggedInUser.UserID, ConvWithUserID, MessageText, AttachedPicID, LocationLatLng,
                sDirectPhonePhotoPath, sDirectPhonePhoto);

        Boolean hideAttachment = true;
        switch (sendMessageMode.toString()) {
            case "MessageText":
                newMessageText.setText("");
                hideAttachment = false;
                break;
            case "UploadedPhoto":
                break;
            case "DirectPhoto":
                DirectPhonePhoto = "";
                DirectPhonePhotoPath = "";
                break;
            case "Location":
                break;
        }
        if (hideAttachment) {
            ShowHideAttachmentLayout(false);
        }

        listMessage.Direction = 1;
        messagesAdapter.addMessage(listMessage);
        messagesList.setSelection(messagesAdapter.getCount() - 1);

        Boolean addedToDB = mGDMessagesDBHelper.AddMessageToCache(LoggedInUser.UserID, newMessage.MessageID,
                ConvWithUserID, LoggedInUser.UserID, LoggedInUser.UserName, ConvWithUserID, newMessage.MessageText,
                newMessage.AttachedPicIDs, newMessage.Location, newMessage.AttachedFilePath,
                ImageHelper.SuperCompressPicSrc(listMessage.AttachedFilePath), "P",
                listMessage.SentDateTime, "", false);
        if (!addedToDB) {
            return;
        }
        SortAndUpdateMessages();
        APICallInfo.APITimeouts apiTimeout = newMessage.DirectPhonePic.trim().equals("") ? APICallInfo.APITimeouts.MEDIUM : APICallInfo.APITimeouts.SEMILONG;
        APICallInfo apiCallInfo = new APICallInfo("Home", "NewUserMessage", null, "POST", newMessage, MessageID, false, null, apiTimeout);
        GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (result == null || result.equals("") || result.equals("-1")) {
                    return;
                }
                SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                if (successResult != null) {
                    if (successResult.SuccessResult == 1) {
                        UpdateSentMessageStatus(ExtraData.toString(), true, successResult.FailureMessage);
                    } else {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), successResult.FailureMessage, TopSnackBar.LENGTH_LONG, true).show();
                        if (successResult.SuccessResult == -100) {
                            UpdateSentMessageStatus(ExtraData.toString(), false, "");
                        }
                    }
                }
            } catch (Exception e) {
                GDLogHelper.LogException(e);
            }
        }, () -> {
            UpdateSentMessageStatus(newMessage.MessageID, false, "");
            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
        });
    }


    //Message Add, Update - START
    private void UpdateSentMessageStatus(String messageID, Boolean success, String dateTime) {
        int index = mMessageList.indexOf(new GDMessage(messageID));
        if (index == -1) {
            return;
        }
        GDMessage message = mMessageList.get(index);
        if (success) {
            message.MessageStatus = "S";
            message.SentDateTime = dateTime;
            message.dSentDateTime = GDDateTimeHelper.GetDateFromString(dateTime);

            ArrayList<zMessageIDAndDT> messagesIDAndTimeList = new ArrayList<>();
            messagesIDAndTimeList.add(new zMessageIDAndDT(messageID, dateTime));
            mGDMessagesDBHelper.UpdateMessagesListStatusForSent(LoggedInUser.UserID, messagesIDAndTimeList);
        } else {
            message.MessageStatus = "E";
            mGDMessagesDBHelper.UpdateMessageListStatusForError(new ArrayList<>(Arrays.asList(message)));
        }

        SortAndUpdateMessages();
    }

    private void SortAndUpdateMessages() {
        GDMessage.SetDateForMessageList(mMessageList);
        Collections.sort(mMessageList, new MessagesComparator());
        messagesAdapter.SetMessageList(mMessageList);

        LoadAllMessagesAttachedPics();
        LoadAllMessagesDirectPics();
    }

    private void SetInboundMessagesLocallyRead() {
        ArrayList<String> UnSeenMessages = GDMessage.GetUnseenInboundMessageNotSetLocallyIDs(mMessageList);
        if (UnSeenMessages.size() == 0) {
            return;
        }
        ArrayList<zMessageIDAndDT> oMessageIDAndDTList = new ArrayList<>();
        for (int i = 0; i < UnSeenMessages.size(); i++) {
            oMessageIDAndDTList.add(new zMessageIDAndDT(UnSeenMessages.get(i), ""));
        }
        mGDMessagesDBHelper.UpdateInboundMessagesListStatus(LoggedInUser.UserID, oMessageIDAndDTList, "r");
    }

    private void GetMessages() {
        try {
            GDMessage baseMessage = GDMessage.GetLastBaseRefreshMessage(mMessageList);
            ArrayList<String> messageIDs = mGDMessagesDBHelper.GetMessageIDListAfterTimeForConversation(LoggedInUser.UserID, baseMessage.SentDateTime, ConvWithUserID);
            ArrayList<GDMessage> messagesToAdd = mGDMessagesDBHelper.GetMessagesByMessageIDList(LoggedInUser.UserID, messageIDs);

            Boolean messageWasAdded = GDMessage.AddIfNotExists(messagesToAdd, mMessageList);

            SortAndUpdateMessages();

            //Check if message added and window not closed before getting data
            if (messageWasAdded && !StringHelper.IsNullOrEmpty(PubConvWithUserID)) {
                //Play Sound
                try {
                    if (PersistantPreferencesHelper.GetAppSettings().PlayMessageTones.equals("1")) {
                        MediaPlayer mp = MediaPlayer.create(MessageWindow.this, R.raw.message_received);
                        mp.start();
                    }
                    messagesList.setSelection(messagesAdapter.getCount() - 1);
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
                SetInboundMessagesLocallyRead();
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    private void GetMessageStatusUpdates() {
        ArrayList<GDMessage> UnreadSentMessageList = GDMessage.GetUnreadSentMessages(mMessageList);
        if (UnreadSentMessageList.size() == 0) {
            return;
        }
        ArrayList<GDMessage> updatedMessages = mGDMessagesDBHelper.GetMessagesByMessageIDList
                (LoggedInUser.UserID, GDMessage.GetMessageIDList(UnreadSentMessageList));
        if (updatedMessages.size() == 0) {
            return;
        }

        GDMessage.AddIfNotExists(updatedMessages, mMessageList);
        SortAndUpdateMessages();
    }

    private void GetUpdatesForInboundMessages() {
        ArrayList<GDMessage> messages = GDMessage.GetUnreadReceivedMessages(mMessageList);
        if (messages.size() == 0) {
            return;
        }
        ArrayList<GDMessage> updatedMessages = mGDMessagesDBHelper.GetMessagesByMessageIDList
                (LoggedInUser.UserID, GDMessage.GetMessageIDList(messages));
        if (updatedMessages.size() == 0) {
            return;
        }
        GDMessage.AddIfNotExists(updatedMessages, mMessageList);
    }

    private void SendUpdateForInboundMessages() {
        try {
            ArrayList<String> messageIDList = GDMessage.GetUnseenInboundMessageIDs(mMessageList);
            if (messageIDList.size() == 0) {
                return;
            }
            MessageAndNotificationDownloader.SendUpdateForInboundMessages(mContext, LoggedInUser.UserID, mGDMessagesDBHelper, messageIDList, "R");
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    private void MarkConversationsNeedLocalRefresh() {
        if (MessagesPageFragment.MessagesPageInstance != null) {
            MessagesPageFragment.MessagesPageInstance.SetConversationsNeedLocalRefresh(new ArrayList<>(Arrays.asList(ConvWithUserID)));
        }
    }
    //Message Add, Update - END


    protected void ShowActions(Boolean ShowActions) {
        if (ShowActions) {
            getSupportActionBar().setTitle(Integer.toString(mSelectedMessages) + " Selected");
            if (!AreMessageOptionsVisible) {
                getSupportActionBar().setDisplayShowHomeEnabled(false);
                getSupportActionBar().setHomeAsUpIndicator(null);
                if (mMenu != null) {
                    mMenu.getItem(1).setVisible(true);
                }
            }
            if (mMenu != null) {
                if (mSelectedMessages == 1) {
                    mMenu.getItem(0).setVisible(true);
                } else {
                    mMenu.getItem(0).setVisible(false);
                }
            }
            mMenu.getItem(2).setVisible(false);
            AreMessageOptionsVisible = true;
        } else {
            AreMessageOptionsVisible = false;
            if (mMenu != null) {
                mMenu.getItem(0).setVisible(false);
                mMenu.getItem(1).setVisible(false);
                mMenu.getItem(2).setVisible(true);
            }
            messagesAdapter.notifyDataSetChanged();
            //getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.defaultuserprofilepic, getTheme()));
            //getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(ConvWithUserName);
            SetUserImage(null);
        }
    }

    private void ShowHideActionBarForSearch(Boolean Show) {
        ActionBar actionBar = getSupportActionBar();
        if (Show) {
            IsSearchTextActive = true;
            messagesAdapter.SetIsSearchTextActive(true);
            actionBar.setCustomView(R.layout.messages_search);
            final EditText search = actionBar.getCustomView().findViewById(R.id.txt_SearchMessages);
            final TextView MatchedText = actionBar.getCustomView().findViewById(R.id.MatchedText);
            ImageView CloseMessageSearch = actionBar.getCustomView().findViewById(R.id.CloseMessageSearch);
            final ImageView SearchMessagePrev = actionBar.getCustomView().findViewById(R.id.SearchMessagePrev);
            ImageView SearchMessageNext = actionBar.getCustomView().findViewById(R.id.SearchMessageNext);
//            search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//                @Override
//                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                    return false;
//                }
//            });
            search.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    //show keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
                }
            });
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (SearchMessagesTimer != null) {
                        SearchMessagesTimer.cancel();
                    }
                    SearchMessagesTimer = new CountDownTimer(800, 400) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            messagesAdapter.SetSearchText(search.getText().toString());
                            if (search.getText().toString().equals("")) {
                                MatchedText.setText("");
                                return;
                            }
                            ShowPositionForSearchTextResult(MatchedText);
                        }
                    }.start();
                }
            });
            CloseMessageSearch.setOnClickListener(v -> {
                messagesAdapter.SetSearchText("");
                //hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                ShowHideActionBarForSearch(false);
                messagesAdapter.notifyDataSetChanged();
            });
            SearchMessagePrev.setOnClickListener(v -> {
                //int position = messagesAdapter.GetPrevSearchPosition(messagesList.getFirstVisiblePosition());
                messagesAdapter.SetPrevSearchPosition();
                ShowPositionForSearchTextResult(MatchedText);
            });
            SearchMessageNext.setOnClickListener(v -> {
                //int position = messagesAdapter.GetNextSearchPosition(messagesList.getFirstVisiblePosition());
                messagesAdapter.SetNextSearchPosition();
                ShowPositionForSearchTextResult(MatchedText);
            });
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setHomeAsUpIndicator(null);
            mMenu.getItem(2).setVisible(false);
            actionBar.setDisplayShowCustomEnabled(true);
            search.requestFocus();
        } else {
            IsSearchTextActive = false;
            messagesAdapter.SetIsSearchTextActive(false);
            if (mMenu != null) {
                mMenu.getItem(0).setVisible(false);
                mMenu.getItem(1).setVisible(false);
                mMenu.getItem(2).setVisible(true);
            }
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);

            if (ConvWithUser != null && ConvWithUser.OnlineStatus) {
                SetOnlineIndicationInTitle();
            } else {
                actionBar.setTitle(ConvWithUserName);
            }

            SetUserBitmapImage(null);
            messagesAdapter.notifyDataSetChanged();
        }
    }

    private void ShowPositionForSearchTextResult(final TextView MatchedText) {
        messagesList.post(() -> {
            int SearchCurrentItemPosition = messagesAdapter.GetSearchCurrentItemPosition();
            if (SearchCurrentItemPosition == -1) {
                messagesAdapter.SetLastSearchPosition();
            }
            SearchCurrentItemPosition = messagesAdapter.GetSearchCurrentItemPosition();
            if (SearchCurrentItemPosition == -1) {
                MatchedText.setText("");
            } else {
                messagesList.setSelection(SearchCurrentItemPosition);
                ArrayList<Integer> SearchMatchPositions = messagesAdapter.GetSearchMatchPositions();
                int position = -1;
                for (int i = 0; i < SearchMatchPositions.size(); i++) {
                    if (SearchCurrentItemPosition == SearchMatchPositions.get(i)) {
                        position = i;
                        break;
                    }
                }
                MatchedText.setText(Integer.toString(position + 1) + "/" + Integer.toString(SearchMatchPositions.size()));
            }
        });
    }

    private void GetClickedUserDetails() {
        ConvWithUser = UserObjectsCacheHelper.GetUserFromCache(ConvWithUserID);
        if (ConvWithUser != null) {
            if (ConvWithUserPicID == null || ConvWithUserPicID.equals("")) {
                ConvWithUserPicID = ConvWithUser.PicID;
            }
            SetOnlineIndicationInTitle();
            return;
        }
        new HomeAPICalls(mContext).GetMiniProfilesForUserIDList(StringHelper.ToArrayList(ConvWithUserID),
                new APICallerResultCallback() {
                    @Override
                    public void OnComplete(Object result, Object extraData) {
                        ArrayList<Users> users = (ArrayList<Users>) result;
                        ConvWithUser = users.get(0);
                        SetOnlineIndicationInTitle();
                        if (StringHelper.IsNullOrEmpty(ConvWithUserPicID) || !ConvWithUser.PicID.equalsIgnoreCase(ConvWithUserPicID)) {
                            ConvWithUserPicID = ConvWithUser.PicID;
                            LoadUserPic();
                        }
                    }

                    @Override
                    public void OnError(String result, Object extraData) {
                    }

                    @Override
                    public void OnNoNetwork(Object extraData) {
                    }
                });
    }

    private void DeleteMessagesFromServer(ArrayList<GDMessage> MessagesToDeleteList) {
        if (MessagesToDeleteList.size() == 0) {
            return;
        }
        ArrayList<String> MessagesIDList = new ArrayList<>();
        for (int i = 0; i < MessagesToDeleteList.size(); i++) {
            MessagesIDList.add(MessagesToDeleteList.get(i).MessageID);
        }

        new MessageAPICalls(mContext).DeleteUserMessageList(MessagesIDList);
    }

    private void SetOnlineIndicationInTitle() {
        ActionBar actionBar = getSupportActionBar();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (ConvWithUser != null && ConvWithUser.OnlineStatus) {
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append(" ", new ImageSpan(mContext, R.drawable.online_messagewindow), 0).append(ConvWithUserName);
                    actionBar.setTitle(builder);
                } else {
                    actionBar.setTitle(ConvWithUserName);
                }
            } else {
                actionBar.setTitle(ConvWithUserName);
            }
        } catch (Exception ex) {
            try {
                actionBar.setTitle(ConvWithUserName);
            } catch (Exception exx) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
            }
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private void StartSendLocationFlow() {
        if (IsAttachmentsShown) {
            ShowHideAttachmentLayout(false);
        }
        Intent intent = new Intent(mContext, GDMapActivity.class);
        intent.putExtra("Activity_Mode", GDMapActivity.SELECT_LOCATION);
        intent.putExtra("TooltipMessage", "Send");
        startActivityForResult(intent, 1);
        DirectPhonePhoto = "";
        DirectPhonePhotoPath = "";
    }

    private void LoadUserPic() {
        if (StringHelper.IsNullOrEmpty(ConvWithUserPicID)) {
            return;
        }
        ImageAPIHelper.GetPicsForPicIDList(mContext, StringHelper.ToArrayList(ConvWithUserPicID),
                false, pics -> {
                    if (StringHelper.IsNullOrEmpty(ConvWithUserPicID)) {
                        return;
                    }
                    if (pics.size() > 0 && pics.get(0).PicID.equalsIgnoreCase(ConvWithUserPicID)) {
                        SetUserBitmapImage(pics.get(0).image);
                    }
                });
    }

    private void LoadAllMessagesAttachedPics() {
        ArrayList<String> attachedPicIDs = GDMessage.GetAttachedPicIDListForNullImages(mMessageList);
        ImageAPIHelper.GetPicsForPicIDList(mContext, attachedPicIDs, false, pics -> {
            GDMessage.SetAttachedPicsToMessages(pics, mMessageList);
            messagesAdapter.notifyDataSetChanged();
        });
        ImageAPIHelper.GetFullPicListFromLocalOnly(mContext, attachedPicIDs, true, pics -> {
            GDMessage.SetAttachedPicsToMessages(pics, mMessageList);
            messagesAdapter.notifyDataSetChanged();
        });
    }

    private void LoadAllMessagesDirectPics() {
        ArrayList<String> messageIDs = GDMessage.GetMessageIDListForDirectPicNullImages(mMessageList);
        Collections.reverse(messageIDs);
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            for (String messageID : messageIDs) {
                int index = mMessageList.indexOf(new GDMessage(messageID));
                if (index != -1) {
                    GDMessage message = mMessageList.get(index);
                    Bitmap image = ImageHelper.GetBitmapFromPath(message.AttachedFilePath);
                    message.image = ImageHelper.getResizedBitmap(ImageHelper.scaleDownTo2048(image), 160, 160);
                    gdBackgroundTaskFinished.OnBackgroundTaskFinished(null);
                }
            }
        }, data -> {
            messagesAdapter.notifyDataSetChanged();
        });
    }



    //Attachments
    private void ShowHideAttachmentLayout(Boolean Show) {
        if (Show) {
            //hide keyboard
            GDGenericHelper.HideKeyboard(MessageWindow.this);
            MessageAttachmentLayout.addView(ChatAttachmentInstance.GetView());
            MessageAttachmentLayout.setVisibility(View.VISIBLE);
            IsAttachmentsShown = true;
        } else {
            IsAttachmentsShown = false;
            MessageAttachmentLayout.setVisibility(View.GONE);
            ChatAttachmentInstance.Reset();
            if (!DirectPhonePhoto.equals("")) {
                DirectPhonePhoto = "";
                if (!DirectPhonePhotoPath.equals("")) {
                    ImageHelper.DeleteImageFile(DirectPhonePhotoPath);
                }
                DirectPhonePhotoPath = "";
            }
        }
    }


    //Camera and storage permissions
    private void StartGetCameraPictureFlow() {
        if (!IsCameraPermissionGranted()) {
            mGetCameraPermission = () -> StartGetCameraPictureFlow();
            CheckAndRequestCameraPermission(mGetCameraPermission);
            return;
        }

        CurrentGetFileWritePermission = () -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            CameraPhotoPath = "CameraPhotoPath" + "_" + GDGenericHelper.GetNewGUID() + ".jpg";
            File file = new File(ImageHelper.CreateDirectoryForImage(ImageHelper.UPLOAD_IMAGE),
                    CameraPhotoPath);
            Uri fileUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName()
                    + ".gdudesapp.provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, 3);
            DirectPhonePhoto = "";
            DirectPhonePhotoPath = "";
            PersistantPreferencesHelper.SetFileWritePermission("1");
        };
        CheckAndRequestStoragePermission(CurrentGetFileWritePermission);
    }

    private Boolean IsCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private void CheckAndRequestCameraPermission(GetCameraPermission getCameraPermission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (getCameraPermission != null) {
                    getCameraPermission.OnPermissionGranted();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);
            }
        } else {
            if (getCameraPermission != null) {
                getCameraPermission.OnPermissionGranted();
            }
        }
    }

    public void CheckAndRequestStoragePermission(GetFileWritePermission getFileWritePermission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                if (getFileWritePermission != null) {
                    getFileWritePermission.OnPermissionGranted();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            if (getFileWritePermission != null) {
                getFileWritePermission.OnPermissionGranted();
            }
        }
    }


    //Events
    @Override
    public void onBackPressed() {
        Boolean Handled = false;
        if (AreMessageOptionsVisible) {
            Handled = true;
            messagesAdapter.SetSelectedItemCountZero();
            ShowActions(false);
        } else if (IsSearchTextActive) {
            Handled = true;
            ShowHideActionBarForSearch(false);
        }
        if (IsAttachmentsShown) {
            Handled = true;
            ShowHideAttachmentLayout(false);
        }
        if (!Handled) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (AreMessageOptionsVisible) {
                    messagesAdapter.SetSelectedItemCountZero();
                    ShowActions(false);
                } else if (IsSearchTextActive) {
                    ShowHideActionBarForSearch(false);
                } else {
                    if (ConvWithUser != null || (ConvWithUserID != null && !ConvWithUserID.trim().equals(""))) {
                        Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                        if (ConvWithUser != null) {
                            intent.putExtra("ClickedUserID", ConvWithUser.UserID);
                        } else {
                            intent.putExtra("ClickedUserID", ConvWithUserID);
                        }
                        if (ConvWithUser != null && ConvWithUser.PicID != null && !ConvWithUser.PicID.trim().equals("")) {
                            intent.putExtra("ProfilePicID", ConvWithUser.PicID);
                        }
                        startActivity(intent);
                    }
                }
                return true;
            case R.id.action_Copy:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Messages from GDudes", messagesAdapter.GetFirstSelectedItemText());
                clipboard.setPrimaryClip(clip);

                messagesAdapter.SetSelectedItemCountZero();
                ShowActions(false);
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Message Copied", TopSnackBar.LENGTH_SHORT, false).show();
                return true;
            case R.id.action_Delete:
                final ArrayList<GDMessage> MessagesToDeleteList = new ArrayList<>();
                for (int i = 0; i < mMessageList.size(); i++) {
                    if (mMessageList.get(i).IsSelected) {
                        MessagesToDeleteList.add(mMessageList.get(i));
                    }
                }
                if (MessagesToDeleteList.size() == 0) {
                    return true;
                }
                GDDialogHelper.ShowYesNoTypeDialog(WindowInstance, "Delete messages ?",
                        "Are you sure you want to delete " + Integer.toString(mSelectedMessages) + " message(s)?",
                        GDDialogHelper.BUTTON_TEXT_DELETE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT, new OnDialogButtonClick() {
                            @Override
                            public void dialogButtonClicked() {
                                if (mGDMessagesDBHelper.UpdateMessagesListStatusForDeleted(MessagesToDeleteList)) {
                                    //GDToastHelper.ShowToast(mContext, Integer.toString(mSelectedMessages) + "Message(s) deleted", GDToastHelper.INFO, GDToastHelper.SHORT);
                                    mMessageList.removeAll(MessagesToDeleteList);
                                    messagesAdapter.SetSelectedItemCountZero();
                                    messagesAdapter.SetMessageList(mMessageList);
                                    ShowActions(false);
                                    DeleteMessagesFromServer(MessagesToDeleteList);
                                } else {
                                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Error while deleting. Please try again.", TopSnackBar.LENGTH_SHORT, true).show();
                                }
                            }
                        }, null);
                return true;
            case R.id.action_Search:
                IsSearchTextActive = true;
                ShowHideActionBarForSearch(true);
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    //Delegates
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 1) {
                if (CurrentGetFileWritePermission != null) {
                    CurrentGetFileWritePermission.OnPermissionGranted();
                }
            } else if (requestCode == 2) {
                if (mGetCameraPermission != null) {
                    mGetCameraPermission.OnPermissionGranted();
                }
            }
        }
    }

    public static void NewMessagesAvailable(ArrayList<String> convWithUserIDList) {
        if (StringHelper.ArrayContains(convWithUserIDList, PubConvWithUserID)) {
            NewMessagesAvailable = true;
        }
    }

    public static void SentMessagesStatusUpdated(ArrayList<String> convWithUserIDList) {
        if (StringHelper.ArrayContains(convWithUserIDList, PubConvWithUserID)) {
            SentMessagesStatusUpdatesAvailable = true;
        }
    }

    public static void ReceivedMessagesStatusUpdated() {
        ReceivedMessagesStatusUpdatesAvailable = true;
    }

    public static void DirectPicDownloaded() {
        NewDirectPicAvailable = true;
    }


    //Direct Photo - START
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 0:
                    //Unknown
                    break;
                case 1:
                    //Location Received to send
                    UserLocation SelectedLocation = data.getExtras().getParcelable("UserLocation");
                    SendMessage("", SelectedLocation.LocationLatLng, SendMessageMode.Location);
                    break;
                case 2:
                    //Attached from gallery. Now Crop it.
                    try {
                        CropImage.activity(data.getData())
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setInitialCropWindowPaddingRatio(0)
                                .start(this);
                    } catch (Exception ex) {
                        GDLogHelper.LogException(ex);
                        ShowCropImageError();
                    }
                    break;
                case 3:
                    //Captured image from Camera. Now Crop it.
                    try {
                        File imageFile = GetCameraCapturedImageFile(true);
                        if (imageFile == null) {
                            return;
                        }
                        CropImage.activity(Uri.fromFile(new File(imageFile.getAbsolutePath())))
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setOutputCompressQuality(100)
                                .setInitialCropWindowPaddingRatio(0)
                                .start(this);
                    } catch (Exception ex) {
                        GDLogHelper.LogException(ex);
                        ShowCropImageError();
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri selectedImage = result.getUri();
                    ProcessCroppedImage(selectedImage);
                    break;
            }
        }
        if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            GDLogHelper.LogException(result.getError());
            ShowCropImageError();
        }
    }

    private void ProcessCroppedImage(Uri selectedImage) {
        String CroppedPicturePath = selectedImage.getPath();
        //Copy Cropped Image to Sent Folder in GDudes Images
        DirectPhonePhotoPath = ImageHelper.GetNewFilePath(ImageHelper.SEND_IMAGE);
        Boolean copied = ImageHelper.CopyImage(CroppedPicturePath, DirectPhonePhotoPath);
        if (!copied) {
            GDLogHelper.Log(mLogClass, "ProcessCroppedImage", "Could not copy image.", GDLogHelper.LogLevel.CRITICAL);
            ShowCropImageError();
            return;
        }

        try {
            //Delete the Cropped picture from cache
            File file = new File(CroppedPicturePath);
            Boolean deleted = file.delete();
            if (!deleted) {
                String errorMessage = "Could not delete temp file " + CroppedPicturePath;
                GDLogHelper.Log(mLogClass, "onActivityResult", errorMessage, GDLogHelper.LogLevel.ERROR);
            }

            //Delete camera captured picture
            if (CameraPhotoPath != null && !CameraPhotoPath.trim().equals("")) {
                File imageFile = GetCameraCapturedImageFile(false);
                file = new File(imageFile.getAbsolutePath());
                file.delete();
                CameraPhotoPath = "";
            }
        } catch (Exception e) {
            GDLogHelper.LogException(e);
        }

        String imageStr = ImageHelper.CompressImageFileGetString(DirectPhonePhotoPath, true, true);
        DirectPhonePhoto = StringEncoderHelper.encodeURIComponent(imageStr);
        ChatAttachmentInstance.ShowDirectPicImage(ImageHelper.GetBitmapFromString(imageStr));
    }

    private File GetCameraCapturedImageFile(Boolean showError) {
        File imagePath = new File(ImageHelper.CreateDirectoryForImage(ImageHelper.UPLOAD_IMAGE));
        File imageFile = null;
        for (File temp : imagePath.listFiles()) {
            if (temp.getName().equals(CameraPhotoPath)) {
                imageFile = temp;
                break;
            }
        }
        if (imageFile == null) {
            GDLogHelper.Log(mLogClass, "onActivityResult", "Could not get image captured from camera.",
                    GDLogHelper.LogLevel.CRITICAL);
            if (showError) {
                ShowCropImageError();
            }
        }
        return imageFile;
    }

    private void ShowCropImageError() {
        GDToastHelper.ShowToast(MessageWindow.this, "Could not read image.",
                GDToastHelper.ERROR, GDToastHelper.SHORT);
    }
    //Direct Photo - END

    @Override
    public void OnSubGenericAction(String identifier, Object data) {
        switch (identifier) {
            case "Camera":
                StartGetCameraPictureFlow();
                break;
            case "PhotoLibrary":
                CurrentGetFileWritePermission = () -> {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                    DirectPhonePhoto = "";
                    DirectPhonePhotoPath = "";
                    PersistantPreferencesHelper.SetFileWritePermission("1");
                };
                CheckAndRequestStoragePermission(CurrentGetFileWritePermission);
                break;
            case "SendDirectPic":
                SendMessage("", "", SendMessageMode.DirectPhoto);
                break;
            case "SendUploadedPic":
                SendMessage((String) data, "", SendMessageMode.UploadedPhoto);
                break;
            case "ShowNewPhotoOptions":
                ShowNewPhotoOptions();
                break;
            case "SendLocation":
                StartSendLocationFlow();
                break;
            case "CancelSelectedPhoto":
                ShowHideAttachmentLayout(false);
                ShowHideAttachmentLayout(true);
                break;
        }
    }

    private void ShowNewPhotoOptions() {
        final CharSequence[] options = {"Camera", "Photo Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(WindowInstance);
        builder.setTitle("New photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    StartGetCameraPictureFlow();
                } else if (item == 1) {
                    CurrentGetFileWritePermission = new GetFileWritePermission() {
                        @Override
                        public void OnPermissionGranted() {
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, 2);
                            DirectPhonePhoto = "";
                            DirectPhonePhotoPath = "";
                            PersistantPreferencesHelper.SetFileWritePermission("1");
                        }
                    };
                    CheckAndRequestStoragePermission(CurrentGetFileWritePermission);
                }
                dialog.dismiss();
            }
        });
        builder.show();
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (!isFinishing()) {
//                }
//            }
//        });
    }

    private void SetUserBitmapImage(Bitmap image) {
        if (image == null) {
            SetUserImage(null);
            return;
        }
        SetUserImage(new BitmapDrawable(getResources(), ImageHelper.getResizedBitmap(image, 80,80)));
    }

    private void SetUserImage(Drawable image) {
        if (image != null) {
            getSupportActionBar().setHomeAsUpIndicator(image);
        } else {
            getSupportActionBar().setHomeAsUpIndicator(DefaultUserImage);
        }
    }

    interface GetCameraPermission {
        void OnPermissionGranted();
    }
}
