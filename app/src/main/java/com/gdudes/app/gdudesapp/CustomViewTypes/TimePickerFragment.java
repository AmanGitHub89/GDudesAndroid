package com.gdudes.app.gdudesapp.CustomViewTypes;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.gdudes.app.gdudesapp.Interfaces.TimeSelected;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private TimeSelected mTimeSelected = null;
    private int hours = -1;
    private int min = -1;

    public void SetData(TimeSelected vmTimeSelected, int vhours, int vmin) {
        mTimeSelected = vmTimeSelected;
        hours = vhours;
        min = vmin;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Use the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int hour = hours == -1 ? c.get(Calendar.HOUR_OF_DAY) : hours;
        int minute = min == -1 ? c.get(Calendar.MINUTE) : min;

        //Create and return a new instance of TimePickerDialog
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    //onTimeSet() callback method
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (mTimeSelected != null) {
            mTimeSelected.onTimeSet(view, hourOfDay, minute);
        }
    }
}