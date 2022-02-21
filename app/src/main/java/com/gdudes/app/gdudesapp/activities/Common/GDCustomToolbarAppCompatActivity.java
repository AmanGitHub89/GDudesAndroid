package com.gdudes.app.gdudesapp.activities.Common;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.R;

public class GDCustomToolbarAppCompatActivity extends GDAppCompatActivity {
    private Boolean IsLongClickOptionsVisible = true;
    private Menu mMenu;
    private String mToolbarText;
    protected Toolbar mActivityToolbar;
    protected View mSelectedItem;
    protected int mSelectedItemPosition;
    final int sdk = android.os.Build.VERSION.SDK_INT;
    private int mSelectedItemBackgournd;
    private int mUnSelectedItemBackgournd;
    private Boolean SelectBackgroundToBeSet = false;
    protected Boolean HasActions = false;
    protected Boolean ShowTitleWithoutActions = false;

    public GDCustomToolbarAppCompatActivity(String ToolbarText) {
        mToolbarText = ToolbarText;
    }

    public GDCustomToolbarAppCompatActivity(String ToolbarText, int SelectedItemBackgournd, int UnSelectedItemBackgournd) {
        mToolbarText = ToolbarText;
        mSelectedItemBackgournd = SelectedItemBackgournd;
        mUnSelectedItemBackgournd = UnSelectedItemBackgournd;
        SelectBackgroundToBeSet = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void postCreate() {
        mActivityToolbar = (Toolbar) findViewById(R.id.ActivityToolbar);
        setSupportActionBar(mActivityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(null);
        if (HasActions) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            ShowActions(false);
        } else if (ShowTitleWithoutActions) {
            getSupportActionBar().setTitle(mToolbarText);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        } else {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    protected void setToolbarText(String ToolbarText) {
        mToolbarText = ToolbarText;
    }

    protected void changeToolbarText(String ToolbarText) {
        mToolbarText = ToolbarText;
        getSupportActionBar().setTitle(mToolbarText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        ShowActions(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Boolean vIsLongClickOptionsVisible = IsLongClickOptionsVisible;
        ShowActions(false);
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (vIsLongClickOptionsVisible && HasActions) {
                    ShowActions(false);
                    return false;
                } else {
                    finish();
                    return true;
                }
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onBackPressed() {
        if (IsLongClickOptionsVisible && HasActions) {
            ShowActions(false);
        } else {
            super.onBackPressed();
        }
    }

    protected void ShowActions(Boolean ShowActions) {
        if (!HasActions) {
            return;
        }
        if (ShowActions) {
            getSupportActionBar().setTitle("");
            if (mMenu != null) {
                for (int i = 0; i < mMenu.size(); i++)
                    mMenu.getItem(i).setVisible(true);
            }
            IsLongClickOptionsVisible = true;
            if (SelectBackgroundToBeSet && mSelectedItem != null) {
                SetSelectedItemBackground(mSelectedItemBackgournd);
            }
        } else {
            if (mMenu != null) {
                for (int i = 0; i < mMenu.size(); i++)
                    mMenu.getItem(i).setVisible(false);
            }
            IsLongClickOptionsVisible = false;
            if (SelectBackgroundToBeSet && mSelectedItem != null) {
                SetSelectedItemBackground(mUnSelectedItemBackgournd);
            }
            getSupportActionBar().setTitle(mToolbarText);
        }
    }

    protected Boolean AreItemsVisible() {
        return IsLongClickOptionsVisible;
    }

    protected void SetSelectedItem(View selectedItem, int SelectedItemPosition) {
        mSelectedItem = selectedItem;
        mSelectedItemPosition = SelectedItemPosition;
    }

    private void SetSelectedItemBackground(int Background) {
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mSelectedItem.setBackgroundDrawable(getResources().getDrawable(Background));
        } else {
            mSelectedItem.setBackground(getResources().getDrawable(Background));
        }
    }
}
