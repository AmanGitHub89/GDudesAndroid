package com.gdudes.app.gdudesapp.activities.LoginRegister;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GPSHelper;
import com.gdudes.app.gdudesapp.Helpers.PersistantPreferencesHelper;
import com.gdudes.app.gdudesapp.Interfaces.GetFileWritePermission;
import com.gdudes.app.gdudesapp.R;

public class RequestPermissionsActivity extends AppCompatActivity {

    Context mContext;
    GetFileWritePermission GetFileWritePermission;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_permissions);
        btnContinue = findViewById(R.id.btnContinue);
        mContext = getApplicationContext();

        GetFileWritePermission = () -> {
            PersistantPreferencesHelper.SetFileWritePermission("1");
            CheckLocationPermission();
        };

        btnContinue.setOnClickListener(v -> {
            CheckStoragePermission(false);
        });
    }

    public static Boolean HasAllPermissions(Activity activityInstance) {
        return HasFileWritePermission(activityInstance) && GPSHelper.IsLocationPermissionGranted(activityInstance);
    }

    private void CheckStoragePermission(Boolean Denied) {
        if (Denied) {
            GDDialogHelper.ShowSingleButtonTypeDialog(RequestPermissionsActivity.this, "Need Access permission",
                    "GDudes needs storage access permission to continue.",
                    GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.ALERT, () -> CheckStoragePermission(false));
        } else {
            if (HasFileWritePermission(RequestPermissionsActivity.this)) {
                GetFileWritePermission.OnPermissionGranted();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private static Boolean HasFileWritePermission(Activity activityInstance) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        return PersistantPreferencesHelper.GetFileWritePermission().equals("1") &&
                activityInstance.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void CheckLocationPermission() {
        if (!GPSHelper.IsLocationPermissionGranted(RequestPermissionsActivity.this)) {
            GPSHelper.AskLocationPermission(RequestPermissionsActivity.this, 2);
        } else {
            FinishWithResult();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GetFileWritePermission.OnPermissionGranted();
            } else {
                if (HasFileWritePermission(RequestPermissionsActivity.this)) {
                    GetFileWritePermission.OnPermissionGranted();
                } else {
                    CheckStoragePermission(true);
                }
            }
        } else if (requestCode == 2) {
            FinishWithResult();
        }
    }

    private void FinishWithResult() {
        Intent returnIntent = new Intent();
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
    }
}
