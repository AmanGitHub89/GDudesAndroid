package com.gdudes.app.gdudesapp.GDServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class GDBootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(new Intent(context, GDMessageService.class));
        }
    }
}
