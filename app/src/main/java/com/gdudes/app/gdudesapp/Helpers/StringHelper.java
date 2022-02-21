package com.gdudes.app.gdudesapp.Helpers;

import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringHelper {
    public static Boolean IsNullOrEmpty(String s) {
        return s == null || s.trim().equals("");
    }

    public static ArrayList<String> ToArrayList(String... a) {
        return new ArrayList<>(Arrays.asList(a));
    }

    public static <T> ArrayList<T> ToArrayList(T... a) {
        return new ArrayList<T>(Arrays.asList(a));
    }

    public static ArrayList<String> ArrayToUpperCase(ArrayList<String> arr) {
        ArrayList<String> upperCaseArray = new ArrayList<>();
        for (String str : arr) {
            upperCaseArray.add(str.toUpperCase());
        }
        return upperCaseArray;
    }

    public static Boolean ArrayContains(ArrayList<String> arr, String str) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public static String TrimFirstAndLastCharacter(String input) {
        try {
            if (input.length() > 2) {
                return input.substring(1, input.length() - 1);
            } else {
                return input;
            }
        } catch (Exception ex) {
            return input;
        }
    }

    public static List<String> SplitStringByComma(String input) {
        try {
            return Arrays.asList(input.split("\\s*,\\s*"));
        } catch (Exception ex) {
            return new ArrayList<String>();
        }
    }

    public static ArrayList<String> RemoveDuplicateEntries(ArrayList<String> inputList) {
        try {
            Set<String> hs = new HashSet<>();
            hs.addAll(inputList);
            inputList.clear();
            inputList.addAll(hs);
        } catch (Exception ex) {

        }
        return inputList;
    }

    public static String FlattenCommaSeperatedStringArray(String[] list) {
        String FlattenedString = "";
        try {
            if (list == null || list.length == 0) {
                return FlattenedString;
            }
            for (int i = 0; i < list.length; i++) {
                FlattenedString = FlattenedString + list[i] + ",";
            }
            FlattenedString = FlattenedString.substring(0, FlattenedString.length() - 1);
        } catch (Exception ex) {

        }
        return FlattenedString;
    }

    public static void HighLightSearchText(String SearchText, TextView prose) {
        try {
            Spannable raw = new SpannableString(prose.getText());
            BackgroundColorSpan[] spans = raw.getSpans(0,
                    raw.length(),
                    BackgroundColorSpan.class);

            for (BackgroundColorSpan span : spans) {
                raw.removeSpan(span);
            }

            int index = TextUtils.indexOf(raw.toString().toUpperCase(), SearchText.toUpperCase());

            while (index >= 0) {
                raw.setSpan(new BackgroundColorSpan(Color.parseColor("#55FFFF00")), index, index
                        + SearchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = TextUtils.indexOf(raw.toString().toUpperCase(), SearchText.toUpperCase(), index + SearchText.length());
            }

            prose.setText(raw);
        } catch (Exception ex) {

        }
    }

    public static String AddEndingBracketsIfNeededToJson(String result) {
        if (result != null && result.startsWith("[") && !result.endsWith("]")) {
            result = result + "]";
        }
        if (result != null && result.startsWith("{") && !result.endsWith("}")) {
            result = result + "}";
        }
        return result;
    }

    public static Uri GetUriFromPath(String path) {
        return Uri.parse(path);
    }

    public static ArrayList<String> RemoveEntriesInList(List<String> MainList, List<String> EntriesToRemove) {
        ArrayList<String> stringList = new ArrayList<>();
        try {
            for (int i = 0; i < MainList.size(); i++) {
                Boolean found = false;
                for (int j = 0; j < EntriesToRemove.size(); j++) {
                    if (MainList.get(i).equalsIgnoreCase(EntriesToRemove.get(j))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    stringList.add(MainList.get(i));
                }
            }

        } catch (Exception ex) {

        }
        return stringList;
    }
}
