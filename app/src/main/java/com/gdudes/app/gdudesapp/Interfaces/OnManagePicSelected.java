package com.gdudes.app.gdudesapp.Interfaces;

import com.gdudes.app.gdudesapp.GDTypes.GDPic;

import java.util.List;

public interface OnManagePicSelected {
    void onItemSelected(int selectedItemCount, List<GDPic> selectedItemPositions);

    void onPicClicked(int position);
}
