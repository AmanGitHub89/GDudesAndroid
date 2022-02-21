package com.gdudes.app.gdudesapp.Interfaces;

import com.gdudes.app.gdudesapp.GDTypes.ConversationLastMessage;

import java.util.ArrayList;

public interface LastConversationMessageAvailableListener {
    void OnLastConversationMessageAvailable(ArrayList<ConversationLastMessage> messages);
}
