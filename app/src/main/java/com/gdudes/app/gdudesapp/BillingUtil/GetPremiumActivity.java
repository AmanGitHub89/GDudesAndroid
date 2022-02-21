package com.gdudes.app.gdudesapp.BillingUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.R;

public class GetPremiumActivity extends GDAppCompatActivity {

    Context mContext;
    Users LoggedInUser;
    String LimitExceedMessage = "You have exceeded your limit. Please buy premium membership to continue.";
    Boolean ShowLimitExceedMessage = true;
    TextView LimitText = null;
    TextView AllPremiumBenefits = null;
    Button btnGetPremium = null;
    ImageView btnClose;
    Boolean PurchaseFlowStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_get_premium);
            mContext = GetPremiumActivity.this;
            LoggedInUser = SessionManager.GetLoggedInUser(mContext);
            if (getIntent().hasExtra("LimitExceedMessage")) {
                LimitExceedMessage = StringEncoderHelper.decodeURIComponent
                        (getIntent().getExtras().getString("LimitExceedMessage", LimitExceedMessage));
            } else if (getIntent().hasExtra("ShowLimitExceedMessage")) {
                ShowLimitExceedMessage = getIntent().getExtras().getBoolean("ShowLimitExceedMessage", true);
            }
            LimitText = findViewById(R.id.LimitText);
            AllPremiumBenefits = findViewById(R.id.AllPremiumBenefits);
            btnGetPremium = findViewById(R.id.btnGetPremium);
            btnClose = findViewById(R.id.btnClose);
            btnClose.setOnClickListener(v -> finish());

            String AllPremiumBenefitsStr = getResources().getString(R.string.all_premium_benefits);
            AllPremiumBenefitsStr = AllPremiumBenefitsStr.replace("__", "<u><b>");
            AllPremiumBenefitsStr = AllPremiumBenefitsStr.replace("^^", "</u></b>");
            AllPremiumBenefitsStr = AllPremiumBenefitsStr.replace("..", "<br>");
            AllPremiumBenefits.setText(Html.fromHtml(AllPremiumBenefitsStr));
            LimitText.setText(LimitExceedMessage);
            if (!ShowLimitExceedMessage) {
                LimitText.setVisibility(View.GONE);
            }

            btnGetPremium.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, PurchaseMonthOptionsActivity.class);
                intent.putExtra("SKUTypeCode", "UP"); //User Premium
                startActivityForResult(intent, 11);
                PurchaseFlowStarted = true;
            });
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            GDLogHelper.UploadErrorLogsToServer(getApplicationContext(), true);
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        Double dWidth = size.x * 0.85;
        Double dHeight = size.y * 0.75;
        getWindow().setLayout(dWidth.intValue(), dHeight.intValue());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PurchaseFlowStarted) {
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 11) {
            if (data.hasExtra("Success") && data.getExtras().getBoolean("Success", false)) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Success", true);
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, returnIntent);
                } else {
                    getParent().setResult(Activity.RESULT_OK, returnIntent);
                }
                finish();
            }
        } else {
            finish();
        }
    }

}
