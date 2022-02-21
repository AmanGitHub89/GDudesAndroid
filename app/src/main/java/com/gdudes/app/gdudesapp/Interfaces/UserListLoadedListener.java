package com.gdudes.app.gdudesapp.Interfaces;

import com.gdudes.app.gdudesapp.GDTypes.Users;

import java.util.List;

public interface UserListLoadedListener {
    void onBoxOfficeMoviesLoaded(List<Users> UserList);
}
