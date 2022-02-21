package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.Interfaces.OnGDSpinnerCheckboxSelected;
import com.gdudes.app.gdudesapp.Interfaces.OnRadioOptionselected;
import com.gdudes.app.gdudesapp.R;

public class GDDialogHelper {
    public final static int INFO = 0;
    public final static int ALERT = 1;
    public final static int ERROR = 2;
    public final static int BUTTON_TEXT_YES = 0;
    public final static int BUTTON_TEXT_NO = 1;
    public final static int BUTTON_TEXT_CANCEL = 2;
    public final static int BUTTON_TEXT_OK = 3;
    public final static int BUTTON_TEXT_DELETE = 4;
    public final static int BUTTON_TEXT_UPDATE = 5;
    public final static int BUTTON_TEXT_PREMIUM_BENEFITS = 6;
    public final static int BUTTON_TEXT_UNBLOCK = 7;
    public final static int BUTTON_TEXT_BLOCK_HIM_TOO = 8;
    public final static int BUTTON_TEXT_REPORT_USER = 9;
    public final static int BUTTON_TEXT_CONTINUE = 10;
    public final static int BUTTON_TEXT_BLOCK = 11;

    public static void ShowYesNoTypeDialog(Context context, CharSequence Title, CharSequence Message,
                                           int positiveButtonText, int negativeButtonText, int icon,
                                           final OnDialogButtonClick onDialogPositiveButtonClick,
                                           final OnDialogButtonClick onDialogNegativeButtonClick) {
        try {
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle(Title)
                    .setMessage(Message)
                    .setPositiveButton(getButtonTextStringID(positiveButtonText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogPositiveButtonClick != null) {
                                onDialogPositiveButtonClick.dialogButtonClicked();
                            }
                        }
                    })
                    .setNegativeButton(getButtonTextStringID(negativeButtonText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogNegativeButtonClick != null) {
                                onDialogNegativeButtonClick.dialogButtonClicked();
                            }
                        }
                    })
                    .setIcon(getIconID(icon))
                    .show();
            dialog.setCanceledOnTouchOutside(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowSingleButtonTypeDialog(Context context, CharSequence Title, CharSequence Message,
                                                  int ButtonText, int icon,
                                                  final OnDialogButtonClick onDialogButtonClick) {
        try {
            final ButtonClicked buttonClicked = new ButtonClicked();
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle(Title)
                    .setMessage(Message)
                    .setNeutralButton(getButtonTextStringID(ButtonText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            buttonClicked.IsButtonClicked = true;
                            if (onDialogButtonClick != null) {
                                onDialogButtonClick.dialogButtonClicked();
                            }
                        }
                    })
                    .setIcon(getIconID(icon))
                    .show();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!buttonClicked.IsButtonClicked) {
                        if (onDialogButtonClick != null) {
                            onDialogButtonClick.dialogButtonClicked();
                        }
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowRadioOptionTypeDialog(final Context context, final CharSequence[] OptionsList,
                                                 CharSequence Title, int SelectedItem, int negativeButtonText,
                                                 final OnRadioOptionselected RadioOptionselected,
                                                 final OnDialogButtonClick onDialogNegativeButtonClick) {
        if (SelectedItem < 0) {
            SelectedItem = 0;
        }
        try {
            new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle(Title)
                    .setNegativeButton(getButtonTextStringID(negativeButtonText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogNegativeButtonClick != null) {
                                onDialogNegativeButtonClick.dialogButtonClicked();
                            }
                        }
                    })
                    .setSingleChoiceItems(OptionsList, SelectedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (RadioOptionselected != null) {
                                RadioOptionselected.RadioOptionselected(which);
                                dialog.dismiss();
                            }
                        }
                    })
                    .show();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowRadioOptionTypeDialogWithPositiveButton(final Context context, final CharSequence[] OptionsList,
                                                                   CharSequence Title, int SelectedItem,
                                                                   int positiveButtonText, int negativeButtonText,
                                                                   final OnRadioOptionselected RadioOptionselected,
                                                                   final OnDialogButtonClick onDialogPositiveButtonClick,
                                                                   final OnDialogButtonClick onDialogNegativeButtonClick) {
        if (SelectedItem < 0) {
            SelectedItem = 0;
        }
        try {
            new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle(Title)
                    .setPositiveButton(getButtonTextStringID(positiveButtonText), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogPositiveButtonClick != null) {
                                onDialogPositiveButtonClick.dialogButtonClicked();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(getButtonTextStringID(negativeButtonText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogNegativeButtonClick != null) {
                                onDialogNegativeButtonClick.dialogButtonClicked();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setSingleChoiceItems(OptionsList, SelectedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (RadioOptionselected != null) {
                                RadioOptionselected.RadioOptionselected(which);
                            }
                        }
                    })
                    .show();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }


    public static void ShowCheckboxOptionTypeDialog(final Context context, final CharSequence[] OptionsList,
                                                    CharSequence Title, final boolean[] CheckedItems, int positiveButtonText, int negativeButtonText,
                                                    final OnDialogButtonClick onDialogPositiveButtonClick,
                                                    final OnDialogButtonClick onDialogNegativeButtonClick,
                                                    final OnGDSpinnerCheckboxSelected onGDSpinnerCheckboxSelected) {
        try {
            final ButtonClicked buttonClicked = new ButtonClicked();
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle(Title)
                    .setPositiveButton(getButtonTextStringID(positiveButtonText), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogPositiveButtonClick != null) {
                                onDialogPositiveButtonClick.dialogButtonClicked();
                                dialog.dismiss();
                            }
                        }
                    })
                    .setNegativeButton(getButtonTextStringID(negativeButtonText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            buttonClicked.IsButtonClicked = true;
                            if (onDialogNegativeButtonClick != null) {
                                onDialogNegativeButtonClick.dialogButtonClicked();
                            }
                        }
                    })
                    .setMultiChoiceItems(OptionsList, CheckedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (onGDSpinnerCheckboxSelected != null) {
                                onGDSpinnerCheckboxSelected.GDSpinnerCheckboxSelected(which, isChecked);
                            }
                        }
                    })
                    .show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!buttonClicked.IsButtonClicked) {
                        if (onDialogNegativeButtonClick != null) {
                            onDialogNegativeButtonClick.dialogButtonClicked();
                        }
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static void ShowThreeButtonTypeDialog(Context context, CharSequence Title, CharSequence Message,
                                                 int positiveButtonText, int negativeButtonText, int neutralButtonText, int icon,
                                                 final OnDialogButtonClick onDialogPositiveButtonClick,
                                                 final OnDialogButtonClick onDialogNegativeButtonClick,
                                                 final OnDialogButtonClick onDialogNeutralButtonClick) {
        try {
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle(Title)
                    .setMessage(Message)
                    .setPositiveButton(getButtonTextStringID(positiveButtonText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogPositiveButtonClick != null) {
                                onDialogPositiveButtonClick.dialogButtonClicked();
                            }
                        }
                    })
                    .setNegativeButton(getButtonTextStringID(negativeButtonText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogNegativeButtonClick != null) {
                                onDialogNegativeButtonClick.dialogButtonClicked();
                            }
                        }
                    })
                    .setNeutralButton(getButtonTextStringID(neutralButtonText), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (onDialogNeutralButtonClick != null) {
                                onDialogNeutralButtonClick.dialogButtonClicked();
                            }
                        }
                    })
                    .setIcon(getIconID(icon))
                    .show();
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
            case BUTTON_TEXT_DELETE:
                return R.string.dialog_delete;
            case BUTTON_TEXT_UPDATE:
                return R.string.dialog_update_app;
            case BUTTON_TEXT_PREMIUM_BENEFITS:
                return R.string.dialog_premium_benefits;
            case BUTTON_TEXT_UNBLOCK:
                return R.string.dialog_unblock;
            case BUTTON_TEXT_BLOCK_HIM_TOO:
                return R.string.dialog_blockhimtoo;
            case BUTTON_TEXT_REPORT_USER:
                return R.string.dialog_report_user;
            case BUTTON_TEXT_CONTINUE:
                return R.string.dialog_continue;
            case BUTTON_TEXT_BLOCK:
                return R.string.dialog_block;
            default:
                return R.string.dialog_ok;
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

    static class ButtonClicked {
        public Boolean IsButtonClicked = false;
    }
}
