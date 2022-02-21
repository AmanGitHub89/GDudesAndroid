package com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper;

import android.os.Handler;
import android.os.Looper;

public class GDAsyncHelper {
    public static void DoTask(final GDAsyncDoInBackground gdAsyncDoInBackground, final GDAsyncTaskCallback gdAsyncTaskCallback) {
        Runnable task = () -> {
            gdAsyncDoInBackground.DoInBackground(data -> {
                if (gdAsyncTaskCallback != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        gdAsyncTaskCallback.OnTaskCompleted(data);
                    });
                }
            });
        };
        Thread thread = new Thread(task);
        thread.start();
    }
}
