package com.gdudes.app.gdudesapp.Comparators;

import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.GDNotification;

import java.util.Comparator;

public class GDMessageAndNotificationComparator implements Comparator<Object> {
    @Override
    public int compare(Object lhs, Object rhs) {
        String lhs_DateTime = "";
        String rhs_DateTime = "";
        if (lhs.getClass().equals(GDMessage.class)) {
            lhs_DateTime = ((GDMessage) lhs).SentDateTime;
        } else {
            lhs_DateTime = ((GDNotification) lhs).NotificationDateTime;
        }
        if (rhs.getClass().equals(GDMessage.class)) {
            rhs_DateTime = ((GDMessage) rhs).SentDateTime;
        } else {
            rhs_DateTime = ((GDNotification) rhs).NotificationDateTime;
        }
        return rhs_DateTime.compareTo(lhs_DateTime);
    }
}

