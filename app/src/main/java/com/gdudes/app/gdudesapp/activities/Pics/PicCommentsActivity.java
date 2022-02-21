package com.gdudes.app.gdudesapp.activities.Pics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.Adapters.PicCommentsAdapter;
import com.gdudes.app.gdudesapp.CustomViewTypes.DividerItemDecoration;
import com.gdudes.app.gdudesapp.GDTypes.PicComment;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class PicCommentsActivity extends GDCustomToolbarAppCompatActivity {

    LinearLayoutManager mRecycleViewLayoutManager;
    SwipeRefreshLayout SwipeComments;
    RecyclerView PicCommentslist;
    PicCommentsAdapter picCommentsAdapter;
    CountDownTimer mCountDownTimer;
    private RelativeLayout ContentLoadedContainer = null;
    private RelativeLayout ContentLoadingContainer = null;
    private TextView ContentLoadingText = null;

    private Users LoggedInUser;
    private Context mContext;
    private String PicID = "";
    private String OwnerUserID = "";
    private Boolean ListRefreshIsRunning = false;
    private Boolean AllCommentsLoaded = false;

    EditText NewCommentText;
    ImageView SendComment;
    Boolean RefreshedDataForFirstLoad = false;

    public PicCommentsActivity() {
        super("Photo comments");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_comments);
        mContext = PicCommentsActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        LoadActivityForIntent(getIntent());
        postCreate();
    }

    private void LoadActivityForIntent(Intent intent) {
        ContentLoadedContainer = findViewById(R.id.ContentLoadedContainer);
        ContentLoadingContainer = findViewById(R.id.ContentLoadingContainer);
        ContentLoadingText = findViewById(R.id.ContentLoadingText);
        SwipeComments = findViewById(R.id.SwipeComments);
        PicCommentslist = findViewById(R.id.PicCommentslist);
        mRecycleViewLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                super.onLayoutChildren(recycler, state);
//                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
//                if (firstVisibleItemPosition == -1)
//                    return;
//                if (RefreshedDataForFirstLoad) {
//                    return;
//                }
//                RefreshedDataForFirstLoad = true;
//                int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
//                int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
//                if (StartPosition != -1 && EndPosition != -1) {
//                    picCommentsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
//                }
            }
        };
        PicCommentslist.setLayoutManager(mRecycleViewLayoutManager);
        PicCommentslist.addItemDecoration(new DividerItemDecoration(mContext));
        picCommentsAdapter = new PicCommentsAdapter(mContext, LoggedInUser, (text, duration, IsError) ->
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), text, duration, IsError).show());
        PicCommentslist.setAdapter(picCommentsAdapter);

        NewCommentText = findViewById(R.id.NewCommentText);
        SendComment = findViewById(R.id.SendComment);
        SendComment.setOnClickListener(v -> SendComment());

        SwipeComments.setOnRefreshListener(() -> GetComments(true));
        PicCommentslist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mRecycleViewLayoutManager.findLastVisibleItemPosition() == (picCommentsAdapter.getItemCount() - 1)) {
                    GetComments(false);
                    return;
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mCountDownTimer = new CountDownTimer(600, 300) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
                            int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
                            if (StartPosition != -1 && EndPosition != -1) {
                                picCommentsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
                            }
                        }
                    }.start();
                } else {
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                }
            }
        });
        HasActions = false;
        ShowTitleWithoutActions = true;
        try {
            PicID = intent.getStringExtra("PicID");
            OwnerUserID = intent.getStringExtra("OwnerUserID");
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GetComments(false);
    }

    private void GetComments(final Boolean Reload) {
        if (ListRefreshIsRunning) {
            return;
        }
        if (picCommentsAdapter.getItemCount() == 0) {
            ContentLoadingText.setText("Loading comments..");
        }
        if (AllCommentsLoaded && !Reload) {
            return;
        }
        SwipeComments.setRefreshing(true);
        ListRefreshIsRunning = true;
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_PicID, PicID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_RequestingUserID, LoggedInUser.UserID));
        if (!Reload && picCommentsAdapter.getItemCount() > 0) {
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_CommentID, picCommentsAdapter.getItemAt(picCommentsAdapter.getItemCount() - 1).CommentID));
        }
        APICallInfo apiCallInfo = new APICallInfo("Home", "GetUserPicComments", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (result.equals("0")) {
                    AllCommentsLoaded = true;
                    ListRefreshIsRunning = false;
                    SwipeComments.setRefreshing(false);
                    return;
                }
                ArrayList<PicComment> PicCommentsList = new GsonBuilder().create().fromJson(result, new TypeToken<List<PicComment>>() { }.getType());
                PicComment.SetDateForPicCommentList(PicCommentsList);
                picCommentsAdapter.AddCommentsToList(PicCommentsList, Reload);
//                int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
//                int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
//                if (StartPosition != -1 && EndPosition != -1) {
//                    picCommentsAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
//                }
            } catch (Exception e) {
                GDLogHelper.LogException(e);
            } finally {
                ListRefreshIsRunning = false;
                SwipeComments.setRefreshing(false);
                CommentsListChanged();
            }
        }, () -> {
            ListRefreshIsRunning = false;
            SwipeComments.setRefreshing(false);
            CommentsListChanged();
        });
    }

    private void SendComment() {
        if (NewCommentText.getText().toString().equals("")) {
            return;
        }
        NewUserPicComment newUserPicComment = new NewUserPicComment(GDGenericHelper.GetNewGUID(), PicID, OwnerUserID,
                LoggedInUser.UserID, StringEncoderHelper.encodeURIComponent(NewCommentText.getText().toString().trim()));
        final ArrayList<PicComment> PicCommentsList = new ArrayList<>();
        PicComment picComment = new PicComment(newUserPicComment.CommentID, LoggedInUser.PicID, OwnerUserID, LoggedInUser.UserID,
                StringEncoderHelper.encodeURIComponent(NewCommentText.getText().toString().trim()),
                GDDateTimeHelper.GetCurrentDateTimeAsString(true), LoggedInUser.GetDecodedUserName(), LoggedInUser, true);
        PicCommentsList.add(picComment);
        PicComment.SetDateForPicCommentList(PicCommentsList);
        APICallInfo apiCallInfo = new APICallInfo("Home", "InsertUpdNewUserPicComment", null, "POST", newUserPicComment, newUserPicComment.CommentID, false,
                new APIProgress(PicCommentsActivity.this, "Posting comment. Please wait..", true), APICallInfo.APITimeouts.MEDIUM);
        NewCommentText.setText("");
        GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                if (successResult.SuccessResult == 1) {
                    picCommentsAdapter.AddCommentsToList(PicCommentsList, false);
                } else {
                    NewCommentText.setText(StringEncoderHelper.decodeURIComponent(PicCommentsList.get(0).Comment));
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), successResult.FailureMessage, TopSnackBar.LENGTH_LONG, true).show();
                }
            } catch (Exception e) {
                NewCommentText.setText(StringEncoderHelper.decodeURIComponent(PicCommentsList.get(0).Comment));
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
            } finally {
                CommentsListChanged();
            }
        }, () -> {
            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
            CommentsListChanged();
        });
    }

    private void CommentsListChanged() {
        if (picCommentsAdapter.getItemCount() == 0) {
            ContentLoadingContainer.setVisibility(View.VISIBLE);
            ContentLoadedContainer.setVisibility(View.GONE);
            ContentLoadingText.setText("No comments to show.");
        } else {
            ContentLoadingContainer.setVisibility(View.GONE);
            ContentLoadedContainer.setVisibility(View.VISIBLE);
        }
    }

    class NewUserPicComment {
        public String CommentID;
        public String PicID;
        public String OwnerUserID;
        public String SenderID;
        public String Comment;

        public NewUserPicComment(String vCommentID, String vPicID, String vOwnerUserID, String vSenderID, String vComment) {
            CommentID = vCommentID;
            PicID = vPicID;
            OwnerUserID = vOwnerUserID;
            SenderID = vSenderID;
            Comment = vComment;
        }
    }
}
