package com.gdudes.app.gdudesapp.activities.Common;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.UserLocation;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.GDValidationHelper;
import com.gdudes.app.gdudesapp.Helpers.GPSHelper;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Helpers.TopSnackBar.TopSnackBar;
import com.gdudes.app.gdudesapp.Interfaces.GPSLocationChanged;
import com.gdudes.app.gdudesapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.GsonBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gdudesapp.GDTooltipHelper.Tooltip;

public class GDMapActivity extends GDCustomToolbarAppCompatActivity implements GoogleMap.OnMapClickListener {

    private static String LogClass = "GDMapActivity";
    public final static int VIEW_LOCATION = 0;
    public final static int VIEW_USER_LOCATION = 1;
    public final static int SELECT_LOCATION = 2;
    public final static int NEW_EDIT_USER_LOCATION = 3;
    private int Activity_Mode = VIEW_LOCATION;

    Users LoggedInUser;
    String LoggedInUserPicID = "";
    Context mContext;
    private GoogleMap mMap;
    Boolean mMoveCameraToCurrentLocation = false;
    LatLng mUserLatLng;
    UserLocation mUserLocation;
    String LocationOwnerUserID;
    String LocationOwnerName;
    String LocationOwnerPicID;
    LatLngBounds mBounds;
    String MarkerTitle;
    GDImageDBHelper gdImageDBHelper = null;
    GPSLocationChanged mGPSLocationChanged = null;
    String TooltipMessage = "";

    Menu mMenu;
    LinearLayout GDMapNewLocation;
    EditText LocationNickName;
    ProgressDialog mMapLoadingProgressDialog = null;

    GDValidationHelper gdValidationHelper;

    public GDMapActivity() {
        super("Location");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdmap);
        mContext = GDMapActivity.this;
        Boolean IsPreLogin = false;
        if (getIntent().hasExtra("IsPreLogin")) {
            IsPreLogin = getIntent().getBooleanExtra("IsPreLogin", false);
        }
        LoggedInUser = IsPreLogin ? null : SessionManager.GetLoggedInUser(mContext);
        if(LoggedInUser != null) {
            LoggedInUserPicID = LoggedInUser.PicID;
        }
        gdImageDBHelper = new GDImageDBHelper(mContext);
        try {
            if (!SessionManager.IsSessionManagerInitiated) {
                SessionManager.InitSessionManager(getApplicationContext());
            }

            GDMapNewLocation = findViewById(R.id.GDMapNewLocation);
            LocationNickName = findViewById(R.id.LocationNickName);

            Activity_Mode = getIntent().getExtras().getInt("Activity_Mode", VIEW_LOCATION);

            String LocationLatLng = "0,0";
            if (getIntent().hasExtra("LocationLatLng")) {
                LocationLatLng = getIntent().getExtras().getString("LocationLatLng", "0,0");
                List<String> Location = StringHelper.SplitStringByComma(LocationLatLng);
                mUserLatLng = new LatLng(Double.parseDouble(Location.get(0)), Double.parseDouble(Location.get(1)));
            }
            if (getIntent().hasExtra("Location")) {
                mUserLocation = getIntent().getExtras().getParcelable("Location");
                if (mUserLocation.LocationLatLng != null && !mUserLocation.LocationLatLng.equals("")) {
                    List<String> Location = StringHelper.SplitStringByComma(mUserLocation.LocationLatLng);
                    mUserLatLng = new LatLng(Double.parseDouble(Location.get(0)), Double.parseDouble(Location.get(1)));
                }
            }
            if (getIntent().hasExtra("TooltipMessage")) {
                TooltipMessage = getIntent().getStringExtra("TooltipMessage");
                if (TooltipMessage == null) {
                    TooltipMessage = "";
                }
            }
            switch (Activity_Mode) {
                case VIEW_LOCATION:
                    String ToolbarText = getIntent().getExtras().getString("ToolbarText", "Location");
                    MarkerTitle = getIntent().getExtras().getString("MarkerTitle", "");
                    setToolbarText(ToolbarText);
                    break;
                case VIEW_USER_LOCATION:
                    LocationOwnerUserID = getIntent().getExtras().getString("LocationOwnerUserID");
                    if (StringHelper.IsNullOrEmpty(LocationOwnerUserID)) {
                        finish();
                        return;
                    }
                    LocationOwnerName = getIntent().getExtras().getString("LocationOwnerName");
                    LocationOwnerPicID = getIntent().getExtras().getString("LocationOwnerPicID");
                    Boolean isMyLocation = LocationOwnerUserID.equalsIgnoreCase(LoggedInUser.UserID);
                    setToolbarText(isMyLocation ? "My Location" : LocationOwnerName + "'s Location");
                    break;
                case SELECT_LOCATION:
                    setToolbarText("Select Location");
                    break;
                case NEW_EDIT_USER_LOCATION:
                    setToolbarText(mUserLocation != null ? mUserLocation.LocationNickName : "New Location");
                    gdValidationHelper = new GDValidationHelper(mContext, new ArrayList<>(Arrays.asList(LocationNickName)));
                    mMapLoadingProgressDialog = new ProgressDialog(GDMapActivity.this);
                    mMapLoadingProgressDialog.setCancelable(true);
                    mMapLoadingProgressDialog.setMessage("Loading map");
                    mMapLoadingProgressDialog.show();
                    break;
            }
            setUpMapIfNeeded();

            //Request for location changed updates
            mGPSLocationChanged = location -> MoveCameraOnLocationChanged(location);
            GPSHelper.AddLocationChangeListener(mGPSLocationChanged);

            HasActions = false;
            ShowTitleWithoutActions = true;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            finish();
        }
        postCreate();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.clear();
        GDAddMarker(latLng, LoggedInUserPicID);
        mUserLatLng = latLng;
        mMenu.getItem(0).setVisible(false);
        mMenu.getItem(1).setVisible(false);
        if (Activity_Mode == NEW_EDIT_USER_LOCATION || Activity_Mode == SELECT_LOCATION) {
            ProcessLatLngSelected(latLng);
        }
    }


    private void setUpMapIfNeeded() {
        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GDMap)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    if (mMap != null) {
                        setUpMap();
                    } else {
                        if (mMapLoadingProgressDialog != null) {
                            mMapLoadingProgressDialog.dismiss();
                        }
                        SendErrorLogToServer(null, "Map not initialized.");
                    }
                }
            });
        }
    }

    private void setUpMap() {
        if (mMap == null) {
            return;
        }
        switch (Activity_Mode) {
            case VIEW_LOCATION:
                GDAddMarker(mUserLatLng, LocationOwnerPicID);
                break;
            case VIEW_USER_LOCATION:
                LatLng latLng = null;
                if (!GPSHelper.IsInitiated()) {
                    GPSHelper.InitGPSHelper(GDMapActivity.this);
                }
                if (GPSHelper.CheckLocationAvailability(GDMapActivity.this)) {
                    latLng = GPSHelper.GetGPSLatLng(GDMapActivity.this);
                }
                Boolean isMyLocation = LocationOwnerUserID.equals(LoggedInUser.UserID);
                Marker marker = GDAddMarker(mUserLatLng, LocationOwnerPicID);
                if (!isMyLocation && latLng != null && marker != null) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(marker.getPosition());
                    marker = GDAddMarker(latLng, LoggedInUserPicID);
                    if(marker != null) {
                        builder.include(marker.getPosition());
                        mBounds = builder.build();
                    }
                }
                break;
            case SELECT_LOCATION:
                mMoveCameraToCurrentLocation = true;
                break;
            case NEW_EDIT_USER_LOCATION:
                break;
        }
        mMap.setOnMapLoadedCallback(() -> {
            switch (Activity_Mode) {
                case VIEW_LOCATION:
                case VIEW_USER_LOCATION:
                    if (mBounds != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 200));
                        mBounds = null;
                    } else {
                        MoveCameraOnLatLng(mUserLatLng);
                    }
                    break;
                case SELECT_LOCATION:
                    if (mUserLatLng == null) {
                        CheckLocationAvailabilityAndGoToCurrentLocation();
                    } else {
                        GDAddMarker(mUserLatLng, LoggedInUserPicID);
                        MoveCameraOnLatLng(mUserLatLng);
                    }
                    break;
                case NEW_EDIT_USER_LOCATION:
                    try {
                        mMapLoadingProgressDialog.dismiss();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (mUserLocation != null) {
                        GDAddMarker(mUserLatLng, LoggedInUserPicID);
                        MoveCameraOnLatLng(mUserLatLng);
                        GDMapNewLocation.setVisibility(View.VISIBLE);
                        SetUnsetValidation(true);
                        LocationNickName.setText(mUserLocation.LocationNickName);
                        ShowDotForCurrentLocation();
                    } else {
                        mMoveCameraToCurrentLocation = true;
                        CheckLocationAvailabilityAndGoToCurrentLocation();
                    }
                    break;
            }
            mMap.getUiSettings().setZoomControlsEnabled(true);
        });
        if (Activity_Mode == SELECT_LOCATION || Activity_Mode == NEW_EDIT_USER_LOCATION) {
            mMap.setOnMapClickListener(this);
        }
    }



    private void CheckLocationAvailabilityAndGoToCurrentLocation() {
        if (GPSHelper.CheckLocationAvailability(GDMapActivity.this)) {
            GoToCurrentLocation();
        } else {
            GPSHelper.AskToEnableLocation(GDMapActivity.this, this, 1);
        }
    }

    private void GoToCurrentLocation() {
        LatLng latLng = GPSHelper.GetGPSLatLng(GDMapActivity.this);
        if (latLng != null) {
            ShowDotForCurrentLocation();
            MoveCameraOnLatLng(latLng);
        }
    }

    private void ShowDotForCurrentLocation() {
        if (mMap == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    private void MoveCameraOnLocationChanged(Location location) {
        if (location == null || !mMoveCameraToCurrentLocation) {
            return;
        }
        mMoveCameraToCurrentLocation = false;
        MoveCameraOnLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void MoveCameraOnLatLng(LatLng latLng) {
        if (mMap == null) {
            return;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom((float) 12).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }




    private Marker GDAddMarker(LatLng latLng, String picID) {
        if (mMap == null) {
            return null;
        }
        MarkerOptions markerOptions;
        if (Activity_Mode == SELECT_LOCATION) {
            markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
            if (MarkerTitle != null && !MarkerTitle.equals("")) {
                markerOptions.title(MarkerTitle);
            }
            return mMap.addMarker(markerOptions);
        } else if (Activity_Mode != VIEW_LOCATION || Activity_Mode != VIEW_USER_LOCATION) {
            if (!StringHelper.IsNullOrEmpty(picID)) {
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                View CustomMarker = layoutInflater.inflate(R.layout.home_map_icon, null);
                ImageView ProfilePic = CustomMarker.findViewById(R.id.ProfilePic);
                String PicSrc = gdImageDBHelper.GetImageStringByPicID(picID, false);
                if (PicSrc != null && !PicSrc.trim().equals("")) {
                    ProfilePic.setImageBitmap(ImageHelper.GetBitmapFromString(PicSrc));
                }
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(ImageHelper.createDrawableFromView(mContext, CustomMarker)));
                return mMap.addMarker(markerOptions);
            }
        } else {
            markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
            if (MarkerTitle != null && !MarkerTitle.equals("")) {
                markerOptions.title(MarkerTitle);
            }
            return mMap.addMarker(markerOptions);
        }
        return null;
    }

    private void ProcessLatLngSelected(LatLng latLng) {
        try {
            if (latLng == null) {
                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Could not get location. Please try again.", TopSnackBar.LENGTH_SHORT, true).show();
                mMap.clear();
                return;
            }
            mMenu.getItem(0).setVisible(true);
            mMenu.getItem(1).setVisible(true);
            if (Activity_Mode == NEW_EDIT_USER_LOCATION) {
                GDMapNewLocation.setVisibility(View.VISIBLE);
                SetUnsetValidation(true);
                LocationNickName.setText(mUserLocation == null ? "" : mUserLocation.LocationNickName);
                if (LocationNickName.getText().toString().trim().equals("")) {
                    LocationNickName.requestFocus();
                    GDGenericHelper.ShowKeyboard(GDMapActivity.this, LocationNickName);
                    LocationNickName.setOnKeyListener((v, keyCode, event) -> {
                        try {
                            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                if (!LocationNickName.getText().toString().trim().equals("")) {
                                    ShowToolTip("Save location");
                                }
                                return true;
                            }
                            return false;
                        } catch (Exception ex) {
                            return false;
                        }
                    });
                } else {
                    ShowToolTip("Save location");
                }
            } else if (Activity_Mode == SELECT_LOCATION) {
                if (TooltipMessage != null && !TooltipMessage.trim().equals("")) {
                    ShowToolTip(TooltipMessage);
                }
            }
            if (mUserLocation == null) {
                mUserLocation = new UserLocation(GDGenericHelper.GetNewGUID(), "", GPSHelper.GetStringFromLatLng(latLng),
                        LoggedInUser == null ? GDGenericHelper.GetNewGUID() : LoggedInUser.UserID, false);
            } else {
                mUserLocation.LocationLatLng = GPSHelper.GetStringFromLatLng(latLng);
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }


    private void SetUnsetValidation(Boolean Set) {
        if (Set) {
            gdValidationHelper.RemoveValidators(0);
            gdValidationHelper.AddNonEmptyValidator(0).AddCharRangeValidator(0, 3, 20).
                    AddRegexValidator(0, GDValidationHelper.iAlphaNumericStartWithLetter);
            gdValidationHelper.UpdateFormValidators();
        } else {
            gdValidationHelper.RemoveValidators(0);
            gdValidationHelper.UpdateFormValidators();
        }
    }

    private void SendErrorLogToServer(Exception ex, String ErrorMessage) {
        String ErrorData = "";
        try {
            if (ex != null) {
                StringWriter stringWriter = new StringWriter();
                ex.printStackTrace(new PrintWriter(stringWriter));
                ErrorData = stringWriter.toString();
            }
            if (ErrorMessage != null) {
                ErrorData += " ******** Message **********  " + ErrorMessage;
            }
            String userID = LoggedInUser == null ? GDGenericHelper.GetNewGUID() : LoggedInUser.UserID;
            APICallInfo apiCallInfo = new APICallInfo("Home", "UploadMapErrorLog", null, "POST",
                    new ErrorLog(userID, ErrorData), null, false, null, APICallInfo.APITimeouts.LONG);
            apiCallInfo.CalledFromService = true;
            GDGenericHelper.executeAsyncPOSTAPITask(GDMapActivity.this, apiCallInfo, (result, ExtraData) -> {
                try {
                    if (result != null && result.equals("1")) {
                        GDLogHelper.DeleteAllBackupLogs();
                    }
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
            }, null);
        } catch (Exception ex2) {
            GDLogHelper.LogException(ex2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (mMap != null) {
            Boolean permissionNotGranted = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
            if (!permissionNotGranted) {
                mMap.setMyLocationEnabled(true);
            }
        }
        CheckLocationPermissionAndInit();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GPSHelper.RemoveLocationChangeListener(mGPSLocationChanged);
        GPSHelper.DeInitGPSHelper();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_mapactivity, menu);
        if (mMenu != null) {
            mMenu.getItem(0).setVisible(false);
            mMenu.getItem(1).setVisible(false);
        }
        if (mUserLocation != null || (Activity_Mode == SELECT_LOCATION && mUserLatLng != null)) {
            mMenu.getItem(0).setVisible(true);
            mMenu.getItem(1).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                return (super.onOptionsItemSelected(menuItem));
            case R.id.action_logout:
                GDLogHelper.Log(LogClass, "onOptionsItemSelected", "User deleted account. Logout.");
                SessionManager.UserLogout(GDMapActivity.this, LoggedInUser);
                break;
            case R.id.action_Cancel:
                if (mMap == null) {
                    return true;
                }
                if (Activity_Mode == SELECT_LOCATION || Activity_Mode == NEW_EDIT_USER_LOCATION) {
                    mUserLatLng = null;
                    mMenu.getItem(0).setVisible(false);
                    mMenu.getItem(1).setVisible(false);
                    mMap.clear();
                    if (Activity_Mode == NEW_EDIT_USER_LOCATION) {
                        gdValidationHelper.UpdateFormValidators();
                        GDMapNewLocation.setVisibility(View.GONE);
                        SetUnsetValidation(false);
                    }
                }
                return true;
            case R.id.action_Done:
                if (mMap == null) {
                    return true;
                }
                if (Activity_Mode == NEW_EDIT_USER_LOCATION) {
                    if (!gdValidationHelper.Validate()) {
                        TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), "Please enter a name for location.", TopSnackBar.LENGTH_SHORT, true).show();
                        return true;
                    }
                    mUserLocation.LocationNickName = LocationNickName.getText().toString();
                    APICallInfo apiCallInfo = new APICallInfo("CompleteProfile", "InsertUserLocation", null, "POST", mUserLocation, mUserLocation, false,
                            new APIProgress(GDMapActivity.this, "Saving..", true), APICallInfo.APITimeouts.SEMILONG);
                    GDGenericHelper.executeAsyncPOSTAPITask(mContext, apiCallInfo, new APICallback() {
                        @Override
                        public void onAPIComplete(String result, Object ExtraData) {
                            try {
                                SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                                if (successResult.SuccessResult == 1) {
                                    GDToastHelper.ShowToast(mContext, "Location saved", GDToastHelper.INFO, GDToastHelper.SHORT);
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("Location", mUserLocation);
                                    if (getParent() == null) {
                                        setResult(Activity.RESULT_OK, returnIntent);
                                    } else {
                                        getParent().setResult(Activity.RESULT_OK, returnIntent);
                                    }
                                    finish();
                                } else if (successResult.SuccessResult == Integer.parseInt(getResources().getString(R.string.user_limit_error_code))) {
                                    GDGenericHelper.ShowBuyPremiumIfNotPremium(mContext, successResult.FailureMessage, true);
                                } else {
                                    TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), successResult.FailureMessage, TopSnackBar.LENGTH_SHORT, true).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                GDLogHelper.LogException(e);
                                TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.generic_error_message), TopSnackBar.LENGTH_SHORT, true).show();
                            }
                        }
                    }, new APINoNetwork() {
                        @Override
                        public void onAPINoNetwork() {
                            TopSnackBar.MakeSnackBar(findViewById(R.id.BodyBelowAppBar), getString(R.string.no_network_connection), TopSnackBar.LENGTH_SHORT, true).show();
                        }
                    });
                } else if (Activity_Mode == SELECT_LOCATION) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("UserLocation", mUserLocation);
                    if (getParent() == null) {
                        setResult(Activity.RESULT_OK, returnIntent);
                    } else {
                        getParent().setResult(Activity.RESULT_OK, returnIntent);
                    }
                    finish();
                } else {
                    mUserLatLng = null;
                    mMenu.getItem(0).setVisible(false);
                    mMenu.getItem(1).setVisible(false);
                    mMap.clear();
                }
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                //CheckLocationAvailabilityAndInit();
                break;
            case 2:
                if (GPSHelper.IsLocationPermissionGranted(GDMapActivity.this)) {
                    GPSHelper.InitGPSHelper(GDMapActivity.this);
                    CheckLocationAvailabilityAndGoToCurrentLocation();
                }
                break;
        }
    }

    private void ShowToolTip(String message) {
        Tooltip.TooltipView AttachImagesTooltip = Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(findViewById(R.id.action_Done), Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 5000)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(message)
                        .maxWidth(350)
                        .withArrow(true)
                        .withOverlay(true)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        );
        AttachImagesTooltip.show();
        AttachImagesTooltip.setTextColor(Color.WHITE);
    }

    private void CheckLocationPermissionAndInit() {
        if (GPSHelper.IsLocationPermissionGranted(GDMapActivity.this)) {
            GPSHelper.InitGPSHelper(GDMapActivity.this);
        } else {
            GPSHelper.AskLocationPermission(GDMapActivity.this, 2);
        }
    }

    class ErrorLog {
        public String UserID;
        public String LogData;

        public ErrorLog(String vUserID, String vLogData) {
            UserID = vUserID;
            LogData = vLogData;
        }
    }
}
