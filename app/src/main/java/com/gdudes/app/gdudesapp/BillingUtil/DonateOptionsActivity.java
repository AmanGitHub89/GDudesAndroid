package com.gdudes.app.gdudesapp.BillingUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.APICaller.APICallback;
import com.gdudes.app.gdudesapp.APICaller.APIConstants.APICallParameter;
import com.gdudes.app.gdudesapp.APICaller.APINoNetwork;
import com.gdudes.app.gdudesapp.APICaller.APIProgress;
import com.gdudes.app.gdudesapp.GDTypes.SuccessResult;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.R;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class DonateOptionsActivity extends BillPurchaseBaseActivity {

    Context mContext;
    Users LoggedInUser;

    ImageView btnClose;
    RelativeLayout DonateMainContainer;
    LinearLayout Donate100Dollar;
    LinearLayout Donate200Dollar;
    LinearLayout Donate300Dollar;

    OnSuccessfulPurchase onSuccessfulPurchase;
    OnSuccessfulConsume onSuccessfulConsume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_options);

        mContext = DonateOptionsActivity.this;
        LoggedInUser = SessionManager.GetLoggedInUser(mContext);

        btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        DonateMainContainer = (RelativeLayout) findViewById(R.id.DonateMainContainer);
        Donate100Dollar = (LinearLayout) findViewById(R.id.Donate100Dollar);
        Donate200Dollar = (LinearLayout) findViewById(R.id.Donate200Dollar);
        Donate300Dollar = (LinearLayout) findViewById(R.id.Donate300Dollar);
        Donate300Dollar.setTag(300);
        Donate200Dollar.setTag(200);
        Donate100Dollar.setTag(100);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String SKU = GetSkuForThisPurchase(Integer.parseInt(v.getTag().toString()));
                    if (SKU == null || SKU.trim().equals("")) {
                        FinishWithSuccessCode(false);
                        return;
                    }
                    LaunchPurchaseFlow(SKU, onSuccessfulPurchase, onSuccessfulConsume, ItemType.inapp);
                } catch (Exception ex) {
                    FinishWithSuccessCode(false);
                }
            }
        };
        Donate300Dollar.setOnClickListener(onClickListener);
        Donate200Dollar.setOnClickListener(onClickListener);
        Donate100Dollar.setOnClickListener(onClickListener);
        SetCallbacks();
    }

    @Override
    public void onResume() {
        super.onResume();
        SetupBilling(new OnSuccessfulBillingSetup() {
            @Override
            public void SuccessfulBillingSetup() {
                //Do nothing. Do not check for unconsumed purchases for donation.
                //If user has bought the item it will be in his inventory. But don't check it. Fuck it get more money from him if he's willing to donate.
            }
        });
    }

    private void SetCallbacks() {
        onSuccessfulPurchase = new OnSuccessfulPurchase() {
            @Override
            public void PurchaseSuccessful(String OrderId, String TokenID, String SKU) {
                String PremiumCode_SKU = "";
                if (SKU.equals(GDItemSKUs.Donate300Dollar)) {
                    PremiumCode_SKU = "D3";
                } else if (SKU.equals(GDItemSKUs.Donate200Dollar)) {
                    PremiumCode_SKU = "D2";
                } else if (SKU.equals(GDItemSKUs.Donate100Dollar)) {
                    PremiumCode_SKU = "D1";
                }
                if (PremiumCode_SKU.equals("")) {
                    FinishWithSuccessCode(false);
                    return;
                }
                String API = "";
                String ReqCode = "";
                ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_OrderID, OrderId));
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_TokenID, TokenID));
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumCode, PremiumCode_SKU));
                API = "Home";
                ReqCode = "BuyExtendPremiumFeature";
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureID, LoggedInUser.UserID));
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureCode, "DD"));

                APICallInfo apiCallInfo = new APICallInfo(API, ReqCode, pAPICallParameters, "GET", null, null, false,
                        new APIProgress(DonateOptionsActivity.this, "Processing your donation. Please wait..", false), APICallInfo.APITimeouts.LONG);
                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                    @Override
                    public void onAPIComplete(String result, Object ExtraData) {
                        try {
                            SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                            if (successResult.SuccessResult == -101) {
                                GDDialogHelper.ShowSingleButtonTypeDialog(mContext, "Invalid purchase",
                                        "This purchase item has already been used.", GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.ERROR, new OnDialogButtonClick() {
                                            @Override
                                            public void dialogButtonClicked() {
                                                FinishWithSuccessCode(false);
                                            }
                                        });
                            } else if (successResult.SuccessResult == 1) {
                                ConsumeItem(successResult.FailureMessage);
                            } else {
                                GDToastHelper.ShowToast(mContext, "Your donation could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            GDLogHelper.LogException(e);
                            GDToastHelper.ShowToast(mContext, "Your donation could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                            FinishWithSuccessCode(false);
                        }
                    }
                }, new APINoNetwork() {
                    @Override
                    public void onAPINoNetwork() {
                        GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        FinishWithSuccessCode(false);
                    }
                });
            }
        };
        onSuccessfulConsume = new OnSuccessfulConsume() {
            @Override
            public void ConsumeSuccessful(String TransactionID) {
                ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_TransactionID, TransactionID));
                pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureCode, "DD"));
                APICallInfo apiCallInfo = new APICallInfo("Home", "ConsumePurchaseItem", pAPICallParameters, "GET", null, null, false,
                        new APIProgress(DonateOptionsActivity.this, "Processing your donation. Please wait..", false), APICallInfo.APITimeouts.LONG);
                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                    @Override
                    public void onAPIComplete(String result, Object ExtraData) {
                        try {
                            SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                            if (successResult.SuccessResult == 1) {
                                GDToastHelper.ShowToast(mContext, "Donation made successfully. Thank you. Our team will contact you soon.", GDToastHelper.INFO, GDToastHelper.LONG);
                                FinishWithSuccessCode(true);
                            } else {
                                //GDToastHelper.ShowToast(mContext, "Your purchase could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                                FinishWithSuccessCode(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            GDLogHelper.LogException(e);
                            //GDToastHelper.ShowToast(mContext, "Your purchase could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                            FinishWithSuccessCode(false);
                        }
                    }
                }, new APINoNetwork() {
                    @Override
                    public void onAPINoNetwork() {
                        GDToastHelper.ShowToast(mContext, "No network connection detected", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        FinishWithSuccessCode(false);
                    }
                });
            }
        };
    }

    private String GetSkuForThisPurchase(int amount) {
        String SkuForSelection = "";
        switch (amount) {
            case 300:
                SkuForSelection = GDItemSKUs.Donate300Dollar;
                break;
            case 200:
                SkuForSelection = GDItemSKUs.Donate200Dollar;
                break;
            case 100:
                SkuForSelection = GDItemSKUs.Donate100Dollar;
                break;
        }
        return SkuForSelection;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_donate_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try {
            int width = this.getWindowManager().getDefaultDisplay().getWidth();
            //int height = this.getWindowManager().getDefaultDisplay().getHeight();
            Double dWidth = width * 0.8;
            //Double dHeight = height * 0.6;
            getWindow().setLayout(dWidth.intValue(), DonateMainContainer.getMeasuredHeight());
//        getWindow().setLayout(PurchaseMonthMainContainer.getMeasuredWidth(), PurchaseMonthMainContainer.getMeasuredHeight());
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    private void FinishWithSuccessCode(Boolean success) {
        try {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("Success", success);
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, returnIntent);
            } else {
                getParent().setResult(Activity.RESULT_OK, returnIntent);
            }
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            finish();
        }
    }
}
