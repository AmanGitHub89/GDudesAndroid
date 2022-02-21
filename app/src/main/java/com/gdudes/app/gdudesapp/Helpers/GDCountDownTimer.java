package com.gdudes.app.gdudesapp.Helpers;

import android.os.CountDownTimer;

import com.gdudes.app.gdudesapp.Interfaces.GDCountDownTimerCallback;

public class GDCountDownTimer {
    public static void StartCountDown(long millisInFuture, final GDCountDownTimerCallback countDownTimerCallback) {
        long countDownInterval = millisInFuture / 2;
        new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                countDownTimerCallback.OnCountDownComplete();
            }
        }.start();
    }
}
