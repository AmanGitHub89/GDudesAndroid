package com.gdudes.app.gdudesapp.GDTypes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class NearbyUser  implements Parcelable {
    @SerializedName("Id")
    public String UserID;
    @SerializedName("UName")
    public String UserName;
    public int Age;
    public String PicID;
    @SerializedName("Onl")
    public Boolean OnlineStatus;
    @SerializedName("Dist")
    public float Distance;
    @SerializedName("LatLng")
    public String LocationLatLng;
    @SerializedName("Blkd")
    public String Blocked;
    @SerializedName("IBlkd")
    public String IBlockedHim;
    @SerializedName("SDist")
    public String ShowDistanceInSearchTo;
    @SerializedName("SAge")
    public String ShowAgeInSearchTo;
    @SerializedName("SMap")
    public Boolean ShowInMapSearch;

    public String ShowMyDistanceInSearchTo = "Y";
    public Boolean MyShowInMapSearch = false;

    public static ArrayList<Users> ConvertToUsers(ArrayList<NearbyUser> nearbyUsers) {
        ArrayList<Users> users = new ArrayList<>();
        for (NearbyUser nearbyUser : nearbyUsers) {
            users.add(nearbyUser.ConvertToUser());
        }
        return users;
    }

    public static ArrayList<Users> ConvertToUsers(ArrayList<NearbyUser> nearbyUsers, String showMyDistance,
            Boolean myShowInMapSearch) {
        ArrayList<Users> users = new ArrayList<>();
        for (NearbyUser nearbyUser : nearbyUsers) {
            nearbyUser.ShowMyDistanceInSearchTo = showMyDistance;
            nearbyUser.MyShowInMapSearch = myShowInMapSearch;
            users.add(nearbyUser.ConvertToUser());
        }
        return users;
    }

    public Users ConvertToUser() {
        Users user = new Users(UserID);
        user.UserName = UserName;
        user.Age = Age;
        user.PicID = PicID;
        user.OnlineStatus = OnlineStatus;
        user.Distance = Distance;
        user.LocationLatLng = LocationLatLng;
        user.Blocked = Blocked;
        user.IBlockedHim = IBlockedHim;
        user.ShowDistanceInSearchTo = ShowDistanceInSearchTo;
        user.ShowAgeInSearchTo = ShowAgeInSearchTo;
        user.ShowInMapSearch = ShowInMapSearch;

        user.ShowMyDistanceInSearchTo = ShowMyDistanceInSearchTo;
        user.MyShowInMapSearch = MyShowInMapSearch;

        return user;
    }

    protected NearbyUser(Parcel in) {
        UserID = in.readString();
        UserName = in.readString();
        Age = in.readInt();
        PicID = in.readString();
        byte tmpOnlineStatus = in.readByte();
        OnlineStatus = tmpOnlineStatus == 0 ? null : tmpOnlineStatus == 1;
        Distance = in.readFloat();
        LocationLatLng = in.readString();
        Blocked = in.readString();
        IBlockedHim = in.readString();
        ShowDistanceInSearchTo = in.readString();
        ShowAgeInSearchTo = in.readString();
        byte tmpShowInMapSearch = in.readByte();
        ShowInMapSearch = tmpShowInMapSearch == 0 ? null : tmpShowInMapSearch == 1;
        ShowMyDistanceInSearchTo = in.readString();
        byte tmpMyShowInMapSearch = in.readByte();
        MyShowInMapSearch = tmpMyShowInMapSearch == 0 ? null : tmpMyShowInMapSearch == 1;
    }

    public static final Creator<NearbyUser> CREATOR = new Creator<NearbyUser>() {
        @Override
        public NearbyUser createFromParcel(Parcel in) {
            return new NearbyUser(in);
        }

        @Override
        public NearbyUser[] newArray(int size) {
            return new NearbyUser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UserID);
        dest.writeString(UserName);
        dest.writeInt(Age);
        dest.writeString(PicID);
        dest.writeByte((byte) (OnlineStatus == null ? 0 : OnlineStatus ? 1 : 2));
        dest.writeFloat(Distance);
        dest.writeString(LocationLatLng);
        dest.writeString(Blocked);
        dest.writeString(IBlockedHim);
        dest.writeString(ShowDistanceInSearchTo);
        dest.writeString(ShowAgeInSearchTo);
        dest.writeByte((byte) (ShowInMapSearch == null ? 0 : ShowInMapSearch ? 1 : 2));
        dest.writeString(ShowMyDistanceInSearchTo);
        dest.writeByte((byte) (MyShowInMapSearch == null ? 0 : MyShowInMapSearch ? 1 : 2));
    }
}
