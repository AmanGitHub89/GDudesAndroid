package com.gdudes.app.gdudesapp.activities.LoginRegister;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDRadioOptions;
import com.gdudes.app.gdudesapp.GDTypes.GDSKeyValue;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.OnRadioOptionselected;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.gdudes.app.gdudesapp.activities.MainLayout.LayoutActivity;

import java.util.ArrayList;
import java.util.List;

public class RegisterProfileDescActivity extends GDCustomToolbarAppCompatActivity {

    private static String LogClass = "RegisterProfileDescActivity";
    Context mContext;
    Users LoggedInUser;

    List<GDSKeyValue> SexualOrientationDT;
    List<GDSKeyValue> SexualPreferenceDT;
    String SexualOrientation = "G";
    String SexualPreference = "V";
    GDValidationHelper gdValidationHelper;

    GDRadioOptions SexualOrientationSpinner;
    GDRadioOptions SexualPreferenceSpinner;
    EditText TagLine;
    EditText Description;
    Button btnSave;

    public RegisterProfileDescActivity() {
        super("Profile details");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile_desc);
        mContext = RegisterProfileDescActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        SexualOrientationSpinner = (GDRadioOptions) findViewById(R.id.SexualOrientationSpinner);
        SexualPreferenceSpinner = (GDRadioOptions) findViewById(R.id.SexualPreferenceSpinner);
        TagLine = (EditText) findViewById(R.id.TagLine);
        Description = (EditText) findViewById(R.id.Description);
        btnSave = (Button) findViewById(R.id.btnSave);

        SexualOrientationDT = new ArrayList<>();
        SexualOrientationDT.add(new GDSKeyValue("G", "I am gay"));
        SexualOrientationDT.add(new GDSKeyValue("B", "I am Bi-Sexual"));
        SexualOrientationDT.add(new GDSKeyValue("T", "I am Transgendered"));
        SexualOrientationSpinner.SetData(mContext, "What is your sexual orientation?", SexualOrientationDT, "G",
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        SexualOrientation = SexualOrientationSpinner.getTag().toString();
                    }
                }, null);
        SexualPreferenceDT = new ArrayList<>();
        SexualPreferenceDT.add(new GDSKeyValue("B", "I am Bottom"));
        SexualPreferenceDT.add(new GDSKeyValue("MB", "I am Mostly Bottom"));
        SexualPreferenceDT.add(new GDSKeyValue("V", "I am Versatile"));
        SexualPreferenceDT.add(new GDSKeyValue("MT", "I am Mostly Top"));
        SexualPreferenceDT.add(new GDSKeyValue("T", "I am Top"));
        SexualPreferenceDT.add(new GDSKeyValue("N", "I am not into anals"));
        SexualPreferenceSpinner.SetData(mContext, "What is your sexual preference?", SexualPreferenceDT, "V",
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        SexualPreference = SexualPreferenceSpinner.getTag().toString();
                    }
                }, null);

//        SexualOrientationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
//                ((TextView) parent.getChildAt(0)).setTextSize(14);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        SexualPreferenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
//                ((TextView) parent.getChildAt(0)).setTextSize(14);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveDetails();
            }
        });
        SetValidations();

        HasActions = false;
        ShowTitleWithoutActions = true;
        postCreate();
        //hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void SaveDetails() {
        try {
            if (!gdValidationHelper.Validate()) {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.validation_error), TopSnackBar.LENGTH_SHORT, true).show();
                return;
            }
            UserProfileDetails profileDetails = new UserProfileDetails();
            profileDetails.UserID = LoggedInUser.UserID;
            profileDetails.TagLine = StringEncoderHelper.encodeURIComponent(TagLine.getText().toString());
            profileDetails.UserDetailedDesc = StringEncoderHelper.encodeURIComponent(Description.getText().toString());
            profileDetails.SexualOrientation = SexualOrientation;
            profileDetails.SexualPreference = SexualPreference;
            APICallInfo apiCallInfo = new APICallInfo("CompleteProfile", "InsUpdUserProfileDetails_Mobile", null, "POST", profileDetails,
                    null, false, new APIProgress(mContext, "Saving..", false), APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            return;
                        }
                        if (result.equals("1")) {
                            GDToastHelper.ShowToast(mContext, "Details saved", GDToastHelper.INFO, GDToastHelper.SHORT);
                            LoggedInUser.ProfileComplete = true;
                            SessionManager.UserLogIn(LoggedInUser);
                            Intent intent = new Intent(getApplicationContext(), LayoutActivity.class);
                            //intent.putExtra("IsRegistrationFirstEdit", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
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
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register_profile_desc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Please save the details to continue", TopSnackBar.LENGTH_SHORT, true).show();
        } else if (id == R.id.action_logout) {
            GDLogHelper.Log(LogClass, "onOptionsItemSelected", "User Logged out. Logout.");
            SessionManager.UserLogout(RegisterProfileDescActivity.this, LoggedInUser);
            finish();
        }
        return false;
        //return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Please save the details to continue", TopSnackBar.LENGTH_SHORT, true).show();
    }

    private void SetValidations() {
        List<EditText> TextValidations = new ArrayList<>();
        TextValidations.add(TagLine);
        TextValidations.add(Description);
        gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
        gdValidationHelper.AddNonEmptyValidator(0).AddCharRangeValidator(0, 20, 200);
        gdValidationHelper.AddNonEmptyValidator(1).AddCharRangeValidator(1, 20, 2000);
        gdValidationHelper.UpdateFormValidators();
    }

    class UserProfileDetails {
        public String UserID;
        public String TagLine;
        public String UserDetailedDesc;
        public String SexualOrientation;
        public String SexualPreference;
    }
}
