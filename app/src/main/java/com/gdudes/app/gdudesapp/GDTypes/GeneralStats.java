package com.gdudes.app.gdudesapp.GDTypes;

public class GeneralStats {
    public String UserID;
    public int Waist;
    public String BodyType;
    public String Ethnicity;
    public String Hair;
    public String BodyHair;
    public String FacialHair;
    public String Tattoo;
    public String Piercings;

    public GeneralStats() {
        UserID = "";
        Waist = 0;
        BodyType = "";
        Ethnicity = "";
        Hair = "";
        BodyHair = "";
        FacialHair = "";
        Tattoo = "";
        Piercings = "";
    }

    public GeneralStats(String vUserID, int vWaist, String vBodyType, String vEthnicity,
                        String vHair, String vBodyHair, String vFacialHair, String vTattoo,
                        String vPiercings) {
        UserID = vUserID;
        Waist = vWaist;
        BodyType = vBodyType;
        Ethnicity = vEthnicity;
        Hair = vHair;
        BodyHair = vBodyHair;
        FacialHair = vFacialHair;
        Tattoo = vTattoo;
        Piercings = vPiercings;
    }
}
