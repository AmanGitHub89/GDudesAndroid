package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.GDTypes.GDIncrementalStats;
import com.gdudes.app.gdudesapp.GDTypes.GDSKeyValue;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MasterDataHelper {
    private static SharedPreferences sharedPreferences_MasterData;
    private static SharedPreferences.Editor editor_MasterData;
    private static SharedPreferences sharedPreferences_UserStatsMasterData;
    private static SharedPreferences.Editor editor_UserStatsMasterData;
    private static Context context;
    private static int PRIVATE_MODE = 0;
    private static Boolean InitComplete = false;
    private static final String PREFNAME_MASTERDATA = "PrefMasterData";
    private static final String PREFNAME_USERSTATSMASTERDATA = "PrefUserStatsMasterData";
    private static final String KEYNAME_USERSTATSMASTERDATA = "GDUserStatsMasterData";
    private static final String KEYNAME_LastDownloadedDateTime = "LastDownloadedDateTime";

    private static String GDUserStatsMasterData_Json = "";

    public static List<GDIncrementalStats> MasterUserHeight;
    public static List<GDIncrementalStats> MasterUserWeight;
    public static List<GDIncrementalStats> MasterUserWaist;
    public static List<GDSKeyValue> MasterBodyType;
    public static List<GDSKeyValue> MasterEthnicity;
    public static List<GDSKeyValue> MasterHairType;
    public static List<GDSKeyValue> MasterBodyHairType;
    public static List<GDSKeyValue> MasterFacialHairType;
    public static List<GDSKeyValue> MasterTattoo;
    public static List<GDSKeyValue> MasterPiercings;
    public static List<GDSKeyValue> MasterSexualOrientation;
    public static List<GDSKeyValue> MasterToolSize;
    public static List<GDSKeyValue> MasterToolType;
    public static List<GDSKeyValue> MasterSexualPreference;
    public static List<GDSKeyValue> MasterSucking;
    public static List<GDSKeyValue> MasterSAndM;
    public static List<GDSKeyValue> MasterInterests;
    public static List<GDSKeyValue> MasterFetishes;
    public static List<GDSKeyValue> MasterProfession;
    public static List<GDSKeyValue> MasterReligion;
    public static List<GDSKeyValue> MasterSmoking;
    public static List<GDSKeyValue> MasterDrinking;
    public static List<GDSKeyValue> MasterRelationshipStatus;
    public static List<GDSKeyValue> MasterLanguages;
    public static List<GDSKeyValue> AppConstants;

    public final static String TABLE_MasterUserAge = "TABLE_MasterUserAge";
    public final static String TABLE_MasterUserHeight = "MasterUserHeight";
    public final static String TABLE_MasterUserWeight = "MasterUserWeight";
    public final static String TABLE_MasterUserWaist = "MasterUserWaist";
    public final static String TABLE_MasterBodyType = "MasterBodyType";
    public final static String TABLE_MasterEthnicity = "MasterEthnicity";
    public final static String TABLE_MasterHairType = "MasterHairType";
    public final static String TABLE_MasterBodyHairType = "MasterBodyHairType";
    public final static String TABLE_MasterFacialHairType = "MasterFacialHairType";
    public final static String TABLE_MasterTattoo = "MasterTattoo";
    public final static String TABLE_MasterPiercings = "MasterPiercings";
    public final static String TABLE_MasterSexualOrientation = "MasterSexualOrientation";
    public final static String TABLE_MasterToolSize = "MasterToolSize";
    public final static String TABLE_MasterToolType = "MasterToolType";
    public final static String TABLE_MasterSexualPreference = "MasterSexualPreference";
    public final static String TABLE_MasterSucking = "MasterSucking";
    public final static String TABLE_MasterSAndM = "MasterSAndM";
    public final static String TABLE_MasterInterests = "MasterInterests";
    public final static String TABLE_MasterFetishes = "MasterFetishes";
    public final static String TABLE_MasterProfession = "MasterProfession";
    public final static String TABLE_MasterReligion = "MasterReligion";
    public final static String TABLE_MasterSmoking = "MasterSmoking";
    public final static String TABLE_MasterDrinking = "MasterDrinking";
    public final static String TABLE_MasterRelationshipStatus = "MasterRelationshipStatus";
    public final static String TABLE_MasterLanguages = "MasterLanguages";
    public final static String TABLE_AppConstants = "AppConstants";

    public enum GDAppConstants {
        ULT
    }

    public static void InitMasterDataHelper(Context acontext) {
        context = acontext;
        if (InitComplete) {
            return;
        }
        sharedPreferences_MasterData = context.getSharedPreferences(PREFNAME_MASTERDATA, PRIVATE_MODE);
        sharedPreferences_UserStatsMasterData = context.getSharedPreferences(PREFNAME_USERSTATSMASTERDATA, PRIVATE_MODE);
        editor_MasterData = sharedPreferences_MasterData.edit();
        editor_UserStatsMasterData = sharedPreferences_UserStatsMasterData.edit();
        SetDataToObjects(acontext);
    }

    private static void SetDataToObjects(Context context) {
        try {
            if (GDUserStatsMasterData_Json == null || GDUserStatsMasterData_Json.equals("")) {
                String UserStatsMasterDataJson = sharedPreferences_UserStatsMasterData.getString(KEYNAME_USERSTATSMASTERDATA, "");
                if (UserStatsMasterDataJson.equals("")) {
                    GetDataFromServer();
                } else {
                    GDUserStatsMasterData_Json = UserStatsMasterDataJson;
                    SetUserStatsMasterDataToObjects(GDUserStatsMasterData_Json);
                    CheckForReload();
                }
            } else {
                SetUserStatsMasterDataToObjects(GDUserStatsMasterData_Json);
                CheckForReload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private static void CheckForReload() {
        if (TimeElapsedForReload()) {
            GetDataFromServer();
        }
    }

    private static void GetDataFromServer() {
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        APICallInfo apiCallInfo = new APICallInfo("Login", "UserStatsMasterData", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.LONG);
        GDGenericHelper.executeAsyncAPITask(context, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result == null || result.equals("") || result.equals("-1")) {
                        return;
                    }
                    GDUserStatsMasterData_Json = result;
                    editor_UserStatsMasterData.putString(KEYNAME_USERSTATSMASTERDATA, result);
                    editor_UserStatsMasterData.commit();
                    SetUserStatsMasterDataToObjects(GDUserStatsMasterData_Json);
                    SetLastDownloadedDateTime(GDDateTimeHelper.GetCurrentDateTimeAsString(false));
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                }
            }
        }, null);
    }

    private static void SetUserStatsMasterDataToObjects(String JsonData) {
        try {
            JSONObject jsonObject = new JSONObject(JsonData);
            String sMasterUserHeight = jsonObject.getString("MasterUserHeight");
            MasterUserHeight = new GsonBuilder().create().fromJson(sMasterUserHeight, new TypeToken<List<GDIncrementalStats>>() {
            }.getType());
            String sMasterUserWeight = jsonObject.getString("MasterUserWeight");
            MasterUserWeight = new GsonBuilder().create().fromJson(sMasterUserWeight, new TypeToken<List<GDIncrementalStats>>() {
            }.getType());
            String sMasterUserWaist = jsonObject.getString("MasterUserWaist");
            MasterUserWaist = new GsonBuilder().create().fromJson(sMasterUserWaist, new TypeToken<List<GDIncrementalStats>>() {
            }.getType());

            String sMasterBodyType = jsonObject.getString("MasterBodyType");
            MasterBodyType = new GsonBuilder().create().fromJson(sMasterBodyType, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterEthnicity = jsonObject.getString("MasterEthnicity");
            MasterEthnicity = new GsonBuilder().create().fromJson(sMasterEthnicity, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterHairType = jsonObject.getString("MasterHairType");
            MasterHairType = new GsonBuilder().create().fromJson(sMasterHairType, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterBodyHairType = jsonObject.getString("MasterBodyHairType");
            MasterBodyHairType = new GsonBuilder().create().fromJson(sMasterBodyHairType, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterFacialHairType = jsonObject.getString("MasterFacialHairType");
            MasterFacialHairType = new GsonBuilder().create().fromJson(sMasterFacialHairType, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterTattoo = jsonObject.getString("MasterTattoo");
            MasterTattoo = new GsonBuilder().create().fromJson(sMasterTattoo, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterPiercings = jsonObject.getString("MasterPiercings");
            MasterPiercings = new GsonBuilder().create().fromJson(sMasterPiercings, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterSexualOrientation = jsonObject.getString("MasterSexualOrientation");
            MasterSexualOrientation = new GsonBuilder().create().fromJson(sMasterSexualOrientation, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterToolSize = jsonObject.getString("MasterToolSize");
            MasterToolSize = new GsonBuilder().create().fromJson(sMasterToolSize, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterToolType = jsonObject.getString("MasterToolType");
            MasterToolType = new GsonBuilder().create().fromJson(sMasterToolType, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterSexualPreference = jsonObject.getString("MasterSexualPreference");
            MasterSexualPreference = new GsonBuilder().create().fromJson(sMasterSexualPreference, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterSucking = jsonObject.getString("MasterSucking");
            MasterSucking = new GsonBuilder().create().fromJson(sMasterSucking, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterSAndM = jsonObject.getString("MasterSAndM");
            MasterSAndM = new GsonBuilder().create().fromJson(sMasterSAndM, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterInterests = jsonObject.getString("MasterInterests");
            MasterInterests = new GsonBuilder().create().fromJson(sMasterInterests, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterFetishes = jsonObject.getString("MasterFetishes");
            MasterFetishes = new GsonBuilder().create().fromJson(sMasterFetishes, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterProfession = jsonObject.getString("MasterProfession");
            MasterProfession = new GsonBuilder().create().fromJson(sMasterProfession, new TypeToken<List<GDSKeyValue>>() {
            }.getType());

            String sMasterReligion = jsonObject.getString("MasterReligion");
            MasterReligion = new GsonBuilder().create().fromJson(sMasterReligion, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterSmoking = jsonObject.getString("MasterSmoking");
            MasterSmoking = new GsonBuilder().create().fromJson(sMasterSmoking, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterDrinking = jsonObject.getString("MasterDrinking");
            MasterDrinking = new GsonBuilder().create().fromJson(sMasterDrinking, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterRelationshipStatus = jsonObject.getString("MasterRelationshipStatus");
            MasterRelationshipStatus = new GsonBuilder().create().fromJson(sMasterRelationshipStatus, new TypeToken<List<GDSKeyValue>>() {
            }.getType());
            String sMasterLanguages = jsonObject.getString("MasterLanguages");
            MasterLanguages = new GsonBuilder().create().fromJson(sMasterLanguages, new TypeToken<List<GDSKeyValue>>() {
            }.getType());

            try {
                String sAppConstants = jsonObject.getString("AppConstants");
                AppConstants = new GsonBuilder().create().fromJson(sAppConstants, new TypeToken<List<GDSKeyValue>>() {
                }.getType());
            } catch (Exception ex) {
                GDLogHelper.Log("MasterDataHelper", "SetUserStatsMasterDataToObjects-AppConstants", JsonData, GDLogHelper.LogLevel.EXCEPTION);
            }
            InitComplete = true;
        } catch (Exception ex) {
            GDUserStatsMasterData_Json = "";
            editor_UserStatsMasterData.putString(KEYNAME_USERSTATSMASTERDATA, "");
            editor_UserStatsMasterData.commit();
            ex.printStackTrace();
            if (JsonData != null && !JsonData.equals("-1")) {
                GDLogHelper.Log("MasterDataHelper", "SetUserStatsMasterDataToObjects", JsonData, GDLogHelper.LogLevel.EXCEPTION);
                GDLogHelper.LogException(ex);
            }
        }
    }

    public static void RemoveMasterData() {
        try {
            if (editor_MasterData != null) {
                editor_MasterData.clear();
                editor_MasterData.commit();
            }
            if (editor_UserStatsMasterData != null) {
                editor_UserStatsMasterData.clear();
                editor_UserStatsMasterData.commit();
            }
            GDUserStatsMasterData_Json = "";
            InitComplete = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static String GetUserStatsDesc(final String TableName, String Key) {
        String Value = "";
        GDSKeyValue MatchedVal = null;
        try {
            switch (TableName) {
                case TABLE_MasterBodyType:
                    MatchedVal = GetMatchingGDSKeyValue(MasterBodyType, Key);
                    break;
                case TABLE_MasterEthnicity:
                    MatchedVal = GetMatchingGDSKeyValue(MasterEthnicity, Key);
                    break;
                case TABLE_MasterHairType:
                    MatchedVal = GetMatchingGDSKeyValue(MasterHairType, Key);
                    break;
                case TABLE_MasterBodyHairType:
                    MatchedVal = GetMatchingGDSKeyValue(MasterBodyHairType, Key);
                    break;
                case TABLE_MasterFacialHairType:
                    MatchedVal = GetMatchingGDSKeyValue(MasterFacialHairType, Key);
                    break;
                case TABLE_MasterTattoo:
                    MatchedVal = GetMatchingGDSKeyValue(MasterTattoo, Key);
                    break;
                case TABLE_MasterPiercings:
                    MatchedVal = GetMatchingGDSKeyValue(MasterPiercings, Key);
                    break;
                case TABLE_MasterSexualOrientation:
                    MatchedVal = GetMatchingGDSKeyValue(MasterSexualOrientation, Key);
                    break;
                case TABLE_MasterToolSize:
                    MatchedVal = GetMatchingGDSKeyValue(MasterToolSize, Key);
                    break;
                case TABLE_MasterToolType:
                    MatchedVal = GetMatchingGDSKeyValue(MasterToolType, Key);
                    break;
                case TABLE_MasterSexualPreference:
                    MatchedVal = GetMatchingGDSKeyValue(MasterSexualPreference, Key);
                    break;
                case TABLE_MasterSucking:
                    MatchedVal = GetMatchingGDSKeyValue(MasterSucking, Key);
                    break;
                case TABLE_MasterSAndM:
                    MatchedVal = GetMatchingGDSKeyValue(MasterSAndM, Key);
                    break;
                case TABLE_MasterInterests:
                    MatchedVal = GetMatchingGDSKeyValue(MasterInterests, Key);
                    break;
                case TABLE_MasterFetishes:
                    MatchedVal = GetMatchingGDSKeyValue(MasterFetishes, Key);
                    break;
                case TABLE_MasterProfession:
                    MatchedVal = GetMatchingGDSKeyValue(MasterProfession, Key);
                    break;
                case TABLE_MasterReligion:
                    MatchedVal = GetMatchingGDSKeyValue(MasterReligion, Key);
                    break;
                case TABLE_MasterSmoking:
                    MatchedVal = GetMatchingGDSKeyValue(MasterSmoking, Key);
                    break;
                case TABLE_MasterDrinking:
                    MatchedVal = GetMatchingGDSKeyValue(MasterDrinking, Key);
                    break;
                case TABLE_MasterRelationshipStatus:
                    MatchedVal = GetMatchingGDSKeyValue(MasterRelationshipStatus, Key);
                    break;
                case TABLE_MasterLanguages:
                    MatchedVal = GetMatchingGDSKeyValue(MasterLanguages, Key);
                    break;
                case TABLE_AppConstants:
                    MatchedVal = GetMatchingGDSKeyValue(AppConstants, Key);
                    break;
            }
            if (MatchedVal != null) {
                Value = MatchedVal.GDSValue;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return Value;
    }

    public static String GetUserStatsDesc(final String TableName, List<String> Keys) {
        String Descriptions = "";
        GDSKeyValue MatchedVal = null;
        try {
            for (int i = 0; i < Keys.size(); i++) {
                MatchedVal = null;
                switch (TableName) {
                    case TABLE_MasterInterests:
                        MatchedVal = GetMatchingGDSKeyValue(MasterInterests, Keys.get(i));
                        break;
                    case TABLE_MasterFetishes:
                        MatchedVal = GetMatchingGDSKeyValue(MasterFetishes, Keys.get(i));
                        break;
                    case TABLE_MasterLanguages:
                        MatchedVal = GetMatchingGDSKeyValue(MasterLanguages, Keys.get(i));
                        break;
                }
                if (MatchedVal != null) {
                    Descriptions = Descriptions + (Descriptions.equals("") ? MatchedVal.GDSValue : ", " + MatchedVal.GDSValue);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return Descriptions;
    }

    private static GDSKeyValue GetMatchingGDSKeyValue(List<GDSKeyValue> DataStore, String Key) {
        try {
            for (int i = 0; i < DataStore.size(); i++) {
                if (DataStore.get(i).GDSKey.equalsIgnoreCase(Key)) {
                    return DataStore.get(i);
                }
            }
        } catch (NullPointerException e) {
            InitComplete = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return null;
    }

    public static List<GDSKeyValue> GetDataStoreForIncremental(final String TableName, Boolean ConvertToImperial) {
        List<GDSKeyValue> DataStore = new ArrayList<>();
        try {
            int MinValue = 0;
            int MaxValue = 0;
            int IncrementValue = 0;
            String Metric = "";
            String Imperial = "";
            String ImpUnit = "";
            switch (TableName) {
                case TABLE_MasterUserHeight:
                    MinValue = MasterUserHeight.get(0).MinValue;
                    MaxValue = MasterUserHeight.get(0).MaxValue;
                    IncrementValue = MasterUserHeight.get(0).Increment;
                    Metric = MasterUserHeight.get(0).Metric;
                    Imperial = MasterUserHeight.get(0).Imperial;
                    ImpUnit = "H";
                    break;
                case TABLE_MasterUserWeight:
                    MinValue = MasterUserWeight.get(0).MinValue;
                    MaxValue = MasterUserWeight.get(0).MaxValue;
                    IncrementValue = MasterUserWeight.get(0).Increment;
                    Metric = MasterUserWeight.get(0).Metric;
                    Imperial = MasterUserWeight.get(0).Imperial;
                    ImpUnit = "W";
                    break;
                case TABLE_MasterUserWaist:
                    MinValue = MasterUserWaist.get(0).MinValue;
                    MaxValue = MasterUserWaist.get(0).MaxValue;
                    IncrementValue = MasterUserWaist.get(0).Increment;
                    Metric = MasterUserWaist.get(0).Metric;
                    Imperial = MasterUserWaist.get(0).Imperial;
                    break;
                case TABLE_MasterUserAge:
                    MinValue = 18;
                    MaxValue = 100;
                    IncrementValue = 1;
                    Metric = "";
                    Imperial = "";
                    break;
            }
            for (int i = MinValue; i < MaxValue; i = i + IncrementValue) {
                if (ConvertToImperial) {
                    if (ImpUnit.equals("H")) {
                        DataStore.add(new GDSKeyValue(Integer.toString(i), GDUnitHelper.Height_MTI(i, false) + " " + Imperial));
                    } else {
                        DataStore.add(new GDSKeyValue(Integer.toString(i), GDUnitHelper.Weight_MTI(i, false) + " " + Imperial));
                    }
                } else {
                    DataStore.add(new GDSKeyValue(Integer.toString(i), Integer.toString(i) + " " + Metric));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return DataStore;
    }

    private static void SetLastDownloadedDateTime(String LastDownloadedDateTime) {
        try {
            editor_UserStatsMasterData.putString(KEYNAME_LastDownloadedDateTime, LastDownloadedDateTime);
            editor_UserStatsMasterData.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private static String GetLastDownloadedDateTime() {
        try {
            if (sharedPreferences_UserStatsMasterData == null) {
                return "";
            }
            return sharedPreferences_UserStatsMasterData.getString(KEYNAME_LastDownloadedDateTime, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    private static Boolean TimeElapsedForReload() {
        Boolean TimeElapsed = false;
        try {
            String sLastDT = GetLastDownloadedDateTime();
            if (sLastDT == null || sLastDT.equals("")) {
                return true;
            }
            Date LastInteractionDate = GDDateTimeHelper.GetDateFromString(sLastDT);
            Date TimeNow = new Date();
            long hours = (TimeNow.getTime() - LastInteractionDate.getTime()) / 1000 / 60 / 60;
            if (hours > 120) {      //5 days
                TimeElapsed = true;
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return TimeElapsed;
    }
}
