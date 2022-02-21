package com.gdudes.app.gdudesapp.activities.MainLayout;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.Adapters.NotificationsAdapter;
import com.gdudes.app.gdudesapp.CustomViewTypes.DividerItemDecoration;
import com.gdudes.app.gdudesapp.GDTypes.GDNotification;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDTimer;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Interfaces.GDTimerTaskRun;
import com.gdudes.app.gdudesapp.Notifications.NotificationHelper;
import com.gdudes.app.gdudesapp.R;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class NotificationsPageFragment extends Fragment {
    Users LoggedInUser = null;
    Context mContext = null;
    SwipeRefreshLayout swipeNotifications;
    RecyclerView NotificationsRecyclerView;
    LinearLayoutManager mLayoutManager;
    Boolean IsLoading = false;
    RecyclerView.OnScrollListener onScrollListener = null;

    NotificationsAdapter notificationsAdapter;
    GDTimer mUpdateNotificationsTimer;
    private int UnseenNotificationsCount = 0;

    private RelativeLayout ContentLoadedContainer = null;
    private RelativeLayout ContentLoadingContainer = null;
    private TextView ContentLoadingText = null;
    private Boolean RefreshedDataForFirstLoad = false;

    public static NotificationsPageFragment NotificationsPageInstance = null;

    @Override
    public void onResume() {
        super.onResume();
        mUpdateNotificationsTimer = new GDTimer(800, 10000, new Handler(), new GDTimerTaskRun() {
            @Override
            public void OnTimerElapsed() {
                RefeshNotificationList(true, false);
            }
        });
        mUpdateNotificationsTimer.Start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        NotificationsPageInstance = NotificationsPageFragment.this;
        mContext = this.getActivity().getApplicationContext();
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);
        View NotificationsPagelayout = inflater.inflate(R.layout.notificationspagemain_layout, container, false);

        IsLoading = false;

        ContentLoadedContainer = NotificationsPagelayout.findViewById(R.id.ContentLoadedContainer);
        ContentLoadingContainer = NotificationsPagelayout.findViewById(R.id.ContentLoadingContainer);
        ContentLoadingText = NotificationsPagelayout.findViewById(R.id.ContentLoadingText);
        swipeNotifications = NotificationsPagelayout.findViewById(R.id.swipeNotifications);
        NotificationsRecyclerView = NotificationsPagelayout.findViewById(R.id.Notificationslist);

        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition == -1)
                    return;
                if (RefreshedDataForFirstLoad) {
                    return;
                }
                RefreshedDataForFirstLoad = true;
                int StartPosition = mLayoutManager.findFirstVisibleItemPosition();
                int EndPosition = mLayoutManager.findLastVisibleItemPosition();
                if (StartPosition != -1 && EndPosition != -1) {
                    notificationsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
                }
            }
        };
        NotificationsRecyclerView.setLayoutManager(mLayoutManager);
        NotificationsRecyclerView.addItemDecoration(new DividerItemDecoration(mContext));
        notificationsAdapter = new NotificationsAdapter(mContext, LoggedInUser);
        NotificationsRecyclerView.setAdapter(notificationsAdapter);

        onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        RefeshNotificationList(false, true);
                    }

                    int StartPosition = mLayoutManager.findFirstVisibleItemPosition();
                    int EndPosition = mLayoutManager.findLastVisibleItemPosition();
                    if (StartPosition != -1 && EndPosition != -1) {
                        notificationsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
                    }
                }
            }
        };
        NotificationsRecyclerView.addOnScrollListener(onScrollListener);

        swipeNotifications.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RefeshNotificationList(true, true);
            }
        });

        RefeshNotificationList(false, true);
        return NotificationsPagelayout;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mUpdateNotificationsTimer != null) {
            mUpdateNotificationsTimer.Stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public int GetUnseenNotificationsCount() {
        return UnseenNotificationsCount;
    }

    public void MarkNotificationsSeen() {
        if (notificationsAdapter == null) {
            return;
        }
        notificationsAdapter.MarkNotificationsSeen();
        UnseenNotificationsCount = 0;
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        APICallInfo apiCallInfo = new APICallInfo("Home", "MarkUserNotificationsSeen", pAPICallParameters, "GET", null,
                null, false, null, APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    if (result == null || result.equals("") || result.equals("-1")) {
                        return;
                    }
                    if (result.equals("1")) {
                        NotificationHelper.MarkNotificationsAsSeen();
                        LayoutActivity.TabIconCountsNeedRefresh = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                }
            }
        }, null);
    }

    private void RefeshNotificationList(final Boolean IsSwipeRefresh, Boolean ShowSwipeRefresh) {
        if (IsLoading) {
            return;
        }
        IsLoading = true;
        if (ShowSwipeRefresh) {
            swipeNotifications.setRefreshing(true);
        }
        String LastSinceDateTime = "";
        int getNext = 0;
        if (notificationsAdapter.getItemCount() > 0) {
            if (IsSwipeRefresh) {
                List<GDNotification> GDNotificationList = notificationsAdapter.GetItemList();
                for (int i = 0; i < GDNotificationList.size(); i++) {
                    if (!GDNotificationList.get(i).NotificationType.equals("FR")) {
                        LastSinceDateTime = notificationsAdapter.getItem(i).NotificationDateTime;
                        break;
                    }
                }
                getNext = 0;
            } else {
                LastSinceDateTime = notificationsAdapter.getItem(notificationsAdapter.getItemCount() - 1).NotificationDateTime;
                getNext = 1;
            }
        }
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_GetNext, Integer.toString(getNext)));
        if (LastSinceDateTime != null && !LastSinceDateTime.equals("")) {
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_SinceDateTime, LastSinceDateTime));
        }
        APICallInfo apiCallInfo = new APICallInfo("Home", "GetUserNotifications", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            IsLoading = false;
            try {
                if (result == null || result.equals("") || result.equals("-1")) {
                    return;
                }
                List<GDNotification> NotificationsList = new GsonBuilder().create().fromJson(result, new TypeToken<List<GDNotification>>() {
                }.getType());
                if (NotificationsList.size() > 0) {
                    notificationsAdapter.setNotificationsList(NotificationsList, IsSwipeRefresh);
                    UnseenNotificationsCount = NotificationsList.get(NotificationsList.size() - 1).UnseenCount;
                    LayoutActivity.TabIconCountsNeedRefresh = true;
                    int StartPosition = mLayoutManager.findFirstVisibleItemPosition();
                    int EndPosition = mLayoutManager.findLastVisibleItemPosition();
                    if (StartPosition != -1 && EndPosition != -1) {
                        notificationsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
                    }
                } else if (NotificationsList.size() == 0 && IsSwipeRefresh) {
                    notificationsAdapter.setNotificationsList(NotificationsList, IsSwipeRefresh);
                } else {
                    if (notificationsAdapter.getItemCount() > 0 && !IsSwipeRefresh) {
                        NotificationsRecyclerView.removeOnScrollListener(onScrollListener);
                    }
                }
            } catch (Exception e) {
                GDLogHelper.LogException(e);
            } finally {
                PostUserNotificationsCall();
            }
        }, () -> {
            PostUserNotificationsCall();
        });
    }

    private void PostUserNotificationsCall() {
        swipeNotifications.setRefreshing(false);
        if (notificationsAdapter.getItemCount() == 0) {
            ContentLoadingContainer.setVisibility(View.VISIBLE);
            ContentLoadedContainer.setVisibility(View.GONE);
            ContentLoadingText.setText("No notifications to show.");
        } else {
            ContentLoadingContainer.setVisibility(View.GONE);
            ContentLoadedContainer.setVisibility(View.VISIBLE);
        }
    }

}
