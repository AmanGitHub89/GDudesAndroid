package com.gdudes.app.gdudesapp.activities.LoginRegister;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.BuildConfig;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDCountDownTimer;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.MasterDataHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.TextLinkHelper;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.GDCountDownTimerCallback;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.R;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends GDAppCompatActivity {

    Context mContext;
    GDValidationHelper gdValidationHelper;
    String LoginPassword;

    RelativeLayout LoginActivityMainContainer;
    TextView ForgotPassword;
    TextView Register;
    Button btnLogin;
    EditText txtEmailID;
    EditText txtPassword;
    ImageView PasswordIcon;
    ImageView ViewPasswordIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SessionManager.InitSessionManager(getApplicationContext());
        PersistantPreferencesHelper.InitPersistantPreferences(getApplicationContext());
        mContext = LoginActivity.this;

        LoginActivityMainContainer = findViewById(R.id.LoginActivityMainContainer);
        ForgotPassword = findViewById(R.id.ForgotPassword);
        Register = findViewById(R.id.Register);
        btnLogin = findViewById(R.id.btnLogin);
        txtEmailID = findViewById(R.id.txtEmailID);
        txtPassword = findViewById(R.id.txtPassword);
        PasswordIcon = findViewById(R.id.PasswordIcon);
        ViewPasswordIcon = findViewById(R.id.ViewPasswordIcon);

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!txtPassword.getText().toString().trim().equals("")) {
                    ViewPasswordIcon.setVisibility(View.VISIBLE);
                    PasswordIcon.setVisibility(View.GONE);
                } else {
                    ViewPasswordIcon.setVisibility(View.GONE);
                    PasswordIcon.setVisibility(View.VISIBLE);
                }
            }
        });
        ViewPasswordIcon.setOnClickListener(v -> {
            txtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            txtPassword.setSelection(txtPassword.getText().length());
            GDCountDownTimer.StartCountDown(2000, () -> {
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                txtPassword.setSelection(txtPassword.getText().length());
            });
        });

        btnLogin.setOnClickListener(v -> {
            if (!gdValidationHelper.Validate()) {
                GDToastHelper.ShowValidationErrorToast(mContext);
                return;
            }
            String emailID = txtEmailID.getText().toString().trim();
            if (emailID.contains("@gdudes")) {
                GDToastHelper.ShowToast(mContext, getString(R.string.invalid_email), GDToastHelper.ERROR, GDToastHelper.SHORT);
                return;
            }
            LoginPassword = txtPassword.getText().toString();
            List<APICallParameter> pAPICallParameters = new ArrayList<>();
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_EmailID, emailID));
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_Password, LoginPassword));
            APICallInfo apiCallInfo = new APICallInfo("Login", "Login", pAPICallParameters, "GET", null, null, false,
                    new APIProgress(mContext, "Signing in..", false), APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                try {
                    if (result == null || result.trim().equals("-1") || result.trim().equals("")) {
                        GDToastHelper.ShowToast(LoginActivity.this, "Could not login.\nPlease make sure internet is on.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        return;
                    }
                    Users LoggedInUser = new GsonBuilder().create().fromJson(result, Users.class);
                    if (LoggedInUser != null && LoggedInUser.UserID != null && !LoggedInUser.UserID.trim().equals("")) {
                        UserObjectsCacheHelper.AddUpdUserToCache(LoggedInUser);
                        SessionManager.UserLogIn(LoggedInUser);
                        SessionManager.SetLoginPassword(LoginPassword);
                        SessionManager.RedirectUserToActivity(LoggedInUser, LoginActivity.this);
                        finish();
                    } else {
                        SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                        if (successResult != null && successResult.SuccessResult == -101) {
                            GDToastHelper.ShowToast(LoginActivity.this, "Invalid Credentials.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        } else {
                            GDToastHelper.ShowGenericErrorToast(LoginActivity.this);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                    GDToastHelper.ShowGenericErrorToast(LoginActivity.this);
                }
            }, () -> GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT));
        });

        AddTextLinks();
        SetValidations();
        String LoggedOutEmailID = PersistantPreferencesHelper.GetLoggedOutEmailID();
        if (LoggedOutEmailID != null && !LoggedOutEmailID.trim().equals("")) {
            txtEmailID.setText(LoggedOutEmailID);
        }
        CheckForAppUpdate(LoginActivity.this);

        //hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void AddTextLinks() {
        TextLinkHelper.AddLink(ForgotPassword, ForgotPassword.getText().toString(), () -> {
            Intent intent = new Intent(mContext, ForgotPasswordAndReSendVerificationActivity.class);
            intent.putExtra("Activity_Mode", ForgotPasswordAndReSendVerificationActivity.FORGOT_PASSWORD);
            startActivityForResult(intent, 2);
        }, false);
        TextLinkHelper.AddLink(Register, Register.getText().toString(), new OnDialogButtonClick() {
            @Override
            public void dialogButtonClicked() {
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivityForResult(intent, 1);
            }
        }, false);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                //Registration Success
                Boolean IsRegistrationSuccess = data.getExtras().getBoolean("IsSuccess", false);
                if (IsRegistrationSuccess) {
                    finish();
                }
            } else if (requestCode == 2) {
                GDToastHelper.ShowToast(LoginActivity.this, "An Email has been sent to change password. The link expires in 3 hours.", GDToastHelper.INFO, GDToastHelper.LONG);
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        MasterDataHelper.InitMasterDataHelper(LoginActivity.this);
    }

    private void SetValidations() {
        List<EditText> TextValidations = new ArrayList<>();
        TextValidations.add(txtEmailID);
        TextValidations.add(txtPassword);
        gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
        gdValidationHelper.AddNonEmptyValidator(0).AddEmailValidator(0);
        gdValidationHelper.AddNonEmptyValidator(1);
        gdValidationHelper.UpdateFormValidators();
    }

    public static void CheckForAppUpdate(final Context context) {
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        APICallInfo apiCallInfo = new APICallInfo("Login", "GetLatestAppVersion", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(context, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result == null || result.equals("") || result.equals("-1")) {
                        return;
                    }
                    int LatestAppVersion = Integer.parseInt(result);
                    if (BuildConfig.VERSION_CODE < LatestAppVersion) {
                        GDDialogHelper.ShowYesNoTypeDialog(context, "Get latest version of GDudes",
                                "A new version of GDudes is available.\nWould you like to upgrade it?",
                                GDDialogHelper.BUTTON_TEXT_UPDATE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.INFO,
                                new OnDialogButtonClick() {
                                    @Override
                                    public void dialogButtonClicked() {
                                        final String appPackageName = context.getPackageName();
                                        try {
                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        }
                                    }
                                }, null);
                    }
                } catch (NumberFormatException e) {
                    //Do nothing
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                }
            }
        }, null);
    }
}
