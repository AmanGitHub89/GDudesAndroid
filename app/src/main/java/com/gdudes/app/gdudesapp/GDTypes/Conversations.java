package com.gdudes.app.gdudesapp.GDTypes;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Conversations implements Parcelable {
    public String UserID;
    public String UserName;
    public String PicID;
    public String ProfilePicMini;
    public String ProfilePic;
    public String LastMessageDT;
    public Date dLastMessageDT;
    public String SenderType;
    public String UnreadCount;
    public Boolean IsDataRefreshed;
    public Date dLastDataRefreshedDT;
    public Users ConversationWithUser;
    public String LastMessageLocalDT;
    public Date dLastMessageLocalDT;
    public GDMessage LastMessage;
    public Boolean GetCountFromLocalDB = true;
    public int UnMessageCount = 0;
    public Bitmap image = null;

    public Conversations() {
        UserID = "";
        UserName = "";
        PicID = "";
        ProfilePic = "";
        LastMessageDT = "";
        dLastMessageDT = Calendar.getInstance().getTime();
        SenderType = "";
        UnreadCount = "";
        IsDataRefreshed = false;
        ConversationWithUser = null;
        LastMessage = null;
        GetCountFromLocalDB = true;
    }

    public Conversations(String vUserID) {
        UserID = vUserID;
        UserName = "";
        PicID = "";
        ProfilePic = "";
        LastMessageDT = "";
        dLastMessageDT = Calendar.getInstance().getTime();
        SenderType = "";
        UnreadCount = "";
        IsDataRefreshed = false;
        ConversationWithUser = null;
        LastMessage = null;
        GetCountFromLocalDB = true;
    }

    public Conversations(String vUserID, String vUserName, String vPicID, String vProfilePic, String vsLastMessageDT,
                         String vSenderType, String vUnreadCount, Boolean vIsDataRefreshed, Users vConversationWithUser,
                         String vsLastMessageLocalDT) {
        UserID = vUserID;
        UserName = vUserName;
        PicID = vPicID;
        ProfilePic = vProfilePic;
        LastMessageDT = vsLastMessageDT;
        dLastMessageDT = GDDateTimeHelper.GetDateFromString(vsLastMessageDT);
        SenderType = vSenderType;
        UnreadCount = vUnreadCount;
        IsDataRefreshed = vIsDataRefreshed;
        ConversationWithUser = vConversationWithUser;
        LastMessage = null;
        LastMessageLocalDT = (vsLastMessageLocalDT == null || vsLastMessageLocalDT.equals("")) ? vsLastMessageDT : vsLastMessageLocalDT;
        GetCountFromLocalDB = true;
    }

    @Override
    public boolean equals(Object second) {
        if (this.UserID.equalsIgnoreCase(((Conversations) second).UserID)) {
            return true;
        }
        return false;
    }

    public static void SetDateForConversationList(ArrayList<Conversations> ConversationsList) {
        try {
            for (int i = 0; i < ConversationsList.size(); i++) {
                ConversationsList.get(i).dLastMessageDT = GDDateTimeHelper.GetDateFromString(ConversationsList.get(i).LastMessageDT);
                ConversationsList.get(i).dLastMessageLocalDT = GDDateTimeHelper.GetDateFromString(ConversationsList.get(i).LastMessageLocalDT);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void SetDataRefreshed(ArrayList<Conversations> ToList, ArrayList<Conversations> FromList) {
        try {
            for (int i = 0; i < FromList.size(); i++) {
                Conversations conversation = new Conversations();
                conversation.UserID = FromList.get(i).UserID;
                int index = ToList.indexOf(conversation);
                ToList.get(index).IsDataRefreshed = true;
                ToList.get(i).dLastDataRefreshedDT = new Date();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void SetDataRefreshed(ArrayList<Conversations> conversations) {
        try {
            for (int i = 0; i < conversations.size(); i++) {
                conversations.get(i).IsDataRefreshed = true;
                conversations.get(i).dLastDataRefreshedDT = new Date();
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public static void SetDataRefreshed(ArrayList<Conversations> ConversationsList, int StartIndex, int EndIndex) {
        try {
            for (int i = StartIndex; i <= EndIndex; i++) {
                ConversationsList.get(i).IsDataRefreshed = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void SetMiniPicSrc(ArrayList<Conversations> ConversationsList) {
        try {
            for (int i = 0; i < ConversationsList.size(); i++) {
                ConversationsList.get(i).ProfilePicMini = ConversationsList.get(i).ProfilePic;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void SetThumbnailPicSrcFromLocalDB(ArrayList<Conversations> ConversationsList, Context context) {
        try {
            final GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(context);
            String PicSrc = "";
            for (int i = 0; i < ConversationsList.size(); i++) {
                PicSrc = gdImageDBHelper.GetImageStringByPicID(ConversationsList.get(i).PicID, false);
                if (!PicSrc.equals("")) {
                    ConversationsList.get(i).ProfilePic = PicSrc;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void SetGetCountFromLocalDBToTrue(ArrayList<Conversations> ConversationsList, ArrayList<String> UserIDList) {
        try {
            Conversations conversation;
            for (int i = 0; i < UserIDList.size(); i++) {
                try {
                    conversation = new Conversations(UserIDList.get(i));
                    int index = ConversationsList.indexOf(conversation);
                    if (index > -1) {
                        ConversationsList.get(index).GetCountFromLocalDB = true;
                        ConversationsList.get(index).LastMessage = null;
                    }
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static ArrayList<String> GetUserIDList(ArrayList<Conversations> ConversationsList) {
        ArrayList<String> UserIDs = new ArrayList<>();
        try {
            for (int i = 0; i < ConversationsList.size(); i++) {
                UserIDs.add(ConversationsList.get(i).UserID);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return UserIDs;
    }

    public static ArrayList<String> GetPicIDList(ArrayList<Conversations> conversations) {
        ArrayList<String> picIDList = new ArrayList<>();
        try {
            for (int i = 0; i < conversations.size(); i++) {
                String picID = conversations.get(i).PicID;
                if (!StringHelper.IsNullOrEmpty(picID)) {
                    picIDList.add(picID);
                }
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        StringHelper.RemoveDuplicateEntries(picIDList);
        return picIDList;
    }

    public static ArrayList<String> GetPicIDListForNullImages(ArrayList<Conversations> conversations) {
        ArrayList<String> picIDList = new ArrayList<>();
        try {
            for (int i = 0; i < conversations.size(); i++) {
                String picID = conversations.get(i).PicID;
                if (!StringHelper.IsNullOrEmpty(picID) && conversations.get(i).image == null) {
                    picIDList.add(picID);
                }
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        StringHelper.RemoveDuplicateEntries(picIDList);
        return picIDList;
    }

    public static Boolean DataRefreshedMoreThan3MinsAgo(Conversations conversation) {
        try {
            if (conversation.dLastDataRefreshedDT == null) {
                return true;
            }
            Date TimeNow = new Date();
            long secs = (TimeNow.getTime() - conversation.dLastDataRefreshedDT.getTime()) / 1000;
            if (secs > 180) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    protected Conversations(Parcel in) {
        UserID = in.readString();
        UserName = in.readString();
        PicID = in.readString();
        ProfilePic = in.readString();
        ProfilePicMini = in.readString();
        LastMessageDT = in.readString();
        long tmpLastMessageDT = in.readLong();
        dLastMessageDT = tmpLastMessageDT != -1 ? new Date(tmpLastMessageDT) : null;
        SenderType = in.readString();
        UnreadCount = in.readString();
        byte IsDataRefreshedVal = in.readByte();
        IsDataRefreshed = IsDataRefreshedVal == 0x02 ? null : IsDataRefreshedVal != 0x00;
        ConversationWithUser = (Users) in.readValue(Users.class.getClassLoader());
        LastMessage = (GDMessage) in.readValue(Users.class.getClassLoader());
        LastMessageLocalDT = in.readString();
        long tmpLastMessageLocalDT = in.readLong();
        dLastMessageLocalDT = tmpLastMessageLocalDT != -1 ? new Date(tmpLastMessageLocalDT) : null;
        long tmpLastDataRefreshedDT = in.readLong();
        dLastDataRefreshedDT = tmpLastDataRefreshedDT != -1 ? new Date(tmpLastDataRefreshedDT) : null;
        byte GetCountFromLocalDBVal = in.readByte();
        GetCountFromLocalDB = GetCountFromLocalDBVal == 0x02 ? null : GetCountFromLocalDBVal != 0x00;
        UnMessageCount = in.readInt();
    }

    public static void AddOrUpdateConversations(ArrayList<Conversations> FromList, ArrayList<Conversations> ToList) {
        try {
            for (int i = 0; i < FromList.size(); i++) {
                if (!ToList.contains(FromList.get(i))) {
                    if (FromList.get(i).LastMessageLocalDT == null || FromList.get(i).LastMessageLocalDT.equals("")) {
                        FromList.get(i).LastMessageLocalDT = FromList.get(i).LastMessageDT;
                    }
                    ToList.add(FromList.get(i));
                } else {
                    int index = ToList.indexOf(FromList.get(i));
                    ToList.get(index).LastMessageDT = FromList.get(i).LastMessageDT;
                    ToList.get(index).LastMessageLocalDT = (FromList.get(i).LastMessageLocalDT == null || FromList.get(i).LastMessageLocalDT.equals("")) ?
                            FromList.get(i).LastMessageDT : FromList.get(i).LastMessageLocalDT;
                    ToList.get(index).IsDataRefreshed = false;
                    ToList.get(index).GetCountFromLocalDB = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static String GetLastServerDT(ArrayList<Conversations> ConversationsList) {
        String LastMessageDateTime = "";
        try {
            if (ConversationsList.size() == 0) {
                return GDDateTimeHelper.GetCurrentDateTimeAsString(true);
            }
            LastMessageDateTime = ConversationsList.get(0).LastMessageDT;
            for (int i = 1; i < ConversationsList.size(); i++) {
                if (ConversationsList.get(i).LastMessageDT.compareTo(LastMessageDateTime) == 1) {
                    LastMessageDateTime = ConversationsList.get(i).LastMessageDT;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return LastMessageDateTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UserID);
        dest.writeString(UserName);
        dest.writeString(PicID);
        dest.writeString(ProfilePic);
        dest.writeString(ProfilePicMini);
        dest.writeString(LastMessageDT);
        dest.writeLong(dLastMessageDT != null ? dLastMessageDT.getTime() : -1L);
        dest.writeString(SenderType);
        dest.writeString(UnreadCount);
        if (IsDataRefreshed == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsDataRefreshed ? 0x01 : 0x00));
        }
        dest.writeValue(ConversationWithUser);
        dest.writeValue(LastMessage);
        dest.writeString(LastMessageLocalDT);
        dest.writeLong(dLastMessageLocalDT != null ? dLastMessageLocalDT.getTime() : -1L);
        dest.writeLong(dLastDataRefreshedDT != null ? dLastDataRefreshedDT.getTime() : -1L);
        if (GetCountFromLocalDB == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (GetCountFromLocalDB ? 0x01 : 0x00));
        }
        dest.writeInt(UnMessageCount);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Conversations> CREATOR = new Parcelable.Creator<Conversations>() {
        @Override
        public Conversations createFromParcel(Parcel in) {
            return new Conversations(in);
        }

        @Override
        public Conversations[] newArray(int size) {
            return new Conversations[size];
        }
    };
}