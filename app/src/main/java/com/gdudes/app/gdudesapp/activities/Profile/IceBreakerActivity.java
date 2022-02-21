package com.gdudes.app.gdudesapp.activities.Profile;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.R;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class IceBreakerActivity extends GDAppCompatActivity implements View.OnClickListener {

    Context mContext;
    Users LoggedInUser;
    String ClickedUserID = "";

    GridView IceBreakers;
    IceBreakerAdapter mIceBreakerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ice_breaker);
        mContext = IceBreakerActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        ClickedUserID = getIntent().getExtras().getString("ClickedUserID", "");

        IceBreakers = (GridView) findViewById(R.id.IceBreakers);
        mIceBreakerAdapter = new IceBreakerAdapter(mContext);
        IceBreakers.setAdapter(mIceBreakerAdapter);

        IceBreakers.setOnItemClickListener((parent, view, position, id) -> {
            List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, ClickedUserID));
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_RequestingUserID, LoggedInUser.UserID));
            pAPICallParameters.add(new APICallParameter(APICallParameter.param_IceBreakerCode, mIceBreakerAdapter.getItem(position)));
            APICallInfo apiCallInfo = new APICallInfo("Home", "NewIceBreaker", pAPICallParameters, "GET", null, null, false,
                    new APIProgress(mContext, "Sending Ice-breaker", true), APICallInfo.APITimeouts.MEDIUM);
            GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                        if (successResult.SuccessResult == 1) {
                            GDToastHelper.ShowToast(mContext, "Ice breaker sent", GDToastHelper.INFO, GDToastHelper.SHORT);
                        } else {
                            GDToastHelper.ShowErrorToastForSuccessResult(mContext, successResult);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GDLogHelper.LogException(e);
                        GDToastHelper.ShowGenericErrorToast(mContext);
                    } finally {
                        finish();
                    }
                }
            }, () -> {
                GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                finish();
            });
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        Double dWidth = size.x * 0.8;
        Double dHeight = size.y * 0.6;
        getWindow().setLayout(dWidth.intValue(), dHeight.intValue());
    }

    @Override
    public void onClick(View v) {
        GDToastHelper.ShowToast(mContext, v.getTag().toString(), GDToastHelper.INFO, GDToastHelper.SHORT);

    }

    class IceBreakerAdapter extends BaseAdapter {
        private Context mAdapterContext;
        private ArrayList<String> IceBreakerCodes;
        LayoutInflater mInflater;

        public IceBreakerAdapter(Context c) {
            mAdapterContext = c;
            mInflater = LayoutInflater.from(mAdapterContext);
            IceBreakerCodes = new ArrayList<>();
            AddIceBreakerCodes();
        }

        @Override
        public int getCount() {
            return IceBreakerCodes.size();
        }

        @Override
        public String getItem(int position) {
            return IceBreakerCodes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void AddIceBreakerCodes() {
            IceBreakerCodes.add("HI1");
            IceBreakerCodes.add("H01");
            IceBreakerCodes.add("ILU");
            IceBreakerCodes.add("LGL");
            IceBreakerCodes.add("NA1");
            IceBreakerCodes.add("NB1");
            IceBreakerCodes.add("NDS");
            IceBreakerCodes.add("NH1");
            IceBreakerCodes.add("NP1");
            IceBreakerCodes.add("NO1");
            IceBreakerCodes.add("TH1");
            IceBreakerCodes.add("THY");
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.icebreaker_item, null);
            ImageView IceBreakerImage = (ImageView) convertView.findViewById(R.id.IceBrekerImage);
            TextView IceBreakerText = (TextView) convertView.findViewById(R.id.IceBreakerText);
            convertView.setTag(IceBreakerCodes.get(position));

            Pair<Integer, String> IceBreakerInfo = GetIceBreakerInfo(position);
            IceBreakerImage.setImageResource(IceBreakerInfo.first);
            IceBreakerText.setText(GDGenericHelper.GetIceBreakMessageFromCode(IceBreakerInfo.second));
            return convertView;
        }

        private Pair<Integer, String> GetIceBreakerInfo(int position) {
            Pair<Integer, String> IceBreakerInfo = null;
            switch (position) {
                case 0:
                    IceBreakerInfo = new Pair<>(R.drawable.hi1, "HI1");
                    break;
                case 1:
                    IceBreakerInfo = new Pair<>(R.drawable.hot1, "HO1");
                    break;
                case 2:
                    IceBreakerInfo = new Pair<>(R.drawable.iloveyou1, "ILU");
                    break;
                case 3:
                    IceBreakerInfo = new Pair<>(R.drawable.letsgetlaid1, "LGL");
                    break;
                case 4:
                    IceBreakerInfo = new Pair<>(R.drawable.niceass1, "NA1");
                    break;
                case 5:
                    IceBreakerInfo = new Pair<>(R.drawable.nicebody1, "NB1");
                    break;
                case 6:
                    IceBreakerInfo = new Pair<>(R.drawable.nicedressingsense1, "NDS");
                    break;
                case 7:
                    IceBreakerInfo = new Pair<>(R.drawable.nicehair1, "NH1");
                    break;
                case 8:
                    IceBreakerInfo = new Pair<>(R.drawable.niceprofile1, "NP1");
                    break;
                case 9:
                    IceBreakerInfo = new Pair<>(R.drawable.notinterested1, "NO1");
                    break;
                case 10:
                    IceBreakerInfo = new Pair<>(R.drawable.thanks1, "TH1");
                    break;
                case 11:
                    IceBreakerInfo = new Pair<>(R.drawable.thankyou1, "THY");
                    break;
            }
            return IceBreakerInfo;
        }
    }
}
