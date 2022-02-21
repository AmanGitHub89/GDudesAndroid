package com.gdudes.app.gdudesapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Interfaces.OnManagePicSelected;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.List;

public class ManagePicsAdapter extends BaseAdapter {
    private final int CATEGORIZED = 0;
    private final int UNCATEGORIZED = 1;
    private List<OnManagePicSelected> listeners = new ArrayList<OnManagePicSelected>();
    private int mSelectedItemCount = 0;
    private Context mContext;
    private List<GDPic> mSelectedItems;
    public List<GDPic> mPiclist;
    private String ClubID = "";

    public ManagePicsAdapter(Context c, String vClubID) {
        mContext = c;
        mPiclist = new ArrayList<>();
        mSelectedItems = new ArrayList<>();
        ClubID = vClubID;
    }

    @Override
    public int getCount() {
        return mPiclist.size();
    }

    @Override
    public GDPic getItem(int position) {
        return mPiclist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return mPiclist.get(position).IsCategorized ? CATEGORIZED : UNCATEGORIZED;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public List<GDPic> GetSelectedItems() {
        return mSelectedItems;
    }

    public List<GDPic> GetAllItems() {
        List<GDPic> CategorizedPics = new ArrayList<>();
        for (int i = 0; i < mPiclist.size(); i++) {
            if (mPiclist.get(i).IsCategorized) {
                CategorizedPics.add(mPiclist.get(i));
            }
        }
        return CategorizedPics;
    }

    public void DeleteItem(int position) {
        mPiclist.remove(position);
        notifyDataSetChanged();
    }

    public void DeleteList(List<GDPic> PicList) {
        mSelectedItemCount = 0;
        mSelectedItems.clear();
        mPiclist.removeAll(PicList);
        notifyDataSetChanged();
    }

    public void SetPictureFolderList(List<GDPic> Piclist) {
        this.mPiclist.clear();
        this.mPiclist.addAll(Piclist);
        notifyDataSetChanged();
    }

    public void ResetSelectedCount() {
        mSelectedItemCount = 0;
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public void addListener(OnManagePicSelected listener) {
        listeners.add(listener);
    }

    public void UpdateImagesSource(List<GDPic> PicList) {
        for (int i = 0; i < PicList.size(); i++) {
            for (int j = 0; j < mPiclist.size(); j++) {
                try {
                    if (PicList.get(i).PicID.equalsIgnoreCase(mPiclist.get(j).PicID)) {
                        mPiclist.get(j).PicThumbnail = PicList.get(i).PicThumbnail;
                        break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    GDLogHelper.LogException(ex);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void UpdateImageSource(String PicID, String PicSrc) {
        for (int i = 0; i < mPiclist.size(); i++) {
            if (mPiclist.get(i).PicID.equalsIgnoreCase(PicID)) {
                mPiclist.get(i).PicThumbnail = PicSrc;
            }
        }
    }

    public void RefreshForCachedImages() {
        notifyDataSetChanged();
    }

    public void UpdateItemForEditPic(GDPic EditedPic) {
        if (mPiclist.contains(EditedPic)) {
            int index = mPiclist.indexOf(EditedPic);
            mPiclist.get(index).VisibleInProfile = EditedPic.VisibleInProfile;
            mPiclist.get(index).VisibleOnlyToFriends = EditedPic.VisibleOnlyToFriends;
            mPiclist.get(index).Caption = EditedPic.Caption;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = null;
        int IsCategorized = getItemViewType(position);
        GDPic CurrentItem = mPiclist.get(position);

        if (convertView == null) {
            inflater = LayoutInflater.from(mContext);
            PicFolderListViewHolder picFolderListViewHolder = new PicFolderListViewHolder();
            if (IsCategorized == CATEGORIZED) {
                grid = inflater.inflate(R.layout.manage_pic_item, null);
                picFolderListViewHolder.PicCategory = (TextView) grid.findViewById(R.id.PicCategory);
            } else {
                grid = inflater.inflate(R.layout.manage_pic_uncategorized_item, null);
            }
            picFolderListViewHolder.GDPic = (ImageView) grid.findViewById(R.id.GDPic);
            picFolderListViewHolder.SelectPic = (CheckBox) grid.findViewById(R.id.SelectPic);
            grid.setTag(picFolderListViewHolder);
        } else {
            grid = convertView;
        }

        PicFolderListViewHolder holder = (PicFolderListViewHolder) grid.getTag();
        holder.SelectPic.setTag(position);
        if (mSelectedItems.contains(getItem(position))) {
            holder.SelectPic.setChecked(true);
        } else {
            holder.SelectPic.setChecked(false);
        }
        holder.SelectPic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CheckBox checkBox = (CheckBox) buttonView;
                int iPosition = Integer.parseInt(checkBox.getTag().toString());
                if (isChecked) {
                    if (!mSelectedItems.contains(getItem(iPosition))) {
                        mSelectedItemCount++;
                        mSelectedItems.add(getItem(iPosition));
                        for (OnManagePicSelected hl : listeners)
                            hl.onItemSelected(mSelectedItemCount, mSelectedItems);
                    }
                } else {
                    if (mSelectedItems.contains(getItem(iPosition))) {
                        mSelectedItemCount--;
                        mSelectedItems.remove(getItem(iPosition));
                        for (OnManagePicSelected hl : listeners)
                            hl.onItemSelected(mSelectedItemCount, mSelectedItems);
                    }
                }
            }
        });
        if (CurrentItem.PicThumbnail != null && !CurrentItem.PicThumbnail.equals("")) {
            holder.GDPic.setImageBitmap(ImageHelper.GetBitmapFromString(CurrentItem.PicThumbnail));
        } else {
            holder.GDPic.setImageResource(android.R.color.transparent);
        }
        if (IsCategorized == CATEGORIZED) {
            holder.GDPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (OnManagePicSelected hl : listeners)
                        hl.onPicClicked(position);
                }
            });
            holder.PicCategory.setText(CurrentItem.Category);
        }
        return grid;
    }

    private static class PicFolderListViewHolder {
        ImageView GDPic = null;
        CheckBox SelectPic = null;
        TextView PicCategory = null;
    }
}