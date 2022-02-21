package com.gdudes.app.gdudesapp.Helpers;

import java.text.DecimalFormat;

public class GDUnitHelper {
    private static DecimalFormat decimalFormat = new DecimalFormat(".##");

    public static String Distance_MTI(float ValueInKm) {
        try {
            if ((int) (ValueInKm * 0.621371192) >= 1) {
                if ((int) (ValueInKm * 0.621371192) >= 100) {
                    return Integer.toString((int) (ValueInKm * 0.621371192)) + " mi";
                } else {
                    return decimalFormat.format(ValueInKm * 0.621371192) + " mi";
                }
            } else {
                return Integer.toString((int) (ValueInKm * 1000 * 3.2808399)) + " ft";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    public static String Height_MTI(int HeightInMetric, Boolean AppendUnit) {
        try {
            return Integer.toString((int) (HeightInMetric / 30)) + "'" +
                    Integer.toString((int) ((HeightInMetric % 30) / 2.5)) + (AppendUnit ? " ft" : "");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    public static String Weight_MTI(int WeightInMetric, Boolean AppendUnit) {
        try {
            return Integer.toString((int) (WeightInMetric * 2.20462262)) + (AppendUnit ? " lbs" : "");
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    public static int KmToMiles(int KmVal) {
        return (int) (KmVal / 1.609344);
    }

    public static int MilesToKm(int MilesVal) {
        return (int) (MilesVal * 1.609344);
    }

    public static int Height_ITM(int HeightInImperial) {
        return 0;
    }

    public static int Weight_ITM(int WeightInImperial) {
        return 0;
    }

    public static String FormatKM(float ValueInKm) {
        try {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(1);
            String FormattedVal = df.format(ValueInKm) + " Km";
            if (ValueInKm < 1) {
                FormattedVal = Integer.toString((int) (ValueInKm * 1000)) + " m";
            } else if (ValueInKm >= 10) {
                FormattedVal = Integer.toString((int) ValueInKm) + " Km";
            }
            return FormattedVal;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }
}
