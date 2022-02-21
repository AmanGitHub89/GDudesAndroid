package com.gdudes.app.gdudesapp.GDTypes;

import android.os.Parcel;
import android.os.Parcelable;

public class Professional implements Parcelable {
    public String ProfessionID;
    public String UserID;
    public String ProfessionType;
    public String ProfessionTypeCode;
    public String UserName;
    public String ProfilePic;
    public String PicID;
    public float Age;
    public String Location;
    public String LocationLatLng;
    public String TimeStart;
    public String TimeEnd;
    public String AvailableDays;
    public String CanHost;
    public String Charges;
    public String Currency;
    public String CurrencyCode;
    public String MobileNumber;
    public String ExperiencedSince;
    public String TagLine;
    public String Description;
    public String Blocked;
    public String RecieveMsgFrom;
    public int IsFriend;
    public float Distance;
    public String PhoneNoVisible;
    public String ShowOnMap;
    public String AvailableDaysCode;
    public String OtherProfession;
    public Boolean IsActive;
    public String CreatedDT;
    public String EndsOnDate;

    public Professional(String vProfessionID, String vUserID, String vProfessionType, String vOtherProfession,
                        String vLocation, String vLocationLatLng, String vShowOnMap, String vTimeStart, String vTimeEnd,
                        String vAvailableDays, String vCanHost, String vCharges, String vCurrency, String vPhoneNoVisible,
                        String vExperiencedSince, String vTagLine, String vDescription) {
        ProfessionID = vProfessionID;
        UserID = vUserID;
        ProfessionType = vProfessionType;
        OtherProfession = vOtherProfession;
        Location = vLocation;
        LocationLatLng = vLocationLatLng;
        ShowOnMap = vShowOnMap;
        TimeStart = vTimeStart;
        TimeEnd = vTimeEnd;
        AvailableDays = vAvailableDays;
        CanHost = vCanHost;
        Charges = vCharges;
        Currency = vCurrency;
        PhoneNoVisible = vPhoneNoVisible;
        ExperiencedSince = vExperiencedSince;
        TagLine = vTagLine;
        Description = vDescription;
    }

    @Override
    public boolean equals(Object second) {
        if (this.ProfessionID.equalsIgnoreCase(((Professional) second).ProfessionID)) {
            return true;
        }
        return false;
    }

    protected Professional(Parcel in) {
        ProfessionID = in.readString();
        UserID = in.readString();
        ProfessionType = in.readString();
        ProfessionTypeCode = in.readString();
        UserName = in.readString();
        ProfilePic = in.readString();
        PicID = in.readString();
        Age = in.readFloat();
        Location = in.readString();
        LocationLatLng = in.readString();
        TimeStart = in.readString();
        TimeEnd = in.readString();
        AvailableDays = in.readString();
        CanHost = in.readString();
        Charges = in.readString();
        Currency = in.readString();
        CurrencyCode = in.readString();
        MobileNumber = in.readString();
        ExperiencedSince = in.readString();
        TagLine = in.readString();
        Description = in.readString();
        Blocked = in.readString();
        RecieveMsgFrom = in.readString();
        IsFriend = in.readInt();
        Distance = in.readFloat();
        PhoneNoVisible = in.readString();
        ShowOnMap = in.readString();
        AvailableDaysCode = in.readString();
        OtherProfession = in.readString();
        byte IsActiveVal = in.readByte();
        IsActive = IsActiveVal == 0x02 ? null : IsActiveVal != 0x00;
        CreatedDT = in.readString();
        EndsOnDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ProfessionID);
        dest.writeString(UserID);
        dest.writeString(ProfessionType);
        dest.writeString(ProfessionTypeCode);
        dest.writeString(UserName);
        dest.writeString(ProfilePic);
        dest.writeString(PicID);
        dest.writeFloat(Age);
        dest.writeString(Location);
        dest.writeString(LocationLatLng);
        dest.writeString(TimeStart);
        dest.writeString(TimeEnd);
        dest.writeString(AvailableDays);
        dest.writeString(CanHost);
        dest.writeString(Charges);
        dest.writeString(Currency);
        dest.writeString(CurrencyCode);
        dest.writeString(MobileNumber);
        dest.writeString(ExperiencedSince);
        dest.writeString(TagLine);
        dest.writeString(Description);
        dest.writeString(Blocked);
        dest.writeString(RecieveMsgFrom);
        dest.writeInt(IsFriend);
        dest.writeFloat(Distance);
        dest.writeString(PhoneNoVisible);
        dest.writeString(ShowOnMap);
        dest.writeString(AvailableDaysCode);
        dest.writeString(OtherProfession);
        if (IsActive == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsActive ? 0x01 : 0x00));
        }
        dest.writeString(CreatedDT);
        dest.writeString(EndsOnDate);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Professional> CREATOR = new Parcelable.Creator<Professional>() {
        @Override
        public Professional createFromParcel(Parcel in) {
            return new Professional(in);
        }

        @Override
        public Professional[] newArray(int size) {
            return new Professional[size];
        }
    };
}
