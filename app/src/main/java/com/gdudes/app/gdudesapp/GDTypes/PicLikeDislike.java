package com.gdudes.app.gdudesapp.GDTypes;

import android.graphics.Bitmap;

import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.util.ArrayList;
import java.util.Date;

public class PicLikeDislike {
    public String LikeDislikeID = "";
    public String PicID = "";
    public String SenderUserID = "";
    public String SenderPicID = "";
    public String LDDateTime = "";
    public Date dLDDT;
    public String UserName = "";
    public String UserPic = "";
    public Users LDByUser = null;
    public Boolean IsDataRefreshed = false;
    public Bitmap image = null;

    public PicLikeDislike() {
    }

    public PicLikeDislike(String likeDislikeID) {
        this.LikeDislikeID = likeDislikeID;
    }

    public static ArrayList<String> GetUserIDList(ArrayList<PicLikeDislike> PicLikeDislikeList) {
        ArrayList<String> UserIDs = new ArrayList<>();
        for (int i = 0; i < PicLikeDislikeList.size(); i++) {
            UserIDs.add(PicLikeDislikeList.get(i).SenderUserID);
        }
        return StringHelper.RemoveDuplicateEntries(UserIDs);
    }

    public static ArrayList<String> GetPicIDList(ArrayList<PicLikeDislike> PicLikeDislikeList) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < PicLikeDislikeList.size(); i++) {
            String picID = PicLikeDislikeList.get(i).SenderPicID;
            if (!StringHelper.IsNullOrEmpty(picID)) {
                picIDs.add(picID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static ArrayList<String> GetPicIDListForNullImages(ArrayList<PicLikeDislike> PicLikeDislikeList) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < PicLikeDislikeList.size(); i++) {
            String picID = PicLikeDislikeList.get(i).SenderPicID;
            if (!StringHelper.IsNullOrEmpty(picID) && PicLikeDislikeList.get(i).image == null) {
                picIDs.add(picID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static void SetPics(ArrayList<GDPic> pics, ArrayList<PicLikeDislike> PicLikeDislikeList) {
        for (int i = 0; i < pics.size(); i++) {
            for (int j = 0; j < PicLikeDislikeList.size(); j++) {
                String nPicID = PicLikeDislikeList.get(j).SenderPicID;
                if (!StringHelper.IsNullOrEmpty(nPicID) && nPicID.equalsIgnoreCase(pics.get(i).PicID)) {
                    PicLikeDislikeList.get(j).image = pics.get(i).image;
                    break;
                }
            }
        }
    }

    public static void UpdateUserDetails(ArrayList<Users> users, ArrayList<PicLikeDislike> PicLikeDislikeList) {
        for (int i = 0; i < users.size(); i++) {
            for (int j = 0; j < PicLikeDislikeList.size(); j++) {
                if (PicLikeDislikeList.get(i).SenderUserID.equalsIgnoreCase(users.get(i).UserID)) {
                    PicLikeDislikeList.get(j).UserName = users.get(i).GetDecodedUserName();
                    PicLikeDislikeList.get(j).SenderPicID = users.get(i).PicID;
                    PicLikeDislikeList.get(j).LDByUser = users.get(i);
                    break;
                }
            }
        }
    }

    @Override
    public boolean equals(Object second) {
        if (this.LikeDislikeID.equalsIgnoreCase(((PicLikeDislike) second).LikeDislikeID)) {
            return true;
        }
        return false;
    }
}
