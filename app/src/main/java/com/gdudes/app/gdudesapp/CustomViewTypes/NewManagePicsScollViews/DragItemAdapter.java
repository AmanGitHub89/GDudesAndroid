package com.gdudes.app.gdudesapp.CustomViewTypes.NewManagePicsScollViews;

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public abstract class DragItemAdapter<T, VH extends DragItemAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {

    interface DragStartCallback {
        boolean startDrag(View itemView, long itemId);

        boolean isDragging();
    }

    private DragStartCallback mDragStartCallback;
    private long mDragItemId = RecyclerView.NO_ID;
    private long mDropTargetId = RecyclerView.NO_ID;
    protected List<T> mItemList;

    public void setItemList(List<T> itemList) {
        mItemList = itemList;
        notifyDataSetChanged();
    }

    public List<T> getItemList() {
        return mItemList;
    }

    public Object removeItem(int pos) {
        if (mItemList != null && mItemList.size() > pos) {
            Object item = mItemList.remove(pos);
            notifyItemRemoved(pos);
            return item;
        }
        return null;
    }

    public void addItem(int pos, T item) {
        if (mItemList != null && mItemList.size() >= pos) {
            mItemList.add(pos, item);
            notifyItemInserted(pos);
        }
    }

    public void changeItemPosition(int fromPos, int toPos) {
        if (mItemList != null && mItemList.size() > fromPos && mItemList.size() > toPos) {
            T item = mItemList.remove(fromPos);
            mItemList.add(toPos, item);
            notifyItemMoved(fromPos, toPos);
        }
    }

    public void swapItems(int pos1, int pos2) {
        if (mItemList != null && mItemList.size() > pos1 && mItemList.size() > pos2) {
            Collections.swap(mItemList, pos1, pos2);
            notifyDataSetChanged();
        }
    }

    public int getPositionForItemId(long id) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (id == getItemId(i)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        long itemId = getItemId(position);
        holder.mItemId = itemId;
        holder.itemView.setVisibility(mDragItemId == itemId ? View.INVISIBLE : View.VISIBLE);
        holder.setDragStartCallback(mDragStartCallback);
    }

    @Override
    public void onViewRecycled(VH holder) {
        super.onViewRecycled(holder);
        holder.setDragStartCallback(null);
    }

    void setDragStartedListener(DragStartCallback dragStartedListener) {
        mDragStartCallback = dragStartedListener;
    }

    void setDragItemId(long dragItemId) {
        mDragItemId = dragItemId;
    }

    void setDropTargetId(long dropTargetId) {
        mDropTargetId = dropTargetId;
    }

    public long getDropTargetId() {
        return mDropTargetId;
    }

    public static abstract class ViewHolder extends RecyclerView.ViewHolder {
        public View mGrabView;
        public long mItemId;

        private DragStartCallback mDragStartCallback;

        public ViewHolder(final View itemView, int handleResId, boolean dragOnLongPress) {
            super(itemView);
            mGrabView = itemView.findViewById(handleResId);

            if (dragOnLongPress) {
                mGrabView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (mDragStartCallback == null) {
                            return false;
                        }

                        if (mDragStartCallback.startDrag(itemView, mItemId)) {
                            return true;
                        }
                        if (itemView == mGrabView) {
                            return onItemLongClicked(view);
                        }
                        return false;
                    }
                });
            } else {
                mGrabView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (mDragStartCallback == null) {
                            return false;
                        }

                        if (event.getAction() == MotionEvent.ACTION_DOWN && mDragStartCallback.startDrag(itemView, mItemId)) {
                            return true;
                        }
                        if (!mDragStartCallback.isDragging() && itemView == mGrabView) {
                            return onItemTouch(view, event);
                        }
                        return false;
                    }
                });
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClicked(view);
                }
            });

            if (itemView != mGrabView) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return onItemLongClicked(view);
                    }
                });
                itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        return onItemTouch(view, event);
                    }
                });
            }
        }

        public void setDragStartCallback(DragStartCallback dragStartedListener) {
            mDragStartCallback = dragStartedListener;
        }

        public void onItemClicked(View view) {
        }

        public boolean onItemLongClicked(View view) {
            return false;
        }

        public boolean onItemTouch(View view, MotionEvent event) {
            return false;
        }
    }
}

