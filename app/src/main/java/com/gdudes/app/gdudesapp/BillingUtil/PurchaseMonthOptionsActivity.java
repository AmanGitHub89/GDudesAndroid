package com.gdudes.app.gdudesapp.BillingUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.gdudes.app.gdudesapp.Helpers.UserObjectsCacheHelper;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.R;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class PurchaseMonthOptionsActivity extends BillPurchaseBaseActivity {

    Context mContext;
    Users LoggedInUser;
    String SKUTypeCode;
    String PremiumFeatureID;
    String mLogClassName = "PurchaseMonthOptionsActivity";

    ImageView btnClose;
    RelativeLayout PurchaseMonthMainContainer;
    LinearLayout OneYear;
    LinearLayout SixMonth;
    LinearLayout ThreeMonth;
    LinearLayout OneMonth;

    TextView Text12Months;
    TextView Text6Months;
    TextView Text3Months;
    TextView Text1Months;

    OnSuccessfulPurchase onSuccessfulPurchase;
    OnSuccessfulConsume onSuccessfulConsume;
    Boolean UnUsedPurchaseChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            if (getIntent().hasExtra("SKUTypeCode")) {
                SKUTypeCode = getIntent().getExtras().getString("SKUTypeCode", "");
                if (SKUTypeCode == null || SKUTypeCode.trim().equals("") ||
                        (!SKUTypeCode.equals("UP") && !SKUTypeCode.equals("CP") && !SKUTypeCode.equals("PP"))) {
                    FinishWithSuccessCode(false);
                }
            } else {
                FinishWithSuccessCode(false);
            }
            setContentView(SKUTypeCode.equals("UP") ? R.layout.activity_purchase_month_options_subscription
                    : R.layout.activity_purchase_month_options);
            Text12Months = (TextView) findViewById(R.id.Text12Months);
            Text6Months = (TextView) findViewById(R.id.Text6Months);
            Text3Months = (TextView) findViewById(R.id.Text3Months);
            Text1Months = (TextView) findViewById(R.id.Text1Months);
            HighlightPrice(Text12Months);
            HighlightPrice(Text6Months);
            HighlightPrice(Text3Months);
            HighlightPrice(Text1Months);

            if (getIntent().hasExtra("PremiumFeatureID")) {
                PremiumFeatureID = getIntent().getExtras().getString("PremiumFeatureID", "");
            }

            mContext = PurchaseMonthOptionsActivity.this;
            LoggedInUser = SessionManager.GetLoggedInUser(mContext);

            btnClose = (ImageView) findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            PurchaseMonthMainContainer = (RelativeLayout) findViewById(R.id.PurchaseMonthMainContainer);
            OneYear = (LinearLayout) findViewById(R.id.OneYear);
            SixMonth = (LinearLayout) findViewById(R.id.SixMonth);
            ThreeMonth = (LinearLayout) findViewById(R.id.ThreeMonth);
            OneMonth = (LinearLayout) findViewById(R.id.OneMonth);
            OneYear.setTag(12);
            SixMonth.setTag(6);
            ThreeMonth.setTag(3);
            OneMonth.setTag(1);
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String SKU = GetSkuForThisPurchase(Integer.parseInt(v.getTag().toString()));
                        if (SKU == null || SKU.trim().equals("")) {
                            FinishWithSuccessCode(false);
                            return;
                        }
                        //User selected 1/3/6/12 month premium
                        if (SKU != null && !SKU.equals("")) {
                            Boolean IsSubscription = IsSubscription(SKUTypeCode, SKU);
                            LaunchPurchaseFlow(SKU, onSuccessfulPurchase, onSuccessfulConsume, IsSubscription ? ItemType.subs : ItemType.inapp);
                        }
                    } catch (Exception ex) {
                        FinishWithSuccessCode(false);
                    }
                }
            };
            OneYear.setOnClickListener(onClickListener);
            SixMonth.setOnClickListener(onClickListener);
            ThreeMonth.setOnClickListener(onClickListener);
            OneMonth.setOnClickListener(onClickListener);
            SetCallbacks();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            GDLogHelper.UploadErrorLogsToServer(getApplicationContext(), true);
            FinishWithSuccessCode(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (UnUsedPurchaseChecked) {
                return;
            }
            UnUsedPurchaseChecked = true;
            OnSuccessfulBillingSetup SuccessfulBillingSetup = new OnSuccessfulBillingSetup() {
                @Override
                public void SuccessfulBillingSetup() {
                    final ProgressDialog dialog = new ProgressDialog(mContext);
                    dialog.setMessage("Checking your purchase history. Please wait..");
                    dialog.setCancelable(false);
                    dialog.show();
                    CheckPendingConsumesForPurchaseType(SKUTypeCode, new OnInitialQueryInventoryFinished() {
                        @Override
                        public void OnQueryInventoryFinished(Boolean HasItemToConsume, final Boolean IsSubscription,
                                                             final String OrderId, final String TokenId,
                                                             String ItemTitle, final String SKU) {
                            dialog.dismiss();
                            if (HasItemToConsume) {
                                GDDialogHelper.ShowYesNoTypeDialog(mContext, "Un-used purchase found.",
                                        "You have an un-used purchase \n\n\"" + ItemTitle + "\"Do you want to apply this purchase?\nYou will not be charged.",
                                        GDDialogHelper.BUTTON_TEXT_YES, GDDialogHelper.BUTTON_TEXT_NO, GDDialogHelper.INFO, new OnDialogButtonClick() {
                                            @Override
                                            public void dialogButtonClicked() {
                                                if (IsSubscription) {
                                                    onSuccessfulPurchase.PurchaseSuccessful(OrderId, TokenId, SKU);
                                                    //GetTransactionIDForUnConsumedPurchase(OrderId, SKU);
                                                } else {
                                                    LaunchPurchaseFlow(SKU, onSuccessfulPurchase, onSuccessfulConsume, IsSubscription ? ItemType.subs : ItemType.inapp);
                                                }
                                            }
                                        }, null);
                            }
                        }
                    });
                }
            };
            SetupBilling(SuccessfulBillingSetup);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            FinishWithSuccessCode(false);
        }
    }

    private void GetTransactionIDForUnConsumedPurchase(String OrderId, final String SKU) {
        GDLogHelper.Log(mLogClassName, "GetTransactionIDForUnConsumedPurchase", "OrderId:" + OrderId, GDLogHelper.LogLevel.INFO);
        ArrayList<APICallParameter> pAPICallParameters = new ArrayList<APICallParameter>();
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_UserID, LoggedInUser.UserID));
        pAPICallParameters.add(new APICallParameter(APICallParameter.param_OrderID, OrderId));
        APICallInfo apiCallInfo = new APICallInfo("Home", "GetTransactionIDForLatestOrderID", pAPICallParameters, "GET", null, null, false,
                new APIProgress(PurchaseMonthOptionsActivity.this, "Processing your purchase. Please wait..", false), APICallInfo.APITimeouts.LONG);
        GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
            @Override
            public void onAPIComplete(String result, Object ExtraData) {
                try {
                    SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                    if (successResult != null && successResult.SuccessResult == 1) {
                        GDLogHelper.Log(mLogClassName, "GetTransactionIDForUnConsumedPurchase", "Got transactionID", GDLogHelper.LogLevel.INFO);
                    } else {
                        GDLogHelper.Log(mLogClassName, "GetTransactionIDForUnConsumedPurchase", "Could not Get transactionID. " + result, GDLogHelper.LogLevel.ERROR);
                    }

                    if (successResult == null) {
                        GDToastHelper.ShowToast(mContext, "Your purchase could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        FinishWithSuccessCode(false);
                    } else if (successResult.SuccessResult == -101) {
                        GDDialogHelper.ShowSingleButtonTypeDialog(mContext, "Invalid purchase",
                                "This purchase could not be verified.", GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.ERROR, new OnDialogButtonClick() {
                                    @Override
                                    public void dialogButtonClicked() {
                                        FinishWithSuccessCode(false);
                                    }
                                });
                    } else if (successResult.SuccessResult == 1) {
                        if (IsSubscription(SKUTypeCode, SKU)) {
                            onSuccessfulConsume.ConsumeSuccessful(successResult.FailureMessage);
                        } else {
                            ConsumeItem(successResult.FailureMessage);
                        }
                    } else {
                        GDToastHelper.ShowToast(mContext, "Your purchase could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        FinishWithSuccessCode(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                    GDToastHelper.ShowToast(mContext, "Your purchase could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
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

    private void SetCallbacks() {
        onSuccessfulPurchase = new OnSuccessfulPurchase() {
            @Override
            public void PurchaseSuccessful(String OrderId, String TokenID, final String SKU) {
                GDLogHelper.Log(mLogClassName, "OnSuccessfulPurchase", "OrderId:" + OrderId, GDLogHelper.LogLevel.INFO);
                String PremiumCode_SKU = "";
                if (SKU.equals(GDItemSKUs.GDPremium1Month) || SKU.equals(GDItemSKUs.GDClub1Month) || SKU.equals(GDItemSKUs.GDProfession1Month)) {
                    PremiumCode_SKU = "OM";
                } else if (SKU.equals(GDItemSKUs.GDPremiumSubscription3Month) || SKU.equals(GDItemSKUs.GDClub3Month) || SKU.equals(GDItemSKUs.GDProfession3Month)) {
                    PremiumCode_SKU = "TM";
                } else if (SKU.equals(GDItemSKUs.GDPremiumSubscription6Month) || SKU.equals(GDItemSKUs.GDClub6Month) || SKU.equals(GDItemSKUs.GDProfession6Month)) {
                    PremiumCode_SKU = "SM";
                } else if (SKU.equals(GDItemSKUs.GDPremiumSubscription12Month) || SKU.equals(GDItemSKUs.GDClub12Month) || SKU.equals(GDItemSKUs.GDProfession12Month)) {
                    PremiumCode_SKU = "OY";
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
                if (SKUTypeCode.equals("UP")) {
                    API = "Home";
                    ReqCode = "BuyExtendUserPremium";
                } else if (SKUTypeCode.equals("CP")) {
                    API = "Home";
                    ReqCode = "BuyExtendPremiumFeature";
                    pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureID, PremiumFeatureID));
                    pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureCode, "CL"));
                } else if (SKUTypeCode.equals("PP")) {
                    API = "Home";
                    ReqCode = "BuyExtendPremiumFeature";
                    pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureID, PremiumFeatureID));
                    pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureCode, "PL"));
                }
                APICallInfo apiCallInfo = new APICallInfo(API, ReqCode, pAPICallParameters, "GET", null, null, false,
                        new APIProgress(PurchaseMonthOptionsActivity.this, "Processing your purchase. Please wait..", false), APICallInfo.APITimeouts.LONG);
                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                    @Override
                    public void onAPIComplete(String result, Object ExtraData) {
                        try {
                            SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                            if (successResult != null && successResult.SuccessResult == 1) {
                                GDLogHelper.Log(mLogClassName, "OnSuccessfulPurchase", "Purchase processed. Consuming..", GDLogHelper.LogLevel.INFO);
                            } else {
                                GDLogHelper.Log(mLogClassName, "OnSuccessfulPurchase", "Purchase could not be processed. " + result, GDLogHelper.LogLevel.ERROR);
                            }

                            if (successResult.SuccessResult == -101) {
                                GDDialogHelper.ShowSingleButtonTypeDialog(mContext, "Invalid purchase",
                                        "This purchase item has already been used.", GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.ERROR, new OnDialogButtonClick() {
                                            @Override
                                            public void dialogButtonClicked() {
                                                FinishWithSuccessCode(false);
                                            }
                                        });
                            } else if (successResult.SuccessResult == 1) {
                                if (IsSubscription(SKUTypeCode, SKU)) {
                                    onSuccessfulConsume.ConsumeSuccessful(successResult.FailureMessage);
                                } else {
                                    ConsumeItem(successResult.FailureMessage);
                                }
                            } else {
                                GDToastHelper.ShowToast(mContext, "Your purchase could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                                FinishWithSuccessCode(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            GDLogHelper.LogException(e);
                            GDToastHelper.ShowToast(mContext, "Your purchase could not be processed.", GDToastHelper.ERROR, GDToastHelper.SHORT);
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
                if (SKUTypeCode.equals("UP")) {
                    pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureCode, "UL"));
                } else if (SKUTypeCode.equals("CP")) {
                    pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureCode, "CL"));
                } else if (SKUTypeCode.equals("PP")) {
                    pAPICallParameters.add(new APICallParameter(APICallParameter.param_PremiumFeatureCode, "PL"));
                }
                APICallInfo apiCallInfo = new APICallInfo("Home", "ConsumePurchaseItem", pAPICallParameters, "GET", null, null, false,
                        new APIProgress(PurchaseMonthOptionsActivity.this, "Processing your purchase. Please wait..", false), APICallInfo.APITimeouts.LONG);
                GDGenericHelper.executeAsyncAPITask(mContext, apiCallInfo, new APICallback() {
                    @Override
                    public void onAPIComplete(String result, Object ExtraData) {
                        try {
                            SuccessResult successResult = new GsonBuilder().create().fromJson(result, SuccessResult.class);
                            if (successResult != null && successResult.SuccessResult == 1) {
                                GDLogHelper.Log(mLogClassName, "OnSuccessfulPurchase", "Purchase consumed.", GDLogHelper.LogLevel.INFO);
                            } else {
                                GDLogHelper.Log(mLogClassName, "OnSuccessfulPurchase", "Purchase could not be consumed. " + result, GDLogHelper.LogLevel.ERROR);
                            }

                            if (successResult.SuccessResult == 1) {
                                if (SKUTypeCode.equals("UP")) {
                                    GDToastHelper.ShowToast(mContext, "Premium membership bought successfully", GDToastHelper.INFO, GDToastHelper.SHORT);
                                    LoggedInUser.IsPremium = true;
                                    SessionManager.UserLogIn(LoggedInUser);
                                    UserObjectsCacheHelper.AddUpdUserToCache(LoggedInUser);
                                    FinishWithSuccessCode(true);
                                } else {
                                    GDToastHelper.ShowToast(mContext, "Payment made successfully", GDToastHelper.INFO, GDToastHelper.SHORT);
                                    FinishWithSuccessCode(true);
                                }
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

    private String GetSkuForThisPurchase(int selectedMonths) {
        String SkuForSelection = "";
        switch (selectedMonths) {
            case 1:
                if (SKUTypeCode.equals("UP")) {
                    SkuForSelection = GDItemSKUs.GDPremium1Month;
                } else if (SKUTypeCode.equals("CP")) {
                    SkuForSelection = GDItemSKUs.GDClub1Month;
                } else if (SKUTypeCode.equals("PP")) {
                    SkuForSelection = GDItemSKUs.GDProfession1Month;
                }
                break;
            case 3:
                if (SKUTypeCode.equals("UP")) {
                    SkuForSelection = GDItemSKUs.GDPremiumSubscription3Month;
                } else if (SKUTypeCode.equals("CP")) {
                    SkuForSelection = GDItemSKUs.GDClub3Month;
                } else if (SKUTypeCode.equals("PP")) {
                    SkuForSelection = GDItemSKUs.GDProfession3Month;
                }
                break;
            case 6:
                if (SKUTypeCode.equals("UP")) {
                    SkuForSelection = GDItemSKUs.GDPremiumSubscription6Month;
                } else if (SKUTypeCode.equals("CP")) {
                    SkuForSelection = GDItemSKUs.GDClub6Month;
                } else if (SKUTypeCode.equals("PP")) {
                    SkuForSelection = GDItemSKUs.GDProfession6Month;
                }
                break;
            case 12:
                if (SKUTypeCode.equals("UP")) {
                    SkuForSelection = GDItemSKUs.GDPremiumSubscription12Month;
                } else if (SKUTypeCode.equals("CP")) {
                    SkuForSelection = GDItemSKUs.GDClub12Month;
                } else if (SKUTypeCode.equals("PP")) {
                    SkuForSelection = GDItemSKUs.GDProfession12Month;
                }
                break;
        }
        return SkuForSelection;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try {
            Point size = new Point();
            this.getWindowManager().getDefaultDisplay().getSize(size);
            Double dWidth = size.x * 0.8;
            getWindow().setLayout(dWidth.intValue(), PurchaseMonthMainContainer.getMeasuredHeight());
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_purchase_month_options, menu);
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

    private void HighlightPrice(TextView textView) {
        String TextToSet = textView.getText().toString();
        TextToSet = TextToSet.replaceAll("__", "<b><u>");
        TextToSet = TextToSet.replaceAll("--", "</u></b>");
        textView.setText(Html.fromHtml(TextToSet));
    }
}
