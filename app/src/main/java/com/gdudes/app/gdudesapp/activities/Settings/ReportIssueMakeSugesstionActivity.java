package com.gdudes.app.gdudesapp.activities.Settings;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;

public class ReportIssueMakeSugesstionActivity extends GDCustomToolbarAppCompatActivity {

    public static final int ISSUE = 0;
    public static final int SUGESSTION = 1;
    public static final int REPORTUSER = 2;
    public int Activity_Mode = ISSUE;

    private String ReportUserID = "";
    private String ReportUserReason = "";
    private String EmptySnackbarText = "";

    Users LoggedInUser = null;
    Context mContext = null;
    String IssuePrepend = "";

    EditText Description;

    public ReportIssueMakeSugesstionActivity() {
        super("Report an issue");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);
        mContext = ReportIssueMakeSugesstionActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);


        Description = (EditText) findViewById(R.id.Description);
        if (getIntent().hasExtra("Activity_Mode")) {
            Activity_Mode = getIntent().getExtras().getInt("Activity_Mode", ISSUE);
        }
        if (Activity_Mode == SUGESSTION) {
            setToolbarText("Make a suggestion");
            Description.setHint("Please describe the suggestion you have for us...");
            EmptySnackbarText =  "Please describe the suggestion you have for us.";
        }
        if (Activity_Mode == ISSUE) {
            if (getIntent().hasExtra("IssuePrepend")) {
                IssuePrepend = getIntent().getExtras().getString("IssuePrepend", "");
            }
            EmptySnackbarText = "Please describe the issue you are facing.";
        }
        if (Activity_Mode == REPORTUSER){
            setToolbarText("Report User");
            Description.setHint("Please describe the reason you are reporting this user.");
            ReportUserID = getIntent().getExtras().getString("ReportUserID", "");
            ReportUserReason = getIntent().getExtras().getString("ReportUserReason", "");
            EmptySnackbarText =  "Please describe the reason you are reporting this user.";
        }

        HasActions = false;
        ShowTitleWithoutActions = true;
        postCreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report_issue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (Description.getText().toString().trim().equals("")) {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Add Description to save.\n" + EmptySnackbarText, TopSnackBar.LENGTH_LONG, true).show();
                return false;
            }
            String ReqType = "";
            Object objectToPost = null;
            switch (Activity_Mode) {
                case ISSUE:
                    ReqType = "ReportAnIssue";
                    objectToPost = new ReportedIssue(LoggedInUser.UserID, StringEncoderHelper.encodeURIComponent(IssuePrepend + "--" + Description.getText().toString()));
                    break;
                case SUGESSTION:
                    ReqType = "MakeASugesstion";
                    objectToPost = new Suggestion(LoggedInUser.UserID, StringEncoderHelper.encodeURIComponent(Description.getText().toString()));
                    break;
                case REPORTUSER:
                    ReqType = "ReportUser";
                    objectToPost = new ReportUser(LoggedInUser.UserID, ReportUserID, ReportUserReason,
                            StringEncoderHelper.encodeURIComponent(Description.getText().toString()));
                    break;
            }
            APICallInfo apiCallInfo = new APICallInfo("Home", ReqType, null, "POST", objectToPost, null, false,
                    new APIProgress(mContext, "Saving. Please wait..", true), APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result.equals("1")) {
                            String successAlertMessage = "";
                            switch (Activity_Mode) {
                                case ISSUE:
                                    successAlertMessage = "Thank you for reporting the issue.\nWe will get back to you shortly on your Email.";
                                    break;
                                case SUGESSTION:
                                    successAlertMessage = "Thank you for making the suggestion. We will get back to you if needed.";
                                    break;
                                case REPORTUSER:
                                    successAlertMessage = "Thank you for reporting the user. We will take an action within 48 hours and get back to you if needed.";
                                    break;
                            }
                            GDToastHelper.ShowToast(ReportIssueMakeSugesstionActivity.this, successAlertMessage, GDToastHelper.INFO, GDToastHelper.LONG);
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

    class Suggestion {
        public String UserID;
        public String SugesstionType;
        public String DeviceType;
        public String ODeviceType;
        public String Description;

        public Suggestion(String vUserID, String vDescription) {
            UserID = vUserID;
            SugesstionType = "F";
            DeviceType = "M";
            ODeviceType = "Android";
            Description = vDescription;
        }
    }

    class ReportedIssue {
        public String UserID;
        public String IssueType;
        public String DeviceType;
        public String ODeviceType;
        public String Severity;
        public String Description;

        public ReportedIssue(String vUserID, String vDescription) {
            UserID = vUserID;
            IssueType = "A";
            DeviceType = "M";
            ODeviceType = "Android";
            Severity = "M";
            Description = vDescription;
        }
    }

    class ReportUser {
        public String RequestingUserID;
        public String UserID;
        public String IssueType;
        public String Description;

        public ReportUser(String vRequestingUserID, String vUserID,  String vIssueType, String vDescription) {
            RequestingUserID = vRequestingUserID;
            UserID = vUserID;
            IssueType = vIssueType;
            Description = vDescription;
        }
    }

}
