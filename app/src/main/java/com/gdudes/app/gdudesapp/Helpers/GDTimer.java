package com.gdudes.app.gdudesapp.Helpers;

import android.os.Handler;

import com.gdudes.app.gdudesapp.Interfaces.GDTimerTaskRun;

import java.util.Timer;
import java.util.TimerTask;

public class GDTimer extends Timer {
    private TimerTask mTimerTask;
    private Handler mTimerHandler;
    private GDTimerTaskRun mGDTimerTaskRun;
    private long mDelay;
    private long mInterval;

    public GDTimer(long delay, long interval, Handler TimerHandler, GDTimerTaskRun GDTimerTaskRun) {
        mTimerHandler = TimerHandler;
        mGDTimerTaskRun = GDTimerTaskRun;
        mDelay = delay;
        mInterval = interval;
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mTimerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mGDTimerTaskRun != null) {
                            mGDTimerTaskRun.OnTimerElapsed();
                        }
                    }
                });
            }
        };
    }

    public void Start() {
        schedule(mTimerTask, mDelay, mInterval);
    }

    public void Stop() {
        try {
            if (this != null) {
                this.cancel();
                this.purge();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }
}
