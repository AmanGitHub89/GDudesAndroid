package com.gdudes.app.gdudesapp.BillingUtil;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.GDToastHelper;

import java.util.List;

public class BillPurchaseBaseActivity extends AppCompatActivity {
    private final String Base64EncodedPublicString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmnxcpnknYqY4dXk9r8VVng+zIXm4eKNV06xXUW4drUqPs1HMoCxLyuFxki/D4p1/T/UYL2AniWIilXnzwJ6WnBxVkvW6trDetIBpolcanuzE2kq/lbY/mSrUJLbhRQTPqXB4S9mhTe1C/KnTIvYLDgkKM95CFJtHwpJnseVwxgIkgKrTV1jgBZaBp8jt8e2Stfv/Qvap0Y3lh7bJkyhBctIP2KBHXvgcMNIsJK6h0ljWzwPm/24NArumO00c2VYW5qxax5bAeIL07wMv5r6vHYdNWYHzXN6zw4ZJKOI3Souat//sifgS1eBvqOfpp5xKifKJ6F/KIUIyovpzy77s9wIDAQAB";
    private IabHelper iabHelper;
    private Context mContext;
    private String SKUItem;
    private OnSuccessfulPurchase mOnSuccessfulPurchase;
    private OnSuccessfulConsume mOnSuccessfulConsume;
    private Inventory inventory;
    private IabHelper.QueryInventoryFinishedListener queryInventoryFinishedListener;

    protected enum ItemType {
        inapp,
        subs
    }


    public BillPurchaseBaseActivity() {
        mContext = BillPurchaseBaseActivity.this;
    }

    public void SetupBilling(final OnSuccessfulBillingSetup SuccessfulBillingSetup) {
        iabHelper = new IabHelper(mContext, Base64EncodedPublicString);
        //If billing setup properly then start the Purchase Flow
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                try {
                    if (!result.isSuccess()) {
                        GDToastHelper.ShowToast(mContext, "Billing could not be started. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        finish();
                    } else {
                        if (SuccessfulBillingSetup != null) {
                            SuccessfulBillingSetup.SuccessfulBillingSetup();
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    GDLogHelper.LogException(ex);
                    finish();
                }
            }
        });
    }

    //Check for earlier purchases
    public void CheckPendingConsumesForPurchaseType(final String SKUTypeCode, final OnInitialQueryInventoryFinished OnQueryInventoryFinished) {
        iabHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                                          @Override
                                          public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                                              UnusedPurchase unusedPurchase = null;
                                              try {
                                                  List<Purchase> AllPurchases = inv.getAllPurchases();
                                                  if (AllPurchases.size() > 0) {
                                                      //User has items purchased that are not yet consumed. Get the lowest period item for this purchase type and return.
                                                      switch (SKUTypeCode) {
                                                          case "UP":
                                                              unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDPremium1Month, inv);
                                                              if (!unusedPurchase.ContainsItem) {
                                                                  unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDPremiumSubscription3Month, inv);
                                                                  if (!unusedPurchase.ContainsItem) {
                                                                      unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDPremiumSubscription6Month, inv);
                                                                      if (!unusedPurchase.ContainsItem) {
                                                                          unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDPremiumSubscription12Month, inv);
                                                                      }
                                                                  }
                                                              }
                                                              break;
                                                          case "CP":
                                                              unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDClub1Month, inv);
                                                              if (!unusedPurchase.ContainsItem) {
                                                                  unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDClub3Month, inv);
                                                                  if (!unusedPurchase.ContainsItem) {
                                                                      unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDClub6Month, inv);
                                                                      if (!unusedPurchase.ContainsItem) {
                                                                          unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDClub12Month, inv);
                                                                      }
                                                                  }
                                                              }
                                                              break;
                                                          case "PP":
                                                              unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDProfession1Month, inv);
                                                              if (!unusedPurchase.ContainsItem) {
                                                                  unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDProfession3Month, inv);
                                                                  if (!unusedPurchase.ContainsItem) {
                                                                      unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDProfession6Month, inv);
                                                                      if (!unusedPurchase.ContainsItem) {
                                                                          unusedPurchase = ContainsSKU(AllPurchases, GDItemSKUs.GDProfession12Month, inv);
                                                                      }
                                                                  }
                                                              }
                                                              break;
                                                          //Not being used for Donations currently
                                                          case "DD":
//                                                              unusedPurchase = ContainsSKU(NonConsumedSKUAndTitles, GDItemSKUs.Donate300Dollar);
//                                                              if (!unusedPurchase.ContainsItem) {
//                                                                  unusedPurchase = ContainsSKU(NonConsumedSKUAndTitles, GDItemSKUs.Donate200Dollar);
//                                                                  if (!unusedPurchase.ContainsItem) {
//                                                                      unusedPurchase = ContainsSKU(NonConsumedSKUAndTitles, GDItemSKUs.Donate100Dollar);
//                                                                  }
//                                                              }
                                                              break;
                                                      }
                                                  }
                                              } catch (Exception ex) {
                                                  ex.printStackTrace();
                                                  GDLogHelper.LogException(ex);
                                              } finally {
                                                  if (OnQueryInventoryFinished != null) {
                                                      if (unusedPurchase != null) {
                                                          OnQueryInventoryFinished.OnQueryInventoryFinished(
                                                                  unusedPurchase.ContainsItem, unusedPurchase.IsSubscription,
                                                                  unusedPurchase.OrderId, unusedPurchase.TokenId,
                                                                  unusedPurchase.ItemTitle, unusedPurchase.ItemSKU);
                                                      } else {
                                                          OnQueryInventoryFinished.OnQueryInventoryFinished(false, false, "", "", "", "");
                                                      }
                                                  }
                                              }
                                          }
                                      }
        );
    }

    private UnusedPurchase ContainsSKU(List<Purchase> AllPurchases, String SKU, Inventory inv) {
        try {
            UnusedPurchase unusedPurchase = new UnusedPurchase();
            String NonConsumedSKU = "";
            for (int i = 0; i < AllPurchases.size(); i++) {
                NonConsumedSKU = AllPurchases.get(i).getSku();
                if (NonConsumedSKU.equals(SKU)) {
                    unusedPurchase.ContainsItem = true;
                    unusedPurchase.ItemTitle = inv.getSkuDetails(AllPurchases.get(i).getSku()).getTitle();
                    unusedPurchase.ItemSKU = NonConsumedSKU;
                    unusedPurchase.TokenId = AllPurchases.get(i).getToken();
                    unusedPurchase.OrderId = AllPurchases.get(i).getOrderId();
                    unusedPurchase.IsSubscription = unusedPurchase.ItemSKU.equals(GDItemSKUs.GDPremiumSubscription3Month)
                            || unusedPurchase.ItemSKU.equals(GDItemSKUs.GDPremiumSubscription6Month)
                            || unusedPurchase.ItemSKU.equals(GDItemSKUs.GDPremiumSubscription12Month);
                    break;
                }
            }
            return unusedPurchase;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return null;
    }

    //Start the purchase flow, if on successful purchase inform calling activity to update info in DB
    public void LaunchPurchaseFlow(String SKU, OnSuccessfulPurchase onSuccessfulPurchase, OnSuccessfulConsume onSuccessfulConsume, ItemType itemType) {
        this.SKUItem = SKU;
        this.mOnSuccessfulPurchase = onSuccessfulPurchase;
        this.mOnSuccessfulConsume = onSuccessfulConsume;
        //If user has bought the item it will be in his inventory. Query the inventory for item.
        queryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                try {
                    if (result.isFailure()) {
                        GDToastHelper.ShowToast(mContext, "Billing was not successful. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        finish();
                    } else {
                        if (mOnSuccessfulPurchase != null) {
                            inventory = inv;
                            mOnSuccessfulPurchase.PurchaseSuccessful(inv.getPurchase(SKUItem).getOrderId(), inv.getPurchase(SKUItem).getToken(), SKUItem);
                        } else {
                            GDToastHelper.ShowToast(mContext, "Billing was not successful. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                            finish();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    GDLogHelper.LogException(ex);
                    finish();
                }
            }
        };

        if (itemType.toString().equals(IabHelper.ITEM_TYPE_INAPP)) {
            iabHelper.launchPurchaseFlow(this, SKUItem, 101, new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase info) {
                    try {
                        if (result.isFailure()) {
                            if (result.mResponse == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                                //User has already bought the item but its not yet consumed. Consume it.
                                iabHelper.queryInventoryAsync(queryInventoryFinishedListener);
                            } else {
                                GDToastHelper.ShowToast(mContext, "Billing was not successful. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                                finish();
                            }
                        } else if (info.getSku().equals(SKUItem)) {
                            iabHelper.queryInventoryAsync(queryInventoryFinishedListener);
                        }
                    } catch (Exception ex) {
                        finish();
                    }
                }
            }, null);
        } else if (itemType.toString().equals(IabHelper.ITEM_TYPE_SUBS)) {
            iabHelper.launchSubscriptionPurchaseFlow(this, SKUItem, 101, new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase info) {
                    try {
                        if (result.isFailure()) {
                            if (result.mResponse == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                                //User has already bought the item but its not yet consumed. Consume it.
                                iabHelper.queryInventoryAsync(queryInventoryFinishedListener);
                            } else {
                                GDToastHelper.ShowToast(mContext, "Billing was not successful. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                                finish();
                            }
                        } else if (info.getSku().equals(SKUItem)) {
                            iabHelper.queryInventoryAsync(queryInventoryFinishedListener);
                        }
                    } catch (Exception ex) {
                        finish();
                    }
                }
            }, null);
        }
    }

    //User has item in inventory(already verified), consume the item and update to DB.
    public void ConsumeItem(final String TransactionID) {
        iabHelper.consumeAsync(inventory.getPurchase(SKUItem), new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                try {
                    if (result.isSuccess()) {
                        if (mOnSuccessfulConsume != null) {
                            mOnSuccessfulConsume.ConsumeSuccessful(TransactionID);
                        }
                    } else {
                        GDToastHelper.ShowToast(mContext, "Billing was not successful. Please try again.", GDToastHelper.ERROR, GDToastHelper.SHORT);
                        finish();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    GDLogHelper.LogException(ex);
                    finish();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface OnSuccessfulPurchase {
        void PurchaseSuccessful(String OrderId, String TokenID, String SKU);
    }

    public interface OnSuccessfulConsume {
        void ConsumeSuccessful(String TransactionID);
    }

    public interface OnInitialQueryInventoryFinished {
        void OnQueryInventoryFinished(Boolean HasItemToConsume, Boolean IsSubscription, String OrderId, String TokenId, String ItemTitle, String SKU);
    }

    public interface OnSuccessfulBillingSetup {
        void SuccessfulBillingSetup();
    }

    public static Boolean IsSubscription(String SKUTypeCode, String SKU) {
        return SKUTypeCode.equals("UP") && !SKU.equals(GDItemSKUs.GDPremium1Month);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iabHelper != null) {
            iabHelper.dispose();
        }
        iabHelper = null;
    }

    class UnusedPurchase {
        Boolean ContainsItem = false;
        Boolean IsSubscription = false;
        String OrderId = "";
        String TokenId = "";
        String ItemTitle = "";
        String ItemSKU = "";
    }
}
