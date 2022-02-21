package com.gdudes.app.gdudesapp.GDTypes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class GDGuidAndGuid implements Parcelable {
    public String Guid1;
    public String Guid2;

    public GDGuidAndGuid() {
        Guid1 = "";
        Guid2 = "";
    }

    public GDGuidAndGuid(String vGuid1, String vGuid2) {
        Guid1 = vGuid1;
        Guid2 = vGuid2;
    }

    protected GDGuidAndGuid(Parcel in) {
        Guid1 = in.readString();
        Guid2 = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Guid1);
        dest.writeString(Guid2);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GDGuidAndGuid> CREATOR = new Parcelable.Creator<GDGuidAndGuid>() {
        @Override
        public GDGuidAndGuid createFromParcel(Parcel in) {
            return new GDGuidAndGuid(in);
        }

        @Override
        public GDGuidAndGuid[] newArray(int size) {
            return new GDGuidAndGuid[size];
        }
    };

    @Override
    public boolean equals(Object second) {
        if (this.Guid1.equalsIgnoreCase(((GDGuidAndGuid) second).Guid1)) {
            return true;
        }
        return false;
    }

    public static String FlattenGuid1List(ArrayList<GDGuidAndGuid> GDGuidAndGuidList) {
        String FlattenedString = "";
        if (GDGuidAndGuidList == null || GDGuidAndGuidList.size() == 0) {
            return FlattenedString;
        }
        for (int i = 0; i < GDGuidAndGuidList.size(); i++) {
            FlattenedString = FlattenedString + GDGuidAndGuidList.get(i).Guid1 + ",";
        }
        FlattenedString = FlattenedString.substring(0, FlattenedString.length() - 1);
        return FlattenedString;
    }

    public static String FlattenGuid2List(ArrayList<GDGuidAndGuid> GDGuidAndGuidList) {
        String FlattenedString = "";
        if (GDGuidAndGuidList == null || GDGuidAndGuidList.size() == 0) {
            return FlattenedString;
        }
        for (int i = 0; i < GDGuidAndGuidList.size(); i++) {
            FlattenedString = FlattenedString + GDGuidAndGuidList.get(i).Guid2 + ",";
        }
        FlattenedString = FlattenedString.substring(0, FlattenedString.length() - 1);
        return FlattenedString;
    }
}