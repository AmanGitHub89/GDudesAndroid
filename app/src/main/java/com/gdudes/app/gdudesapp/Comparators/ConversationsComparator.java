package com.gdudes.app.gdudesapp.Comparators;

import com.gdudes.app.gdudesapp.GDTypes.Conversations;

import java.util.Comparator;

public class ConversationsComparator implements Comparator<Conversations> {
    @Override
    public int compare(Conversations lhs, Conversations rhs) {
        return rhs.LastMessageLocalDT.compareTo(lhs.LastMessageLocalDT);
    }
}
