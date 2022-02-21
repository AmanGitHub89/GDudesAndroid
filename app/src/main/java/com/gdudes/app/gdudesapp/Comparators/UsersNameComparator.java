package com.gdudes.app.gdudesapp.Comparators;

import com.gdudes.app.gdudesapp.GDTypes.Users;

import java.util.Comparator;

public class UsersNameComparator implements Comparator<Users> {
    @Override
    public int compare(Users lhs, Users rhs) {
        return lhs.UserName.compareTo(rhs.UserName);
    }
}
