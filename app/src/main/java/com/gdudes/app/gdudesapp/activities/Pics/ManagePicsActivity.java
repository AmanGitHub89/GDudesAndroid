package com.gdudes.app.gdudesapp.activities.Pics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.UserIDAndGUIDList;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.UserIDXIDAndGUIDList;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gdudesapp.GDFloatingActionButton.FloatingActionButton;
import gdudesapp.GDFloatingActionButton.FloatingActionsMenu;

public class ManagePicsActivity extends GDCustomToolbarAppCompatActivity {

    private ViewPager FragmentsViewPager;
    private ManagePicsFragmentAdapter mAdapter;
    private FloatingActionsMenu FABMenuAddPhoto;
    private FloatingActionButton FABAddPublicPhoto;
    private FloatingActionButton FABAddPrivatePhoto;
    public static int ImageWidth = 106;
    Context mContext;
    ManagePicsFragment PublicManagePicsFragment;
    ManagePicsFragment PrivateManagePicsFragment;
    Menu mMenu;
    Users LoggedInUser;

    public ManagePicsActivity() {
        super("Public Photos");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pics);
        FragmentsViewPager = findViewById(R.id.FragmentsViewPager);
        mContext = ManagePicsActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        FABMenuAddPhoto = findViewById(R.id.FABMenuAddPhoto);
        FABAddPublicPhoto = findViewById(R.id.FABAddPublicPhoto);
        FABAddPrivatePhoto = findViewById(R.id.FABAddPrivatePhoto);
        SetFABEvents();
        CalcImageWidth();

        SetUpFragments();

        HasActions = true;
        ShowTitleWithoutActions = true;
        postCreate();
    }

    private void SetFABEvents() {
        FABAddPublicPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(ManagePicsActivity.this, UploadPicsActivity.class);
            intent.putExtra("IsPublicUserPic", true);
            startActivityForResult(intent, 1);
            CollapseFAB();
        });
        FABAddPrivatePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(ManagePicsActivity.this, UploadPicsActivity.class);
            intent.putExtra("IsPublicUserPic", false);
            startActivityForResult(intent, 2);
            CollapseFAB();
        });
    }

    private void SetUpFragments() {

        mAdapter = new ManagePicsFragmentAdapter(getSupportFragmentManager());

        PublicManagePicsFragment = new ManagePicsFragment();
        PublicManagePicsFragment.SetItemSelectedListener((itemCount, AnyItemSelected) -> {
            CollapseFAB();
            SetMenuItemVisibility(true);
        });
        PublicManagePicsFragment.SetListChangedListener(itemCount -> SetMenuItemVisibility(true));
        Bundle PublicPicsBundle = new Bundle();
        PublicPicsBundle.putBoolean("IsPublic", true);
        PublicManagePicsFragment.setArguments(PublicPicsBundle);
        mAdapter.addFrag(PublicManagePicsFragment);

        PrivateManagePicsFragment = new ManagePicsFragment();
        PrivateManagePicsFragment.SetItemSelectedListener((itemCount, AnyItemSelected) -> {
            CollapseFAB();
            SetMenuItemVisibility(false);
        });
        PrivateManagePicsFragment.SetListChangedListener(itemCount -> SetMenuItemVisibility(false));
        Bundle PrivatePicsBundle = new Bundle();
        PrivatePicsBundle.putBoolean("IsPublic", false);
        PrivateManagePicsFragment.setArguments(PrivatePicsBundle);
        mAdapter.addFrag(PrivateManagePicsFragment);

        FragmentsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                CollapseFAB();
                if (position == 0) {
                    changeToolbarText("Public Photos");
                    SetMenuItemVisibility(true);
                    PublicManagePicsFragment.FragmentSelected();
                } else if (position == 1) {
                    changeToolbarText("Private Photos");
                    SetMenuItemVisibility(false);
                    PrivateManagePicsFragment.FragmentSelected();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        FragmentsViewPager.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_pics, menu);
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);
        menu.getItem(2).setVisible(false);
        menu.getItem(3).setVisible(false);
        menu.getItem(4).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        switch (id) {
            case android.R.id.home:
                if (CloseOnBackPressed()) {
                    return super.onOptionsItemSelected(item);
                } else {
                    return false;
                }
            case R.id.action_Edit:
                final List<GDPic> SelectedPicList = new ArrayList<>();
                SelectedPicList.addAll(FragmentsViewPager.getCurrentItem() == 0 ?
                        PublicManagePicsFragment.GetSelectedItems() : PrivateManagePicsFragment.GetSelectedItems());
                if (SelectedPicList.size() != 1) {
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Select a single photo to edit.", TopSnackBar.LENGTH_SHORT, true).show();
                    return false;
                }
                intent = new Intent(mContext, EditPicActivity.class);
                intent.putExtra("GDPic", SelectedPicList.get(0));
                startActivityForResult(intent, 3);
                break;
            case R.id.action_DeletePic:
                GDDialogHelper.ShowYesNoTypeDialog(mContext, "Delete photos?", "Are you sure you want to delete the selected photos?",
                        GDDialogHelper.BUTTON_TEXT_YES, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT,
                        new OnDialogButtonClick() {
                            @Override
                            public void dialogButtonClicked() {
                                final List<GDPic> DeletePicList = new ArrayList<>();
                                DeletePicList.addAll(FragmentsViewPager.getCurrentItem() == 0 ?
                                        PublicManagePicsFragment.GetSelectedItems() : PrivateManagePicsFragment.GetSelectedItems());
                                ArrayList<String> guidList = new ArrayList<>();
                                for (int i = 0; i < DeletePicList.size(); i++) {
                                    guidList.add(DeletePicList.get(i).PicID);
                                }
                                UserIDAndGUIDList userIDAndGUIDList = new UserIDAndGUIDList(LoggedInUser.UserID, guidList);
                                APICallInfo apiCallInfo = new APICallInfo("Home", "DeleteMultipleUserPics", null, "POST",
                                        userIDAndGUIDList, FragmentsViewPager.getCurrentItem() == 0, false,
                                        new APIProgress(mContext, "Deleting photos. Please wait..", true), APICallInfo.APITimeouts.MEDIUM);
                                GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                                    try {
                                        if (result == null || result.equals("") || result.equals("-1")) {
                                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                                            return;
                                        }
                                        JSONObject jsonObject = new JSONObject(result);
                                        String sSuccessResult = jsonObject.getString("SuccessResult");
                                        SuccessResult successResult = new GsonBuilder().create().fromJson(StringHelper.TrimFirstAndLastCharacter(sSuccessResult), SuccessResult.class);
                                        String sDeletedPics = jsonObject.getString("DeletedPics");
                                        List<GDPicID> DeletedPicIdList = new GsonBuilder().create().fromJson(sDeletedPics, new TypeToken<ArrayList<GDPicID>>() {
                                        }.getType());

                                        if (DeletedPicIdList.size() > 0) {
                                            List<GDPic> ToDeleteList = new ArrayList<>();
                                            GDPic TempGDPic = null;
                                            for (int i = 0; i < DeletedPicIdList.size(); i++) {
                                                TempGDPic = new GDPic();
                                                TempGDPic.PicID = DeletedPicIdList.get(i).PicID;
                                                ToDeleteList.add(TempGDPic);
                                            }

                                            ArrayList<GDPic> RemovePics = new ArrayList<GDPic>();
                                            for (int i = 0; i < DeletedPicIdList.size(); i++) {
                                                RemovePics.add(new GDPic(DeletedPicIdList.get(i).PicID));
                                            }
                                            Boolean isPublic = (Boolean) ExtraData;
                                            ImageAPIHelper.DeletePics(ToDeleteList, isPublic);
                                            if (isPublic) {
                                                PublicManagePicsFragment.DeleteList(RemovePics);
                                            } else {
                                                PrivateManagePicsFragment.DeleteList(RemovePics);
                                            }
                                        }
                                        if (successResult.SuccessResult == 1) {
                                            SetMenuItemVisibility((Boolean) ExtraData);
                                        } else {
                                            if (successResult.FailureMessage != null && !successResult.FailureMessage.trim().equals("")) {
                                                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), successResult.FailureMessage, TopSnackBar.LENGTH_LONG, true).show();
                                            } else {
                                                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                                            }
                                        }
                                    } catch (Exception e) {
                                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                                        GDLogHelper.LogException(e);
                                    }
                                }, () -> TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show());
                            }
                        }, null);
                break;
            case R.id.action_MakePrivate:
                MakePublicPrivate(true);
                break;
            case R.id.action_MakePublic:
                MakePublicPrivate(false);
                break;
            case R.id.action_OpenPhotos:
                intent = new Intent(mContext, GDPicViewerActivity.class);
                if (FragmentsViewPager.getCurrentItem() == 0) {
                    intent.putParcelableArrayListExtra("GDPicList", PublicManagePicsFragment.GetAllCategorizedItems());
                } else {
                    intent.putParcelableArrayListExtra("GDPicList", PrivateManagePicsFragment.GetAllCategorizedItems());
                }
                intent.putExtra("SelectedPic", 0);
                intent.putExtra("ClickedUserID", LoggedInUser.UserID);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void MakePublicPrivate(final Boolean MakePrivate) {
        final ArrayList<GDPic> ChangePrivacyPicList = new ArrayList<>();
        ChangePrivacyPicList.addAll(MakePrivate ? PublicManagePicsFragment.GetSelectedItems() :
                PrivateManagePicsFragment.GetSelectedItems());

        if (!MakePrivate && HasHardCoreOffensiveOrMinorSelected(ChangePrivacyPicList)) {
            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Only Non-Sexual or Soft-core photos can be made public.", TopSnackBar.LENGTH_LONG, true).show();
            PrivateManagePicsFragment.DeselectNotAllowedPublicPics();
            return;
        }
        if (ChangePrivacyPicList.size() == 0) {
            return;
        }
        UserIDXIDAndGUIDList userIDXIDAndGUIDList = new UserIDXIDAndGUIDList();
        userIDXIDAndGUIDList.UserID = LoggedInUser.UserID;
        userIDXIDAndGUIDList.XID = MakePrivate ? "1" : "0";
        userIDXIDAndGUIDList.GUIDList = new ArrayList<>();
        for (int i = 0; i < ChangePrivacyPicList.size(); i++) {
            userIDXIDAndGUIDList.GUIDList.add(ChangePrivacyPicList.get(i).PicID);
        }
        APICallInfo apiCallInfo = new APICallInfo("Home", "MakePhotosPrivatePublic", null, "POST",
                userIDXIDAndGUIDList, userIDXIDAndGUIDList.GUIDList, false,
                new APIProgress(mContext, "Making photos " + (MakePrivate ? "private" : "public") + ".\nPlease wait..", true),
                APICallInfo.APITimeouts.MEDIUM);
        GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                if (result == null || result.equals("") || result.equals("-1")) {
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                    return;
                }
                SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                if (successResult != null && successResult.SuccessResult == 1) {
                    ArrayList<GDPic> MovedPics = new ArrayList<GDPic>();
                    ArrayList<String> MovedPicIDs = (ArrayList<String>) ExtraData;
                    ArrayList<GDPic> AllPicsFromMoved = MakePrivate ? PublicManagePicsFragment.GetAllItems() :
                            PrivateManagePicsFragment.GetAllItems();
                    for (int i = 0; i < MovedPicIDs.size(); i++) {
                        for (int j = 0; j < AllPicsFromMoved.size(); j++) {
                            if (MovedPicIDs.get(i).equalsIgnoreCase(AllPicsFromMoved.get(j).PicID)) {
                                MovedPics.add(AllPicsFromMoved.get(j));
                                break;
                            }
                        }
                    }
                    if (MakePrivate) {
                        PublicManagePicsFragment.DeleteList(MovedPics);
                        PrivateManagePicsFragment.AddList(MovedPics);
                    } else {
                        PrivateManagePicsFragment.DeleteList(MovedPics);
                        PublicManagePicsFragment.AddList(MovedPics);
                    }
                } else if (successResult != null && (successResult.SuccessResult == 0
                        || successResult.SuccessResult == Integer.parseInt(getResources().getString(R.string.user_limit_error_code)))) {
                    if (successResult.FailureMessage != null && !successResult.FailureMessage.trim().equals("")) {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), successResult.FailureMessage, TopSnackBar.LENGTH_LONG, true).show();
                    }
                    PrivateManagePicsFragment.Reload();
                    PublicManagePicsFragment.Reload();
                } else {
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                    return;
                }
            } catch (Exception e) {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                GDLogHelper.LogException(e);
            }
        }, () -> TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show());
    }

    private Boolean HasHardCoreOffensiveOrMinorSelected(ArrayList<GDPic> SelectedPics) {
        for (int i = 0; i < SelectedPics.size(); i++) {
            if (SelectedPics.get(i).Category.trim().equalsIgnoreCase("Hardcore") ||
                    SelectedPics.get(i).Category.trim().equalsIgnoreCase("Offensive") ||
                    SelectedPics.get(i).Category.trim().equalsIgnoreCase("Minor")) {
                return true;
            }
        }
        return false;
    }

    private void CalcImageWidth() {
        ImageWidth = 106;
        try {
            Point size = new Point();
            this.getWindowManager().getDefaultDisplay().getSize(size);
            float scaleFactor = (size.x - 40) / 3.0f;
            ImageWidth = (int) (scaleFactor / getResources().getDisplayMetrics().density);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            ImageWidth = (int) (ImageWidth * metrics.density);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            ImageWidth = 106;
        }
    }

    public void CollapseFAB() {
        if (FABMenuAddPhoto.isExpanded()) {
            FABMenuAddPhoto.collapse();
        }
    }

    private void SetMenuItemVisibility(Boolean IsPublicFragmentShown) {
        if (IsPublicFragmentShown) {
            Boolean HasItemsSelected = PublicManagePicsFragment.IsAnyItemSelected();
            ArrayList<GDPic> SelectedItems = PublicManagePicsFragment.GetSelectedItems();
            ArrayList<GDPic> AllItems = PublicManagePicsFragment.GetAllItems();
            Boolean HasItems = AllItems.size() > 0;
            if (mMenu != null) {
                mMenu.getItem(0).setVisible((HasItems && SelectedItems.size() == 1) ? true : false);
                mMenu.getItem(1).setVisible((HasItems && HasItemsSelected) ? true : false);
                mMenu.getItem(2).setVisible((HasItems && HasItemsSelected) ? true : false);
                mMenu.getItem(3).setVisible(false);
                mMenu.getItem(4).setVisible((HasItems && !HasItemsSelected && ListHasAnyCategorizedPics(AllItems)) ? true : false);
            }
        } else {
            Boolean HasItemsSelected = PrivateManagePicsFragment.IsAnyItemSelected();
            ArrayList<GDPic> SelectedItems = PrivateManagePicsFragment.GetSelectedItems();
            ArrayList<GDPic> AllItems = PrivateManagePicsFragment.GetAllItems();
            Boolean HasItems = AllItems.size() > 0;
            if (mMenu != null) {
                mMenu.getItem(0).setVisible((HasItems && SelectedItems.size() == 1) ? true : false);
                mMenu.getItem(1).setVisible((HasItems && HasItemsSelected) ? true : false);
                mMenu.getItem(2).setVisible(false);
                mMenu.getItem(3).setVisible((HasItems && HasItemsSelected) ? true : false);
                mMenu.getItem(4).setVisible((HasItems && !HasItemsSelected && ListHasAnyCategorizedPics(AllItems)) ? true : false);
            }
        }
    }

    private Boolean ListHasAnyCategorizedPics(ArrayList<GDPic> AllItems) {
        Boolean HasAnyCategorizedPics = false;
        for (int i = 0; i < AllItems.size(); i++) {
            if (AllItems.get(i).IsCategorized) {
                HasAnyCategorizedPics = true;
                break;
            }
        }
        return HasAnyCategorizedPics;
    }

    private Boolean CloseOnBackPressed() {
        if (PublicManagePicsFragment.IsAnyItemSelected() || PrivateManagePicsFragment.IsAnyItemSelected() || FABMenuAddPhoto.isExpanded()) {
            //Deselect all items
            PublicManagePicsFragment.DeselectAll();
            PrivateManagePicsFragment.DeselectAll();

            //Collapse all FABs
            FABMenuAddPhoto.collapse();

            SetMenuItemVisibility(FragmentsViewPager.getCurrentItem() == 0);
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (CloseOnBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Boolean Uploaded = data.getBooleanExtra("Uploaded", false);
                if (Uploaded) {
                    ImageAPIHelper.ClearCachedList(true);
                    PublicManagePicsFragment.Reload();
                    FragmentsViewPager.setCurrentItem(0);
                }
            } else if (requestCode == 2) {
                Boolean Uploaded = data.getBooleanExtra("Uploaded", false);
                if (Uploaded) {
                    ImageAPIHelper.ClearCachedList(false);
                    PrivateManagePicsFragment.Reload();
                    FragmentsViewPager.setCurrentItem(1);
                }
            } else if (requestCode == 3) {
                GDPic gdPic = data.getParcelableExtra("GDPic");
                if (gdPic != null && gdPic.PicID != null && !gdPic.PicID.trim().equals("")) {
                    if (FragmentsViewPager.getCurrentItem() == 0) {
                        PublicManagePicsFragment.UpdCaption(gdPic);
                    } else {
                        PrivateManagePicsFragment.UpdCaption(gdPic);
                    }
                }
                SetMenuItemVisibility(FragmentsViewPager.getCurrentItem() == 0);
            }
        }
    }

    class ManagePicsFragmentAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ManagePicsFragmentAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
        }
    }

    class GDPicID {
        public String PicID;

        public GDPicID(String vPicID) {
            PicID = vPicID;
        }
    }
}
