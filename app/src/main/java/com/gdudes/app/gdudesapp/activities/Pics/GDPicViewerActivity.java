package com.gdudes.app.gdudesapp.activities.Pics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.CustomViewTypes.GDViewPager;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.GDFullImage;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageAPIHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TextLinkHelper;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gdudesapp.ZoomablePhotoView.PhotoView;

public class GDPicViewerActivity extends GDCustomToolbarAppCompatActivity {
    private GDViewPager mViewPager;
    private LinearLayout mGDPicsContainer;
    private LinearLayout FullImageContainer;
    private HorizontalScrollView GDPicScrollView;
    private int SelectedImagePosition = 0;
    private Users LoggedInUser;
    private ArrayList<GDPic> mGDPicList;
    private String SinglePicID;
    private String SinglePicOwnerID;
    private Context mContext;
    private Boolean IsPhonePhoto = false;
    private int mImageWidth = 106;
    private Boolean bImageWidthCalculated = false;
    static public String LogClass = "GDPicViewerActivity";

    public GDPicViewerActivity() {
        super("Photos");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = GDPicViewerActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);
        HasActions = false;
        ShowTitleWithoutActions = true;

        LoadActivityForIntent(getIntent());
        postCreate();
    }

    private void LoadActivityForIntent(Intent intent) {
        try {
            setContentView(R.layout.activity_gdpic_viewer);
            if (intent.hasExtra("GDPicList")) {
                mGDPicList = intent.getParcelableArrayListExtra("GDPicList");
            } else {
                SinglePicID = intent.getExtras().getString("SinglePicID", "");
                SinglePicOwnerID = intent.getExtras().getString("SinglePicOwnerID", "");
                if (SinglePicID == null || SinglePicID.equals("") || SinglePicOwnerID == null || SinglePicOwnerID.equals("")) {
                    finish();
                } else {
                    mGDPicList = new ArrayList<>();
                    GDPic gdPic = new GDPic();
                    gdPic.PicID = SinglePicID;
                    gdPic.UserID = SinglePicOwnerID;
                    mGDPicList.add(gdPic);
                }
            }
            SelectedImagePosition = intent.getIntExtra("SelectedPic", 0);
            if (intent.hasExtra("IsPhonePhoto")) {
                IsPhonePhoto = intent.getBooleanExtra("IsPhonePhoto", false);
            }
            if (mGDPicList == null || mGDPicList.size() == 0) {
            /*Get the GDPic list passed by another activity,
             If this is null or doesnt contain any item it means that the user was on "this" activity
             and then opened another app and the system relaimed  memory. So finish this activity.*/
                //SessionManager.RedirectRequestToHome(GDPicViewerActivity.this);
                finish();
                return;
            }
            GDPicScrollView = findViewById(R.id.GDPicScrollView);
            mGDPicsContainer = findViewById(R.id.GDPicsContainer);

            if (!IsPhonePhoto) {
                GetNonCachedPics();
            }
            InsertGDPics();

            //Get the Viewpager from the "gdfullpic_vewpager" layout
            LayoutInflater layoutInflater = (LayoutInflater) GDPicViewerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View ViewPagerView = layoutInflater.inflate(R.layout.gdfullpic_vewpager, null);
            //Set the Viewpager as the view for  LinearLayout "FullImageContainer"
            mViewPager = ViewPagerView.findViewById(R.id.GDFullPicViewPager);
            FullImageContainer = findViewById(R.id.FullImageContainer);
            FullImageContainer.addView(mViewPager);


            mViewPager.setAdapter(new GDFullPicPagerAdapter(GDPicViewerActivity.this, mGDPicList, LoggedInUser,
                    IsPhonePhoto, new LikeDisLikeLongPressedListener() {
                @Override
                public void OnLikeDisLikeLongPressed(String PicID, Boolean LikePressed) {
                    Intent PLDIntent = new Intent(GDPicViewerActivity.this, PicLikesDetailsActivity.class);
                    PLDIntent.putExtra("PicID", PicID);
                    startActivity(PLDIntent);
                }
            }));
            //Scroll the thumbnails on page scroll.
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    try {
                        if (position < 2) {
                            GDPicScrollView.scrollTo(0, 0);
                        } else {
                            GDPicScrollView.scrollTo((position - 1) * mImageWidth, 0);
                        }
                    } catch (Exception ex) {
                        GDLogHelper.LogException(ex);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            mViewPager.setCurrentItem(SelectedImagePosition);
            if (mGDPicList.size() < 2) {
                GDPicScrollView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            finish();
        }
    }

    private void InsertGDPics() {
        CalcTopPicsWidth();
        LayoutInflater layoutInflater = (LayoutInflater) GDPicViewerActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < mGDPicList.size(); i++) {
            View GDPicView = layoutInflater.inflate(R.layout.gdpic_layout, null);
            ImageView imageView = (ImageView) GDPicView.findViewById(R.id.GDPic);
            imageView.getLayoutParams().width = mImageWidth;
            imageView.getLayoutParams().height = mImageWidth;
            if (mGDPicList.get(i).image != null) {
                imageView.setImageBitmap(mGDPicList.get(i).image);
            } else {
                imageView.setImageResource(R.drawable.defaultuserprofilepic);
            }
            imageView.setTag(i);
            imageView.setOnClickListener(v -> {
                int position = (Integer) v.getTag();
                mViewPager.setCurrentItem(position);
            });
            mGDPicsContainer.addView(GDPicView);
        }
    }

    private void GetNonCachedPics() {
        ArrayList<String> picIDs = GDPic.GetPicIDListForNullImages(mGDPicList);
        if (picIDs.size() == 0) {
            return;
        }
        ImageAPIHelper.GetPicsForPicIDList(mContext, picIDs, false, pics -> {
            UpdateNonCachedPics(pics);
        });
    }

    private void UpdateNonCachedPics(ArrayList<GDPic> pics) {
        GDPic.SetPics(pics, mGDPicList);
        for (int i = 0; i < mGDPicList.size(); i++) {
            Bitmap image = mGDPicList.get(i).image;
            if (image != null) {
                ImageView imageView = mGDPicsContainer.getChildAt(i).findViewById(R.id.GDPic);
                imageView.setImageBitmap(image);
            }
        }
    }

    private void CalcTopPicsWidth() {
        if (bImageWidthCalculated) {
            return;
        }
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        double scaleFactor = size.x / 6.5;
        int imageWidth = (int) (scaleFactor / getResources().getDisplayMetrics().density);
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        mImageWidth = (int) (imageWidth * logicalDensity);
        bImageWidthCalculated = true;
    }

    static class GDFullPicPagerAdapter extends PagerAdapter {
        private static List<GDPic> maGDPicList;
        private Context mContext;
        private Users mLoggedInUser;
        private static Boolean IsLikeDislikeRunning = false;
        Boolean IsPhonePhoto;
        LikeDisLikeLongPressedListener likeDisLikeLongPressedListener;

        public GDFullPicPagerAdapter(Context c, List<GDPic> GDPicList, Users LoggedInUser,
                                     Boolean vIsPhonePhoto, LikeDisLikeLongPressedListener vlikeDisLikeLongPressedListener) {
            mContext = c;
            maGDPicList = GDPicList;
            mLoggedInUser = LoggedInUser;
            notifyDataSetChanged();
            IsPhonePhoto = vIsPhonePhoto;
            likeDisLikeLongPressedListener = vlikeDisLikeLongPressedListener;
        }

        @Override
        public int getCount() {
            return maGDPicList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View GDFullPicLayout = layoutInflater.inflate(R.layout.gdfullpic_layout, null);
            final PhotoView photoView = GDFullPicLayout.findViewById(R.id.FullImageView);
            final TextView LoadingPic = GDFullPicLayout.findViewById(R.id.LoadingPic);
            LinearLayout PicCaptionContainer = GDFullPicLayout.findViewById(R.id.PicCaptionContainer);
            LinearLayout FullImageDetails = GDFullPicLayout.findViewById(R.id.FullImageDetails);
            RelativeLayout LoadingCachedPicdetails = GDFullPicLayout.findViewById(R.id.LoadingCachedPicdetails);
            LinearLayout FullPicActions = GDFullPicLayout.findViewById(R.id.FullPicActions);
            final ToggleButton LikeToggle = GDFullPicLayout.findViewById(R.id.LikeToggle);
            final TextView LikeCount = GDFullPicLayout.findViewById(R.id.LikeCount);
            LinearLayout CommentContainer = GDFullPicLayout.findViewById(R.id.CommentContainer);
            TextView CommentCount = GDFullPicLayout.findViewById(R.id.CommentCount);

            LikeToggle.setOnClickListener(v -> PlaySound());
            LikeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> LikeDislikePic(LikeToggle, LikeCount, isChecked, maGDPicList.get(position).UserID, maGDPicList.get(position).PicID));
            CommentContainer.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, PicCommentsActivity.class);
                intent.putExtra("PicID", maGDPicList.get(position).PicID);
                intent.putExtra("OwnerUserID", maGDPicList.get(position).UserID);
                mContext.startActivity(intent);
            });
            LikeCount.setTag(position);

            if (!IsPhonePhoto) {
                //Get the Pic Src from DB or API and set it to photoView
                SetSelectedImage(position, photoView, FullPicActions, PicCaptionContainer, FullImageDetails, LoadingCachedPicdetails, LoadingPic, LikeToggle,
                        LikeCount, CommentCount);
                SetLikeCount(LikeCount, "");
            } else {
                try {
                    GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
                        Bitmap image = ImageHelper.GetBitmapFromPath(maGDPicList.get(position).DirectPicAttachedFilePath);
                        gdBackgroundTaskFinished.OnBackgroundTaskFinished(ImageHelper.scaleDownTo2048(image));
                    }, data -> {
                        photoView.setImageBitmap((Bitmap) data);
                    });
//                    photoView.setImageBitmap(ImageHelper.scaleDownTo2048(BitmapFactory.decodeFile(maGDPicList.get(position).CompletePicSrc)));
                    LoadingPic.setVisibility(View.GONE);
                } catch (Exception ex) {
                    GDLogHelper.LogException(ex);
                    GDToastHelper.ShowToast(mContext, "An error occurred while opening the image", GDToastHelper.ERROR, GDToastHelper.SHORT);
                    LoadingPic.setText("An error occurred while opening the image");
                }
            }

            // Now just add Layout to ViewPager and return it
            container.addView(GDFullPicLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return GDFullPicLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        private void SetSelectedImage(final int position, final PhotoView photoView, final LinearLayout FullPicActions,
                                      final LinearLayout PicCaptionContainer, final LinearLayout FullImageDetails, final RelativeLayout LoadingCachedPicdetails,
                                      final TextView LoadingPic, final ToggleButton LikeToggle, final TextView LikeCount, final TextView CommentCount) {
            ImageAPIHelper.GetFullPic(mContext, maGDPicList.get(position).PicID, false, true, picID -> {
                CallPicAPI(false, position, photoView, FullPicActions, PicCaptionContainer, FullImageDetails,
                        LoadingCachedPicdetails, LoadingPic, LikeToggle, LikeCount, CommentCount);
            }, pics -> {
                photoView.setImageBitmap(pics.get(0).image);
                LoadingPic.setVisibility(View.GONE);
                LoadingCachedPicdetails.setVisibility(View.VISIBLE);
                CallPicAPI(true, position, photoView, FullPicActions, PicCaptionContainer, FullImageDetails,
                        LoadingCachedPicdetails, LoadingPic, LikeToggle, LikeCount, CommentCount);
            }, null);

        }

        private void CallPicAPI(final Boolean picExistsInCache, final int position, final PhotoView photoView, final LinearLayout FullPicActions,
                                final LinearLayout PicCaptionContainer, final LinearLayout FullImageDetails, final RelativeLayout LoadingCachedPicdetails,
                                final TextView LoadingPic, final ToggleButton LikeToggle, final TextView LikeCount, final TextView CommentCount) {
            try {
                String ReqType = picExistsInCache ? "GetFullUserImageCachedInDB" : "GetFullUserImage";
                List<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_PicID, maGDPicList.get(position).PicID));
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_RequestingUserID, mLoggedInUser.UserID));
                APICallInfo apiCallInfo = new APICallInfo("Home", ReqType, pAPICallParameters, "GET", null, null, false, null, APICallInfo.APITimeouts.SEMILONG);
                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
                    try {
                        if (LoadingCachedPicdetails.getVisibility() == View.VISIBLE) {
                            LoadingCachedPicdetails.setVisibility(View.GONE);
                        }
                        if (result == null || result.equals("") || result.equals("-1")) {
                            LoadingPic.setText("Error while loading photo");
                            return;
                        }
                        JSONObject jsonObject = new JSONObject(result);

                        String FullImageObj = jsonObject.getString("FullUserImage");

                        String sTemp = jsonObject.getString("LikesCount");
                        sTemp = sTemp.substring(1, sTemp.length() - 1);
                        int iLikesCount = Integer.parseInt(new JSONObject(sTemp).getString("LikesCount"));

                        sTemp = jsonObject.getString("CommentsCount");
                        sTemp = sTemp.substring(1, sTemp.length() - 1);
                        int iCommentsCount = Integer.parseInt(new JSONObject(sTemp).getString("CommentsCount"));
                        FullImageObj = FullImageObj.substring(1, FullImageObj.length() - 1);
                        GDFullImage fullImage = new GsonBuilder().create().fromJson(FullImageObj, GDFullImage.class);

                        if (!picExistsInCache) {
                            Bitmap bitmap;
                            try {
                                bitmap = ImageHelper.GetBitmapFromString(fullImage.FullImage, true);
                            } catch (Exception err) {
                                LoadingPic.setText("Error while loading photo");
                                GDLogHelper.LogException(err);
                                return;
                            }
                            photoView.setImageBitmap(bitmap);
                            LoadingPic.setVisibility(View.GONE);

                            GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
                                GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(mContext);
                                gdImageDBHelper.AddImageToCache(maGDPicList.get(position).PicID, maGDPicList.get(position).UserID,
                                        fullImage.FullImage, true);
                            }, null);
                        }

                        if (fullImage.Likes) {
                            LikeToggle.toggle();
                        }
                        if (fullImage.Caption == null || fullImage.Caption.equals("")) {
                            PicCaptionContainer.setVisibility(View.GONE);
                        } else {
                            try {
                                ((TextView) PicCaptionContainer.findViewById(R.id.PicCaption)).setText(StringEncoderHelper.decodeURIComponent(fullImage.Caption).trim());
                            } catch (Exception ex) {
                                PicCaptionContainer.setVisibility(View.GONE);
                                GDLogHelper.LogException(ex);
                                GDLogHelper.Log(LogClass, "SetSelectedImage", "PicID: " + maGDPicList.get(position).PicID, GDLogHelper.LogLevel.EXCEPTION);
                            }
                        }
                        SetLikeCount(LikeCount,Integer.toString(iLikesCount) + " guys like this pic.");
                        PicInitialLikeDislikeState picInitialLikeDislikeState = new PicInitialLikeDislikeState(fullImage.Likes, iLikesCount);
                        LikeToggle.setTag(picInitialLikeDislikeState);
                        CommentCount.setText(Integer.toString(iCommentsCount));
                        FullImageDetails.setVisibility(View.VISIBLE);

                        photoView.setOnPhotoTapListener((imageView, v, v1) -> {
                            if (FullImageDetails.getVisibility() == View.GONE) {
                                FullImageDetails.setVisibility(View.VISIBLE);
                            } else {
                                FullImageDetails.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {
                        GDLogHelper.LogException(e);
                        LoadingPic.setText("Error while loading photo");
                    }
                }, () -> {
                    GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                    LoadingPic.setText("Error while loading photo");
                });
            } catch (Exception ex) {
                GDLogHelper.LogException(ex);
            }
        }

        private void LikeDislikePic(final ToggleButton LikeToggle, final TextView LikeCount, Boolean Likes,
                                    String OwnerUserID, String PicID) {
            try {
                if (!IsLikeDislikeRunning) {
                    IsLikeDislikeRunning = true;
                    if (LikeToggle.getTag() == null) {
                        IsLikeDislikeRunning = false;
                        return;
                    }
                    PicInitialLikeDislikeState picInitialLikeDislikeState = (PicInitialLikeDislikeState) LikeToggle.getTag();
                    int likeCount = 0;
                    if (Likes) {
                        likeCount = picInitialLikeDislikeState.Likes ? picInitialLikeDislikeState.LikeCount : picInitialLikeDislikeState.LikeCount + 1;
                    } else {
                        likeCount = picInitialLikeDislikeState.Likes ? picInitialLikeDislikeState.LikeCount - 1 : picInitialLikeDislikeState.LikeCount;
                    }
                    if (likeCount == 1) {
                        SetLikeCount(LikeCount, Integer.toString(likeCount) + " guy likes this pic.");
                    } else {
                        SetLikeCount(LikeCount, Integer.toString(likeCount) + " guys like this pic.");
                    }

                    Boolean WasSelected = picInitialLikeDislikeState.Likes;
                    LikeDislikeUserPic likeDislikeUserPic = new LikeDislikeUserPic(PicID, OwnerUserID, mLoggedInUser.UserID,
                            Boolean.toString(Likes && !WasSelected), Boolean.toString(false));
                    APICallInfo apiCallInfo = new APICallInfo("Home", "LikeDislikeUserPic", null, "POST", likeDislikeUserPic, null, false, null, APICallInfo.APITimeouts.MEDIUM);
                    GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                        @Override
                        public void onAPIComplete(String result, Object ExtraData) {
                            IsLikeDislikeRunning = false;
                        }
                    }, new APINoNetwork() {
                        @Override
                        public void onAPINoNetwork() {
                            IsLikeDislikeRunning = false;
                            GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        }
                    });
                }
            } catch (Exception ex) {
                IsLikeDislikeRunning = false;
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
            }
        }

        private void SetLikeCount(TextView LikeCount, String text) {
            LikeCount.setText(text);
            int position = (int) LikeCount.getTag();
            if (StringHelper.IsNullOrEmpty(text)) {
                return;
            }
            TextLinkHelper.AddLink(LikeCount, text, () -> {
                likeDisLikeLongPressedListener.OnLikeDisLikeLongPressed(maGDPicList.get(position).PicID, true);
            }, false, Color.parseColor("#ffffff"));
        }

        private void PlaySound() {
            try {
                MediaPlayer mp = MediaPlayer.create(mContext, R.raw.likedislike);
                mp.start();
            } catch (Exception e) {
            }
        }

        class LikeDislikeUserPic {
            private String PicID;
            private String OwnerUserID;
            private String SenderID;
            private String Likes;
            private String DisLikes;

            public LikeDislikeUserPic() {
                PicID = "";
                OwnerUserID = "";
                SenderID = "";
                Likes = "";
                DisLikes = "";
            }

            public LikeDislikeUserPic(String vPicID, String vOwnerUserID, String vSenderID, String vLikes, String vDisLikes) {
                PicID = vPicID;
                OwnerUserID = vOwnerUserID;
                SenderID = vSenderID;
                Likes = vLikes;
                DisLikes = vDisLikes;
            }
        }

        class PicInitialLikeDislikeState {
            public Boolean Likes = false;
            public int LikeCount = 0;

            public PicInitialLikeDislikeState(Boolean vLikes, int vLikeCount) {
                Likes = vLikes;
                LikeCount = vLikeCount;
            }
        }
    }

    interface LikeDisLikeLongPressedListener {
        void OnLikeDisLikeLongPressed(String PicID, Boolean LikePressed);
    }
}
