package com.gdudes.app.gdudesapp.activities.Pics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.R;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//Do not extend from GDAppCompatActivity
public class EditPicActivity extends AppCompatActivity {

    private static String LogClass = "EditPicActivity";
    Context mContext;
    Users LoggedInUser;
    GDPic mGDPic = null;
    String ClubID = "";

    RelativeLayout EditPicMainLayout;
    ImageView EditPic;
    EditText PicCaption;
    ImageView btnClose;
    Boolean ResumeCalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_pic);
            mContext = EditPicActivity.this;
            LoggedInUser = SessionManager.GetLoggedInUser(mContext);

            mGDPic = getIntent().getExtras().getParcelable("GDPic");
            if (getIntent().hasExtra("ClubID")) {
                ClubID = getIntent().getExtras().getString("ClubID", "");
            }
            InitFieldsAndEvents();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            GDLogHelper.UploadErrorLogsToServer(getApplicationContext(), true);
            finish();
        }
    }

    private void InitFieldsAndEvents() {
        EditPicMainLayout = (RelativeLayout) findViewById(R.id.EditPicMainLayout);
        PicCaption = (EditText) findViewById(R.id.PicCaption);
        btnClose = (ImageView) findViewById(R.id.btnClose);
        EditPic = (ImageView) findViewById(R.id.EditPic);

        if (mGDPic.Caption != null) {
            PicCaption.setText(StringEncoderHelper.decodeURIComponent(mGDPic.Caption));
        }
        if (mGDPic.PicID != null && !mGDPic.PicID.trim().equals("")) {
            GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(EditPicActivity.this);
            String PicSrc = gdImageDBHelper.GetImageStringByPicID(mGDPic.PicID, false);
            if (PicSrc != null && !PicSrc.trim().equals("")) {
                try {
                    EditPic.setImageBitmap(ImageHelper.GetBitmapFromString(PicSrc));
                } catch (Exception ex) {
                    GDLogHelper.LogException(ex);
                }
            }
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (ClubID == null || ClubID.trim().equals("")) {
                        UpdateUserPicture updateUserPicture = new UpdateUserPicture(LoggedInUser.UserID, mGDPic.PicID,
                                StringEncoderHelper.encodeURIComponent(PicCaption.getText().toString()));
                        APICallInfo apiCallInfo = new APICallInfo("Home", "UpdateUserPicture", null, "POST",
                                updateUserPicture, null, false, new APIProgress(mContext, "Saving..", true), APICallInfo.APITimeouts.MEDIUM);
                        GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                            @Override
                            public void onAPIComplete(String result, Object ExtraData) {
                                try {
                                    if (result == null || result.equals("") || result.equals("-1")) {
                                        return;
                                    }
                                    SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                                    if (successResult.SuccessResult == 1) {
                                        GDToastHelper.ShowToast(mContext, "Saved", GDToastHelper.INFO, GDToastHelper.SHORT);
                                        FinishWithResult();
                                    } else if (successResult.SuccessResult == Integer.parseInt(getResources().getString(R.string.user_limit_error_code))) {
                                        GDGenericHelper.ShowBuyPremiumIfNotPremium(mContext, successResult.FailureMessage,true);
                                        finish();
                                    } else {
                                        GDToastHelper.ShowErrorToastForSuccessResult(mContext, successResult);
                                    }
                                } catch (Exception e) {
                                    GDLogHelper.LogException(e);
                                }
                            }
                        }, new APINoNetwork() {
                            @Override
                            public void onAPINoNetwork() {
                                GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                            }
                        });
                    } else {
                        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
                        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
                        pAPICallParameters.add(new APICallParameter(APICallParameter.param_ClubID, ClubID));
                        pAPICallParameters.add(new APICallParameter(APICallParameter.param_PicID, mGDPic.PicID));
                        pAPICallParameters.add(new APICallParameter(APICallParameter.param_Caption, StringEncoderHelper.encodeURIComponent(PicCaption.getText().toString())));
                        APICallInfo apiCallInfo = new APICallInfo("Clubs", "UpdateClubPicture", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
                        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                            @Override
                            public void onAPIComplete(String result, Object ExtraData) {
                                try {
                                    if (result == null || result.equals("") || result.equals("-1")) {
                                        return;
                                    }
                                    JSONObject jsonObject = new JSONObject(result);
                                    String sSuccessResult = jsonObject.getString("SuccessResult");
                                    SuccessResult successResult = new GsonBuilder().create().fromJson(StringHelper.TrimFirstAndLastCharacter(sSuccessResult), SuccessResult.class);

                                    if (successResult.SuccessResult == 1) {
                                        GDToastHelper.ShowToast(mContext, "Saved", GDToastHelper.INFO, GDToastHelper.SHORT);
                                        FinishWithResult();
                                    } else {
                                        GDToastHelper.ShowErrorToastForSuccessResult(mContext, successResult);
                                    }
                                } catch (Exception e) {
                                    GDLogHelper.LogException(e);
                                }
                            }
                        }, new APINoNetwork() {
                            @Override
                            public void onAPINoNetwork() {
                                GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                            }
                        });
                    }
                } catch (Exception ex) {
                    GDLogHelper.LogException(ex);
                } finally {
                    FinishWithResult();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_edit_pic, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        getWindow().setLayout(EditPicMainLayout.getMeasuredWidth(), EditPicMainLayout.getMeasuredHeight());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ResumeCalled) {
            finish();
        }
        ResumeCalled = true;
    }

    private void FinishWithResult() {
        mGDPic.Caption = StringEncoderHelper.encodeURIComponent(PicCaption.getText().toString());
        Intent returnIntent = new Intent();
        returnIntent.putExtra("GDPic", mGDPic);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
    }

    class UpdateUserPicture {
        public String UserID;
        public String PicID;
        public String Caption;

        public UpdateUserPicture(String vUserID, String vPicID, String vCaption) {
            UserID = vUserID;
            PicID = vPicID;
            Caption = vCaption;
        }
    }
}
