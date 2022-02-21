package com.gdudes.app.gdudesapp.CustomViewTypes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class GDGridView extends GridView {
    public GDGridView(Context context) {
        super(context);
    }

    public GDGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GDGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }
}
