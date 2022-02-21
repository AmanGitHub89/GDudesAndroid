package com.gdudes.app.gdudesapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.CustomViewTypes.NewManagePicsScollViews.DragItemAdapter;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.Interfaces.NewManagePicsItemClickListener;
import com.gdudes.app.gdudesapp.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class ManagePicsItemAdapter extends DragItemAdapter<GDPic, ManagePicsItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private NewManagePicsItemClickListener itemClickedListener;
    private Boolean IsPublicList = false;
    private Context mContext;
    private int ImageWidth;

    public ManagePicsItemAdapter(List<GDPic> list, int layoutId, int grabHandleId, boolean dragOnLongPress, Boolean vIsPublicList,
                                 Context vContext, int vImageWidth, NewManagePicsItemClickListener vitemClickedListener) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).DragItemID = Long.valueOf(i);
            list.get(i).IsDragItemSelected = false;
        }
        mContext = vContext;
        ImageWidth = vImageWidth;
        setItemList(list);
        IsPublicList = vIsPublicList;
        itemClickedListener = vitemClickedListener;
    }

    public void AddItemList(ArrayList<GDPic> gdPicList) {
        for (int i = 0; i < gdPicList.size(); i++) {
            AddItem(gdPicList.get(i));
        }
    }

    public void AddItem(GDPic gdPic) {
        gdPic.DragItemID = Long.valueOf(mItemList.size());
        gdPic.IsDragItemSelected = false;
        addItem(mItemList.size(), gdPic);
        notifyDataSetChanged();
    }

    public void UpdateImages(ArrayList<GDPic> picList) {
        List<GDPic> myPicsList = getItemList();
        for (int i = 0; i < picList.size(); i++) {
            for (int j = 0; j < myPicsList.size(); j++) {
                if (picList.get(i).PicID.equalsIgnoreCase(myPicsList.get(j).PicID)) {
                    myPicsList.get(j).image = picList.get(i).image;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public int SelectedItemCount() {
        int Count = 0;
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).IsDragItemSelected) {
                Count++;
            }
        }
        return Count;
    }

    public void SetItemListForOrderChanged(ArrayList<GDPic> list) {
        Boolean IsSameOrder = true;
        if (mItemList.size() == list.size()) {
            for (int i = 0; i < list.size(); i++) {
                if (!mItemList.get(i).PicID.equalsIgnoreCase(list.get(i).PicID)) {
                    IsSameOrder = false;
                    break;
                }
            }
        } else {
            IsSameOrder = false;
        }
        if (!IsSameOrder) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).DragItemID = Long.valueOf(i);
                list.get(i).IsDragItemSelected = false;
            }
            setItemList(list);
        }
    }

    public void NotifyChange() {
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);

        holder.PicCategory.setText(mItemList.get(position).Category);
        if (mItemList.get(position).image != null) {
            holder.GDPic.setImageBitmap(mItemList.get(position).image);
        } else {
            holder.GDPic.setImageResource(R.drawable.placeholder_image);
        }
        if (mItemList.get(position).IsCategorized) {
            holder.NotCategorizedText.setVisibility(View.GONE);
        } else {
            holder.NotCategorizedText.setVisibility(View.VISIBLE);
        }
        if (IsPublicList && position == 0 && mItemList.get(position).IsCategorized) {
            holder.GDPic.setBorderWidth(12.0f);
        } else {
            holder.GDPic.setBorderWidth(0.0f);
        }

        if (mItemList.get(position).IsDragItemSelected) {
            holder.PicFrame.setForeground(mContext.getResources().getDrawable(R.drawable.manage_pics_selected));
            holder.PicSelectCheck.setVisibility(View.VISIBLE);
        } else {
            holder.PicFrame.setForeground(mContext.getResources().getDrawable(R.drawable.message_unselected));
            holder.PicSelectCheck.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemList.get(position).IsDragItemSelected) {
                    mItemList.get(position).IsDragItemSelected = false;
                    notifyDataSetChanged();
                } else {
                    mItemList.get(position).IsDragItemSelected = true;
                    notifyDataSetChanged();
                }
                if (itemClickedListener != null) {
                    itemClickedListener.OnItemClicked(position, mItemList.get(position).PicID);
                }
            }
        });
    }

    public Boolean IsAnyDragItemSelected() {
        if (mItemList == null) {
            return false;
        }
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).IsDragItemSelected) {
                return true;
            }
        }
        return false;
    }

    public void DeselectAll() {
        for (int i = 0; i < mItemList.size(); i++) {
            mItemList.get(i).IsDragItemSelected = false;
        }
        notifyDataSetChanged();
    }

    public void DeselectNotAllowedPublicPics() {
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).Category.trim().equalsIgnoreCase("Hardcore") ||
                    mItemList.get(i).Category.trim().equalsIgnoreCase("Offensive") ||
                    mItemList.get(i).Category.trim().equalsIgnoreCase("Minor")) {
                mItemList.get(i).IsDragItemSelected = false;
            }
        }
        notifyDataSetChanged();
    }

    public void UpdCaption(GDPic gdPic) {
        int index = mItemList.indexOf(gdPic);
        if (index >= 0) {
            mItemList.get(index).Caption = gdPic.Caption;
        }
        notifyDataSetChanged();
    }

    public ArrayList<GDPic> GetSelectedPics() {
        ArrayList<GDPic> SelectedPics = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).IsDragItemSelected) {
                SelectedPics.add(mItemList.get(i));
            }
        }
        return SelectedPics;
    }

    public ArrayList<GDPic> GetAllItems() {
        ArrayList<GDPic> SelectedPics = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            SelectedPics.add(mItemList.get(i));
        }
        return SelectedPics;
    }

    public ArrayList<GDPic> GetAllCategorizedItems() {
        ArrayList<GDPic> SelectedPics = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).IsCategorized) {
                SelectedPics.add(mItemList.get(i));
            }
        }
        return SelectedPics;
    }

    public void DeleteList(ArrayList<GDPic> PicList) {
        mItemList.removeAll(PicList);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).DragItemID;
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView PicCategory;
        public RoundedImageView GDPic;
        public TextView NotCategorizedText;
        public ImageView PicSelectCheck;
        public FrameLayout PicFrame;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            PicCategory = (TextView) itemView.findViewById(R.id.PicCategory);
            GDPic = (RoundedImageView) itemView.findViewById(R.id.GDPic);
            GDPic.getLayoutParams().width = (int) (ImageWidth);
            GDPic.getLayoutParams().height = (int) (ImageWidth);
            NotCategorizedText = (TextView) itemView.findViewById(R.id.NotCategorizedText);
            PicSelectCheck = (ImageView) itemView.findViewById(R.id.PicSelectCheck);
            PicFrame = (FrameLayout) itemView.findViewById(R.id.PicFrame);
        }

        @Override
        public boolean onItemLongClicked(View view) {
            return true;
        }
    }
}