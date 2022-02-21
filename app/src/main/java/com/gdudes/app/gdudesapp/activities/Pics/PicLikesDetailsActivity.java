package com.gdudes.app.gdudesapp.activities.Pics;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.CustomViewTypes.DividerItemDecoration;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.GDTypes.PicLikeDislike;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.ImageAPIHelperDelegate;
import com.gdudes.app.gdudesapp.Interfaces.OnGDItemClickListener;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PicLikesDetailsActivity extends GDCustomToolbarAppCompatActivity {

    LinearLayoutManager mRecycleViewLayoutManager;
    SwipeRefreshLayout SwipeLikeDislike;
    RecyclerView PicLikeDislikelist;
    PicLikeDislikeAdapter picLikeDislikeAdapter;
    CountDownTimer mCountDownTimer;
    private RelativeLayout ContentLoadedContainer = null;
    private RelativeLayout ContentLoadingContainer = null;
    private TextView ContentLoadingText = null;

    private Users LoggedInUser;
    private Context mContext;
    private String PicID = "";
    private Boolean ListRefreshIsRunning = false;
    private Boolean AllLoaded = false;
    Boolean RefreshedDataForFirstLoad = false;

    public PicLikesDetailsActivity() {
        super("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_like_dislike_details);

        mContext = PicLikesDetailsActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        LoadActivityForIntent(getIntent());
        HasActions = false;
        ShowTitleWithoutActions = true;
        postCreate();
    }

    private void LoadActivityForIntent(Intent intent) {
        try {
            setToolbarText("Photo likes");
            PicID = intent.getStringExtra("PicID");
            ContentLoadedContainer = findViewById(R.id.ContentLoadedContainer);
            ContentLoadingContainer = findViewById(R.id.ContentLoadingContainer);
            ContentLoadingText = findViewById(R.id.ContentLoadingText);
            SwipeLikeDislike = findViewById(R.id.SwipeLikeDislike);
            PicLikeDislikelist = findViewById(R.id.PicLikeDislikelist);
            mRecycleViewLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false) {
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
                        picLikeDislikeAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
                    }
                }
            };
            PicLikeDislikelist.setLayoutManager(mRecycleViewLayoutManager);
            PicLikeDislikelist.addItemDecoration(new DividerItemDecoration(mContext));
            picLikeDislikeAdapter = new PicLikeDislikeAdapter(mContext);
            PicLikeDislikelist.setAdapter(picLikeDislikeAdapter);
            SwipeLikeDislike.setOnRefreshListener(() -> GetLikesDislikesDetails(true));
            PicLikeDislikelist.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (mRecycleViewLayoutManager.findLastVisibleItemPosition() == (picLikeDislikeAdapter.getItemCount() - 1)) {
                        GetLikesDislikesDetails(false);
                        return;
                    }
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        mCountDownTimer = new CountDownTimer(1000, 500) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
                                int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
                                if (StartPosition != -1 && EndPosition != -1) {
                                    picLikeDislikeAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
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
            GetLikesDislikesDetails(false);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    private void GetLikesDislikesDetails(final Boolean Reload) {
        if (ListRefreshIsRunning) {
            return;
        }
        if (picLikeDislikeAdapter.getItemCount() == 0) {
            ContentLoadingText.setText("Loading..");
        }
        if (AllLoaded && !Reload) {
            return;
        }
        SwipeLikeDislike.setRefreshing(true);
        ListRefreshIsRunning = true;
        List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_PicID, PicID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_RequestingUserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_Likes, "TRUE"));
        if (!Reload && picLikeDislikeAdapter.getItemCount() > 0) {
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_LikeDislikeID, picLikeDislikeAdapter.getItemAt(picLikeDislikeAdapter.getItemCount() - 1).LikeDislikeID));
        }
        APICallInfo apiCallInfo = new APICallInfo("Home", "GetPicLikeDislikeDetails", pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (result.equals("0")) {
                    AllLoaded = true;
                    ListRefreshIsRunning = false;
                    SwipeLikeDislike.setRefreshing(false);
                    return;
                }
                ArrayList<PicLikeDislike> PicLikeDislikeList = new GsonBuilder().create().fromJson(result, new TypeToken<List<PicLikeDislike>>() {}.getType());
                picLikeDislikeAdapter.SetDateForConversationList(PicLikeDislikeList);
                picLikeDislikeAdapter.AddLikesDislikesToList(PicLikeDislikeList, Reload);
                int StartPosition = mRecycleViewLayoutManager.findFirstVisibleItemPosition();
                int EndPosition = mRecycleViewLayoutManager.findLastVisibleItemPosition();
                if (StartPosition != -1 && EndPosition != -1) {
                    picLikeDislikeAdapter.RefreshDataForVisibleConversations(StartPosition, EndPosition);
                }
            } catch (Exception e) {
                GDLogHelper.LogException(e);
            } finally {
                ListRefreshIsRunning = false;
                SwipeLikeDislike.setRefreshing(false);
                LikeDetailsListChanged();
            }
        }, () -> {
            ListRefreshIsRunning = false;
            SwipeLikeDislike.setRefreshing(false);
            LikeDetailsListChanged();
        });
    }

    private void LikeDetailsListChanged() {
        if (picLikeDislikeAdapter.getItemCount() == 0) {
            ContentLoadingContainer.setVisibility(View.VISIBLE);
            ContentLoadedContainer.setVisibility(View.GONE);
            ContentLoadingText.setText("Nothing to show");
        } else {
            ContentLoadingContainer.setVisibility(View.GONE);
            ContentLoadedContainer.setVisibility(View.VISIBLE);
        }
    }

    class PicLikeDislikeAdapter extends RecyclerView.Adapter<PicLikeDislikeViewHolder> {

        private ArrayList<PicLikeDislike> mPicLikeDislikeList;
        private Context mContext;
        GDImageDBHelper gdImageDBHelper;
        private Drawable DefaultPicDrawable;

        public PicLikeDislikeAdapter(Context context) {
            mContext = context;
            mPicLikeDislikeList = new ArrayList<>();
            gdImageDBHelper = new GDImageDBHelper(mContext);
            DefaultPicDrawable = ContextCompat.getDrawable(mContext, R.drawable.defaultuserprofilepic);
        }

        public void AddLikesDislikesToList(ArrayList<PicLikeDislike> PicLikeDislikeList, Boolean IsReload) {
            if (IsReload) {
                mPicLikeDislikeList.clear();
            }
            for (int i = 0; i < PicLikeDislikeList.size(); i++) {
                if (!mPicLikeDislikeList.contains(PicLikeDislikeList.get(i))) {
                    mPicLikeDislikeList.add(PicLikeDislikeList.get(i));
                }
            }
            Collections.sort(mPicLikeDislikeList, new PicLikeDislikeComparator());
            LoadPicThumbnails(mPicLikeDislikeList);
            notifyDataSetChanged();
        }

        @Override
        public PicLikeDislikeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.comment, parent, false);
            PicLikeDislikeViewHolder picLikeDislikeViewHolder = new PicLikeDislikeViewHolder(view, new OnGDItemClickListener() {
                @Override
                public void onItemClick(View view, final int position, Boolean IsImageClicked) {
                    if (IsImageClicked) {
                        Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                        intent.putExtra("ClickedUserID", mPicLikeDislikeList.get(position).SenderUserID);
                        if (mPicLikeDislikeList.get(position).PicID != null && !mPicLikeDislikeList.get(position).PicID.trim().equals("")) {
                            intent.putExtra("ProfilePicID", mPicLikeDislikeList.get(position).PicID);
                        }
                        mContext.startActivity(intent);
                    }
                }
            });
            return picLikeDislikeViewHolder;
        }

        @Override
        public void onBindViewHolder(PicLikeDislikeViewHolder holder, int position) {
            PicLikeDislike CurrentLikeDisLike = mPicLikeDislikeList.get(position);
            if (CurrentLikeDisLike.image != null) {
                holder.UserImage.setImageBitmap(CurrentLikeDisLike.image);
            } else {
                holder.UserImage.setImageDrawable(DefaultPicDrawable);
            }
            holder.UserName.setText(StringEncoderHelper.decodeURIComponent(CurrentLikeDisLike.UserName));
            holder.Comment.setText("Liked the photo");
            holder.CommentDateTime.setText(GDDateTimeHelper.GetFormattedDateAndTime(CurrentLikeDisLike.dLDDT, true));
        }

        @Override
        public int getItemCount() {
            return mPicLikeDislikeList.size();
        }

        public PicLikeDislike getItemAt(int position) {
            return mPicLikeDislikeList.get(position);
        }

        public void RefreshDataForVisibleConversations(int StartPosition, int EndPosition) {
            if (EndPosition <= StartPosition) {
                return;
            }
            ArrayList<PicLikeDislike> RefreshDataForPicLikeDislike = new ArrayList<>();

            PicLikeDislike currentLike;
            for (int i = StartPosition; i <= EndPosition; i++) {
                currentLike = mPicLikeDislikeList.get(i);
                if (currentLike != null && (currentLike.IsDataRefreshed == null || !currentLike.IsDataRefreshed)) {
                    Users user = UserObjectsCacheHelper.GetUserFromCache(currentLike.SenderUserID);
                    if (user != null) {
                        currentLike.UserName = user.GetDecodedUserName();
                        currentLike.PicID = user.PicID;
                        currentLike.LDByUser = user;
                        currentLike.IsDataRefreshed = true;
                    } else {
                        RefreshDataForPicLikeDislike.add(mPicLikeDislikeList.get(i));
                    }
                }
            }
            if (RefreshDataForPicLikeDislike.size() > 0) {
                GetMiniViewsForUsers(RefreshDataForPicLikeDislike);
            }
        }

        private void GetMiniViewsForUsers(final ArrayList<PicLikeDislike> RefreshDataForPicLikeDislike) {
            new HomeAPICalls(mContext).GetMiniProfilesForUserIDList(PicLikeDislike.GetUserIDList(RefreshDataForPicLikeDislike),
                    new APICallerResultCallback() {
                @Override
                public void OnComplete(Object result, Object extraData) {
                    PicLikeDislike.UpdateUserDetails((ArrayList<Users>) result, mPicLikeDislikeList);
                    SetDataRefreshed(mPicLikeDislikeList, RefreshDataForPicLikeDislike);
                    notifyDataSetChanged();
                    LoadPicThumbnails(RefreshDataForPicLikeDislike);
                }
                @Override
                public void OnError(String result, Object extraData) {
                }
                @Override
                public void OnNoNetwork(Object extraData) {
                }
            });
        }

        private void LoadPicThumbnails(final ArrayList<PicLikeDislike> PicLikeDislikeList) {
            ArrayList<String> picIdList = PicLikeDislike.GetPicIDListForNullImages(PicLikeDislikeList);
            ImageAPIHelper.GetPicsForPicIDList(mContext, picIdList, false, new ImageAPIHelperDelegate() {
                @Override
                public void OnGDPicsAvailable(ArrayList<GDPic> pics) {
                    PicLikeDislike.SetPics(pics, mPicLikeDislikeList);
                    notifyDataSetChanged();
                }
            });
        }

        void SetDateForConversationList(ArrayList<PicLikeDislike> PicLikeDislikeList) {
            for (int i = 0; i < PicLikeDislikeList.size(); i++) {
                PicLikeDislikeList.get(i).dLDDT = GDDateTimeHelper.GetDateFromString(PicLikeDislikeList.get(i).LDDateTime);
            }
        }

        void SetDataRefreshed(ArrayList<PicLikeDislike> ToList, ArrayList<PicLikeDislike> FromList) {
            for (int i = 0; i < FromList.size(); i++) {
                int index = ToList.indexOf(new PicLikeDislike(FromList.get(i).LikeDislikeID));
                if (index != -1) {
                    ToList.get(index).IsDataRefreshed = true;
                }
            }
        }
    }

    static class PicLikeDislikeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView UserImage;
        TextView UserName;
        TextView Comment;
        TextView CommentDateTime;
        private OnGDItemClickListener ItemClickListener;

        public PicLikeDislikeViewHolder(View itemView, OnGDItemClickListener itemClickListener) {
            super(itemView);
            ItemClickListener = itemClickListener;
            UserImage = itemView.findViewById(R.id.UserImage);
            UserName = itemView.findViewById(R.id.UserName);
            Comment = itemView.findViewById(R.id.Comment);
            CommentDateTime = itemView.findViewById(R.id.CommentDateTime);

            UserImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ItemClickListener != null) {
                ItemClickListener.onItemClick(v, getLayoutPosition(), true);
            }
        }
    }

    class PicLikeDislikeComparator implements Comparator<PicLikeDislike> {
        @Override
        public int compare(PicLikeDislike lhs, PicLikeDislike rhs) {
            return rhs.LDDateTime.compareTo(lhs.LDDateTime);
        }
    }
}
