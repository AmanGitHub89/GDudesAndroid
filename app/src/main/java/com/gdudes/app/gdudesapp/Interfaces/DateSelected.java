package com.gdudes.app.gdudesapp.Interfaces;

import android.widget.DatePicker;

import java.util.Date;

public interface DateSelected {
    void onDateSelect(DatePicker view, int year, int month, int day, Date date);
}
