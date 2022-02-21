package com.gdudes.app.gdudesapp.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDUnitHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.List;

public class UserListGridAdapter extends BaseAdapter {
    private Context mContext;
    private Users LoggedInUser;
    private int imageWidth = 106;
    private int imageHeight = 106;
    private float density = 1;
    public ArrayList<Users> Userlist = new ArrayList<>();
    private Drawable DefaultPicDrawable;

    public UserListGridAdapter(Context context, int width, int height, float dispDensity, Users vLoggedInUser) {
        mContext = context;
        imageWidth = width;
        imageHeight = height;
        density = dispDensity;
        LoggedInUser = vLoggedInUser;
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

    public int GetUserPosition(Users user) {
        int position = -1;
        if (Userlist.contains(user)) {
            position = Userlist.indexOf(user);
        }
        return position;
    }

    public void AppendUserList(List<Users> usersList, Boolean notifyChange, Boolean bMoveSelfToTopForDistance) {
        for (int i = 0; i < usersList.size(); i++) {
            if (!UserListContainsItem(usersList.get(i))) {
                this.Userlist.add(usersList.get(i));
            }
        }
        if (bMoveSelfToTopForDistance) {
            Users.MoveSelfToTopForDistance(this.Userlist, LoggedInUser);
        }

        //update the adapter to reflect the new set of movies
        if (notifyChange) {
            notifyDataSetChanged();
        }
    }

    public void ReplaceUserList(List<Users> usersList, Boolean notifyChange, Boolean bMoveSelfToTopForDistance) {
        this.Userlist.clear();
        for (int i = 0; i < usersList.size(); i++) {
            if (!UserListContainsItem(usersList.get(i))) {
                this.Userlist.add(usersList.get(i));
            }
        }
        if (bMoveSelfToTopForDistance) {
            Users.MoveSelfToTopForDistance(this.Userlist, LoggedInUser);
        }
        if (notifyChange) {
            notifyDataSetChanged();
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater;
        Boolean IsImperial = PersistantPreferencesHelper.GetAppSettings().UnitSystem.equals("I");

        if (convertView == null) {
            inflater = LayoutInflater.from(mContext);
            grid = inflater.inflate(R.layout.usertemplate_layout, null);

            UserListViewHolder userListViewHolder = new UserListViewHolder();
            userListViewHolder.tvUserame = grid.findViewById(R.id.usertemplateUserName);
            userListViewHolder.tvAge = grid.findViewById(R.id.usertemplateUserAge);
            userListViewHolder.tvDistance = grid.findViewById(R.id.usertemplateDistance);
            userListViewHolder.imageView = grid.findViewById(R.id.usertemplateProfilePic);
            userListViewHolder.imageView.getLayoutParams().width = (int) (imageWidth * density);
            userListViewHolder.imageView.getLayoutParams().height = (int) (imageHeight * density);
            userListViewHolder.OnlineIndicator = grid.findViewById(R.id.OnlineIndicator);

            grid.setTag(userListViewHolder);
        } else {
            grid = convertView;
        }

        UserListViewHolder holder = (UserListViewHolder) grid.getTag();
        holder.tvUserame.setText(Userlist.get(position).GetDecodedUserName());
        holder.tvAge.setText(Integer.toString(Userlist.get(position).Age));
        holder.tvDistance.setText(IsImperial ? GDUnitHelper.Distance_MTI(Userlist.get(position).Distance) : GDUnitHelper.FormatKM(Userlist.get(position).Distance));

        if (Userlist.get(position).image != null) {
            holder.imageView.setImageBitmap(Userlist.get(position).image);
        } else {
            holder.imageView.setImageDrawable(DefaultPicDrawable);
        }

        if (Userlist.get(position).OnlineStatus) {
            holder.OnlineIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.OnlineIndicator.setVisibility(View.GONE);
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
        return grid;
    }

    private static class UserListViewHolder {
        TextView tvUserame = null;
        TextView tvAge = null;
        TextView tvDistance = null;
        ImageView imageView = null;
        ImageView OnlineIndicator = null;
    }
}