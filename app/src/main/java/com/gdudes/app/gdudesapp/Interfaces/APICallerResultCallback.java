package com.gdudes.app.gdudesapp.Interfaces;

public interface APICallerResultCallback {
    void OnComplete(Object result, Object extraData);

    void OnError(String result, Object extraData);

    void OnNoNetwork(Object extraData);
}
