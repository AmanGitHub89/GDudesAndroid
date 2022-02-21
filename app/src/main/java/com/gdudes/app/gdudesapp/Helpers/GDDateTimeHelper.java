package com.gdudes.app.gdudesapp.Helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GDDateTimeHelper {

    public static Date GetDateFromString(String InputDate) {
        Date date;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            InputDate = FormatToSqlDateStringWithPrecision(InputDate);
            date = format.parse(InputDate);
        } catch (Exception e) {
            date = Calendar.getInstance().getTime();
            e.printStackTrace();
            GDLogHelper.LogException(e);
        }
        return date;
    }

    public static String GetStringFromDate(Date InputDate) {
        String StringDateTime;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
            StringDateTime = format.format(InputDate);
        } catch (Exception e) {
            StringDateTime = "";
            e.printStackTrace();
            GDLogHelper.LogException(e);
        }
        return StringDateTime;
    }

    public static String GetCurrentDateTimeAsString(Boolean GetUTCTime) {
        SimpleDateFormat format;
        String StringDateTime;
        try {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS", Locale.US);
            if (GetUTCTime) {
                format.setTimeZone(TimeZone.getTimeZone("gmt"));
            }
            Date date = Calendar.getInstance().getTime();
            StringDateTime = format.format(date);
        } catch (Exception e) {
            StringDateTime = "";
            e.printStackTrace();
            GDLogHelper.LogException(e);
        }
        return StringDateTime;
    }

    public static String GetDateStringWithoutTime(String SqlDateTime, Boolean ConvertToLocalTime) {
        SimpleDateFormat format;
        String ReturnDate;
        Date date;
        try {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            SqlDateTime = FormatToSqlDateStringWithPrecision(SqlDateTime);
            date = format.parse(SqlDateTime);
            if (ConvertToLocalTime) {
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                date = new Date(date.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
            }
        } catch (Exception ex) {
            date = Calendar.getInstance().getTime();
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        try {
            DateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");
            ReturnDate = outputFormatter.format(date);
            ReturnDate = ReturnDate + "T00:00:00.000";
            return ReturnDate;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return "";
    }

    public static String GetMonthDateYearString(Date InputDate, Boolean GetLocalTime) {
        SimpleDateFormat format;
        String StringDateTime;
        try {
            format = new SimpleDateFormat("MMM dd, yyyy");
            if (GetLocalTime) {
                //format.setTimeZone(TimeZone.getTimeZone("gmt"));
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                //Date fromGmt = new Date(gmtTime.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
                StringDateTime = format.format(new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime())));
            } else {
                StringDateTime = format.format(InputDate);
            }
        } catch (Exception e) {
            StringDateTime = "";
            e.printStackTrace();
            GDLogHelper.LogException(e);
        }
        return StringDateTime;
    }

    public static String GetTimeStringFromDate(Date InputDate, Boolean GetLocalTime) {
        SimpleDateFormat format;
        String StringDateTime;
        if (InputDate == null) {
            return "";
        }
        try {
            format = new SimpleDateFormat("HH:mm");
            if (GetLocalTime) {
                //format.setTimeZone(TimeZone.getTimeZone("gmt"));
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                //Date fromGmt = new Date(gmtTime.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
                StringDateTime = format.format(new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime())));
            } else {
                StringDateTime = format.format(InputDate);
            }
            int hours = Integer.parseInt(StringDateTime.substring(0, 2));
            int mins = Integer.parseInt(StringDateTime.substring(3, 5));
            Boolean IsPM = false;
            if (hours >= 12) {
                IsPM = true;
                if (hours > 12) {
                    hours = hours - 12;
                }
            } else if (hours == 0) {
                hours = 12;
            }
            StringDateTime = String.format("%02d", hours) + ":" + String.format("%02d", mins) + (IsPM ? " PM" : " AM");
        } catch (Exception e) {
            StringDateTime = "";
            e.printStackTrace();
            GDLogHelper.LogException(e);
        }
        return StringDateTime;
    }

    public static String Get24HoursTimeStringFromDate(Date InputDate, Boolean GetLocalTime) {
        SimpleDateFormat format;
        String StringDateTime;
        try {
            format = new SimpleDateFormat("HH:mm");
            if (GetLocalTime) {
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                StringDateTime = format.format(new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime())));
            } else {
                StringDateTime = format.format(InputDate);
            }
            int hours = Integer.parseInt(StringDateTime.substring(0, 2));
            int mins = Integer.parseInt(StringDateTime.substring(3, 5));
            StringDateTime = String.format("%02d", hours) + ":" + String.format("%02d", mins);
        } catch (Exception e) {
            StringDateTime = "";
            e.printStackTrace();
            GDLogHelper.LogException(e);
        }
        return StringDateTime;
    }

    public static String FormatToSqlDateStringWithPrecision(String InputDate) {
        String FormattedDate = InputDate;
        try {
            if (FormattedDate.indexOf(".") < 0) {
                FormattedDate = FormattedDate + ".000";
            } else {
                if (FormattedDate.length() < 23) {
                    for (int i = FormattedDate.length(); i <= 23; i++) {
                        FormattedDate = FormattedDate + "0";
                    }
                }
            }
//        InputDate = AddZerosToDateStringIfRequired(InputDate);
//        FormattedDate = InputDate.substring(0, InputDate.indexOf(".") < 0 ? InputDate.length() : InputDate.indexOf("."));
//        FormattedDate = FormattedDate + "." + (InputDate.indexOf(".") < 0 ? String.format("%.3f", 0d).substring(2, 5) :
//                String.format("%.3f", Double.parseDouble(InputDate.substring(InputDate.indexOf("."), InputDate.length()))).substring(2, 5));
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return FormattedDate;
    }

    public static String GetDateOnlyStringFromDate(Date InputDate, Boolean GetLocalTime) {
        String StringDateTime;
        Date InputDateToUse = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            if (GetLocalTime) {
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                InputDateToUse = new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
            } else {
                InputDateToUse = InputDate;
            }
            StringDateTime = format.format(InputDateToUse);
        } catch (Exception e) {
            StringDateTime = "";
            e.printStackTrace();
            GDLogHelper.LogException(e);
        }
        return StringDateTime;
    }

    public static String GetFormattedDateAndTime(Date InputDate, Boolean GetLocalTime) {
        try {
            Date TimeNow = new Date();
            Date InputDateToUse = null;
            Calendar CalendarNow = Calendar.getInstance();
            Calendar CalendarInput = Calendar.getInstance();
            if (GetLocalTime) {
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                InputDateToUse = new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
            } else {
                InputDateToUse = InputDate;
            }
            CalendarInput.setTime(InputDateToUse);
            return GetDateOnlyStringFromDate(InputDateToUse, false) + ", " + GetTimeStringFromDate(InputDateToUse, false);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return "";
    }

    public static String GetTimeDateBeforeFromDate(Date InputDate, Boolean GetLocalTime) {
        try {
            Date TimeNow = new Date();
            Date InputDateToUse = null;
            Calendar CalendarNow = Calendar.getInstance();
            Calendar CalendarInput = Calendar.getInstance();

            if (GetLocalTime) {
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                InputDateToUse = new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
            } else {
                InputDateToUse = InputDate;
            }
            CalendarInput.setTime(InputDateToUse);
            try {
                long secs = (TimeNow.getTime() - InputDateToUse.getTime()) / 1000;
                if (secs >= 0) {
                    int hours = (int) secs / 3600;
                    if (hours <= 24 && CalendarNow.get(Calendar.DAY_OF_MONTH) == CalendarInput.get(Calendar.DAY_OF_MONTH)) {
                        return GetTimeStringFromDate(InputDateToUse, false);
                    } else {
                        return GetDateOnlyStringFromDate(InputDateToUse, false);
                    }
                } else {
                    return GetDateOnlyStringFromDate(InputDateToUse, false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
                return GetDateOnlyStringFromDate(InputDate, false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return "";
    }

    public static String GetTimeDateBeforeFromDateWithTime(Date InputDate, Boolean GetLocalTime) {
        try {
            Date TimeNow = new Date();
            Date InputDateToUse = null;
            Calendar CalendarNow = Calendar.getInstance();
            Calendar CalendarInput = Calendar.getInstance();

            if (GetLocalTime) {
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                InputDateToUse = new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
            } else {
                InputDateToUse = InputDate;
            }
            CalendarInput.setTime(InputDateToUse);
            try {
                long secs = (TimeNow.getTime() - InputDateToUse.getTime()) / 1000;
                if (secs >= 0) {
                    int hours = (int) secs / 3600;
                    if (hours <= 24 && CalendarNow.get(Calendar.DAY_OF_MONTH) == CalendarInput.get(Calendar.DAY_OF_MONTH)) {
                        return GetTimeStringFromDate(InputDateToUse, false);
                    } else {
                        return GetDateOnlyStringFromDate(InputDateToUse, false) + ", " + GetTimeStringFromDate(InputDateToUse, false);
                    }
                } else {
                    return GetDateOnlyStringFromDate(InputDateToUse, false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
                return GetDateOnlyStringFromDate(InputDate, false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return "";
    }

    public static Boolean IsDateInSameDay(Date InputDate, Boolean GetLocalTime) {
        try {
            Date TimeNow = new Date();
            Date InputDateToUse = null;
            Calendar CalendarNow = Calendar.getInstance();
            Calendar CalendarInput = Calendar.getInstance();

            if (GetLocalTime) {
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                InputDateToUse = new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
            } else {
                InputDateToUse = InputDate;
            }
            CalendarInput.setTime(InputDateToUse);
            try {
                long secs = (TimeNow.getTime() - InputDateToUse.getTime()) / 1000;
                if (secs >= 0) {
                    int hours = (int) secs / 3600;
                    if (hours <= 24 && CalendarNow.get(Calendar.DAY_OF_MONTH) == CalendarInput.get(Calendar.DAY_OF_MONTH)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return false;
    }

    public static long GetTimeElapsedInSeconds(Date date) {
        try {
            Date timeNow = GDDateTimeHelper.GetDateFromString(GDDateTimeHelper.GetCurrentDateTimeAsString(false));
            return (timeNow.getTime() - date.getTime()) / 1000;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return -1;
        }
    }

//    public static String GetTimeDateBeforeFromDate(Context c, Date InputDate, Boolean GetLocalTime) {
//        try {
//            if (GetLocalTime) {
//                long ts = System.currentTimeMillis();
//                Date localTime = new Date(ts);
//                Date TempDate = new Date(InputDate.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
//                return DateUtils.getRelativeDateTimeString(c, TempDate.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
//            } else {
//                return DateUtils.getRelativeDateTimeString(c, InputDate.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return GetTimeStringFromDate(InputDate, GetLocalTime);
//        }
//    }
//    private static String AddZerosToDateStringIfRequired(String DateAsString) {
//        if (DateAsString.indexOf(".") < 0) {
//            DateAsString = DateAsString + ".000";
//        } else {
//            if (DateAsString.length() < 23) {
//                for (int i = DateAsString.length(); i <= 23; i++) {
//                    DateAsString = DateAsString + "0";
//                }
//            }
//        }
//        return DateAsString;
//    }
}
