package com.gdudes.app.gdudesapp.Interfaces;

import android.util.Pair;

import java.util.ArrayList;

public interface ConversationsUnreadMessageCountAvailableListener {
    void OnConversationsUnreadMessageCountAvailable(ArrayList<Pair<String, String>> ConvWithUserIDAndUnreadMessageCountList);
}
