package com.gdudes.app.gdudesapp.APICaller.APICalls;

import android.content.Context;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallType;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APIRequestTypes;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.BaseAPITypes;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class APICalls {
    private List<APICallParameter> apiCallParameters = new ArrayList<APICallParameter>();
    protected APICallInfo apiCallInfo;
    protected Users LoggedInUser;
    protected Context mContext;

    public APICalls(Context context) {
        mContext = context;
        apiCallInfo = new APICallInfo(BaseAPITypes.Home, APIRequestTypes.Login, apiCallParameters, APICallType.Get,
                null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        LoggedInUser = SessionManager.GetLoggedInUser(context);
    }

    protected void AddParam(String name, String value) {
        apiCallParameters.add(new APICallParameter(name, value));
        apiCallInfo.APICallParameters = apiCallParameters;
    }

    protected Boolean IsUserLoggedIn() {
        return LoggedInUser != null && !LoggedInUser.UserID.equalsIgnoreCase("");
    }

    protected Boolean IsResultError(String result) {
        return result == null || result.equals("") || result.equals("-1");
    }
}
