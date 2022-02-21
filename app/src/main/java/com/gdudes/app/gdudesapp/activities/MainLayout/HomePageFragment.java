package com.gdudes.app.gdudesapp.activities.MainLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.Adapters.UserListGridAdapter;
import com.gdudes.app.gdudesapp.CustomViewTypes.GifMovieView;
import com.gdudes.app.gdudesapp.CustomViewTypes.HeaderGridView;
import com.gdudes.app.gdudesapp.GDTypes.FirstPageNearByUsers;
import com.gdudes.app.gdudesapp.GDTypes.NearbyUser;
import com.gdudes.app.gdudesapp.GDTypes.SavedFilter;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDUnitHelper;
import com.gdudes.app.gdudesapp.Helpers.GPSHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.APIFailureCallback;
import com.gdudes.app.gdudesapp.Interfaces.APISuccessCallback;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.Interfaces.OnFragmentBackPressed;
import com.gdudes.app.gdudesapp.Interfaces.SubGenericAction;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomePageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    UserListGridAdapter madapter;
    Users LoggedInUser = null;
    Context mcontext = null;

    public Boolean mUserListIsRefreshing = false;
    int mUserListLastPageCalled = 0;
    Boolean IsFilterDropDownShown = false;
    Boolean IsMapViewSelected = false;
    Boolean AreUserFiltersLoaded = false;
    Boolean AllResultsLoaded = false;
    Boolean NonPremiumLimitReached = false;

    LayoutInflater HomePageInflater;
    HeaderGridView gridView = null;
    SwipeRefreshLayout swipeUserList;
    FrameLayout GDMapLayout;
    RelativeLayout HomeMainLayout;
    RelativeLayout GetPremiumToLoadMoreGuys;
    Button btnGetPremium;
    View AttachFilterToView = null;
    RelativeLayout PopularGuysSelectedInfoView;
    Button btnPopularGuysInfo;

    PopupWindow FilterPopup;
    ListView FilterListView;
    GifMovieView LoadingGIF;
    RelativeLayout Addfilter;
    FilterAdapter mFilterAdapter;
    ArrayList<SavedFilter> SavedFiltersList;
    Boolean IsOnlineSelected = false;
    Boolean IsRecentlyOnlineSelected = false;
    Boolean IsShowPopularGuysSelected = false;
    Boolean IsWithPicsSelected = false;
    Boolean IsHideBlockedSelected = false;
    String SelectedCustomFilterSearchID = "";
    String UpdatedFilterID = "";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GDInfoWindowAdapter mGDInfoWindowAdapter;
    private ArrayList<LocationAndUserList> mLocationAndUserList = new ArrayList<>();

    private static Boolean IsSearchActive = false;
    public static HomePageFragment HomePageInstance = null;
    private String SearchPhrase = "";

    private RelativeLayout ContentLoadedContainer = null;
    private RelativeLayout ContentLoadingContainer = null;
    private TextView ContentLoadingText = null;

    private FloatingActionButton MapViewNext;
    private FloatingActionButton MapViewPrev;
    private FloatingActionButton MapViewReload;
    static int MapViewCurrentSelection = -1;

    //The key used to store list of users on this frame
    private static final String STATE_USERS = "state_users";
    Date LastPauseTime = null;
    Boolean CheckedForLocationOnce = false;

    private Boolean IsPrivacyAlertShown = false;
    private SubGenericAction mSubGenericAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        HomePageInstance = HomePageFragment.this;
        HomePageInflater = inflater;
        View homePagelayout = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            homePagelayout = inflater.inflate(R.layout.homepagemain_layout, container, false);
        } else {
            homePagelayout = inflater.inflate(R.layout.homepagemain_layout_kitkat, container, false);
        }
        mcontext = this.getActivity();
        LoggedInUser = SessionManager.GetLoggedInUser(mcontext);

        HomeMainLayout = homePagelayout.findViewById(R.id.HomeMainLayout);
        ContentLoadedContainer = homePagelayout.findViewById(R.id.ContentLoadedContainer);
        ContentLoadingContainer = homePagelayout.findViewById(R.id.ContentLoadingContainer);
        ContentLoadingText = homePagelayout.findViewById(R.id.ContentLoadingText);
        GetPremiumToLoadMoreGuys = homePagelayout.findViewById(R.id.GetPremiumToLoadMoreGuys);
        btnGetPremium = homePagelayout.findViewById(R.id.btnGetPremium);
        gridView = homePagelayout.findViewById(R.id.gvGrid);
        MapViewNext = homePagelayout.findViewById(R.id.MapViewNext);
        MapViewPrev = homePagelayout.findViewById(R.id.MapViewPrev);
        MapViewReload = homePagelayout.findViewById(R.id.MapViewReload);
        PopularGuysSelectedInfoView = (RelativeLayout) LayoutInflater.from(mcontext).inflate(R.layout.popular_guys_selected_info_view, null);
        btnPopularGuysInfo = PopularGuysSelectedInfoView.findViewById(R.id.btnPopularGuysInfo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gridView.setNestedScrollingEnabled(true);
        }
        swipeUserList = homePagelayout.findViewById(R.id.swipeUserList);
        GDMapLayout = homePagelayout.findViewById(R.id.GDMapLayout);

        try {

            swipeUserList.setOnRefreshListener(this);
            gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (madapter != null && !mUserListIsRefreshing) {
                        int TotalCount = madapter.getCount();
                        if (TotalCount < 60) {
                            return;
                        }
                        //uncomment
                        //if ((firstVisibleItem + visibleItemCount) >= TotalCount && TotalCount != 0) {
                        if ((firstVisibleItem + visibleItemCount) >= (TotalCount - 36)) {
                            if (NonPremiumLimitReached && !GDGenericHelper.IsUserPremium(mcontext)) {
                                GetPremiumToLoadMoreGuys.setVisibility(View.VISIBLE);
                                return;
                            }
                            if (AllResultsLoaded) {
                                return;
                            }
                            RefreshUserList(mUserListLastPageCalled + 1, "");
                        } else {
                            if (GetPremiumToLoadMoreGuys.getVisibility() == View.VISIBLE) {
                                GetPremiumToLoadMoreGuys.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });

            Point size = new Point();
            this.getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            float scaleFactor = size.x / 3;
            int imageWidth = (int) (scaleFactor / getResources().getDisplayMetrics().density);
            DisplayMetrics metrics = new DisplayMetrics();
            this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float logicalDensity = metrics.density;

            madapter = new UserListGridAdapter(HomePageInflater.getContext(), imageWidth, imageWidth, logicalDensity, LoggedInUser);
            gridView.setAdapter(madapter);

            gridView.setOnItemClickListener((parent, view, position, id) -> {
                Users user = (Users)((HeaderGridView) parent).getAdapter().getItem(position);
                Intent intent = new Intent(mcontext, NewProfileViewActivity.class);
                intent.putExtra("ClickedUserID", user.UserID);
                if (!StringHelper.IsNullOrEmpty(user.PicID)) {
                    intent.putExtra("ProfilePicID", user.PicID);
                }
                mcontext.startActivity(intent);
            });
            btnPopularGuysInfo.setOnClickListener(v -> {
                Intent intent = new Intent(mcontext, GDPopularGuysInfoActivity.class);
                startActivity(intent);
            });
            btnGetPremium.setOnClickListener(v -> {
                GDGenericHelper.ShowBuyPremiumIfNotPremium(mcontext, "Get GDudes premium to see twice as many guys..", false);
            });
            ContentLoadingContainer.setOnClickListener(v -> {
                if (!mUserListIsRefreshing) {
                    mUserListLastPageCalled = 0;
                    RefreshUserList(mUserListLastPageCalled + 1, "");
                }
            });
            MapViewNext.setOnClickListener(v -> MoveMapToUser(true));
            MapViewPrev.setOnClickListener(v -> MoveMapToUser(false));
            MapViewReload.setOnClickListener(v -> {
                mUserListLastPageCalled = 0;
                RefreshUserList(mUserListLastPageCalled + 1, "");
            });

            //Resume GridView and data
            AllResultsLoaded = false;
            mUserListLastPageCalled = 0;
            IsMapViewSelected = false;
            swipeUserList.setVisibility(View.VISIBLE);
            GDMapLayout.setVisibility(View.GONE);
            mLocationAndUserList = new ArrayList<>();

            Boolean RefreshListNeeded = true;
            try {
                FirstPageNearByUsers firstPageNearByUsers = SessionManager.GetFirstPageNearByUsers();
                if (firstPageNearByUsers != null && firstPageNearByUsers.UsersJSON != null && !firstPageNearByUsers.UsersJSON.trim().equals("")) {
                    ArrayList<Users> UsersList = new GsonBuilder().create().fromJson(firstPageNearByUsers.UsersJSON, new TypeToken<List<Users>>() {
                    }.getType());
                    if (UsersList != null && UsersList.size() > 0) {
                        madapter.ReplaceUserList(UsersList, !IsMapViewSelected, !IsShowPopularGuysSelected);
                        ContentLoadingContainer.setVisibility(View.GONE);
                        ContentLoadedContainer.setVisibility(View.VISIBLE);
                    }
                    Date TimeLastUpd = GDDateTimeHelper.GetDateFromString(firstPageNearByUsers.LastStoreDT);
                    Date TimeNow = GDDateTimeHelper.GetDateFromString(GDDateTimeHelper.GetCurrentDateTimeAsString(false));
                    long secs = (TimeNow.getTime() - TimeLastUpd.getTime()) / 1000;
                    if (secs < 300) {
                        RefreshListNeeded = false;
                        mUserListLastPageCalled = 1;
                    }
                    GetUserPics(UsersList, false);
                }
            } catch (Exception ex) {
                RefreshListNeeded = true;
            }

            if (RefreshListNeeded) {
                swipeUserList.setRefreshing(true);
                RefreshUserList(mUserListLastPageCalled + 1, "");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }

        return homePagelayout;
    }

    public void SetSubGenericAction(SubGenericAction subGenericAction) {
        mSubGenericAction = subGenericAction;
    }

    private void RefreshUserList(final int pageNumber, String LocationID) {
        try {
            if (!CheckedForLocationOnce && pageNumber == 1) {
                if (SessionManager.GetUseGPS().equals("1") && !GPSHelper.CheckLocationAvailability(mcontext)) {
                    GDDialogHelper.ShowSingleButtonTypeDialog(mcontext, "Please Enable location", "GDudes needs to access your location",
                            GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.ALERT, () -> {
                                CheckedForLocationOnce = true;
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                            });

                }
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        if (pageNumber == 1) {
            AllResultsLoaded = false;
            NonPremiumLimitReached = false;
            if (madapter.getCount() == 0) {
                ContentLoadingText.setText("Loading guys nearby..");
            }
        }
        mUserListIsRefreshing = true;
        swipeUserList.setRefreshing(true);
        MapViewReload.setVisibility(View.INVISIBLE);
        int APIVersion = 0;
        try {
            APIVersion = Build.VERSION.SDK_INT;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_PageNo, Integer.toString(pageNumber)));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_OnlineOnly, IsOnlineSelected.toString()));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_RecentlyOnline, IsRecentlyOnlineSelected.toString()));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_GetPopularGuys, IsShowPopularGuysSelected.toString()));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_WithPics, IsWithPicsSelected.toString()));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_HideBlocked, IsHideBlockedSelected.toString()));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_SearchPhrase, IsSearchActive ? SearchPhrase : ""));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_APIVersion, Integer.toString(APIVersion)));
        if (!SelectedCustomFilterSearchID.equals("")) {
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_SearchID, SelectedCustomFilterSearchID));
        }
        if (SessionManager.GetUseGPS().equals("1")) {
            Location location = GPSHelper.GetGPSLocation(mcontext);
            if (location != null) {
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_LocationLatLng, GPSHelper.GetStringFromLocation(location)));
            }
        }
        if (LocationID != null && !LocationID.trim().equals("")) {
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_LocationID, LocationID.trim()));
        }
        APICallInfo apiCallInfo = new APICallInfo("Home", "GetNearByUsers_New",
                pAPICallParameters, "GET", null, IsShowPopularGuysSelected,
                false, null, APICallInfo.APITimeouts.SEMILONG);
        GDGenericHelper.executeAsyncAPITask(mcontext, apiCallInfo, (result, ExtraData) -> {
            swipeUserList.setRefreshing(false);
            MapViewReload.setVisibility(View.VISIBLE);
            try {
                if (result == null || result.equals("") || result.equals("-1")) {
                    return;
                }
                mUserListLastPageCalled = pageNumber;
                if (result.equals("0")) {
                    if (pageNumber == 1) {
                        madapter.ReplaceUserList(new ArrayList<>(), !IsMapViewSelected, !(Boolean) ExtraData);
                        return;
                    }
                    AllResultsLoaded = true;
                    return;
                } else if (result.equals("-99")) {
                    if (!GDGenericHelper.IsUserPremium(mcontext)) {
                        GetPremiumToLoadMoreGuys.setVisibility(View.VISIBLE);
                    }
                    AllResultsLoaded = true;
                    NonPremiumLimitReached = true;
                    return;
                }

                ArrayList<Users> UsersList;
                JSONObject jsonObject = new JSONObject(result);
                ArrayList<NearbyUser> NearbyUsersList = new GsonBuilder().create().fromJson(jsonObject.
                        getString("NearbyUsers"), new TypeToken<List<NearbyUser>>() {}.getType());

                ArrayList<MyNearbySearchData> myNearbySearchData = new GsonBuilder().create().fromJson(jsonObject.
                        getString("MyData"), new TypeToken<List<MyNearbySearchData>>() {}.getType());

                if (myNearbySearchData.size() > 0) {
                    UsersList = NearbyUser.ConvertToUsers(NearbyUsersList,
                            myNearbySearchData.get(0).ShowMyDistance,
                            myNearbySearchData.get(0).MyShowInMapSearch);
                } else {
                    UsersList = NearbyUser.ConvertToUsers(NearbyUsersList);
                }

                UserObjectsCacheHelper.AddUpdUserListToCache(UsersList);
                if (pageNumber == 1) {
                    madapter.ReplaceUserList(UsersList, !IsMapViewSelected, !(Boolean) ExtraData);
                    if(mFilterAdapter.IsPopularGuysfilterSelected()) {
                        gridView.setAdapter(null);
                        gridView.addHeaderView(PopularGuysSelectedInfoView);
                        gridView.setAdapter(madapter);
                    } else if (UsersList != null && UsersList.size() > 0) {
                        SaveFirstPageUsersIfNeeded(result);
                    }
                } else {
                    madapter.AppendUserList(UsersList, !IsMapViewSelected, !(Boolean) ExtraData);
                }
                if (UsersList.size() > 0) {
                    GetUserPics(UsersList, true);
                }
                if (IsMapViewSelected) {
                    if (pageNumber == 1) {
                        if (mMap != null) {
                            mMap.clear();
                        }
                        mLocationAndUserList = new ArrayList<>();
                    }
                    AddMapMarkers(madapter.Userlist);
                    if (pageNumber == 1 && madapter.Userlist.size() > 0) {
                        MoveCameraToFirst();
                    }
                    if (UsersList.size() > 0 && pageNumber != 1) {
                        MapViewNext.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                GDLogHelper.LogException(e);
            } finally {
                mUserListIsRefreshing = false;
                swipeUserList.setRefreshing(false);
                MapViewReload.setVisibility(View.VISIBLE);
                if (madapter.getCount() == 0) {
                    ContentLoadingContainer.setVisibility(View.VISIBLE);
                    ContentLoadedContainer.setVisibility(View.GONE);
                    ContentLoadingText.setText("Nothing to show. Touch to reload");
                } else {
                    ContentLoadingContainer.setVisibility(View.GONE);
                    ContentLoadedContainer.setVisibility(View.VISIBLE);
                }
            }
        }, () -> {
            try {
                TopSnackBar.MakeSnackBar(getActivity().findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                mUserListIsRefreshing = false;
                swipeUserList.setRefreshing(false);
                MapViewReload.setVisibility(View.VISIBLE);
                if (madapter.getCount() == 0) {
                    ContentLoadingContainer.setVisibility(View.VISIBLE);
                    ContentLoadedContainer.setVisibility(View.GONE);
                    ContentLoadingText.setText("Nothing to show. Touch to reload");
                } else {
                    ContentLoadingContainer.setVisibility(View.GONE);
                    ContentLoadedContainer.setVisibility(View.VISIBLE);
                }
            } catch (Exception ex) {

            }
        });
    }

    private void GetUserPics(final ArrayList<Users> users, final Boolean GetNotFoundFromAPI) {
        ArrayList<String> picIDList = Users.GetPicIDListForNullImages(users);
        ImageAPIHelper.GetPicsForPicIDList(mcontext, picIDList, !GetNotFoundFromAPI, pics -> {
            Users.SetPicsToUsers(pics, madapter.Userlist);
            madapter.notifyDataSetChanged();
        });
    }

    public void ShowHideFilterDropDown(Boolean Show, final View AttachToView) {
        if (AttachFilterToView == null) {
            AttachFilterToView = AttachToView;
        }
        if (Show) {
            ((LayoutActivity) getActivity()).SetOnFragmentBackPressedListener(new OnFragmentBackPressed() {
                @Override
                public void BackPressed() {
                    ShowHideFilterDropDown(false, AttachToView);
                }
            });
            if (FilterPopup == null) {
                FilterPopup = new PopupWindow(mcontext);
                View layout = HomePageInflater.inflate(R.layout.filter_layout, null);
                FilterListView = (ListView) layout.findViewById(R.id.FilterListView);
                LoadingGIF = (GifMovieView) layout.findViewById(R.id.LoadingGIF);
                Addfilter = (RelativeLayout) layout.findViewById(R.id.Addfilter);

                if (mFilterAdapter == null) {
                    mFilterAdapter = new FilterAdapter(mcontext, new ArrayList<SavedFilter>());
                }
                FilterListView.setAdapter(mFilterAdapter);
                FilterPopup.setContentView(layout);

                // Set content width and height
                Point size = new Point();
                this.getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                Double dWidth = size.x * 0.5;
                FilterPopup.setWidth(dWidth.intValue());
                FilterPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                // Closes the popup window when touch outside of it - when looses focus
                FilterPopup.setOutsideTouchable(true);
                FilterPopup.setFocusable(true);
                BitmapDrawable bd = new BitmapDrawable();
                bd.setAlpha(0);
                FilterPopup.setBackgroundDrawable(bd);
                FilterPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        FilterPopupDismissed(AttachToView);
                    }
                });
                Addfilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mcontext, AddUserFilter.class);
                        startActivityForResult(intent, 2);
                    }
                });
            }
            // Show anchored to app bar
            FilterPopup.showAsDropDown(AttachToView, 10, 0, Gravity.RIGHT);
            GetUserFilters();
        } else {
            ((LayoutActivity) getActivity()).SetOnFragmentBackPressedListener(null);
            if (FilterPopup != null) {
                FilterPopup.dismiss();
            }
        }
        IsFilterDropDownShown = Show;
    }

    private void FilterPopupDismissed(final View AttachToView) {
        ShowHideFilterDropDown(false, AttachToView);

        //Detect Filter Change
        ArrayList<SavedFilter> SelectedFilters = mFilterAdapter.GetSelectedFilters();
        Boolean Contains;
        Boolean RefreshNeeded = false;

        if (SelectedFilters.size() == 0) {
            LayoutActivity.LayoutActivityInstance.SetFilterIcon(false, IsPopularGuysfilterSelected());
        } else {
            LayoutActivity.LayoutActivityInstance.SetFilterIcon(true, IsPopularGuysfilterSelected());
        }

        SavedFilter filter = new SavedFilter("Temp", "O");
        Contains = SelectedFilters.contains(filter);
        if ((Contains && !IsOnlineSelected) || (!Contains && IsOnlineSelected)) {
            RefreshNeeded = true;
            IsOnlineSelected = Contains;
        }
        filter = new SavedFilter("Temp", "R");
        Contains = SelectedFilters.contains(filter);
        if ((Contains && !IsRecentlyOnlineSelected) || (!Contains && IsRecentlyOnlineSelected)) {
            RefreshNeeded = true;
            IsRecentlyOnlineSelected = Contains;
        }
        filter = new SavedFilter("Temp", "H");
        Contains = SelectedFilters.contains(filter);
        if ((Contains && !IsShowPopularGuysSelected) || (!Contains && IsShowPopularGuysSelected)) {
            RefreshNeeded = true;
            IsShowPopularGuysSelected = Contains;
        }
        filter = new SavedFilter("Temp", "P");
        Contains = SelectedFilters.contains(filter);
        if ((Contains && !IsWithPicsSelected) || (!Contains && IsWithPicsSelected)) {
            RefreshNeeded = true;
            IsWithPicsSelected = Contains;
        }
        filter = new SavedFilter("Temp", "B");
        Contains = SelectedFilters.contains(filter);
        if ((Contains && !IsHideBlockedSelected) || (!Contains && IsHideBlockedSelected)) {
            RefreshNeeded = true;
            IsHideBlockedSelected = Contains;
        }

        Boolean ContainsCustom = false;
        for (int i = 0; i < SelectedFilters.size(); i++) {
            if (SelectedFilters.get(i).IsCustom) {
                ContainsCustom = true;
                if (!SelectedFilters.get(i).SearchID.equalsIgnoreCase(SelectedCustomFilterSearchID)
                        || SelectedCustomFilterSearchID.equalsIgnoreCase(UpdatedFilterID)) {
                    RefreshNeeded = true;
                    SelectedCustomFilterSearchID = SelectedFilters.get(i).SearchID;
                    UpdatedFilterID = "";
                }
            }
        }
        if (!ContainsCustom && !SelectedCustomFilterSearchID.equals("")) {
            RefreshNeeded = true;
            SelectedCustomFilterSearchID = "";
        }
        if (RefreshNeeded) {
            mUserListLastPageCalled = 0;
            AllResultsLoaded = false;
            NonPremiumLimitReached = false;
            if (IsMapViewSelected) {
                mLocationAndUserList = new ArrayList<>();
            }
            RefreshUserList(mUserListLastPageCalled + 1, "");
        }
        if(!mFilterAdapter.IsPopularGuysfilterSelected()) {
            gridView.removeHeaderView(PopularGuysSelectedInfoView);
            ViewGroup PopularInfoViewParent = (ViewGroup)PopularGuysSelectedInfoView.getParent();
            if (PopularInfoViewParent != null) {
                PopularInfoViewParent.removeView(PopularGuysSelectedInfoView);
            }
        }
    }

    private void GetUserFilters() {
        if (AreUserFiltersLoaded) {
            return;
        }
        AreUserFiltersLoaded = true;
        LoadingGIF.setVisibility(View.VISIBLE);
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_Type, "U"));
        APICallInfo apiCallInfo = new APICallInfo("Search", "GetSavedFilters", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mcontext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result == null || result.equals("") || result.equals("-1")) {
                        return;
                    }
                    SavedFiltersList = new GsonBuilder().create().fromJson(result, new TypeToken<List<SavedFilter>>() {
                    }.getType());
                    if (SavedFiltersList.size() < 1) {
                        return;
                    }
                    mFilterAdapter.SetDataSource(SavedFiltersList);
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                } finally {
                    LoadingGIF.setVisibility(View.GONE);
                }
            }
        }, new APINoNetwork() {
            @Override
            public void onAPINoNetwork() {
                LoadingGIF.setVisibility(View.GONE);
            }
        });
//        mFilterAdapter.SetDataSource(SavedFiltersList);
    }

    public void SetSearchActiveState(Boolean IsActive) {
        Boolean wasSearchActive = IsSearchActive;
        IsSearchActive = IsActive;
        if (!IsActive) {
            if (wasSearchActive && !StringHelper.IsNullOrEmpty(SearchPhrase)) {
                SearchPhrase = "";
                mUserListLastPageCalled = 0;
                RefreshUserList(mUserListLastPageCalled + 1, "");
            }
        }
    }

    public void ReloadList(String LocationID) {
        mUserListLastPageCalled = 0;
        RefreshUserList(mUserListLastPageCalled + 1, LocationID);
    }

    public void SearchByName(String vSearchPhrase) {
        SearchPhrase = vSearchPhrase;
        mUserListLastPageCalled = 0;
        RefreshUserList(mUserListLastPageCalled + 1, "");
    }

    public Boolean AreFiltersSelected() {
        try {
            if (AttachFilterToView != null) {
                return mFilterAdapter.GetSelectedFilters().size() > 0 ? true : false;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    public void FiltersDeselectedOnBackPressed(View FilterDropDownAttachToView) {
        if (mFilterAdapter == null) {
            mFilterAdapter = new FilterAdapter(mcontext, new ArrayList<SavedFilter>());
        }
        if (IsPopularGuysfilterSelected()) {
            SelectUnselectPopularGuysfilter(FilterDropDownAttachToView);
        } else {
            mFilterAdapter.UnSelectAllFilters();
            FilterPopupDismissed(FilterDropDownAttachToView);
            LayoutActivity.LayoutActivityInstance.SetFilterIcon(false, IsPopularGuysfilterSelected());
        }
        mUserListLastPageCalled = 0;
        RefreshUserList(mUserListLastPageCalled + 1, "");
    }

    private void SaveFirstPageUsersIfNeeded(String sUsersList) {
        if (!IsOnlineSelected && !IsRecentlyOnlineSelected && !IsWithPicsSelected && !IsShowPopularGuysSelected &&
                !IsHideBlockedSelected && !IsSearchActive && SelectedCustomFilterSearchID.equals("")) {
            SessionManager.SetFirstPageNearByUsers(new FirstPageNearByUsers(GDDateTimeHelper.GetCurrentDateTimeAsString(false), sUsersList));
        }
    }

    public Boolean IsPopularGuysfilterSelected() {
        if (mFilterAdapter == null) {
            mFilterAdapter = new FilterAdapter(mcontext, new ArrayList<SavedFilter>());
        }
        return mFilterAdapter.IsPopularGuysfilterSelected();
    }

    public void SelectUnselectPopularGuysfilter(View FilterDropDownAttachToView) {
        if (mFilterAdapter == null) {
            mFilterAdapter = new FilterAdapter(mcontext, new ArrayList<>());
        }
        mFilterAdapter.SelectUnselectPopularGuysfilter();
        FilterPopupDismissed(FilterDropDownAttachToView);
    }

    @Override
    public void onRefresh() {
//        Intent intent = new Intent(mcontext, GDMapActivity.class);
//        intent.putExtra("Activity_Mode", GDMapActivity.VIEW_Users_LOCATION);
//        intent.putParcelableArrayListExtra("GDUsersList", (ArrayList) madapter.Userlist);
//        startActivity(intent);
        mUserListLastPageCalled = 0;
        RefreshUserList(mUserListLastPageCalled + 1, "");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (madapter != null) {
            madapter.notifyDataSetChanged();
        }
        if (LastPauseTime != null) {
            Date TimeNow = new Date();
            long secs = (TimeNow.getTime() - LastPauseTime.getTime()) / 1000;
            if (secs > 300 && mUserListLastPageCalled == 1) {
                mUserListLastPageCalled = 0;
                RefreshUserList(mUserListLastPageCalled + 1, "");
            }
            LastPauseTime = null;
        }
    }

    @Override
    public void onPause() {
        ((LayoutActivity) getActivity()).SetOnFragmentBackPressedListener(null);
        LastPauseTime = new Date();
        super.onPause();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2) {
                //Filter added
                SavedFilter filter = data.getExtras().getParcelable("Filter");
                mFilterAdapter.AddFilter(filter);
            } else if (requestCode == 3) {
                //Existing Filter
                SavedFilter filter = data.getExtras().getParcelable("Filter");
                if (data.hasExtra("Delete") && data.getExtras().getInt("Delete", 0) == 1) {
                    //Filter deleted
                    mFilterAdapter.RemoveFilter(filter);
                } else {
                    //Filter updated
                    mFilterAdapter.UpdateFilter(filter);
                    UpdatedFilterID = filter.SearchID;
                }
            }
        }
        if (requestCode == 1) {
            if ((new GPSHelper()).CheckLocationAvailability(mcontext)) {
                mUserListLastPageCalled = 0;
                RefreshUserList(mUserListLastPageCalled + 1, "");
            }
        }
    }

    public void ShowSnackBar(String Message, int duration, Boolean IsError) {
        TopSnackBar.MakeSnackBar(getActivity().findViewById(R.id.BodyBelowAppBar), Message, duration, IsError).show();
    }

    //Map View - START
    private void SetUpMapView() {
        try {
            if (mMap == null) {
                //((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.GDMap)).getMapAsync(new OnMapReadyCallback() {
                ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.GDMap)).getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mMap = googleMap;
                        mMap.getUiSettings().setZoomControlsEnabled(true);
                        if (mMap != null) {
                            SetUpInfoWindowAdapter();
                        }
                    }
                });
            }
            if (mMap != null) {
                SetUpInfoWindowAdapter();
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    private void SetUpInfoWindowAdapter() {
        if (mGDInfoWindowAdapter == null) {
            mGDInfoWindowAdapter = new GDInfoWindowAdapter(mcontext);
        }
        mMap.setInfoWindowAdapter(mGDInfoWindowAdapter);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mLocationAndUserList.size() == 0) {
                    return;
                }
                for (int i = 0; i < mLocationAndUserList.size(); i++) {
                    if (mLocationAndUserList.get(i).latLng.equals(marker.getPosition())) {
                        Intent intent = new Intent(mcontext, NewProfileViewActivity.class);
                        intent.putExtra("ClickedUserID", mLocationAndUserList.get(i).User.UserID);
                        String picID = mLocationAndUserList.get(i).User.PicID;
                        if (picID != null && !picID.trim().equals("")) {
                            intent.putExtra("ProfilePicID", picID);
                        }
                        mcontext.startActivity(intent);
                    }
                }
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (AllResultsLoaded || mMap == null || mLocationAndUserList.size() == 0) {
                    return;
                }
                LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                if (latLngBounds.contains(mLocationAndUserList.get(mLocationAndUserList.size() - 1).latLng) && !mUserListIsRefreshing
                        && madapter.getCount() >= 60) {
                    if (!IsPopularGuysfilterSelected()) {
                        TopSnackBar.MakeSnackBar(getActivity().findViewById(R.id.BodyBelowAppBar), "Loading more guys..", TopSnackBar.LENGTH_SHORT, false).show();
                    }
                    RefreshUserList(mUserListLastPageCalled + 1, "");
                }
            }
        });
        mMap.clear();
        AddMapMarkers(madapter.Userlist);
        MoveCameraToFirst();
    }

    public void MakeMyMapViewShownInList() {
        for(int i=0; i< madapter.Userlist.size(); i++) {
            madapter.Userlist.get(i).MyShowInMapSearch = true;
        }
    }

    private void AddMapMarkers(final ArrayList<Users> Userlist) {
        if (mLocationAndUserList.size() == 0 && mMap != null) {
            mMap.clear();
        }
        if (Userlist.size() == 0) {
            return;
        }
        if (!Userlist.get(0).MyShowInMapSearch) {
            if (!IsPrivacyAlertShown) {
                IsPrivacyAlertShown = true;

                String title = "Show users on map?<br> [Privacy Alert]";
                title = "<small>" + title + "</small>";

                String mapLocationPrivacyAlertMessage = getString(R.string.map_location_privacy_alert_message);
                mapLocationPrivacyAlertMessage = mapLocationPrivacyAlertMessage.replace("##", "<br>");
                mapLocationPrivacyAlertMessage = "<small>" + mapLocationPrivacyAlertMessage + "</small>";

                GDDialogHelper.ShowYesNoTypeDialog(mcontext, Html.fromHtml(title), Html.fromHtml(mapLocationPrivacyAlertMessage),
                        GDDialogHelper.BUTTON_TEXT_CONTINUE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT,
                        new OnDialogButtonClick() {
                            @Override
                            public void dialogButtonClicked() {
                                IsPrivacyAlertShown = false;
                                new HomeAPICalls(mcontext).ShowInMapSearch(new ProgressDialog(HomePageFragment.this.getActivity()), new APISuccessCallback() {
                                    @Override
                                    public void onSuccess(Object data, Object ExtraData) {
                                        MakeMyMapViewShownInList();
                                        SessionManager.SetFirstPageNearByUsersShowMyInMap();
                                        SendSubGenericAction("StartMapFreemiumTimer");
                                        AddMapMarkers(Userlist);
                                        MoveCameraToFirst();
                                    }
                                }, new APIFailureCallback() {
                                    @Override
                                    public void onFailure(Object data, Object ExtraData) {
                                        ShowSnackBar(getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true);
                                        SendSubGenericAction("CloseMapView");
                                    }
                                }, new APINoNetwork() {
                                    @Override
                                    public void onAPINoNetwork() {
                                        ShowSnackBar(getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true);
                                        SendSubGenericAction("CloseMapView");
                                    }
                                });
                            }
                        }, new OnDialogButtonClick() {
                            @Override
                            public void dialogButtonClicked() {
                                IsPrivacyAlertShown = false;
                                SendSubGenericAction("CloseMapView");
                            }
                        });
            }
            return;
        } else {
            SendSubGenericAction("StartMapFreemiumTimer");
        }

        MarkerOptions markerOptions;
        View CustomMarker;
        LayoutInflater layoutInflater;
        layoutInflater = LayoutInflater.from(mcontext);
        ImageView ProfilePic;
        LatLng latLng;
        for (int i = 0; i < Userlist.size(); i++) {
            latLng = GetLatLngFromString(Userlist.get(i).LocationLatLng);
            if (!LocationAndUserListContains(latLng, Userlist.get(i))) {
                if(!Userlist.get(i).UserID.equalsIgnoreCase(LoggedInUser.UserID) && !Userlist.get(i).ShowInMapSearch) {
                    continue;
                }
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                CustomMarker = layoutInflater.inflate(R.layout.home_map_icon, null);
                ProfilePic = CustomMarker.findViewById(R.id.ProfilePic);
                if (Userlist.get(i).image != null) {
                    ProfilePic.setImageBitmap(Userlist.get(i).image);
                }
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(ImageHelper.createDrawableFromView(mcontext, CustomMarker)));
                if (mMap != null) {
                    Marker marker = mMap.addMarker(markerOptions);
                    mLocationAndUserList.add(new LocationAndUserList(Userlist.get(i).UserID, latLng, Userlist.get(i), marker));
                }
            }
        }
    }

    private void SendSubGenericAction(String action) {
        if (mSubGenericAction != null) {
            mSubGenericAction.OnSubGenericAction(action, null);
        }
    }

    private Boolean LocationAndUserListContains(LatLng latLng, Users User) {
        for (int i = 0; i < mLocationAndUserList.size(); i++) {
            if (mLocationAndUserList.get(i).latLng.equals(latLng) || mLocationAndUserList.get(i).User.equals(User)) {
                return true;
            }
        }
        return false;
    }

    public void MoveCamera(LatLng latLng, int ZoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom((float) ZoomLevel).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void HomeGridViewSelected() {
        mLocationAndUserList = new ArrayList<>();
        swipeUserList.setVisibility(View.VISIBLE);
        GDMapLayout.setVisibility(View.GONE);
        IsMapViewSelected = false;
        try {
            ContentLoadedContainer.removeView(GDMapLayout);
            if (swipeUserList.getParent() == null) {
                ContentLoadedContainer.addView(swipeUserList);
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }

        mMap = null;
        madapter.notifyDataSetChanged();
    }

    public void HomeMapViewSelected() {
        swipeUserList.setVisibility(View.GONE);
        GDMapLayout.setVisibility(View.VISIBLE);
        try {
            ContentLoadedContainer.removeView(swipeUserList);
            if (GDMapLayout.getParent() == null) {
                ContentLoadedContainer.addView(GDMapLayout);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
//        if (GDMapLayout.getParent() == null) {
//            HomeMainLayout.addView(GDMapLayout);
//        }
        IsMapViewSelected = true;
        SetUpMapView();
    }

    public Boolean bIsMapViewSelected() {
        return IsMapViewSelected;
    }

    private void MoveMapToUser(Boolean IsNext) {
        if (IsNext) {
            if (MapViewCurrentSelection < ((mLocationAndUserList.size() - 1))) {
                MapViewCurrentSelection = MapViewCurrentSelection + 1;
                if (mLocationAndUserList.size() > MapViewCurrentSelection) {
                    MoveCamera(mLocationAndUserList.get(MapViewCurrentSelection).latLng, 18);
                    mLocationAndUserList.get(MapViewCurrentSelection).marker.showInfoWindow();
                }
            }
        } else {
            if (MapViewCurrentSelection > 0) {
                MapViewCurrentSelection = MapViewCurrentSelection - 1;
                if (mLocationAndUserList.size() > MapViewCurrentSelection) {
                    MoveCamera(mLocationAndUserList.get(MapViewCurrentSelection).latLng, 18);
                    mLocationAndUserList.get(MapViewCurrentSelection).marker.showInfoWindow();
                }
            }
        }
        SetMapViewButtonsVisibility();
    }

    private void SetMapViewButtonsVisibility() {
        if ((mLocationAndUserList.size() - 1) <= MapViewCurrentSelection) {
            MapViewNext.setVisibility(View.GONE);
        } else {
            MapViewNext.setVisibility(View.VISIBLE);
        }
        if (MapViewCurrentSelection <= 0) {
            MapViewPrev.setVisibility(View.GONE);
        } else {
            MapViewPrev.setVisibility(View.VISIBLE);
        }
    }

    private void MoveCameraToFirst() {
        try {
            if (madapter.Userlist.size() > 0 && mLocationAndUserList.size() > 0) {
                MapViewCurrentSelection = 0;
                if (mLocationAndUserList.size() >= 10) {
                    LatLngBounds bounds;
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(mLocationAndUserList.get(0).latLng);
                    builder.include(mLocationAndUserList.get(1).latLng);
                    builder.include(mLocationAndUserList.get(2).latLng);
                    bounds = builder.build();
                    if (bounds != null) {
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                        mMap.animateCamera(cu);
                    } else {
                        MoveCamera(mLocationAndUserList.get(MapViewCurrentSelection).latLng, 18);
                    }
                } else {
                    MoveCamera(mLocationAndUserList.get(MapViewCurrentSelection).latLng, 18);
                }
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            MapViewCurrentSelection = 0;
            if (madapter.Userlist.size() > 0 && mLocationAndUserList.size() > 0) {
                MoveCamera(mLocationAndUserList.get(MapViewCurrentSelection).latLng, 18);
            }
        }
        SetMapViewButtonsVisibility();
    }
    //Map View - END

    private LatLng GetLatLngFromString(String LocationLatLng) {
        LatLng latLng = null;
        try {
            List<String> Location = StringHelper.SplitStringByComma(LocationLatLng);
            latLng = new LatLng(Double.parseDouble(Location.get(0)), Double.parseDouble(Location.get(1)));
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return latLng;
    }


    private class GDInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public GDInfoWindowAdapter(Context context) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        private LocationAndUserList FindUserFromListByLatLng(ArrayList<LocationAndUserList> locationAndUserLists, String LatLng) {
            for (int i = 0; i < locationAndUserLists.size(); i++) {
                if (GetStringFromLatLng(locationAndUserLists.get(i).latLng).equals(LatLng)) {
                    return locationAndUserLists.get(i);
                }
            }
            return null;
        }

        private String GetStringFromLatLng(LatLng latLng) {
            return Double.toString(latLng.latitude) + "," + Double.toString(latLng.longitude);
        }

        public int GetUserMarkerPosition(LocationAndUserList locationAndUserList) {
            int position = -1;
            if (mLocationAndUserList.contains(locationAndUserList)) {
                position = mLocationAndUserList.indexOf(locationAndUserList);
            }
            return position;
        }

        private Users GetUserByUserID(String UserID) {
            Users TempUser = new Users(UserID);
            int position = -1;
            if (madapter.Userlist.contains(TempUser)) {
                position = madapter.Userlist.indexOf(TempUser);
                return madapter.Userlist.get(position);
            }
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            View contentView = null;
            LatLng latLng = marker.getPosition();
            LocationAndUserList locationAndUserList = FindUserFromListByLatLng(mLocationAndUserList, latLng.latitude + "," + latLng.longitude);
            MapViewCurrentSelection = GetUserMarkerPosition(locationAndUserList);
            SetMapViewButtonsVisibility();
            final Users CurrentUser = GetUserByUserID(locationAndUserList.UserID);
            ImageView ProfilePic;
            TextView UserName;
            TextView UserAge;
            TextView Distance;
            ImageView OnlineIndicator;
            Boolean IsImperial = PersistantPreferencesHelper.GetAppSettings().UnitSystem.equals("I");
            if (CurrentUser != null) {
                contentView = mLayoutInflater.inflate(R.layout.home_map_infowindow, null);
                ProfilePic = contentView.findViewById(R.id.ProfilePic);
                UserName = contentView.findViewById(R.id.UserName);
                UserAge = contentView.findViewById(R.id.UserAge);
                Distance = contentView.findViewById(R.id.Distance);
                OnlineIndicator = contentView.findViewById(R.id.OnlineIndicator);

                UserName.setText(CurrentUser.GetDecodedUserName());
                UserAge.setText(Integer.toString(CurrentUser.Age));
                Distance.setText(IsImperial ? GDUnitHelper.Distance_MTI(CurrentUser.Distance) : GDUnitHelper.FormatKM(CurrentUser.Distance));
                if (CurrentUser.image != null) {
                    ProfilePic.setImageBitmap(CurrentUser.image);
                } else {
                    ProfilePic.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.defaultuserprofilepic));
                }
                if (CurrentUser.OnlineStatus) {
                    OnlineIndicator.setVisibility(View.VISIBLE);
                } else {
                    OnlineIndicator.setVisibility(View.GONE);
                }

                Boolean isMyProfile = CurrentUser.UserID.equalsIgnoreCase(LoggedInUser.UserID);
                //Apply privacy hidings
                if (CurrentUser.ShowAgeInSearchTo.equals("N") && !isMyProfile) {
                    UserAge.setVisibility(View.GONE);
                } else {
                    UserAge.setVisibility(View.VISIBLE);
                }
                //Distance
                Boolean showHisDistance = !CurrentUser.ShowDistanceInSearchTo.equals("N");
                Boolean showMyDistance = !CurrentUser.ShowMyDistanceInSearchTo.equals("N");
                if ((showHisDistance && showMyDistance) || isMyProfile) {
                    Distance.setVisibility(View.VISIBLE);
                } else {
                    Distance.setVisibility(View.GONE);
                }
                contentView.setTag(CurrentUser);
            }
            return contentView;
        }
    }

    private class LocationAndUserList {
        String UserID;
        LatLng latLng;
        Users User;
        Marker marker;

        public LocationAndUserList() {
            UserID = "";
            latLng = null;
            User = null;
            marker = null;
        }

        public LocationAndUserList(String vUserID, LatLng vlatLng, Users vUser, Marker vMarker) {
            UserID = vUserID;
            latLng = vlatLng;
            User = vUser;
            marker = vMarker;
        }
    }

    private class FilterAdapter extends BaseAdapter {
        ArrayList<SavedFilter> mFilterList;
        Context mContext;
        LayoutInflater mlayoutInflater;
        List<String> MutuallyExclusiveStaticFilterCodes = new ArrayList<>();

        public FilterAdapter(Context context, ArrayList<SavedFilter> FilterList) {
            mFilterList = new ArrayList<>();
            AddStaticFilters();
            mFilterList.addAll(FilterList);
            mContext = context;
            MutuallyExclusiveStaticFilterCodes.add("O");
            MutuallyExclusiveStaticFilterCodes.add("R");
        }

        public void SetDataSource(ArrayList<SavedFilter> FilterList) {
//            mFilterList = new ArrayList<>();
//            AddStaticFilters();
            if (FilterList != null) {
                mFilterList.addAll(FilterList);
                notifyDataSetChanged();
            }
        }

        private void AddStaticFilters() {
            SavedFilter filter = new SavedFilter("Recently Online", "R");
            filter.IsCustom = false;
            filter.IsSelected = false;
            mFilterList.add(filter);
            filter = new SavedFilter("Online Now", "O");
            filter.IsCustom = false;
            mFilterList.add(filter);
            filter = new SavedFilter("Top Popular Guys", "H");    //Hot/Popular guys
            filter.IsCustom = false;
            mFilterList.add(filter);
            filter = new SavedFilter("With photos", "P");
            filter.IsCustom = false;
            mFilterList.add(filter);
            filter = new SavedFilter("Hide Blocked", "B");
            filter.IsCustom = false;
            mFilterList.add(filter);
        }

        private void AddFilter(SavedFilter filter) {
            if (!mFilterList.contains(filter)) {
                mFilterList.add(filter);
            }
            notifyDataSetChanged();
        }

        private void UpdateFilter(SavedFilter filter) {
            int index = mFilterList.indexOf(filter);
            if (index >= 0) {
                mFilterList.set(index, filter);
            }
            notifyDataSetChanged();
        }

        private void RemoveFilter(SavedFilter filter) {
            mFilterList.remove(filter);
            notifyDataSetChanged();
        }

        public ArrayList<SavedFilter> GetSelectedFilters() {
            ArrayList<SavedFilter> SelectedFilters = new ArrayList<>();
            for (int i = 0; i < mFilterList.size(); i++) {
                if (mFilterList.get(i).IsSelected) {
                    SelectedFilters.add(mFilterList.get(i));
                }
            }
            return SelectedFilters;
        }

        private void UnSelectOtherCustomFilters(String SearchID) {
            for (int i = 0; i < mFilterList.size(); i++) {
                if (mFilterList.get(i).IsCustom && !mFilterList.get(i).SearchID.equalsIgnoreCase(SearchID)) {
                    mFilterList.get(i).IsSelected = false;
                }
            }
            notifyDataSetChanged();
        }

        private void UnSelectAllOtherFilters(String SearchID) {
            for (int i = 0; i < mFilterList.size(); i++) {
                if (!mFilterList.get(i).SearchID.equalsIgnoreCase(SearchID)) {
                    mFilterList.get(i).IsSelected = false;
                }
            }
            notifyDataSetChanged();
        }

        private void UnSelectAllFilters() {
            for (int i = 0; i < mFilterList.size(); i++) {
                mFilterList.get(i).IsSelected = false;
            }
            notifyDataSetChanged();
        }

        public Boolean IsPopularGuysfilterSelected() {
            Boolean IsSelected = false;
            for (int i = 0; i < mFilterList.size(); i++) {
                if (mFilterList.get(i).SearchID.equalsIgnoreCase("H") && mFilterList.get(i).IsSelected) {
                    IsSelected = true;
                    break;
                }
            }
            return IsSelected;
        }

        public void SelectUnselectPopularGuysfilter() {
            Boolean WasSelected = false;
            SavedFilter filter = new SavedFilter("", "H");
            filter.IsCustom = false;
            int index = mFilterList.indexOf(filter);
            if (index > -1) {
                if (mFilterList.get(index).IsSelected) {
                    mFilterList.get(index).IsSelected = false;

//                    //Select Recently Online
//                    filter = new SavedFilter("", "R");
//                    filter.IsCustom = false;
//                    index = mFilterList.indexOf(filter);
//                    if (index > -1) {
//                        mFilterList.get(index).IsSelected = true;
//                    }

                    WasSelected = true;
                }
            }
            if (!WasSelected) {
                if (index > -1) {
                    mFilterList.get(index).IsSelected = true;
                    UnSelectAllOtherFilters("H");
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mFilterList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFilterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                mlayoutInflater = LayoutInflater.from(mContext);
                convertView = mlayoutInflater.inflate(R.layout.filter_item, null);
                FilterListViewHolder filterListViewHolder = new FilterListViewHolder();
                filterListViewHolder.FilterItemCheckBox = (CheckBox) convertView.findViewById(R.id.FilterItemCheckBox);
                filterListViewHolder.FilterItemText = (TextView) convertView.findViewById(R.id.FilterItemText);
                filterListViewHolder.Edit = (ImageView) convertView.findViewById(R.id.Edit);
                convertView.setTag(filterListViewHolder);
            }
            final FilterListViewHolder filterListViewHolder = (FilterListViewHolder) convertView.getTag();
            filterListViewHolder.FilterItemText.setText(mFilterList.get(position).SearchName);
            if (mFilterList.get(position).IsCustom) {
                filterListViewHolder.Edit.setVisibility(View.VISIBLE);
                filterListViewHolder.Edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mcontext, AddUserFilter.class);
                        intent.putExtra("Mode", AddUserFilter.EDIT_FILTER);
                        intent.putExtra("Filter", mFilterList.get(position));
                        startActivityForResult(intent, 3);
                    }
                });
            } else {
                filterListViewHolder.Edit.setVisibility(View.GONE);
            }
            filterListViewHolder.FilterItemText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (filterListViewHolder.FilterItemCheckBox.isChecked()) {
                        filterListViewHolder.FilterItemCheckBox.setChecked(false);
                    } else {
                        filterListViewHolder.FilterItemCheckBox.setChecked(true);
                    }
                }
            });
            filterListViewHolder.FilterItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !GDGenericHelper.IsUserPremium(mContext) &&
                            (mFilterList.get(position).SearchID.equals("O") || mFilterList.get(position).SearchID.equals("B"))) {
                        String message = mFilterList.get(position).SearchID.equals("O") ? "Get GDudes premium to see only guys that are online." :
                                "Get GDudes premium to see hide guys that are blocked.";
                        GDGenericHelper.ShowBuyPremiumIfNotPremium(mcontext, message, false);
                        buttonView.setChecked(false);
                        return;
                    }
                    mFilterList.get(position).IsSelected = isChecked;
                    if (isChecked && mFilterList.get(position).IsCustom) {
                        UnSelectOtherCustomFilters(mFilterList.get(position).SearchID);
                    } else if (isChecked && mFilterList.get(position).SearchID.equals("R")) {
                        UnSelectMutuallyExclusiveStaticFilters("R");
                    } else if (isChecked && mFilterList.get(position).SearchID.equals("O")) {
                        UnSelectMutuallyExclusiveStaticFilters("O");
                    }
                    if (isChecked && mFilterList.get(position).SearchID.equals("H")) {
                        CheckFilterSelectedForPopularGuys();
                    } else if (isChecked) {
                        SavedFilter filter = new SavedFilter("", "H");
                        int index = mFilterList.indexOf(filter);
                        if (index > -1) {
                            mFilterList.get(index).IsSelected = false;
                            notifyDataSetChanged();
                        }
                    }
                }
            });
            filterListViewHolder.FilterItemCheckBox.setChecked(mFilterList.get(position).IsSelected);
            return convertView;
        }

        private void UnSelectMutuallyExclusiveStaticFilters(String SelectedFilterCode) {
            Boolean AnyChanged = false;
            SavedFilter filter;
            int index;
            for (int i = 0; i < MutuallyExclusiveStaticFilterCodes.size(); i++) {
                if (!MutuallyExclusiveStaticFilterCodes.get(i).equalsIgnoreCase(SelectedFilterCode)) {
                    filter = new SavedFilter("", MutuallyExclusiveStaticFilterCodes.get(i));
                    filter.IsCustom = false;
                    index = mFilterList.indexOf(filter);
                    if (index > -1) {
                        if (mFilterList.get(index).IsSelected) {
                            mFilterList.get(index).IsSelected = false;
                            AnyChanged = true;
                        }
                    }
                }
            }
            if (AnyChanged) {
                notifyDataSetChanged();
            }
        }

        private void CheckFilterSelectedForPopularGuys() {
            SavedFilter filter = new SavedFilter("", "H");
            int index = mFilterList.indexOf(filter);
            if (index > -1) {
                if (mFilterList.get(index).IsSelected) {
                    UnSelectAllOtherFilters("H");
                }
            }
        }
    }

    private static class FilterListViewHolder {
        CheckBox FilterItemCheckBox = null;
        TextView FilterItemText = null;
        ImageView Edit = null;
    }

    private class MyNearbySearchData {
        @SerializedName("SMyDist")
        public String ShowMyDistance = "Y";
        @SerializedName("MySMap")
        public Boolean MyShowInMapSearch = false;
    }
}


