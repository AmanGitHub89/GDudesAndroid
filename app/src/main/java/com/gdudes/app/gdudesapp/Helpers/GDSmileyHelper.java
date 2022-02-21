package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Pair;

import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;

public class GDSmileyHelper {
    private static ArrayList<Pair<Integer, String>> mSmileyCodes = new ArrayList<>();
    private static Boolean SmileysLoaded = false;

    private static void LoadSmileys() {
        if (SmileysLoaded) {
            return;
        }
        try {
            SmileysLoaded = true;
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley1, "%F0%9F%98%80"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley2, "%F0%9F%98%8A"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley3, "%F0%9F%98%89"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley4, "%F0%9F%98%8D"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley5, "%F0%9F%98%98"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley6, "%F0%9F%98%9C"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley7, "%F0%9F%98%9D"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley8, "%F0%9F%98%9B"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley9, "%F0%9F%98%B3"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley10, "%F0%9F%98%81"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley11, "%F0%9F%98%94"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley12, "%F0%9F%98%82"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley13, "%F0%9F%98%A5"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley14, "%F0%9F%98%A9"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley15, "%F0%9F%98%A8"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley16, "%F0%9F%98%B1"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley17, "%F0%9F%98%A1"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley18, "%F0%9F%98%A4"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley19, "%F0%9F%98%96"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley20, "%F0%9F%98%8B"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley21, "%F0%9F%98%B7"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley22, "%F0%9F%98%8E"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley23, "%F0%9F%98%B4"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley24, "%F0%9F%98%B2"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley25, "%F0%9F%98%88"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley26, "%F0%9F%98%AC"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley27, "%F0%9F%98%90"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley28, "%F0%9F%98%95"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley29, "%F0%9F%98%AF"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley30, "%F0%9F%98%87"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley31, "%F0%9F%98%8F"));
            mSmileyCodes.add(new Pair<Integer, String>(R.drawable.smiley32, "%F0%9F%98%91"));
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public static Spannable ShowSmileysForText(Context context, String text, Resources resources, Boolean IsDecoded) {
        LoadSmileys();
        int index;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(text);
        for (int i = 0; i < mSmileyCodes.size(); i++) {
            try {
                String SmileyCode = IsDecoded ? StringEncoderHelper.decodeURIComponent(mSmileyCodes.get(i).second) : mSmileyCodes.get(i).second;
                int length = SmileyCode.length();
                for (index = text.indexOf(SmileyCode); index >= 0; index = text.indexOf(SmileyCode, index + 1)) {
                    builder.setSpan(new ImageSpan(context, ImageHelper.getBitmapForResource(resources, mSmileyCodes.get(i).first)), index, index + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            } catch (Exception e) {
                e.printStackTrace();
                GDLogHelper.LogException(e);
            }
        }

        return builder;
    }

    public static Boolean IsSmileyCode(String code) {
        LoadSmileys();
        for (int i = 0; i < mSmileyCodes.size(); i++) {
            if (mSmileyCodes.get(i).second.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
