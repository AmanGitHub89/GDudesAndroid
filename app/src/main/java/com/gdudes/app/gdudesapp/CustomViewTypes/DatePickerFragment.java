package com.gdudes.app.gdudesapp.CustomViewTypes;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.gdudes.app.gdudesapp.Interfaces.DateSelected;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DateSelected DateSelected;
    private int mYear;
    private int mMonth;
    private int mDay;
    public Boolean IsDOB = false;
    public Boolean SetMaxDayToday = false;

    public void SetData(DateSelected vDateSelected, int vYear, int vMonth, int vDay) {
        DateSelected = vDateSelected;
        mYear = vYear;
        mMonth = vMonth;
        mDay = vDay;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c18YearOld = null;
        Calendar c = null;

        c = Calendar.getInstance();
        int year = mYear == -1 ? c.get(Calendar.YEAR) : mYear;
        int month = mMonth == -1 ? c.get(Calendar.MONTH) : mMonth;
        int day = mDay == -1 ? c.get(Calendar.DAY_OF_MONTH) : mDay;
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        if (IsDOB) {
            c18YearOld = Calendar.getInstance();
            c18YearOld.add(Calendar.YEAR, -18);
            c18YearOld.add(Calendar.DAY_OF_MONTH, -2);
            if (c.getTime().getTime() > c18YearOld.getTime().getTime()) {
                c.setTime(c18YearOld.getTime());
            }
        }

        DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        if (IsDOB) {
            dpd.getDatePicker().setMaxDate(c18YearOld.getTime().getTime());
        } else if (SetMaxDayToday) {
            dpd.getDatePicker().setMaxDate(Calendar.getInstance().getTime().getTime());
        }
        return dpd;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (DateSelected != null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            Date date = c.getTime();
            DateSelected.onDateSelect(view, year, month, day, date);
        }
    }
}