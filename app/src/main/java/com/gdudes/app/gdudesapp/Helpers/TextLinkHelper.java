package com.gdudes.app.gdudesapp.Helpers;

import android.graphics.Color;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.Interfaces.OnDialogButtonClick;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.klinker.android.link_builder.TouchableMovementMethod;

import java.util.ArrayList;
import java.util.List;

public class TextLinkHelper {
    public static void AddLink(TextView textView, String text, final OnDialogButtonClick onDialogButtonClick, Boolean SetMovementMethod) {
        AddLink(textView, text, onDialogButtonClick, SetMovementMethod, Color.parseColor("#259B24"));
    }
    public static void AddLink(TextView textView, String text, final OnDialogButtonClick onDialogButtonClick, Boolean SetMovementMethod, int color) {
        try {
            Link link = new Link(text)
                    .setTextColor(color)    // optional, defaults to holo blue
                    .setHighlightAlpha(.4f)                       // optional, defaults to .15f
                    .setUnderlined(true)                         // optional, defaults to true
                    .setBold(true)                                // optional, defaults to false
                    .setOnLongClickListener(clickedText -> {
                        // long clicked
                    })
                    .setOnClickListener(clickedText -> {
                        if (onDialogButtonClick != null) {
                            onDialogButtonClick.dialogButtonClicked();
                        }
                    });
            List<Link> links = new ArrayList<>();
            links.add(link);
            LinkBuilder.on(textView)
                    .addLinks(links)
                    .build();
            // if you forget to set the movement method, then your text will not be clickable!
            //JoinClub.setMovementMethod(TouchableMovementMethod.getInstance());
            if (SetMovementMethod) {
                textView.setMovementMethod(TouchableMovementMethod.getInstance());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }
}
