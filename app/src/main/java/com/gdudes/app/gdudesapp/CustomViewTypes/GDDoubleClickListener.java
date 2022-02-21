package com.gdudes.app.gdudesapp.CustomViewTypes;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;

public abstract class GDDoubleClickListener implements AdapterView.OnItemClickListener {

    private static final long DOUBLE_CLICK_TIME_DELTA = 600;//milliseconds
    static long lastClickTime = 0;
    static long NewClickTime = 0;
    static CountDownTimer mCountDownTimer;
    static Boolean IsDoubleClick = false;

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        NewClickTime = System.currentTimeMillis();
        if (NewClickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            IsDoubleClick = true;
            onDoubleClick(parent, view, position, id);
        } else {
            mCountDownTimer = new CountDownTimer(DOUBLE_CLICK_TIME_DELTA, 300) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    if (!IsDoubleClick) {
                        onSingleClick(parent, view, position, id);
                    }
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    IsDoubleClick = false;
                }
            }.start();
        }
        lastClickTime = NewClickTime;
    }

    public abstract void onSingleClick(AdapterView<?> parent, View view, int position, long id);

    public abstract void onDoubleClick(AdapterView<?> parent, View view, int position, long id);
}