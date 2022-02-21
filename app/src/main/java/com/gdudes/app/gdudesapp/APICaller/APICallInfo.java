package com.gdudes.app.gdudesapp.APICaller;

import android.app.ProgressDialog;

import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.RijndaelCryptLib;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;

import java.util.ArrayList;
import java.util.List;

public class APICallInfo {
    //private static String APIURL = "https://gdudes.com/api/";
    private static String APIURL = "http://192.168.0.101/api/";

    public enum APITimeouts {
        SHORT,
        MEDIUM,
        SEMILONG,
        LONG
    }


    public ProgressDialog progressDialog;
    public String BaseAPIType;
    public String APIReqType;
    public List<APICallParameter> APICallParameters = new ArrayList<>();
    public String CallType;
    public Object PostJsonObject;
    public Object ExtraData;
    public Boolean OverrideByPassedURL;
    public APIProgress apiProgress;
    public Boolean CalledFromService = false;
    public APITimeouts apiTimeout = APITimeouts.LONG;

    public APICallInfo(String pBaseAPIType, String pAPIReqType, List<APICallParameter> pAPICallParameters,
                       String pCallType, Object pPostJsonObject, Object pExtraData, Boolean pOverrideByPassedURL,
                       APIProgress vAPIProgress, APITimeouts vapiTimeout) {
        this.BaseAPIType = pBaseAPIType;
        this.APIReqType = pAPIReqType;
        this.APICallParameters = pAPICallParameters;
        this.CallType = pCallType;
        this.PostJsonObject = pPostJsonObject;
        this.ExtraData = pExtraData;
        this.OverrideByPassedURL = pOverrideByPassedURL;
        this.apiProgress = vAPIProgress;
        this.apiTimeout = vapiTimeout;
    }

    public int GetConnectTimeout() {
        if (apiTimeout == APITimeouts.SHORT) {
            return 3000;
        } else if (apiTimeout == APITimeouts.MEDIUM) {
            return 5000;
        } else if (apiTimeout == APITimeouts.SEMILONG) {
            return 5000;
        } else if (apiTimeout == APITimeouts.LONG) {
            return 8000;
        }
        return 8000;
    }

    public int GetReadTimeout() {
        if (apiTimeout == APITimeouts.SHORT) {
            return 5000;
        } else if (apiTimeout == APITimeouts.MEDIUM) {
            return 11000;
        } else if (apiTimeout == APITimeouts.SEMILONG) {
            return 20000;
        } else if (apiTimeout == APITimeouts.LONG) {
            return 70000;
        }
        return 8000;
    }

    public String[] GetFullURL() {
        String FullURL = APIURL;
        String[] returndata = new String[2];
        try {
            if (OverrideByPassedURL) {
                FullURL = BaseAPIType + "?";
            } else {
                FullURL += this.BaseAPIType + "?";
                FullURL += "ReqType=" + StringEncoderHelper.encodeURIComponent(RijndaelCryptLib.Encrypt(this.APIReqType));
                FullURL += "&GDudesK=" + "v47abv";
            }
            if (this.APICallParameters != null) {
                for (int i = 0; i < this.APICallParameters.size(); i++) {
                    if (OverrideByPassedURL) {
                        FullURL += ((i == 0) ? "" : "&") + this.APICallParameters.get(i).Name + "=" + this.APICallParameters.get(i).Value;
                    } else {
                        FullURL += "&" + this.APICallParameters.get(i).Name + "=" + StringEncoderHelper.encodeURIComponent(RijndaelCryptLib.Encrypt(this.APICallParameters.get(i).Value));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        returndata[0] = FullURL;
        returndata[1] = "v47abv";
        return returndata;
    }

    public String GetCallInfoLog() {
        try {
            if (OverrideByPassedURL) {
                return BaseAPIType;
            } else {
                return this.BaseAPIType + ":" + this.APIReqType;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }
}
