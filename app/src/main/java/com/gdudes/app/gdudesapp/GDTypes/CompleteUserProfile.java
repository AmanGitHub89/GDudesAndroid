package com.gdudes.app.gdudesapp.GDTypes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;

public class CompleteUserProfile implements Parcelable {
    public String UserID;
    public String ProfilePicID;
    public String UserName;
    public String FullName;
    public int Height;
    public int Weight;
    public String DOB;
    public int Age;
    public String Email;
    public String MobileNo;
    public String TagLine;
    public int WaistSize;
    public String BodyType;
    public String Ethnicity;
    public String HairType;
    public String BodyHairType;
    public String FacialHairType;
    public String Tattoo;
    public String Piercings;
    public String SexualOrientation;
    public String SexualPreference;
    public String ToolSize;
    public String ToolType;
    public String Orals;
    public String SAndM;
    public String LookingFor;
    public String Fetish;
    public String Profession;
    public String Religion;
    public String Smoking;
    public String Drinking;
    public String RelationshipStatus;
    public String Languages;
    public String DetailedText;
    public float Distance;
    public String Area;
    public String LatLng;
    public String ShowDistanceInSearchTo;
    public String ShowAgeInSearchTo;
    public String BlockedByMe;
    public String MyLatLng;
    public Boolean OnlineStatus;
    public String LastActiveDateTime;
    public Boolean ShowInMapSearch;
    public Boolean MyShowInMapSearch;
    public String ShowMyDistanceInSearchTo;
    public String InstagramUserName;
    public String EndsOnDate;
    public Boolean IsPremium;
    public int MyPhotosCount;
    public int HasPicsToBeCategorized;
    public int PopularityRank;
    public String ProfileCachedDateTime = "";
    public String IsFavorite;
    public Bitmap image;

    @Override
    public boolean equals(Object second) {
        if (this.UserID.equalsIgnoreCase(((CompleteUserProfile) second).UserID)) {
            return true;
        }
        return false;
    }

    public String GetDecodedUserName() {
        return StringEncoderHelper.decodeURIComponent(this.UserName);
    }


    public CompleteUserProfile(String vUserID) {
        this.UserID = vUserID;
    }

    protected CompleteUserProfile(Parcel in) {
        UserID = in.readString();
        ProfilePicID = in.readString();
        UserName = in.readString();
        FullName = in.readString();
        Height = in.readInt();
        Weight = in.readInt();
        DOB = in.readString();
        Age = in.readInt();
        Email = in.readString();
        MobileNo = in.readString();
        TagLine = in.readString();
        WaistSize = in.readInt();
        BodyType = in.readString();
        Ethnicity = in.readString();
        HairType = in.readString();
        BodyHairType = in.readString();
        FacialHairType = in.readString();
        Tattoo = in.readString();
        Piercings = in.readString();
        SexualOrientation = in.readString();
        SexualPreference = in.readString();
        ToolSize = in.readString();
        ToolType = in.readString();
        Orals = in.readString();
        SAndM = in.readString();
        LookingFor = in.readString();
        Fetish = in.readString();
        Profession = in.readString();
        Religion = in.readString();
        Smoking = in.readString();
        Drinking = in.readString();
        RelationshipStatus = in.readString();
        Languages = in.readString();
        DetailedText = in.readString();
        Distance = in.readFloat();
        Area = in.readString();
        LatLng = in.readString();
        ShowDistanceInSearchTo = in.readString();
        ShowAgeInSearchTo = in.readString();
        BlockedByMe = in.readString();
        MyLatLng = in.readString();
        byte OnlineStatusVal = in.readByte();
        OnlineStatus = OnlineStatusVal == 0x02 ? null : OnlineStatusVal != 0x00;
        LastActiveDateTime = in.readString();
        ShowMyDistanceInSearchTo = in.readString();
        byte ShowInMapSearchVal = in.readByte();
        ShowInMapSearch = ShowInMapSearchVal == 0x02 ? null : ShowInMapSearchVal != 0x00;
        byte MyShowInMapSearchVal = in.readByte();
        MyShowInMapSearch = MyShowInMapSearchVal == 0x02 ? null : MyShowInMapSearchVal != 0x00;
        InstagramUserName = in.readString();
        byte IsPremiumVal = in.readByte();
        IsPremium = IsPremiumVal == 0x02 ? null : IsPremiumVal != 0x00;
        EndsOnDate = in.readString();
        MyPhotosCount = in.readInt();
        IsFavorite = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UserID);
        dest.writeString(ProfilePicID);
        dest.writeString(UserName);
        dest.writeString(FullName);
        dest.writeInt(Height);
        dest.writeInt(Weight);
        dest.writeString(DOB);
        dest.writeInt(Age);
        dest.writeString(Email);
        dest.writeString(MobileNo);
        dest.writeString(TagLine);
        dest.writeInt(WaistSize);
        dest.writeString(BodyType);
        dest.writeString(Ethnicity);
        dest.writeString(HairType);
        dest.writeString(BodyHairType);
        dest.writeString(FacialHairType);
        dest.writeString(Tattoo);
        dest.writeString(Piercings);
        dest.writeString(SexualOrientation);
        dest.writeString(SexualPreference);
        dest.writeString(ToolSize);
        dest.writeString(ToolType);
        dest.writeString(Orals);
        dest.writeString(SAndM);
        dest.writeString(LookingFor);
        dest.writeString(Fetish);
        dest.writeString(Profession);
        dest.writeString(Religion);
        dest.writeString(Smoking);
        dest.writeString(Drinking);
        dest.writeString(RelationshipStatus);
        dest.writeString(Languages);
        dest.writeString(DetailedText);
        dest.writeFloat(Distance);
        dest.writeString(Area);
        dest.writeString(LatLng);
        dest.writeString(ShowDistanceInSearchTo);
        dest.writeString(ShowAgeInSearchTo);
        dest.writeString(BlockedByMe);
        dest.writeString(MyLatLng);
        if (OnlineStatus == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (OnlineStatus ? 0x01 : 0x00));
        }
        dest.writeString(LastActiveDateTime);
        dest.writeString(ShowMyDistanceInSearchTo);
        dest.writeString(InstagramUserName);
        if (IsPremium == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsPremium ? 0x01 : 0x00));
        }
        dest.writeString(EndsOnDate);
        dest.writeInt(MyPhotosCount);
        dest.writeString(IsFavorite);
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
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CompleteUserProfile> CREATOR = new Parcelable.Creator<CompleteUserProfile>() {
        @Override
        public CompleteUserProfile createFromParcel(Parcel in) {
            return new CompleteUserProfile(in);
        }

        @Override
        public CompleteUserProfile[] newArray(int size) {
            return new CompleteUserProfile[size];
        }
    };
}
