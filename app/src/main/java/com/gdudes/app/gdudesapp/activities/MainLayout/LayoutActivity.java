package com.gdudes.app.gdudesapp.activities.MainLayout;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.CustomViewTypes.GifMovieView;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDServices.FirebaseInstanceIDService;
import com.gdudes.app.gdudesapp.GDServices.GDMessageService;
import com.gdudes.app.gdudesapp.GDServices.MessageAndNotificationDownloader;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.DeleteUserLocation;
import com.gdudes.app.gdudesapp.GDTypes.AppSettings;
import com.gdudes.app.gdudesapp.GDTypes.UserLocation;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncHelper;
import com.gdudes.app.gdudesapp.Helpers.GDCountDownTimer;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDRatingsHelper;
import com.gdudes.app.gdudesapp.Helpers.GDTimer;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GPSHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.MasterDataHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.Interfaces.OnFragmentBackPressed;
import com.gdudes.app.gdudesapp.Interfaces.SubGenericAction;
import com.gdudes.app.gdudesapp.Notifications.NotificationHelper;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDMapActivity;
import com.gdudes.app.gdudesapp.activities.LoginRegister.LoginActivity;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class LayoutActivity extends AppCompatActivity implements OnFragmentBackPressed, SubGenericAction {
    private static String LogClass = "LayoutActivity";
    private Toolbar mSideBarToggle;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private Menu mMenu;
    private CountDownTimer NonPremiumMapTimer;

    private YourPagerAdapter mAdapter;
    private LayoutInflater layoutInflater;
    private OnFragmentBackPressed FragmentBackPressedListener;

    private Context mContext = null;
    public static LayoutActivity LayoutActivityInstance = null;
    Users LoggedInUser;
    GDTimer mUpdateTabIconCounts;
    GDMessagesDBHelper gdMessagesDBHelper;
    GDImageDBHelper gdImageDBHelper;
    public static Boolean TabIconCountsNeedRefresh = true;
    Boolean IsRegistrationFirstEdit = false;
    int tabIcons[] = {R.drawable.ic_favorite_selected, R.drawable.ic_radar, R.drawable.ic_chat_tab, R.drawable.ic_notification_tab};

    Boolean IsSearchActive = false;
    CountDownTimer SearchMessagesTimer;
    Boolean MapViewSelected = false;
    Boolean FilterSelected = false;

    CoordinatorLayout LayoutCoordinatorLayout = null;
    AppBarLayout LayoutAppBarLayout = null;

    Boolean CheckLocationPermissionDoneOnce = false;
    int OnResumeCalledCount = 0;

    PopupWindow LocationsPopup;
    LocationsAdapter locationsAdapter;
    ListView LocationsListView;
    GifMovieView LoadingGIF;
    RelativeLayout AddLocation;
    FloatingActionButton PopularGuysFAB;
    Boolean LocationsLoaded = false;
    Boolean IsLocationsShown = false;
    Boolean IsLayoutActivityOnTop = false;
    Boolean NotificationsAreOffCheckedOnce = false;

    HomePageFragment mHomePageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutActivityInstance = LayoutActivity.this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setContentView(R.layout.activity_layout);
        } else {
            setContentView(R.layout.activity_layout_kitkat);
        }

        int SelectTab = 1;
        if (getIntent().hasExtra("SelectTab")) {
            SelectTab = getIntent().getExtras().getInt("SelectTab", 1);
        }

        mContext = getApplicationContext();
        LoggedInUser = SessionManager.GetLoggedInUser();
        if (LoggedInUser == null) {
            GDLogHelper.Log(LogClass, "onCreate", "SessionManager.GetLoggedInUser() returned null. Logout.");
            SessionManager.UserLogout(mContext, LoggedInUser);
            return;
        }
        if (LoggedInUser == null || LoggedInUser.UserID == null || LoggedInUser.UserID.equals("")) {
            GDLogHelper.Log(LogClass, "onCreate",
                    (LoggedInUser == null ? "LoggedInUser is null. "
                            : LoggedInUser.UserID == null ? "LoggedInUser.UserID is null. "
                            : "LoggedInUser.UserID is empty. ")
                            + "DestroyService(false) and return START_NOT_STICKY.");
            SessionManager.UserLogout(mContext, LoggedInUser);
            return;
        }
        //Start Background Service
        try {
            if (GDMessageService.ServiceInstance == null) {
                Context ccc = getApplicationContext();
                //Context ccc = getActivity().getBaseContext();
                Intent intent = new Intent(ccc, GDMessageService.class);
                ccc.startService(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }

        layoutInflater = LayoutInflater.from(mContext);

        //If after registraion is complete show user profile for edit.
        if (getIntent().hasExtra("IsRegistrationFirstEdit")) {
            IsRegistrationFirstEdit = getIntent().getBooleanExtra("IsRegistrationFirstEdit", false);
        }

        LayoutCoordinatorLayout = findViewById(R.id.LayoutCoordinatorLayout);
        LayoutAppBarLayout = findViewById(R.id.LayoutAppBarLayout);


        mSideBarToggle = findViewById(R.id.SideBarToggle);
        PopularGuysFAB = findViewById(R.id.PopularGuysFAB);
        setSupportActionBar(mSideBarToggle);

        PopularGuysFAB.setOnClickListener(v -> {
            if (HomePageFragment.HomePageInstance.mUserListIsRefreshing) {
                ShowSnackBarOrToast("Please wait while list is refreshing..", TopSnackBar.LENGTH_SHORT, true);
                return;
            }
            if (HomePageFragment.HomePageInstance.IsPopularGuysfilterSelected()) {
                PopularGuysFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorSecondary)));
                mMenu.getItem(5).setVisible(false);
            } else {
                PopularGuysFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent)));
                mMenu.getItem(5).setVisible(true);
            }
            HomePageFragment.HomePageInstance.SelectUnselectPopularGuysfilter(mSideBarToggle);
        });

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(3);
        setupViewPager();
        mViewPager.setCurrentItem(SelectTab);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TabSelected();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (IsSearchActive) {
                    ShowHideActionBarForSearch(false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        setupTabIcons();

        if (SelectTab == 3) {
            //Mark notifications seen.
            if (NotificationsPageFragment.NotificationsPageInstance != null) {
                NotificationsPageFragment.NotificationsPageInstance.MarkNotificationsSeen();
            }
        }

        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> new GDMessagesDBHelper(LayoutActivity.this).UpdatePendingPicMessagesToError(LoggedInUser.UserID), null);

        FirebaseInstanceIDService.UpdateTokenToServerIfNeeded(mContext);

        LoginActivity.CheckForAppUpdate(LayoutActivity.this);

        MessageAndNotificationDownloader.StartDownloadingDirectMessagePics(mContext);
    }

    private void setupTabIcons() {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }
    }

    public void SetOnFragmentBackPressedListener(OnFragmentBackPressed vFragmentBackPressedListener) {
        FragmentBackPressedListener = vFragmentBackPressedListener;
    }

    private void setupViewPager() {
        mAdapter = new YourPagerAdapter(getSupportFragmentManager());
        mAdapter.addFrag(new FavoritesPageFragment());

        mHomePageFragment = new HomePageFragment();
        mAdapter.addFrag(mHomePageFragment);
        mHomePageFragment.SetSubGenericAction(this);

        mAdapter.addFrag(new MessagesPageFragment());
        mAdapter.addFrag(new NotificationsPageFragment());
        mViewPager.setAdapter(mAdapter);
    }

    private void SetIconForTab() {
        View IConWithNumber;

        //For messages
        if (mTabLayout.getTabAt(2).getCustomView() == null) {
            IConWithNumber = layoutInflater.inflate(R.layout.icon_with_number, null);
        } else {
            IConWithNumber = mTabLayout.getTabAt(2).getCustomView();
        }
        ImageView Icon = (ImageView) IConWithNumber.findViewById(R.id.icon);
        TextView Count = (TextView) IConWithNumber.findViewById(R.id.text1);
        int iCount = 0;
        if (gdMessagesDBHelper == null) {
            gdMessagesDBHelper = new GDMessagesDBHelper(mContext);
        }
        iCount = gdMessagesDBHelper.GetAllUnreadInboundMessageCount(LoggedInUser.UserID);
        if (iCount == 0) {
            Count.setVisibility(View.GONE);
        } else {
            Count.setVisibility(View.VISIBLE);
        }
        Icon.setImageBitmap(ImageHelper.getBitmapForResource(getResources(), R.drawable.ic_chat_tab));
        Count.setText(Integer.toString(iCount));
        if (mTabLayout.getTabAt(2).getCustomView() == null) {
            mTabLayout.getTabAt(2).setCustomView(IConWithNumber);
        }

        //For notifications
        if (mTabLayout.getTabAt(3).getCustomView() == null) {
            IConWithNumber = layoutInflater.inflate(R.layout.icon_with_number, null);
        } else {
            IConWithNumber = mTabLayout.getTabAt(3).getCustomView();
        }
        Icon = (ImageView) IConWithNumber.findViewById(R.id.icon);
        Count = (TextView) IConWithNumber.findViewById(R.id.text1);
        if (NotificationsPageFragment.NotificationsPageInstance != null) {
            iCount = NotificationsPageFragment.NotificationsPageInstance.GetUnseenNotificationsCount();
        } else {
            iCount = 0;
        }
        if (iCount == 0) {
            Count.setVisibility(View.GONE);
        } else {
            Count.setVisibility(View.VISIBLE);
        }
        Icon.setImageBitmap(ImageHelper.getBitmapForResource(getResources(), R.drawable.ic_notification_tab));
        Count.setText(Integer.toString(iCount));
        if (mTabLayout.getTabAt(3).getCustomView() == null) {
            mTabLayout.getTabAt(3).setCustomView(IConWithNumber);
        }
    }

    private void ShowHideActionBarForSearch(Boolean Show) {
        try {
            ActionBar actionBar = getSupportActionBar();
            if (Show) {
                PopularGuysFAB.setVisibility(View.GONE);
                IsSearchActive = true;
                mMenu.getItem(0).setVisible(false);
                mMenu.getItem(1).setVisible(false);
                mMenu.getItem(2).setVisible(false);
                mMenu.getItem(3).setVisible(false);
                mMenu.getItem(4).setVisible(false);
                mMenu.getItem(5).setVisible(false);
                mMenu.getItem(6).setVisible(false);
                actionBar.setCustomView(R.layout.layout_search);
                final EditText search = (EditText) actionBar.getCustomView().findViewById(R.id.txt_Search);
                ImageView CloseSearch = (ImageView) actionBar.getCustomView().findViewById(R.id.CloseSearch);

                if (mTabLayout.getSelectedTabPosition() == 1) {
                    search.setHint("Search users (min 3 chars)");
                    if (HomePageFragment.HomePageInstance != null) {
                        HomePageFragment.HomePageInstance.SetSearchActiveState(true);
                    }
                } else if (mTabLayout.getSelectedTabPosition() == 2) {
                    search.setHint("Search chat");
                    if (MessagesPageFragment.MessagesPageInstance != null) {
                        MessagesPageFragment.MessagesPageInstance.SetSearchActive(true);
                    }
                } else {
                    search.setHint("Search");
                }

                search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            //show keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });
                search.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (SearchMessagesTimer != null) {
                            SearchMessagesTimer.cancel();
                        }
                        SearchMessagesTimer = new CountDownTimer(1000, 500) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (mTabLayout.getSelectedTabPosition() == 2) {
                                    if (MessagesPageFragment.MessagesPageInstance != null) {
                                        MessagesPageFragment.MessagesPageInstance.SearchByName(search.getText().toString());
                                    }
                                }
                            }

                            @Override
                            public void onFinish() {
                                if (mTabLayout.getSelectedTabPosition() == 1) {
                                    if (search.getText().toString().length() >= 3) {
                                        if (HomePageFragment.HomePageInstance != null) {
                                            HomePageFragment.HomePageInstance.SearchByName(search.getText().toString());
                                        }
                                    }
                                }
                            }
                        }.start();
                    }
                });
                CloseSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //hide keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(search.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                        ShowHideActionBarForSearch(false);
                    }
                });
                actionBar.setDisplayShowHomeEnabled(false);
                //actionBar.setHomeAsUpIndicator(null);

                mMenu.getItem(0).setVisible(false);
                actionBar.setDisplayShowCustomEnabled(true);
                search.requestFocus();
            } else {
                PopularGuysFAB.setVisibility(View.VISIBLE);
                IsSearchActive = false;
                if (mTabLayout.getSelectedTabPosition() == 1 && HomePageFragment.HomePageInstance != null) {
                    HomePageFragment.HomePageInstance.SetSearchActiveState(false);
                }
                if (mTabLayout.getSelectedTabPosition() == 2 && MessagesPageFragment.MessagesPageInstance != null) {
                    MessagesPageFragment.MessagesPageInstance.SetSearchActive(false);
                }
                if (mMenu != null) {
                    mMenu.getItem(5).setVisible(true);
                }
                //hide keyboard
                this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowHomeEnabled(true);
                if (mTabLayout.getSelectedTabPosition() == 1) {
                    if (MapViewSelected) {
                        mMenu.getItem(0).setVisible(false);
                        mMenu.getItem(1).setVisible(true);
                    } else {
                        mMenu.getItem(0).setVisible(true);
                        mMenu.getItem(1).setVisible(false);
                    }
                    if (FilterSelected) {
                        mMenu.getItem(3).setVisible(false);
                        mMenu.getItem(4).setVisible(true);
                    } else {
                        mMenu.getItem(3).setVisible(true);
                        mMenu.getItem(4).setVisible(false);
                    }
                    mMenu.getItem(2).setVisible(true);
                } else if (mTabLayout.getSelectedTabPosition() == 2) {
                    mMenu.getItem(6).setVisible(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public void SetFilterIcon(Boolean IsSelected, Boolean IsPopularGuysFilterSelected) {
        if (IsSelected) {
            FilterSelected = true;
            mMenu.getItem(3).setVisible(false);
            mMenu.getItem(4).setVisible(true);
        } else {
            FilterSelected = false;
            mMenu.getItem(3).setVisible(true);
            mMenu.getItem(4).setVisible(false);
        }
        if (IsPopularGuysFilterSelected) {
            PopularGuysFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent)));
            mMenu.getItem(5).setVisible(false);
        } else {
            PopularGuysFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorSecondary)));
            mMenu.getItem(5).setVisible(true);
        }
    }

    private void TabSelected() {
        if (mTabLayout.getSelectedTabPosition() == 1
                && HomePageFragment.HomePageInstance != null
                && HomePageFragment.HomePageInstance.bIsMapViewSelected()) {
            ScrollCoordinatorToTop();
        }
        if (mMenu == null) {
            return;
        }
        if (mViewPager.getCurrentItem() != mTabLayout.getSelectedTabPosition()) {
            mViewPager.setCurrentItem(mTabLayout.getSelectedTabPosition());
        }
        if (mTabLayout.getSelectedTabPosition() == 3) {
            mMenu.getItem(5).setVisible(false);
            if (NotificationsPageFragment.NotificationsPageInstance != null) {
                NotificationsPageFragment.NotificationsPageInstance.MarkNotificationsSeen();
                NotificationHelper.CancelAllNotifications();
                SetIconForTab();
            }
        } else if (mTabLayout.getSelectedTabPosition() == 0) {
            mMenu.getItem(5).setVisible(false);
        } else {
            mMenu.getItem(5).setVisible(true);
        }

        //Show/hide mark all messages as read
        if (mTabLayout.getSelectedTabPosition() != 2) {
            mMenu.getItem(6).setVisible(false);
        } else {
            mMenu.getItem(6).setVisible(true);
        }

        if (IsSearchActive) {
            mMenu.getItem(0).setVisible(false);
            mMenu.getItem(1).setVisible(false);
            mMenu.getItem(2).setVisible(false);
            mMenu.getItem(3).setVisible(false);
            mMenu.getItem(4).setVisible(false);
            mMenu.getItem(5).setVisible(false);
            mMenu.getItem(6).setVisible(false);
        } else {
            if (mTabLayout.getSelectedTabPosition() == 1) {
                if (MapViewSelected) {
                    mMenu.getItem(0).setVisible(false);
                    mMenu.getItem(1).setVisible(true);
                } else {
                    mMenu.getItem(0).setVisible(true);
                    mMenu.getItem(1).setVisible(false);
                }
                if (FilterSelected) {
                    mMenu.getItem(3).setVisible(false);
                    mMenu.getItem(4).setVisible(true);
                } else {
                    mMenu.getItem(3).setVisible(true);
                    mMenu.getItem(4).setVisible(false);
                }
                mMenu.getItem(2).setVisible(true);
            } else {
                mMenu.getItem(0).setVisible(false);
                mMenu.getItem(1).setVisible(false);
                mMenu.getItem(2).setVisible(false);
                mMenu.getItem(3).setVisible(false);
                mMenu.getItem(4).setVisible(false);
            }
        }
        //Show/hide mark all messages as read
        if (mTabLayout.getSelectedTabPosition() != 2) {
            mMenu.getItem(6).setVisible(false);
        } else {
            mMenu.getItem(6).setVisible(true);
        }

        if (mTabLayout.getSelectedTabPosition() == 1) {
            if (MapViewSelected) {
                PopularGuysFAB.setVisibility(View.GONE);
            } else {
                PopularGuysFAB.setVisibility(View.VISIBLE);
            }
            if (HomePageFragment.HomePageInstance != null && mMenu != null) {
                if (HomePageFragment.HomePageInstance.IsPopularGuysfilterSelected()) {
                    mMenu.getItem(5).setVisible(false);
                } else {
                    mMenu.getItem(5).setVisible(true);
                }
            }
        } else {
            PopularGuysFAB.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (FragmentBackPressedListener != null) {
            //LayoutActivity.this.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentBackPressedListener.BackPressed();
        } else if (IsSearchActive) {
            ShowHideActionBarForSearch(false);
        } else if (mTabLayout.getSelectedTabPosition() != 1) {
            mTabLayout.getTabAt(1).select();
        } else if (MapViewSelected) {
            MapViewSelected = false;
            mMenu.getItem(0).setVisible(true);
            mMenu.getItem(1).setVisible(false);
            HomePageFragment.HomePageInstance.HomeGridViewSelected();
        } else if (HomePageFragment.HomePageInstance != null && HomePageFragment.HomePageInstance.AreFiltersSelected()) {
            HomePageFragment.HomePageInstance.FiltersDeselectedOnBackPressed(mSideBarToggle);
        } else if (IsLocationsShown) {
            ShowHideLocationsDropDown(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void BackPressed() {
    }

    @Override
    public void onResume() {
        super.onResume();
        MasterDataHelper.InitMasterDataHelper(LayoutActivity.this);
        TabSelected();

        if (IsRegistrationFirstEdit) {
            Intent intent = new Intent(LayoutActivity.this, NewProfileViewActivity.class);
            intent.putExtra("ClickedUser", LoggedInUser);
            intent.putExtra("IsRegistrationFirstEdit", true);
            if (LoggedInUser.PicID != null && !LoggedInUser.PicID.trim().equals("")) {
                intent.putExtra("ProfilePicID", LoggedInUser.PicID);
            }
            startActivity(intent);
            IsRegistrationFirstEdit = false;
        }
        mUpdateTabIconCounts = new GDTimer(2000, 1500, new Handler(), () -> {
            if (TabIconCountsNeedRefresh) {
                TabIconCountsNeedRefresh = false;
                SetIconForTab();
            }
        });
        mUpdateTabIconCounts.Start();
        CheckLocationPermissionAndInit();

        OnResumeCalledCount++;
        if (OnResumeCalledCount == 6) {
            GDRatingsHelper.ShowRateAppIfNeeded(LayoutActivity.this);
        }

        IsLayoutActivityOnTop = true;
        StartMapFreemiumTimer();

        if (OnResumeCalledCount == 2 && !NotificationsAreOffCheckedOnce) {
            GDCountDownTimer.StartCountDown(1500, () -> {
                NotificationsAreOffCheckedOnce = true;
                ShowNotificationsAreOffAlertIfNeeded();
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        GPSHelper.DeInitGPSHelper();
        mUpdateTabIconCounts.Stop();
        mUpdateTabIconCounts = null;
        IsLayoutActivityOnTop = false;
    }

    @Override
    protected void onDestroy() {
        //LogoutFromServer(LoggedInUser.UserID);
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (GPSHelper.CheckLocationAvailability(LayoutActivity.this)) {
                    //Do nothing, let the user select GPS again from locations dropdown
                    GPSHelper.GetGPSLocation(LayoutActivity.this);
                }
                break;
            case 2:
                if (GPSHelper.IsLocationPermissionGranted(LayoutActivityInstance)) {
                    GPSHelper.InitGPSHelper(mContext);
                }
                break;
            case 3:
                //Location Added
                try {
                    UserLocation userLocation = data.getExtras().getParcelable("Location");
                    locationsAdapter.AddUpdLocation(userLocation);
                } catch (Exception ex) {
                    RefreshLocationsList();
                }
                break;
            case 4:
                if (GPSHelper.CheckLocationAvailability(LayoutActivity.this)) {
                    //Location permission granted
                    //Do nothing, let the user select GPS again from locations dropdown
                    GPSHelper.GetGPSLocation(LayoutActivity.this);
                }
                break;
        }
    }


    private void StartMapFreemiumTimer() {
        if (NonPremiumMapTimer != null) {
            NonPremiumMapTimer.cancel();
        }
        if (GDGenericHelper.IsUserPremium(mContext) || !MapViewSelected) {
            return;
        }
        NonPremiumMapTimer = new CountDownTimer(10000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                try {
//                    ActivityManager activityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
//                    Boolean isLayoutActivityOnTop = activityManager.getAppTasks().get(0).getTaskInfo().topActivity.getClassName().endsWith("LayoutActivity");
                    Boolean isMapViewSelected = mMenu.getItem(1).isVisible();
                    if (isMapViewSelected) {
                        OnGridViewSelected();
                        if (IsLayoutActivityOnTop) {
                            GDGenericHelper.ShowBuyPremiumIfNotPremium(mContext,
                                    "Get GDudes premium to continue searching guys on map.", true);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    GDLogHelper.LogException(ex);
                }
            }
        };
        NonPremiumMapTimer.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        mMenu.getItem(0).setVisible(true);
        mMenu.getItem(1).setVisible(false);
        mMenu.getItem(2).setVisible(true);
        mMenu.getItem(3).setVisible(true);
        mMenu.getItem(4).setVisible(false);
        mMenu.getItem(5).setVisible(true);
        mMenu.getItem(6).setVisible(false);
        TabSelected();
        return super.onCreateOptionsMenu(mMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_MapView:
                if (HomePageFragment.HomePageInstance.mUserListIsRefreshing) {
                    ShowSnackBarOrToast("Please wait while list is refreshing..", TopSnackBar.LENGTH_SHORT, true);
                    break;
                }
                PopularGuysFAB.setVisibility(View.GONE);
                MapViewSelected = true;
                mMenu.getItem(0).setVisible(false);
                mMenu.getItem(1).setVisible(true);
                HomePageFragment.HomePageInstance.HomeMapViewSelected();
                ScrollCoordinatorToTop();
                return true;
            case R.id.action_GridView:
//                if (HomePageFragment.HomePageInstance.mUserListIsRefreshing) {
//                    break;
//                }
                OnGridViewSelected();
                return true;
            case R.id.action_Locations:
                ShowHideLocationsDropDown(true);
                return true;
            case R.id.action_Filter:
                HomePageFragment.HomePageInstance.ShowHideFilterDropDown(true, mSideBarToggle);
                return true;
            case R.id.action_FilterSelected:
                HomePageFragment.HomePageInstance.ShowHideFilterDropDown(true, mSideBarToggle);
                return true;
            case R.id.action_Search:
                ShowHideActionBarForSearch(true);
                return true;
            case R.id.action_MarkAllRead:
                MessagesPageFragment.MessagesPageInstance.MarkAllRead();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void OnGridViewSelected() {
        PopularGuysFAB.setVisibility(View.VISIBLE);
        MapViewSelected = false;
        mMenu.getItem(0).setVisible(true);
        mMenu.getItem(1).setVisible(false);
        HomePageFragment.HomePageInstance.HomeGridViewSelected();
    }



    private void ShowSnackBarOrToast(String Message, int duration, Boolean IsError) {
        if (HomePageFragment.HomePageInstance != null) {
            HomePageFragment.HomePageInstance.ShowSnackBar(Message, duration, IsError);
        } else {
            GDToastHelper.ShowToast(LayoutActivity.this, Message, IsError ? GDToastHelper.ERROR : GDToastHelper.INFO, GDToastHelper.SHORT);
        }
    }

    private void CheckLocationPermissionAndInit() {
        if (GPSHelper.IsLocationPermissionGranted(LayoutActivityInstance)) {
            GPSHelper.InitGPSHelper(mContext);
        } else if (!CheckLocationPermissionDoneOnce) {
            GPSHelper.AskLocationPermission(LayoutActivityInstance, 2);
        }
        CheckLocationPermissionDoneOnce = true;
    }

    public void ScrollCoordinatorToTop() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) LayoutAppBarLayout.getLayoutParams();
                AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                if (behavior != null) {
                    behavior.onNestedFling(LayoutCoordinatorLayout, LayoutAppBarLayout, null, 0, -10000, true);
                }
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public void RefreshTabIconCount() {
        SetIconForTab();
    }

    public void ShowHideLocationsDropDown(Boolean Show) {
        if (Show) {
            if (LocationsPopup == null) {
                LocationsPopup = new PopupWindow(mContext);
                View layout = LayoutInflater.from(mContext).inflate(R.layout.filter_layout, null);
                LocationsListView = layout.findViewById(R.id.FilterListView);
                LoadingGIF = layout.findViewById(R.id.LoadingGIF);
                AddLocation = layout.findViewById(R.id.Addfilter);
                TextView AddLocationText = layout.findViewById(R.id.AddFilterText);
                AddLocationText.setText("Add Location");

                locationsAdapter = new LocationsAdapter(new ArrayList<UserLocation>(), new LocationChangedListenner() {
                    @Override
                    public void LocationChanged(int position, String LocationID) {
                        if (position == 0) {
                            SessionManager.SetUseGPS("1");
                            HomePageFragment.HomePageInstance.ReloadList("");
                        } else {
                            SessionManager.SetUseGPS("0");
                            HomePageFragment.HomePageInstance.ReloadList(LocationID);
                        }
                        LocationsPopup.dismiss();
                    }
                });
                LocationsListView.setAdapter(locationsAdapter);
                LocationsPopup.setContentView(layout);

                // Set content width and height
                Point size = new Point();
                this.getWindowManager().getDefaultDisplay().getSize(size);
                Double dWidth = size.x * 0.6;
                LocationsPopup.setWidth(dWidth.intValue());
                LocationsPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                // Closes the popup window when touch outside of it - when looses focus
                LocationsPopup.setOutsideTouchable(true);
                LocationsPopup.setFocusable(true);
                BitmapDrawable bd = new BitmapDrawable();
                bd.setAlpha(0);
                LocationsPopup.setBackgroundDrawable(bd);
                LocationsPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //Reload home page fragment
                        IsLocationsShown = false;
                    }
                });

                AddLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, GDMapActivity.class);
                        intent.putExtra("Activity_Mode", GDMapActivity.NEW_EDIT_USER_LOCATION);
                        startActivityForResult(intent, 3);
                    }
                });
            }
            // Show anchored to app bar
            LocationsPopup.showAsDropDown(mSideBarToggle, 10, 0, Gravity.RIGHT);
            if (!LocationsLoaded) {
                RefreshLocationsList();
            }
            IsLocationsShown = true;
        } else {
            if (LocationsPopup != null) {
                LocationsPopup.dismiss();
            }
            IsLocationsShown = false;
        }
    }

    private void RefreshLocationsList() {
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_PageNo, "1"));
        APICallInfo apiCallInfo = new APICallInfo("Home", "GetUserLocations", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        ShowHideLocationsGif(true);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result == null || result.equals("") || result.equals("-1")) {
                        return;
                    }
                    ArrayList<UserLocation> UserLocationList = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<UserLocation>>() {
                    }.getType());
                    if (UserLocationList.size() > 0) {
                        locationsAdapter.SetDataSource(UserLocationList);
                    }
                    LocationsLoaded = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                } finally {
                    ShowHideLocationsGif(false);
                }
            }
        }, new APINoNetwork() {
            @Override
            public void onAPINoNetwork() {
                ShowSnackBarOrToast(getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true);
                ShowHideLocationsGif(false);
            }
        });
    }

    private void ShowHideLocationsGif(Boolean show)
    {
        if (LoadingGIF != null) {
            if(show) {
                LoadingGIF.setVisibility(View.VISIBLE);
            } else {
                LoadingGIF.setVisibility(View.GONE);
            }
        }
    }


    private void ShowNotificationsAreOffAlertIfNeeded() {
        if (!NotificationManagerCompat.from(LayoutActivity.this).areNotificationsEnabled()) {
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            if (uri != null) {
                GDDialogHelper.ShowYesNoTypeDialog(LayoutActivity.this, "Missing out on notification alerts?",
                        "Notifications for GDudes are turned off.\nDo you want to turn them on now?",
                        GDDialogHelper.BUTTON_TEXT_YES, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT, () -> {
                            GDDialogHelper.ShowSingleButtonTypeDialog(LayoutActivity.this, "Turn on notifications!",
                                    "• Click Ok to open app settings.\n• Go to App Notifications and enable.\n",
                                    GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.INFO, () -> {
                                        EnableNotificationsInAppSetting();
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    });
                        }, null);
            }
        } else if (!PersistantPreferencesHelper.GetAppSettings().ShowNotifications.equals("1")) {
            GDDialogHelper.ShowYesNoTypeDialog(LayoutActivity.this, "Missing out on notification alerts?",
                    "Notifications for GDudes are turned off.\nDo you want to turn them on now?",
                    GDDialogHelper.BUTTON_TEXT_YES, GDDialogHelper.BUTTON_TEXT_CANCEL,
                    GDDialogHelper.ALERT, () -> {
                        EnableNotificationsInAppSetting();
                        GDToastHelper.ShowToast(LayoutActivity.this, "Notifications enabled!", GDToastHelper.INFO, GDToastHelper.SHORT);
                    }, null);
        }
    }

    private void EnableNotificationsInAppSetting() {
        AppSettings appSettings = PersistantPreferencesHelper.GetAppSettings();
        appSettings.ShowNotifications = "1";
        PersistantPreferencesHelper.SetAppSettings(appSettings);
    }

    @Override
    public void OnSubGenericAction(String action, Object data) {
        switch (action) {
            case "StartMapFreemiumTimer":
                StartMapFreemiumTimer();
                break;
            case "CloseMapView":
                OnGridViewSelected();
                break;
        }
    }

    class LocationsAdapter extends BaseAdapter {
        ArrayList<UserLocation> mLocationsList;
        LocationChangedListenner locationChangedListenner;
        String SelectedID = "";

        public LocationsAdapter(ArrayList<UserLocation> LocationsList,
                                LocationChangedListenner vlocationChangedListenner) {
            mLocationsList = new ArrayList<>();
            AddStaticLocations();
            mLocationsList.addAll(LocationsList);
            locationChangedListenner = vlocationChangedListenner;
        }

        public void SetDataSource(ArrayList<UserLocation> LocationsList) {
            if (LocationsList != null) {
                mLocationsList.clear();
                AddStaticLocations();
                mLocationsList.addAll(LocationsList);
                if (!SessionManager.GetUseGPS().equals("1")) {
                    for (int i = 0; i < mLocationsList.size(); i++) {
                        if (mLocationsList.get(i).PrimaryLocation) {
                            if (!SessionManager.GetUseGPS().equals("1")) {
                                mLocationsList.get(i).IsSelected = true;
                                SelectedID = mLocationsList.get(i).LocationID;
                            }
                        }
                    }
                }
                notifyDataSetChanged();
            }
        }

        private void AddStaticLocations() {
            UserLocation location = new UserLocation();
            location.LocationID = GDGenericHelper.GetNewGUID();
            location.LocationNickName = "GPS Location";
            if (SessionManager.GetUseGPS().equals("1")) {
                location.IsSelected = true;
                SelectedID = location.LocationID;
            }
            mLocationsList.add(location);
        }

        private void AddUpdLocation(UserLocation location) {
            if (!mLocationsList.contains(location)) {
                mLocationsList.add(location);
            } else {
                int index = mLocationsList.indexOf(location);
                if (index >= 0) {
                    if (mLocationsList.get(index).IsSelected) {
                        location.IsSelected = true;
                    }
                    mLocationsList.set(index, location);
                }
            }
            notifyDataSetChanged();
        }

        private void RemoveLocation(int position) {
            mLocationsList.remove(position);
            notifyDataSetChanged();
        }

        public UserLocation GetSelectedLocation() {
            for (int i = 0; i < mLocationsList.size(); i++) {
                if (mLocationsList.get(i).IsSelected) {
                    return mLocationsList.get(i);
                }
            }
            return null;
        }

        private void SelectOneLocations(int position) {
            for (int i = 0; i < mLocationsList.size(); i++) {
                if (i == position) {
                    mLocationsList.get(i).IsSelected = true;
                    SelectedID = mLocationsList.get(i).LocationID;
                } else {
                    mLocationsList.get(i).IsSelected = false;
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mLocationsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mLocationsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(LayoutActivity.this).inflate(R.layout.location_item, null);
            final LocationListViewHolder locationListViewHolder = new LocationListViewHolder();
            locationListViewHolder.LocationRadio = convertView.findViewById(R.id.LocationRadio);
            locationListViewHolder.LocationNickName = convertView.findViewById(R.id.LocationNickName);
            locationListViewHolder.Edit = convertView.findViewById(R.id.Edit);
            locationListViewHolder.Delete = convertView.findViewById(R.id.Delete);
            locationListViewHolder.LocationNickName.setText(mLocationsList.get(position).LocationNickName);

            if (position == 0 || (position == 1 && mLocationsList.size() == 2)) {
                if (position == 0) {
                    locationListViewHolder.Edit.setVisibility(View.GONE);
                    locationListViewHolder.Delete.setVisibility(View.GONE);
                }
            }

            if (mLocationsList.get(position).IsSelected) {
                locationListViewHolder.LocationRadio.setChecked(true);
            } else {
                locationListViewHolder.LocationRadio.setChecked(false);
            }

            locationListViewHolder.LocationRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked) {
                    SelectOneLocations(position);
                }
                if (isChecked && mLocationsList.get(position).LocationID.equalsIgnoreCase(SelectedID)) {
                    return;
                }
                if (isChecked) {
                    if (position == 0) {
                        if (GPSHelper.CheckLocationAvailability(LayoutActivity.this)) {
                            LatLng GPSLatLng = GPSHelper.GetGPSLatLng(LayoutActivity.this);
                            if (GPSLatLng == null || GPSHelper.GetStringFromLatLng(GPSLatLng).trim().equals("")) {
                                ShowHideLocationsDropDown(false);
                                ShowSnackBarOrToast("Could not get GPS location!", TopSnackBar.LENGTH_SHORT, true);
                                mLocationsList.get(0).IsSelected = false;
                                notifyDataSetChanged();
                            } else {
                                locationChangedListenner.LocationChanged(position, "");
                                SelectOneLocations(position);
                            }
                        } else {
                            GPSHelper.AskToEnableLocation(LayoutActivity.this, LayoutActivityInstance, 4);
                            notifyDataSetChanged();
                        }
                    } else {
                        SelectOneLocations(position);
                        locationChangedListenner.LocationChanged(position, mLocationsList.get(position).LocationID);
                    }
                }
            });

            locationListViewHolder.LocationNickName.setOnClickListener(v -> {
                if (locationListViewHolder.LocationRadio.isChecked()) {
                    locationListViewHolder.LocationRadio.setChecked(false);
                } else {
                    locationListViewHolder.LocationRadio.setChecked(true);
                }
            });

            locationListViewHolder.Edit.setOnClickListener(v -> {
                Intent intent = new Intent(LayoutActivity.this, GDMapActivity.class);
                intent.putExtra("Activity_Mode", GDMapActivity.NEW_EDIT_USER_LOCATION);
                intent.putExtra("Location", mLocationsList.get(position));
                startActivityForResult(intent, 3);
            });
            locationListViewHolder.Delete.setOnClickListener(v -> GDDialogHelper.ShowYesNoTypeDialog(LayoutActivity.this, "Delete Location", "Are you sure you want to delete this location?", GDDialogHelper.BUTTON_TEXT_DELETE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT, new OnDialogButtonClick() {
                @Override
                public void dialogButtonClicked() {
                    DeleteUserLocation deleteUserLocation = new DeleteUserLocation(LoggedInUser.UserID, mLocationsList.get(position).LocationID);
                    APICallInfo apiCallInfo = new APICallInfo("Home", "DeleteUserLocation", null, "POST", deleteUserLocation, null, false,
                            new APIProgress(LayoutActivity.this, "Deleting location..", true), APICallInfo.APITimeouts.MEDIUM);
                    GDGenericHelper.executeAsyncPOSTAPITask(LayoutActivity.this, apiCallInfo, new APICallback() {
                        @Override
                        public void onAPIComplete(String result, Object ExtraData) {
                            try {
                                if (StringHelper.TrimFirstAndLastCharacter(result).equals("1")) {
                                    ShowSnackBarOrToast("Location deleted", TopSnackBar.LENGTH_SHORT, false);
                                    RemoveLocation(position);
                                } else {
                                    ShowSnackBarOrToast(getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true);
                                }
                            } catch (Exception e) {
                                ShowSnackBarOrToast(getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true);
                                e.printStackTrace();
                                GDLogHelper.LogException(e);
                            }
                        }
                    }, () -> ShowSnackBarOrToast(getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true));
                }
            }, null));

            return convertView;
        }
    }

    private static class LocationListViewHolder {
        RadioButton LocationRadio = null;
        TextView LocationNickName = null;
        ImageView Edit = null;
        ImageView Delete = null;
    }

    interface LocationChangedListenner {
        void LocationChanged(int position, String LocationID);
    }

    class YourPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public YourPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
        }
    }
}

