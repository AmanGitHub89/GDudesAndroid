package com.gdudes.app.gdudesapp.Comparators;

import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;

import java.util.Comparator;

public class MessagesComparator implements Comparator<GDMessage> {
    @Override
    public int compare(GDMessage lhs, GDMessage rhs) {
        int result = GDDateTimeHelper.FormatToSqlDateStringWithPrecision(lhs.SentDateTime).compareTo(
                GDDateTimeHelper.FormatToSqlDateStringWithPrecision(rhs.SentDateTime));
        if (result == 0) {
            if (lhs.Direction == 2) {
                return -1;
            } else if (rhs.Direction == 2) {
                return 1;
            }
        }
        return result;
    }
}

