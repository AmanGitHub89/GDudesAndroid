package com.gdudes.app.gdudesapp.Comparators;

import com.gdudes.app.gdudesapp.GDTypes.DirectPicDownloadingInfo;

import java.util.Comparator;

public class DirectPicDownloadingInfoComparator implements Comparator<DirectPicDownloadingInfo> {
    @Override
    public int compare(DirectPicDownloadingInfo lhs, DirectPicDownloadingInfo rhs) {
        if (rhs.FailureCount < lhs.FailureCount) {
            return 1;
        } else if (rhs.FailureCount > lhs.FailureCount) {
            return -1;
        } else {
            return 0;
        }
    }
}
