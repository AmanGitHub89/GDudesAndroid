package com.gdudes.app.gdudesapp.activities.Profile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APICalls.PicsAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.BillingUtil.GetPremiumActivity;
import com.gdudes.app.gdudesapp.BillingUtil.PurchaseMonthOptionsActivity;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDCheckboxItems;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDRadioOptions;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.CompleteUserProfile;
import com.gdudes.app.gdudesapp.GDTypes.GDFullImage;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.GDTypes.GeneralStats;
import com.gdudes.app.gdudesapp.GDTypes.SexualStats;
import com.gdudes.app.gdudesapp.GDTypes.SocialStats;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.CompleteProfileRepository;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDUnitHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.MasterDataHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.APIFailureCallback;
import com.gdudes.app.gdudesapp.Interfaces.APISuccessCallback;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.Interfaces.OnRadioOptionselected;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDMapActivity;
import com.gdudes.app.gdudesapp.activities.MainLayout.HomePageFragment;
import com.gdudes.app.gdudesapp.activities.MessageWindow;
import com.gdudes.app.gdudesapp.activities.Pics.GDPicViewerActivity;
import com.gdudes.app.gdudesapp.activities.Pics.ManagePicsActivity;
import com.gdudes.app.gdudesapp.activities.Settings.ReportIssueMakeSugesstionActivity;
import com.gdudes.app.gdudesapp.activities.Settings.SettingsActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import gdudesapp.GDFloatingActionButton.FloatingActionButton;

public class NewProfileViewActivity extends GDAppCompatActivity {

    Toolbar toolbar;
    TextView toolbarTitleAge;
    ImageView ProfilePic;
    TextView lblHeightWeight;
    TextView lblDistance;
    TextView TagLine;
    TextView EmailID;
    TextView LastSeen;
    int ReportUserSelectedOption = 0;
    Menu mMenu;

    FrameLayout TitleFrameLayout;
    ImageView FABBlockUser;
    ImageView FABFavorite;
    ImageView FABChat;
    ImageView FABIceBreaker;
    ImageView FABEdit;
    ImageView FABPremiumUser;
    ImageView OnlineIndicator;
    ImageView MapLocation;
    ImageView InstagramLink;
    RelativeLayout ProfilePicsContainer;
    LinearLayout PublicProfilePicsScroll;
    Button btnMyPhotos;
    ImageView MySettings;
    LinearLayout MyPhotoBeingReviewed;

    private Boolean IsProfileOwner;
    private Context mContext;
    private Users LoggedInUser;
    private Users ClickedUser = null;
    private String ClickedUserID = "";
    private Boolean ProfilePicSet = false;
    private Boolean LargeProfilePicSet = false;
    private Boolean IsActivityInView = false;
    LinearLayout NestedScrollContent;

    Boolean IsRegistrationFirstEdit = false;
    String mProfilePicID = "";
    CompleteUserProfile mCompleteUserProfile;
    ArrayList<GDPic> mProfilePicsList = new ArrayList<>();
    Boolean AreFieldsInstantiated = false;

    //Details Section - START
    int ExpandedEditSection = 0;
    GDValidationHelper gdValidationHelper;

    LinearLayout GeneralStatsHeader;
    LinearLayout SexualStatsHeader;
    LinearLayout SocialStatsHeader;
    LinearLayout DescriptionHeader;
    LinearLayout GeneralStatsBody;
    LinearLayout SexualStatsBody;
    LinearLayout SocialStatsBody;
    LinearLayout DescriptionBody;
    TextView GeneralStats_Plus;
    TextView GeneralStats_Minus;
    TextView SexualStats_Plus;
    TextView SexualStats_Minus;
    TextView SocialStats_Plus;
    TextView SocialStats_Minus;
    TextView Description_Plus;
    TextView Description_Minus;
    ImageView GeneralStats_Edit;
    ImageView SexualStats_Edit;
    ImageView SocialStats_Edit;
    ImageView Description_Edit;

    TextView Description;
    TextView Waist;
    TextView BodyType;
    TextView Ethnicity;
    TextView Hair;
    TextView BodyHair;
    TextView FacialHair;
    TextView Tattoo;
    TextView Piercings;

    TextView SexualOrientation;
    TextView SexualPreference;
    TextView ToolSize;
    TextView ToolType;
    TextView Oral;
    TextView SAndM;
    TextView LookingFor;
    TextView Fetish;

    TextView Profession;
    TextView Religion;
    TextView Smoking;
    TextView Drinking;
    TextView RelationshipStatus;
    TextView LanguagesKnown;

    TableRow WaistRow;
    TableRow BodyTypeRow;
    TableRow EthnicityRow;
    TableRow HairRow;
    TableRow BodyHairRow;
    TableRow FacialHairRow;
    TableRow TattooRow;
    TableRow PiercingsRow;
    TableRow SexualOrientationRow;
    TableRow SexualPreferenceRow;
    TableRow ToolSizeRow;
    TableRow ToolTypeRow;
    TableRow OralRow;
    TableRow SAndMRow;
    TableRow LookingForRow;
    TableRow FetishRow;
    TableRow ProfessionRow;
    TableRow ReligionRow;
    TableRow SmokingRow;
    TableRow DrinkingRow;
    TableRow RelationshipStatusRow;
    TableRow LanguagesKnownRow;

    GDRadioOptions WaistSizeSpinner;
    GDRadioOptions BodyTypeSpinner;
    GDRadioOptions EthnicitySpinner;
    GDRadioOptions HairSpinner;
    GDRadioOptions BodyHairSpinner;
    GDRadioOptions FacialHairSpinner;
    GDRadioOptions TattooSpinner;
    GDRadioOptions PiercingsSpinner;
    GDRadioOptions SexualOrientationSpinner;
    GDRadioOptions SexualPreferenceSpinner;
    GDRadioOptions ToolSizeSpinner;
    GDRadioOptions ToolTypeSpinner;
    GDRadioOptions OralSpinner;
    GDRadioOptions SAndMSpinner;
    GDCheckboxItems LookingForSpinner;
    GDCheckboxItems FetishSpinner;
    GDRadioOptions ProfessionSpinner;
    GDRadioOptions ReligionSpinner;
    GDRadioOptions SmokingSpinner;
    GDRadioOptions DrinkingSpinner;
    GDRadioOptions RelationshipStatusSpinner;
    GDCheckboxItems LanguagesKnownSpinner;
    EditText DescriptionEditText;

    LinearLayout GeneralStatsBottomLine;
    LinearLayout SexualStatsBottomLine;
    LinearLayout SocialStatsBottomLine;
    //Details Section - END

    FloatingActionButton PopularGuysFAB;
    TextView FabRankText;
    RelativeLayout BodyBelowAppBar;
    Boolean IsAppBarCollapsed = false;
    NestedScrollView PNestedScrollView;
    Boolean PNestedScrollViewPaddingSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_new_profile_view);
            mContext = NewProfileViewActivity.this;
            LoggedInUser = SessionManager.GetLoggedInUser(mContext);

            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(null);

            ReportUserSelectedOption = 0;

            AppBarLayout appBarLayout = findViewById(R.id.appbar);
            BodyBelowAppBar = findViewById(R.id.BodyBelowAppBar);
            PNestedScrollView = findViewById(R.id.PNestedScrollView);
            appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
                if (verticalOffset == 0) {
                    IsAppBarCollapsed = false;
                    PNestedScrollView.setPadding(0, 0, 0, 0);
                    BodyBelowAppBar.setPadding(0, 0, 0, 0);
                    PNestedScrollViewPaddingSet = false;
                } else {
                    IsAppBarCollapsed = true;
                    if (!PNestedScrollViewPaddingSet) {
                        PNestedScrollView.setPadding(0, getSupportActionBar().getHeight(), 0, 0);
                        BodyBelowAppBar.setPadding(0, getSupportActionBar().getHeight(), 0, 0);
                    }
                }
            });

            PopularGuysFAB = findViewById(R.id.PopularGuysFAB);
            PopularGuysFAB.setOnClickListener(v -> {
                if (mCompleteUserProfile == null) {
                    return;
                }
                if (LoggedInUser.UserID.trim().equalsIgnoreCase(mCompleteUserProfile.UserID.trim())) {
                    GDDialogHelper.ShowSingleButtonTypeDialog(NewProfileViewActivity.this, "Yay! You are popular today.",
                            "You have been ranked " + Integer.toString(mCompleteUserProfile.PopularityRank)
                                    + " on the most popular list on GDudes today.\n\nCongrats!",
                            GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.INFO, null);
                } else {
                    GDDialogHelper.ShowSingleButtonTypeDialog(NewProfileViewActivity.this, "He is popular today!",
                            "He has been ranked " + Integer.toString(mCompleteUserProfile.PopularityRank)
                                    + " on the most popular list on GDudes today.",
                            GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.INFO, null);
                }
            });

            LoadActivityForIntent(getIntent());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void LoadActivityForIntent(Intent intent) {
        try {
            if (intent.hasExtra("ClickedUser")) {
                ClickedUser = (Users) intent.getExtras().get("ClickedUser");
                ClickedUserID = ClickedUser.UserID;
            } else {
                ClickedUserID = intent.getExtras().getString("ClickedUserID", "");
                if (ClickedUserID == null || ClickedUserID.equals("")) {
                    finish();
                }
            }
            IsProfileOwner = LoggedInUser.UserID.equalsIgnoreCase(ClickedUserID);
            btnMyPhotos = findViewById(R.id.btnMyPhotos);
            if (IsProfileOwner) {
                if (intent.hasExtra("IsRegistrationFirstEdit")) {
                    IsRegistrationFirstEdit = intent.getBooleanExtra("IsRegistrationFirstEdit", false);
                    if (IsRegistrationFirstEdit) {
                        GDDialogHelper.ShowSingleButtonTypeDialog(mContext, "Edit profile", "This is your profile. Have a look & add more info if you like.",
                                GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.INFO, null);
                    }
                }
                btnMyPhotos.setVisibility(View.VISIBLE);
                btnMyPhotos.setOnClickListener(v -> {
                    Intent managePicsIntent = new Intent(NewProfileViewActivity.this, ManagePicsActivity.class);
                    startActivity(managePicsIntent);
                });
            }

            NestedScrollContent = findViewById(R.id.NestedScrollContent);
            ProfilePic = findViewById(R.id.ProfilePic);
            NestedScrollContent.setVisibility(View.INVISIBLE);
            if (intent.hasExtra("ProfilePicID")) {
                String profilePicID = intent.getExtras().getString("ProfilePicID", "");
                if (StringHelper.IsNullOrEmpty(profilePicID)) {
                    mProfilePicID = profilePicID;
                    LoadProfilePic();
                }
            }

            GetData();
        } catch (Exception ex) {
            TopSnackBar.MakeSnackBar(BodyBelowAppBar, "Error while loading profile", TopSnackBar.LENGTH_SHORT, true).show();
            GDLogHelper.LogException(ex);
        }
    }

    private void GetData() {
        CompleteUserProfile profile = CompleteProfileRepository.GetProfile(ClickedUserID);
        if (profile !=null && profile.UserID != null && !profile.UserID.trim().equals("")) {
            SetProfileData(profile);
            ShowProfileData();
            return;
        }

        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, ClickedUserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_RequestingUserID, LoggedInUser.UserID));
        APICallInfo apiCallInfo = new APICallInfo("Home", "GetCompleteProfile", pAPICallParameters, "GET", null, null, false,
                new APIProgress(mContext, "Loading profile..", true), APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(NewProfileViewActivity.this, apiCallInfo, (result, ExtraData) -> {
            try {
                if (result == null || result.equals("") || result.equals("-1")) {
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, "Error while loading profile", TopSnackBar.LENGTH_SHORT, true).show();
                    return;
                }
                JSONObject jsonObject = new JSONObject(result);
                String sCompleteUserProfile = jsonObject.getString("CompleteProfile");
                String sSuccessResult = jsonObject.getString("SuccessResult");

                SuccessResult successResult = new GsonBuilder().create().fromJson(StringHelper.TrimFirstAndLastCharacter(sSuccessResult), SuccessResult.class);
                CompleteUserProfile oCompleteUserProfile = new GsonBuilder().create().fromJson(StringHelper.TrimFirstAndLastCharacter(sCompleteUserProfile), CompleteUserProfile.class);
                SetProfileData(oCompleteUserProfile);

                if (mCompleteUserProfile != null && mCompleteUserProfile.UserID != null && !mCompleteUserProfile.UserID.trim().equals("")) {
                    if (!mCompleteUserProfile.UserID.equalsIgnoreCase(LoggedInUser.UserID)) {
                        CompleteProfileRepository.AddToCache(mCompleteUserProfile);
                    }
                    ShowProfileData();
                } else {
                    if (successResult != null && successResult.SuccessResult == -102) {
                        ShowDialogForBlock(successResult.FailureMessage, false);
                    } else if (successResult != null && successResult.SuccessResult == -103) {
                        ShowDialogForBlock(successResult.FailureMessage, true);
                    } else if (successResult != null && successResult.SuccessResult == -104) {
                        ShowDialogForBlock(successResult.FailureMessage, false);
                    } else {
                        if (successResult.FailureMessage.trim() == "") {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, "Error while loading profile. Please try again.", TopSnackBar.LENGTH_SHORT, true).show();
                        } else {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, successResult.FailureMessage, TopSnackBar.LENGTH_LONG, true).show();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
                TopSnackBar.MakeSnackBar(BodyBelowAppBar, "Error while loading profile. Please try again.", TopSnackBar.LENGTH_SHORT, true).show();
            }
        }, () -> TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show());
    }

    private void InitFields() {
        if (!AreFieldsInstantiated) {
            toolbarTitleAge = findViewById(R.id.toolbarTitleAge);
            lblHeightWeight = findViewById(R.id.lblHeightWeight);
            lblDistance = findViewById(R.id.lblDistance);
            TagLine = findViewById(R.id.TagLine);
            LastSeen = findViewById(R.id.LastSeen);
            TitleFrameLayout = findViewById(R.id.TitleFrameLayout);
            FABBlockUser = findViewById(R.id.FABBlockUser);
            FABFavorite = findViewById(R.id.FABFavorite);
            FABChat = findViewById(R.id.FABChat);
            FABIceBreaker = findViewById(R.id.FABIceBreaker);
            FABEdit = findViewById(R.id.FABEdit);
            FABPremiumUser = findViewById(R.id.FABPremiumUser);
            OnlineIndicator = findViewById(R.id.OnlineIndicator);
            MySettings = findViewById(R.id.MySettings);
            MyPhotoBeingReviewed = findViewById(R.id.MyPhotoBeingReviewed);
            MapLocation = findViewById(R.id.MapLocation);
            InstagramLink = findViewById(R.id.InstagramLink);
            ProfilePicsContainer = findViewById(R.id.ProfilePicsContainer);
            PublicProfilePicsScroll = findViewById(R.id.PublicProfilePicsScroll);
            EmailID = findViewById(R.id.EmailID);

            //Details Section - START
            GeneralStatsHeader = findViewById(R.id.GeneralStatsHeader);
            SexualStatsHeader = findViewById(R.id.SexualStatsHeader);
            SocialStatsHeader = findViewById(R.id.SocialStatsHeader);
            DescriptionHeader = findViewById(R.id.DescriptionHeader);
            GeneralStatsBody = findViewById(R.id.GeneralStatsBody);
            SexualStatsBody = findViewById(R.id.SexualStatsBody);
            SocialStatsBody = findViewById(R.id.SocialStatsBody);
            DescriptionBody = findViewById(R.id.DescriptionBody);
            GeneralStats_Plus = findViewById(R.id.GeneralStats_Plus);
            GeneralStats_Minus = findViewById(R.id.GeneralStats_Minus);
            SexualStats_Plus = findViewById(R.id.SexualStats_Plus);
            SexualStats_Minus = findViewById(R.id.SexualStats_Minus);
            SocialStats_Plus = findViewById(R.id.SocialStats_Plus);
            SocialStats_Minus = findViewById(R.id.SocialStats_Minus);
            Description_Plus = findViewById(R.id.Description_Plus);
            Description_Minus = findViewById(R.id.Description_Minus);

            Waist = findViewById(R.id.Waist);
            BodyType = findViewById(R.id.BodyType);
            Ethnicity = findViewById(R.id.Ethnicity);
            Hair = findViewById(R.id.Hair);
            BodyHair = findViewById(R.id.BodyHair);
            FacialHair = findViewById(R.id.FacialHair);
            Tattoo = findViewById(R.id.Tattoo);
            Piercings = findViewById(R.id.Piercings);
            SexualOrientation = findViewById(R.id.SexualOrientation);
            SexualPreference = findViewById(R.id.SexualPreference);
            ToolSize = findViewById(R.id.ToolSize);
            ToolType = findViewById(R.id.ToolType);
            Oral = findViewById(R.id.Oral);
            SAndM = findViewById(R.id.SAndM);
            LookingFor = findViewById(R.id.LookingFor);
            Fetish = findViewById(R.id.Fetish);
            Profession = findViewById(R.id.Profession);
            Religion = findViewById(R.id.Religion);
            Smoking = findViewById(R.id.Smoking);
            Drinking = findViewById(R.id.Drinking);
            RelationshipStatus = findViewById(R.id.RelationshipStatus);
            LanguagesKnown = findViewById(R.id.LanguagesKnown);
            Description = findViewById(R.id.Description);

            WaistRow = findViewById(R.id.WaistSizeRow);
            BodyTypeRow = findViewById(R.id.BodyTypeRow);
            EthnicityRow = findViewById(R.id.EthnicityRow);
            HairRow = findViewById(R.id.HairRow);
            BodyHairRow = findViewById(R.id.BodyHairRow);
            FacialHairRow = findViewById(R.id.FacialHairRow);
            TattooRow = findViewById(R.id.TattooRow);
            PiercingsRow = findViewById(R.id.PiercingsRow);
            SexualOrientationRow = findViewById(R.id.SexualOrientationRow);
            SexualPreferenceRow = findViewById(R.id.SexualPreferenceRow);
            ToolSizeRow = findViewById(R.id.ToolSizeRow);
            ToolTypeRow = findViewById(R.id.ToolTypeRow);
            OralRow = findViewById(R.id.OralRow);
            SAndMRow = findViewById(R.id.SAndMRow);
            LookingForRow = findViewById(R.id.LookingForRow);
            FetishRow = findViewById(R.id.FetishRow);
            ProfessionRow = findViewById(R.id.ProfessionRow);
            ReligionRow = findViewById(R.id.ReligionRow);
            SmokingRow = findViewById(R.id.SmokingRow);
            DrinkingRow = findViewById(R.id.DrinkingRow);
            RelationshipStatusRow = findViewById(R.id.RelationshipStatusRow);
            LanguagesKnownRow = findViewById(R.id.LanguagesKnownRow);

            GeneralStatsBottomLine = findViewById(R.id.GeneralStatsBottomLine);
            SexualStatsBottomLine = findViewById(R.id.SexualStatsBottomLine);
            SocialStatsBottomLine = findViewById(R.id.SocialStatsBottomLine);

            AreFieldsInstantiated = true;
        }

        if (IsProfileOwner) {
            GeneralStats_Edit = findViewById(R.id.GeneralStats_Edit);
            SexualStats_Edit = findViewById(R.id.SexualStats_Edit);
            SocialStats_Edit = findViewById(R.id.SocialStats_Edit);
            Description_Edit = findViewById(R.id.Description_Edit);
            GeneralStats_Edit.setVisibility(View.VISIBLE);
            SexualStats_Edit.setVisibility(View.VISIBLE);
            SocialStats_Edit.setVisibility(View.VISIBLE);
            Description_Edit.setVisibility(View.VISIBLE);

            WaistSizeSpinner = findViewById(R.id.WaistSizeSpinner);
            BodyTypeSpinner = findViewById(R.id.BodyTypeSpinner);
            EthnicitySpinner = findViewById(R.id.EthnicitySpinner);
            HairSpinner = findViewById(R.id.HairSpinner);
            BodyHairSpinner = findViewById(R.id.BodyHairSpinner);
            FacialHairSpinner = findViewById(R.id.FacialHairSpinner);
            TattooSpinner = findViewById(R.id.TattooSpinner);
            PiercingsSpinner = findViewById(R.id.PiercingsSpinner);
            SexualOrientationSpinner = findViewById(R.id.SexualOrientationSpinner);
            SexualPreferenceSpinner = findViewById(R.id.SexualPreferenceSpinner);
            ToolSizeSpinner = findViewById(R.id.ToolSizeSpinner);
            ToolTypeSpinner = findViewById(R.id.ToolTypeSpinner);
            OralSpinner = findViewById(R.id.OralSpinner);
            SAndMSpinner = findViewById(R.id.SAndMSpinner);
            LookingForSpinner = findViewById(R.id.LookingForSpinner);
            FetishSpinner = findViewById(R.id.FetishSpinner);
            ProfessionSpinner = findViewById(R.id.ProfessionSpinner);
            ReligionSpinner = findViewById(R.id.ReligionSpinner);
            SmokingSpinner = findViewById(R.id.SmokingSpinner);
            DrinkingSpinner = findViewById(R.id.DrinkingSpinner);
            RelationshipStatusSpinner = findViewById(R.id.RelationshipStatusSpinner);
            LanguagesKnownSpinner = findViewById(R.id.LanguagesKnownSpinner);
            DescriptionEditText = findViewById(R.id.DescriptionEditText);

            List<EditText> TextValidations = new ArrayList<>();
            TextValidations.add(DescriptionEditText);
            gdValidationHelper = new GDValidationHelper(mContext, TextValidations);
            gdValidationHelper.AddNonEmptyValidator(0).AddCharRangeValidator(0, 20, 2000);
            gdValidationHelper.UpdateFormValidators();
            //Details Section - END
        }
    }


    private void SetProfileData(CompleteUserProfile profile) {
        mCompleteUserProfile = profile;
        mProfilePicID = mCompleteUserProfile.ProfilePicID;
    }

    private void ShowProfileData() {
        NestedScrollContent.setVisibility(View.VISIBLE);
        InitFields();
        toolbar.setTitle(mCompleteUserProfile.GetDecodedUserName());
        if (mCompleteUserProfile.Age != 0) {
            toolbarTitleAge.setText(Integer.toString(mCompleteUserProfile.Age));
        }
        SetHeightWeightDistance();
        SetFabsVisibility();
        if (IsProfileOwner) {
            EmailID.setVisibility(View.VISIBLE);
            EmailID.setText(mCompleteUserProfile.Email);
        } else {
            EmailID.setVisibility(View.GONE);
        }

        TagLine.setText(StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.TagLine));

        SetInstagramButton();
        SetEvents();
        SetDetailsFieldsData();
        GetPicsInProfile();
        if (IsActivityInView && !LargeProfilePicSet && mCompleteUserProfile != null && !StringHelper.IsNullOrEmpty(mProfilePicID)) {
            LoadProfilePic();
        }
    }

    private void SetEvents() {
        ProfilePic.setOnClickListener(v -> {
            if (mCompleteUserProfile ==  null || StringHelper.IsNullOrEmpty(mProfilePicID)) {
                return;
            }
            ArrayList<GDPic> pics;
            if (mProfilePicsList.size() > 1 && IsFirstPublicPicProfilePic()) {
                //If public pics are available and Profile pic is first of them (it should be), then open all.
                pics =  mProfilePicsList;
            } else {
                pics = new ArrayList<>();
                pics.add(new GDPic(mProfilePicID, mCompleteUserProfile.UserID, true, "", "", true));
            }
            Intent intent = new Intent(mContext, GDPicViewerActivity.class);
            intent.putParcelableArrayListExtra("GDPicList", pics);
            intent.putExtra("SelectedPic", 0);
            startActivity(intent);
        });
        MapLocation.setOnClickListener(v -> {
            if (mCompleteUserProfile ==  null) {
                return;
            }
            final Intent intent = new Intent(mContext, GDMapActivity.class);
            intent.putExtra("Activity_Mode", GDMapActivity.VIEW_USER_LOCATION);
            intent.putExtra("LocationOwnerUserID", mCompleteUserProfile.UserID);
            intent.putExtra("LocationOwnerName", mCompleteUserProfile.GetDecodedUserName());
            intent.putExtra("LocationOwnerPicID", mCompleteUserProfile.ProfilePicID);
            intent.putExtra("LocationLatLng", mCompleteUserProfile.LatLng);
            if(!mCompleteUserProfile.UserID.equalsIgnoreCase(LoggedInUser.UserID)) {
                if (!mCompleteUserProfile.ShowInMapSearch) {
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, "He is currently not sharing his map location.", TopSnackBar.LENGTH_LONG, true).show();
                    return;
                }
                if (!mCompleteUserProfile.MyShowInMapSearch) {
                    ShowMapPrivacyAlert(new APISuccessCallback() {
                        @Override
                        public void onSuccess(Object data, Object ExtraData) {
                            mContext.startActivity(intent);
                        }
                    });
                    return;
                }
            }
            mContext.startActivity(intent);
        });
        FABBlockUser.setOnClickListener(v -> {
            if (mCompleteUserProfile ==  null) {
                return;
            }
            String username = StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.UserName);
            String title = "Block " + username + "?";
            String message = "Are you sure you want to block him? You both will not be able to contact each other.";
            GDDialogHelper.ShowYesNoTypeDialog(mContext, title, message,
                    GDDialogHelper.BUTTON_TEXT_BLOCK, GDDialogHelper.BUTTON_TEXT_CANCEL,
                    GDDialogHelper.ALERT, new OnDialogButtonClick() {
                @Override
                public void dialogButtonClicked() {
                    BlockUnblock(true);
                }
            }, null);
        });
        FABFavorite.setOnClickListener(v -> {
            if (mCompleteUserProfile ==  null) {
                return;
            }
            AddRemoveFavorite();
        });
        FABChat.setOnClickListener(v -> {
            if (mCompleteUserProfile ==  null) {
                return;
            }
            Users ClickedUser = new Users(mCompleteUserProfile.UserID);
            ClickedUser.PicID = mProfilePicID;
            Intent intent = new Intent(mContext, MessageWindow.class);
            intent.putExtra("ConvWithUserID", mCompleteUserProfile.UserID);
            intent.putExtra("ConvWithUserName", mCompleteUserProfile.GetDecodedUserName());
            intent.putExtra("ConvWithUserPicID", mProfilePicID);
            //intent.putExtra("ConvWithUser", ClickedUser);
            startActivity(intent);
        });
        FABIceBreaker.setOnClickListener(v -> {
            if (mCompleteUserProfile ==  null) {
                return;
            }
            GDGenericHelper.ShowIceBreakerModal(NewProfileViewActivity.this, mCompleteUserProfile.UserID);
        });
        FABEdit.setOnClickListener(v -> {
            if (mCompleteUserProfile ==  null) {
                return;
            }
            Intent intent = new Intent(mContext, ProfileEditActivity.class);
            intent.putExtra("CompleteUserProfile", mCompleteUserProfile);
            startActivityForResult(intent, 1);
        });
        FABPremiumUser.setOnClickListener(v -> {
            if (mCompleteUserProfile ==  null) {
                return;
            }
            if (mCompleteUserProfile.IsPremium && mCompleteUserProfile.EndsOnDate != null && !mCompleteUserProfile.EndsOnDate.trim().equals("")) {
                String ActiveTill = GDDateTimeHelper.GetDateOnlyStringFromDate(GDDateTimeHelper.GetDateFromString(mCompleteUserProfile.EndsOnDate), true);
                GDDialogHelper.ShowThreeButtonTypeDialog(mContext, "You are a GDudes Premium User.", "Premium ends on: " + ActiveTill + ".\nDo you wish to extend Premium?",
                        GDDialogHelper.BUTTON_TEXT_YES, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.BUTTON_TEXT_PREMIUM_BENEFITS, GDDialogHelper.INFO,
                        () -> {
                            Intent intent = new Intent(NewProfileViewActivity.this, PurchaseMonthOptionsActivity.class);
                            intent.putExtra("SKUTypeCode", "UP"); //User Premium
                            startActivityForResult(intent, 11);
                        }, null
                        , () -> {
                            ShowPremiumInfoActivity();
                        });
            } else {
                ShowPremiumInfoActivity();
            }
        });

        //Details Section - START
        GeneralStatsHeader.setTag(false);
        SexualStatsHeader.setTag(true);
        SocialStatsHeader.setTag(false);
        DescriptionHeader.setTag(true);
        GeneralStatsBody.setVisibility(View.GONE);
        GeneralStats_Plus.setVisibility(View.VISIBLE);
        GeneralStats_Minus.setVisibility(View.GONE);
        SocialStatsBody.setVisibility(View.GONE);
        SocialStats_Plus.setVisibility(View.VISIBLE);
        SocialStats_Minus.setVisibility(View.GONE);

        GeneralStatsHeader.setOnClickListener(v -> {
            if ((Boolean) v.getTag()) {
                ShowHideSection(1, true);
            } else {
                ShowHideSection(1, false);
            }
        });

        SexualStatsHeader.setOnClickListener(v -> {
            if ((Boolean) v.getTag()) {
                ShowHideSection(2, true);
            } else {
                ShowHideSection(2, false);
            }
        });

        SocialStatsHeader.setOnClickListener(v -> {
            if ((Boolean) v.getTag()) {
                ShowHideSection(3, true);
            } else {
                ShowHideSection(3, false);
            }
        });

        DescriptionHeader.setOnClickListener(v -> {
            if ((Boolean) v.getTag()) {
                ShowHideSection(4, true);
            } else {
                ShowHideSection(4, false);
            }
        });

        if (IsProfileOwner) {
            GeneralStats_Edit.setTag(true);
            SexualStats_Edit.setTag(true);
            SocialStats_Edit.setTag(true);
            Description_Edit.setTag(true);
            GeneralStats_Edit.setOnClickListener(v -> {
                if ((Boolean) v.getTag()) {
                    if (ExpandedEditSection > 0) {
                        ShowHideEditSections(ExpandedEditSection, false);
                    }
                    ShowHideEditSections(1, true);
                    ShowHideSection(1, false);
                } else {
                    ShowHideEditSections(1, false);
                }
            });
            SexualStats_Edit.setOnClickListener(v -> {
                if ((Boolean) v.getTag()) {
                    if (ExpandedEditSection > 0) {
                        ShowHideEditSections(ExpandedEditSection, false);
                    }
                    ShowHideEditSections(2, true);
                    ShowHideSection(2, false);
                } else {
                    ShowHideEditSections(2, false);
                }
            });
            SocialStats_Edit.setOnClickListener(v -> {
                if ((Boolean) v.getTag()) {
                    if (ExpandedEditSection > 0) {
                        ShowHideEditSections(ExpandedEditSection, false);
                    }
                    ShowHideEditSections(3, true);
                    ShowHideSection(3, false);
                } else {
                    ShowHideEditSections(3, false);
                }
            });
            Description_Edit.setOnClickListener(v -> {
                if ((Boolean) v.getTag()) {
                    if (ExpandedEditSection > 0) {
                        ShowHideEditSections(ExpandedEditSection, false);
                    }
                    ShowHideEditSections(4, true);
                    ShowHideSection(4, false);
                } else {
                    ShowHideEditSections(4, false);
                }
            });
            DescriptionEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    //show keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(DescriptionEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            });
            MySettings.setOnClickListener(v -> {
                Intent intent = new Intent(NewProfileViewActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }
        //Details Section - END
    }

    private void ShowPremiumInfoActivity() {
        Context context = NewProfileViewActivity.this;
        try {
            Intent intent = new Intent(context, GetPremiumActivity.class);
            intent.putExtra("ShowLimitExceedMessage", false);
            startActivityForResult(intent, 11);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            GDLogHelper.UploadErrorLogsToServer(context, true);
        }
    }

    private void SetFabsVisibility() {
        if (LoggedInUser.UserID.trim().equalsIgnoreCase(mCompleteUserProfile.UserID.trim())) {
            FABBlockUser.setVisibility(View.GONE);
            FABFavorite.setVisibility(View.GONE);
            FABChat.setVisibility(View.GONE);
            FABIceBreaker.setVisibility(View.GONE);
            FABEdit.setVisibility(View.VISIBLE);
            OnlineIndicator.setVisibility(View.GONE);
            MySettings.setVisibility(View.VISIBLE);
            LastSeen.setVisibility(View.GONE);
            FABPremiumUser.setVisibility(View.VISIBLE);
            if (StringHelper.IsNullOrEmpty(mProfilePicID) && mCompleteUserProfile.HasPicsToBeCategorized > 0) {
                MyPhotoBeingReviewed.setVisibility(View.VISIBLE);
            } else {
                MyPhotoBeingReviewed.setVisibility(View.GONE);
            }

        } else {
            FABFavorite.setVisibility(View.VISIBLE);
            SetFavoiteImage();
            FABChat.setVisibility(View.VISIBLE);
            FABIceBreaker.setVisibility(View.VISIBLE);
            FABEdit.setVisibility(View.GONE);
            MySettings.setVisibility(View.GONE);
            MyPhotoBeingReviewed.setVisibility(View.GONE);
            if (mCompleteUserProfile.OnlineStatus) {
                OnlineIndicator.setVisibility(View.VISIBLE);
                LastSeen.setVisibility(View.GONE);
            } else {
                OnlineIndicator.setVisibility(View.GONE);
                LastSeen.setVisibility(View.VISIBLE);
                LastSeen.setText("Last seen: " + GDDateTimeHelper.GetTimeDateBeforeFromDate(GDDateTimeHelper.GetDateFromString(mCompleteUserProfile.LastActiveDateTime), true));
            }
        }
        if (mCompleteUserProfile.PopularityRank > 0 && mCompleteUserProfile.PopularityRank < 100) {
            PopularGuysFAB.setVisibility(View.VISIBLE);
            try {
                FabRankText = new TextView(NewProfileViewActivity.this);
                FabRankText.setTypeface(null, Typeface.BOLD);
                FabRankText.setText(Integer.toString(mCompleteUserProfile.PopularityRank));
                PopularGuysFAB.setImageDrawable(ImageHelper.createDrawableFromView(NewProfileViewActivity.this, getResources(), FabRankText));
            } catch (Exception exx) {
            }
        } else {
            PopularGuysFAB.setVisibility(View.GONE);
        }
    }

    private void SetHeightWeightDistance() {
        Boolean isMyProfile = mCompleteUserProfile.UserID.equalsIgnoreCase(LoggedInUser.UserID);
        Boolean IsImperial = PersistantPreferencesHelper.GetAppSettings().UnitSystem.equals("I");
        String sHeightWeight = "";
        String sDistance = "";
        if (mCompleteUserProfile.Height != 0) {
            sHeightWeight = (IsImperial ? GDUnitHelper.Height_MTI(mCompleteUserProfile.Height, true) :
                    Integer.toString(mCompleteUserProfile.Height) + " cm");
        }
        if (mCompleteUserProfile.Weight != 0) {
            sHeightWeight += (sHeightWeight.trim().equals("") ? "" : ", ") +
                    (IsImperial ? GDUnitHelper.Weight_MTI(mCompleteUserProfile.Weight, true) :
                            Integer.toString(mCompleteUserProfile.Weight) + " Kg");

        }
        //Distance
        Boolean showHisDistance = !mCompleteUserProfile.ShowDistanceInSearchTo.equals("N");
        Boolean showMyDistance = !mCompleteUserProfile.ShowMyDistanceInSearchTo.equals("N");
        if ((showHisDistance && showMyDistance) || isMyProfile) {
            sDistance += (IsImperial ? GDUnitHelper.Distance_MTI(mCompleteUserProfile.Distance) + " away" :
                    GDUnitHelper.FormatKM(mCompleteUserProfile.Distance) + " away");
        }

        lblHeightWeight.setText(sHeightWeight);
        lblDistance.setText(sDistance);
    }

    private void SetInstagramButton() {
        if (mCompleteUserProfile.InstagramUserName != null && !mCompleteUserProfile.InstagramUserName.trim().equals("")) {
            InstagramLink.setVisibility(View.VISIBLE);
            InstagramLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCompleteUserProfile == null) {
                        return;
                    }
                    try {
                        Uri uri = Uri.parse("https://instagram.com/_u/" + StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.InstagramUserName) + "/");
                        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                        likeIng.setPackage("com.instagram.android");
                        try {
                            startActivity(likeIng);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://instagram.com/" + StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.InstagramUserName) + "/")));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        GDLogHelper.LogException(ex);
                    }
                }
            });
            if (mCompleteUserProfile.UserID.equalsIgnoreCase(LoggedInUser.UserID)) {
                InstagramLink.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mCompleteUserProfile == null) {
                            return false;
                        }
                        Intent intent = new Intent(mContext, AddInstagramLinkActivity.class);
                        intent.putExtra("InstagramUserName", mCompleteUserProfile.InstagramUserName);
                        startActivityForResult(intent, 2);
                        return true;
                    }
                });
            }
        } else if (mCompleteUserProfile.UserID.equalsIgnoreCase(LoggedInUser.UserID)) {
            InstagramLink.setVisibility(View.VISIBLE);
            InstagramLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCompleteUserProfile == null) {
                        return;
                    }
                    Intent intent = new Intent(mContext, AddInstagramLinkActivity.class);
                    intent.putExtra("InstagramUserName", mCompleteUserProfile.InstagramUserName);
                    startActivityForResult(intent, 2);
                }
            });
        }
    }

    private void SetFavoiteImage() {
        if (mCompleteUserProfile.IsFavorite.equals("1")) {
            FABFavorite.setImageResource(R.drawable.ic_favorite_selected);
            FABBlockUser.setVisibility(View.GONE);
        } else {
            FABFavorite.setImageResource(R.drawable.ic_favorite);
            FABBlockUser.setVisibility(View.VISIBLE);
        }
    }

    private void AddRemoveFavorite() {
        final Boolean addFavorite = !mCompleteUserProfile.IsFavorite.equals("1");
        final ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, mCompleteUserProfile.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_RequestingUserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_MarkFavorite, addFavorite ? "1" : "0"));

        String progressMessage = addFavorite ? "Adding favorite.." : "Removing favorite..";
        APICallInfo apiCallInfo = new APICallInfo("Home", "MarkUnmarkFavorite", pAPICallParameters, "GET", null, null, false,
                new APIProgress(mContext, progressMessage, true), APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result.equals("1")) {
                        String message = addFavorite ? mCompleteUserProfile.GetDecodedUserName() + " added to favorites." : mCompleteUserProfile.GetDecodedUserName() + " removed from favorites.";
                        GDToastHelper.ShowToast(mContext, message, GDToastHelper.INFO, GDToastHelper.SHORT);
                        mCompleteUserProfile.IsFavorite = addFavorite ? "1" : "0";

                        CompleteProfileRepository.AddToCache(mCompleteUserProfile);
                        SetFavoiteImage();
                    } else {
                        TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                }
            }
        }, new APINoNetwork() {
            @Override
            public void onAPINoNetwork() {
                TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                SetProfileData(data.getParcelableExtra("CompleteUserProfile"));

                toolbarTitleAge.setText(Integer.toString(getAge(GDDateTimeHelper.GetDateFromString(mCompleteUserProfile.DOB))));

                SetHeightWeightDistance();
                TagLine.setText(StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.TagLine));
            } else if (requestCode == 11) {
                RefreshThisActicity();
            }
            if (requestCode == 2) {
                RefreshThisActicity();
            }
        }
    }

    public int getAge(Date date) {
        Calendar cal = Calendar.getInstance();
        int y, m, d, a;
        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(date);
        a = y - cal.get(Calendar.YEAR);
        if ((m < cal.get(Calendar.MONTH))
                || ((m == cal.get(Calendar.MONTH)) && (d < cal
                .get(Calendar.DAY_OF_MONTH)))) {
            --a;
        }
        return a;
    }

    public void RefreshThisActicity() {
        Intent intent = new Intent(NewProfileViewActivity.this, NewProfileViewActivity.class);
        intent.putExtra("ClickedUserID", mCompleteUserProfile.UserID);
        if (!StringHelper.IsNullOrEmpty(mProfilePicID)) {
            intent.putExtra("ProfilePicID", mProfilePicID);
        }
        finish();
        startActivity(intent);
    }

    private void LoadProfilePic() {
        if (StringHelper.IsNullOrEmpty(mProfilePicID) || !IsActivityInView) {
            return;
        }
        ImageAPIHelper.GetFullPic(mContext, mProfilePicID, true, false, picID -> {
        }, pics -> {
            if (pics.size() == 0) {
                return;
            }
            GDPic pic = pics.get(0);
            if (pic.IsFullPic) {
                ProfilePic.setImageBitmap(pic.image);
                LargeProfilePicSet = true;
            } else {
                ProfilePic.setImageBitmap(pic.image);
            }
            ProfilePicSet = true;
        }, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                GDFullImage fullImage = (GDFullImage) result;
                ProfilePic.setImageBitmap(fullImage.image);
                LargeProfilePicSet = true;
            }

            @Override
            public void OnError(String result, Object extraData) {

            }

            @Override
            public void OnNoNetwork(Object extraData) {

            }
        });
    }

    private void GetPicsInProfile() {
        new PicsAPICalls(mContext).GetPicsInProfile(mCompleteUserProfile.UserID, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                mProfilePicsList = (ArrayList<GDPic>) result;
                if (mProfilePicsList.size() == 0) {
                    ProfilePicsContainer.setVisibility(View.GONE);
                    return;
                }
                String firstPicID = mProfilePicsList.get(0).PicID;
                String profilePic = mProfilePicID;
                if (mProfilePicsList.size() == 1 &&
                        !StringHelper.IsNullOrEmpty(firstPicID) &&
                        !StringHelper.IsNullOrEmpty(profilePic) &&
                        firstPicID.equalsIgnoreCase(profilePic)) {
                    ProfilePicsContainer.setVisibility(View.GONE);
                    return;
                }
                LoadAllPics();
                ProfilePicsContainer.setVisibility(View.VISIBLE);
            }
            @Override
            public void OnError(String result, Object extraData) {
            }
            @Override
            public void OnNoNetwork(Object extraData) {
            }
        });
    }

    private void LoadAllPics() {
        AddPublicProfilePics();
        ArrayList<String> picIDList = GDPic.GetPicIDListForNullImages(mProfilePicsList);
        ImageAPIHelper.GetPicsForPicIDList(mContext, picIDList, false, pics -> {
            GDPic.SetPics(pics, mProfilePicsList);
            UpdatePublicProfilePics();
        });
    }

    private void AddPublicProfilePics() {
        LayoutInflater layoutInflater = (LayoutInflater) NewProfileViewActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < mProfilePicsList.size(); i++) {
            if (i == 0 && IsFirstPublicPicProfilePic()) {
                continue;
            }
            GDPic CurrentPic = mProfilePicsList.get(i);
            View GDPicView = layoutInflater.inflate(R.layout.gdpic_layout, null);
            ImageView imageView = GDPicView.findViewById(R.id.GDPic);
            imageView.getLayoutParams().width = 160;
            imageView.getLayoutParams().height = 160;
            if (CurrentPic.image != null) {
                imageView.setImageBitmap(CurrentPic.image);
            } else {
                imageView.setImageResource(R.drawable.defaultuserprofilepic);
            }
            imageView.setTag(i);
            imageView.setOnClickListener(v -> {
                int position = (Integer) v.getTag();
                Intent intent = new Intent(NewProfileViewActivity.this, GDPicViewerActivity.class);
                intent.putParcelableArrayListExtra("GDPicList", mProfilePicsList);
                intent.putExtra("SelectedPic", position);
                startActivity(intent);
            });
            PublicProfilePicsScroll.addView(GDPicView);
        }
    }

    private void UpdatePublicProfilePics() {
        for (int i = 0; i < PublicProfilePicsScroll.getChildCount(); i++) {
            if (mProfilePicsList.size() > i) {
                ImageView imageView = PublicProfilePicsScroll.getChildAt(i).findViewById(R.id.GDPic);
                int position = (int)imageView.getTag();
                imageView.setImageBitmap(mProfilePicsList.get(position).image);
            } else {
                break;
            }
        }
    }

    private Boolean IsFirstPublicPicProfilePic() {
        return mProfilePicsList.size() > 0 && !StringHelper.IsNullOrEmpty(mProfilePicID)
                && mProfilePicsList.get(0).PicID.equalsIgnoreCase(mProfilePicID);
    }

    private void ShowMapPrivacyAlert(final APISuccessCallback successShowUserMapLocationCallback) {
        String title = "Show users on map?<br> [Privacy Alert]";
        title = "<small>" + title + "</small>";
        String mapLocationPrivacyAlertMessage = getString(R.string.map_location_privacy_alert_message);
        mapLocationPrivacyAlertMessage = mapLocationPrivacyAlertMessage.replace("##", "<br>");
        mapLocationPrivacyAlertMessage = "<small>" + mapLocationPrivacyAlertMessage + "</small>";
        GDDialogHelper.ShowYesNoTypeDialog(mContext, Html.fromHtml(title), Html.fromHtml(mapLocationPrivacyAlertMessage),
                GDDialogHelper.BUTTON_TEXT_CONTINUE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT,
                new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        new HomeAPICalls(mContext).ShowInMapSearch(new ProgressDialog(NewProfileViewActivity.this), new APISuccessCallback() {
                            @Override
                            public void onSuccess(Object data, Object ExtraData) {
                                mCompleteUserProfile.MyShowInMapSearch = true;
                                HomePageFragment homePageFragment = HomePageFragment.HomePageInstance;
                                if (homePageFragment != null) {
                                    homePageFragment.MakeMyMapViewShownInList();
                                }
                                SessionManager.SetFirstPageNearByUsersShowMyInMap();
                                successShowUserMapLocationCallback.onSuccess(null, null);
                            }
                        }, new APIFailureCallback() {
                            @Override
                            public void onFailure(Object data, Object ExtraData) {
                                TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                            }
                        }, new APINoNetwork() {
                            @Override
                            public void onAPINoNetwork() {
                                TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                            }
                        });
                    }
                }, new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                    }
                });
    }


    //Details Section - START
    private void SetDetailsFieldsData() {
        if (mCompleteUserProfile.WaistSize != 0) {
            Waist.setText(mCompleteUserProfile.WaistSize + " inches");
        }
        BodyType.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterBodyType, mCompleteUserProfile.BodyType));
        Ethnicity.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterEthnicity, mCompleteUserProfile.Ethnicity));
        Hair.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterHairType, mCompleteUserProfile.HairType));
        BodyHair.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterBodyHairType, mCompleteUserProfile.BodyHairType));
        FacialHair.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterFacialHairType, mCompleteUserProfile.FacialHairType));
        Tattoo.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterTattoo, mCompleteUserProfile.Tattoo));
        Piercings.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterPiercings, mCompleteUserProfile.Piercings));
        SexualOrientation.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSexualOrientation, mCompleteUserProfile.SexualOrientation));
        SexualPreference.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSexualPreference, mCompleteUserProfile.SexualPreference));
        ToolSize.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterToolSize, mCompleteUserProfile.ToolSize));
        ToolType.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterToolType, mCompleteUserProfile.ToolType));
        String sOral = MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSucking, mCompleteUserProfile.Orals);
        if (!sOral.trim().equals("")) {
            if (sOral.toUpperCase().trim().equals("BOTH")) {
                Oral.setText("Likes " + sOral);
            } else {
                Oral.setText("Likes to " + sOral);
            }
        }
        SAndM.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSAndM, mCompleteUserProfile.SAndM));
        List<String> LookingForList = StringHelper.SplitStringByComma(mCompleteUserProfile.LookingFor);
        LookingFor.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterInterests, LookingForList));
        List<String> FetishList = StringHelper.SplitStringByComma(mCompleteUserProfile.Fetish);
        Fetish.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterFetishes, FetishList));
        Profession.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterProfession, mCompleteUserProfile.Profession));
        Religion.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterReligion, mCompleteUserProfile.Religion));
        Smoking.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSmoking, mCompleteUserProfile.Smoking));
        Drinking.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterDrinking, mCompleteUserProfile.Drinking));
        RelationshipStatus.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterRelationshipStatus, mCompleteUserProfile.RelationshipStatus));
        List<String> LanguagesList = StringHelper.SplitStringByComma(mCompleteUserProfile.Languages);
        LanguagesKnown.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterLanguages, LanguagesList));
        Description.setText(StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.DetailedText));
        if (IsProfileOwner) {
            btnMyPhotos.setText("My photos (" + Integer.toString(mCompleteUserProfile.MyPhotosCount) + ")");
        }


        if (IsProfileOwner) {
            InitEditViews();
        } else {
            HideNoEntryRows();
        }
    }

    private void InitEditViews() {
        //General Stats
        WaistSizeSpinner.SetData(mContext, "Set your waist size", MasterDataHelper.GetDataStoreForIncremental(MasterDataHelper.TABLE_MasterUserWaist, false),
                Integer.toString(mCompleteUserProfile.WaistSize),
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        if (WaistSizeSpinner.getTag().toString().trim().equals("")) {
                            mCompleteUserProfile.WaistSize = 0;
                            Waist.setText("");
                        } else {
                            try {
                                mCompleteUserProfile.WaistSize = Integer.parseInt(WaistSizeSpinner.getTag().toString());
                                Waist.setText(Integer.toString(mCompleteUserProfile.WaistSize));
                            } catch (Exception ex) {
                            }
                        }
                        SaveEditData(1);
                    }
                }, null, true);
        BodyTypeSpinner.SetData(mContext, "Set your body type", MasterDataHelper.MasterBodyType, mCompleteUserProfile.BodyType,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.BodyType = BodyTypeSpinner.getTag().toString();
                        BodyType.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterBodyType, mCompleteUserProfile.BodyType));
                        SaveEditData(1);
                    }
                }, null, true);
        EthnicitySpinner.SetData(mContext, "Set your ethnicity", MasterDataHelper.MasterEthnicity, mCompleteUserProfile.Ethnicity,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.Ethnicity = EthnicitySpinner.getTag().toString();
                        Ethnicity.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterEthnicity, mCompleteUserProfile.Ethnicity));
                        SaveEditData(1);
                    }
                }, null, true);
        HairSpinner.SetData(mContext, "Set your hair type", MasterDataHelper.MasterHairType, mCompleteUserProfile.HairType,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.HairType = HairSpinner.getTag().toString();
                        Hair.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterHairType, mCompleteUserProfile.HairType));
                        SaveEditData(1);
                    }
                }, null, true);
        BodyHairSpinner.SetData(mContext, "Set your body hair type", MasterDataHelper.MasterBodyHairType, mCompleteUserProfile.BodyHairType,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.BodyHairType = BodyHairSpinner.getTag().toString();
                        BodyHair.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterBodyHairType, mCompleteUserProfile.BodyHairType));
                        SaveEditData(1);
                    }
                }, null, true);
        FacialHairSpinner.SetData(mContext, "Set your facial hair type", MasterDataHelper.MasterFacialHairType, mCompleteUserProfile.FacialHairType,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.FacialHairType = FacialHairSpinner.getTag().toString();
                        FacialHair.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterFacialHairType, mCompleteUserProfile.FacialHairType));
                        SaveEditData(1);
                    }
                }, null, true);
        TattooSpinner.SetData(mContext, "Do you have tattoos?", MasterDataHelper.MasterTattoo, mCompleteUserProfile.Tattoo,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.Tattoo = TattooSpinner.getTag().toString();
                        Tattoo.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterTattoo, mCompleteUserProfile.Tattoo));
                        SaveEditData(1);
                    }
                }, null, true);
        PiercingsSpinner.SetData(mContext, "Do you have piercings?", MasterDataHelper.MasterPiercings, mCompleteUserProfile.Piercings,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.Piercings = PiercingsSpinner.getTag().toString();
                        Piercings.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterPiercings, mCompleteUserProfile.Piercings));
                        SaveEditData(1);
                    }
                }, null, true);


        //Sexual Stats
        SexualOrientationSpinner.SetData(mContext, "What is your sexual orientation?", MasterDataHelper.MasterSexualOrientation, mCompleteUserProfile.SexualOrientation,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.SexualOrientation = SexualOrientationSpinner.getTag().toString();
                        SexualOrientation.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSexualOrientation, mCompleteUserProfile.SexualOrientation));
                        SaveEditData(2);
                    }
                }, null);
        SexualPreferenceSpinner.SetData(mContext, "What is your sexual preference?", MasterDataHelper.MasterSexualPreference, mCompleteUserProfile.SexualPreference,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.SexualPreference = SexualPreferenceSpinner.getTag().toString();
                        SexualPreference.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSexualPreference, mCompleteUserProfile.SexualPreference));
                        SaveEditData(2);
                    }
                }, null);
        ToolSizeSpinner.SetData(mContext, "Tool Size", MasterDataHelper.MasterToolSize, mCompleteUserProfile.ToolSize,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.ToolSize = ToolSizeSpinner.getTag().toString();
                        ToolSize.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterToolSize, mCompleteUserProfile.ToolSize));
                        SaveEditData(2);
                    }
                }, null, true);
        ToolTypeSpinner.SetData(mContext, "Tool Type", MasterDataHelper.MasterToolType, mCompleteUserProfile.ToolType,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.ToolType = ToolTypeSpinner.getTag().toString();
                        ToolType.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterToolType, mCompleteUserProfile.ToolType));
                        SaveEditData(2);
                    }
                }, null, true);
        OralSpinner.SetData(mContext, "Orals", MasterDataHelper.MasterSucking, mCompleteUserProfile.Orals,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.Orals = OralSpinner.getTag().toString();
                        String sOral = MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSucking, mCompleteUserProfile.Orals);
                        if (sOral.trim().equals("")) {
                            Oral.setText("");
                        } else if (sOral.toUpperCase().trim().equals("BOTH")) {
                            Oral.setText("Likes " + sOral);
                        } else {
                            Oral.setText("Likes to " + sOral);
                        }

                        SaveEditData(2);
                    }
                }, null, true);
        SAndMSpinner.SetData(mContext, "S&M", MasterDataHelper.MasterSAndM, mCompleteUserProfile.SAndM,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.SAndM = SAndMSpinner.getTag().toString();
                        SAndM.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSAndM, mCompleteUserProfile.SAndM));
                        SaveEditData(2);
                    }
                }, null, true);
        LookingForSpinner.SetData(mContext, "Looking for..", MasterDataHelper.MasterInterests, mCompleteUserProfile.LookingFor, null,
                new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        mCompleteUserProfile.LookingFor = LookingForSpinner.getTag().toString();
                        LookingFor.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterInterests, StringHelper.SplitStringByComma(mCompleteUserProfile.LookingFor)));
                        SaveEditData(2);
                    }
                }, null);
        FetishSpinner.SetData(mContext, "Choose your Fetishes", MasterDataHelper.MasterFetishes, mCompleteUserProfile.Fetish, null,
                new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        mCompleteUserProfile.Fetish = FetishSpinner.getTag().toString();
                        Fetish.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterFetishes, StringHelper.SplitStringByComma(mCompleteUserProfile.Languages)));
                        SaveEditData(2);
                    }
                }, null);

        //Social Stats
        ProfessionSpinner.SetData(mContext, "Profession", MasterDataHelper.MasterProfession, mCompleteUserProfile.Profession,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.Profession = ProfessionSpinner.getTag().toString();
                        Profession.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterProfession, mCompleteUserProfile.Profession));
                        SaveEditData(3);
                    }
                }, null, true);
        ReligionSpinner.SetData(mContext, "Religion", MasterDataHelper.MasterReligion, mCompleteUserProfile.Religion,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.Religion = ReligionSpinner.getTag().toString();
                        Religion.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterReligion, mCompleteUserProfile.Religion));
                        SaveEditData(3);
                    }
                }, null, true);
        SmokingSpinner.SetData(mContext, "Smoking", MasterDataHelper.MasterSmoking, mCompleteUserProfile.Smoking,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.Smoking = SmokingSpinner.getTag().toString();
                        Religion.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterSmoking, mCompleteUserProfile.Smoking));
                        SaveEditData(3);
                    }
                }, null, true);
        DrinkingSpinner.SetData(mContext, "Drinking", MasterDataHelper.MasterDrinking, mCompleteUserProfile.Drinking,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.Drinking = DrinkingSpinner.getTag().toString();
                        Drinking.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterDrinking, mCompleteUserProfile.Drinking));
                        SaveEditData(3);
                    }
                }, null, true);
        RelationshipStatusSpinner.SetData(mContext, "Relationship Status", MasterDataHelper.MasterRelationshipStatus, mCompleteUserProfile.RelationshipStatus,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        mCompleteUserProfile.RelationshipStatus = RelationshipStatusSpinner.getTag().toString();
                        RelationshipStatus.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterRelationshipStatus, mCompleteUserProfile.RelationshipStatus));
                        SaveEditData(3);
                    }
                }, null, true);
        LanguagesKnownSpinner.SetData(mContext, "Languages you know", MasterDataHelper.MasterLanguages, mCompleteUserProfile.Languages, null,
                new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        mCompleteUserProfile.Languages = LanguagesKnownSpinner.getTag().toString();
                        LanguagesKnown.setText(MasterDataHelper.GetUserStatsDesc(MasterDataHelper.TABLE_MasterLanguages, StringHelper.SplitStringByComma(mCompleteUserProfile.Languages)));
                        SaveEditData(3);
                    }
                }, null);
    }

    private void HideNoEntryRows() {
        Boolean HideGeneralStats = true;
        Boolean HideSexualStats = true;
        Boolean HideSocialStats = true;

        if (mCompleteUserProfile.WaistSize == 0) {
            WaistRow.setVisibility(View.GONE);
        } else {
            HideGeneralStats = false;
        }
        if (mCompleteUserProfile.BodyType == null || mCompleteUserProfile.BodyType.equals("")) {
            BodyTypeRow.setVisibility(View.GONE);
        } else {
            HideGeneralStats = false;
        }
        if (mCompleteUserProfile.Ethnicity == null || mCompleteUserProfile.Ethnicity.equals("")) {
            EthnicityRow.setVisibility(View.GONE);
        } else {
            HideGeneralStats = false;
        }
        if (mCompleteUserProfile.HairType == null || mCompleteUserProfile.HairType.equals("")) {
            HairRow.setVisibility(View.GONE);
        } else {
            HideGeneralStats = false;
        }
        if (mCompleteUserProfile.BodyHairType == null || mCompleteUserProfile.BodyHairType.equals("")) {
            BodyHairRow.setVisibility(View.GONE);
        } else {
            HideGeneralStats = false;
        }
        if (mCompleteUserProfile.FacialHairType == null || mCompleteUserProfile.FacialHairType.equals("")) {
            FacialHairRow.setVisibility(View.GONE);
        } else {
            HideGeneralStats = false;
        }
        if (mCompleteUserProfile.Tattoo == null || mCompleteUserProfile.Tattoo.equals("")) {
            TattooRow.setVisibility(View.GONE);
        } else {
            HideGeneralStats = false;
        }
        if (mCompleteUserProfile.Piercings == null || mCompleteUserProfile.Piercings.equals("")) {
            PiercingsRow.setVisibility(View.GONE);
        } else {
            HideGeneralStats = false;
        }
        if (HideGeneralStats) {
            GeneralStatsHeader.setVisibility(View.GONE);
            GeneralStatsBody.setVisibility(View.GONE);
            GeneralStatsBottomLine.setVisibility(View.GONE);
        }

        if (mCompleteUserProfile.SexualOrientation == null || mCompleteUserProfile.SexualOrientation.equals("")) {
            SexualOrientationRow.setVisibility(View.GONE);
        } else {
            HideSexualStats = false;
        }
        if (mCompleteUserProfile.SexualPreference == null || mCompleteUserProfile.SexualPreference.equals("")) {
            SexualPreferenceRow.setVisibility(View.GONE);
        } else {
            HideSexualStats = false;
        }
        if (mCompleteUserProfile.ToolSize == null || mCompleteUserProfile.ToolSize.equals("")) {
            ToolSizeRow.setVisibility(View.GONE);
        } else {
            HideSexualStats = false;
        }
        if (mCompleteUserProfile.ToolType == null || mCompleteUserProfile.ToolType.equals("")) {
            ToolTypeRow.setVisibility(View.GONE);
        } else {
            HideSexualStats = false;
        }
        if (mCompleteUserProfile.Orals == null || mCompleteUserProfile.Orals.equals("")) {
            OralRow.setVisibility(View.GONE);
        } else {
            HideSexualStats = false;
        }
        if (mCompleteUserProfile.SAndM == null || mCompleteUserProfile.SAndM.equals("")) {
            SAndMRow.setVisibility(View.GONE);
        } else {
            HideSexualStats = false;
        }
        if (mCompleteUserProfile.LookingFor == null || mCompleteUserProfile.LookingFor.equals("")) {
            LookingForRow.setVisibility(View.GONE);
        } else {
            HideSexualStats = false;
        }
        if (mCompleteUserProfile.Fetish == null || mCompleteUserProfile.Fetish.equals("")) {
            FetishRow.setVisibility(View.GONE);
        } else {
            HideSexualStats = false;
        }
        if (HideSexualStats) {
            SexualStatsHeader.setVisibility(View.GONE);
            SexualStatsBody.setVisibility(View.GONE);
            SexualStatsBottomLine.setVisibility(View.GONE);
        }

        if (mCompleteUserProfile.Profession == null || mCompleteUserProfile.Profession.equals("")) {
            ProfessionRow.setVisibility(View.GONE);
        } else {
            HideSocialStats = false;
        }
        if (mCompleteUserProfile.Religion == null || mCompleteUserProfile.Religion.equals("")) {
            ReligionRow.setVisibility(View.GONE);
        } else {
            HideSocialStats = false;
        }
        if (mCompleteUserProfile.Smoking == null || mCompleteUserProfile.Smoking.equals("")) {
            SmokingRow.setVisibility(View.GONE);
        } else {
            HideSocialStats = false;
        }
        if (mCompleteUserProfile.Drinking == null || mCompleteUserProfile.Drinking.equals("")) {
            DrinkingRow.setVisibility(View.GONE);
        } else {
            HideSocialStats = false;
        }
        if (mCompleteUserProfile.RelationshipStatus == null || mCompleteUserProfile.RelationshipStatus.equals("")) {
            RelationshipStatusRow.setVisibility(View.GONE);
        } else {
            HideSocialStats = false;
        }
        if (mCompleteUserProfile.Languages == null || mCompleteUserProfile.Languages.equals("")) {
            LanguagesKnownRow.setVisibility(View.GONE);
        } else {
            HideSocialStats = false;
        }
        if (HideSocialStats) {
            SocialStatsHeader.setVisibility(View.GONE);
            SocialStatsBody.setVisibility(View.GONE);
            SocialStatsBottomLine.setVisibility(View.GONE);
        }
    }

    private void ShowHideSection(int section, Boolean Show) {
        if (section != 4 && ExpandedEditSection == 4) {
            ShowHideEditSections(4, false);
        }
        switch (section) {
            case 1:
                if (Show) {
                    GeneralStatsHeader.setTag(false);
                    GeneralStatsBody.setVisibility(View.GONE);
                    GeneralStats_Plus.setVisibility(View.VISIBLE);
                    GeneralStats_Minus.setVisibility(View.GONE);
                } else {
                    GeneralStatsHeader.setTag(true);
                    GeneralStatsBody.setVisibility(View.VISIBLE);
                    GeneralStats_Plus.setVisibility(View.GONE);
                    GeneralStats_Minus.setVisibility(View.VISIBLE);
                }
                break;
            case 2:
                if (Show) {
                    SexualStatsHeader.setTag(false);
                    SexualStatsBody.setVisibility(View.GONE);
                    SexualStats_Plus.setVisibility(View.VISIBLE);
                    SexualStats_Minus.setVisibility(View.GONE);
                } else {
                    SexualStatsHeader.setTag(true);
                    SexualStatsBody.setVisibility(View.VISIBLE);
                    SexualStats_Plus.setVisibility(View.GONE);
                    SexualStats_Minus.setVisibility(View.VISIBLE);
                }
                break;
            case 3:
                if (Show) {
                    SocialStatsHeader.setTag(false);
                    SocialStatsBody.setVisibility(View.GONE);
                    SocialStats_Plus.setVisibility(View.VISIBLE);
                    SocialStats_Minus.setVisibility(View.GONE);
                } else {
                    SocialStatsHeader.setTag(true);
                    SocialStatsBody.setVisibility(View.VISIBLE);
                    SocialStats_Plus.setVisibility(View.GONE);
                    SocialStats_Minus.setVisibility(View.VISIBLE);
                }
                break;
            case 4:
                if (Show) {
                    DescriptionHeader.setTag(false);
                    DescriptionBody.setVisibility(View.GONE);
                    Description_Plus.setVisibility(View.VISIBLE);
                    Description_Minus.setVisibility(View.GONE);
                } else {
                    DescriptionHeader.setTag(true);
                    DescriptionBody.setVisibility(View.VISIBLE);
                    Description_Plus.setVisibility(View.GONE);
                    Description_Minus.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void ShowHideEditSections(int section, boolean edit) {
        if (section != 1 && section != 2 && section != 3 && section != 4) {
            return;
        }
        if (edit) {
            ExpandedEditSection = section;
        } else {
            ExpandedEditSection = 0;
        }
        if (section == 1) {
            if (edit) {
                GeneralStats_Edit.setTag(false);
                Waist.setVisibility(View.GONE);
                BodyType.setVisibility(View.GONE);
                Ethnicity.setVisibility(View.GONE);
                Hair.setVisibility(View.GONE);
                BodyHair.setVisibility(View.GONE);
                FacialHair.setVisibility(View.GONE);
                Tattoo.setVisibility(View.GONE);
                Piercings.setVisibility(View.GONE);

                WaistSizeSpinner.setVisibility(View.VISIBLE);
                BodyTypeSpinner.setVisibility(View.VISIBLE);
                EthnicitySpinner.setVisibility(View.VISIBLE);
                HairSpinner.setVisibility(View.VISIBLE);
                BodyHairSpinner.setVisibility(View.VISIBLE);
                FacialHairSpinner.setVisibility(View.VISIBLE);
                TattooSpinner.setVisibility(View.VISIBLE);
                PiercingsSpinner.setVisibility(View.VISIBLE);
            } else {
                GeneralStats_Edit.setTag(true);
                Waist.setVisibility(View.VISIBLE);
                BodyType.setVisibility(View.VISIBLE);
                Ethnicity.setVisibility(View.VISIBLE);
                Hair.setVisibility(View.VISIBLE);
                BodyHair.setVisibility(View.VISIBLE);
                FacialHair.setVisibility(View.VISIBLE);
                Tattoo.setVisibility(View.VISIBLE);
                Piercings.setVisibility(View.VISIBLE);

                WaistSizeSpinner.setVisibility(View.GONE);
                BodyTypeSpinner.setVisibility(View.GONE);
                EthnicitySpinner.setVisibility(View.GONE);
                HairSpinner.setVisibility(View.GONE);
                BodyHairSpinner.setVisibility(View.GONE);
                FacialHairSpinner.setVisibility(View.GONE);
                TattooSpinner.setVisibility(View.GONE);
                PiercingsSpinner.setVisibility(View.GONE);
            }
        } else if (section == 2) {
            if (edit) {
                SexualStats_Edit.setTag(false);
                SexualOrientation.setVisibility(View.GONE);
                SexualPreference.setVisibility(View.GONE);
                ToolSize.setVisibility(View.GONE);
                ToolType.setVisibility(View.GONE);
                Oral.setVisibility(View.GONE);
                SAndM.setVisibility(View.GONE);
                LookingFor.setVisibility(View.GONE);
                Fetish.setVisibility(View.GONE);

                SexualOrientationSpinner.setVisibility(View.VISIBLE);
                SexualPreferenceSpinner.setVisibility(View.VISIBLE);
                ToolSizeSpinner.setVisibility(View.VISIBLE);
                ToolTypeSpinner.setVisibility(View.VISIBLE);
                OralSpinner.setVisibility(View.VISIBLE);
                SAndMSpinner.setVisibility(View.VISIBLE);
                LookingForSpinner.setVisibility(View.VISIBLE);
                FetishSpinner.setVisibility(View.VISIBLE);
            } else {
                SexualStats_Edit.setTag(true);
                SexualOrientation.setVisibility(View.VISIBLE);
                SexualPreference.setVisibility(View.VISIBLE);
                ToolSize.setVisibility(View.VISIBLE);
                ToolType.setVisibility(View.VISIBLE);
                Oral.setVisibility(View.VISIBLE);
                SAndM.setVisibility(View.VISIBLE);
                LookingFor.setVisibility(View.VISIBLE);
                Fetish.setVisibility(View.VISIBLE);

                SexualOrientationSpinner.setVisibility(View.GONE);
                SexualPreferenceSpinner.setVisibility(View.GONE);
                ToolSizeSpinner.setVisibility(View.GONE);
                ToolTypeSpinner.setVisibility(View.GONE);
                OralSpinner.setVisibility(View.GONE);
                SAndMSpinner.setVisibility(View.GONE);
                LookingForSpinner.setVisibility(View.GONE);
                FetishSpinner.setVisibility(View.GONE);
            }
        } else if (section == 3) {
            if (edit) {
                SocialStats_Edit.setTag(false);
                Profession.setVisibility(View.GONE);
                Religion.setVisibility(View.GONE);
                Smoking.setVisibility(View.GONE);
                Drinking.setVisibility(View.GONE);
                RelationshipStatus.setVisibility(View.GONE);
                LanguagesKnown.setVisibility(View.GONE);

                ProfessionSpinner.setVisibility(View.VISIBLE);
                ReligionSpinner.setVisibility(View.VISIBLE);
                SmokingSpinner.setVisibility(View.VISIBLE);
                DrinkingSpinner.setVisibility(View.VISIBLE);
                RelationshipStatusSpinner.setVisibility(View.VISIBLE);
                LanguagesKnownSpinner.setVisibility(View.VISIBLE);
            } else {
                SocialStats_Edit.setTag(true);
                Profession.setVisibility(View.VISIBLE);
                Religion.setVisibility(View.VISIBLE);
                Smoking.setVisibility(View.VISIBLE);
                Drinking.setVisibility(View.VISIBLE);
                RelationshipStatus.setVisibility(View.VISIBLE);
                LanguagesKnown.setVisibility(View.VISIBLE);

                ProfessionSpinner.setVisibility(View.GONE);
                ReligionSpinner.setVisibility(View.GONE);
                SmokingSpinner.setVisibility(View.GONE);
                DrinkingSpinner.setVisibility(View.GONE);
                RelationshipStatusSpinner.setVisibility(View.GONE);
                LanguagesKnownSpinner.setVisibility(View.GONE);
            }
        } else if (section == 4) {
            if (edit) {
                Description_Edit.setTag(false);
                Description.setVisibility(View.GONE);
                DescriptionEditText.setVisibility(View.VISIBLE);
                DescriptionEditText.setText(StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.DetailedText));
            } else {
                Description_Edit.setTag(true);
                Description.setVisibility(View.VISIBLE);
                DescriptionEditText.setVisibility(View.GONE);
                SaveDescription();
            }
        }
    }

    private void SaveEditData(int section) {
        GeneralStats oGeneralStats = null;
        SexualStats oSexualStats = null;
        SocialStats oSocialStats = null;
        if (section == 1) {
            //General Stats
            oGeneralStats = new GeneralStats(LoggedInUser.UserID, mCompleteUserProfile.WaistSize, mCompleteUserProfile.BodyType,
                    mCompleteUserProfile.Ethnicity, mCompleteUserProfile.HairType, mCompleteUserProfile.BodyHairType,
                    mCompleteUserProfile.FacialHairType, mCompleteUserProfile.Tattoo, mCompleteUserProfile.Piercings);
            APICallInfo apiCallInfo = new APICallInfo("Home", "EditProfileSecBodyStats", null, "POST", oGeneralStats, null, false, null, APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result.equals("1")) {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, "Changes saved", TopSnackBar.LENGTH_SHORT, false).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                        TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                }
            });
        } else if (section == 2) {
            //Sexual Stats
            oSexualStats = new SexualStats(LoggedInUser.UserID, mCompleteUserProfile.SexualOrientation, mCompleteUserProfile.SexualPreference,
                    mCompleteUserProfile.ToolSize, mCompleteUserProfile.ToolType, mCompleteUserProfile.Orals, mCompleteUserProfile.SAndM,
                    mCompleteUserProfile.LookingFor, mCompleteUserProfile.Fetish);
            APICallInfo apiCallInfo = new APICallInfo("Home", "EditProfileSecSexualStats", null, "POST", oSexualStats, null, false, null, APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result.equals("1")) {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, "Changes saved", TopSnackBar.LENGTH_SHORT, false).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                        TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                }
            });
        } else if (section == 3) {
            //Social Stats
            oSocialStats = new SocialStats(LoggedInUser.UserID, mCompleteUserProfile.Profession, mCompleteUserProfile.Religion,
                    mCompleteUserProfile.Smoking, mCompleteUserProfile.Drinking, mCompleteUserProfile.RelationshipStatus, mCompleteUserProfile.Languages);
            APICallInfo apiCallInfo = new APICallInfo("Home", "EditProfileSecOtherStats", null, "POST", oSocialStats, null, false, null, APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result.equals("1")) {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, "Changes saved", TopSnackBar.LENGTH_SHORT, false).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                        TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                }
            });
        }
    }

    private void SaveDescription() {
        try {
            if (!gdValidationHelper.Validate()) {
                TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.validation_error), TopSnackBar.LENGTH_SHORT, true).show();
                Description_Edit.setTag(false);
                Description.setVisibility(View.GONE);
                DescriptionEditText.setVisibility(View.VISIBLE);
                return;
            }
            UserProfileDetails profileDetails = new UserProfileDetails();
            profileDetails.UserID = LoggedInUser.UserID;
            profileDetails.TagLine = mCompleteUserProfile.TagLine;
            profileDetails.UserDetailedDesc = StringEncoderHelper.encodeURIComponent(DescriptionEditText.getText().toString());
            profileDetails.SexualOrientation = mCompleteUserProfile.SexualOrientation;
            profileDetails.SexualPreference = mCompleteUserProfile.SexualPreference;
            APICallInfo apiCallInfo = new APICallInfo("CompleteProfile", "InsUpdUserProfileDetails_Mobile", null, "POST", profileDetails,
                    null, false, new APIProgress(mContext, "Saving..", true), APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result.equals("1")) {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, "Changes saved", TopSnackBar.LENGTH_SHORT, false).show();
                            mCompleteUserProfile.DetailedText = StringEncoderHelper.encodeURIComponent(DescriptionEditText.getText().toString());
                            Description.setText(StringEncoderHelper.decodeURIComponent(mCompleteUserProfile.DetailedText));

                            //hide keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(DescriptionEditText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                        } else {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                            Description_Edit.setTag(false);
                            Description.setVisibility(View.GONE);
                            DescriptionEditText.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                        TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                        Description_Edit.setTag(false);
                        Description.setVisibility(View.GONE);
                        DescriptionEditText.setVisibility(View.VISIBLE);
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                    Description_Edit.setTag(false);
                    Description.setVisibility(View.GONE);
                    DescriptionEditText.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    //Details Section - END

    private void ShowDialogForBlock(String MessageText, final Boolean Block) {
        GDDialogHelper.ShowYesNoTypeDialog(NewProfileViewActivity.this, "Cannot open profile..", MessageText,
                GDDialogHelper.BUTTON_TEXT_CANCEL,
                Block ? GDDialogHelper.BUTTON_TEXT_BLOCK_HIM_TOO : GDDialogHelper.BUTTON_TEXT_UNBLOCK,
                GDDialogHelper.ERROR, () -> finish(), () -> BlockUnblock(Block));
    }

    private void BlockUnblock(Boolean bBlock) {
        APIProgress apiProgress;
        if (bBlock) {
            apiProgress= new APIProgress(mContext, "Blocking..", false);
            new HomeAPICalls(mContext).BlockUser(mCompleteUserProfile.UserID, apiProgress.progressDialog,
                    new APICallerResultCallback() {
                @Override
                public void OnComplete(Object result, Object extraData) {
                    GDToastHelper.ShowToast(mContext, "He has been blocked", GDToastHelper.INFO, GDToastHelper.SHORT);
                    UserObjectsCacheHelper.SetIBlockedUnBlockedHim(ClickedUserID, true);
                    CompleteProfileRepository.RemoveFromCache(mCompleteUserProfile.UserID);
                    finish();
                }
                @Override
                public void OnError(String result, Object extraData) {
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                }
                @Override
                public void OnNoNetwork(Object extraData) {
                    TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                }
            });
        } else {
            apiProgress= new APIProgress(mContext, "Un-blocking..", false);
            new HomeAPICalls(mContext).BlockUnBlockUsers(StringHelper.ToArrayList(mCompleteUserProfile.UserID),
                    apiProgress.progressDialog, new APICallerResultCallback() {
                        @Override
                        public void OnComplete(Object result, Object extraData) {
                            GDToastHelper.ShowToast(mContext, "He has been un-blocked.", GDToastHelper.INFO, GDToastHelper.SHORT);
                            UserObjectsCacheHelper.SetIBlockedUnBlockedHim(ClickedUserID, false);
                            CompleteProfileRepository.RemoveFromCache(mCompleteUserProfile.UserID);
                            finish();
                        }
                        @Override
                        public void OnError(String result, Object extraData) {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                        }
                        @Override
                        public void OnNoNetwork(Object extraData) {
                            TopSnackBar.MakeSnackBar(BodyBelowAppBar, getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        boolean isMyProfile = LoggedInUser.UserID.equalsIgnoreCase(ClickedUserID);
        getMenuInflater().inflate(R.menu.menu_new_profile_view, menu);
        mMenu = menu;
        if (isMyProfile) {
            mMenu.getItem(0).setVisible(false);
        }
        return !isMyProfile;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_report:
                final CharSequence[] OptionsList = new CharSequence[5];
                OptionsList[0] = "Nudity";
                OptionsList[1] = "Abusive";
                OptionsList[2] = "Minor";
                OptionsList[3] = "Impersonation";
                OptionsList[4] = "Other";
                GDDialogHelper.ShowRadioOptionTypeDialogWithPositiveButton(mContext, OptionsList, "Reason to report?", ReportUserSelectedOption, GDDialogHelper.BUTTON_TEXT_REPORT_USER, GDDialogHelper.BUTTON_TEXT_CANCEL, new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        ReportUserSelectedOption = position;
                    }
                }, new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        Intent intent = new Intent(mContext, ReportIssueMakeSugesstionActivity.class);
                        intent.putExtra("Activity_Mode", ReportIssueMakeSugesstionActivity.REPORTUSER);
                        intent.putExtra("ReportUserID", ClickedUserID);
                        intent.putExtra("ReportUserReason", OptionsList[ReportUserSelectedOption]);
                        ReportUserSelectedOption = 0;
                        startActivity(intent);
                    }
                }, new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        ReportUserSelectedOption = 0;
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IsActivityInView = true;
        if (!LargeProfilePicSet && mCompleteUserProfile != null && !StringHelper.IsNullOrEmpty(mProfilePicID)) {
            GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(mContext);
            String PicSrc = gdImageDBHelper.GetImageStringByPicID(mProfilePicID, true);
            if (!PicSrc.equals("")) {
                try {
                    ProfilePic.setImageBitmap(ImageHelper.GetBitmapFromString(PicSrc, true));
                    LargeProfilePicSet = true;
                    ProfilePicSet = true;
                } catch (OutOfMemoryError err) {
                    //Do nothing
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        IsActivityInView = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class UserProfileDetails {
        public String UserID;
        public String TagLine;
        public String UserDetailedDesc;
        public String SexualOrientation;
        public String SexualPreference;
    }
}

