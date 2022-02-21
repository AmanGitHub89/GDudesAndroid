package com.gdudes.app.gdudesapp.CustomViewTypes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.gdudes.app.gdudesapp.GDTypes.GDSKeyValue;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.Interfaces.OnGDSpinnerCheckboxSelected;

import java.util.ArrayList;
import java.util.List;

public class GDCheckboxItems extends Spinner {
    private ArrayAdapter SpinnerAdapter;
    private Context mContext;
    private List<String> DataList;
    private String Title;
    private OnGDSpinnerCheckboxSelected onCheckboxSelected;
    private OnDialogButtonClick onCancelled;
    private OnDialogButtonClick onOKButtonClick;
    private boolean[] CheckedItems;
    private List<GDSKeyValue> DataStore;
    private Boolean IsOKClicked = false;

    public GDCheckboxItems(Context context) {
        super(context);
    }

    public GDCheckboxItems(Context context, int mode) {
        super(context, mode);
    }

    public GDCheckboxItems(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GDCheckboxItems(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GDCheckboxItems(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public void SetData(Context context, String Title, List<GDSKeyValue> DataStore, String SelectedValue,
                        OnGDSpinnerCheckboxSelected onCheckboxSelected,
                        OnDialogButtonClick onOKButtonClick,
                        OnDialogButtonClick onCancelled) {
        mContext = context;
        this.Title = Title;
        this.onCheckboxSelected = onCheckboxSelected;
        this.onCancelled = onCancelled;
        this.onOKButtonClick = onOKButtonClick;
        this.DataStore = DataStore;

        DataList = new ArrayList<String>();
        for (int i = 0; i < DataStore.size(); i++) {
            DataList.add(DataStore.get(i).GDSValue);
        }
        SetCheckedItemsForSelectedValue(SelectedValue);
        SpinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, DataList);
        SetTextForSelectedItems(false);

        SetTagForPreSelected();
    }

    private void SetCheckedItemsForSelectedValue(String SelectedValue) {
        List<String> SelectedValues = StringHelper.SplitStringByComma(SelectedValue);
        CheckedItems = new boolean[DataList.size()];
        for (int i = 0; i < DataStore.size(); i++) {
            if (SelectedValues.contains(DataStore.get(i).GDSKey)) {
                CheckedItems[i] = true;
            }
        }
    }

    @Override
    public boolean performClick() {
        this.setAdapter(SpinnerAdapter);
        IsOKClicked = false;
        GDDialogHelper.ShowCheckboxOptionTypeDialog(mContext, DataList.toArray(new CharSequence[DataList.size()]),
                Title, CheckedItems, GDDialogHelper.BUTTON_TEXT_OK, GDDialogHelper.BUTTON_TEXT_CANCEL,
                new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        String CheckedVal = "";
                        String CheckedDesc = "";
                        for (int i = 0; i < CheckedItems.length; i++) {
                            if (CheckedItems[i]) {
                                CheckedVal = CheckedVal + DataStore.get(i).GDSKey + ",";
                                CheckedDesc = CheckedDesc + DataStore.get(i).GDSValue + ", ";
                            }
                        }
                        if (CheckedVal.length() > 0) {
                            //Remove the last comma
                            CheckedVal = CheckedVal.substring(0, CheckedVal.length() - 1);
                            CheckedDesc = CheckedDesc.substring(0, CheckedDesc.length() - 2);
                        }

                        List<String> NewDataList = new ArrayList<String>();
                        NewDataList.add(CheckedDesc);
                        ArrayAdapter NewSpinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, NewDataList);
                        GDCheckboxItems.this.setAdapter(NewSpinnerAdapter);
                        setTag(CheckedVal);
                        if (onOKButtonClick != null) {
                            onOKButtonClick.dialogButtonClicked();
                        }
                        IsOKClicked = true;
                    }
                }, new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        SetTextForSelectedItems(true);
                        if (onCancelled != null) {
                            onCancelled.dialogButtonClicked();
                        }
                    }
                }, new OnGDSpinnerCheckboxSelected() {
                    @Override
                    public void GDSpinnerCheckboxSelected(int position, boolean IsChecked) {
                        CheckedItems[position] = IsChecked;
                        if (onCheckboxSelected != null) {
                            onCheckboxSelected.GDSpinnerCheckboxSelected(position, IsChecked);
                        }
                    }
                });

        return true;
    }

    private void SetTextForSelectedItems(Boolean Dismissed) {
        String CheckedDesc = "";
        if (Dismissed && !IsOKClicked) {
            List<String> SelectedTags = StringHelper.SplitStringByComma(getTag().toString());
            for (int j = 0; j < DataStore.size(); j++) {
                CheckedItems[j] = false;
                for (int i = 0; i < SelectedTags.size(); i++) {
                    if (DataStore.get(j).GDSKey.equals(SelectedTags.get(i))) {
                        CheckedItems[j] = true;
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < CheckedItems.length; i++) {
            if (CheckedItems[i]) {
                CheckedDesc = CheckedDesc + DataStore.get(i).GDSValue + ", ";
            }
        }
        if (CheckedDesc.length() > 0) {
            //Remove the last comma
            CheckedDesc = CheckedDesc.substring(0, CheckedDesc.length() - 2);
        }
        List<String> NewDataList = new ArrayList<String>();
        NewDataList.add(CheckedDesc);
        ArrayAdapter NewSpinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, NewDataList);
        GDCheckboxItems.this.setAdapter(NewSpinnerAdapter);
    }

    private void SetTagForPreSelected() {
        String CheckedVal = "";
        for (int i = 0; i < CheckedItems.length; i++) {
            if (CheckedItems[i]) {
                CheckedVal = CheckedVal + DataStore.get(i).GDSKey + ",";
            }
        }
        if (CheckedVal.length() > 0) {
            //Remove the last comma
            CheckedVal = CheckedVal.substring(0, CheckedVal.length() - 1);
        }
        setTag(CheckedVal);
    }
}
