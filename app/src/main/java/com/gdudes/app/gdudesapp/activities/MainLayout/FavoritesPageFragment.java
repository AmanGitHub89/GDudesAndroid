package com.gdudes.app.gdudesapp.activities.MainLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.Adapters.UserListGridAdapter;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDGridView;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;

import java.util.ArrayList;

public class FavoritesPageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static FavoritesPageFragment FavoritesPageInstance = null;
    private LayoutInflater HomePageInflater;
    private RelativeLayout ContentLoadedContainer = null;
    private RelativeLayout ContentLoadingContainer = null;
    private TextView ContentLoadingText = null;
    private GDGridView gridView = null;
    private SwipeRefreshLayout swipeUserList;
    private UserListGridAdapter mAdapter;

    private Users LoggedInUser = null;
    private Context mContext = null;

    public Boolean mUserListIsRefreshing = false;
    int mUserListLastPageCalled = 0;
    Boolean AllResultsLoaded = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FavoritesPageInstance = FavoritesPageFragment.this;
        HomePageInflater = inflater;
        View fragmentPagelayout = inflater.inflate(R.layout.favorites_page_layout, container, false);
        mContext = this.getActivity();
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        ContentLoadedContainer = fragmentPagelayout.findViewById(R.id.ContentLoadedContainer);
        ContentLoadingContainer = fragmentPagelayout.findViewById(R.id.ContentLoadingContainer);
        ContentLoadingText = fragmentPagelayout.findViewById(R.id.ContentLoadingText);
        gridView = fragmentPagelayout.findViewById(R.id.gvGrid);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gridView.setNestedScrollingEnabled(true);
        }
        swipeUserList = fragmentPagelayout.findViewById(R.id.swipeUserList);

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
                    if (mAdapter != null && !mUserListIsRefreshing) {
                        int TotalCount = mAdapter.getCount();
                        if (TotalCount < 60) {
                            return;
                        }
                        if ((firstVisibleItem + visibleItemCount) >= (TotalCount - 36)) {
                            if (AllResultsLoaded) {
                                return;
                            }
                            RefreshUserList(mUserListLastPageCalled + 1);
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

            mAdapter = new UserListGridAdapter(HomePageInflater.getContext(), imageWidth, imageWidth, logicalDensity, LoggedInUser);
            gridView.setAdapter(mAdapter);

            gridView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                intent.putExtra("ClickedUserID", mAdapter.getItem(position).UserID);
                if (mAdapter.getItem(position).PicID != null && !mAdapter.getItem(position).PicID.trim().equals("")) {
                    intent.putExtra("ProfilePicID", mAdapter.getItem(position).PicID);
                }
                mContext.startActivity(intent);
            });
            ContentLoadingContainer.setOnClickListener(v -> {
                if (!mUserListIsRefreshing) {
                    mUserListLastPageCalled = 0;
                    RefreshUserList(mUserListLastPageCalled + 1);
                }
            });
            swipeUserList.setVisibility(View.VISIBLE);
            swipeUserList.setRefreshing(true);
            RefreshUserList(mUserListLastPageCalled + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }

        return fragmentPagelayout;
    }

    @Override
    public void onRefresh() {
        mUserListLastPageCalled = 0;
        RefreshUserList(mUserListLastPageCalled + 1);
    }


    private void RefreshUserList(final int pageNumber) {
        mUserListLastPageCalled = pageNumber;
        if (pageNumber == 1) {
            AllResultsLoaded = false;
            if (mAdapter.getCount() == 0) {
                ContentLoadingContainer.setVisibility(View.VISIBLE);
                ContentLoadedContainer.setVisibility(View.GONE);
                ContentLoadingText.setText("Loading your favorite guys..");
            }
        }
        mUserListIsRefreshing = true;
        swipeUserList.setRefreshing(true);

        new HomeAPICalls(mContext).GetFavorites(pageNumber, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                ArrayList<Users> users = (ArrayList<Users>) result;
                if (pageNumber == 1) {
                    mAdapter.ReplaceUserList(users, true, false);
                } else {
                    mAdapter.AppendUserList(users, true, false);
                }
                if (users.size() > 0) {
                    GetUserPics(users);
                }
                OnGetFavoritesDoneRunning();
            }

            @Override
            public void OnError(String result, Object extraData) {
                if (result != null && result.toString().equals("0")) {
                    if (pageNumber == 1) {
                        mAdapter.ReplaceUserList(new ArrayList<>(), true, false);
                    }
                    AllResultsLoaded = true;
                }
                OnGetFavoritesDoneRunning();
            }

            @Override
            public void OnNoNetwork(Object extraData) {
                try {
                    TopSnackBar.MakeSnackBar(getActivity().findViewById(R.id.BodyBelowAppBar),
                            getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                } catch (Exception ex) {
                    GDLogHelper.LogException(ex);
                }
                OnGetFavoritesDoneRunning();
            }
        });
    }

    private void OnGetFavoritesDoneRunning() {
        mUserListIsRefreshing = false;
        swipeUserList.setRefreshing(false);
        if (mAdapter.getCount() == 0) {
            ContentLoadingContainer.setVisibility(View.VISIBLE);
            ContentLoadedContainer.setVisibility(View.GONE);
            ContentLoadingText.setText("No favorites found. Touch to reload.");
        } else {
            ContentLoadingContainer.setVisibility(View.GONE);
            ContentLoadedContainer.setVisibility(View.VISIBLE);
        }
    }

    private void GetUserPics(final ArrayList<Users> users) {
        ArrayList<String> picIDList = Users.GetPicIDListFromUsers(users);
        ImageAPIHelper.GetPicsForPicIDList(mContext, picIDList, false, pics -> {
            Users.SetPicsToUsers(pics, mAdapter.Userlist);
            mAdapter.notifyDataSetChanged();
        });
    }
}
