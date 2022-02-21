package com.gdudes.app.gdudesapp.activities.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AccountSettingsActivity extends GDCustomToolbarAppCompatActivity implements View.OnClickListener {

    private static String LogClass = "AccountSettingsActivity";
    Context mContext;
    Users LoggedInUser;

    Menu mMenu;
    String mEmail;
    String mMobileNo;

    LinearLayout PageContainer;
    LinearLayout AccountSettings_Options;
    LinearLayout AccountSettings_ChangeMobileContainer;
    LinearLayout AccountSettings_ChangeUserNameContainer;
    LinearLayout AccountSettings_ChangePasswordContainer;
    RelativeLayout AccountSettings_ChangeMobile;
    RelativeLayout AccountSettings_ChangeUserName;
    RelativeLayout AccountSettings_ChangePassword;
    RelativeLayout AccountSettings_DeleteAccount;

    TextView ChangeMobileCurrentMobile;
    EditText ChangeMobileNewMobile;
    TextView ChangeUserNameCurrentUserName;
    EditText ChangeUserNameNewUserName;
    EditText ChangePasswordCurrentPassword;
    EditText ChangePasswordNewPassword;
    EditText ConfirmChangePasswordNewPassword;

    TextView CurrentMobile;
    TextView CurrentUserName;

    int CurrentSelectedOptions = 0;
    GDValidationHelper gdValidationHelper;

    public AccountSettingsActivity() {
        super("Account Settings");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        mContext = AccountSettingsActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        PageContainer = (LinearLayout) findViewById(R.id.PageContainer);
        AccountSettings_Options = (LinearLayout) findViewById(R.id.AccountSettings_Options);
        AccountSettings_ChangeMobileContainer = (LinearLayout) findViewById(R.id.AccountSettings_ChangeMobileContainer);
        AccountSettings_ChangeUserNameContainer = (LinearLayout) findViewById(R.id.AccountSettings_ChangeUserNameContainer);
        AccountSettings_ChangePasswordContainer = (LinearLayout) findViewById(R.id.AccountSettings_ChangePasswordContainer);
        AccountSettings_ChangeMobile = (RelativeLayout) findViewById(R.id.AccountSettings_ChangeMobile);
        AccountSettings_ChangeUserName = (RelativeLayout) findViewById(R.id.AccountSettings_ChangeUserName);
        AccountSettings_ChangePassword = (RelativeLayout) findViewById(R.id.AccountSettings_ChangePassword);
        AccountSettings_DeleteAccount = (RelativeLayout) findViewById(R.id.AccountSettings_DeleteAccount);

        ChangeMobileCurrentMobile = (TextView) AccountSettings_ChangeMobileContainer.findViewById(R.id.ChangeMobileCurrentMobile);
        ChangeMobileNewMobile = (EditText) AccountSettings_ChangeMobileContainer.findViewById(R.id.ChangeMobileNewMobile);
        ChangeUserNameCurrentUserName = (TextView) AccountSettings_ChangeUserNameContainer.findViewById(R.id.ChangeUserNameCurrentUserName);
        ChangeUserNameNewUserName = (EditText) AccountSettings_ChangeUserNameContainer.findViewById(R.id.ChangeUserNameNewUserName);
        ChangePasswordCurrentPassword = (EditText) AccountSettings_ChangePasswordContainer.findViewById(R.id.ChangePasswordCurrentPassword);
        ChangePasswordNewPassword = (EditText) AccountSettings_ChangePasswordContainer.findViewById(R.id.ChangePasswordNewPassword);
        ConfirmChangePasswordNewPassword = (EditText) AccountSettings_ChangePasswordContainer.findViewById(R.id.ConfirmChangePasswordNewPassword);

        CurrentMobile = (TextView) AccountSettings_ChangeMobile.findViewById(R.id.CurrentMobile);
        CurrentUserName = (TextView) AccountSettings_ChangeUserName.findViewById(R.id.CurrentUserName);

        AccountSettings_ChangeMobile.setTag(1);
        AccountSettings_ChangeUserName.setTag(2);
        AccountSettings_ChangePassword.setTag(3);
        AccountSettings_DeleteAccount.setTag(4);
        AccountSettings_ChangeMobile.setOnClickListener(this);
        AccountSettings_ChangeUserName.setOnClickListener(this);
        AccountSettings_ChangePassword.setOnClickListener(this);
        AccountSettings_DeleteAccount.setOnClickListener(this);


        mEmail = getIntent().getExtras().getString("EmailID", "");
        mMobileNo = getIntent().getExtras().getString("MobileNo", "");

        CurrentMobile.setText(mMobileNo);
        ChangeMobileCurrentMobile.setText(mMobileNo);
        CurrentUserName.setText(LoggedInUser.GetDecodedUserName());
        ChangeUserNameCurrentUserName.setText(LoggedInUser.GetDecodedUserName());

        HasActions = false;
        ShowTitleWithoutActions = true;
        postCreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_account_settings, menu);
        mMenu.getItem(0).setVisible(false);
        mMenu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && CurrentSelectedOptions != 0) {
            ShowContainer(0);
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            if (gdValidationHelper.Validate()) {
                switch (CurrentSelectedOptions) {
                    case 1:
                        ChangeMobileNo(ChangeMobileNewMobile.getText().toString());
                        break;
                    case 2:
                        ChangeUserName(ChangeUserNameNewUserName.getText().toString());
                        break;
                    case 3:
                        if (!ChangePasswordNewPassword.getText().toString().trim().equals(ConfirmChangePasswordNewPassword.getText().toString().trim())) {
                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Passwords do not match", TopSnackBar.LENGTH_SHORT, true).show();
                            return true;
                        }
                        ChangePassword(ChangePasswordCurrentPassword.getText().toString().trim(), ChangePasswordNewPassword.getText().toString().trim());
                        break;
                }
            } else {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.validation_error), TopSnackBar.LENGTH_SHORT, true).show();
            }
            return true;
        }
        FinishWithReturnIntent();
        return true;
    }

    @Override
    public void onClick(View v) {
        int order = (int) v.getTag();
        Intent intent = null;
        switch (order) {
            case 1:
                ShowContainer(1);
                break;
            case 2:
                ShowContainer(2);
                break;
            case 3:
                ShowContainer(3);
                break;
            case 4:
                GDDialogHelper.ShowYesNoTypeDialog(mContext, "Delete GDudes Account", "Are you sure you want to delete your GDudes account?", GDDialogHelper.BUTTON_TEXT_DELETE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT, new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        GDDialogHelper.ShowYesNoTypeDialog(mContext, "Are you sure?", "All your data would be deleted permanently. Are you sure you want to delete your GDudes account?", GDDialogHelper.BUTTON_TEXT_DELETE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT, new OnDialogButtonClick() {
                            @Override
                            public void dialogButtonClicked() {
                                ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
                                pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
                                APICallInfo apiCallInfo = new APICallInfo("Home", "DeleteAccount", pAPICallParameters, "GET", null, null, false,
                                        new APIProgress(AccountSettingsActivity.this, "Deleting account..", false), APICallInfo.APITimeouts.MEDIUM);
                                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                                    @Override
                                    public void onAPIComplete(String result, Object ExtraData) {
                                        try {
                                            if (result.equals("1")) {
                                                GDToastHelper.ShowToast(AccountSettingsActivity.this, "GDudes account deleted", GDToastHelper.INFO, GDToastHelper.SHORT);
                                                GDLogHelper.Log(LogClass, "onClick->DeleteAccount", "User Logged out. Logout.");
                                                SessionManager.UserLogout(AccountSettingsActivity.this, LoggedInUser);
                                                finish();
                                            } else {
                                                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred. Please try again.", TopSnackBar.LENGTH_SHORT, true).show();
                                            }
                                        } catch (Exception e) {
                                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred. Please try again.", TopSnackBar.LENGTH_SHORT, true).show();
                                            e.printStackTrace();
                                            GDLogHelper.LogException(e);
                                        }
                                    }
                                }, new APINoNetwork() {
                                    @Override
                                    public void onAPINoNetwork() {
                                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                                    }
                                });
                            }
                        }, null);
                    }
                }, null);
                break;
        }
    }

    private void ShowContainer(int order) {
        switch (order) {
            case 0:
                CurrentSelectedOptions = 0;
                AccountSettings_Options.setVisibility(View.VISIBLE);
                AccountSettings_ChangeMobileContainer.setVisibility(View.GONE);
                AccountSettings_ChangePasswordContainer.setVisibility(View.GONE);
                AccountSettings_ChangeUserNameContainer.setVisibility(View.GONE);
                changeToolbarText("Account Settings");
                mMenu.getItem(1).setVisible(false);
                break;
            case 1:
                CurrentSelectedOptions = 1;
                AccountSettings_Options.setVisibility(View.GONE);
                AccountSettings_ChangeMobileContainer.setVisibility(View.VISIBLE);
                AccountSettings_ChangePasswordContainer.setVisibility(View.GONE);
                AccountSettings_ChangeUserNameContainer.setVisibility(View.GONE);
                changeToolbarText("Change mobile");
                ChangeUserNameNewUserName.setText("");
                mMenu.getItem(1).setVisible(true);
                break;
            case 2:
                CurrentSelectedOptions = 2;
                AccountSettings_Options.setVisibility(View.GONE);
                AccountSettings_ChangeMobileContainer.setVisibility(View.GONE);
                AccountSettings_ChangePasswordContainer.setVisibility(View.GONE);
                AccountSettings_ChangeUserNameContainer.setVisibility(View.VISIBLE);
                changeToolbarText("Change username");
                ChangeMobileNewMobile.setText("");
                mMenu.getItem(1).setVisible(true);
                break;
            case 3:
                CurrentSelectedOptions = 3;
                AccountSettings_Options.setVisibility(View.GONE);
                AccountSettings_ChangeMobileContainer.setVisibility(View.GONE);
                AccountSettings_ChangePasswordContainer.setVisibility(View.VISIBLE);
                AccountSettings_ChangeUserNameContainer.setVisibility(View.GONE);
                changeToolbarText("Change password");
                ChangePasswordCurrentPassword.setText("");
                ChangePasswordNewPassword.setText("");
                ConfirmChangePasswordNewPassword.setText("");
                mMenu.getItem(1).setVisible(true);
                break;
        }
        CurrentMobile.setText(mMobileNo);
        ChangeMobileCurrentMobile.setText(mMobileNo);
        SetValidatorForTab(order);
    }

    private void SetValidatorForTab(int index) {
        try {
            List<EditText> TextValidations = new ArrayList<>();

            switch (index) {
                case 1:
                    TextValidations.add(ChangeMobileNewMobile);
                    gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
                    gdValidationHelper.AddNonEmptyValidator(0).AddCharRangeValidator(0, 10, 30).
                            AddRegexValidator(0, GDValidationHelper.iNumberOnly);
                    break;
                case 2:
                    TextValidations.add(ChangeUserNameNewUserName);
                    gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
                    gdValidationHelper.AddNonEmptyValidator(0).AddCharRangeValidator(0, 3, 15);
                    break;
                case 3:
                    TextValidations.add(ChangePasswordCurrentPassword);
                    TextValidations.add(ChangePasswordNewPassword);
                    TextValidations.add(ConfirmChangePasswordNewPassword);
                    gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
                    gdValidationHelper.AddNonEmptyValidator(0);
                    gdValidationHelper.AddNonEmptyValidator(1).AddCharRangeValidator(1, 6, 20).
                            AddRegexValidator(1, GDValidationHelper.iPasswordValidation);
                    gdValidationHelper.AddNonEmptyValidator(2).AddCharRangeValidator(2, 6, 20).
                            AddRegexValidator(2, GDValidationHelper.iPasswordValidation);
                    break;
            }
            gdValidationHelper.UpdateFormValidators();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private void ChangeMobileNo(String MobileNo) {
        ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_MobileNo, MobileNo));
        APICallInfo apiCallInfo = new APICallInfo("Home", "ChangeMobile", pAPICallParameters, "GET", null, MobileNo, false,
                new APIProgress(mContext, "Changing mobile number. Please wait..", true), APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result.equals("1")) {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Mobile number changed", TopSnackBar.LENGTH_SHORT, false).show();
                        mMobileNo = (String) ExtraData;
                        ShowContainer(0);
                    } else {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while updating mobile number.", TopSnackBar.LENGTH_SHORT, true).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while updating mobile number.", TopSnackBar.LENGTH_SHORT, true).show();
                }
            }
        }, new APINoNetwork() {
            @Override
            public void onAPINoNetwork() {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
            }
        });
    }

    private void ChangePassword(String Password, final String NewPassword) {
        ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_Password, Password));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_NewPassword, NewPassword));
        APICallInfo apiCallInfo = new APICallInfo("Home", "ChangePassword", pAPICallParameters, "GET", null, null, false,
                new APIProgress(mContext, "Changing password. Please wait..", false), APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result.equals("1")) {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Account password changed", TopSnackBar.LENGTH_SHORT, false).show();
                        SessionManager.SetLoginPassword(NewPassword);
                        ShowContainer(0);
                    } else {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while updating account password.", TopSnackBar.LENGTH_SHORT, true).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while updating account password.", TopSnackBar.LENGTH_SHORT, true).show();
                }
            }
        }, new APINoNetwork() {
            @Override
            public void onAPINoNetwork() {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
            }
        });
    }

    private void ChangeUserName(final String NewUserName) {
        ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserName, StringEncoderHelper.encodeURIComponent(NewUserName)));
        APICallInfo apiCallInfo = new APICallInfo("Home", "ChangeUserName", pAPICallParameters, "GET", null, null, false,
                new APIProgress(mContext, "Changing username. Please wait..", false), APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result.equals("1")) {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Username changed", TopSnackBar.LENGTH_SHORT, false).show();
                        LoggedInUser.UserName = StringEncoderHelper.encodeURIComponent(NewUserName);
                        UserObjectsCacheHelper.AddUpdUserToCache(LoggedInUser);
                        SessionManager.UserLogIn(LoggedInUser);
                        CurrentUserName.setText(LoggedInUser.GetDecodedUserName());
                        ChangeUserNameCurrentUserName.setText(LoggedInUser.GetDecodedUserName());
                        ShowContainer(0);
                    } else {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while updating username.", TopSnackBar.LENGTH_SHORT, true).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while updating username.", TopSnackBar.LENGTH_SHORT, true).show();
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
    public void onBackPressed() {
        if (CurrentSelectedOptions != 0) {
            ShowContainer(0);
        } else {
            FinishWithReturnIntent();
        }
    }

    private void FinishWithReturnIntent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("EmailID", mEmail);
        returnIntent.putExtra("MobileNo", mMobileNo);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
    }
}
