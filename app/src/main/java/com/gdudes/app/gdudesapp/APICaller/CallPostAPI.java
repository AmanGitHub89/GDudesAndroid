package com.gdudes.app.gdudesapp.APICaller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.RijndaelCryptLib;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class CallPostAPI extends AsyncTask<APICallInfo, Integer, String> {
    private static String LogClass = "CallPostAPI";
    final APICallback callback;
    final APIProgressUpdate progressUpdate;
    Object ExtraData;
    APIProgress APIProgress;
    Boolean CalledFromService;
    APINoNetwork apiNoNetwork;
    Boolean IsNetworkAvailable = false;
    Context mContext;

//    public CallPostAPI(APICallback callback, APIProgress apiProgress, Boolean vCalledFromService) {
//        this.callback = callback;
//        this.progressUpdate = null;
//        this.APIProgress = apiProgress;
//        this.CalledFromService = vCalledFromService;
//    }

    public CallPostAPI(Context context, APICallback callback, APIProgress apiProgress, Boolean vCalledFromService, APINoNetwork vapiNoNetwork) {
        this.mContext = context;
        this.callback = callback;
        this.progressUpdate = null;
        this.APIProgress = apiProgress;
        this.CalledFromService = vCalledFromService;
        this.apiNoNetwork = vapiNoNetwork;
    }

//    public CallPostAPI(APICallback callback, APIProgress apiProgress, APIProgressUpdate apiProgressUpdate, Boolean vCalledFromService) {
//        this.callback = callback;
//        this.progressUpdate = apiProgressUpdate;
//        this.APIProgress = apiProgress;
//        this.CalledFromService = vCalledFromService;
//    }

    public CallPostAPI(Context context, APICallback callback, APIProgress apiProgress, APIProgressUpdate apiProgressUpdate, Boolean vCalledFromService, APINoNetwork vapiNoNetwork) {
        this.mContext = context;
        this.callback = callback;
        this.progressUpdate = apiProgressUpdate;
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
        DataOutputStream dataOutputStream = null;
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
            if (apiCallInfo[0].CallType == "POST") {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestProperty("Content-Type", "application/json");
                //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            connection.connect();

            dataOutputStream = new DataOutputStream(connection.getOutputStream());
            Gson gson = new Gson();
            String jsonDataToPass = gson.toJson(apiCallInfo[0].PostJsonObject);

            if (jsonDataToPass.length() > 1024000) {
                GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " Sending data length " + Integer.toString(jsonDataToPass.length()));
            }

            //Encrypt data being sent
            jsonDataToPass = RijndaelCryptLib.Encrypt(jsonDataToPass);
            PostData postData = new PostData(jsonDataToPass);
            jsonDataToPass = gson.toJson(postData);

            int bufferSize = 512;
            int bytesRead, progress = 0, bytesAvailable;
            byte[] UploadBuffer = new byte[bufferSize];
            byte[] TotalBytes = jsonDataToPass.getBytes(StandardCharsets.UTF_8);
            InputStream UploadDataStream = new ByteArrayInputStream(TotalBytes);
            bytesRead = UploadDataStream.read(UploadBuffer, 0, bufferSize);
            while (bytesRead > 0) {
                progress += bytesRead;
                dataOutputStream.write(UploadBuffer, 0, bytesRead);
                bytesAvailable = UploadDataStream.available();
                publishProgress((int) ((progress * 100) / (TotalBytes.length)));
                bufferSize = Math.min(bytesAvailable, bufferSize);
                UploadBuffer = new byte[bufferSize];
                bytesRead = UploadDataStream.read(UploadBuffer, 0, bufferSize);
            }
            UploadDataStream.close();
            publishProgress(100);
            dataOutputStream.flush();
            dataOutputStream.close();

//            dataOutputStream.writeBytes(jsonDataToPass);
//            dataOutputStream.flush();
//            dataOutputStream.close();

            InputStream stream = connection.getInputStream();
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
            return RijndaelCryptLib.Decrypt(ReturnString).replace("\\", "");
        } catch (UnknownHostException ex) {
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " UnknownHostException.");
        } catch (SocketTimeoutException ex) {
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " SocketTimeoutException.");
        } catch (IllegalArgumentException ex) {
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + "IllegalArgumentException");
        } catch (MalformedURLException e) {
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " MalformedURLException.");
        } catch (ConnectException e) {
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " ConnectException.");
        } catch (IOException e) {
            //GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " IOException.");
        } catch (Exception e) {
            e.printStackTrace();
            GDLogHelper.Log(LogClass, "doInBackground", apiCallInfo[0].GetCallInfoLog() + " Exception.", GDLogHelper.LogLevel.EXCEPTION);
            GDLogHelper.LogException(e);
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

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (progressUpdate != null) {
            progressUpdate.onAPIProgressUpdate(values[0]);
        }
    }

    void CheckNetworkAvailability() {
        IsNetworkAvailable = false;
        try {
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

    class PostData {
        String data;

        public PostData(String vdata) {
            data = vdata;
        }
    }
}
