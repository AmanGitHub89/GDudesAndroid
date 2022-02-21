package com.gdudes.app.gdudesapp.activities.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.gdudes.app.gdudesapp.CustomViewTypes.GDRadioOptions;
import com.gdudes.app.gdudesapp.GDTypes.AppSettings;
import com.gdudes.app.gdudesapp.GDTypes.GDSKeyValue;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.OnRadioOptionselected;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AppSettingsActivity extends GDCustomToolbarAppCompatActivity {

    private static String LogClass = "LayoutActivity";
    MediaPlayer mediaPlayer;
    GDRadioOptions UnitSystemSpinner = null;
    GDRadioOptions NotificationSoundsSpinner = null;
    Switch NotificationVibrate = null;
    Switch PlayMessageTones = null;
    Switch ShowNotifications = null;

    Users LoggedInUser = null;
    Context mContext = null;
    static List<GDSKeyValue> UnitSystemDT;
    static List<GDSKeyValue> NotificationTonesDT;
    String mUnitSystem = "M";
    int SelectedTone = 0;

    public AppSettingsActivity() {
        super("App Settings");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_app_settings);
            mContext = AppSettingsActivity.this;
            LoggedInUser = SessionManager.GetLoggedInUser(mContext);

            UnitSystemSpinner = (GDRadioOptions) findViewById(R.id.UnitSystemSpinner);
            NotificationSoundsSpinner = (GDRadioOptions) findViewById(R.id.NotificationSoundsSpinner);
            NotificationVibrate = (Switch) findViewById(R.id.NotificationVibrate);
            PlayMessageTones = (Switch) findViewById(R.id.PlayMessageTones);
            ShowNotifications = (Switch) findViewById(R.id.ShowNotifications);

            mUnitSystem = PersistantPreferencesHelper.GetAppSettings().UnitSystem;
            SelectedTone = Integer.parseInt(PersistantPreferencesHelper.GetAppSettings().NotificationTone);
            NotificationVibrate.setChecked(PersistantPreferencesHelper.GetAppSettings().NotificationVibrate.equals("1"));
            NotificationVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SetVibrateForNotification(isChecked);
                    if (isChecked) {
                        try {
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            v.vibrate(500);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            GDLogHelper.LogException(ex);
                        }
                    }
                }
            });
            PlayMessageTones.setChecked(PersistantPreferencesHelper.GetAppSettings().PlayMessageTones.equals("1"));
            PlayMessageTones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SetPlayMessageTone(isChecked);
                    if (isChecked) {
                        mediaPlayer = MediaPlayer.create(AppSettingsActivity.this, R.raw.message_received);
                        mediaPlayer.start();
                    }
                }
            });
            ShowNotifications.setChecked(PersistantPreferencesHelper.GetAppSettings().ShowNotifications.equals("1"));
            ShowNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    GDLogHelper.Log(LogClass, "onCreate", "onCreate->ShowNotifications.setOnCheckedChangeListener : " + Boolean.toString(isChecked));
                    SetShowNotifications(isChecked);
                }
            });

            HasActions = false;
            ShowTitleWithoutActions = true;
            postCreate();
            InstantiateDTAndSpinners();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
        }
    }

    private void InstantiateDTAndSpinners() {
        try {
            UnitSystemDT = new ArrayList<>();
            UnitSystemDT.add(new GDSKeyValue("M", "Metric"));
            UnitSystemDT.add(new GDSKeyValue("I", "Imperial"));

            NotificationTonesDT = new ArrayList<>();
            NotificationTonesDT.add(new GDSKeyValue("0", "Tone 1"));
            NotificationTonesDT.add(new GDSKeyValue("1", "Tone 2"));
            NotificationTonesDT.add(new GDSKeyValue("2", "Tone 3"));
            NotificationTonesDT.add(new GDSKeyValue("99", "System default"));
            NotificationTonesDT.add(new GDSKeyValue("3", "Off"));

            UnitSystemSpinner.SetData(mContext, "Preferred unit system", UnitSystemDT, mUnitSystem,
                    new OnRadioOptionselected() {
                        @Override
                        public void RadioOptionselected(int position) {
                            mUnitSystem = UnitSystemSpinner.getTag().toString();
                            SetUnitSystem(mUnitSystem);
                        }
                    }, null, false);
            NotificationSoundsSpinner.SetData(mContext, "Select Notification Sound", NotificationTonesDT, Integer.toString(SelectedTone), new OnRadioOptionselected() {
                @Override
                public void RadioOptionselected(int position) {
                    int SountID = R.raw.gdn0;
                    switch (position) {
                        case 0:
                            SountID = R.raw.gdn0;
                            SelectedTone = 0;
                            break;
                        case 1:
                            SountID = R.raw.gdn1;
                            SelectedTone = 1;
                            break;
                        case 2:
                            SountID = R.raw.gdn2;
                            SelectedTone = 2;
                            break;
                        case 3:
                            SelectedTone = 99;
                            //System default
                            break;
                        case 4:
                            //Sound off
                            SelectedTone = 3;
                            break;
                    }
                    if (SelectedTone == 99) {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(AppSettingsActivity.this, notification);
                        r.play();
                    } else if (SelectedTone == 3) {
                        //No Sound
                    } else {
                        mediaPlayer = MediaPlayer.create(AppSettingsActivity.this, SountID);
                        mediaPlayer.start();
                    }
                    SetNotificationTone(Integer.toString(SelectedTone));
                }
            }, null, false);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }


    private void SetUnitSystem(String unitSystem) {
        AppSettings appSettings = PersistantPreferencesHelper.GetAppSettings();
        appSettings.UnitSystem = unitSystem;
        PersistantPreferencesHelper.SetAppSettings(appSettings);
    }

    private void SetNotificationTone(String notificationTone) {
        AppSettings appSettings = PersistantPreferencesHelper.GetAppSettings();
        appSettings.NotificationTone = notificationTone;
        PersistantPreferencesHelper.SetAppSettings(appSettings);
    }

    private void SetVibrateForNotification(Boolean vibrateForNotification) {
        AppSettings appSettings = PersistantPreferencesHelper.GetAppSettings();
        appSettings.NotificationVibrate = vibrateForNotification ? "1" : "0";
        PersistantPreferencesHelper.SetAppSettings(appSettings);
    }

    private void SetPlayMessageTone(Boolean playMessageTone) {
        AppSettings appSettings = PersistantPreferencesHelper.GetAppSettings();
        appSettings.PlayMessageTones = playMessageTone ? "1" : "0";
        PersistantPreferencesHelper.SetAppSettings(appSettings);
    }

    private void SetShowNotifications(Boolean showNotifications) {
        AppSettings appSettings = PersistantPreferencesHelper.GetAppSettings();
        appSettings.ShowNotifications = showNotifications ? "1" : "0";
        PersistantPreferencesHelper.SetAppSettings(appSettings);
    }

    @Override
    public void onBackPressed() {
        FinishWithReturnIntent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FinishWithReturnIntent();
        return true;
    }

    private void FinishWithReturnIntent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("UnitSystem", mUnitSystem);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
    }
}
