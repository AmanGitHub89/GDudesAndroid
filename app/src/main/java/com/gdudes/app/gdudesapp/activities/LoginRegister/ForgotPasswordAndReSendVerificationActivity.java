package com.gdudes.app.gdudesapp.activities.LoginRegister;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.List;

public class ForgotPasswordAndReSendVerificationActivity extends GDAppCompatActivity {

    Context mContext;
    GDValidationHelper gdValidationHelper;
    Button btnSendMail;
    EditText txtEmailID;
    TextView ScreenText;

    public static int FORGOT_PASSWORD = 0;
    public static int RESEND_VERIFICATION_MAIL = 1;
    public static int Activity_Mode = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_and_re_send_verification);

        mContext = ForgotPasswordAndReSendVerificationActivity.this;
        if (getIntent().hasExtra("Activity_Mode")) {
            Activity_Mode = getIntent().getExtras().getInt("Activity_Mode", -1);
        }
        if (Activity_Mode == -1) {
            finish();
        }

        btnSendMail = (Button) findViewById(R.id.btnSendMail);
        txtEmailID = (EditText) findViewById(R.id.txtEmailID);
        ScreenText = (TextView) findViewById(R.id.ScreenText);
        SetValidations();

        if (Activity_Mode == FORGOT_PASSWORD) {
            ScreenText.setText("Reset password");
        } else {
            ScreenText.setText("Re-send verification Email");
        }

        btnSendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gdValidationHelper.Validate()) {
                    GDToastHelper.ShowValidationErrorToast(mContext);
                    return;
                }
                List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_EmailID, txtEmailID.getText().toString()));
                APICallInfo apiCallInfo = new APICallInfo("Login", "CheckUsersEmailID", pAPICallParameters, "GET", null, null, false,
                        new APIProgress(mContext, "Sending mail. Please wait..", false), APICallInfo.APITimeouts.SEMILONG);
                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                    @Override
                    public void onAPIComplete(String result, Object ExtraData) {
                        try {
                            if (result.equals("1")) {
                                Intent returnIntent = new Intent();
                                if (getParent() == null) {
                                    setResult(Activity.RESULT_OK, returnIntent);
                                } else {
                                    getParent().setResult(Activity.RESULT_OK, returnIntent);
                                }
                                finish();
                            } else if (result.equals("-99")) {
                                GDToastHelper.ShowToast(ForgotPasswordAndReSendVerificationActivity.this, "Invalid Email ID.\nThis Email ID could not be found in our records.", GDToastHelper.ALERT, GDToastHelper.SHORT);
                            } else {
                                GDToastHelper.ShowToast(ForgotPasswordAndReSendVerificationActivity.this, "Email not sent.\nAn Email could not be sent to this Email ID. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            GDLogHelper.LogException(e);
                            GDToastHelper.ShowToast(ForgotPasswordAndReSendVerificationActivity.this, "Email not sent.\nAn Email could not be sent to this Email ID. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        }
                    }
                }, new APINoNetwork() {
                    @Override
                    public void onAPINoNetwork() {
                        GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                    }
                });

            }
        });
    }


    private void SetValidations() {
        List<EditText> TextValidations = new ArrayList<>();
        TextValidations.add(txtEmailID);
        gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
        gdValidationHelper.AddNonEmptyValidator(0).AddEmailValidator(0);
        gdValidationHelper.UpdateFormValidators();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forgot_password_and_re_send_verification, menu);
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
}
