package com.gdudes.app.gdudesapp.activities.Pics;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.UplImage;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.GetFileWritePermission;
import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;
import com.gdudes.app.gdudesapp.activities.LoginRegister.RegisterProfileDescActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.GsonBuilder;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class UploadPicsActivity extends GDCustomToolbarAppCompatActivity {

    private static String LogClass = "UploadPicsActivity";
    GridLayout UplPicsGrid;
    ArrayList<Integer> UploadErrorList;
    FloatingActionButton UploadPicsFAB;
    ImageView CurrentlyCroppingImageViewCropButton = null;
    ImageView CurrentlyCroppingImageView = null;
    Button SkipUploadingPics = null;
    String CameraPhotoPath = "";

    Context mContext;
    Users LoggedInUser;
    Menu mMenu;
    final int MAXImages = 5;
    int selectedImages = 0;
    String PicFolderID = "";
    Boolean IsRegistrationFirstPic = false;
    int UploadPicNumber = -1;
    int TotalImagesToUpload = 0;
    Boolean LimitErrorOccurred = false;
    GetFileWritePermission CurrentGetFileWritePermission;
    Boolean IsPublicUserPic = true;
    Boolean HasAnyPicBeenUploaded = false;
    GetCameraPermission mGetCameraPermission;

    public UploadPicsActivity() {
        super("Upload photos");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pics);
        mContext = UploadPicsActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);
        ShowTitleWithoutActions = true;

        if (getIntent().hasExtra("PicFolderID")) {
            PicFolderID = getIntent().getExtras().getString("PicFolderID", "");
        }
        if (getIntent().hasExtra("IsRegistrationFirstPic")) {
            IsRegistrationFirstPic = getIntent().getBooleanExtra("IsRegistrationFirstPic", true);
        }
        if (getIntent().hasExtra("IsPublicUserPic")) {
            IsPublicUserPic = getIntent().getBooleanExtra("IsPublicUserPic", true);
        }

        UplPicsGrid = findViewById(R.id.GDPicGrid);
        UploadPicsFAB = findViewById(R.id.PicturesAddFAB);
        UploadPicsFAB.setOnClickListener(v -> ShowPicUploadOptions());
        if (IsRegistrationFirstPic) {
            SkipUploadingPics = findViewById(R.id.SkipUploadingPics);
            SkipUploadingPics.setVisibility(View.VISIBLE);
            SkipUploadingPics.setOnClickListener(v -> ShowMessageForFirsTimeSkip());
        }

        selectedImages = 0;
        ShowPicUploadOptions();
        postCreate();
    }

    private void UploadPic() {
        if (UploadPicNumber >= TotalImagesToUpload) {
            mMenu.getItem(0).setVisible(false);
            mMenu.getItem(1).setVisible(false);
            if (UploadErrorList.size() == 0) {
                UplPicsGrid.removeAllViews();
                UplPicsGrid.setVisibility(View.GONE);
                selectedImages = 0;
                GDToastHelper.ShowToast(mContext, "All images uploaded successfully", GDToastHelper.INFO, GDToastHelper.SHORT);
                UploadPicsFAB.setVisibility(View.VISIBLE);
            } else {
                ArrayList<View> RemoveViewList = new ArrayList<>();
                for (int i = 0; i < TotalImagesToUpload; i++) {
                    if (!UploadErrorList.contains(i)) {
                        RemoveViewList.add(UplPicsGrid.getChildAt(i));
                    }
                }
                for (int i = 0; i < RemoveViewList.size(); i++) {
                    UplPicsGrid.removeView(RemoveViewList.get(i));
                }
                mMenu.getItem(0).setVisible(true);
                mMenu.getItem(1).setVisible(true);

                selectedImages = UploadErrorList.size();
                if (!LimitErrorOccurred) {
                    GDToastHelper.ShowToast(mContext, Integer.toString(UploadErrorList.size()) + " images could not be uploaded.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                }
                if (selectedImages < TotalImagesToUpload) {
                    UploadPicsFAB.setVisibility(View.VISIBLE);
                }
                RemoveViewList.clear();
                UploadErrorList.clear();
            }
            if (UploadErrorList.size() < TotalImagesToUpload && IsRegistrationFirstPic) {
                LoggedInUser.HasPicsToBeCategorized = "1";
                SessionManager.UserLogIn(LoggedInUser);
                Intent intent = new Intent(getApplicationContext(), RegisterProfileDescActivity.class);
                startActivity(intent);
                finish();
            }
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(UploadPicsActivity.this);
        progressDialog.setMessage("Uploading Image " + Integer.toString(UploadPicNumber + 1) + " of " + Integer.toString(TotalImagesToUpload) + "\nPlease wait...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String PicID = GDGenericHelper.GetNewGUID();
        ImageView img = UplPicsGrid.getChildAt(UploadPicNumber).findViewById(R.id.UplImage);
        String PicSrc = ImageHelper.GetStringFromBitmap(((BitmapDrawable) img.getDrawable()).getBitmap());
        EditText caption = UplPicsGrid.getChildAt(UploadPicNumber).findViewById(R.id.PicCaption);

        UplImage NewUplImg = new UplImage(LoggedInUser.UserID, "", PicID, PicSrc,
                StringEncoderHelper.encodeURIComponent(caption.getText().toString().trim()),
                PicFolderID, Integer.toString(PicSrc.length()), IsPublicUserPic);

        APICallInfo apiCallInfo = new APICallInfo("CompleteProfile", "InsertNewUserPic",
                null, "POST", NewUplImg, null,
                false, null, APICallInfo.APITimeouts.LONG);
        GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, (result, ExtraData) -> {
            try {
                progressDialog.dismiss();
                SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                if (successResult != null && successResult.SuccessResult != 1) {
                    if (successResult.SuccessResult == Integer.parseInt(getResources().getString(R.string.user_limit_error_code))) {
                        while (UploadPicNumber < TotalImagesToUpload) {
                            UploadErrorList.add(UploadPicNumber);
                            UploadPicNumber = UploadPicNumber + 1;
                        }
                        LimitErrorOccurred = true;
                        GDGenericHelper.ShowBuyPremiumIfNotPremium(mContext, successResult.FailureMessage, true);
                    } else {
                        UploadErrorList.add(UploadPicNumber);
                    }
                } else if (successResult != null && successResult.SuccessResult == 1) {
                    HasAnyPicBeenUploaded = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                GDLogHelper.LogException(e);
                UploadErrorList.add(UploadPicNumber);
            } finally {
                UploadPicNumber = UploadPicNumber + 1;
                UploadPic();
            }
        }, null, () -> {
            GDToastHelper.ShowToast(UploadPicsActivity.this, getString(R.string.no_network_connection), GDToastHelper.ERROR, GDToastHelper.SHORT);
            UploadPicNumber = UploadPicNumber + 1;
            UploadPic();
        });
    }

    private void ShowPicUploadOptions() {
        final CharSequence[] options = {"From Camera", "From Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Add photos");
        builder.setItems(options, (dialog, item) -> {
            if (item == 0) {
                StartGetCameraPictureFlow();
            } else if (item == 1) {
                CurrentGetFileWritePermission = () -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);
                        PersistantPreferencesHelper.SetFileWritePermission("1");
                    } catch (Exception ex) {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occured while opening Gallery. Please try again.", TopSnackBar.LENGTH_SHORT, true).show();
                        GDLogHelper.LogException(ex);
                    }
                };
                CheckAndRequestStoragePermission(CurrentGetFileWritePermission);
            }  else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void StartGetCameraPictureFlow() {
        if (!IsCameraPermissionGranted()) {
            mGetCameraPermission = () -> StartGetCameraPictureFlow();
            CheckAndRequestCameraPermission(mGetCameraPermission);
            return;
        }
        CurrentGetFileWritePermission = () -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            CameraPhotoPath = "CameraPhotoPath" + "_" + GDGenericHelper.GetNewGUID() + ".jpg";
            File file = new File(ImageHelper.CreateDirectoryForImage(ImageHelper.UPLOAD_IMAGE),
                    CameraPhotoPath);
            Uri fileUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName()
                    + ".gdudesapp.provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, 1);
            PersistantPreferencesHelper.SetFileWritePermission("1");
        };
        CheckAndRequestStoragePermission(CurrentGetFileWritePermission);
    }

    private void CheckAndRequestStoragePermission(GetFileWritePermission getFileWritePermission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                if (getFileWritePermission != null) {
                    getFileWritePermission.OnPermissionGranted();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            if (getFileWritePermission != null) {
                getFileWritePermission.OnPermissionGranted();
            }
        }
    }

    private Boolean IsCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private void CheckAndRequestCameraPermission(GetCameraPermission getCameraPermission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (getCameraPermission != null) {
                    getCameraPermission.OnPermissionGranted();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);
            }
        } else {
            if (getCameraPermission != null) {
                getCameraPermission.OnPermissionGranted();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 1) {
                if (CurrentGetFileWritePermission != null) {
                    CurrentGetFileWritePermission.OnPermissionGranted();
                }
            } else if (requestCode == 2) {
                if (mGetCameraPermission != null) {
                    mGetCameraPermission.OnPermissionGranted();
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(ImageHelper.CreateDirectoryForImage(ImageHelper.UPLOAD_IMAGE));
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals(CameraPhotoPath)) {
                        f = temp;
                        break;
                    }
                }
                try {
                    AddImgToGrid(ImageHelper.CompressImageFile(f.getAbsolutePath(), true, false), f.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                }
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                Bitmap loadedimage;
                try {
                    loadedimage = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(selectedImage));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while loading the selected photo.\nPlease try again.", TopSnackBar.LENGTH_SHORT, true).show();
                    return;
                }
                if (loadedimage == null) {
                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "An error occurred while loading the selected photo.\nPlease try again.", TopSnackBar.LENGTH_SHORT, true).show();
                    return;
                }
                String LocalUploadImagePath = ImageHelper.MakeDirectoryAndSaveImage(loadedimage, ImageHelper.UPLOAD_IMAGE);
                AddImgToGrid(ImageHelper.CompressImageFile(LocalUploadImagePath, true, false), LocalUploadImagePath);
            } else if (requestCode == 3) {
                Object[] SelectedItems = (Object[]) data.getExtras().get("SelectedImages");
                if (SelectedItems != null) {
                    for (int i = 0; i < SelectedItems.length; i++) {
                        AddImgToGrid(ImageHelper.CompressImageFile(SelectedItems[i].toString(), true, false), SelectedItems[i].toString());
                    }
                }
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Send Direct Photo after cropping done
            if (resultCode == RESULT_OK) {
                Uri selectedImage = result.getUri();
                String CroppedPicturePath = selectedImage.getPath();

                CurrentlyCroppingImageView.setImageBitmap(ImageHelper.CompressImageFile(CroppedPicturePath, true, false));
                CurrentlyCroppingImageViewCropButton.setVisibility(View.GONE);
                //Delete the Cropped picture from cache
                File file = new File(CroppedPicturePath);
                file.delete();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Exception error = result.getError();
            }
        }
    }

    private void AddImgToGrid(Bitmap image, final String FilePath) {
        //image = ImageHelper.scaleDownTo2048(image);
        LayoutInflater layoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView = layoutInflater.inflate(R.layout.upload_pic_item, null);
        final ImageView imageView = convertView.findViewById(R.id.UplImage);
        ImageButton imageButton = convertView.findViewById(R.id.RemoveImage);
        final ImageView CropImage = convertView.findViewById(R.id.CropImage);
        imageView.setImageBitmap(image);

        selectedImages++;
        if (mMenu != null) {
            mMenu.getItem(0).setVisible(true);
            mMenu.getItem(1).setVisible(true);
        }
        if (selectedImages >= MAXImages) {
            UploadPicsFAB.setVisibility(View.GONE);
        }
        imageButton.setOnClickListener(v -> {
            UplPicsGrid.removeView(convertView);
            selectedImages--;
            if (selectedImages <= 0) {
                selectedImages = 0;
                UplPicsGrid.setVisibility(View.GONE);
                mMenu.getItem(0).setVisible(false);
                mMenu.getItem(1).setVisible(false);
            }
            UploadPicsFAB.setVisibility(View.VISIBLE);
        });
        CropImage.setOnClickListener(v -> {
            CurrentlyCroppingImageView = imageView;
            CurrentlyCroppingImageViewCropButton = CropImage;
            com.theartofdev.edmodo.cropper.CropImage.activity(Uri.fromFile(new File(FilePath)))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setOutputCompressQuality(100)
                    .setInitialCropWindowPaddingRatio(0)
                    .start(UploadPicsActivity.this);
        });

        UplPicsGrid.setVisibility(View.VISIBLE);
        UplPicsGrid.addView(convertView);
        if (IsRegistrationFirstPic) {
            SkipUploadingPics.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_uploadpics, menu);
        mMenu.getItem(0).setVisible(false);
        mMenu.getItem(1).setVisible(false);
        if (selectedImages > 0) {
            mMenu.getItem(0).setVisible(true);
            mMenu.getItem(1).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (IsRegistrationFirstPic) {
                ShowMessageForFirsTimeSkip();
            } else {
                FinishWithResult();
            }
            return false;
        } else if (id == R.id.action_Done) {
            mMenu.getItem(0).setVisible(false);
            mMenu.getItem(1).setVisible(false);
            TotalImagesToUpload = UplPicsGrid.getChildCount();
            if (TotalImagesToUpload < 1) {
                return false;
            }
            UploadErrorList = new ArrayList<>();
            UploadPicNumber = 0;
            LimitErrorOccurred = false;
            GDGenericHelper.HideKeyboard(UploadPicsActivity.this);
            UploadPic();
        } else if (id == R.id.action_Cancel) {
            UplPicsGrid.removeAllViews();
            mMenu.getItem(0).setVisible(false);
            mMenu.getItem(1).setVisible(false);
            UploadPicsFAB.setVisibility(View.VISIBLE);
            selectedImages = 0;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (IsRegistrationFirstPic) {
            ShowMessageForFirsTimeSkip();
        } else {
            FinishWithResult();
        }
    }

    private void ShowMessageForFirsTimeSkip() {
        GDDialogHelper.ShowYesNoTypeDialog(UploadPicsActivity.this, "Skip uploading profile photo?",
                "If you do not upload a profile photo, you may not show up in search results for other guys around you. Are you sure you want to skip?",
                GDDialogHelper.BUTTON_TEXT_YES, GDDialogHelper.BUTTON_TEXT_CANCEL, GDDialogHelper.ALERT, () -> {
                    Intent intent = new Intent(UploadPicsActivity.this, RegisterProfileDescActivity.class);
                    startActivity(intent);
                    finish();
                }, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImageHelper.DeleteAllUploadedImges();
    }

    private void FinishWithResult() {
        if (HasAnyPicBeenUploaded) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("Uploaded", true);
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, returnIntent);
            } else {
                getParent().setResult(Activity.RESULT_OK, returnIntent);
            }
        }
        finish();
    }

    interface GetCameraPermission {
        void OnPermissionGranted();
    }
}
