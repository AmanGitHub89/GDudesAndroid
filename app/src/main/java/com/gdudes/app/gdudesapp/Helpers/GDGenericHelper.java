package com.gdudes.app.gdudesapp.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgressUpdate;
import com.gdudes.app.gdudesapp.APICaller.CallGetAPI;
import com.gdudes.app.gdudesapp.APICaller.CallPostAPI;
import com.gdudes.app.gdudesapp.BillingUtil.GetPremiumActivity;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Profile.IceBreakerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GDGenericHelper {

    public static Boolean IsUserPremium(Context context) {
        Users user = SessionManager.GetLoggedInUser(context);
        if (user != null && user.IsPremium != null) {
            return user.IsPremium;
        }
        return false;
    }


    public static void executeAsyncAPITask(Context context, APICallInfo apiCallInfo, APICallback callback, APINoNetwork apiNoNetwork) {
        new CallGetAPI(context, callback, apiCallInfo.apiProgress, apiCallInfo.CalledFromService, apiNoNetwork).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, apiCallInfo);
    }

    public static void executeAsyncPOSTAPITask(Context context, APICallInfo apiCallInfo, APICallback callback, APINoNetwork apiNoNetwork) {
        new CallPostAPI(context, callback, apiCallInfo.apiProgress, apiCallInfo.CalledFromService, apiNoNetwork).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, apiCallInfo);
    }

    public static void executeAsyncPOSTAPITask(Context context, APICallInfo apiCallInfo, APICallback callback, APIProgressUpdate progressUpdate, APINoNetwork apiNoNetwork) {
        new CallPostAPI(context, callback, apiCallInfo.apiProgress, progressUpdate, apiCallInfo.CalledFromService, apiNoNetwork).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, apiCallInfo);
    }


    public static String GetNewGUID() {
        return UUID.randomUUID().toString();
    }

    public static <T> ArrayList<T> RemoveDuplicate(ArrayList<T> list) {
        ArrayList<T> uniqueList = new ArrayList<>();
        for (T item : list) {
            if (!uniqueList.contains(item)) {
                uniqueList.add(item);
            }
        }
        return uniqueList;
    }


    public static int GetIceBreakResourceFromCode(final String ICCode) {
        int IceBreakResource = R.drawable.hi1;
        switch (ICCode) {
            case "HI1":
                IceBreakResource = R.drawable.hi1;
                break;
            case "HO1":
                IceBreakResource = R.drawable.hot1;
                break;
            case "ILU":
                IceBreakResource = R.drawable.iloveyou1;
                break;
            case "LGL":
                IceBreakResource = R.drawable.letsgetlaid1;
                break;
            case "NA1":
                IceBreakResource = R.drawable.niceass1;
                break;
            case "NB1":
                IceBreakResource = R.drawable.nicebody1;
                break;
            case "NDS":
                IceBreakResource = R.drawable.nicedressingsense1;
                break;
            case "NH1":
                IceBreakResource = R.drawable.nicehair1;
                break;
            case "NP1":
                IceBreakResource = R.drawable.niceprofile1;
                break;
            case "NO1":
                IceBreakResource = R.drawable.notinterested1;
                break;
            case "TH1":
                IceBreakResource = R.drawable.thanks1;
                break;
            case "THY":
                IceBreakResource = R.drawable.thankyou1;
                break;
            default:
                IceBreakResource = R.drawable.hi1;
                break;
        }
        return IceBreakResource;
    }

    public static String GetIceBreakMessageFromCode(final String ICCode) {
        String IceBreakMessage = "Hi";
        switch (ICCode) {
            case "HI1":
                IceBreakMessage = "Hi";
                break;
            case "HO1":
                IceBreakMessage = "Hot";
                break;
            case "ILU":
                IceBreakMessage = "I Love You";
                break;
            case "LGL":
                IceBreakMessage = "Let's get laid";
                break;
            case "NA1":
                IceBreakMessage = "Nice Ass";
                break;
            case "NB1":
                IceBreakMessage = "Nice Body";
                break;
            case "NDS":
                IceBreakMessage = "Nice dressing sense";
                break;
            case "NH1":
                IceBreakMessage = "Nice Hair";
                break;
            case "NP1":
                IceBreakMessage = "Nice Profile";
                break;
            case "NO1":
                IceBreakMessage = "Not Interested";
                break;
            case "TH1":
                IceBreakMessage = "Thanks";
                break;
            case "THY":
                IceBreakMessage = "Thank You";
                break;
            default:
                IceBreakMessage = "Hi";
                break;
        }
        return IceBreakMessage;
    }


    public static void HideKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowKeyboard(Activity activity, View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowIceBreakerModal(Context context, String ClickedUserID) {
        try {
            Intent intent = new Intent(context, IceBreakerActivity.class);
            intent.putExtra("ClickedUserID", ClickedUserID);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowBuyPremiumIfNotPremium(Context context, String Message, Boolean newTask) {
        try {
            if (!GDGenericHelper.IsUserPremium(context)) {
                Intent intent = new Intent(context, GetPremiumActivity.class);
                intent.putExtra("LimitExceedMessage", Message);
                if (newTask) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            } else {
                GDDialogHelper.ShowSingleButtonTypeDialog(context, "Limit exceeded", Message, GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.ALERT, null);
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            GDLogHelper.UploadErrorLogsToServer(context, true);
        }
    }

    public static float DistBetweenLocations(String LatLng1, String LatLng2) {
        try {
            List<String> Location1 = StringHelper.SplitStringByComma(LatLng1);
            List<String> Location2 = StringHelper.SplitStringByComma(LatLng2);
            float lat1 = Float.parseFloat(Location1.get(0));
            float lng1 = Float.parseFloat(Location1.get(1));
            float lat2 = Float.parseFloat(Location2.get(0));
            float lng2 = Float.parseFloat(Location2.get(1));
            double earthRadius = 6371000; //meters
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            float dist = (float) (earthRadius * c);

            return dist;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return 0;
        }
    }
}
