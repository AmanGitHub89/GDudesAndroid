package com.gdudes.app.gdudesapp.GDTypes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.util.ArrayList;

public class GDPic implements Parcelable {
    public String UserID;
    public String PicID;
    public Boolean VisibleInProfile;
    public Boolean VisibleOnlyToFriends;
    public String Caption;
    public String Category;
    public Boolean IsCategorized;
    public long DragItemID;
    public Boolean IsDragItemSelected;
    public int PicOrder;
    public String PicThumbnail = "";
    public String DirectPicAttachedFilePath = "";
    public Boolean IsFullPic = false;
    public Bitmap image;

    public GDPic() {
        this.PicID = "";
        this.VisibleInProfile = false;
        this.VisibleOnlyToFriends = false;
        this.Caption = "";
        this.Category = "";
        this.IsCategorized = false;
        this.UserID = "";
        this.PicOrder = -1;
    }

    public GDPic(String vPicID) {
        this.PicID = vPicID;
        this.VisibleInProfile = false;
        this.VisibleOnlyToFriends = false;
        this.Caption = "";
        this.Category = "";
        this.IsCategorized = false;
        this.UserID = "";
        this.PicOrder = -1;
    }

    public GDPic(String vPicID, String vUserID, Boolean vVisibleInProfile,
                 String vCaption, String vCategory, Boolean vIsCategorized) {
        this.PicID = vPicID;
        this.UserID = vUserID;
        this.VisibleInProfile = vVisibleInProfile;
        this.Caption = vCaption;
        this.Category = vCategory;
        this.IsCategorized = vIsCategorized;
        this.PicOrder = -1;
    }

    public static ArrayList<String> GetPicIDListForNullImages(ArrayList<GDPic> pics) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < pics.size(); i++) {
            String picID = pics.get(i).PicID;
            if (!StringHelper.IsNullOrEmpty(picID) && pics.get(i).image == null) {
                picIDs.add(pics.get(i).PicID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static void SetPics(ArrayList<GDPic> fromList, ArrayList<GDPic> toList) {
        for (int i = 0; i < fromList.size(); i++) {
            for (int j = 0; j < toList.size(); j++) {
                String toPicID = toList.get(j).PicID;
                if (!StringHelper.IsNullOrEmpty(toPicID) && toPicID.equalsIgnoreCase(fromList.get(i).PicID)) {
                    toList.get(j).image = fromList.get(i).image;
                }
            }
        }
    }

    @Override
    public boolean equals(Object second) {
        if (this.PicID.equalsIgnoreCase(((GDPic) second).PicID)) {
            return true;
        }
        return false;
    }

    protected GDPic(Parcel in) {
        UserID = in.readString();
        PicID = in.readString();
        byte VisibleInProfileVal = in.readByte();
        VisibleInProfile = VisibleInProfileVal == 0x02 ? null : VisibleInProfileVal != 0x00;
        byte VisibleOnlyToFriendsVal = in.readByte();
        VisibleOnlyToFriends = VisibleOnlyToFriendsVal == 0x02 ? null : VisibleOnlyToFriendsVal != 0x00;
        Caption = in.readString();
        Category = in.readString();
        DirectPicAttachedFilePath = in.readString();
        byte IsCategorizedVal = in.readByte();
        IsCategorized = IsCategorizedVal == 0x02 ? null : IsCategorizedVal != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UserID);
        dest.writeString(PicID);
        if (VisibleInProfile == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (VisibleInProfile ? 0x01 : 0x00));
        }
        if (VisibleOnlyToFriends == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (VisibleOnlyToFriends ? 0x01 : 0x00));
        }
        dest.writeString(Caption);
        dest.writeString(Category);
        dest.writeString(DirectPicAttachedFilePath);
        if (IsCategorized == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (IsCategorized ? 0x01 : 0x00));
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GDPic> CREATOR = new Parcelable.Creator<GDPic>() {
        @Override
        public GDPic createFromParcel(Parcel in) {
            return new GDPic(in);
        }

        @Override
        public GDPic[] newArray(int size) {
            return new GDPic[size];
        }
    };
}
