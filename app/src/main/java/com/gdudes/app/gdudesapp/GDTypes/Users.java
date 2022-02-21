package com.gdudes.app.gdudesapp.GDTypes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Users implements Parcelable {
    public String UserID;
    public String UserName;
    public int Age;
    public String PicID;
    public Boolean OnlineStatus;
    public float Distance;
    public String LocationLatLng;
    public String Blocked;
    public String IBlockedHim;
    public String ShowDistanceInSearchTo;
    public String ShowAgeInSearchTo;
    public Boolean ShowInMapSearch;

    public String ShowMyDistanceInSearchTo;
    public Boolean MyShowInMapSearch;

    public String DOB;
    public int Height;
    public int Weight;
    public String BodyType;
    public String SexualOrientation;
    public String SexualPreference;
    public String LastActiveDateTime;
    public String EmailID;
    public Boolean EmailVerified;
    public Boolean PasswordNeedsUpdate;
    public Boolean ProfileComplete;
    public Boolean CanCategorizePics;
    public String UnitSystem;
    public String MobileLoginDT;
    public String HasPicsToBeCategorized;
    public Boolean IsPremium;
    public String RegisterDateTime;
    public Bitmap image = null;

    public Users(String vUserID) {
        this.UserID = vUserID;
    }

    @Override
    public boolean equals(Object second) {
        if (this.UserID.equalsIgnoreCase(((Users) second).UserID)) {
            return true;
        }
        return false;
    }

    public String GetDecodedUserName() {
        return StringEncoderHelper.decodeURIComponent(this.UserName);
    }

    protected Users(Parcel in) {
        UserID = in.readString();
        UserName = in.readString();
        DOB = in.readString();
        Age = in.readInt();
        Height = in.readInt();
        Weight = in.readInt();
        BodyType = in.readString();
        SexualOrientation = in.readString();
        SexualPreference = in.readString();
        PicID = in.readString();
        byte OnlineStatusVal = in.readByte();
        OnlineStatus = OnlineStatusVal == 0x02 ? null : OnlineStatusVal != 0x00;
        LastActiveDateTime = in.readString();
        LocationLatLng = in.readString();
        Distance = in.readFloat();
        Blocked = in.readString();
        IBlockedHim = in.readString();
        ShowDistanceInSearchTo = in.readString();
        ShowAgeInSearchTo = in.readString();
        EmailID = in.readString();
        byte EmailVerifiedVal = in.readByte();
        EmailVerified = EmailVerifiedVal == 0x02 ? null : EmailVerifiedVal != 0x00;
        byte PasswordNeedsUpdateVal = in.readByte();
        PasswordNeedsUpdate = PasswordNeedsUpdateVal == 0x02 ? null : PasswordNeedsUpdateVal != 0x00;
        byte ProfileCompleteVal = in.readByte();
        ProfileComplete = ProfileCompleteVal == 0x02 ? null : ProfileCompleteVal != 0x00;
        byte CanCategorizePicsVal = in.readByte();
        CanCategorizePics = CanCategorizePicsVal == 0x02 ? null : CanCategorizePicsVal != 0x00;
        UnitSystem = in.readString();
        MobileLoginDT = in.readString();
        HasPicsToBeCategorized = in.readString();
        byte IsPremiumVal = in.readByte();
        IsPremium = IsPremiumVal == 0x02 ? null : IsPremiumVal != 0x00;
        ShowMyDistanceInSearchTo = in.readString();
        byte ShowInMapSearchVal = in.readByte();
        ShowInMapSearch = ShowInMapSearchVal == 0x02 ? null : ShowInMapSearchVal != 0x00;
        byte MyShowInMapSearchVal = in.readByte();
        MyShowInMapSearch = MyShowInMapSearchVal == 0x02 ? null : MyShowInMapSearchVal != 0x00;
        RegisterDateTime = in.readString();
    }

    public static void MoveSelfToTopForDistance(List<Users> usersList, Users LoggedInUser) {
        if (usersList.size() < 2) {
            return;
        }
        try {
            int MyIndex = usersList.indexOf(LoggedInUser);
            Users MeUser = null;
            if (MyIndex != -1 && MyIndex != 0) {
                MeUser = usersList.get(MyIndex);
                if (MeUser != null) {
                    usersList.remove(MyIndex);
                    usersList.add(0, MeUser);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static Boolean BlockForUnverifiedEmail(Users LoggedInUser) {
        Boolean BlockForUnverifiedEmail = false;
        try {
            if (LoggedInUser != null && !LoggedInUser.EmailVerified) {
                Date RegisterDateTime = GDDateTimeHelper.GetDateFromString(LoggedInUser.RegisterDateTime);
                Date CurrentDateTime = GDDateTimeHelper.GetDateFromString(GDDateTimeHelper.GetCurrentDateTimeAsString(true));
                long diff = CurrentDateTime.getTime() - RegisterDateTime.getTime();
                long days = diff / 1000 / 60 / 60 / 24;
                if (days >= 60) {
                    BlockForUnverifiedEmail = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return BlockForUnverifiedEmail;
    }

    public static ArrayList<String> GetPicIDListFromUsers(ArrayList<Users> users) {
        ArrayList<String> picIDList = new ArrayList<>();
        String picID;
        for (int i = 0; i < users.size(); i++) {
            picID = users.get(i).PicID;
            if (picID != null && !picID.trim().equals("") && !picIDList.contains(picID.toLowerCase())) {
                picIDList.add(picID.toLowerCase());
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDList);
    }


    public static ArrayList<String> GetUserIDList(ArrayList<Users> users) {
        ArrayList<String> UserIDs = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            UserIDs.add(users.get(i).UserID);
        }
        return StringHelper.RemoveDuplicateEntries(UserIDs);
    }

    public static ArrayList<String> GetPicIDList(ArrayList<Users> users) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            String picID = users.get(i).PicID;
            if (!StringHelper.IsNullOrEmpty(picID)) {
                picIDs.add(users.get(i).PicID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static ArrayList<String> GetPicIDListForNullImages(ArrayList<Users> users) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            String picID = users.get(i).PicID;
            if (!StringHelper.IsNullOrEmpty(picID) && users.get(i).image == null) {
                picIDs.add(users.get(i).PicID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static void SetPicsToUsers(ArrayList<GDPic> pics, ArrayList<Users> users) {
        for (int i = 0; i < pics.size(); i++) {
            for (int j = 0; j < users.size(); j++) {
                String nPicID = users.get(j).PicID;
                if (!StringHelper.IsNullOrEmpty(nPicID) && nPicID.equalsIgnoreCase(pics.get(i).PicID)) {
                    users.get(j).image = pics.get(i).image;
                }
            }
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UserID);
        dest.writeString(UserName);
        dest.writeString(DOB);
        dest.writeInt(Age);
        dest.writeInt(Height);
        dest.writeInt(Weight);
        dest.writeString(BodyType);
        dest.writeString(SexualOrientation);
        dest.writeString(SexualPreference);
        dest.writeString(PicID);
        if (OnlineStatus == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (OnlineStatus ? 0x01 : 0x00));
        }
        dest.writeString(LastActiveDateTime);
        dest.writeString(LocationLatLng);
        dest.writeFloat(Distance);
        dest.writeString(Blocked);
        dest.writeString(IBlockedHim);
        dest.writeString(ShowDistanceInSearchTo);
        dest.writeString(ShowAgeInSearchTo);
        dest.writeString(EmailID);
        if (EmailVerified == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (EmailVerified ? 0x01 : 0x00));
        }
        if (PasswordNeedsUpdate == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (PasswordNeedsUpdate ? 0x01 : 0x00));
        }
        if (ProfileComplete == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (ProfileComplete ? 0x01 : 0x00));
        }
        if (CanCategorizePics == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (CanCategorizePics ? 0x01 : 0x00));
        }
        dest.writeString(UnitSystem);
        dest.writeString(MobileLoginDT);
        dest.writeString(HasPicsToBeCategorized);
        if (IsPremium == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsPremium ? 0x01 : 0x00));
        }
        dest.writeString(ShowMyDistanceInSearchTo);
        if (MyShowInMapSearch == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (MyShowInMapSearch ? 0x01 : 0x00));
        }
        if (ShowInMapSearch == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (ShowInMapSearch ? 0x01 : 0x00));
        }
        dest.writeString(RegisterDateTime);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Users> CREATOR = new Parcelable.Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };
}