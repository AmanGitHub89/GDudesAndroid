package com.gdudes.app.gdudesapp.CustomViewTypes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.gdudes.app.gdudesapp.GDTypes.GDSKeyValue;
import com.gdudes.app.gdudesapp.Helpers.GDDialogHelper;
import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.gdudes.app.gdudesapp.Interfaces.OnRadioOptionselected;

import java.util.ArrayList;
import java.util.List;

public class GDRadioOptions extends Spinner {
    private ArrayAdapter SpinnerAdapter;
    private Context mContext;
    private List<String> DataList;
    private String Title;
    private OnRadioOptionselected RadioOptionselected;
    private OnDialogButtonClick onCancelled;
    private int SelectedPosition;
    private List<GDSKeyValue> DataStore;

    public GDRadioOptions(Context context) {
        super(context);
    }

    public GDRadioOptions(Context context, int mode) {
        super(context, mode);
    }

    public GDRadioOptions(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GDRadioOptions(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GDRadioOptions(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public void SetData(Context context, String Title, List<GDSKeyValue> vDataStore, String selectedValue,
                        OnRadioOptionselected RadioOptionselected,
                        OnDialogButtonClick onCancelled) {
        SetData(context, Title, vDataStore, selectedValue, RadioOptionselected, onCancelled, false);
    }

    public void SetData(Context context, String Title, List<GDSKeyValue> vDataStore, String selectedValue,
                        OnRadioOptionselected RadioOptionselected,
                        OnDialogButtonClick onCancelled, Boolean PrependBlank) {
        mContext = context;
        this.Title = Title;
        this.RadioOptionselected = RadioOptionselected;
        this.onCancelled = onCancelled;
        if (PrependBlank) {
            this.DataStore = new ArrayList<>();
            this.DataStore.add(new GDSKeyValue("", "---"));
            this.DataStore.addAll(vDataStore);
        } else {
            this.DataStore = vDataStore;
        }

        this.SelectedPosition = 0;
        DataList = new ArrayList<String>();
        for (int i = 0; i < DataStore.size(); i++) {
            DataList.add(DataStore.get(i).GDSValue);
            if (DataStore.get(i).GDSKey.equals(selectedValue)) {
                this.SelectedPosition = i;
            }
        }
        SpinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, DataList);
        this.setAdapter(SpinnerAdapter);
        setSelection(this.SelectedPosition);
        setTag(DataStore.get(this.SelectedPosition).GDSKey);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.SelectedPosition = selectedPosition;
    }

    @Override
    public boolean performClick() {
        GDDialogHelper.ShowRadioOptionTypeDialog(mContext, DataList.toArray(new CharSequence[DataList.size()]), Title,
                this.getSelectedItemPosition(), GDDialogHelper.BUTTON_TEXT_CANCEL,
                new OnRadioOptionselected() {
                    @Override
                    public void RadioOptionselected(int position) {
                        setSelection(position);
                        setTag(DataStore.get(position).GDSKey);
                        if (RadioOptionselected != null) {
                            RadioOptionselected.RadioOptionselected(position);
                        }
                    }
                }, new OnDialogButtonClick() {
                    @Override
                    public void dialogButtonClicked() {
                        if (onCancelled != null) {
                            onCancelled.dialogButtonClicked();
                        }
                    }
                });
        return true;
    }
}
