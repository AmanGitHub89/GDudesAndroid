package com.gdudes.app.gdudesapp.subActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.GDSubGenericAction;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAttachment {
    private Context mContext;
    private GDSubGenericAction subGenericAction;
    private LayoutInflater mLayoutInflater;
    private LinearLayout AttachmentMainView;
    private LinearLayout PublicPicsScroll;
    private LinearLayout PrivatePicsScroll;
    private LinearLayout NoPrivatePicsFoundLayout;
    private LinearLayout NoPublicPicsFoundLayout;

    private LinearLayout NewPhoto;
    private LinearLayout SendLocation;
    private LinearLayout SelectPhotoLayout;
    private LinearLayout SelectedPhotoLayout;
    private ImageView SelectedPhoto;
    private LinearLayout SelectedPhotoBackButton;

    private Button btnSendPhoto;
    private Bitmap shownImage;
    private Boolean IsDirectPicShown = false;
    private String SelectedUploadedPicID = "";

    private static ArrayList<GDPic> mPrivatePics = new ArrayList<>();
    private static ArrayList<GDPic> mPublicPics = new ArrayList<>();

    public ChatAttachment(Context context, GDSubGenericAction subGenericAction) {
        this.mContext = context;
        this.subGenericAction = subGenericAction;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        InitializeData();
    }

    private void InitializeData() {
        this.AttachmentMainView = (LinearLayout) mLayoutInflater.inflate(R.layout.message_attach_layout, null);
        this.PrivatePicsScroll = AttachmentMainView.findViewById(R.id.PrivatePicsScroll);
        this.PublicPicsScroll = AttachmentMainView.findViewById(R.id.PublicPicsScroll);
        this.NoPrivatePicsFoundLayout = AttachmentMainView.findViewById(R.id.NoPrivatePicsFoundLayout);
        this.NoPublicPicsFoundLayout = AttachmentMainView.findViewById(R.id.NoPublicPicsFoundLayout);

        this.NewPhoto = AttachmentMainView.findViewById(R.id.NewPhoto);
        this.SendLocation = AttachmentMainView.findViewById(R.id.SendLocation);
        this.SelectPhotoLayout = AttachmentMainView.findViewById(R.id.SelectPhotoLayout);
        this.SelectedPhotoLayout = AttachmentMainView.findViewById(R.id.SelectedPhotoLayout);
        this.SelectedPhoto = AttachmentMainView.findViewById(R.id.SelectedPhoto);
        this.btnSendPhoto = AttachmentMainView.findViewById(R.id.btnSendPhoto);
        this.SelectedPhotoBackButton = AttachmentMainView.findViewById(R.id.SelectedPhotoBackButton);

        NewPhoto.setOnClickListener(v -> subGenericAction.OnSubGenericAction("ShowNewPhotoOptions", ""));
        SendLocation.setOnClickListener(v -> subGenericAction.OnSubGenericAction("SendLocation", ""));
        btnSendPhoto.setOnClickListener(v -> {
            if (IsDirectPicShown) {
                subGenericAction.OnSubGenericAction("SendDirectPic", "");
            } else {
                subGenericAction.OnSubGenericAction("SendUploadedPic", SelectedUploadedPicID);
            }
        });
        SelectedPhotoBackButton.setOnClickListener(v -> subGenericAction.OnSubGenericAction("CancelSelectedPhoto", ""));

        if (mPrivatePics.size() == 0) {
            GetPics(false);
        } else {
            AddPicsToView(false);
            ArrayList<String> picIDs = GDPic.GetPicIDListForNullImages(mPrivatePics);
            ImageAPIHelper.GetPicsForPicIDList(mContext, picIDs, false, pics -> UpdatePicsSrc(pics, false));
        }
        if (mPublicPics.size() == 0) {
            GetPics(true);
        } else {
            AddPicsToView(true);
            ArrayList<String> picIDs = GDPic.GetPicIDListForNullImages(mPublicPics);
            ImageAPIHelper.GetPicsForPicIDList(mContext, picIDs, false, pics -> UpdatePicsSrc(pics, false));
        }
    }

    public void Reset() {
        SelectPhotoLayout.setVisibility(View.VISIBLE);
        SelectedPhotoLayout.setVisibility(View.GONE);
        IsDirectPicShown = false;
        SelectedUploadedPicID = "";
        shownImage = null;
    }

    public void ShowDirectPicImage(Bitmap image) {
        IsDirectPicShown = true;
        shownImage = image;
        SelectedPhoto.setImageBitmap(shownImage);
        SelectPhotoLayout.setVisibility(View.GONE);
        SelectedPhotoLayout.setVisibility(View.VISIBLE);
    }

    private void ShowUploadedPicImage(String picID) {
        Bitmap image = null;
        for (int i = 0; i < mPrivatePics.size(); i++) {
            if (mPrivatePics.get(i).PicID.equalsIgnoreCase(picID)) {
                image = mPrivatePics.get(i).image;
                break;
            }
        }
        if (image == null) {
            for (int i = 0; i < mPublicPics.size(); i++) {
                if (mPublicPics.get(i).PicID.equalsIgnoreCase(picID)) {
                    image = mPublicPics.get(i).image;
                    break;
                }
            }
        }

        if (image != null) {
            IsDirectPicShown = false;
            SelectedUploadedPicID = picID;
            shownImage = image;
            SelectedPhoto.setImageBitmap(shownImage);
            subGenericAction.OnSubGenericAction("PicSelected", picID);
            SelectPhotoLayout.setVisibility(View.GONE);
            SelectedPhotoLayout.setVisibility(View.VISIBLE);
        }
    }


    public View GetView() {
        ViewParent parent = AttachmentMainView.getParent();
        if (parent != null) {
            ViewGroup parentAsView = (ViewGroup) parent;
            if (parentAsView != null) {
                parentAsView.removeAllViews();
            }
        }
        return AttachmentMainView;
    }

    private void GetPics(final Boolean isPublic) {
        ImageAPIHelper.GetUserPictures(mContext, isPublic, false, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                if (isPublic) {
                    mPublicPics = (ArrayList<GDPic>) result;
                } else {
                    mPrivatePics = (ArrayList<GDPic>) result;
                }
                AddPicsToView(isPublic);
            }

            @Override
            public void OnError(String result, Object extraData) {
                SetViewsVisibility(isPublic, false);
            }

            @Override
            public void OnNoNetwork(Object extraData) {
            }
        }, pics -> {
            if (isPublic) {
                mPublicPics = pics;
            } else {
                mPrivatePics = pics;
            }
            UpdatePicsSrc(pics, isPublic);
        });
    }

    private void AddPicsToView(Boolean isPublic) {
        List<GDPic> picsList = isPublic ? mPublicPics : mPrivatePics;
        LinearLayout PicsScroll = isPublic ? PublicPicsScroll : PrivatePicsScroll;
        for (int i = 0; i < picsList.size(); i++) {
            View GDPicView = mLayoutInflater.inflate(R.layout.gdpic_layout, null);
            ImageView imageView = (ImageView) GDPicView.findViewById(R.id.GDPic);
            imageView.getLayoutParams().width = 160;
            imageView.getLayoutParams().height = 160;
            if (picsList.get(i).image != null) {
                imageView.setImageBitmap(picsList.get(i).image);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }
            imageView.setTag(picsList.get(i).PicID);
            imageView.setOnClickListener(v -> ShowUploadedPicImage((String) v.getTag()));
            PicsScroll.addView(GDPicView);
        }
        SetViewsVisibility(isPublic, picsList.size() > 0);
    }

    private void SetViewsVisibility(Boolean isPublic, Boolean hasPics) {
        LinearLayout PicsScroll = isPublic ? PublicPicsScroll : PrivatePicsScroll;
        LinearLayout noPicsLayout = isPublic ? NoPublicPicsFoundLayout : NoPrivatePicsFoundLayout;
        if (hasPics) {
            noPicsLayout.setVisibility(View.GONE);
            PicsScroll.setVisibility(View.VISIBLE);
        } else {
            noPicsLayout.setVisibility(View.VISIBLE);
            PicsScroll.setVisibility(View.GONE);
        }
    }

    private void UpdatePicsSrc(List<GDPic> imagePicsList, Boolean isPublic) {
        List<GDPic> picsList = isPublic ? mPublicPics : mPrivatePics;
        LinearLayout PicsScroll = isPublic ? PublicPicsScroll : PrivatePicsScroll;
        for (int i = 0; i < imagePicsList.size(); i++) {
            for (int j = 0; j < picsList.size(); j++) {
                if (imagePicsList.get(i).PicID.equalsIgnoreCase(picsList.get(j).PicID)) {
                    ImageView imageView = PicsScroll.getChildAt(j).findViewById(R.id.GDPic);
                    picsList.get(j).image = imagePicsList.get(i).image;
                    imageView.setImageBitmap(picsList.get(j).image);
                    break;
                }
            }
        }
    }

}
