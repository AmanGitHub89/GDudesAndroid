package com.gdudes.app.gdudesapp.GDTypes;

public class SocialStats {
    public String UserID;
    public String Profession;
    public String Religion;
    public String Smoking;
    public String Drinking;
    public String RelationshipStatus;
    public String Languages;

    public SocialStats() {
        UserID = "";
        Profession = "";
        Religion = "";
        Smoking = "";
        Drinking = "";
        RelationshipStatus = "";
        Languages = "";
    }

    public SocialStats(String vUserID, String vProfession, String vReligion, String vSmoking,
                       String vDrinking, String vRelationshipStatus, String vLanguages) {
        UserID = vUserID;
        Profession = vProfession;
        Religion = vReligion;
        Smoking = vSmoking;
        Drinking = vDrinking;
        RelationshipStatus = vRelationshipStatus;
        Languages = vLanguages;
    }
}
