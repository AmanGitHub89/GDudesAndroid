package com.gdudes.app.gdudesapp.GDTypes;

import android.os.Parcel;
import android.os.Parcelable;

public class UserLocation implements Parcelable {
    public String LocationID;
    public String LocationNickName;
    public String LocationLatLng;
    public String UserID;
    public Boolean PrimaryLocation;
    public Boolean IsSelected = false;

    public UserLocation() {
        this.LocationID = "";
        this.LocationNickName = "";
        this.LocationLatLng = "";
        this.UserID = "";
        this.PrimaryLocation = false;
    }

    public UserLocation(String vLocationID, String vLocationNickName, String vLocationLatLng, String vUserID, Boolean vPrimaryLocation) {
        this.LocationID = vLocationID;
        this.LocationNickName = vLocationNickName;
        this.LocationLatLng = vLocationLatLng;
        this.UserID = vUserID;
        this.PrimaryLocation = vPrimaryLocation;
    }


    @Override
    public boolean equals(Object second) {
        if (this.LocationID.equalsIgnoreCase(((UserLocation) second).LocationID)) {
            return true;
        }
        return false;
    }


    protected UserLocation(Parcel in) {
        LocationID = in.readString();
        LocationNickName = in.readString();
        LocationLatLng = in.readString();
        UserID = in.readString();
        byte PrimaryLocationVal = in.readByte();
        PrimaryLocation = PrimaryLocationVal == 0x02 ? null : PrimaryLocationVal != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(LocationID);
        dest.writeString(LocationNickName);
        dest.writeString(LocationLatLng);
        dest.writeString(UserID);
        if (PrimaryLocation == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (PrimaryLocation ? 0x01 : 0x00));
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserLocation> CREATOR = new Parcelable.Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel in) {
            return new UserLocation(in);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };
}