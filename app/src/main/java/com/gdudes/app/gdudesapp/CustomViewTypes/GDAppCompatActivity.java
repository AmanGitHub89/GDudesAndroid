package com.gdudes.app.gdudesapp.CustomViewTypes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.gdudes.app.gdudesapp.APICaller.ConnectionManager;

public class GDAppCompatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectionManager.RemoveConnectionsForContext(GDAppCompatActivity.this);
    }
}
