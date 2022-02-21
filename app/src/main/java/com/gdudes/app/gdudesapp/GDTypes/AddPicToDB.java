package com.gdudes.app.gdudesapp.GDTypes;

public class AddPicToDB {
    public String PicID;
    public String UserID;
    public String PicSrc;
    public Boolean IsCompleteSrc;

    public AddPicToDB(String vPicID, String vUserID, String vPicSrc, Boolean vIsCompleteSrc) {
        this.PicID = vPicID;
        this.UserID = vUserID;
        this.PicSrc = vPicSrc;
        this.IsCompleteSrc = vIsCompleteSrc;
    }
}
