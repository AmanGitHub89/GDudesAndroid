package com.gdudes.app.gdudesapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.CustomViewTypes.GDSentMessageStateButton;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDSmileyHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.SnackCallback;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.OnGDMessageClickListener;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDMapActivity;
import com.gdudes.app.gdudesapp.activities.Pics.GDPicViewerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends BaseAdapter {
    private static int DIRECTION_OUTGOING = 1;
    private static int DIRECTION_INCOMING = 0;
    private static int DIRECTION_MESSAGEDATE = 2;
    private ArrayList<GDMessage> mMessageList;
    private Context mContext;
    private Users LoggedInUser;
    private String ConvWithUserID;
    private String ConvWithUserName;
    private String ConvWithUserPicID;
    LayoutInflater mlayoutInflater;
    private OnGDMessageClickListener mOnGDMessageClickListener;
    private SnackCallback mSnackCallback;
    private int mSelectedItemCount = 0;
    private Boolean IsSearchTextActive = false;
    private String SearchText = "";
    private int SearchCurrentItemPosition = -1;
    private Bitmap AttachedPicPlaceholder;

    public void SetSearchText(String vSearchText) {
        SearchText = vSearchText;
        SearchCurrentItemPosition = -1;
        SetSearchContainsForAllText();
    }

    public void SetIsSearchTextActive(Boolean vIsSearchTextActive) {
        IsSearchTextActive = vIsSearchTextActive;
        if (!vIsSearchTextActive) {
            SearchText = "";
        }
    }

    public MessagesAdapter(Context c, Users vLoggedInUser, String vConvWithUserID, String vConvWithUserName, String vConvWithUserPicID,
                           OnGDMessageClickListener listener, SnackCallback snackCallback) {
        mContext = c;
        this.LoggedInUser = vLoggedInUser;
        ConvWithUserID = vConvWithUserID;
        ConvWithUserName = vConvWithUserName;
        ConvWithUserPicID = vConvWithUserPicID;
        mMessageList = new ArrayList<>();
        mOnGDMessageClickListener = listener;
        mSnackCallback = snackCallback;
        AttachedPicPlaceholder = ImageHelper.getBitmapForResource(mContext.getResources(), R.drawable.message_pic_placeholder_2);
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public GDMessage getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).Direction;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }


    public void SetMessageList(ArrayList<GDMessage> messageList) {
        this.mMessageList = messageList;
        GDMessage.AddDateDisplayForMessages(mMessageList);
        notifyDataSetChanged();
    }

    public void addMessage(GDMessage message) {
        if (IsSearchTextActive && !SearchText.equals("")) {
            if (message.MessageText.toUpperCase().contains(SearchText.toUpperCase())) {
                message.ContainsSearchedText = true;
            }
        }
        this.mMessageList.add(message);
        GDMessage.AddDateDisplayForMessages(mMessageList);
        notifyDataSetChanged();
    }

    public void SetSelectedItemCountZero() {
        mSelectedItemCount = 0;
        for (int i = 0; i < mMessageList.size(); i++) {
            mMessageList.get(i).IsSelected = false;
        }
    }

    public String GetFirstSelectedItemText() {
        for (int i = 0; i < mMessageList.size(); i++) {
            if (mMessageList.get(i).IsSelected) {
                return StringEncoderHelper.decodeURIComponent(mMessageList.get(i).MessageText);
            }
        }
        return "";
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int direction = getItemViewType(position);
        final GDMessage thisMessage = getItem(position);
        View MessageItem = null;
        if (convertView == null) {
            mlayoutInflater = LayoutInflater.from(mContext);
            MessageListViewHolder messageListViewHolder = new MessageListViewHolder();
            if (direction == DIRECTION_MESSAGEDATE) {
                MessageItem = mlayoutInflater.inflate(R.layout.message_date, null);
                messageListViewHolder.txtMessageChangeDate = MessageItem.findViewById(R.id.MessageChangeDate);
            } else {
                if (direction == DIRECTION_OUTGOING) {
                    MessageItem = mlayoutInflater.inflate(R.layout.message_right, null);
                    messageListViewHolder.MessageStatus = MessageItem.findViewById(R.id.MessageStatus);
                } else if (direction == DIRECTION_INCOMING) {
                    MessageItem = mlayoutInflater.inflate(R.layout.message_left, null);
                }
                messageListViewHolder.MapLocation = MessageItem.findViewById(R.id.MapLocation);
                messageListViewHolder.txtMessage = MessageItem.findViewById(R.id.txtMessage);
                messageListViewHolder.MessageSentDT = MessageItem.findViewById(R.id.MessageSentDT);
                messageListViewHolder.AttachedPhoto = MessageItem.findViewById(R.id.AttachedPhoto);
            }

            MessageItem.setTag(messageListViewHolder);
        } else {
            MessageItem = convertView;
        }
        FrameLayout frameLayout = (FrameLayout) MessageItem;
        if (thisMessage.IsSelected) {
            frameLayout.setForeground(mContext.getResources().getDrawable(R.drawable.message_selected));
        } else {
            frameLayout.setForeground(mContext.getResources().getDrawable(R.drawable.message_unselected));
        }
        MessageListViewHolder holder = (MessageListViewHolder) MessageItem.getTag();
        if (direction == DIRECTION_MESSAGEDATE) {
            holder.txtMessageChangeDate.setText(GDDateTimeHelper.GetMonthDateYearString(thisMessage.dSentDateTime, true));
            return MessageItem;
        }

        if (getItemViewType(position) == DIRECTION_OUTGOING) {
            String TextToShow = StringEncoderHelper.doubleDecodeURIComponent(thisMessage.MessageText) + StringEncoderHelper.decodeURIComponent("\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0");
            holder.txtMessage.setText(GDSmileyHelper.ShowSmileysForText(mContext, TextToShow, mContext.getResources(), true));
            if (IsSearchTextActive && thisMessage.ContainsSearchedText && !SearchText.equals("")) {
                StringHelper.HighLightSearchText(SearchText, holder.txtMessage);
            }
            holder.MessageStatus.setButtonState(thisMessage.MessageStatus);
            holder.MessageStatus.setVisibility(View.VISIBLE);
        } else if (getItemViewType(position) == DIRECTION_INCOMING) {
            String TextToShow = StringEncoderHelper.doubleDecodeURIComponent(thisMessage.MessageText) + StringEncoderHelper.decodeURIComponent("\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0");
            holder.txtMessage.setText(GDSmileyHelper.ShowSmileysForText(mContext, TextToShow, mContext.getResources(), true));
            if (IsSearchTextActive && thisMessage.ContainsSearchedText && !SearchText.equals("")) {
                StringHelper.HighLightSearchText(SearchText, holder.txtMessage);
            }
        }
        holder.MessageSentDT.setText(GDDateTimeHelper.GetTimeStringFromDate(thisMessage.dSentDateTime, true));
        if (thisMessage.Location != null && !thisMessage.Location.equals("")) {
            holder.MapLocation.setVisibility(View.VISIBLE);
            holder.MapLocation.setTag(thisMessage.Location);
        } else {
            holder.MapLocation.setVisibility(View.GONE);
            holder.MapLocation.setTag("");
        }
        final View finalMessageItem = MessageItem;
        holder.MapLocation.setOnClickListener(v -> {
            if (mSelectedItemCount > 0) {
                finalMessageItem.performClick();
                return;
            }
            if (v.getTag() != null) {
                String Location = v.getTag().toString();
                if (!Location.equals("")) {
                    Intent intent = new Intent(mContext, GDMapActivity.class);
                    intent.putExtra("Activity_Mode", GDMapActivity.VIEW_USER_LOCATION);
                    intent.putExtra("LocationOwnerUserID", direction == DIRECTION_INCOMING ? ConvWithUserID : "");
                    intent.putExtra("LocationOwnerName", direction == DIRECTION_INCOMING ? ConvWithUserName : "");
                    intent.putExtra("LocationOwnerPicID", direction == DIRECTION_INCOMING ? ConvWithUserPicID : "");
                    intent.putExtra("LocationLatLng", Location);
                    mContext.startActivity(intent);
                }
            }
        });

        MessageItem.setOnClickListener(v -> {
            if (mSelectedItemCount < 1 || mMessageList.get(position).Direction == 2) {
                return;
            }
            FrameLayout vFrameLayout = (FrameLayout) v;
            if (mMessageList.get(position).IsSelected) {
                mMessageList.get(position).IsSelected = false;
                mSelectedItemCount = mSelectedItemCount - 1;
                vFrameLayout.setForeground(mContext.getResources().getDrawable(R.drawable.message_unselected));
            } else {
                mMessageList.get(position).IsSelected = true;
                mSelectedItemCount = mSelectedItemCount + 1;
                vFrameLayout.setForeground(mContext.getResources().getDrawable(R.drawable.message_selected));
            }
            if (mOnGDMessageClickListener != null) {
                mOnGDMessageClickListener.onMessageClick(mSelectedItemCount, mMessageList.get(position).IsSelected);
            }
        });
        MessageItem.setOnLongClickListener(v -> {
            if (mSelectedItemCount > 0 || mMessageList.get(position).Direction == 2 || IsSearchTextActive) {
                return false;
            }
            mMessageList.get(position).IsSelected = true;
            mSelectedItemCount = mSelectedItemCount + 1;
            FrameLayout vFrameLayout = (FrameLayout) v;
            vFrameLayout.setForeground(mContext.getResources().getDrawable(R.drawable.message_selected));
            if (mOnGDMessageClickListener != null) {
                mOnGDMessageClickListener.onMessageLongClick(mSelectedItemCount);
            }
            return true;
        });


        if(thisMessage.MessageContainsPhoto()) {
            holder.AttachedPhoto.setVisibility(View.VISIBLE);
            if (thisMessage.image != null) {
                holder.AttachedPhoto.setImageBitmap(thisMessage.image);
            } else {
                holder.AttachedPhoto.setImageBitmap(AttachedPicPlaceholder);
            }
        } else {
            holder.AttachedPhoto.setVisibility(View.GONE);
        }
        holder.AttachedPhoto.setOnLongClickListener(v -> {
            if (mSelectedItemCount > 0 || mMessageList.get(position).Direction == 2 || IsSearchTextActive) {
                return false;
            }
            mMessageList.get(position).IsSelected = true;
            mSelectedItemCount = mSelectedItemCount + 1;
            FrameLayout vFrameLayout = (FrameLayout) finalMessageItem;
            vFrameLayout.setForeground(mContext.getResources().getDrawable(R.drawable.message_selected));
            if (mOnGDMessageClickListener != null) {
                mOnGDMessageClickListener.onMessageLongClick(mSelectedItemCount);
            }
            return true;
        });
        holder.AttachedPhoto.setOnClickListener(v -> {
            if (mSelectedItemCount > 0) {
                finalMessageItem.performClick();
                return;
            }
            if (thisMessage.HasDirectPic()) {
                try {
                    File imageFile = new File(thisMessage.AttachedFilePath);
                    if (imageFile.exists()) {
                        Intent intent = new Intent(mContext, GDPicViewerActivity.class);
                        List<GDPic> AttPicList = new ArrayList<GDPic>();
                        GDPic pic = new GDPic(GDGenericHelper.GetNewGUID(), LoggedInUser.UserID, false, "", "", false);
                        pic.DirectPicAttachedFilePath = thisMessage.AttachedFilePath;
                        AttPicList.add(pic);
                        intent.putParcelableArrayListExtra("GDPicList", (ArrayList) AttPicList);
                        intent.putExtra("SelectedPic", 0);
                        intent.putExtra("IsPhonePhoto", true);
                        mContext.startActivity(intent);
                    } else {
                        mSnackCallback.MakeSnack("Picture not found on device", TopSnackBar.LENGTH_SHORT, true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    GDLogHelper.LogException(ex);
                }
            } else {
                String attachedPicID = StringHelper.ToArrayList(thisMessage.AttachedPicIDs.split("\\s*,\\s*")).get(0);
                Intent intent = new Intent(mContext, GDPicViewerActivity.class);
                ArrayList<GDPic> AttPicList = new ArrayList<>();
                AttPicList.add(new GDPic(attachedPicID, thisMessage.SenderID, false, "", "", false));
                intent.putParcelableArrayListExtra("GDPicList", AttPicList);
                intent.putExtra("SelectedPic", 0);
                intent.putExtra("ClickedUserID", thisMessage.SenderID);
                mContext.startActivity(intent);
            }
        });
        return MessageItem;
    }

    public void SetSearchContainsForAllText() {
        for (int i = 0; i < mMessageList.size(); i++) {
            if (IsSearchTextActive && mMessageList.get(i).Direction != 2) {
                if (mMessageList.get(i).MessageText.toUpperCase().contains(SearchText.toUpperCase()) && !SearchText.equals("")) {
                    mMessageList.get(i).ContainsSearchedText = true;
                } else {
                    mMessageList.get(i).ContainsSearchedText = false;
                }
            }
        }
        notifyDataSetChanged();
    }


    public void SetPrevSearchPosition() {
        int position = -1;
        for (int i = SearchCurrentItemPosition - 1; i >= 0; i--) {
            if (mMessageList.get(i).ContainsSearchedText) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            SearchCurrentItemPosition = position;
        }
    }

    public void SetNextSearchPosition() {
        int position = -1;
        for (int i = SearchCurrentItemPosition + 1; i <= mMessageList.size() - 1; i++) {
            if (mMessageList.get(i).ContainsSearchedText) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            SearchCurrentItemPosition = position;
        }
    }

    public ArrayList<Integer> GetSearchMatchPositions() {
        ArrayList<Integer> SearchMatchPositions = new ArrayList<>();
        for (int i = 0; i < mMessageList.size(); i++) {
            if (mMessageList.get(i).ContainsSearchedText) {
                SearchMatchPositions.add(i);
            }
        }
        return SearchMatchPositions;
    }

    public void SetLastSearchPosition() {
        int position = -1;
        for (int i = mMessageList.size() - 1; i >= 0; i--) {
            if (mMessageList.get(i).ContainsSearchedText) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            SearchCurrentItemPosition = position;
        }
    }

    public int GetSearchCurrentItemPosition() {
        return SearchCurrentItemPosition;
    }

    private static class MessageListViewHolder {
        ImageView MapLocation = null;
        TextView txtMessage = null;
        GDSentMessageStateButton MessageStatus = null;
        TextView MessageSentDT = null;
        TextView txtMessageChangeDate = null;
        ImageView AttachedPhoto = null;
    }
}
