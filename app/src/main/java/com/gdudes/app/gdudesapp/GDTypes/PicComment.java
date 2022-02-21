package com.gdudes.app.gdudesapp.GDTypes;

import android.graphics.Bitmap;

import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.util.ArrayList;
import java.util.Date;

public class PicComment {
    public String CommentID;
    public String PicID;
    public String OwnerUserID;
    public String SenderUserID;
    public String Comment;
    public String LDDateTime;
    public String UserName;
    public Users CommentByUser;
    public Date dCommentDT;
    public Boolean IsDataRefreshed;
    public Bitmap image = null;

    public PicComment() {
        CommentID = "";
        PicID = "";
        OwnerUserID = "";
        SenderUserID = "";
        Comment = "";
        LDDateTime = "";
        UserName = "";
        CommentByUser = null;
        dCommentDT = new Date(0);
        IsDataRefreshed = false;
    }

    public PicComment(String vCommentID, String vPicID, String vOwnerUserID, String vSenderUserID,
                      String vComment, String vLDDateTime, String vUserName,
                      Users vCommentByUser, Boolean vIsDataRefreshed) {
        CommentID = vCommentID;
        PicID = vPicID;
        OwnerUserID = vOwnerUserID;
        SenderUserID = vSenderUserID;
        Comment = vComment;
        LDDateTime = vLDDateTime;
        UserName = vUserName;
        CommentByUser = vCommentByUser;
        dCommentDT = new Date(0);
        IsDataRefreshed = vIsDataRefreshed;
    }

    @Override
    public boolean equals(Object second) {
        if (this.CommentID.equalsIgnoreCase(((PicComment) second).CommentID)) {
            return true;
        }
        return false;
    }

    public static void SetDateForPicCommentList(ArrayList<PicComment> PicCommentsList) {
        for (int i = 0; i < PicCommentsList.size(); i++) {
            PicCommentsList.get(i).dCommentDT = GDDateTimeHelper.GetDateFromString(PicCommentsList.get(i).LDDateTime);
        }
    }

    public static void SetDataRefreshed(ArrayList<PicComment> picList, ArrayList<Users> users) {
        for (int i = 0; i < users.size(); i++) {
            for (int j = 0; j < picList.size(); j ++) {
                if (picList.get(j).SenderUserID.equalsIgnoreCase(users.get(i).UserID)) {
                    picList.get(j).IsDataRefreshed = true;
                }
            }
        }
    }


    public static ArrayList<String> GetUserIDList(ArrayList<PicComment> PicCommentsList) {
        ArrayList<String> UserIDs = new ArrayList<>();
        for (int i = 0; i < PicCommentsList.size(); i++) {
            UserIDs.add(PicCommentsList.get(i).SenderUserID);
        }
        return StringHelper.RemoveDuplicateEntries(UserIDs);
    }

    public static ArrayList<String> GetPicIDList(ArrayList<PicComment> comments) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < comments.size(); i++) {
            String picID = comments.get(i).PicID;
            if (!StringHelper.IsNullOrEmpty(picID)) {
                picIDs.add(comments.get(i).PicID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static ArrayList<String> GetPicIDListForNullImages(ArrayList<PicComment> comments) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < comments.size(); i++) {
            String picID = comments.get(i).PicID;
            if (!StringHelper.IsNullOrEmpty(picID) && comments.get(i).image == null) {
                picIDs.add(comments.get(i).PicID);
            }
        }
        return StringHelper.RemoveDuplicateEntries(picIDs);
    }

    public static void SetPicsToComments(ArrayList<GDPic> pics, ArrayList<PicComment> comments) {
        for (int i = 0; i < pics.size(); i++) {
            for (int j = 0; j < comments.size(); j++) {
                String nPicID = comments.get(j).PicID;
                if (!StringHelper.IsNullOrEmpty(nPicID) && !StringHelper.IsNullOrEmpty(pics.get(i).PicID)
                        && nPicID.equalsIgnoreCase(pics.get(i).PicID)) {
                    comments.get(j).image = pics.get(i).image;
                }
            }
        }
    }

    public static void UpdateUserDetails(ArrayList<Users> users, ArrayList<PicComment> comments) {
        for (int i = 0; i < users.size(); i++) {
            UpdateUserDetails(users.get(i), comments);
        }
    }

    public static void UpdateUserDetails(Users user, ArrayList<PicComment> comments) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).SenderUserID.equalsIgnoreCase(user.UserID)) {
                comments.get(i).UserName = user.GetDecodedUserName();
                comments.get(i).PicID = user.PicID;
                comments.get(i).CommentByUser = user;
            }
        }
    }
}
