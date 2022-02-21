package com.gdudes.app.gdudesapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.GDNotification;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.OnGDItemClickListener;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Pics.GDPicViewerActivity;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsMainViewHolder> {
    private static final int NOTIFICATION_VIEW = 0;
    private static final int NOTIFICATION_ICEBREAKER_VIEW = 1;
    private static final int NOTIFICATION_POPULARRANK_VIEW = 2;
    private Context mContext;
    private ArrayList<GDNotification> mNotificationsList;
    private LayoutInflater mInflater;
    Users LoggedInUser;
    GDImageDBHelper mGdImageDBHelper;
    private Drawable mDefaultUserProfilePic;

    public NotificationsAdapter(Context c, Users vLoggedInUser) {
        mContext = c;
        LoggedInUser = vLoggedInUser;
        mInflater = LayoutInflater.from(c);
        mNotificationsList = new ArrayList<>();
        mGdImageDBHelper = new GDImageDBHelper(mContext);
        mDefaultUserProfilePic = ContextCompat.getDrawable(mContext, R.drawable.defaultuserprofilepic);
    }

    public void setNotificationsList(List<GDNotification> vNotificationsList, Boolean IsSwipeRefresh) {
        if (IsSwipeRefresh) {
            //Delete all friend requests first
            List<GDNotification> FRNotifications = new ArrayList<>();
            for (int i = 0; i < mNotificationsList.size(); i++) {
                if (mNotificationsList.get(i).NotificationType.trim().equals("FR")) {
                    FRNotifications.add(mNotificationsList.get(i));
                }
            }
            mNotificationsList.removeAll(FRNotifications);

            List<GDNotification> NotificationInOrder = new ArrayList<>();
            for (int i = 0; i < vNotificationsList.size(); i++) {
                if (!mNotificationsList.contains(vNotificationsList.get(i))) {
                    NotificationInOrder.add(vNotificationsList.get(i));
                }
            }
            NotificationInOrder.addAll(mNotificationsList);
            mNotificationsList.clear();
            mNotificationsList.addAll(NotificationInOrder);
        } else {
            for (int i = 0; i < vNotificationsList.size(); i++) {
                if (!mNotificationsList.contains(vNotificationsList.get(i))) {
                    mNotificationsList.add(vNotificationsList.get(i));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public NotificationsMainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case NOTIFICATION_VIEW:
                return new NotificationsViewHolder(mInflater.inflate(R.layout.notification_item, parent, false),
                        (view, position, IsUserImageClickced) -> {
                            if (IsUserImageClickced) {
                                Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                                intent.putExtra("ClickedUserID", mNotificationsList.get(position).SenderID);
                                if (mNotificationsList.get(position).PicID != null && !mNotificationsList.get(position).PicID.trim().equals("")) {
                                    intent.putExtra("ProfilePicID", mNotificationsList.get(position).PicID);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            } else {
                                OpenActivityForNotificationLink(position);
                            }
                        });
            case NOTIFICATION_ICEBREAKER_VIEW:
                return new NotificationsIceBreakerViewHolder(mInflater.inflate(R.layout.notification_icebreaker_item, parent, false),
                        (view, position, IsUserImageClickced) -> {
                            if (IsUserImageClickced) {
                                Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                                intent.putExtra("ClickedUserID", mNotificationsList.get(position).SenderID);
                                if (mNotificationsList.get(position).PicID != null && !mNotificationsList.get(position).PicID.trim().equals("")) {
                                    intent.putExtra("ProfilePicID", mNotificationsList.get(position).PicID);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            } else {
                                OpenActivityForNotificationLink(position);
                            }
                        });
            case NOTIFICATION_POPULARRANK_VIEW:
                return new NotificationsPopularityRankViewHolder(mInflater.inflate(R.layout.notification_popularityrank_item, parent, false),
                        (view, position, IsUserImageClickced) -> {
                            if (IsUserImageClickced) {
                                Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                                intent.putExtra("ClickedUserID", mNotificationsList.get(position).SenderID);
                                if (mNotificationsList.get(position).PicID != null && !mNotificationsList.get(position).PicID.trim().equals("")) {
                                    intent.putExtra("ProfilePicID", mNotificationsList.get(position).PicID);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            } else {
                                OpenActivityForNotificationLink(position);
                            }
                        });
        }
        return null;
    }

    @Override
    public void onBindViewHolder(NotificationsMainViewHolder holder, int position) {
        GDNotification CurrentNotification = mNotificationsList.get(position);
        if (holder.getItemViewType() == NOTIFICATION_VIEW) {
            NotificationsViewHolder notificationsViewHolder = (NotificationsViewHolder) holder;
            if (CurrentNotification != null) {
                if (CurrentNotification.image != null) {
                    notificationsViewHolder.UserImage.setImageBitmap(CurrentNotification.image);
                } else {
                    notificationsViewHolder.UserImage.setImageDrawable(mDefaultUserProfilePic);
                }

                notificationsViewHolder.NotificationSenderName.setText(StringEncoderHelper.decodeURIComponent(CurrentNotification.SenderName));
                notificationsViewHolder.NotificationMessage.setText(CurrentNotification.Message);
                if (CurrentNotification.NotificationSeen != 1 || CurrentNotification.NotificationType.equals("FR")) {
                    notificationsViewHolder.NotificationContainer.setBackgroundColor(Color.parseColor("#c1c2c5"));
                } else {
                    notificationsViewHolder.NotificationContainer.setBackgroundColor(Color.parseColor("#00c1c2c5"));
                }
                if (GDDateTimeHelper.IsDateInSameDay(GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true)) {
                    notificationsViewHolder.NotificationDate.setText(GDDateTimeHelper.GetTimeStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsViewHolder.NotificationTime.setText("");
                    notificationsViewHolder.NotificationTime.setVisibility(View.GONE);
                } else {
                    notificationsViewHolder.NotificationDate.setText(GDDateTimeHelper.GetDateOnlyStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsViewHolder.NotificationTime.setText(GDDateTimeHelper.GetTimeStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsViewHolder.NotificationTime.setVisibility(View.VISIBLE);
                }
                if (CurrentNotification.NotificationType.equals("FR")) {
                    notificationsViewHolder.FriendsIcon.setVisibility(View.VISIBLE);
                } else {
                    notificationsViewHolder.FriendsIcon.setVisibility(View.GONE);
                }
            }
        } else if (holder.getItemViewType() == NOTIFICATION_ICEBREAKER_VIEW) {
            NotificationsIceBreakerViewHolder notificationsIceBreakerViewHolder = (NotificationsIceBreakerViewHolder) holder;
            if (CurrentNotification != null) {
                if (CurrentNotification.image != null) {
                    notificationsIceBreakerViewHolder.UserImage.setImageBitmap(CurrentNotification.image);
                } else {
                    notificationsIceBreakerViewHolder.UserImage.setImageDrawable(mDefaultUserProfilePic);
                }
                notificationsIceBreakerViewHolder.NotificationSenderName.setText(StringEncoderHelper.decodeURIComponent(CurrentNotification.SenderName));
                notificationsIceBreakerViewHolder.NotificationIceBreakerImage.setImageResource(GDGenericHelper.GetIceBreakResourceFromCode(CurrentNotification.Message));
                notificationsIceBreakerViewHolder.NotificationMessage.setText(GDGenericHelper.GetIceBreakMessageFromCode(CurrentNotification.Message));
                if (CurrentNotification.NotificationSeen == 1) {
                    notificationsIceBreakerViewHolder.NotificationContainer.setBackgroundColor(Color.parseColor("#00c1c2c5"));
                } else {
                    notificationsIceBreakerViewHolder.NotificationContainer.setBackgroundColor(Color.parseColor("#c1c2c5"));
                }
                if (GDDateTimeHelper.IsDateInSameDay(GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true)) {
                    notificationsIceBreakerViewHolder.NotificationDate.setText(GDDateTimeHelper.GetTimeStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsIceBreakerViewHolder.NotificationTime.setText("");
                    notificationsIceBreakerViewHolder.NotificationTime.setVisibility(View.GONE);
                } else {
                    notificationsIceBreakerViewHolder.NotificationDate.setText(GDDateTimeHelper.GetDateOnlyStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsIceBreakerViewHolder.NotificationTime.setText(GDDateTimeHelper.GetTimeStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsIceBreakerViewHolder.NotificationTime.setVisibility(View.VISIBLE);
                }
            }
        } else if (holder.getItemViewType() == NOTIFICATION_POPULARRANK_VIEW) {
            NotificationsPopularityRankViewHolder notificationsPopularityRankViewHolder = (NotificationsPopularityRankViewHolder) holder;
            if (CurrentNotification != null) {
                if (CurrentNotification.image != null) {
                    notificationsPopularityRankViewHolder.UserImage.setImageBitmap(CurrentNotification.image);
                } else {
                    notificationsPopularityRankViewHolder.UserImage.setImageDrawable(mDefaultUserProfilePic);
                }
                notificationsPopularityRankViewHolder.NotificationSenderName.setText(StringEncoderHelper.decodeURIComponent(CurrentNotification.SenderName));
                notificationsPopularityRankViewHolder.PopularityRankText.setText((CurrentNotification.PopularityRank < 10 ? " " : "")
                        + Integer.toString(CurrentNotification.PopularityRank));
                notificationsPopularityRankViewHolder.NotificationMessage.setText(StringEncoderHelper.decodeURIComponent(CurrentNotification.Message));
                notificationsPopularityRankViewHolder.NotificationContainer.setBackgroundColor(Color.parseColor("#00c1c2c5"));
//                if (CurrentNotification.NotificationSeen == 1) {
//                    notificationsPopularityRankViewHolder.NotificationContainer.setBackgroundColor(Color.parseColor("#00c1c2c5"));
//                } else {
//                    notificationsPopularityRankViewHolder.NotificationContainer.setBackgroundColor(Color.parseColor("#c1c2c5"));
//                }
                if (GDDateTimeHelper.IsDateInSameDay(GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true)) {
                    notificationsPopularityRankViewHolder.NotificationDate.setText(GDDateTimeHelper.GetTimeStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsPopularityRankViewHolder.NotificationTime.setText("");
                    notificationsPopularityRankViewHolder.NotificationTime.setVisibility(View.GONE);
                } else {
                    notificationsPopularityRankViewHolder.NotificationDate.setText(GDDateTimeHelper.GetDateOnlyStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsPopularityRankViewHolder.NotificationTime.setText(GDDateTimeHelper.GetTimeStringFromDate(
                            GDDateTimeHelper.GetDateFromString(CurrentNotification.NotificationDateTime), true));
                    notificationsPopularityRankViewHolder.NotificationTime.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public GDNotification getItem(int position) {
        return mNotificationsList.get(position);
    }

    public List<GDNotification> GetItemList() {
        return mNotificationsList;
    }

    @Override
    public int getItemCount() {
        return mNotificationsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mNotificationsList.get(position).NotificationType.trim()) {
            case "IB":
                return NOTIFICATION_ICEBREAKER_VIEW;
            case "GPR":
                return NOTIFICATION_POPULARRANK_VIEW;
            default:
                return NOTIFICATION_VIEW;
        }
    }

    private void OpenActivityForNotificationLink(int position) {
        Intent intent = null;
        switch (mNotificationsList.get(position).NotificationType.trim()) {
            case "IB":
                //Friendship Request Response
                intent = new Intent(mContext, NewProfileViewActivity.class);
                intent.putExtra("ClickedUserID", mNotificationsList.get(position).SenderID);
                if (mNotificationsList.get(position).PicID != null && !mNotificationsList.get(position).PicID.trim().equals("")) {
                    intent.putExtra("ProfilePicID", mNotificationsList.get(position).PicID);
                }
                break;
            //Friendship Request
            //Ice Breaker
//                intent = new Intent(mContext, NewProfileViewActivity.class);
//                intent.putExtra("ClickedUserID", mNotificationsList.get(position).LinkID);
//                break;

            case "PCA":
                //Owner also commented on his Picture
                intent = new Intent(mContext, GDPicViewerActivity.class);
                intent.putExtra("SinglePicID", mNotificationsList.get(position).LinkID);
                intent.putExtra("SinglePicOwnerID", mNotificationsList.get(position).SenderID);
                break;
            case "PC":
                //Picture Comment
            case "PLD":
                //Picture Like Dislike
                intent = new Intent(mContext, GDPicViewerActivity.class);
                intent.putExtra("SinglePicID", mNotificationsList.get(position).LinkID);
                intent.putExtra("SinglePicOwnerID", LoggedInUser.UserID);
                break;
            case "GPR":
                //GDudes Popularity Rank
                intent = new Intent(mContext, NewProfileViewActivity.class);
                intent.putExtra("ClickedUserID", mNotificationsList.get(position).SenderID);
                if (mNotificationsList.get(position).PicID != null && !mNotificationsList.get(position).PicID.trim().equals("")) {
                    intent.putExtra("ProfilePicID", mNotificationsList.get(position).PicID);
                }
                break;
        }
        if (intent != null) {
            try {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
            }
        }
    }

    public void MarkNotificationsSeen() {
        for (int i = 0; i < mNotificationsList.size(); i++) {
            mNotificationsList.get(i).NotificationSeen = 1;
        }
        notifyDataSetChanged();
    }

    public void RefreshDataForVisibleConversations(int StartPosition, int EndPosition) {
        if (EndPosition < StartPosition || mNotificationsList.size() < (EndPosition + 1)) {
            return;
        }
        ArrayList<GDNotification> RefreshDataForConversations = new ArrayList<>();
        for (int i = StartPosition; i <= EndPosition; i++) {
            if (!mNotificationsList.get(i).IsDataRefreshed) {
                RefreshDataForConversations.add(mNotificationsList.get(i));
            }
        }
        if (RefreshDataForConversations.size() > 0) {
            GetMiniViewsForUsers(RefreshDataForConversations);
        }
    }

    private void GetMiniViewsForUsers(final ArrayList<GDNotification> RefreshDataForNotifications) {
        ArrayList<String> userIDs = GDNotification.GetUserIDList(RefreshDataForNotifications);
        new HomeAPICalls(mContext).GetMiniProfilesForUserIDList(userIDs, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                GDNotification.SetDataRefreshed(mNotificationsList, RefreshDataForNotifications);
                notifyDataSetChanged();
                GetPicsForNotifications(RefreshDataForNotifications);
            }

            @Override
            public void OnError(String result, Object extraData) {
            }

            @Override
            public void OnNoNetwork(Object extraData) {
            }
        });
    }

    public void GetPicsForNotifications(final ArrayList<GDNotification> notifications) {
        ArrayList<String> picIDList = GDNotification.GetPicIDListForNullImages(notifications);
        ImageAPIHelper.GetPicsForPicIDList(mContext, picIDList, false, pics -> {
            if (pics.size() == 0) {
                return;
            }
            GDNotification.SetPicsToNotifications(pics, mNotificationsList);
            notifyDataSetChanged();
        });
    }


    static class NotificationsViewHolder extends NotificationsMainViewHolder implements View.OnClickListener {
        RoundedImageView UserImage;
        TextView NotificationSenderName;
        TextView NotificationMessage;
        OnGDItemClickListener ItemClickListener;
        LinearLayout NotificationContainer;
        TextView NotificationDate;
        TextView NotificationTime;
        ImageView FriendsIcon;

        public NotificationsViewHolder(View itemView, OnGDItemClickListener itemClickListener) {
            super(itemView);
            ItemClickListener = itemClickListener;
            UserImage = (RoundedImageView) itemView.findViewById(R.id.NotificationUserImage);
            NotificationSenderName = (TextView) itemView.findViewById(R.id.NotificationUserName);
            NotificationMessage = (TextView) itemView.findViewById(R.id.NotificationMessage);
            NotificationContainer = (LinearLayout) itemView.findViewById(R.id.NotificationContainer);
            NotificationDate = (TextView) itemView.findViewById(R.id.NotificationDate);
            NotificationTime = (TextView) itemView.findViewById(R.id.NotificationTime);
            FriendsIcon = (ImageView) itemView.findViewById(R.id.FriendsIcon);

            UserImage.setTag(true);
            UserImage.setOnClickListener(this);
            itemView.setTag(false);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ItemClickListener != null) {
                ItemClickListener.onItemClick(v, getLayoutPosition(), (Boolean) v.getTag());
            }
        }
    }

    static class NotificationsIceBreakerViewHolder extends NotificationsMainViewHolder implements View.OnClickListener {
        RoundedImageView UserImage;
        TextView NotificationSenderName;
        TextView NotificationMessage;
        ImageView NotificationIceBreakerImage;
        OnGDItemClickListener ItemClickListener;
        LinearLayout NotificationContainer;
        TextView NotificationDate;
        TextView NotificationTime;

        public NotificationsIceBreakerViewHolder(View itemView, OnGDItemClickListener itemClickListener) {
            super(itemView);
            ItemClickListener = itemClickListener;
            UserImage = (RoundedImageView) itemView.findViewById(R.id.NotificationUserImage);
            NotificationSenderName = (TextView) itemView.findViewById(R.id.NotificationUserName);
            NotificationMessage = (TextView) itemView.findViewById(R.id.NotificationMessage);
            NotificationIceBreakerImage = (ImageView) itemView.findViewById(R.id.NotificationIceBreakerImage);
            NotificationContainer = (LinearLayout) itemView.findViewById(R.id.NotificationContainer);
            NotificationDate = (TextView) itemView.findViewById(R.id.NotificationDate);
            NotificationTime = (TextView) itemView.findViewById(R.id.NotificationTime);

            UserImage.setTag(true);
            UserImage.setOnClickListener(this);
            itemView.setTag(false);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ItemClickListener != null) {
                ItemClickListener.onItemClick(v, getLayoutPosition(), (Boolean) v.getTag());
            }
        }
    }

    static class NotificationsPopularityRankViewHolder extends NotificationsMainViewHolder implements View.OnClickListener {
        RoundedImageView UserImage;
        TextView NotificationSenderName;
        TextView NotificationMessage;
        TextView PopularityRankText;
        OnGDItemClickListener ItemClickListener;
        LinearLayout NotificationContainer;
        TextView NotificationDate;
        TextView NotificationTime;

        public NotificationsPopularityRankViewHolder(View itemView, OnGDItemClickListener itemClickListener) {
            super(itemView);
            ItemClickListener = itemClickListener;
            UserImage = (RoundedImageView) itemView.findViewById(R.id.NotificationUserImage);
            NotificationSenderName = (TextView) itemView.findViewById(R.id.NotificationUserName);
            NotificationMessage = (TextView) itemView.findViewById(R.id.NotificationMessage);
            PopularityRankText = (TextView) itemView.findViewById(R.id.PopularityRankText);
            NotificationContainer = (LinearLayout) itemView.findViewById(R.id.NotificationContainer);
            NotificationDate = (TextView) itemView.findViewById(R.id.NotificationDate);
            NotificationTime = (TextView) itemView.findViewById(R.id.NotificationTime);

            UserImage.setTag(true);
            UserImage.setOnClickListener(this);
            itemView.setTag(false);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ItemClickListener != null) {
                ItemClickListener.onItemClick(v, getLayoutPosition(), (Boolean) v.getTag());
            }
        }
    }

    static class NotificationsMainViewHolder extends RecyclerView.ViewHolder {
        public NotificationsMainViewHolder(View v) {
            super(v);
        }
    }
}
