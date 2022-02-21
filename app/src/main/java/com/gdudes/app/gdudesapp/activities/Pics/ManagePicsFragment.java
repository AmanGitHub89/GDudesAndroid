package com.gdudes.app.gdudesapp.activities.Pics;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.Adapters.ManagePicsItemAdapter;
import com.gdudes.app.gdudesapp.Comparators.PicCategorizedComparator;
import com.gdudes.app.gdudesapp.CustomViewTypes.NewManagePicsScollViews.DragListView;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.ManagePicsFragmentItemSelectedListener;
import com.gdudes.app.gdudesapp.Interfaces.ManagePicsListChangedListener;
import com.gdudes.app.gdudesapp.Interfaces.NewManagePicsItemClickListener;
import com.gdudes.app.gdudesapp.R;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagePicsFragment extends Fragment {

    RelativeLayout ContentLoadedContainer;
    RelativeLayout ContentLoadingContainer;
    TextView ContentLoadingText;
    DragListView ManagePicsDragList;
    Users LoggedInUser;
    Context mContext;
    ManagePicsFragmentItemSelectedListener managePicsFragmentItemSelectedListener;
    ManagePicsListChangedListener managePicsListChangedListener;
    Boolean IsPublicPicsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View FragmentLayout = inflater.inflate(R.layout.manage_pics_fragment, container, false);

        IsPublicPicsList = getArguments().getBoolean("IsPublic");
        mContext = getActivity();
        LoggedInUser = SessionManager.GetLoggedInUser(getActivity());

        ManagePicsDragList = (DragListView) FragmentLayout.findViewById(R.id.ManagePicsDragList);
        ContentLoadedContainer = (RelativeLayout) FragmentLayout.findViewById(R.id.ContentLoadedContainer);
        ContentLoadingContainer = (RelativeLayout) FragmentLayout.findViewById(R.id.ContentLoadingContainer);
        ContentLoadingText = (TextView) FragmentLayout.findViewById(R.id.ContentLoadingText);
        ContentLoadingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetPictures();
            }
        });

        GetPictures();
        SetDragListListeners();
        return FragmentLayout;
    }

    private void GetPictures() {
        ContentLoadingText.setText(IsPublicPicsList ? "Loading public photos.." : "Loading private photos..");
        ImageAPIHelper.GetUserPictures(mContext, IsPublicPicsList, true, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                ContentLoadingContainer.setVisibility(View.GONE);
                ContentLoadedContainer.setVisibility(View.VISIBLE);
                ArrayList<GDPic> pics = (ArrayList<GDPic>) result;
                if (pics.size() == 0) {
                    ShowLoadingErrorText(IsPublicPicsList ? "No public photos found" : "No private photos found");
                    return;
                }
                ShowPics(pics);
            }

            @Override
            public void OnError(String result, Object extraData) {
                ShowLoadingErrorText("An error occurred while loading " + (IsPublicPicsList ? "public" : "private") + " photos.\nTouch to reload.");

            }

            @Override
            public void OnNoNetwork(Object extraData) {
                TopSnackBar.MakeSnackBar(getActivity().findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                ShowLoadingErrorText("An error occurred while loading " + (IsPublicPicsList ? "public" : "private") + " photos.\nTouch to reload.");
            }
        }, pics -> {
            ((ManagePicsItemAdapter)ManagePicsDragList.getAdapter()).UpdateImages(pics);
        });
    }

    private void ShowLoadingErrorText(String Message) {
        ContentLoadingContainer.setVisibility(View.VISIBLE);
        ContentLoadedContainer.setVisibility(View.GONE);
        ContentLoadingText.setText(Message);
    }

    private void ShowPics(ArrayList<GDPic> PicsList) {
        ManagePicsDragList.setLayoutManager(new GridLayoutManager(mContext, 3, LinearLayoutManager.VERTICAL, false));
        ManagePicsItemAdapter listAdapter = new ManagePicsItemAdapter(PicsList, R.layout.new_manage_pic_item, R.id.item_layout, true,
                IsPublicPicsList, mContext, ManagePicsActivity.ImageWidth, new NewManagePicsItemClickListener() {
            @Override
            public void OnItemClicked(int position, String PicID) {
                Boolean IsAnyDragItemSelected = ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).IsAnyDragItemSelected();
                if (managePicsFragmentItemSelectedListener != null) {
                    managePicsFragmentItemSelectedListener.OnItemSelected(ManagePicsDragList.getAdapter().getItemList().size(), IsAnyDragItemSelected);
                }
            }
        });
        ManagePicsDragList.setAdapter(listAdapter, true);
        ManagePicsDragList.setCustomDragItem(null);
        if (managePicsListChangedListener != null) {
            managePicsListChangedListener.OnListChanged(PicsList.size());
        }
    }

    private void SetDragListListeners() {
        ManagePicsDragList.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
            }

            @Override
            public void onItemDragEnded(final int fromPosition, final int toPosition) {
                if (fromPosition != toPosition) {
                    if (!((GDPic) ManagePicsDragList.getAdapter().getItemList().get(toPosition)).IsCategorized) {
                        ShowReOrderFailMessage("Photo not yet categorized.\nCannot change order.", false, toPosition, fromPosition);
                    } else {
                        ReOrderPhoto(fromPosition, toPosition);
                    }
                }
            }
        });
    }

    private void ReOrderPhoto(final int fromPosition, final int toPosition) {
        try {
            PicOrdersList picOrdersList = GetPicOrdersList(GetAllItems());
            APICallInfo apiCallInfo = new APICallInfo("Home", "ChangeUserPicOrder", null, "POST", picOrdersList, null, false,
                    new APIProgress(mContext, "Changing photo order..", true), APICallInfo.APITimeouts.SHORT);
            GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                @Override
                public void onAPIComplete(String result, Object ExtraData) {
                    try {
                        if (result == null || result.equals("") || result.equals("-1")) {
                            return;
                        }
                        ArrayList<GDPic> PicsList = new GsonBuilder().create().fromJson(result, new TypeToken<ArrayList<GDPic>>() {
                        }.getType());

                        List<String> NonCachedPicIDs = new ArrayList<>();
                        String PicSrc = "";
                        final GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(mContext);
                        for (int i = 0; i < PicsList.size(); i++) {
                            PicSrc = gdImageDBHelper.GetImageStringByPicID(PicsList.get(i).PicID, false);
                            if (!PicSrc.equals("")) {
                                PicsList.get(i).PicThumbnail = PicSrc;
                            } else {
                                NonCachedPicIDs.add(PicsList.get(i).PicID);
                            }
                        }
                        Collections.sort(PicsList, new PicCategorizedComparator());
                        ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).SetItemListForOrderChanged(PicsList);
                        if (managePicsListChangedListener != null) {
                            managePicsListChangedListener.OnListChanged(ManagePicsDragList.getAdapter().getItemList().size());
                        }
                    } catch (Exception e) {
                        ShowReOrderFailMessage("Error while changing order.\nPlease try again", true, toPosition, fromPosition);
                        GDLogHelper.LogException(e);
                    }
                }
            }, new APINoNetwork() {
                @Override
                public void onAPINoNetwork() {
                    ShowReOrderFailMessage("No network connection detected.\nError while changing order.", true, toPosition, fromPosition);
                }
            });
        } catch (Exception ex) {
            ShowReOrderFailMessage("Error while changing order.\nPlease try again", true, toPosition, fromPosition);
            GDLogHelper.LogException(ex);
        }
    }

    private void ShowReOrderFailMessage(String Message, Boolean IsError, int fromPosition, int toPosition) {
        TopSnackBar.MakeSnackBar(getActivity().findViewById(R.id.BodyBelowAppBar), Message, TopSnackBar.LENGTH_SHORT, true).show();
        ManagePicsDragList.getAdapter().changeItemPosition(fromPosition, toPosition);
    }

    private PicOrdersList GetPicOrdersList(List<GDPic> GDPics) {
        PicOrdersList picOrdersList = new PicOrdersList(LoggedInUser.UserID, IsPublicPicsList);
        for (int i = 0; i < GDPics.size(); i++) {
            picOrdersList.PicOrderList.add(new GDPicOrder(GDPics.get(i).PicID, i + 1));
        }
        return picOrdersList;
    }

    public void SetItemSelectedListener(ManagePicsFragmentItemSelectedListener vManagePicsFragmentItemSelectedListener) {
        managePicsFragmentItemSelectedListener = vManagePicsFragmentItemSelectedListener;
    }

    public void SetListChangedListener(ManagePicsListChangedListener vManagePicsListChangedListener) {
        managePicsListChangedListener = vManagePicsListChangedListener;
    }

    public Boolean IsAnyItemSelected() {
        return ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null &&
                ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).IsAnyDragItemSelected();
    }

    public void DeselectAll() {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).DeselectAll();
        }
    }

    public void Reload() {
        if (ContentLoadingContainer != null) {
            ContentLoadingContainer.setVisibility(View.VISIBLE);
            ContentLoadedContainer.setVisibility(View.GONE);
        }
        GetPictures();
    }

    public ArrayList<GDPic> GetSelectedItems() {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            return ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).GetSelectedPics();
        }
        return new ArrayList<>();
    }

    public ArrayList<GDPic> GetAllItems() {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            return ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).GetAllItems();
        }
        return new ArrayList<>();
    }

    public void DeleteList(ArrayList<GDPic> PicList) {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).DeleteList(PicList);
        }
    }

    public void AddList(ArrayList<GDPic> PicList) {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).AddItemList(PicList);
        } else {
            ShowPics(PicList);
        }
    }

    public void FragmentSelected() {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).NotifyChange();
            if (((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).getItemList().size() > 0) {
                ContentLoadingContainer.setVisibility(View.GONE);
                ContentLoadedContainer.setVisibility(View.VISIBLE);
            } else {
                ShowLoadingErrorText(IsPublicPicsList ? "No public photos found" : "No private photos found");
            }
        } else {
            ShowLoadingErrorText(IsPublicPicsList ? "No public photos found" : "No private photos found");
        }
    }

    public ArrayList<GDPic> GetAllCategorizedItems() {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            return ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).GetAllCategorizedItems();
        }
        return new ArrayList<>();
    }

    public void DeselectNotAllowedPublicPics() {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).DeselectNotAllowedPublicPics();
        }
    }

    public void UpdCaption(GDPic gdPic) {
        if (ManagePicsDragList != null && ManagePicsDragList.getAdapter() != null) {
            ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).UpdCaption(gdPic);
            ((ManagePicsItemAdapter) ManagePicsDragList.getAdapter()).DeselectAll();
        }
    }

    class PicOrdersList {
        public String UserID;
        public Boolean IsPublicList;
        public ArrayList<GDPicOrder> PicOrderList;

        public PicOrdersList(String vUserID, Boolean vIsPublicList) {
            UserID = vUserID;
            IsPublicList = vIsPublicList;
            PicOrderList = new ArrayList<>();
        }
    }

    class GDPicOrder {
        public String PicID;
        public int PicOrder;

        public GDPicOrder(String vPicID, int vPicOrder) {
            PicID = vPicID;
            PicOrder = vPicOrder;
        }
    }
}
