package com.gdudes.app.gdudesapp.Comparators;

import com.gdudes.app.gdudesapp.GDTypes.PicComment;

import java.util.Comparator;

public class CommentsComparator implements Comparator<PicComment> {
    @Override
    public int compare(PicComment lhs, PicComment rhs) {
        return rhs.LDDateTime.compareTo(lhs.LDDateTime);
    }
}
