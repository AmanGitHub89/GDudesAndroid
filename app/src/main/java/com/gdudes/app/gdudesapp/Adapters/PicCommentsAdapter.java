package com.gdudes.app.gdudesapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.Comparators.CommentsComparator;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.PicComment;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDSmileyHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.SnackCallback;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.OnGDItemClickListener;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PicCommentsAdapter extends RecyclerView.Adapter<PicCommentsAdapter.PicCommentsViewHolder> {

    private ArrayList<PicComment> mPicCommentsList;
    private Users LoggedInUser;
    private Context mContext;
    GDImageDBHelper gdImageDBHelper;
    private SnackCallback mSnackCallback;
    private Drawable DefaultPicDrawable;

    public PicCommentsAdapter(Context c, Users vLoggedInUser, SnackCallback snackCallback) {
        mContext = c;
        LoggedInUser = vLoggedInUser;
        mPicCommentsList = new ArrayList<>();
        gdImageDBHelper = new GDImageDBHelper(mContext);
        mSnackCallback = snackCallback;
        DefaultPicDrawable = ContextCompat.getDrawable(mContext, R.drawable.defaultuserprofilepic);
    }

    public void AddCommentsToList(ArrayList<PicComment> picCommentsList, Boolean IsReload) {
        if (IsReload) {
            mPicCommentsList.clear();
        }
        for (int i = 0; i < picCommentsList.size(); i++) {
            if (!mPicCommentsList.contains(picCommentsList.get(i))) {
                mPicCommentsList.add(picCommentsList.get(i));
            }
        }
        Collections.sort(mPicCommentsList, new CommentsComparator());
        GetLocalPicsForAllComments();
        notifyDataSetChanged();
    }

    @Override
    public PicCommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment, parent, false);
        PicCommentsViewHolder picCommentsViewHolder = new PicCommentsViewHolder(view, (view1, position, IsImageClicked) -> {
            PicComment CurrentPicComment = mPicCommentsList.get(position);
            if (IsImageClicked) {
                Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                intent.putExtra("ClickedUserID", CurrentPicComment.SenderUserID);
                if (CurrentPicComment.PicID != null && !CurrentPicComment.PicID.trim().equals("")) {
                    intent.putExtra("ProfilePicID", CurrentPicComment.PicID);
                }
                mContext.startActivity(intent);
            } else {
                if (CurrentPicComment.SenderUserID.equalsIgnoreCase(LoggedInUser.UserID)
                        || CurrentPicComment.OwnerUserID.equalsIgnoreCase(LoggedInUser.UserID)) {
                    GDDialogHelper.ShowYesNoTypeDialog(mContext, "Delete Comment?", "Do you want to delete this comment?",
                            GDDialogHelper.BUTTON_TEXT_DELETE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.INFO, () -> {
                                List<APICallParameter> pAPICallParameters = new ArrayList<>();
                                pAPICallParameters.add(new APICallParameter(APICallParameter.param_CommentID, CurrentPicComment.CommentID));
                                pAPICallParameters.add(new APICallParameter(APICallParameter.param_RequestingUserID, LoggedInUser.UserID));
                                APICallInfo apiCallInfo = new APICallInfo("Home", "DeleteUserPicComment", pAPICallParameters, "GET", null, null, false,
                                        new APIProgress(mContext, "Deleting..", true), APICallInfo.APITimeouts.MEDIUM);
                                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                                    try {
                                        if (result.equals("1")) {
                                            mPicCommentsList.remove(position);
                                            notifyDataSetChanged();
                                        } else {
                                            mSnackCallback.MakeSnack(mContext.getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true);
                                        }
                                    } catch (Exception e) {
                                        GDLogHelper.LogException(e);
                                        mSnackCallback.MakeSnack(mContext.getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true);
                                    }
                                }, () -> mSnackCallback.MakeSnack(mContext.getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true));
                            }, null);
                }
            }
        });
        return picCommentsViewHolder;
    }

    @Override
    public void onBindViewHolder(PicCommentsViewHolder holder, int position) {
        PicComment CurrentComment = mPicCommentsList.get(position);
        if (CurrentComment.image != null) {
            holder.UserImage.setImageBitmap(CurrentComment.image);
        } else {
            holder.UserImage.setImageDrawable(DefaultPicDrawable);
        }
        holder.UserName.setText(StringEncoderHelper.decodeURIComponent(CurrentComment.UserName));
        String TextToShow = StringEncoderHelper.doubleDecodeURIComponent(CurrentComment.Comment).trim();
        holder.Comment.setText(GDSmileyHelper.ShowSmileysForText(mContext, TextToShow, mContext.getResources(), true));
        holder.CommentDateTime.setText(GDDateTimeHelper.GetFormattedDateAndTime(CurrentComment.dCommentDT, true));
    }

    @Override
    public int getItemCount() {
        return mPicCommentsList.size();
    }

    public PicComment getItemAt(int position) {
        return mPicCommentsList.get(position);
    }

    public void RefreshDataForVisibleConversations(int StartPosition, int EndPosition) {
        if (EndPosition <= StartPosition) {
            return;
        }
        ArrayList<PicComment> RefreshDataForPicComments = new ArrayList<>();
        for (int i = StartPosition; i <= EndPosition; i++) {
            PicComment picComment = mPicCommentsList.get(i);
            if (picComment != null && !picComment.IsDataRefreshed) {
                Users TempUser = UserObjectsCacheHelper.GetUserFromCache(mPicCommentsList.get(i).SenderUserID);
                if (TempUser != null) {
                    PicComment.UpdateUserDetails(TempUser, mPicCommentsList);
                } else {
                    RefreshDataForPicComments.add(mPicCommentsList.get(i));
                }
            }
        }
        if (RefreshDataForPicComments.size() > 0) {
            GetMiniViewsForUsers(RefreshDataForPicComments);
        }
    }

    private void GetMiniViewsForUsers(final ArrayList<PicComment> RefreshDataForPicComments) {
        new HomeAPICalls(mContext).GetMiniProfilesForUserIDList(PicComment.GetUserIDList(RefreshDataForPicComments),
                new APICallerResultCallback() {
                    @Override
                    public void OnComplete(Object result, Object extraData) {
                        ArrayList<Users> users = (ArrayList<Users>) result;
                        PicComment.UpdateUserDetails(users, mPicCommentsList);
                        PicComment.SetDataRefreshed(mPicCommentsList, users);
                        notifyDataSetChanged();
                        GetPicsForComments(RefreshDataForPicComments);
                    }

                    @Override
                    public void OnError(String result, Object extraData) {
                    }

                    @Override
                    public void OnNoNetwork(Object extraData) {
                    }
                });
    }

    private void GetLocalPicsForAllComments() {
        ArrayList<String> picIDList = PicComment.GetPicIDListForNullImages(mPicCommentsList);
        ImageAPIHelper.GetPicsForPicIDList(mContext, picIDList, true, pics -> {
            PicComment.SetPicsToComments(pics, mPicCommentsList);
            notifyDataSetChanged();
        });
    }

    private void GetPicsForComments(final ArrayList<PicComment> PicCommentsList) {
        ArrayList<String> picIDList = PicComment.GetPicIDListForNullImages(PicCommentsList);
        ImageAPIHelper.GetPicsForPicIDList(mContext, picIDList, false, pics -> {
            PicComment.SetPicsToComments(pics, mPicCommentsList);
            notifyDataSetChanged();
        });
    }


    static class PicCommentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView UserImage;
        TextView UserName;
        TextView Comment;
        TextView CommentDateTime;
        private OnGDItemClickListener ItemClickListener;

        public PicCommentsViewHolder(View itemView, OnGDItemClickListener itemClickListener) {
            super(itemView);
            ItemClickListener = itemClickListener;
            UserImage = (RoundedImageView) itemView.findViewById(R.id.UserImage);
            UserName = (TextView) itemView.findViewById(R.id.UserName);
            Comment = (TextView) itemView.findViewById(R.id.Comment);
            CommentDateTime = (TextView) itemView.findViewById(R.id.CommentDateTime);
            UserImage.setTag(true);
            itemView.setTag(false);
            UserImage.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ItemClickListener != null) {
                ItemClickListener.onItemClick(v, getLayoutPosition(), (Boolean) v.getTag());
            }
        }
    }
}
