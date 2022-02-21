package com.gdudes.app.gdudesapp.activities.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.GDServices.GDMessageService;
import com.gdudes.app.gdudesapp.GDTypes.PrivacySettings;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends GDCustomToolbarAppCompatActivity implements View.OnClickListener {
    private static String LogClass = "SettingsActivity";

    RelativeLayout Settings_AccountSettings;
    RelativeLayout Settings_PrivacySettings;
    RelativeLayout Settings_AppSettings;
    RelativeLayout Settings_BlockedList;
    RelativeLayout Settings_ReportIssue;
    RelativeLayout Settings_MakeSuggesstion;
    RelativeLayout Settings_TermOfUse;
    RelativeLayout Settings_PrivacyPolicy;
    RelativeLayout Settings_Logout;
    TextView BlockedGuysCount;

    Users LoggedInUser = null;
    Context mContext = null;

    String EmailID = "";
    String MobileNo = "";
    PrivacySettings mPrivacySettings = null;
    int mBlockedUsersCount = 0;

    public SettingsActivity() {
        super("Settings");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_settings);
            mContext = SettingsActivity.this;
            LoggedInUser = SessionManager.GetLoggedInUser(mContext);

            Settings_AccountSettings = (RelativeLayout) findViewById(R.id.Settings_AccountSettings);
            Settings_PrivacySettings = (RelativeLayout) findViewById(R.id.Settings_PrivacySettings);
            Settings_AppSettings = (RelativeLayout) findViewById(R.id.Settings_AppSettings);
            Settings_BlockedList = (RelativeLayout) findViewById(R.id.Settings_BlockedList);
            Settings_ReportIssue = (RelativeLayout) findViewById(R.id.Settings_ReportIssue);
            Settings_MakeSuggesstion = (RelativeLayout) findViewById(R.id.Settings_MakeSuggesstion);
            Settings_TermOfUse = (RelativeLayout) findViewById(R.id.Settings_TermOfUse);
            Settings_PrivacyPolicy = (RelativeLayout) findViewById(R.id.Settings_PrivacyPolicy);
            Settings_Logout = (RelativeLayout) findViewById(R.id.Settings_Logout);
            BlockedGuysCount = (TextView) findViewById(R.id.BlockedGuysCount);

            Settings_AccountSettings.setTag(1);
            Settings_PrivacySettings.setTag(2);
            Settings_AppSettings.setTag(3);
            Settings_BlockedList.setTag(4);
            Settings_ReportIssue.setTag(5);
            Settings_MakeSuggesstion.setTag(6);
            Settings_TermOfUse.setTag(7);
            Settings_PrivacyPolicy.setTag(8);
            Settings_AccountSettings.setOnClickListener(this);
            Settings_PrivacySettings.setOnClickListener(this);
            Settings_AppSettings.setOnClickListener(this);
            Settings_BlockedList.setOnClickListener(this);
            Settings_ReportIssue.setOnClickListener(this);
            Settings_MakeSuggesstion.setOnClickListener(this);
            Settings_TermOfUse.setOnClickListener(this);
            Settings_PrivacyPolicy.setOnClickListener(this);
            Settings_Logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GDDialogHelper.ShowYesNoTypeDialog(SettingsActivity.this, "Logout?", "Are you sure you want to logout from your GDudes account?", GDDialogHelper.BUTTON_TEXT_YES, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {
                            List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
                            pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
                            APICallInfo apiCallInfo = new APICallInfo("Login", "Logout", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.SHORT);
                            GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                                @Override
                                public void onAPIComplete(String result, Object ExtraData) {
                                    try {
                                        if (result != null && result.equals("1")) {
                                            GDMessageService service = GDMessageService.ServiceInstance;
                                            if (service != null) {
                                                service.DestroyService(false);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        GDLogHelper.LogException(e);
                                    }
                                }
                            }, null);
                            GDLogHelper.Log(LogClass, "onCreate->Settings_Logout", "User Logged out. Logout.");
                            SessionManager.UserLogout(mContext, LoggedInUser);
                        }
                    }, null);
                }
            });

            HasActions = false;
            ShowTitleWithoutActions = true;
            postCreate();
            GetSettingsData();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            GDToastHelper.ShowGenericErrorToast(SettingsActivity.this);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void GetSettingsData() {
        ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        APICallInfo apiCallInfo = new APICallInfo("Home", "GetUserSettingsData", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result == null || result.equals("") || result.equals("-1")) {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while loading settings data", TopSnackBar.LENGTH_SHORT, true).show();
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(result);
                    try {
                        EmailID = (new JSONObject(StringHelper.TrimFirstAndLastCharacter(jsonObject.
                                getString("UsersContactInfo")))).getString("EmailID");
                        MobileNo = (new JSONObject(StringHelper.TrimFirstAndLastCharacter(jsonObject.
                                getString("UsersContactInfo")))).getString("MobileNumber");
                    } catch (Exception e) {
                    }

                    try {
                        String USPrivacy = StringHelper.TrimFirstAndLastCharacter(jsonObject.getString("UserSettings_Privacy"));
                        if (USPrivacy != null && !USPrivacy.equals("") && !USPrivacy.equals("[]")) {
                            mPrivacySettings = new GsonBuilder().create().fromJson(USPrivacy, PrivacySettings.class);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                    }

                    try {
                        String USBlockedList = jsonObject.getString("UsersSettings_BlockedList");
                        mBlockedUsersCount = Integer.parseInt(new JSONObject(StringHelper.TrimFirstAndLastCharacter(USBlockedList)).getString("BlockedCount"));
                        BlockedGuysCount.setText(Integer.toString(mBlockedUsersCount) + " guys blocked");
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while loading settings data", TopSnackBar.LENGTH_SHORT, true).show();
                }
            }
        }, new APINoNetwork() {
            @Override
            public void onAPINoNetwork() {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int order = (int) v.getTag();
        Intent intent = null;
        switch (order) {
            case 1:
                intent = new Intent(SettingsActivity.this, AccountSettingsActivity.class);
                intent.putExtra("EmailID", EmailID);
                intent.putExtra("MobileNo", MobileNo);
                startActivityForResult(intent, 1);
                break;
            case 2:
                intent = new Intent(SettingsActivity.this, PrivacySettingsActivity.class);
                if (mPrivacySettings != null && mPrivacySettings.UserID != null && !mPrivacySettings.UserID.equals("")) {
                    intent.putExtra("PrivacySettings", mPrivacySettings);
                }
                startActivityForResult(intent, 2);
                break;
            case 3:
                intent = new Intent(SettingsActivity.this, AppSettingsActivity.class);
                startActivityForResult(intent, 3);
                break;
            case 4:
                startActivity(new Intent(SettingsActivity.this, UnblockUsersActivity.class));
                break;
            case 5:
                intent = new Intent(SettingsActivity.this, ReportIssueMakeSugesstionActivity.class);
                intent.putExtra("Activity_Mode", ReportIssueMakeSugesstionActivity.ISSUE);
                startActivity(intent);
                break;
            case 6:
                intent = new Intent(SettingsActivity.this, ReportIssueMakeSugesstionActivity.class);
                intent.putExtra("Activity_Mode", ReportIssueMakeSugesstionActivity.SUGESSTION);
                startActivity(intent);
                break;
            case 7:
                intent = new Intent(mContext, TermsAndPrivacyPolicyActivity.class);
                intent.putExtra("Activity_Mode", TermsAndPrivacyPolicyActivity.TERMS_OF_USE);
                startActivity(intent);
                break;
            case 8:
                intent = new Intent(mContext, TermsAndPrivacyPolicyActivity.class);
                intent.putExtra("Activity_Mode", TermsAndPrivacyPolicyActivity.PRIVACY_STATEMENT);
                startActivity(intent);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                EmailID = data.getExtras().getString("EmailID", "");
                MobileNo = data.getExtras().getString("MobileNo", "");
            } else if (requestCode == 2) {
                mPrivacySettings = data.getExtras().getParcelable("PrivacySettings");
            }
        }
    }
}
