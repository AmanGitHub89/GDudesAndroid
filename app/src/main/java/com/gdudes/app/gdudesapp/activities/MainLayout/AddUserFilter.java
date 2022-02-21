package com.gdudes.app.gdudesapp.activities.MainLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDCheckboxItems;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDRadioOptions;
import com.gdudes.app.gdudesapp.GDTypes.SavedFilter;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.MasterDataHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.Interfaces.OnRadioOptionselected;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class AddUserFilter extends GDCustomToolbarAppCompatActivity {

    private static String LogClass = "AddUserFilter";
    public static int ADD_FILTER = 0;
    public static int EDIT_FILTER = 1;
    private int Activity_Mode = 0;
    private Boolean IsProfessionalFilter = false;

    private Context mContext;
    private Users LoggedInUser;
    private SavedFilter mSavedFilter;
    EditText FilterName;
    GDValidationHelper gdValidationHelper;
    Boolean SearchRadiusActive = false;

    LinearLayout SexualStatsHeader;
    LinearLayout SexualStatsBody;
    LinearLayout BodyStatsHeader;
    LinearLayout BodyStatsBody;
    LinearLayout OtherStatsHeader;
    LinearLayout OtherStatsBody;

    TextView SexualStats_Plus;
    TextView SexualStats_Minus;
    TextView BodyStats_Plus;
    TextView BodyStats_Minus;
    TextView OtherStats_Plus;
    TextView OtherStats_Minus;

    GDCheckboxItems PreferenceSpinner;
    GDCheckboxItems OralsSpinner;
    GDCheckboxItems LookingforSpinner;
    GDCheckboxItems ToolSizeSpinner;
    GDRadioOptions ToolTypeSpinner;
    GDCheckboxItems SexualOrientationSpinner;

    GDRadioOptions MinHeightSpinner;
    GDRadioOptions MaxHeightSpinner;
    GDRadioOptions MinWeightSpinner;
    GDRadioOptions MaxWeightSpinner;
    GDRadioOptions MinAgeSpinner;
    GDRadioOptions MaxAgeSpinner;
    GDCheckboxItems BodyTypeSpinner;
    GDCheckboxItems TattoosSpinner;
    GDCheckboxItems PiercingsSpinner;
    GDCheckboxItems HairSpinner;
    GDCheckboxItems BodyHairSpinner;
    GDCheckboxItems FacialHairSpinner;

    GDCheckboxItems RelationshipStatusSpinner;
    GDCheckboxItems EthnicitySpinner;
    GDCheckboxItems ReligionSpinner;
    GDCheckboxItems SmokingSpinner;
    GDCheckboxItems DrinkingSpinner;

    Boolean IsImperial = false;

    public AddUserFilter() {
        super("Add Filter");
        IsImperial = PersistantPreferencesHelper.GetAppSettings().UnitSystem.equals("I");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_filter);
        if (getIntent().hasExtra("Mode")) {
            Activity_Mode = getIntent().getExtras().getInt("Mode", ADD_FILTER);
        }
        if (Activity_Mode == EDIT_FILTER) {
            mSavedFilter = getIntent().getExtras().getParcelable("Filter");
            setToolbarText(mSavedFilter.SearchName);
        }
        if (getIntent().hasExtra("IsProfessionalFilter")) {
            IsProfessionalFilter = getIntent().getExtras().getBoolean("IsProfessionalFilter", false);
        }
        HasActions = false;
        ShowTitleWithoutActions = true;
        mContext = AddUserFilter.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);
        InitFields();

        postCreate();
        //hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void InitFields() {
        FilterName = (EditText) findViewById(R.id.FilterName);
        SexualStatsHeader = (LinearLayout) findViewById(R.id.SexualStatsHeader);
        SexualStatsBody = (LinearLayout) findViewById(R.id.SexualStatsBody);
        SexualStats_Plus = (TextView) findViewById(R.id.SexualStats_Plus);
        SexualStats_Minus = (TextView) findViewById(R.id.SexualStats_Minus);
        BodyStatsHeader = (LinearLayout) findViewById(R.id.BodyStatsHeader);
        BodyStatsBody = (LinearLayout) findViewById(R.id.BodyStatsBody);
        BodyStats_Plus = (TextView) findViewById(R.id.BodyStats_Plus);
        BodyStats_Minus = (TextView) findViewById(R.id.BodyStats_Minus);
        OtherStatsHeader = (LinearLayout) findViewById(R.id.OtherStatsHeader);
        OtherStatsBody = (LinearLayout) findViewById(R.id.OtherStatsBody);
        OtherStats_Plus = (TextView) findViewById(R.id.OtherStats_Plus);
        OtherStats_Minus = (TextView) findViewById(R.id.OtherStats_Minus);

        PreferenceSpinner = (GDCheckboxItems) findViewById(R.id.PreferenceSpinner);
        OralsSpinner = (GDCheckboxItems) findViewById(R.id.OralsSpinner);
        LookingforSpinner = (GDCheckboxItems) findViewById(R.id.LookingforSpinner);
        ToolSizeSpinner = (GDCheckboxItems) findViewById(R.id.ToolSizeSpinner);
        ToolTypeSpinner = (GDRadioOptions) findViewById(R.id.ToolTypeSpinner);
        SexualOrientationSpinner = (GDCheckboxItems) findViewById(R.id.SexualOrientationSpinner);

        MinHeightSpinner = (GDRadioOptions) findViewById(R.id.MinHeightSpinner);
        MaxHeightSpinner = (GDRadioOptions) findViewById(R.id.MaxHeightSpinner);
        MinWeightSpinner = (GDRadioOptions) findViewById(R.id.MinWeightSpinner);
        MaxWeightSpinner = (GDRadioOptions) findViewById(R.id.MaxWeightSpinner);
        MinAgeSpinner = (GDRadioOptions) findViewById(R.id.MinAgeSpinner);
        MaxAgeSpinner = (GDRadioOptions) findViewById(R.id.MaxAgeSpinner);
        BodyTypeSpinner = (GDCheckboxItems) findViewById(R.id.BodyTypeSpinner);
        TattoosSpinner = (GDCheckboxItems) findViewById(R.id.TattoosSpinner);
        PiercingsSpinner = (GDCheckboxItems) findViewById(R.id.PiercingsSpinner);
        HairSpinner = (GDCheckboxItems) findViewById(R.id.HairSpinner);
        BodyHairSpinner = (GDCheckboxItems) findViewById(R.id.BodyHairSpinner);
        FacialHairSpinner = (GDCheckboxItems) findViewById(R.id.FacialHairSpinner);

        RelationshipStatusSpinner = (GDCheckboxItems) findViewById(R.id.RelationshipStatusSpinner);
        EthnicitySpinner = (GDCheckboxItems) findViewById(R.id.EthnicitySpinner);
        ReligionSpinner = (GDCheckboxItems) findViewById(R.id.ReligionSpinner);
        SmokingSpinner = (GDCheckboxItems) findViewById(R.id.SmokingSpinner);
        DrinkingSpinner = (GDCheckboxItems) findViewById(R.id.DrinkingSpinner);

        SexualStatsHeader.setTag(true);
        SexualStatsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Boolean) v.getTag()) {
                    v.setTag(false);
                    ShowHideTabs(1, false);
                } else {
                    v.setTag(true);
                    ShowHideTabs(1, true);
                    ShowHideTabs(2, false);
                    ShowHideTabs(3, false);
                    BodyStatsHeader.setTag(false);
                    OtherStatsHeader.setTag(false);
                }
            }
        });
        BodyStatsHeader.setTag(false);
        BodyStatsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Boolean) v.getTag()) {
                    v.setTag(false);
                    ShowHideTabs(2, false);
                } else {
                    v.setTag(true);
                    ShowHideTabs(2, true);
                    ShowHideTabs(1, false);
                    ShowHideTabs(3, false);
                    SexualStatsHeader.setTag(false);
                    OtherStatsHeader.setTag(false);
                }
            }
        });
        OtherStatsHeader.setTag(false);
        OtherStatsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Boolean) v.getTag()) {
                    v.setTag(false);
                    ShowHideTabs(3, false);
                } else {
                    v.setTag(true);
                    ShowHideTabs(3, true);
                    ShowHideTabs(1, false);
                    ShowHideTabs(2, false);
                    SexualStatsHeader.setTag(false);
                    BodyStatsHeader.setTag(false);
                }
            }
        });
        PopulateData();

        List<EditText> TextValidations = new ArrayList<>();
        TextValidations.add(FilterName);
        gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
        gdValidationHelper.AddNonEmptyValidator(0).AddCharRangeValidator(0, 3, 20);
        gdValidationHelper.UpdateFormValidators();
    }

    private void PopulateData() {
        try {
            FilterName.setText(mSavedFilter == null ? "" : mSavedFilter.SearchName);
            Boolean bSetSearchRadius = false;
            if (mSavedFilter != null && !mSavedFilter.SetSearchRadius.equals("")) {
                bSetSearchRadius = Boolean.parseBoolean(mSavedFilter.SetSearchRadius);
            }
            PreferenceSpinner.SetData(mContext, "Sexual Preference", MasterDataHelper.MasterSexualPreference,
                    mSavedFilter == null ? "" : mSavedFilter.SexualPreference, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            OralsSpinner.SetData(mContext, "Orals", MasterDataHelper.MasterSucking,
                    mSavedFilter == null ? "" : mSavedFilter.Sucking, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            LookingforSpinner.SetData(mContext, "Looking for", MasterDataHelper.MasterInterests,
                    mSavedFilter == null ? "" : mSavedFilter.Interests, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            ToolSizeSpinner.SetData(mContext, "Tool Size", MasterDataHelper.MasterToolSize,
                    mSavedFilter == null ? "" : mSavedFilter.ToolSize, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            ToolTypeSpinner.SetData(mContext, "Tool Type", MasterDataHelper.MasterToolType,
                    mSavedFilter == null ? "" : mSavedFilter.ToolType,
                    new OnRadioOptionselected() {
                        @Override
                        public void RadioOptionselected(int position) {
                            //Integer.parseInt(MinHeightSpinner.getTag().toString());
                        }
                    }, null, true);
            SexualOrientationSpinner.SetData(mContext, "Sexual orientation", MasterDataHelper.MasterSexualOrientation,
                    mSavedFilter == null ? "" : mSavedFilter.SexualOrientation, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);

            MinHeightSpinner.SetData(mContext, "Min height", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserHeight, IsImperial),
                    mSavedFilter == null ? "" : mSavedFilter.MinHeight,
                    new OnRadioOptionselected() {
                        @Override
                        public void RadioOptionselected(int position) {
                            //Integer.parseInt(MinHeightSpinner.getTag().toString());
                            //Check, shouldnt be less than max height
                        }
                    }, null, true);
            MaxHeightSpinner.SetData(mContext, "Max height", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserHeight, IsImperial),
                    mSavedFilter == null ? "" : mSavedFilter.MaxHeight,
                    new OnRadioOptionselected() {
                        @Override
                        public void RadioOptionselected(int position) {
                            //Check, shouldnt be less than min height
                        }
                    }, null, true);
            MinWeightSpinner.SetData(mContext, "Min weight", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserWeight, IsImperial),
                    mSavedFilter == null ? "" : mSavedFilter.MinWeight,
                    new OnRadioOptionselected() {
                        @Override
                        public void RadioOptionselected(int position) {
                            //Integer.parseInt(MinHeightSpinner.getTag().toString());
                        }
                    }, null, true);
            MaxWeightSpinner.SetData(mContext, "Max height", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserWeight, IsImperial),
                    mSavedFilter == null ? "" : mSavedFilter.MaxWeight,
                    new OnRadioOptionselected() {
                        @Override
                        public void RadioOptionselected(int position) {
                            //Integer.parseInt(MinHeightSpinner.getTag().toString());
                        }
                    }, null, true);
            MinAgeSpinner.SetData(mContext, "Min age", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserAge, false),
                    mSavedFilter == null ? "" : mSavedFilter.MinAge,
                    new OnRadioOptionselected() {
                        @Override
                        public void RadioOptionselected(int position) {
                            //Integer.parseInt(MinHeightSpinner.getTag().toString());
                        }
                    }, null, true);
            MaxAgeSpinner.SetData(mContext, "Max age", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserAge, false),
                    mSavedFilter == null ? "" : mSavedFilter.MaxAge,
                    new OnRadioOptionselected() {
                        @Override
                        public void RadioOptionselected(int position) {
                            //Integer.parseInt(MinHeightSpinner.getTag().toString());
                        }
                    }, null, true);
            BodyTypeSpinner.SetData(mContext, "Body type", MasterDataHelper.MasterBodyType,
                    mSavedFilter == null ? "" : mSavedFilter.BodyType, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            TattoosSpinner.SetData(mContext, "Tattoos", MasterDataHelper.MasterTattoo,
                    mSavedFilter == null ? "" : mSavedFilter.Tattoos, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            PiercingsSpinner.SetData(mContext, "Piercings", MasterDataHelper.MasterPiercings,
                    mSavedFilter == null ? "" : mSavedFilter.Piercings, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            HairSpinner.SetData(mContext, "Hair", MasterDataHelper.MasterHairType,
                    mSavedFilter == null ? "" : mSavedFilter.Hair, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            BodyHairSpinner.SetData(mContext, "Body hair", MasterDataHelper.MasterBodyHairType,
                    mSavedFilter == null ? "" : mSavedFilter.BodyHair, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            FacialHairSpinner.SetData(mContext, "Facial hair", MasterDataHelper.MasterFacialHairType,
                    mSavedFilter == null ? "" : mSavedFilter.FacialHair, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);

            RelationshipStatusSpinner.SetData(mContext, "Relationship status", MasterDataHelper.MasterRelationshipStatus,
                    mSavedFilter == null ? "" : mSavedFilter.RelationshipStatus, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            EthnicitySpinner.SetData(mContext, "Ethnicity", MasterDataHelper.MasterEthnicity,
                    mSavedFilter == null ? "" : mSavedFilter.Ethnicity, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            ReligionSpinner.SetData(mContext, "Religion", MasterDataHelper.MasterReligion,
                    mSavedFilter == null ? "" : mSavedFilter.Religion, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            SmokingSpinner.SetData(mContext, "Smoking", MasterDataHelper.MasterSmoking,
                    mSavedFilter == null ? "" : mSavedFilter.Smoking, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
            DrinkingSpinner.SetData(mContext, "Drinking", MasterDataHelper.MasterDrinking,
                    mSavedFilter == null ? "" : mSavedFilter.Drinking, null, new OnDialogButtonClick() {
                        @Override
                        public void dialogButtonClicked() {

                        }
                    }, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            finish();
        }
    }

    private void ShowHideTabs(int tabOrder, Boolean Show) {
        switch (tabOrder) {
            case 1:
                if (Show) {
                    SexualStatsBody.setVisibility(View.VISIBLE);
                    SexualStats_Plus.setVisibility(View.GONE);
                    SexualStats_Minus.setVisibility(View.VISIBLE);
                } else {
                    SexualStatsBody.setVisibility(View.GONE);
                    SexualStats_Plus.setVisibility(View.VISIBLE);
                    SexualStats_Minus.setVisibility(View.GONE);
                }
                break;
            case 2:
                if (Show) {
                    BodyStatsBody.setVisibility(View.VISIBLE);
                    BodyStats_Plus.setVisibility(View.GONE);
                    BodyStats_Minus.setVisibility(View.VISIBLE);
                } else {
                    BodyStatsBody.setVisibility(View.GONE);
                    BodyStats_Plus.setVisibility(View.VISIBLE);
                    BodyStats_Minus.setVisibility(View.GONE);
                }
                break;
            case 3:
                if (Show) {
                    OtherStatsBody.setVisibility(View.VISIBLE);
                    OtherStats_Plus.setVisibility(View.GONE);
                    OtherStats_Minus.setVisibility(View.VISIBLE);
                } else {
                    OtherStatsBody.setVisibility(View.GONE);
                    OtherStats_Plus.setVisibility(View.VISIBLE);
                    OtherStats_Minus.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void SaveFilter() {
        if (!gdValidationHelper.Validate()) {
            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.validation_error), TopSnackBar.LENGTH_SHORT, true).show();
            if (gdValidationHelper.GetFirstErrorPosition() > 1) {
                OtherStatsHeader.setTag(true);
                ShowHideTabs(3, true);
                ShowHideTabs(1, false);
                ShowHideTabs(2, false);
                SexualStatsHeader.setTag(false);
                BodyStatsHeader.setTag(false);
            }
            return;
        }
        SetSelectedDataToObject();
        APICallInfo apiCallInfo = new APICallInfo("Search", "SaveDetailedSearch", null, "POST", mSavedFilter, null, false,
                new APIProgress(mContext, "Saving. Please wait..", true), APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                    if (successResult.SuccessResult == 1) {
                        FinishActivityWithSuccess(false);
                    } else if (successResult.SuccessResult == Integer.parseInt(getResources().getString(R.string.user_limit_error_code))) {
                        GDGenericHelper.ShowBuyPremiumIfNotPremium(mContext, successResult.FailureMessage, true);
                    } else {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), successResult.FailureMessage, TopSnackBar.LENGTH_SHORT, true).show();
                    }
                } catch (Exception e) {
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

    private void SetSelectedDataToObject() {
        try {
            if (mSavedFilter == null) {
                mSavedFilter = new SavedFilter();
                mSavedFilter.UserID = LoggedInUser.UserID;
                mSavedFilter.SearchID = GDGenericHelper.GetNewGUID();
                mSavedFilter.SearchType = IsProfessionalFilter ? "P" : "U";
                //Below fields are not used in mobile
                mSavedFilter.SortByCode = "D";
                mSavedFilter.WithPics = "false";
                mSavedFilter.CurrentlyOnline = "false";
                mSavedFilter.MapLocationVisible = "false";
            }
            mSavedFilter.SetSearchRadius = SearchRadiusActive.toString();
            mSavedFilter.SearchRadiusMin = Integer.toString(0);
            mSavedFilter.SearchRadiusMax = Integer.toString(0);
            mSavedFilter.SearchName = FilterName.getText().toString();
            mSavedFilter.MinHeight = MinHeightSpinner.getTag().toString().equals("") ? "0" : MinHeightSpinner.getTag().toString();
            mSavedFilter.MaxHeight = MaxHeightSpinner.getTag().toString().equals("") ? "0" : MaxHeightSpinner.getTag().toString();
            mSavedFilter.MinWeight = MinWeightSpinner.getTag().toString().equals("") ? "0" : MinWeightSpinner.getTag().toString();
            mSavedFilter.MaxWeight = MaxWeightSpinner.getTag().toString().equals("") ? "0" : MaxWeightSpinner.getTag().toString();
            mSavedFilter.MinAge = MinAgeSpinner.getTag().toString().equals("") ? "0" : MinAgeSpinner.getTag().toString();
            mSavedFilter.MaxAge = MaxAgeSpinner.getTag().toString().equals("") ? "0" : MaxAgeSpinner.getTag().toString();
            mSavedFilter.SexualPreference = PreferenceSpinner.getTag().toString();
            mSavedFilter.Sucking = OralsSpinner.getTag().toString();
            mSavedFilter.Interests = LookingforSpinner.getTag().toString();
            mSavedFilter.BodyType = BodyTypeSpinner.getTag().toString();
            mSavedFilter.Ethnicity = EthnicitySpinner.getTag().toString();
            mSavedFilter.Religion = ReligionSpinner.getTag().toString();
            mSavedFilter.Hair = HairSpinner.getTag().toString();
            mSavedFilter.BodyHair = BodyHairSpinner.getTag().toString();
            mSavedFilter.FacialHair = FacialHairSpinner.getTag().toString();
            mSavedFilter.Tattoos = TattoosSpinner.getTag().toString();
            mSavedFilter.Piercings = PiercingsSpinner.getTag().toString();
            mSavedFilter.ToolSize = ToolSizeSpinner.getTag().toString();
            mSavedFilter.ToolType = ToolTypeSpinner.getTag().toString();
            mSavedFilter.Smoking = SmokingSpinner.getTag().toString();
            mSavedFilter.Drinking = DrinkingSpinner.getTag().toString();
            mSavedFilter.SexualOrientation = SexualOrientationSpinner.getTag().toString();
            mSavedFilter.RelationshipStatus = RelationshipStatusSpinner.getTag().toString();
            mSavedFilter.TagLineContains = "";
            mSavedFilter.DetailedDescContains = "";
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private void DeleteFilter() {
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_SearchID, mSavedFilter.SearchID));
        APICallInfo apiCallInfo = new APICallInfo("Search", "DeleteDetailedSearch", pAPICallParameters, "GET", null, null, false,
                new APIProgress(mContext, "Deleting. Please wait..", true), APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result.equals("1")) {
                        FinishActivityWithSuccess(true);
                    } else {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                    }
                } catch (Exception e) {
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

    private void FinishActivityWithSuccess(Boolean IsDelete) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("Filter", mSavedFilter);
        if (IsDelete) {
            returnIntent.putExtra("Delete", 1);
        }
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_user_filter, menu);
        if (mSavedFilter == null || mSavedFilter.SearchID.equals("")) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            SaveFilter();
            return true;
        } else if (id == R.id.action_delete) {
            GDDialogHelper.ShowYesNoTypeDialog(mContext, "Delete filter", "Are you sure you want to delete this filter?", GDDialogHelper.BUTTON_TEXT_DELETE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT, new OnDialogButtonClick() {
                @Override
                public void dialogButtonClicked() {
                    DeleteFilter();
                }
            }, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
