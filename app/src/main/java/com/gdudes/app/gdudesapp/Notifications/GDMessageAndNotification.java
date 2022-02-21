package com.gdudes.app.gdudesapp.Notifications;

import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.GDTypes.GDNotification;

class GDMessageAndNotification {
    public GDMessage message;
    public GDNotification notification;

    public GDMessageAndNotification(GDMessage message, GDNotification notification) {
        this.message = message;
        this.notification = notification;
    }
}
