package com.gdudes.app.gdudesapp.Comparators;

import com.gdudes.app.gdudesapp.GDTypes.GDPic;

import java.util.Comparator;

public class PicCategorizedComparator implements Comparator<GDPic> {
    @Override
    public int compare(GDPic lhs, GDPic rhs) {
        return rhs.IsCategorized.compareTo(lhs.IsCategorized);
    }
}
