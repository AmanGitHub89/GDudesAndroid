package com.gdudes.app.gdudesapp.Helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gdudes.app.gdudesapp.Interfaces.GPSLocationChanged;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class GPSHelper {
    private static LocationManager locationManager;
    private static Location mLocation = null;
    private static LocationListener locationListener = null;
    private static Boolean IsInstantiated = false;
    private static ArrayList<GPSLocationChanged> GPSLocationChangedListeners = null;
    private static final String LogClass = "GPSHelper";

    public static Boolean IsInitiated() {
        return IsInstantiated;
    }

    @SuppressLint("MissingPermission")
    public static void InitGPSHelper(Context context) {
        if (IsInstantiated) {
            return;
        }
        if (!IsLocationPermissionGrantedByContext(context)) {
            return;
        }
        try {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLocation = location;
                    if (GPSLocationChangedListeners != null) {
                        for (int i = 0; i < GPSLocationChangedListeners.size(); i++) {
                            GPSLocationChangedListeners.get(i).LocationChanged(location);
                        }
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            String bestProvider = null;
            for (String provider : providers) {
                Location loc = locationManager.getLastKnownLocation(provider);
                if (loc == null) {
                    continue;
                }
                if (bestLocation == null || loc.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestProvider = provider;
                    bestLocation = loc;
                }
            }
            mLocation = bestLocation;

            if (bestProvider != null) {
                locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
            }
            if (GPSLocationChangedListeners == null) {
                GPSLocationChangedListeners = new ArrayList<>();
            }
            IsInstantiated = true;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public static void ForceInitGPSHelper(Context context) {
        IsInstantiated = false;
        locationListener = null;
        InitGPSHelper(context);
    }

    public static void DeInitGPSHelper() {
        if (!IsInstantiated) {
            return;
        }
        try {
            IsInstantiated = false;
            locationManager.removeUpdates(locationListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Location GetGPSLocation(Context context) {
        if (!IsInstantiated) {
            InitGPSHelper(context);
        }
        if (mLocation == null) {
            try {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (mLocation == null) {
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            } catch (SecurityException ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
            }
        }
        return mLocation;
    }

    public static LatLng GetGPSLatLng(Context context) {
        if (!IsInstantiated) {
            InitGPSHelper(context);
        }
        LatLng latLng = null;
        try {
            Location location = GetGPSLocation(context);
            if (location != null) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return latLng;
    }

    //*********************************************************************************************
    public static void AddLocationChangeListener(GPSLocationChanged gpsLocationChanged) {
        if (GPSLocationChangedListeners == null) {
            GPSLocationChangedListeners = new ArrayList<>();
        }
        GPSLocationChangedListeners.add(gpsLocationChanged);
    }

    public static void RemoveLocationChangeListener(GPSLocationChanged gpsLocationChanged) {
        if (gpsLocationChanged == null) {
            return;
        }
        if (GPSLocationChangedListeners == null) {
            GPSLocationChangedListeners = new ArrayList<>();
        }
        GPSLocationChangedListeners.remove(gpsLocationChanged);
    }

    //*********************************************************************************************
    public static Boolean CheckLocationAvailability(Context context) {
        Boolean bLocationAvailable = false;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;
            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
            }

            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
            }
            if (gps_enabled || network_enabled) {
                bLocationAvailable = true;
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return bLocationAvailable;
    }

    public static void AskToEnableLocation(Context context, final Activity ActivityInstance, final int startCode) {
        GDDialogHelper.ShowSingleButtonTypeDialog(context, "Please Enable location", "GDudes needs to access your location",
                GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.ALERT, new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        ActivityInstance.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), startCode);
                    }
                });
    }

    public static Boolean IsLocationPermissionGranted(Activity ActivityInstance) {
        return IsLocationPermissionGrantedByContext(ActivityInstance);
    }

    public static Boolean IsLocationPermissionGrantedByContext(Context context) {
        try {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return false;
    }

    public static void AskLocationPermission(final Activity ActivityInstance, final int startCode) {
        try {
            ActivityCompat.requestPermissions(ActivityInstance, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    startCode);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    //******************************************************************
    public static LatLng GetLatLngFromString(String LocationLatLng) {
        LatLng latLng = null;
        try {
            List<String> Location = StringHelper.SplitStringByComma(LocationLatLng);
            latLng = new LatLng(Double.parseDouble(Location.get(0)), Double.parseDouble(Location.get(1)));
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            GDLogHelper.Log(LogClass, "GetLatLngFromString", "IndexOutOfBoundsException - " + (LocationLatLng == null ? "" : LocationLatLng), GDLogHelper.LogLevel.ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return latLng;
    }

    public static String GetStringFromLatLng(LatLng latLng) {
        try {
            return Double.toString(latLng.latitude) + "," + Double.toString(latLng.longitude);
        } catch (Exception ex) {
            return "";
        }
    }

    public static String GetStringFromLocation(Location location) {
        try {
            return Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());
        } catch (Exception ex) {
            return "";
        }
    }
}
