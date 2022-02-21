package com.gdudes.app.gdudesapp.GDTypes;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;

import com.gdudes.app.gdudesapp.Comparators.MessagesComparator;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class GDMessage implements Parcelable {
    public String MessageID;
    public String SenderID;
    public String SenderName;
    public String RecieverID;
    public String MessageText;
    public Spannable DecodedMessageText;
    public String AttachedPicIDs;
    public String Location;
    public String AttachedFilePath;
    public String DirectPhonePic;
    public String MessageStatus;
    public String SentDateTime;
    public String ReadDateTime;
    public Date dSentDateTime;
    public Date dReadDateTime;
    public Boolean IsSentTimeUpdatedFromServer;
    public String UserName;
    public String PicID;
    public Boolean IsSelected;
    public Boolean ContainsSearchedText;
    public String SenderType;
    public Bitmap image;
    public int Direction = -1;

    public GDMessage() {
        MessageID = "";
        SenderID = "";
        SenderName = "";
        RecieverID = "";
        MessageText = "";
        AttachedPicIDs = "";
        Location = "";
        MessageStatus = "";
        SentDateTime = "";
        ReadDateTime = "";
        IsSentTimeUpdatedFromServer = false;
        UserName = "";
        PicID = "";
        IsSelected = false;
        ContainsSearchedText = false;
    }

    public GDMessage(String messageID) {
        MessageID = messageID;
        SenderID = "";
        SenderName = "";
        RecieverID = "";
        MessageText = "";
        AttachedPicIDs = "";
        Location = "";
        MessageStatus = "";
        SentDateTime = "";
        ReadDateTime = "";
        IsSentTimeUpdatedFromServer = false;
        UserName = "";
        PicID = "";
        IsSelected = false;
        ContainsSearchedText = false;
    }

    public GDMessage(String vMessageID, String vSenderID, String vRecieverID, String vMessageText, String vAttachedPicIDs,
                     String vLocation, String vAttachedFilePath, String vAttachedFileSrc, String vMessageStatus,
                     String vSentDateTime, String vReadDateTime, Boolean vIsSentTimeUpdatedFromServer) {
        MessageID = vMessageID;
        SenderID = vSenderID;
        SenderName = "";
        RecieverID = vRecieverID;
        MessageText = vMessageText;
        AttachedPicIDs = vAttachedPicIDs;
        Location = vLocation;
        AttachedFilePath = vAttachedFilePath;
        DirectPhonePic = vAttachedFileSrc;
        MessageStatus = vMessageStatus;
        SentDateTime = vSentDateTime;
        ReadDateTime = vReadDateTime;
        IsSentTimeUpdatedFromServer = vIsSentTimeUpdatedFromServer;
        UserName = "";
        PicID = "";
        IsSelected = false;
        ContainsSearchedText = false;
    }

    public GDMessage(String vMessageID, String vSenderID, String vSenderName, String vRecieverID, String vMessageText, String vAttachedPicIDs,
                     String vLocation, String vAttachedFilePath, String vAttachedFileSrc, String vMessageStatus, String vSentDateTime, String vReadDateTime,
                     Boolean vIsSentTimeUpdatedFromServer) {
        MessageID = vMessageID;
        SenderID = vSenderID;
        SenderName = vSenderName;
        RecieverID = vRecieverID;
        MessageText = vMessageText;
        AttachedPicIDs = vAttachedPicIDs;
        Location = vLocation;
        AttachedFilePath = vAttachedFilePath;
        DirectPhonePic = vAttachedFileSrc;
        MessageStatus = vMessageStatus;
        SentDateTime = vSentDateTime;
        ReadDateTime = vReadDateTime;
        IsSentTimeUpdatedFromServer = vIsSentTimeUpdatedFromServer;
        UserName = "";
        PicID = "";
        IsSelected = false;
        ContainsSearchedText = false;
    }

    @Override
    public boolean equals(Object second) {
        if (this.MessageID.equalsIgnoreCase(((GDMessage) second).MessageID)) {
            return true;
        }
        return false;
    }

    public Boolean HasAttachedPic() {
        return !StringHelper.IsNullOrEmpty(AttachedPicIDs);
    }

    public Boolean HasDirectPic() {
        return !StringHelper.IsNullOrEmpty(AttachedFilePath);
    }

    public Boolean MessageContainsPhoto() {
        return HasAttachedPic() || HasDirectPic();
    }

    public Boolean HasLocation() {
        return !StringHelper.IsNullOrEmpty(Location);
    }


    public String GetAttachedPicID() {
        if (HasAttachedPic()) {
            return StringHelper.ToArrayList(AttachedPicIDs.split("\\s*,\\s*")).get(0);
        }
        return "";
    }

    public static void SetDateForMessageList(ArrayList<GDMessage> MessagesList) {
        try {
            for (int i = 0; i < MessagesList.size(); i++) {
                if (MessagesList.get(i).SentDateTime != null && !MessagesList.get(i).SentDateTime.equals("")) {
                    MessagesList.get(i).dSentDateTime = GDDateTimeHelper.GetDateFromString(MessagesList.get(i).SentDateTime);
                }
                if (MessagesList.get(i).ReadDateTime != null && !MessagesList.get(i).ReadDateTime.equals("")) {
                    MessagesList.get(i).dReadDateTime = GDDateTimeHelper.GetDateFromString(MessagesList.get(i).ReadDateTime);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static Boolean AddIfNotExists(ArrayList<GDMessage> FromList, ArrayList<GDMessage> ToList) {
        Boolean MessageAdded = false;
        Boolean MessageExists = false;
        Integer position = -1;
        try {
            for (int i = 0; i < FromList.size(); i++) {
                MessageExists = false;
                position = -1;
                for (int j = 0; j < ToList.size(); j++) {
                    if (FromList.get(i).MessageID.equalsIgnoreCase(ToList.get(j).MessageID)) {
                        MessageExists = true;
                        position = j;
                        break;
                    }
                }
                if (!MessageExists) {
                    MessageAdded = true;
                    ToList.add(FromList.get(i));
                } else {
                    if ((ToList.get(position).Direction == 0 || ToList.get(position).Direction == 1)
                            && !ToList.get(position).MessageStatus.equals("R")) {
                        if (!ToList.get(position).IsSentTimeUpdatedFromServer) {
                            ToList.get(position).SentDateTime = FromList.get(i).SentDateTime;
                            ToList.get(position).IsSentTimeUpdatedFromServer = true;
                        }
                        ToList.get(position).MessageStatus = FromList.get(i).MessageStatus;
                        if (FromList.get(i).ReadDateTime != null && !FromList.get(i).ReadDateTime.equals("")) {
                            ToList.get(position).ReadDateTime = FromList.get(i).ReadDateTime;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            MessageAdded = false;
        }
        return MessageAdded;
    }

    public static GDMessage GetLastBaseRefreshMessage(ArrayList<GDMessage> MessagesList) {
        GDMessage gdMessage = null;
        try {
            GDMessage.SetDateForMessageList(MessagesList);
            Collections.sort(MessagesList, new MessagesComparator());
            for (int i = MessagesList.size() - 1; i >= 0; i--) {
                if (MessagesList.get(i).Direction == 0 || MessagesList.get(i).IsSentTimeUpdatedFromServer ||
                        MessagesList.get(i).MessageStatus.equals("D") || MessagesList.get(i).MessageStatus.equals("R")) {
                    gdMessage = MessagesList.get(i);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return gdMessage;
    }

    public static ArrayList<GDMessage> GetUnreadSentMessages(ArrayList<GDMessage> MessagesList) {
        return GetUnreadMessages(MessagesList, 1);
    }

    public static ArrayList<GDMessage> GetUnreadReceivedMessages(ArrayList<GDMessage> MessagesList) {
        return GetUnreadMessages(MessagesList, 0);
    }

    private static ArrayList<GDMessage> GetUnreadMessages(ArrayList<GDMessage> MessagesList, int direction) {
        Date TimeNow = GDDateTimeHelper.GetDateFromString(GDDateTimeHelper.GetCurrentDateTimeAsString(true));
        long hours;
        ArrayList<GDMessage> UnReadSentMessages = new ArrayList<>();
        try {
            for (int i = 0; i < MessagesList.size(); i++) {
                if (MessagesList.get(i).Direction == direction && !MessagesList.get(i).MessageStatus.equals("R")) {
                    hours = (TimeNow.getTime() - GDDateTimeHelper.GetDateFromString(MessagesList.get(i).SentDateTime).getTime()) / 1000 / 60 / 60;
                    if (hours < GDMessagesDBHelper.MessageServerCommThresholdHours) {
                        UnReadSentMessages.add(MessagesList.get(i));
                    }
                }
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return UnReadSentMessages;
    }

    public static ArrayList<String> GetUnseenInboundMessageIDs(ArrayList<GDMessage> MessagesList) {
        //Messages not set as Read on server
        ArrayList<String> UnSeenMessages = new ArrayList<>();
        try {
            for (int i = 0; i < MessagesList.size(); i++) {
                if (MessagesList.get(i).Direction == 0 && !MessagesList.get(i).MessageStatus.equals("R")) {
                    UnSeenMessages.add(MessagesList.get(i).MessageID);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return UnSeenMessages;
    }

    public static ArrayList<String> GetUnseenInboundMessageNotSetLocallyIDs(ArrayList<GDMessage> MessagesList) {
        //Messages not set as Read on local and server
        ArrayList<String> UnSeenMessages = new ArrayList<>();
        try {
            for (int i = 0; i < MessagesList.size(); i++) {
                if (MessagesList.get(i).Direction == 0 && !MessagesList.get(i).MessageStatus.equalsIgnoreCase("R")) {
                    UnSeenMessages.add(MessagesList.get(i).MessageID);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return UnSeenMessages;
    }

    public static void AddDateDisplayForMessages(ArrayList<GDMessage> MessagesList) {
        ArrayList<String> DatesList = new ArrayList<>();
        ArrayList<String> FirstMessageTimeDatesList = new ArrayList<>();
        String TempDate;
        try {
            //Remove Dates if any
            ArrayList<GDMessage> RemoveDatesList = new ArrayList<>();
            for (int i = 0; i < MessagesList.size(); i++) {
                if (MessagesList.get(i).Direction == 2) {
                    RemoveDatesList.add(MessagesList.get(i));
                }
            }
            MessagesList.removeAll(RemoveDatesList);

            for (int i = 0; i < MessagesList.size(); i++) {
                TempDate = GDDateTimeHelper.GetDateStringWithoutTime(MessagesList.get(i).SentDateTime, true);
                if (!DatesList.contains(TempDate)) {
                    DatesList.add(TempDate);
                    FirstMessageTimeDatesList.add(MessagesList.get(i).SentDateTime);
                }
            }
            GDMessage gdMessage;
            for (int i = 0; i < DatesList.size(); i++) {
                gdMessage = new GDMessage(GDGenericHelper.GetNewGUID(), GDGenericHelper.GetNewGUID(), GDGenericHelper.GetNewGUID(), "",
                        "", "", "", "", "Z",
                        FirstMessageTimeDatesList.get(i), DatesList.get(i), false);
                gdMessage.dSentDateTime = GDDateTimeHelper.GetDateFromString(gdMessage.SentDateTime);
                gdMessage.dReadDateTime = GDDateTimeHelper.GetDateFromString(gdMessage.ReadDateTime);
                gdMessage.Direction = 2;
                MessagesList.add(gdMessage);
            }
            Collections.sort(MessagesList, new MessagesComparator());
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    //Called from Service
    public Boolean ContainsDirectPicToDownload() {
        return (!StringHelper.IsNullOrEmpty(DirectPhonePic) && DirectPhonePic.trim().equals("Y")) ||
                !StringHelper.IsNullOrEmpty(AttachedFilePath);
    }

    public static Boolean SetImagePath(GDMessage message) {
        try {
            if (message.ContainsDirectPicToDownload() && StringHelper.IsNullOrEmpty(message.AttachedFilePath)) {
                message.AttachedFilePath = ImageHelper.GetFileNameDirectoryForImage(ImageHelper.RECEIVED_IMAGE, message.MessageID);
            }
            return true;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return false;
        }
    }

    public static ArrayList<GDMessage> SetBigImageForPendingForDirectPhotos(ArrayList<GDMessage> MessagesList, Context context) {
        ArrayList<GDMessage> UnSuccessfulMessagesList = new ArrayList<>();
        File oFile;
        try {
            for (int i = 0; i < MessagesList.size(); i++) {
                if (MessagesList.get(i).DirectPhonePic != null && !MessagesList.get(i).DirectPhonePic.equals("")
                        && !MessagesList.get(i).DirectPhonePic.equals("N")) {
                    if ((MessagesList.get(i).AttachedFilePath != null && !MessagesList.get(i).AttachedFilePath.trim().equals(""))) {
                        oFile = new File(MessagesList.get(i).AttachedFilePath);
                        if (oFile.exists()) {
                            MessagesList.get(i).DirectPhonePic = StringEncoderHelper.encodeURIComponent(
                                    ImageHelper.CompressImageFileGetString(MessagesList.get(i).AttachedFilePath, true, true));
                        } else {
                            UnSuccessfulMessagesList.add(MessagesList.get(i));
                        }
                    } else {
                        UnSuccessfulMessagesList.add(MessagesList.get(i));
                    }
                }
            }
            MessagesList.removeAll(UnSuccessfulMessagesList);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return UnSuccessfulMessagesList;
    }
    //Functions used for direct phone image message - END

    public static ArrayList<GDMessage> GetMessagesUnDeliveredForMoreThan6Hours(ArrayList<GDMessage> MessagesList) {
        ArrayList<GDMessage> ToMarkErrorMessagesList = new ArrayList<>();
        Date CurrentDate;
        try {
            CurrentDate = GDDateTimeHelper.GetDateFromString(GDDateTimeHelper.GetCurrentDateTimeAsString(true));
            for (int i = 0; i < MessagesList.size(); i++) {
                MessagesList.get(i).dSentDateTime = GDDateTimeHelper.GetDateFromString(MessagesList.get(i).SentDateTime);
                long diff = CurrentDate.getTime() - MessagesList.get(i).dSentDateTime.getTime();
                long hours = diff / 1000 / 60 / 60;
                if (((int) hours) >= 6) {
                    ToMarkErrorMessagesList.add(MessagesList.get(i));
                }
            }
            MessagesList.removeAll(ToMarkErrorMessagesList);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return ToMarkErrorMessagesList;
    }


    public static ArrayList<String> GetMessageIDList(ArrayList<GDMessage> messages) {
        ArrayList<String> messageIDs = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            messageIDs.add(messages.get(i).MessageID);
        }
        return StringHelper.RemoveDuplicateEntries(messageIDs);
    }

    public static ArrayList<String> GetAttachedPicIDList(ArrayList<GDMessage> messages) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).HasAttachedPic()) {
                picIDs.add(messages.get(i).GetAttachedPicID());
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static ArrayList<String> GetAttachedPicIDListForNullImages(ArrayList<GDMessage> messages) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).image == null && messages.get(i).HasAttachedPic()) {
                picIDs.add(messages.get(i).GetAttachedPicID());
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static ArrayList<String> GetMessageIDListForDirectPicNullImages(ArrayList<GDMessage> messages) {
        ArrayList<String> messageIDs = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).image == null && !StringHelper.IsNullOrEmpty(messages.get(i).AttachedFilePath)) {
                messageIDs.add(messages.get(i).MessageID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(messageIDs);
    }

    public static ArrayList<String> GetSenderIDList(ArrayList<GDMessage> messages) {
        ArrayList<String> userIDs = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            userIDs.add(messages.get(i).SenderID);
        }
        return StringHelper.RemoveDuplicateEntries(userIDs);
    }

    public static void SetAttachedPicsToMessages(ArrayList<GDPic> pics, ArrayList<GDMessage> messages) {
        for (int i = 0; i < pics.size(); i++) {
            GDPic pic = pics.get(i);
            for (int j = 0; j < messages.size(); j++) {
                GDMessage message = messages.get(j);
                String attachedPicID = message.HasAttachedPic() ? message.GetAttachedPicID() : "";
                if (!attachedPicID.equals("") && attachedPicID.equalsIgnoreCase(pic.PicID)) {
                    if (message.image == null) {
                        message.image = pic.image;
                    } else if (pic.IsFullPic) {
                        message.image = pic.image;
                    }
                    break;
                }
            }
        }
    }

    protected GDMessage(Parcel in) {
        MessageID = in.readString();
        SenderID = in.readString();
        SenderName = in.readString();
        RecieverID = in.readString();
        MessageText = in.readString();
        AttachedPicIDs = in.readString();
        Location = in.readString();
        MessageStatus = in.readString();
        SentDateTime = in.readString();
        ReadDateTime = in.readString();
        long tmpDSentDateTime = in.readLong();
        dSentDateTime = tmpDSentDateTime != -1 ? new Date(tmpDSentDateTime) : null;
        long tmpDReadDateTime = in.readLong();
        dReadDateTime = tmpDReadDateTime != -1 ? new Date(tmpDReadDateTime) : null;
        byte IsSentTimeUpdatedFromServerVal = in.readByte();
        IsSentTimeUpdatedFromServer = IsSentTimeUpdatedFromServerVal == 0x02 ? null : IsSentTimeUpdatedFromServerVal != 0x00;
        byte ReplaceAddToLocalDBVal = in.readByte();
        UserName = in.readString();
        PicID = in.readString();
        byte IsSelectedVal = in.readByte();
        IsSelected = IsSelectedVal == 0x02 ? null : IsSelectedVal != 0x00;
        byte ContainsSearchedTextVal = in.readByte();
        ContainsSearchedText = ContainsSearchedTextVal == 0x02 ? null : ContainsSearchedTextVal != 0x00;
        byte PhonoPhotoConversionDoneVal = in.readByte();
        SenderType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(MessageID);
        dest.writeString(SenderID);
        dest.writeString(SenderName);
        dest.writeString(RecieverID);
        dest.writeString(MessageText);
        dest.writeString(AttachedPicIDs);
        dest.writeString(Location);
        dest.writeString(MessageStatus);
        dest.writeString(SentDateTime);
        dest.writeString(ReadDateTime);
        dest.writeLong(dSentDateTime != null ? dSentDateTime.getTime() : -1L);
        dest.writeLong(dReadDateTime != null ? dReadDateTime.getTime() : -1L);
        if (IsSentTimeUpdatedFromServer == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsSentTimeUpdatedFromServer ? 0x01 : 0x00));
        }
        dest.writeString(UserName);
        dest.writeString(PicID);
        if (IsSelected == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsSelected ? 0x01 : 0x00));
        }
        if (ContainsSearchedText == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (ContainsSearchedText ? 0x01 : 0x00));
        }
        dest.writeString(SenderType);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GDMessage> CREATOR = new Parcelable.Creator<GDMessage>() {
        @Override
        public GDMessage createFromParcel(Parcel in) {
            return new GDMessage(in);
        }

        @Override
        public GDMessage[] newArray(int size) {
            return new GDMessage[size];
        }
    };

//    public static ArrayList<GDMessage> UpdateMessagesStatusAndTime(ArrayList<GDMessage> FromList, ArrayList<GDMessage> ToList) {
//        ArrayList<GDMessage> UnReadSentMessages = new ArrayList<>();
//        for (int i = 0; i < FromList.size(); i++) {
//            for (int j = 0; j < ToList.size(); j++) {
//                if (FromList.get(i).MessageID.equalsIgnoreCase(ToList.get(j).MessageID) &&
//                        !FromList.get(i).MessageStatus.equals(ToList.get(j).MessageStatus)) {
//                    if ((ToList.get(j).MessageStatus.equals("S") && (FromList.get(i).MessageStatus.equals("D") || FromList.get(i).MessageStatus.equals("R"))) ||
//                            ToList.get(j).MessageStatus.equals("D") && FromList.get(i).MessageStatus.equals("R")) {
//                        ToList.get(j).ReplaceAddToLocalDB = true;
//                        ToList.get(j).SentDateTime = FromList.get(i).SentDateTime;
//                        ToList.get(j).IsSentTimeUpdatedFromServer = FromList.get(i).IsSentTimeUpdatedFromServer;
//                        ToList.get(j).MessageStatus = FromList.get(i).MessageStatus;
//                        if (FromList.get(i).ReadDateTime != null && !FromList.get(i).ReadDateTime.equals("")) {
//                            ToList.get(j).ReadDateTime = FromList.get(i).ReadDateTime;
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//        return UnReadSentMessages;
//    }
}