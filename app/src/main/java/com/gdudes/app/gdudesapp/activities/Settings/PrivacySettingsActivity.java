package com.gdudes.app.gdudesapp.activities.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.GDTypes.PrivacySettings;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.gdudes.app.gdudesapp.activities.MainLayout.HomePageFragment;

public class PrivacySettingsActivity extends GDCustomToolbarAppCompatActivity {

    Users LoggedInUser = null;
    Context mContext = null;
    Menu mMenu = null;
    PrivacySettings mPrivacySettings = null;


    Switch ShowMyAge;
    Switch ShowMyDistance;

    TextView ShowMyDistanceDetailsMessage;

    public PrivacySettingsActivity() {
        super("Privacy settings");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);
        mContext = PrivacySettingsActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        ShowMyAge = (Switch) findViewById(R.id.ShowMyAge);
        ShowMyDistance = (Switch) findViewById(R.id.ShowMyDistance);

        ShowMyDistanceDetailsMessage = (TextView) findViewById(R.id.ShowMyDistanceDetailsMessage);

        HasActions = false;
        ShowTitleWithoutActions = true;
        postCreate();
        if (getIntent().hasExtra("PrivacySettings")) {
            mPrivacySettings = getIntent().getExtras().getParcelable("PrivacySettings");
        }
        if (mPrivacySettings == null) {
            mPrivacySettings = new PrivacySettings(LoggedInUser.UserID);
        }
        SetExistingData();
        SetEvents();
    }

    private void SetEvents() {
        ShowMyDistance.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                ShowMyDistanceDetailsMessage.setVisibility(View.VISIBLE);
            } else {
                ShowMyDistanceDetailsMessage.setVisibility(View.GONE);
            }
        });
    }

    private void SetExistingData() {
        if (!mPrivacySettings.ShowAgeInSearchTo.equals("N")) {
            ShowMyAge.setChecked(true);
        } else {
            ShowMyAge.setChecked(false);
        }
        if (!mPrivacySettings.ShowDistanceInSearchTo.equals("N")) {
            ShowMyDistance.setChecked(true);
        } else {
            ShowMyDistance.setChecked(false);
            ShowMyDistanceDetailsMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_privacy_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            mPrivacySettings.UserID = LoggedInUser.UserID;
            mPrivacySettings.ShowAgeInSearchTo = ShowMyAge.isChecked() ? "E" : "N";
            mPrivacySettings.ShowDistanceInSearchTo = ShowMyDistance.isChecked() ? "E" : "N";
            APICallInfo apiCallInfo = new APICallInfo("Home", "SavePrivacySettings", null, "POST", mPrivacySettings, null, false,
                    new APIProgress(mContext, "Saving. Please wait..", true), APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, false).show();
                            return;
                        }
                        if (result.equals("1")) {
                            GDToastHelper.ShowToast(mContext, "Saved", GDToastHelper.INFO, GDToastHelper.SHORT);
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("PrivacySettings", mPrivacySettings);
                            if (getParent() == null) {
                                setResult(Activity.RESULT_OK, returnIntent);
                            } else {
                                getParent().setResult(Activity.RESULT_OK, returnIntent);
                            }
                            finish();
                        } else {
                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
