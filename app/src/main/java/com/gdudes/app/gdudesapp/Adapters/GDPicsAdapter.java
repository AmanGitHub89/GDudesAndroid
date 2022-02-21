package com.gdudes.app.gdudesapp.Adapters;

//Un-used till now

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.List;

public class GDPicsAdapter extends BaseAdapter {
    private List<GDPic> PicList;
    private Context mContext;

    public GDPicsAdapter(Context c) {
        this.mContext = c;
        this.PicList = new ArrayList<GDPic>();
    }

    @Override
    public int getCount() {
        return PicList.size();
    }

    @Override
    public Object getItem(int position) {
        return PicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void SetPicList(List<GDPic> GDPicList) {
        this.PicList = GDPicList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gdpic_layout, null);
        }
        ((ImageView) convertView.findViewById(R.id.GDPic)).setImageBitmap(ImageHelper.GetBitmapFromString(PicList.get(position).PicThumbnail));
        return convertView;
    }
}
