package com.gdudes.app.gdudesapp.GDTypes;

import android.os.Parcel;
import android.os.Parcelable;

public class SavedFilter implements Parcelable {
    public String UserID;
    public String SearchID;
    public String SearchName;
    public String SearchType;
    public String SortByCode;
    public String SetSearchRadius;
    public String SearchRadiusMin;
    public String SearchRadiusMax;
    public String WithPics;
    public String CurrentlyOnline;
    public String MapLocationVisible;
    public String MinHeight;
    public String MaxHeight;
    public String MinWeight;
    public String MaxWeight;
    public String MinAge;
    public String MaxAge;
    public String SexualPreference;
    public String Sucking;
    public String Interests;
    public String BodyType;
    public String Ethnicity;
    public String Religion;
    public String Hair;
    public String BodyHair;
    public String FacialHair;
    public String Tattoos;
    public String Piercings;
    public String ToolSize;
    public String ToolType;
    public String Smoking;
    public String Drinking;
    public String SexualOrientation;
    public String RelationshipStatus;
    public String TagLineContains;
    public String DetailedDescContains;
    public Boolean IsSelected;
    public Boolean IsCustom;

    @Override
    public boolean equals(Object second) {
        if (this.SearchID.equalsIgnoreCase(((SavedFilter) second).SearchID)) {
            return true;
        }
        return false;
    }

    public SavedFilter() {
        UserID = "";
        SearchID = "";
        SearchName = "";
        SearchType = "";
        SortByCode = "";
        SetSearchRadius = "false";
        SearchRadiusMin = "0";
        SearchRadiusMax = "0";
        WithPics = "";
        CurrentlyOnline = "";
        MapLocationVisible = "";
        MinHeight = "0";
        MaxHeight = "0";
        MinWeight = "0";
        MaxWeight = "0";
        MinAge = "0";
        MaxAge = "0";
        SexualPreference = "";
        Sucking = "";
        Interests = "";
        BodyType = "";
        Ethnicity = "";
        Religion = "";
        Hair = "";
        BodyHair = "";
        FacialHair = "";
        Tattoos = "";
        Piercings = "";
        ToolSize = "";
        ToolType = "";
        Smoking = "";
        Drinking = "";
        SexualOrientation = "";
        RelationshipStatus = "";
        TagLineContains = "";
        DetailedDescContains = "";
        IsSelected = false;
        IsCustom = true;
    }

    public SavedFilter(String vSearchName, String vSearchID) {
        UserID = "";
        SearchID = vSearchID;
        SearchName = vSearchName;
        SearchType = "";
        SortByCode = "";
        SetSearchRadius = "false";
        SearchRadiusMin = "0";
        SearchRadiusMax = "0";
        WithPics = "";
        CurrentlyOnline = "";
        MapLocationVisible = "";
        MinHeight = "0";
        MaxHeight = "0";
        MinWeight = "0";
        MaxWeight = "0";
        MinAge = "0";
        MaxAge = "0";
        SexualPreference = "";
        Sucking = "";
        Interests = "";
        BodyType = "";
        Ethnicity = "";
        Religion = "";
        Hair = "";
        BodyHair = "";
        FacialHair = "";
        Tattoos = "";
        Piercings = "";
        ToolSize = "";
        ToolType = "";
        Smoking = "";
        Drinking = "";
        SexualOrientation = "";
        RelationshipStatus = "";
        TagLineContains = "";
        DetailedDescContains = "";
        IsSelected = false;
        IsCustom = true;
    }

    protected SavedFilter(Parcel in) {
        UserID = in.readString();
        SearchID = in.readString();
        SearchName = in.readString();
        SearchType = in.readString();
        SortByCode = in.readString();
        SetSearchRadius = in.readString();
        SearchRadiusMin = in.readString();
        SearchRadiusMax = in.readString();
        WithPics = in.readString();
        CurrentlyOnline = in.readString();
        MapLocationVisible = in.readString();
        MinHeight = in.readString();
        MaxHeight = in.readString();
        MinWeight = in.readString();
        MaxWeight = in.readString();
        MinAge = in.readString();
        MaxAge = in.readString();
        SexualPreference = in.readString();
        Sucking = in.readString();
        Interests = in.readString();
        BodyType = in.readString();
        Ethnicity = in.readString();
        Religion = in.readString();
        Hair = in.readString();
        BodyHair = in.readString();
        FacialHair = in.readString();
        Tattoos = in.readString();
        Piercings = in.readString();
        ToolSize = in.readString();
        ToolType = in.readString();
        Smoking = in.readString();
        Drinking = in.readString();
        SexualOrientation = in.readString();
        RelationshipStatus = in.readString();
        TagLineContains = in.readString();
        DetailedDescContains = in.readString();
        byte IsSelectedVal = in.readByte();
        IsSelected = IsSelectedVal == 0x02 ? null : IsSelectedVal != 0x00;
        byte IsCustomVal = in.readByte();
        IsCustom = IsCustomVal == 0x02 ? null : IsCustomVal != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UserID);
        dest.writeString(SearchID);
        dest.writeString(SearchName);
        dest.writeString(SearchType);
        dest.writeString(SortByCode);
        dest.writeString(SetSearchRadius);
        dest.writeString(SearchRadiusMin);
        dest.writeString(SearchRadiusMax);
        dest.writeString(WithPics);
        dest.writeString(CurrentlyOnline);
        dest.writeString(MapLocationVisible);
        dest.writeString(MinHeight);
        dest.writeString(MaxHeight);
        dest.writeString(MinWeight);
        dest.writeString(MaxWeight);
        dest.writeString(MinAge);
        dest.writeString(MaxAge);
        dest.writeString(SexualPreference);
        dest.writeString(Sucking);
        dest.writeString(Interests);
        dest.writeString(BodyType);
        dest.writeString(Ethnicity);
        dest.writeString(Religion);
        dest.writeString(Hair);
        dest.writeString(BodyHair);
        dest.writeString(FacialHair);
        dest.writeString(Tattoos);
        dest.writeString(Piercings);
        dest.writeString(ToolSize);
        dest.writeString(ToolType);
        dest.writeString(Smoking);
        dest.writeString(Drinking);
        dest.writeString(SexualOrientation);
        dest.writeString(RelationshipStatus);
        dest.writeString(TagLineContains);
        dest.writeString(DetailedDescContains);
        if (IsSelected == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsSelected ? 0x01 : 0x00));
        }
        if (IsCustom == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsCustom ? 0x01 : 0x00));
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SavedFilter> CREATOR = new Parcelable.Creator<SavedFilter>() {
        @Override
        public SavedFilter createFromParcel(Parcel in) {
            return new SavedFilter(in);
        }

        @Override
        public SavedFilter[] newArray(int size) {
            return new SavedFilter[size];
        }
    };
}