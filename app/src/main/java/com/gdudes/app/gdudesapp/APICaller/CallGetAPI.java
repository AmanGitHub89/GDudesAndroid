package com.gdudes.app.gdudesapp.APICaller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.RijndaelCryptLib;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class CallGetAPI extends AsyncTask<APICallInfo, String, String> {
    private static String LogClass = "CallGetAPI";
    final APICallback callback;
    Object ExtraData;
    APIProgress APIProgress;
    Boolean CalledFromService;
    APINoNetwork apiNoNetwork;
    Boolean IsNetworkAvailable = false;
    Context mContext;

//    public CallGetAPI(APICallback callback, APIProgress apiProgress, Boolean vCalledFromService) {
//        this.callback = callback;
//        this.APIProgress = apiProgress;
//        this.CalledFromService = vCalledFromService;
//    }


    public CallGetAPI(Context context, APICallback callback, APIProgress apiProgress, Boolean vCalledFromService, APINoNetwork vapiNoNetwork) {
        this.mContext = context;
        this.callback = callback;
        this.APIProgress = apiProgress;
        this.CalledFromService = vCalledFromService;
        this.apiNoNetwork = vapiNoNetwork;
    }

    @Override
    protected void onPreExecute() {
        if (APIProgress != null) {
            APIProgress.progressDialog.show();
        }
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            if (APIProgress != null) {
                APIProgress.progressDialog.dismiss();
            }
        } catch (Exception ex) {

        }
        if (IsNetworkAvailable) {
            if (result != null) {
                result = StringHelper.AddEndingBracketsIfNeededToJson(result);
            }
            callback.onAPIComplete(result, ExtraData);
        } else {
            if (apiNoNetwork != null) {
                apiNoNetwork.onAPINoNetwork();
            }
        }
    }

    @Override
    protected String doInBackground(APICallInfo... apiCallInfo) {
        if (mContext != null && apiNoNetwork != null) {
            CheckNetworkAvailability();
            if (!IsNetworkAvailable) {
                return "";
            }
        } else {
            IsNetworkAvailable = true;
        }
        ContextConnection contextConnection = null;
        HttpURLConnection connection = null;
        BufferedReader breader = null;
        String LogTag = "MyLogTag";
        try {
            ExtraData = apiCallInfo[0].ExtraData;
            String[] UrlAndVector = apiCallInfo[0].GetFullURL();
//            URL url = new URL(UrlAndVector[0]);
//            connection = (HttpURLConnection) url.openConnection();
            contextConnection = ConnectionManager.GetNewConnection(mContext, UrlAndVector[0]);
            connection = contextConnection.connection;
            connection.setConnectTimeout(apiCallInfo[0].GetConnectTimeout());
            connection.setReadTimeout(apiCallInfo[0].GetReadTimeout());
            connection.setRequestMethod(apiCallInfo[0].CallType);

//            URL url = new URL(params[0][0] + "?" + params[0][1]);
//            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();

//            if (apiCallInfo[0].OverrideByPassedURL) {
//                breader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
//            } else {
//                breader = new BufferedReader(new InputStreamReader(stream));
//            }
            breader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = breader.readLine()) != null) {
                buffer.append(line);
            }
            String ReturnString = buffer.toString();
            if (ReturnString.startsWith("\"")) {
                ReturnString = ReturnString.substring(1, ReturnString.length());
            }
            if (ReturnString.endsWith("\"")) {
                ReturnString = ReturnString.substring(0, ReturnString.length() - 1);
            }
            if (apiCallInfo[0].OverrideByPassedURL) {
                //return ReturnString.replace("\\", "");
                return ReturnString;
            } else {
                return RijndaelCryptLib.Decrypt(ReturnString).replace("\\", "");
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " UnknownHostException.");
        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " SocketTimeoutException.");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + "IllegalArgumentException");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " MalformedURLException.");
        } catch (ConnectException ex) {
            ex.printStackTrace();
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " ConnectException.");
        } catch (IOException ex) {
            ex.printStackTrace();
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " IOException.");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " Exception.", GDLogHelper.LogLevel.EXCEPTION);
            GDLogHelper.LogException(ex);
        } finally {
//            if (connection != null) {
//                connection.disconnect();
//            }
            ConnectionManager.RemoveConnection(contextConnection);
            try {
                if (breader != null) {
                    breader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                GDLogHelper.LogException(e);
            }
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    void CheckNetworkAvailability() {
        try {
            IsNetworkAvailable = false;
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                IsNetworkAvailable = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            IsNetworkAvailable = true;
        }
    }
}