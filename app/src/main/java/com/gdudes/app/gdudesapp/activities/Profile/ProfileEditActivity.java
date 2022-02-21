package com.gdudes.app.gdudesapp.activities.Profile;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.CustomViewTypes.DatePickerFragment;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDRadioOptions;
import com.gdudes.app.gdudesapp.GDTypes.CompleteUserProfile;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.MasterDataHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Interfaces.DateSelected;
import com.gdudes.app.gdudesapp.Interfaces.OnRadioOptionselected;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProfileEditActivity extends GDAppCompatActivity {

    Context mContext;
    Users LoggedInUser;
    CompleteUserProfile mCompleteUserProfile;

    EditText DateOfBirth;
    GDRadioOptions HeightSpinner;
    GDRadioOptions WeightSpinner;
    EditText TagLine;
    Button btnSave;

    Date SelectedBirthDate;
    int Height = 0;
    int Weight = 0;
    GDValidationHelper gdValidationHelper;
    Boolean IsImperial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile_edit);
            mContext = ProfileEditActivity.this;
            LoggedInUser = SessionManager.GetLoggedInUser(mContext);
            IsImperial = PersistantPreferencesHelper.GetAppSettings().UnitSystem.equals("I");

            mCompleteUserProfile = getIntent().getParcelableExtra("CompleteUserProfile");
            Height = mCompleteUserProfile.Height;
            Weight = mCompleteUserProfile.Weight;
            SelectedBirthDate = GDDateTimeHelper.GetDateFromString(mCompleteUserProfile.DOB);

            InitFields();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            GDLogHelper.UploadErrorLogsToServer(getApplicationContext(), true);
            finish();
        }
    }

    private void InitFields() {
        DateOfBirth = (EditText) findViewById(R.id.DateOfBirth);
        HeightSpinner = (GDRadioOptions) findViewById(R.id.HeightSpinner);
        WeightSpinner = (GDRadioOptions) findViewById(R.id.WeightSpinner);
        TagLine = (EditText) findViewById(R.id.TagLine);
        btnSave = (Button) findViewById(R.id.btnSave);

        DateOfBirth.setText(GDDateTimeHelper.GetDateOnlyStringFromDate(GDDateTimeHelper.GetDateFromString(mCompleteUserProfile.DOB), false));
        HeightSpinner.SetData(mContext, "Height", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserHeight, IsImperial),
                Height == 0 ? "" : Integer.toString(mCompleteUserProfile.Height),
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        if (HeightSpinner.getTag().toString().trim().equals("")) {
                            Height = 0;
                        } else {
                            try {
                                Height = Integer.parseInt(HeightSpinner.getTag().toString());
                            } catch (Exception ex) {
                            }
                        }
                    }
                }, null, true);
        WeightSpinner.SetData(mContext, "Weight", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserWeight, IsImperial),
                Weight == 0 ? "" : Integer.toString(mCompleteUserProfile.Weight),
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        if (WeightSpinner.getTag().toString().trim().equals("")) {
                            Weight = 0;
                        } else {
                            try {
                                Weight = Integer.parseInt(WeightSpinner.getTag().toString());
                            } catch (Exception ex) {
                            }
                        }
                    }
                }, null, true);
        TagLine.setText(StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.TagLine));
        DateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDOB();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveData();
            }
        });

        SetValidations();
    }

    private void ShowDOB() {
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
        ((DatePickerFragment) newFragment).SetData(new DateSelected() {
            @Override
            public void onDateSelect(DatePicker view, int year, int month, int day, Date date) {
                DateOfBirth.setText(GDDateTimeHelper.GetDateOnlyStringFromDate(date, false));
                SelectedBirthDate = date;
            }
        }, Year, Month, Day);
        ((DatePickerFragment) newFragment).IsDOB = true;
        newFragment.show(getFragmentManager(), "Birthday");
    }

    private void SetValidations() {
        List<EditText> TextValidations = new ArrayList<>();
        TextValidations.add(DateOfBirth);
        TextValidations.add(TagLine);
        gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
        gdValidationHelper.AddNonEmptyValidator(0);
        gdValidationHelper.AddNonEmptyValidator(1).AddCharRangeValidator(1, 20, 200);
        gdValidationHelper.UpdateFormValidators();
    }

    private void SaveData() {
        try {
            if (!gdValidationHelper.Validate()) {
                GDToastHelper.ShowValidationErrorToast(mContext);
                return;
            }
            ProfileFirstPageEdit profileFirstPageEdit = new ProfileFirstPageEdit(mCompleteUserProfile.UserID, GDDateTimeHelper.GetStringFromDate(SelectedBirthDate),
                    Integer.toString(Height), Integer.toString(Weight), StringEncoderHelper.encodeURIComponent(TagLine.getText().toString()));
            APICallInfo apiCallInfo = new APICallInfo("Home", "ProfileFirstPageEdit", null, "POST", profileFirstPageEdit, null, false,
                    new APIProgress(mContext, "Saving..", true), APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            GDToastHelper.ShowGenericErrorToast(mContext);
                            return;
                        }
                        if (result.equals("1")) {
                            GDToastHelper.ShowToast(mContext, "Saved", GDToastHelper.INFO, GDToastHelper.SHORT);
                            mCompleteUserProfile.DOB = GDDateTimeHelper.GetStringFromDate(SelectedBirthDate);
                            mCompleteUserProfile.TagLine = StringEncoderHelper.encodeURIComponent(TagLine.getText().toString());
                            mCompleteUserProfile.Height = Height;
                            mCompleteUserProfile.Weight = Weight;
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("CompleteUserProfile", mCompleteUserProfile);
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
        getMenuInflater().inflate(R.menu.menu_profile_edit, menu);
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
        Double dHeight = size.y * 0.6;
        getWindow().setLayout(dWidth.intValue(), dHeight.intValue());
    }

    class ProfileFirstPageEdit {
        String UserID;
        String DateOfBirth;
        String Height;
        String Weight;
        String TagLine;

        public ProfileFirstPageEdit(String vUserID, String vDateOfBirth, String vHeight, String vWeight, String vTagLine) {
            UserID = vUserID;
            DateOfBirth = vDateOfBirth;
            Height = vHeight;
            Weight = vWeight;
            TagLine = vTagLine;
        }
    }
}
