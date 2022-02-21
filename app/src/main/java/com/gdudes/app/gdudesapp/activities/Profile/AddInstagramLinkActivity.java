package com.gdudes.app.gdudesapp.activities.Profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.List;

public class AddInstagramLinkActivity extends GDAppCompatActivity {

    Context mContext;
    Users LoggedInUser;
    String sInstagramUserName = "";
    EditText InstagramUserName;
    Button btnSave;
    GDValidationHelper gdValidationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_instagram_link);
            mContext = AddInstagramLinkActivity.this;
            LoggedInUser = SessionManager.GetLoggedInUser(mContext);
            if (getIntent().hasExtra("InstagramUserName")) {
                sInstagramUserName = getIntent().getExtras().getString("InstagramUserName", "");
            }
            InstagramUserName = (EditText) findViewById(R.id.InstagramUserName);
            btnSave = (Button) findViewById(R.id.btnSave);
            InstagramUserName.setText(sInstagramUserName);

            List<EditText> TextValidations = new ArrayList<>();
            TextValidations.add(InstagramUserName);
            gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
            gdValidationHelper.AddCharRangeValidator(0, 0, 100);
            gdValidationHelper.UpdateFormValidators();

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!gdValidationHelper.Validate()) {
                            GDToastHelper.ShowValidationErrorToast(mContext);
                            return;
                        }
                        SaveData();
                    } catch (Exception ex) {

                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            GDLogHelper.UploadErrorLogsToServer(getApplicationContext(), true);
            finish();
        }
    }

    private void SaveData() {
        try {
            if (!gdValidationHelper.Validate()) {
                GDToastHelper.ShowValidationErrorToast(mContext);
                return;
            }
            List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_InstagramUserName, StringEncoderHelper.encodeURIComponent(InstagramUserName.getText().toString())));
            APICallInfo apiCallInfo = new APICallInfo("Home", "SaveInstagramUserName", pAPICallParameters, "GET", null, null, false,
                    new APIProgress(mContext, "Saving..", true), APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result.equals("1")) {
                            GDToastHelper.ShowToast(mContext, "Saved", GDToastHelper.INFO, GDToastHelper.SHORT);
                            Intent returnIntent = new Intent();
                            if (getParent() == null) {
                                setResult(Activity.RESULT_OK, returnIntent);
                            } else {
                                getParent().setResult(Activity.RESULT_OK, returnIntent);
                            }
                            finish();
                        } else {
                            GDToastHelper.ShowGenericErrorToast(mContext);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                        GDToastHelper.ShowGenericErrorToast(mContext);
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                }
            });
        } catch (Exception ex) {
            GDToastHelper.ShowGenericErrorToast(mContext);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_add_instagram_link, menu);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        Double dWidth = size.x * 0.8;
        Double dHeight = size.y * 0.5;
        getWindow().setLayout(dWidth.intValue(), dHeight.intValue());
    }

}
