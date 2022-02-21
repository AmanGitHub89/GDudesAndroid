package com.gdudes.app.gdudesapp.GDTypes;

public class UplImage {
    public String UserID;
    public String ClubID;
    public String PicID;
    public String Pic;
    public String Caption;
    public String PicFolderID;
    public String ImageSize;
    public Boolean IsPublicPic;

    public UplImage() {
        this.UserID = "";
        this.ClubID = "";
        this.PicID = "";
        this.Pic = "";
        this.Caption = "";
        this.PicFolderID = "";
        this.ImageSize = "";
        this.IsPublicPic = true;
    }

    public UplImage(String vUserID, String vClubID, String vPicID, String vPic, String vCaption, String vPicFolderID,
                    String vImageSize, Boolean vIsPublicPic) {
        this.UserID = vUserID;
        this.ClubID = vClubID;
        this.PicID = vPicID;
        this.Pic = vPic;
        this.Caption = vCaption;
        this.PicFolderID = vPicFolderID;
        this.ImageSize = vImageSize;
        this.IsPublicPic = vIsPublicPic;
    }
}
