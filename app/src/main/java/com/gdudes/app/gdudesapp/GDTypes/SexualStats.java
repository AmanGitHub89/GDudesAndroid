package com.gdudes.app.gdudesapp.GDTypes;

public class SexualStats {
    public String UserID;
    public String SexualOrientation;
    public String SexualPreference;
    public String ToolSize;
    public String ToolType;
    public String Sucking;
    public String SAndM;
    public String InterestesCodes;
    public String FetishCodes;

    public SexualStats() {
        UserID = "";
        SexualOrientation = "";
        SexualPreference = "";
        ToolSize = "";
        ToolType = "";
        Sucking = "";
        SAndM = "";
        InterestesCodes = "";
        FetishCodes = "";
    }

    public SexualStats(String vUserID, String vSexualOrientation, String vSexualPreference, String vToolSize,
                       String vToolType, String vSucking, String vSAndM, String vInterestesCodes,
                       String vFetishCodes) {
        UserID = vUserID;
        SexualOrientation = vSexualOrientation;
        SexualPreference = vSexualPreference;
        ToolSize = vToolSize;
        ToolType = vToolType;
        Sucking = vSucking;
        SAndM = vSAndM;
        InterestesCodes = vInterestesCodes;
        FetishCodes = vFetishCodes;
    }
}
