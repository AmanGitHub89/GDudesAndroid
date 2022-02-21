package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.R;

public class GDToastHelper {
    public final static int INFO = 0;
    public final static int ALERT = 1;
    public final static int ERROR = 2;
    public final static int SHORT = 0;
    public final static int LONG = 1;

    public static void ShowToast(Context context, String Message, int IconCode, int LengthCode) {
        try {
            View ToastView = LayoutInflater.from(context).inflate(R.layout.gd_toast, null);
            ImageView image = (ImageView) ToastView.findViewById(R.id.ToastImage);
            image.setImageResource(getIconID(IconCode));
            TextView text = (TextView) ToastView.findViewById(R.id.ToastText);
            text.setText(Message);
            if (IconCode == ERROR) {
                SetColorForError(context, ToastView);
            }
            Toast toast = new Toast(context);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(LengthCode == 1 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            toast.setView(ToastView);
            toast.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowGenericErrorToast(Context context) {
        ShowToast(context, "An error occurred. Please try again.", ERROR, SHORT);
    }

    public static void ShowValidationErrorToast(Context context) {
        ShowToast(context, "One or more fields are incorrect", ERROR, SHORT);
    }

    public static void ShowErrorToastForSuccessResult(Context context, SuccessResult successResult) {
        try {
            String ErrorMessage = "An error has occurred. Please try again";
            if (successResult != null && successResult.FailureMessage != null && !successResult.FailureMessage.trim().equals("")) {
                ErrorMessage = successResult.FailureMessage;
            }
            ShowToast(context, ErrorMessage, ERROR, SHORT);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            ShowGenericErrorToast(context);
        }
    }

    private static void SetColorForError(Context context, View ToastView) {
        try {
            ToastView.setBackgroundColor(ContextCompat.getColor(context, R.color.toastError));
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private static int getIconID(final int icon) {
        switch (icon) {
            case INFO:
                return android.R.drawable.ic_dialog_info;
            case ALERT:
                return android.R.drawable.ic_dialog_alert;
            case ERROR:
                return android.R.drawable.stat_notify_error;
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }
}
