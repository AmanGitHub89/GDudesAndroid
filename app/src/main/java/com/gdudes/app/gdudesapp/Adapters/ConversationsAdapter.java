package com.gdudes.app.gdudesapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APICalls.MessageAPICalls;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDSentMessageStateButton;
import com.gdudes.app.gdudesapp.Database.GDConversationsDBHelper;
import com.gdudes.app.gdudesapp.Database.GDMessagesDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.ConversationLastMessage;
import com.gdudes.app.gdudesapp.GDTypes.Conversations;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDSmileyHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.OnGDItemClickListener;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.MainLayout.LayoutActivity;
import com.gdudes.app.gdudesapp.activities.MessageWindow;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationsViewHolder> {
    private Context mContext;
    private ArrayList<Conversations> ConversationList;
    private LayoutInflater mInflater;
    private Users mLoggedInUser;

    private Boolean IsSearchActive = false;
    private String SearchPhrase = "";
    private ArrayList<Conversations> FilteredConversationList = new ArrayList<>();

    public ConversationsAdapter(Context c, Users LoggedInUser) {
        mContext = c;
        mLoggedInUser = LoggedInUser;
        mInflater = LayoutInflater.from(c);
        ConversationList = new ArrayList<>();
    }

    public void setConversationList(ArrayList<Conversations> vConversationList, Boolean ConversationsAdded) {
        this.ConversationList = vConversationList;
        notifyDataSetChanged();
        RefreshData();
    }


    @Override
    public int getItemCount() {
        return GetActiveConversationList().size();
    }

    public ArrayList<Conversations> GetConversationList() {
        return ConversationList;
    }

    private ArrayList<Conversations> GetActiveConversationList() {
        return IsSearchActive ? FilteredConversationList : ConversationList;
    }

    public void SetSearchActive(Boolean IsActive) {
        IsSearchActive = IsActive;
        if (IsActive) {
            FilteredConversationList.clear();
        } else {
            SearchPhrase = "";
        }
        notifyDataSetChanged();
    }

    public void ApplyNameFilter(String seachPhrase) {
        SearchPhrase = seachPhrase;
        ConversationList.clear();
        for (Conversations conversation : ConversationList) {
            if (IsSearchActive && StringEncoderHelper.decodeURIComponent(conversation.UserName).toUpperCase().contains(SearchPhrase.toUpperCase())) {
                FilteredConversationList.add(conversation);
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public ConversationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.message_conversation, parent, false);
        ConversationsViewHolder viewHolder = new ConversationsViewHolder(view, (view12, position, IsUserImageClickced) -> {
            Conversations conversation = GetActiveConversationList().get(position);
            if (IsUserImageClickced) {
                if (!conversation.UserID.trim().equalsIgnoreCase(mLoggedInUser.UserID.trim())) {
                    Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                    intent.putExtra("ClickedUserID", conversation.UserID);
                    if (!StringHelper.IsNullOrEmpty(conversation.PicID)) {
                        intent.putExtra("ProfilePicID", conversation.PicID);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            } else {
                Intent intent = new Intent(mContext, MessageWindow.class);
                intent.putExtra("ConvWithUserID", conversation.UserID);
                intent.putExtra("ConvWithUserName", StringEncoderHelper.decodeURIComponent(conversation.UserName));
                intent.putExtra("ConvWithUser", conversation.ConversationWithUser);
                intent.putExtra("ConvWithUserPicID", conversation.PicID);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }, (view1, position, IsImageClicked) -> {
            Conversations conversation = GetActiveConversationList().get(position);
            if (!IsImageClicked) {
                final CharSequence[] options = {"Delete Chat", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(LayoutActivity.LayoutActivityInstance);
                builder.setTitle("Delete chat with " + StringEncoderHelper.decodeURIComponent(conversation.UserName) + " ?");
                builder.setItems(options, (dialog, item) -> {
                    if (item == 0) {
                        dialog.dismiss();
                        GDDialogHelper.ShowYesNoTypeDialog(LayoutActivity.LayoutActivityInstance, "Delete Chat",
                                "Are you sure you want to delete all chat history with " + StringEncoderHelper.decodeURIComponent(conversation.UserName),
                                GDDialogHelper.BUTTON_TEXT_DELETE, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT,
                                () -> {
                                    if (new GDConversationsDBHelper(mContext).DeleteConversation(mLoggedInUser.UserID,
                                            conversation.UserID, true)) {
                                        new MessageAPICalls(mContext).DeleteUserConversation(conversation.UserID);
                                        GetActiveConversationList().remove(position);
                                        ConversationList.remove(conversation);
                                        notifyDataSetChanged();
                                    } else {
                                        GDToastHelper.ShowToast(LayoutActivity.LayoutActivityInstance, "Error while Deleting Conversation. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                                    }
                                }, null);
                    } else {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ConversationsViewHolder holder, int position) {
        Conversations CurrentConversation = GetActiveConversationList().get(position);
        if (CurrentConversation != null) {
            if (CurrentConversation.image != null) {
                holder.UserImage.setImageBitmap(CurrentConversation.image);
            } else {
                holder.UserImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.defaultuserprofilepic));
            }
            holder.ConversationUserName.setText(StringEncoderHelper.decodeURIComponent(CurrentConversation.UserName));
            if (IsSearchActive && !SearchPhrase.equals("")) {
                StringHelper.HighLightSearchText(SearchPhrase, holder.ConversationUserName);
            }
            Users conversationWithUser = CurrentConversation.ConversationWithUser;
            if (conversationWithUser != null && conversationWithUser.OnlineStatus && !StringHelper.IsNullOrEmpty(conversationWithUser.UserID)) {
                holder.OnlineIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.OnlineIndicator.setVisibility(View.GONE);
            }
            GDMessage lastMessage = CurrentConversation.LastMessage;
            if (lastMessage != null) {
                if (lastMessage.MessageContainsPhoto()) {
                    holder.LastMessageAttachPic.setVisibility(View.VISIBLE);
                } else {
                    holder.LastMessageAttachPic.setVisibility(View.GONE);
                }
                if (!StringHelper.IsNullOrEmpty(lastMessage.Location)) {
                    holder.LastMessageMap.setVisibility(View.VISIBLE);
                } else {
                    holder.LastMessageMap.setVisibility(View.GONE);
                }
                if (lastMessage.SenderID.equalsIgnoreCase(mLoggedInUser.UserID)) {
                    holder.MessageStatus.setButtonState(lastMessage.MessageStatus);
                    holder.MessageStatus.setVisibility(View.VISIBLE);
                } else {
                    holder.MessageStatus.setVisibility(View.GONE);
                }
                if (lastMessage.DecodedMessageText == null) {
                    String TextToShow = StringEncoderHelper.doubleDecodeURIComponent(lastMessage.MessageText);
                    lastMessage.DecodedMessageText = GDSmileyHelper.ShowSmileysForText(mContext, TextToShow, mContext.getResources(), true);
                    CurrentConversation.LastMessage.DecodedMessageText = lastMessage.DecodedMessageText;
                }
                holder.LastMessageText.setText(lastMessage.DecodedMessageText);

                if (StringHelper.IsNullOrEmpty(lastMessage.SentDateTime)) {
                    holder.LastDateTime.setVisibility(View.GONE);
                } else {
                    holder.LastDateTime.setText(GDDateTimeHelper.GetTimeDateBeforeFromDate(GDDateTimeHelper.GetDateFromString(lastMessage.SentDateTime), true));
                    holder.LastDateTime.setVisibility(View.VISIBLE);
                }
                if (CurrentConversation.UnMessageCount <= 0) { // || CurrentConversation.SenderType.equals("C")
                    holder.UnreadCount.setVisibility(View.GONE);
                } else {
                    holder.UnreadCount.setText("\u00A0" + Integer.toString(CurrentConversation.UnMessageCount) + "\u00A0");
                    //holder.UnreadCount.setText(Integer.toString(UnreadCount));
                    holder.UnreadCount.setVisibility(View.VISIBLE);
                }
            } else {
                holder.LastMessageAttachPic.setVisibility(View.GONE);
                holder.LastMessageMap.setVisibility(View.GONE);
                holder.MessageStatus.setVisibility(View.GONE);
                holder.LastMessageText.setText("");
                holder.LastDateTime.setVisibility(View.GONE);
                holder.UnreadCount.setVisibility(View.GONE);
            }
        }
    }


    public void RefreshDataForVisibleConversations(int StartPosition, int EndPosition) {
        if (EndPosition < StartPosition || ConversationList.size() < (EndPosition + 1)) {
            return;
        }

        ArrayList<Conversations> RefreshDataForConversations = new ArrayList<>();
        for (int i = StartPosition; i <= EndPosition; i++) {
            if (!ConversationList.get(i).IsDataRefreshed || Conversations.DataRefreshedMoreThan3MinsAgo(ConversationList.get(i))) {
                RefreshDataForConversations.add(ConversationList.get(i));
            }
        }
        if (RefreshDataForConversations.size() > 0) {
            GetMiniViewsForUsers(RefreshDataForConversations);
        }
    }

    private void RefreshData() {
        ArrayList<Conversations> ConversationListToUpdateData = GetConversationListToUpdateData();
        if (ConversationListToUpdateData.size() > 0) {
            GetUnreadMessageCounts(ConversationListToUpdateData);
            GetLastMessageForConversations(ConversationListToUpdateData);
        }

        GetUserPicsForConversations(ConversationList, true);
    }

    private Boolean ConvsationNeedsPicRefresh(Conversations conversation, Users user) {
        if (StringHelper.IsNullOrEmpty(user.PicID)) {
            return false;
        }
        if (StringHelper.IsNullOrEmpty(conversation.PicID) || !conversation.PicID.equals(user.PicID)) {
            return true;
        }
        return false;
    }

    private void GetLastMessageForConversations(ArrayList<Conversations> ConversationListToUpdateData) {
        ArrayList<String> userIDs = Conversations.GetUserIDList(ConversationListToUpdateData);
        if (userIDs.size() == 0) {
            return;
        }
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            GDMessagesDBHelper gdMessagesDBHelper = new GDMessagesDBHelper(mContext);
            ArrayList<ConversationLastMessage> messages = new ArrayList<>();
            for (String userID : userIDs) {
                GDMessage message = gdMessagesDBHelper.GetLastMessageForConversation(mLoggedInUser.UserID, userID);
                messages.add(new ConversationLastMessage(userID, message));
            }
            if (messages.size() != 0) {
                gdBackgroundTaskFinished.OnBackgroundTaskFinished(messages);
            }
        }, data -> {
            ArrayList<ConversationLastMessage> messages = (ArrayList<ConversationLastMessage>) data;
            for (ConversationLastMessage message : messages) {
                int index = ConversationList.indexOf(new Conversations(message.ConverseWithUserID));
                if (index != -1) {
                    ConversationList.get(index).LastMessage = message.LastMessage;
                }
            }
            notifyDataSetChanged();
        });
    }

    private void GetUnreadMessageCounts(ArrayList<Conversations> ConversationListToUpdateData) {
        ArrayList<String> userIDs = Conversations.GetUserIDList(ConversationListToUpdateData);
        if (userIDs.size() == 0) {
            return;
        }
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            GDMessagesDBHelper messagesDBHelper = new GDMessagesDBHelper(mContext);
            ArrayList<Conversations> conversations = new ArrayList<>();
            for (String userID : userIDs) {
                Conversations conversation = new Conversations(userID);
                conversation.UnMessageCount = messagesDBHelper.GetConversationUnreadInboundMessageCount(mLoggedInUser.UserID, userID);
                conversations.add(conversation);
            }
            gdBackgroundTaskFinished.OnBackgroundTaskFinished(conversations);
        }, data -> {
            ArrayList<Conversations> conversations = (ArrayList<Conversations>) data;
            if (conversations.size() == 0) {
                return;
            }
            for (Conversations conversation : conversations) {
                int index = ConversationList.indexOf(conversation);
                if (index != -1) {
                    ConversationList.get(index).UnMessageCount = conversation.UnMessageCount;
                    ConversationList.get(index).GetCountFromLocalDB = false;
                }
            }
            notifyDataSetChanged();
        });
    }

    private ArrayList<Conversations> GetConversationListToUpdateData() {
        ArrayList<Conversations> ConversationListToUpdateData = new ArrayList<>();
        for (int i = 0; i < ConversationList.size(); i++) {
            if (ConversationList.get(i).GetCountFromLocalDB || ConversationList.get(i).LastMessage == null) {
                ConversationListToUpdateData.add(ConversationList.get(i));
            }
        }
        return ConversationListToUpdateData;
    }

    private void GetMiniViewsForUsers(final ArrayList<Conversations> RefreshDataForConversations) {
        ArrayList<String> userIDs = Conversations.GetUserIDList(RefreshDataForConversations);
        if (userIDs.size() == 0) {
            return;
        }
        new HomeAPICalls(mContext).GetMiniProfilesForUserIDList(userIDs, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                ArrayList<Users> users = (ArrayList<Users>) result;
                if (users.size() == 0) {
                    return;
                }
                ArrayList<Conversations> conversations = new ArrayList<Conversations>();
                for (int i = 0; i < users.size(); i++) {
                    Users user = users.get(i);
                    int index = ConversationList.indexOf(new Conversations(user.UserID));
                    if (index >= 0) {
                        Conversations conversation = ConversationList.get(index);
                        conversation.UserName = user.GetDecodedUserName();
                        if (ConvsationNeedsPicRefresh(conversation, user)) {
                            conversation.image = null;
                        }
                        conversation.PicID = user.PicID;
                        conversation.ConversationWithUser = user;
                        conversations.add(conversation);
                    }
                }
                Conversations.SetDataRefreshed(conversations);
                notifyDataSetChanged();
                GetUserPicsForConversations(conversations, false);
            }

            @Override
            public void OnError(String result, Object extraData) {
            }

            @Override
            public void OnNoNetwork(Object extraData) {
            }
        });
    }

    private void GetUserPicsForConversations(final ArrayList<Conversations> conversations, final Boolean OnlyFromLocalDB) {
        if (conversations.size() == 0) {
            return;
        }
        ArrayList<String> picIDList = Conversations.GetPicIDListForNullImages(conversations);
        if (picIDList.size() == 0) {
            return;
        }
        ImageAPIHelper.GetPicsForPicIDList(mContext, picIDList, OnlyFromLocalDB, pics -> {
            for (int i = 0; i < ConversationList.size(); i++) {
                for (int j = 0; j < pics.size(); j++) {
                    String conversationPicID = ConversationList.get(i).PicID;
                    if (!StringHelper.IsNullOrEmpty(conversationPicID) && conversationPicID.equalsIgnoreCase(pics.get(j).PicID)) {
                        ConversationList.get(i).image = pics.get(j).image;
                        break;
                    }
                }
            }
            notifyDataSetChanged();
        });
    }

    static class ConversationsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        RoundedImageView UserImage;
        TextView ConversationUserName;
        GDSentMessageStateButton MessageStatus;
        ImageView LastMessageAttachPic;
        ImageView LastMessageMap;
        TextView LastMessageText;
        TextView LastDateTime;
        TextView UnreadCount;
        ImageView OnlineIndicator;
        OnGDItemClickListener ItemClickListener;
        OnGDItemClickListener ItemLongClickListener;

        public ConversationsViewHolder(View itemView, OnGDItemClickListener itemClickListener, OnGDItemClickListener itemLongClickListener) {
            super(itemView);
            ItemClickListener = itemClickListener;
            ItemLongClickListener = itemLongClickListener;
            UserImage = (RoundedImageView) itemView.findViewById(R.id.ConversationUserImage);
            ConversationUserName = (TextView) itemView.findViewById(R.id.ConversationUserName);
            MessageStatus = (GDSentMessageStateButton) itemView.findViewById(R.id.MessageStatus);
            LastMessageAttachPic = (ImageView) itemView.findViewById(R.id.LastMessageAttachPic);
            LastMessageMap = (ImageView) itemView.findViewById(R.id.LastMessageMap);
            LastMessageText = (TextView) itemView.findViewById(R.id.LastMessageText);
            LastDateTime = (TextView) itemView.findViewById(R.id.LastDateTime);
            UnreadCount = (TextView) itemView.findViewById(R.id.UnreadCount);
            OnlineIndicator = (ImageView) itemView.findViewById(R.id.OnlineIndicator);

            UserImage.setTag(true);
            UserImage.setOnClickListener(this);
            itemView.setTag(false);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ItemClickListener != null) {
                ItemClickListener.onItemClick(v, getLayoutPosition(), (Boolean) v.getTag());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (ItemLongClickListener != null) {
                ItemLongClickListener.onItemClick(v, getLayoutPosition(), (Boolean) v.getTag());
                return true;
            }
            return false;
        }
    }

}
