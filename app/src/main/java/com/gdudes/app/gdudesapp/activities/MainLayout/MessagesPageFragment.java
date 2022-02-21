package com.gdudes.app.gdudesapp.activities.MainLayout;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gdudes.app.gdudesapp.Adapters.ConversationsAdapter;
import com.gdudes.app.gdudesapp.Comparators.ConversationsComparator;
import com.gdudes.app.gdudesapp.CustomViewTypes.DividerItemDecoration;
import com.gdudes.app.gdudesapp.Database.GDConversationsDBHelper;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDServices.MessageAndNotificationDownloader;
import com.gdudes.app.gdudesapp.GDTypes.Conversations;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Notifications.NotificationHelper;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.Collections;

public class MessagesPageFragment extends Fragment {
    Users LoggedInUser = null;
    Context mContext = null;
    SwipeRefreshLayout swipeMessages;
    RecyclerView MessagesRecyclerView;
    ConversationsAdapter conversationsAdapter;
    LinearLayoutManager mRecycleViewLayoutManager;
    CountDownTimer mRefreshDataCountDownTimer;
    Boolean RefreshedDataForFirstLoad = false;
    public static Boolean IsPageActive = false;
    public static Boolean IsPageScrolling = false;

    private ArrayList<String> ConversationsNeedLocalRefreshList = new ArrayList<>();
    private RelativeLayout ContentLoadedContainer = null;
    private RelativeLayout ContentLoadingContainer = null;
    private TextView ContentLoadingText = null;

    public static MessagesPageFragment MessagesPageInstance = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = this.getActivity().getBaseContext();
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        View MessagesPagelayout = inflater.inflate(R.layout.messagespagemain_layout, container, false);
        ContentLoadedContainer = MessagesPagelayout.findViewById(R.id.ContentLoadedContainer);
        ContentLoadingContainer = MessagesPagelayout.findViewById(R.id.ContentLoadingContainer);
        ContentLoadingText = MessagesPagelayout.findViewById(R.id.ContentLoadingText);
        swipeMessages = MessagesPagelayout.findViewById(R.id.swipeMessages);
        MessagesRecyclerView = MessagesPagelayout.findViewById(R.id.MessageConversationlist);
        mRecycleViewLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false) {
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
                int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
                int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
                if (StartPosition != -1 && EndPosition != -1) {
                    conversationsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
                }
            }
        };
        MessagesRecyclerView.setLayoutManager(mRecycleViewLayoutManager);
        MessagesRecyclerView.addItemDecoration(new DividerItemDecoration(mContext));
        conversationsAdapter = new ConversationsAdapter(mContext, LoggedInUser);
        MessagesRecyclerView.setAdapter(conversationsAdapter);

        swipeMessages.setOnRefreshListener(() -> {
            swipeMessages.setRefreshing(false);
            MessageAndNotificationDownloader.GetMessagesFromServer(getActivity());
        });

        MessagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    IsPageScrolling = false;
                    mRefreshDataCountDownTimer = new CountDownTimer(1500, 500) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
                            int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
                            if (StartPosition != -1 && EndPosition != -1) {
                                conversationsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
                            }
                        }
                    }.start();
                } else {
                    IsPageScrolling = true;
                    if (mRefreshDataCountDownTimer != null) {
                        mRefreshDataCountDownTimer.cancel();
                    }
                }
            }
        });
        return MessagesPagelayout;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        IsPageActive = true;
        MessagesPageInstance = MessagesPageFragment.this;
        RefreshConversationListFromCache();

        int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
        int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
        if (StartPosition != -1 && EndPosition != -1) {
            conversationsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
        }
        if (conversationsAdapter != null) {
            conversationsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        IsPageActive = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void SetConversationsNeedLocalRefresh(ArrayList<String> vConversationsNeedLocalRefreshList) {
        ConversationsNeedLocalRefreshList.addAll(vConversationsNeedLocalRefreshList);
        RefreshConversationListFromCache();
    }

    public void MarkAllRead() {
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            GDMessagesDBHelper gdMessagesDBHelper = new GDMessagesDBHelper(mContext);
            gdBackgroundTaskFinished.OnBackgroundTaskFinished(gdMessagesDBHelper.MarkAllInboundMessagesLocallyRead(LoggedInUser.UserID));
        }, data -> {
            Boolean isSuccess = (Boolean) data;
            if (isSuccess) {
                for (Conversations conversation : conversationsAdapter.GetConversationList()) {
                    conversation.UnMessageCount = 0;
                    conversation.UnreadCount = "0";
                }
                conversationsAdapter.notifyDataSetChanged();
                NotificationHelper.CancelAllNotifications();
                RefreshConversationListFromCache();
            } else {
                try {
                    TopSnackBar.MakeSnackBar(getActivity().findViewById(R.id.BodyBelowAppBar_Not), getString(R.string.generic_error_message),
                            TopSnackBar.LENGTH_SHORT, true).show();
                } catch (Exception ex) {
                    GDLogHelper.LogException(ex);
                }
            }
        });
    }

    private void RefreshConversationListFromCache() {
        if (conversationsAdapter.GetConversationList().size() == 0) {
            GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
                ArrayList<Conversations> conversations = new GDConversationsDBHelper(mContext).GetAllConversationsFromCache(LoggedInUser.UserID);
                if (conversations.size() > 0) {
                    gdBackgroundTaskFinished.OnBackgroundTaskFinished(conversations);
                } else {
                    gdBackgroundTaskFinished.OnBackgroundTaskFinished(null);
                }
            }, data -> {
                if (data == null) {
                    ConversationListChanged();
                    return;
                }
                ArrayList<Conversations> conversations = (ArrayList<Conversations>) data;
                Conversations.AddOrUpdateConversations(conversations, conversationsAdapter.GetConversationList());
                DoAfterRefreshFromCache(true, true);
            });
        } else if (ConversationsNeedLocalRefreshList != null && ConversationsNeedLocalRefreshList.size() > 0) {
            GDConversationsDBHelper gdConversationsDBHelper = new GDConversationsDBHelper(mContext);
            ArrayList<Conversations> updatedConversations = gdConversationsDBHelper.GetConversationsForConverseWithIDList(LoggedInUser.UserID,
                    ConversationsNeedLocalRefreshList);
            Conversations.AddOrUpdateConversations(updatedConversations, conversationsAdapter.GetConversationList());
            Conversations.SetGetCountFromLocalDBToTrue(conversationsAdapter.GetConversationList(), ConversationsNeedLocalRefreshList);
            DoAfterRefreshFromCache(true, false);
        } else {
            DoAfterRefreshFromCache(false, false);
        }
    }

    private void DoAfterRefreshFromCache(Boolean ConversationListChanged, Boolean ConversationsAdded) {
        if (ConversationListChanged) {
            Conversations.SetDateForConversationList(conversationsAdapter.GetConversationList());
            Collections.sort(conversationsAdapter.GetConversationList(), new ConversationsComparator());
            conversationsAdapter.setConversationList(conversationsAdapter.GetConversationList(), ConversationsAdded);

            int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
            int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
            if (StartPosition != -1 && EndPosition != -1) {
                conversationsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
            }
            ConversationsNeedLocalRefreshList.clear();
            conversationsAdapter.notifyDataSetChanged();
        } else {
            conversationsAdapter.notifyDataSetChanged();
        }
        LayoutActivity.TabIconCountsNeedRefresh = true;
        if (LayoutActivity.LayoutActivityInstance != null) {
            LayoutActivity.LayoutActivityInstance.RefreshTabIconCount();
        }
        ConversationListChanged();
    }

    public void SetSearchActive(Boolean IsActive) {
        conversationsAdapter.SetSearchActive(IsActive);
    }

    public void SearchByName(String vSearchPhrase) {
        conversationsAdapter.ApplyNameFilter(vSearchPhrase);
    }

    private void ConversationListChanged() {
        if (conversationsAdapter.getItemCount() == 0) {
            ContentLoadingContainer.setVisibility(View.VISIBLE);
            ContentLoadedContainer.setVisibility(View.GONE);
            ContentLoadingText.setText("No conversations to show.");
        } else {
            ContentLoadingContainer.setVisibility(View.GONE);
            ContentLoadedContainer.setVisibility(View.VISIBLE);
        }
    }
}
