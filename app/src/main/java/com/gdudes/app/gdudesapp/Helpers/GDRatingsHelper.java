package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Settings.ReportIssueMakeSugesstionActivity;

import java.util.Date;

public class GDRatingsHelper {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;
    private static Boolean IsRatingsSessionManagerInitiated = false;
    private static final String PREF_NAME = "GDRatingsSession";
    private static int PRIVATE_MODE = 0;
    private static int MAX_DECLINES = 10;

    private final static int BUTTON_TEXT_YES = 0;
    private final static int BUTTON_TEXT_NO = 1;
    private final static int BUTTON_TEXT_CANCEL = 2;
    private final static int BUTTON_TEXT_OK = 3;
    private final static int BUTTON_TEXT_NOT_NOW = 4;
    private final static int BUTTON_TEXT_NEVER = 5;

    private static final String KEY_LastRatingShownDT = "LastRatingShownDT";
    private static final String KEY_DeclinedRatingCount = "DeclinedRatingCount";


    public static void ShowRateAppIfNeeded(Context acontext) {
        try {
            context = acontext;
            InitRatingsSessionManager();

            if (GetDeclinedRatingCount() >= MAX_DECLINES || !UserRegisteredBefore6Days()) {
                return;
            }
            String sLastRatingShownDT = GetLastRatingShownDT();
            Boolean ShowDialog = true;
            if (sLastRatingShownDT != null && !sLastRatingShownDT.trim().equals("")) {
                if (!TimeElapsedForRating(GDDateTimeHelper.GetDateFromString(sLastRatingShownDT))) {
                    ShowDialog = false;
                }
            }
            if (ShowDialog) {
                SetLastRatingShownDT();
                ShowLocalRateAppDialog();
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    private static void ShowLocalRateAppDialog() {
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.rate_app_local_dialog, null);
            final RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                }
            });
            final ButtonClicked buttonClicked = new ButtonClicked();
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setView(dialogView)
                    .setTitle("Do you like me?")
                    .setMessage("Please tell us how do you find GDudes?")
                    .setPositiveButton(getButtonTextStringID(BUTTON_TEXT_OK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            buttonClicked.IsButtonClicked = true;
                            float rating = ratingBar.getRating();
                            if (rating >= 4.0f) {
                                ShowPlayStoreRateAppDialog();
                            } else if (rating == 0.0f) {
                                //Do nothing. User didn't select any stars/didn't rate.
                                Toast.makeText(context, "Please select stars to rate or press Cancel", Toast.LENGTH_SHORT).show();
                                ShowLocalRateAppDialog();
                            } else {
                                if (rating < 4.0f) {
                                    ShowDialogForFeedback();
                                }
                                IncreaseDeclinedRatingCount(false);
                            }
                        }
                    })
                    .setNegativeButton(getButtonTextStringID(BUTTON_TEXT_CANCEL), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            buttonClicked.IsButtonClicked = true;
                            IncreaseDeclinedRatingCount(false);
                        }
                    })
                    .show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!buttonClicked.IsButtonClicked) {
                        IncreaseDeclinedRatingCount(false);
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private static void ShowPlayStoreRateAppDialog() {
        try {
            final ButtonClicked buttonClicked = new ButtonClicked();
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle("Wow thanks !")
                    .setMessage("Would you please rate us on play store?")
                    .setPositiveButton(getButtonTextStringID(BUTTON_TEXT_YES), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            buttonClicked.IsButtonClicked = true;
                            String appPackageName = context.getPackageName();
                            try {
                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                try {
                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                } catch (Exception e) {
                                }
                            }
                            IncreaseDeclinedRatingCount(true);
                        }
                    })
                    .setNegativeButton(getButtonTextStringID(BUTTON_TEXT_NOT_NOW), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            buttonClicked.IsButtonClicked = true;
                            IncreaseDeclinedRatingCount(false);
                        }
                    })
//                    .setNeutralButton(getButtonTextStringID(BUTTON_TEXT_NEVER), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            buttonClicked.IsButtonClicked = true;
//                            IncreaseDeclinedRatingCount(true);
//                        }
//                    })
                    .setIcon(R.drawable.smiley1)
                    .show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!buttonClicked.IsButtonClicked) {
                        IncreaseDeclinedRatingCount(false);
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private static void ShowDialogForFeedback() {
        try {
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle("Tell us what went wrong")
                    .setMessage("We are sorry your experience isn't great on GDudes.\nWould you like to share any feedback?")
                    .setPositiveButton(getButtonTextStringID(BUTTON_TEXT_YES), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(context, ReportIssueMakeSugesstionActivity.class);
                            intent.putExtra("Activity_Mode", ReportIssueMakeSugesstionActivity.SUGESSTION);
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton(getButtonTextStringID(BUTTON_TEXT_CANCEL), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(R.drawable.smiley13)
                    .show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }


    private static int getButtonTextStringID(final int buttonText) {
        switch (buttonText) {
            case BUTTON_TEXT_YES:
                return R.string.dialog_yes;
            case BUTTON_TEXT_NO:
                return R.string.dialog_no;
            case BUTTON_TEXT_CANCEL:
                return R.string.cancel;
            case BUTTON_TEXT_OK:
                return R.string.dialog_ok;
            case BUTTON_TEXT_NOT_NOW:
                return R.string.dialog_notnow;
            case BUTTON_TEXT_NEVER:
                return R.string.dialog_never;
            default:
                return R.string.dialog_ok;
        }
    }

    private static void SetLastRatingShownDT() {
        try {
            editor.putString(KEY_LastRatingShownDT, GDDateTimeHelper.GetCurrentDateTimeAsString(false));
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private static String GetLastRatingShownDT() {
        try {
            if (sharedPreferences == null) {
                return "";
            }
            return sharedPreferences.getString(KEY_LastRatingShownDT, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    private static void IncreaseDeclinedRatingCount(Boolean SetToMax) {
        try {
            int DeclinedRatingCount = GetDeclinedRatingCount();
            editor.putInt(KEY_DeclinedRatingCount, DeclinedRatingCount + 1);
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private static int GetDeclinedRatingCount() {
        try {
            if (sharedPreferences == null) {
                return 0;
            }
            return sharedPreferences.getInt(KEY_DeclinedRatingCount, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return 0;
        }
    }

    private static void InitRatingsSessionManager() {
        try {
            if (IsRatingsSessionManagerInitiated) {
                return;
            }
            sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = sharedPreferences.edit();
            IsRatingsSessionManagerInitiated = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private static Boolean TimeElapsedForRating(Date LastRatingShownDT) {
        Boolean TimeElapsed = false;
        Date TimeNow = GDDateTimeHelper.GetDateFromString(GDDateTimeHelper.GetCurrentDateTimeAsString(false));
        long Hours = (TimeNow.getTime() - LastRatingShownDT.getTime()) / 1000 / 60 / 60;
        if (Hours > 78) {      //More than 3 days
            TimeElapsed = true;
        }
        return TimeElapsed;
    }

    private static Boolean UserRegisteredBefore6Days() {
        Boolean TimeElapsed = false;
        try {
            Users LoggedInUser = SessionManager.GetLoggedInUser(context);
            Date TimeNow = GDDateTimeHelper.GetDateFromString(GDDateTimeHelper.GetCurrentDateTimeAsString(true));
            long Hours = (TimeNow.getTime() - (GDDateTimeHelper.GetDateFromString(LoggedInUser.RegisterDateTime)).getTime()) / 1000 / 60 / 60;
            if (Hours > 48) {      //More than 2 days
                TimeElapsed = true;
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return TimeElapsed;
    }

    static class ButtonClicked {
        public Boolean IsButtonClicked = false;
    }
}
