package com.gdudes.app.gdudesapp.GDTypes;

import android.os.Parcel;
import android.os.Parcelable;

public class PrivacySettings implements Parcelable {
    public String UserID;
    public String ShowDistanceInSearchTo;
    public String ShowAgeInSearchTo;

    public PrivacySettings(String vUserID) {
        UserID = vUserID;
        ShowDistanceInSearchTo = "E";
        ShowAgeInSearchTo = "E";
    }

    protected PrivacySettings(Parcel in) {
        UserID = in.readString();
        ShowDistanceInSearchTo = in.readString();
        ShowAgeInSearchTo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UserID);
        dest.writeString(ShowDistanceInSearchTo);
        dest.writeString(ShowAgeInSearchTo);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PrivacySettings> CREATOR = new Parcelable.Creator<PrivacySettings>() {
        @Override
        public PrivacySettings createFromParcel(Parcel in) {
            return new PrivacySettings(in);
        }

        @Override
        public PrivacySettings[] newArray(int size) {
            return new PrivacySettings[size];
        }
    };
}