package com.gdudes.app.gdudesapp.activities.Settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gdudes.app.gdudesapp.APICaller.APICalls.HomeAPICalls;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.Comparators.UsersNameComparator;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDGridView;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDUnitHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.gdudes.app.gdudesapp.activities.Profile.NewProfileViewActivity;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnblockUsersActivity extends GDCustomToolbarAppCompatActivity {

    Boolean mUserListIsRefreshing = false;
    int mUserListLastPageCalled = 0;
    Boolean AllResultsLoaded = false;

    Users LoggedInUser;
    Context mContext;
    SelectableUserListGridAdapter madapter;
    GDGridView gridView = null;
    SwipeRefreshLayout swipeUserList;
    LayoutInflater mLayoutInflater;
    FlowLayout SelectedTags;
    EditText SearchUser;
    ImageView CancelSearch;
    CountDownTimer SearchUserTimer;
    String SearchPhrase = "";
    private RelativeLayout ContentLoadedContainer = null;
    private RelativeLayout ContentLoadingContainer = null;
    private TextView ContentLoadingText = null;
    private Menu mMenu;

    public UnblockUsersActivity() {
        super("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_invite);

        setToolbarText("Unblock guys");
        mContext = UnblockUsersActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);
        ShowTitleWithoutActions = true;

        gridView = (GDGridView) findViewById(R.id.gvGrid);
        SearchUser = (EditText) findViewById(R.id.SearchUser);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gridView.setNestedScrollingEnabled(true);
        }
        ContentLoadedContainer = (RelativeLayout) findViewById(R.id.ContentLoadedContainer);
        ContentLoadingContainer = (RelativeLayout) findViewById(R.id.ContentLoadingContainer);
        ContentLoadingText = (TextView) findViewById(R.id.ContentLoadingText);
        swipeUserList = (SwipeRefreshLayout) findViewById(R.id.swipeUserList);
        swipeUserList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mUserListLastPageCalled = 0;
                RefreshUserList(mUserListLastPageCalled + 1);
            }
        });
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (AllResultsLoaded) {
                    return;
                }
                if (madapter != null && !mUserListIsRefreshing) {
                    int TotalCount = madapter.getCount();
                    if (TotalCount < 30) {
                        return;
                    }
                    if ((firstVisibleItem + visibleItemCount) >= TotalCount && TotalCount != 0) {
                        RefreshUserList(mUserListLastPageCalled + 1);
                    }
                }
            }
        });
        SelectedTags = (FlowLayout) findViewById(R.id.SelectedTags);
        CancelSearch = (ImageView) findViewById(R.id.CancelSearch);

        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        float scaleFactor = size.x / 3;
        int imageWidth = (int) (scaleFactor / getResources().getDisplayMetrics().density);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;

        madapter = new SelectableUserListGridAdapter(mContext, imageWidth, imageWidth, logicalDensity, new SelectableUserSelected() {
            @Override
            public void UserSelected(int position) {
                if (position == -1) {
                    return;
                }
                final Users CurrentUser = madapter.GetSelectedUsers().get(position);
                if (mLayoutInflater == null) {
                    mLayoutInflater = LayoutInflater.from(mContext);
                }
                final View SelectedTagView = mLayoutInflater.inflate(R.layout.selected_tag, null);
                TextView TagName = (TextView) SelectedTagView.findViewById(R.id.TagName);
                ImageView RemoveTag = (ImageView) SelectedTagView.findViewById(R.id.RemoveTag);
                TagName.setText(CurrentUser.GetDecodedUserName());
                RemoveTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        madapter.UnSelectUser(CurrentUser);
                        SelectedTags.removeView(SelectedTagView);
                    }
                });
                TagName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                        intent.putExtra("ClickedUserID", CurrentUser.UserID);
                        if (CurrentUser.PicID != null && !CurrentUser.PicID.trim().equals("")) {
                            intent.putExtra("ProfilePicID", CurrentUser.PicID);
                        }
                        mContext.startActivity(intent);
                    }
                });
                SelectedTags.addView(SelectedTagView, SelectedTags.getChildCount());
            }
        });
        gridView.setAdapter(madapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(mContext, NewProfileViewActivity.class);
                intent.putExtra("ClickedUserID", madapter.getItem(position).UserID);
                if (madapter.getItem(position).PicID != null && !madapter.getItem(position).PicID.trim().equals("")) {
                    intent.putExtra("ProfilePicID", madapter.getItem(position).PicID);
                }
                mContext.startActivity(intent);
            }
        });
        SearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (SearchUserTimer != null) {
                    SearchUserTimer.cancel();
                }
                SearchUserTimer = new CountDownTimer(800, 400) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        if (SearchUser.getText().toString().length() >= 3 || (SearchUser.getText().toString().equals("") && !SearchPhrase.equals(""))) {
                            SearchPhrase = SearchUser.getText().toString();
                            RefreshUserList(1);
                        }
                    }
                }.start();
            }
        });
        CancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SearchUser.getText().toString().equals("")) {
                    return;
                }
                SearchUser.setText("");
            }
        });

        AllResultsLoaded = false;
        mUserListLastPageCalled = 0;
        swipeUserList.setRefreshing(true);
        RefreshUserList(mUserListLastPageCalled + 1);
        postCreate();
        //hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void RefreshUserList(final int pageNumber) {
        mUserListLastPageCalled = pageNumber;
        if (pageNumber == 1) {
            AllResultsLoaded = false;
            if (madapter.getCount() == 0) {
                ContentLoadingText.setText("Loading..");
            }
        }
        mUserListIsRefreshing = true;
        swipeUserList.setRefreshing(true);

        new HomeAPICalls(mContext).GetBlockedUserList(pageNumber, SearchPhrase, null, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                ArrayList<Users> users = (ArrayList<Users>)result;
                if (pageNumber == 1) {
                    madapter.ReplaceUserList(users, true);
                } else {
                    madapter.AppendUserList(users, true);
                }
                if (users.size() > 0) {
                    GetPicsForUsers(users);
                }
                OnGetBlockListAPIComplete();
            }
            @Override
            public void OnError(String result, Object extraData) {
                if (result != null && result.equals("0")) {
                    if (pageNumber == 1) {
                        madapter.ReplaceUserList(new ArrayList<>(), true);
                    }
                    AllResultsLoaded = true;
                    OnGetBlockListAPIComplete();
                    return;
                }
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                OnGetBlockListAPIComplete();
            }
            @Override
            public void OnNoNetwork(Object extraData) {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                OnGetBlockListAPIComplete();
            }
        });
    }

    private void OnGetBlockListAPIComplete() {
        mUserListIsRefreshing = false;
        swipeUserList.setRefreshing(false);
        if (madapter.getCount() == 0) {
            ContentLoadingContainer.setVisibility(View.VISIBLE);
            ContentLoadedContainer.setVisibility(View.GONE);
            ContentLoadingText.setText("No guys to show.");
        } else {
            ContentLoadingContainer.setVisibility(View.GONE);
            ContentLoadedContainer.setVisibility(View.VISIBLE);
        }
    }

    private void GetPicsForUsers(final ArrayList<Users> users) {
        ArrayList<String> picIDList = Users.GetPicIDListFromUsers(users);
        ImageAPIHelper.GetPicsForPicIDList(mContext, picIDList, false, pics -> {
            Users.SetPicsToUsers(pics, users);
            madapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_unblock_users, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (madapter.GetSelectedUsers().size() > 0) {
                ShowMessageToCancelSending();
                return true;
            }
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.action_UnblockUsers) {
            if (madapter.GetSelectedUsers().size() == 0) {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Select guys to un-block", TopSnackBar.LENGTH_SHORT, true).show();
                return true;
            }

            ArrayList<String> userIDList = new ArrayList<>();
            for (int i = 0; i < madapter.GetSelectedUsers().size(); i++) {
                userIDList.add(madapter.GetSelectedUsers().get(i).UserID);
            }
            APIProgress apiProgress= new APIProgress(mContext, "Un-blocking..", false);
            new HomeAPICalls(mContext).BlockUnBlockUsers(userIDList, apiProgress.progressDialog,
                    new APICallerResultCallback() {
                        @Override
                        public void OnComplete(Object result, Object extraData) {
                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Users un-blocked", TopSnackBar.LENGTH_SHORT, false).show();
                            madapter.ClearSelectedListForSent();
                            SelectedTags.removeAllViews();
                        }
                        @Override
                        public void OnError(String result, Object extraData) {
                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                        }
                        @Override
                        public void OnNoNetwork(Object extraData) {
                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!SearchPhrase.equals("")) {
            SearchPhrase = "";
            SearchUser.setText("");
            RefreshUserList(1);
        } else if (madapter.GetSelectedUsers().size() > 0) {
            ShowMessageToCancelSending();
        } else {
            super.onBackPressed();
        }
    }

    private void ShowMessageToCancelSending() {
        String Title = "Cancel un-blocking";
        String Message = "You have selected guys to unblock. Are you sure you want to cancel?";
        GDDialogHelper.ShowYesNoTypeDialog(mContext, Title, Message,
                GDDialogHelper.BUTTON_TEXT_YES, GDDialogHelper.BUTTON_TEXT_NO, GDDialogHelper.ALERT, new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        finish();
                    }
                }, null);
    }

    class SelectableUserListGridAdapter extends BaseAdapter {
        private Context mContext;
        private int imageWidth = 106;
        private int imageHeight = 106;
        private float density = 1;
        private ArrayList<Users> Userlist = new ArrayList<>();
        private ArrayList<Users> UserSelected = new ArrayList<>();
        private SelectableUserSelected UserSelectedListener;
        private Boolean IsImperial = false;
        private Drawable DefaultPicDrawable;

        public SelectableUserListGridAdapter(Context c, int width, int height, float dispDensity,
                                             SelectableUserSelected vUserSelectedListener) {
            mContext = c;
            imageWidth = width;
            imageHeight = height;
            density = dispDensity;
            UserSelectedListener = vUserSelectedListener;
            IsImperial = PersistantPreferencesHelper.GetAppSettings().UnitSystem.equals("I");
            DefaultPicDrawable = ContextCompat.getDrawable(mContext, R.drawable.defaultuserprofilepic);
        }

        @Override
        public int getCount() {
            return Userlist.size();
        }

        @Override
        public Users getItem(int position) {
            return Userlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public ArrayList<Users> GetSelectedUsers() {
            return UserSelected;
        }

        public void AppendUserList(List<Users> usersList, Boolean notifyChange) {
            for (int i = 0; i < usersList.size(); i++) {
                if (!UserListContainsItem(usersList.get(i)) && !UserSelected.contains(usersList.get(i))) {
                    this.Userlist.add(usersList.get(i));
                }
            }
            //update the adapter to reflect the new set of movies
            if (notifyChange) {
                NotifyChange();
            }
        }

        public void ReplaceUserList(List<Users> usersList, Boolean notifyChange) {
            this.Userlist.clear();
            for (int i = 0; i < usersList.size(); i++) {
                if (!UserListContainsItem(usersList.get(i)) && !UserSelected.contains(usersList.get(i))) {
                    this.Userlist.add(usersList.get(i));
                }
            }
            if (notifyChange) {
                NotifyChange();
            }
        }

        private Boolean UserListContainsItem(Users user) {
            for (int i = 0; i < Userlist.size(); i++) {
                if (Userlist.get(i).UserID.equalsIgnoreCase(user.UserID)) {
                    return true;
                }
            }
            return false;
        }

        public void NotifyChange() {
            Collections.sort(Userlist, new UsersNameComparator());
            notifyDataSetChanged();
        }

        public void UnSelectUser(Users user) {
            int index = UserSelected.indexOf(user);
            UserSelected.remove(index);
            Userlist.add(user);
            NotifyChange();
        }

        public void ClearSelectedListForSent() {
            UserSelected.clear();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View grid;
            LayoutInflater inflater = null;
            Users CurrentUser = Userlist.get(position);
            if (UserSelected.contains(CurrentUser.UserID)) {
                return null;
            }

            if (convertView == null) {
                inflater = LayoutInflater.from(mContext);
                grid = inflater.inflate(R.layout.usertemplate_selectable_layout, null);

                UserListViewHolder userListViewHolder = new UserListViewHolder();
                userListViewHolder.tvUserame = (TextView) grid.findViewById(R.id.usertemplateUserName);
                userListViewHolder.tvAge = (TextView) grid.findViewById(R.id.usertemplateUserAge);
                userListViewHolder.tvDistance = (TextView) grid.findViewById(R.id.usertemplateDistance);
                userListViewHolder.imageView = (ImageView) grid.findViewById(R.id.usertemplateProfilePic);
                userListViewHolder.SelectUser = (ImageView) grid.findViewById(R.id.SelectUser);
                userListViewHolder.imageView.getLayoutParams().width = (int) (imageWidth * density);
                userListViewHolder.imageView.getLayoutParams().height = (int) (imageHeight * density);
                grid.setTag(userListViewHolder);
            } else {
                grid = convertView;
            }

            UserListViewHolder holder = (UserListViewHolder) grid.getTag();
            holder.tvUserame.setText(CurrentUser.GetDecodedUserName());
            holder.tvAge.setText(Integer.toString(CurrentUser.Age));
            holder.tvDistance.setText(IsImperial ? GDUnitHelper.Distance_MTI(CurrentUser.Distance) : GDUnitHelper.FormatKM(CurrentUser.Distance));
            if (CurrentUser.image != null) {
                holder.imageView.setImageBitmap(CurrentUser.image);
            } else {
                holder.imageView.setImageDrawable(DefaultPicDrawable);
            }

            Boolean isMyProfile = Userlist.get(position).UserID.equalsIgnoreCase(LoggedInUser.UserID);
            //Apply privacy hidings
            if (Userlist.get(position).ShowAgeInSearchTo.equals("N") && !isMyProfile) {
                holder.tvAge.setVisibility(View.GONE);
            } else {
                holder.tvAge.setVisibility(View.VISIBLE);
            }
            //Distance
            Boolean showHisDistance = !Userlist.get(position).ShowDistanceInSearchTo.equals("N");
            Boolean showMyDistance = !Userlist.get(position).ShowMyDistanceInSearchTo.equals("N");
            if ((showHisDistance && showMyDistance) || isMyProfile) {
                holder.tvDistance.setVisibility(View.VISIBLE);
            } else {
                holder.tvDistance.setVisibility(View.GONE);
            }

            holder.SelectUser.setTag(CurrentUser.UserID);
            holder.SelectUser.setOnClickListener(v -> {
                Users user = Userlist.get(position);
                Userlist.remove(position);
                if (!UserSelected.contains(user)) {
                    UserSelected.add(user);
                }
                if (UserSelectedListener != null) {
                    UserSelectedListener.UserSelected(UserSelected.size() - 1);
                }
                NotifyChange();
            });

            return grid;
        }
    }

    static class UserListViewHolder {
        TextView tvUserame = null;
        TextView tvAge = null;
        TextView tvDistance = null;
        ImageView imageView = null;
        ImageView SelectUser = null;
    }

    interface SelectableUserSelected {
        void UserSelected(int position);
    }
}
