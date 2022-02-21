package com.gdudes.app.gdudesapp.activities.LoginRegister;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.CustomViewTypes.DatePickerFragment;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.CustomViewTypes.GifMovieView;
import com.gdudes.app.gdudesapp.GDTypes.UserLocation;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDCountDownTimer;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.GPSHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.MasterDataHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.TextLinkHelper;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.DateSelected;
import com.gdudes.app.gdudesapp.Interfaces.GPSLocationChanged;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDMapActivity;
import com.gdudes.app.gdudesapp.activities.Settings.TermsAndPrivacyPolicyActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends GDAppCompatActivity {

    private static String LogClass = "RegisterActivity";

    Context mContext;
    String LoginPassword;
    String TempUserID = "";
    Date SelectedBirthDate;
    GDValidationHelper gdValidationHelper;

    EditText UserName;
    EditText Password;
    ImageView PasswordIcon;
    ImageView ViewPasswordIcon;
    EditText DateOfBirth;
    ImageView DateOfBirthIcon;
    EditText Email;
    EditText MobileNumber;
    ImageView CaptchaImage;
    GifMovieView LoadingCaptchaGIF;
    EditText CaptchaCode;
    ImageView NewCaptchaCode;
    CheckBox AcceptTerms;
    TextView AcceptTermsText;
    Button btnRegister;
    String GPSLatLng = "";
    GPSLocationChanged mGPSLocationChanged = null;
    int ActivateLocationDialogCount = 0;
    String ActivateLocationTempID = "";
    int RegisterLocationNotFoundCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        TempUserID = GDGenericHelper.GetNewGUID();

        InitControls();
        SetEvents();
        GetNewCaptcha();
        SetValidations();
        try {
            ActivateLocationTempID = "NoEmail_" + GDGenericHelper.GetNewGUID();
            //hide keyboard
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } catch (Exception ex) {
            //Do nothing
        }
        mGPSLocationChanged = location -> {
            if (GPSLatLng == null || GPSLatLng.trim().equals("")) {
                if (location != null) {
                    GPSLatLng = GPSHelper.GetStringFromLocation(location);
                    if (GPSLatLng == null || GPSLatLng.trim().equals("")) {
                        GetGPSLocation();
                    }
                } else {
                    GetGPSLocation();
                }
            }
        };
        GPSHelper.AddLocationChangeListener(mGPSLocationChanged);
        GPSHelper.InitGPSHelper(RegisterActivity.this);
    }

    private void InitControls() {
        UserName = findViewById(R.id.UserName);
        Password = findViewById(R.id.Password);
        PasswordIcon = findViewById(R.id.PasswordIcon);
        ViewPasswordIcon = findViewById(R.id.ViewPasswordIcon);
        DateOfBirth = findViewById(R.id.DateOfBirth);
        DateOfBirthIcon = findViewById(R.id.DateOfBirthIcon);
        Email = findViewById(R.id.Email);
        MobileNumber = findViewById(R.id.MobileNumber);
        CaptchaImage = findViewById(R.id.CaptchaImage);
        LoadingCaptchaGIF = findViewById(R.id.LoadingCaptchaGIF);
        CaptchaCode = findViewById(R.id.CaptchaCode);
        NewCaptchaCode = findViewById(R.id.NewCaptchaCode);
        AcceptTerms = findViewById(R.id.AcceptTerms);
        AcceptTermsText = findViewById(R.id.AcceptTermsText);
        btnRegister = findViewById(R.id.Register);
        TextLinkHelper.AddLink(AcceptTermsText, "Terms of Use", () -> {
            Intent intent = new Intent(mContext, TermsAndPrivacyPolicyActivity.class);
            intent.putExtra("Activity_Mode", TermsAndPrivacyPolicyActivity.TERMS_OF_USE);
            startActivity(intent);
        }, false);
        TextLinkHelper.AddLink(AcceptTermsText, "Privacy Policy", () -> {
            Intent intent = new Intent(mContext, TermsAndPrivacyPolicyActivity.class);
            intent.putExtra("Activity_Mode", TermsAndPrivacyPolicyActivity.PRIVACY_STATEMENT);
            startActivity(intent);
        }, false);
    }

    private void SetEvents() {
        Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!Password.getText().toString().trim().equals("")) {
                    ViewPasswordIcon.setVisibility(View.VISIBLE);
                    PasswordIcon.setVisibility(View.GONE);
                } else {
                    ViewPasswordIcon.setVisibility(View.GONE);
                    PasswordIcon.setVisibility(View.VISIBLE);
                }
            }
        });
        ViewPasswordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Password.setInputType(InputType.TYPE_CLASS_TEXT);
                Password.setSelection(Password.getText().length());
                GDCountDownTimer.StartCountDown(2000, () -> {
                    Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    Password.setSelection(Password.getText().length());
                });
            }
        });
        DateOfBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ShowDOB();
                }
            }
        });
        DateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDOB();
            }
        });
        DateOfBirthIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDOB();
            }
        });
        NewCaptchaCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingCaptchaGIF.setVisibility(View.VISIBLE);
                CaptchaImage.setVisibility(View.GONE);
                GetNewCaptcha();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });
        CaptchaCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (CaptchaCode.getText().length() == 6) {
                    try {
                        //hide keyboard
                        GDGenericHelper.HideKeyboard(RegisterActivity.this);
                    } catch (Exception ex) {
                        try {
                            //hide keyboard
                            RegisterActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        } catch (Exception exx) {
                        }
                    }
                }
            }
        });
    }

    private void ShowDOB() {
        GDLogHelper.Log(LogClass,"ShowDOB", "Entered method.", GDLogHelper.LogLevel.ERROR);
        int Year = -1;
        int Month = -1;
        int Day = -1;
        if (SelectedBirthDate != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(SelectedBirthDate);
            Year = c.get(Calendar.YEAR);
            Month = c.get(Calendar.MONTH);
            Day = c.get(Calendar.DAY_OF_MONTH);
        }
        DialogFragment newFragment = new DatePickerFragment();
        ((DatePickerFragment) newFragment).SetData((view, year, month, day, date) -> {
            SelectedBirthDate = date;
            DateOfBirth.setText(GDDateTimeHelper.GetDateOnlyStringFromDate(date, false));
        }, Year, Month, Day);
        ((DatePickerFragment) newFragment).IsDOB = true;
        newFragment.show(getFragmentManager(), "Birthday");
        //hide keyboard
        GDGenericHelper.HideKeyboard(RegisterActivity.this);
    }

    private void GetNewCaptcha() {
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_TempUserID, TempUserID));
        APICallInfo apiCallInfo = new APICallInfo("Login", "RefreshCaptcha", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.SEMILONG);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (result != null && !result.equals("")) {
                    CaptchaImage.setImageBitmap(ImageHelper.GetBitmapFromString(result));
                    LoadingCaptchaGIF.setVisibility(View.GONE);
                    CaptchaImage.setVisibility(View.VISIBLE);
                    CaptchaCode.setText("");
                } else {
                    GetNewCaptcha();
                }
            } catch (Exception e) {
                e.printStackTrace();
                GDLogHelper.LogException(e);
                GetNewCaptcha();
            }
        }, () -> GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT));
    }

    private void Register() {
        try {
            if (!gdValidationHelper.Validate()) {
                GDToastHelper.ShowValidationErrorToast(mContext);
                return;
            }
            String emailID = Email.getText().toString().trim();
            if (emailID.contains("@gdudes")) {
                GDToastHelper.ShowToast(mContext, getString(R.string.invalid_email), GDToastHelper.ERROR, GDToastHelper.SHORT);
                return;
            }
            if (!AcceptTerms.isChecked()) {
                GDToastHelper.ShowToast(mContext, "Please accept the Terms of Use and Privacy Statement to continue.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                return;
            }
            if (GPSLatLng == null || GPSLatLng.trim().equals("")) {
                GPSHelper.ForceInitGPSHelper(RegisterActivity.this);
                GetGPSLocation();
                if (GPSLatLng == null || GPSLatLng.trim().equals("")) {
                    SendErrorLogToServer(Email.getText().toString().trim(), "Could not get GPS location. " + GDDateTimeHelper.GetCurrentDateTimeAsString(true));
                    RegisterLocationNotFoundCount++;
                    if (RegisterLocationNotFoundCount >= 2) {
                        GDDialogHelper.ShowSingleButtonTypeDialog(RegisterActivity.this, "Select Location Manually",
                                "We are unable to get your location.\nPlease select your location manually on map.",
                                GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.ALERT, () -> {
                                    Intent intent = new Intent(RegisterActivity.this, GDMapActivity.class);
                                    intent.putExtra("IsPreLogin", true);
                                    intent.putExtra("Activity_Mode", GDMapActivity.SELECT_LOCATION);
                                    intent.putExtra("TooltipMessage", "Done");
                                    startActivityForResult(intent, 3);
                                });
                        return;
                    }
                    GDToastHelper.ShowToast(mContext, "Could not get GPS location. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                    return;
                }
            }
            LoginPassword = Password.getText().toString();
            Register register = null;
            int APIVersion = 0;
            try {
                APIVersion = Build.VERSION.SDK_INT;
            } catch (Exception ex) {
            }
            String sDOB = GDDateTimeHelper.GetStringFromDate(SelectedBirthDate);
            if(sDOB.equals("")) {
                if (SelectedBirthDate == null) {
                    GDLogHelper.Log(LogClass,"Register", "SelectedBirthDate is null.", GDLogHelper.LogLevel.ERROR);
                }
                String stringDateInTextBox = DateOfBirth.getText().toString();
                GDLogHelper.Log(LogClass,"Register", "DateOfBirth: " + stringDateInTextBox, GDLogHelper.LogLevel.ERROR);
                String emailEnteredInTextBox = Email.getText().toString();
                GDLogHelper.Log(LogClass,"Register", "EmailID: " + emailEnteredInTextBox, GDLogHelper.LogLevel.ERROR);
                String langDisplayName = Locale.getDefault().getDisplayLanguage();
                String langCode = Locale.getDefault().getLanguage();
                GDLogHelper.Log(LogClass,"Register", "Language: " + langDisplayName, GDLogHelper.LogLevel.ERROR);
                GDLogHelper.Log(LogClass,"Register", "Language: " + langCode, GDLogHelper.LogLevel.ERROR);
                GDLogHelper.UploadErrorLogsToServer(getApplicationContext(), true);

                DateOfBirth.setText("");
                Register();
                return;
            }
            register = new Register(TempUserID, StringEncoderHelper.encodeURIComponent(UserName.getText().toString().trim()),
                    LoginPassword, sDOB, emailID,
                    MobileNumber.getText().toString(), "TRUE", CaptchaCode.getText().toString(),
                    GPSLatLng, APIVersion);

            APICallInfo apiCallInfo = new APICallInfo("Login", "Register", null, "POST", register, null, false,
                    new APIProgress(mContext, "Creating new GDudes account. Please wait..", false), APICallInfo.APITimeouts.SEMILONG);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                try {
                    final Users LoggedInUser = new GsonBuilder().create().fromJson(result, Users.class);
                    if (LoggedInUser != null && LoggedInUser.UserID != null && !LoggedInUser.UserID.trim().equals("")) {
                        UserObjectsCacheHelper.AddUpdUserToCache(LoggedInUser);
                        SessionManager.UserLogIn(LoggedInUser);
                        SessionManager.SetLoginPassword(LoginPassword);
                        FinishActivityWithSuccess(LoggedInUser);
                    } else {
                        JSONObject jsonObject = new JSONObject(result);
                        String ErrorCode = jsonObject.getString("SuccessResult");
                        if (ErrorCode.trim().equals("-102")) {
                            GDToastHelper.ShowToast(mContext, "This Email ID is already registered with a GDudes Account.\nIf you have forgotten you password, click on the Forgot Password link in Login page.", GDToastHelper.ERROR, GDToastHelper.LONG);
                        } else if (ErrorCode.trim().equals("-104")) {
                            GDToastHelper.ShowToast(mContext, "The captcha code entered does not match the one in image. Please try again", GDToastHelper.ERROR, GDToastHelper.SHORT);
                            GetNewCaptcha();
                            CaptchaCode.setText("");
                        } else if (ErrorCode.trim().equals("-105")) {
                            GDToastHelper.ShowToast(mContext, "An account with this Email ID existed but was deleted. Use another Email ID.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        } else if (ErrorCode.trim().equals("-106")) {
                            GDToastHelper.ShowToast(mContext, "You are less than 18 as per your selected DOB. Guys under the age of 18 are not allowed on GDudes.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        } else if (ErrorCode.trim().equals("-107")) {
                            GDToastHelper.ShowToast(mContext, "Could not get GPS location. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        } else {
                            GDToastHelper.ShowToast(mContext, "An error occurred while creating account. Please try again", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                    GDToastHelper.ShowToast(mContext, "An error occurred while creating account. Please try again", GDToastHelper.ERROR, GDToastHelper.SHORT);
                }
            }, () -> GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT));
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    private void SetValidations() {
        List<EditText> TextValidations = new ArrayList<>();
        TextValidations.add(UserName);
        TextValidations.add(Password);
        TextValidations.add(DateOfBirth);
        TextValidations.add(Email);
        TextValidations.add(CaptchaCode);
        gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
        gdValidationHelper.AddNonEmptyValidator(0).AddCharRangeValidator(0, 3, 15);
        gdValidationHelper.AddNonEmptyValidator(1).AddCharRangeValidator(1, 6, 20).
                AddRegexValidator(1, GDValidationHelper.iPasswordValidation);
        gdValidationHelper.AddNonEmptyValidator(2);
        gdValidationHelper.AddNonEmptyValidator(3).AddEmailValidator(3);
        gdValidationHelper.AddNonEmptyValidator(4);
        gdValidationHelper.UpdateFormValidators();
    }

    private void FinishActivityWithSuccess(final Users LoggedInUser) {
        GDToastHelper.ShowToast(mContext, "Verification mail sent. The link will expire in 7 days.", GDToastHelper.INFO, GDToastHelper.LONG);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("IsSuccess", true);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        SessionManager.RedirectUserToActivity(LoggedInUser, RegisterActivity.this);
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (GPSHelper.CheckLocationAvailability(RegisterActivity.this)) {
                    GPSHelper.ForceInitGPSHelper(RegisterActivity.this);
                    GetGPSLocation();
                }
                break;
            case 2:
                if (GPSHelper.IsLocationPermissionGranted(RegisterActivity.this)) {
                    //CheckLocationAvailabilityAndInit();
                }
                break;
            case 3:
                if (resultCode == Activity.RESULT_OK) {
                    //Location Received
                    UserLocation SelectedLocation = data.getExtras().getParcelable("UserLocation");
                    if (SelectedLocation != null) {
                        GPSLatLng = SelectedLocation.LocationLatLng;
                        if (GPSLatLng != null && !GPSLatLng.trim().equals("")) {
                            SendErrorLogToServer(Email.getText().toString().trim(), "Got GPS location manually. " +
                                    GPSLatLng + "-" + GDDateTimeHelper.GetCurrentDateTimeAsString(true));
                            Register();
                        }
                    }
                }
                break;
        }
    }

    private void CheckLocationPermissionAndInit() {
        if (GPSHelper.IsLocationPermissionGranted(RegisterActivity.this)) {
            CheckLocationAvailabilityAndInit();
        } else {
            GPSHelper.AskLocationPermission(RegisterActivity.this, 2);
        }
    }

    private void CheckLocationAvailabilityAndInit() {
        if (GPSHelper.CheckLocationAvailability(RegisterActivity.this)) {
            GetGPSLocation();
        } else {
            ActivateLocationDialogCount = ActivateLocationDialogCount + 1;
            if (ActivateLocationDialogCount >= 3) {
                SendErrorLogToServer(Email.getText().toString().trim().equals("") ?
                                ActivateLocationTempID : Email.getText().toString().trim(),
                        "Activate location dialog count: " + Integer.toString(ActivateLocationDialogCount));
            }
            GPSHelper.AskToEnableLocation(RegisterActivity.this, this, 1);
        }
    }

    private void GetGPSLocation() {
        try {
            LatLng latLng = GPSHelper.GetGPSLatLng(RegisterActivity.this);
            if (latLng != null) {
                GPSLatLng = GPSHelper.GetStringFromLatLng(latLng);
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    private void SendErrorLogToServer(String EmailID, String Message) {
        int apiVersion = 0;
        try {
            apiVersion = Build.VERSION.SDK_INT;
        } catch (Exception ex) {
        }
        try {
            APICallInfo apiCallInfo = new APICallInfo("Home", "UploadGPSErrorLog", null, "POST",
                    new ErrorLog(EmailID, Message + "-->> Android API: " + Integer.toString(apiVersion)), null, false, null, APICallInfo.APITimeouts.LONG);
            apiCallInfo.CalledFromService = true;
            GDGenericHelper.executeAsyncPOSTAPITask(RegisterActivity.this, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                }
            }, null);
        } catch (Exception ex2) {
            GDLogHelper.LogException(ex2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    @Override
    public void onResume() {
        super.onResume();
        MasterDataHelper.InitMasterDataHelper(RegisterActivity.this);
        GPSHelper.ForceInitGPSHelper(RegisterActivity.this);
        GetGPSLocation();
        if (GPSLatLng == null || GPSLatLng.trim().equals("")) {
            CheckLocationPermissionAndInit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GPSHelper.RemoveLocationChangeListener(mGPSLocationChanged);
        GPSHelper.DeInitGPSHelper();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    class Register {
        public String TempUserID;
        public String UserName;
        public String Password;
        public String DateOfBirth;
        public String EmailID;
        public String MobileNo;
        public String RegisterTermsAccepted;
        public String CaptchaText;
        public String LocationLatLng;
        public int APIVersion;

        public Register(String vTempUserID, String vUserName, String vPassword, String vDateOfBirth,
                        String vEmailID, String vMobileNo, String vRegisterTermsAccepted, String vCaptchaText,
                        String vLocationLatLng, int vAPIVersion) {
            TempUserID = vTempUserID;
            UserName = vUserName;
            Password = vPassword;
            DateOfBirth = vDateOfBirth;
            EmailID = vEmailID;
            MobileNo = vMobileNo;
            RegisterTermsAccepted = vRegisterTermsAccepted;
            CaptchaText = vCaptchaText;
            LocationLatLng = vLocationLatLng;
            APIVersion = vAPIVersion;
        }
    }

    class ErrorLog {
        public String UserID;
        public String LogData;

        public ErrorLog(String vUserID, String vLogData) {
            UserID = vUserID;
            LogData = vLogData;
        }
    }

}
