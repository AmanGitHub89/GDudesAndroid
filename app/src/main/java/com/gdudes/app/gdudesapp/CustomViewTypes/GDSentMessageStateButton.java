package com.gdudes.app.gdudesapp.CustomViewTypes;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

import com.gdudes.app.gdudesapp.R;

public class GDSentMessageStateButton extends AppCompatButton {
    private static final int[] STATE_PENDING = {R.attr.state_pending};
    private static final int[] STATE_SENT = {R.attr.state_sent};
    private static final int[] STATE_DELIVERED = {R.attr.state_delivered};
    private static final int[] STATE_SEEN = {R.attr.state_seen};
    private static final int[] STATE_ERROR = {R.attr.state_error};

    private String myButtonState = "P";

    public GDSentMessageStateButton(Context context) {
        super(context, null);
        refreshDrawableState();
    }

    public GDSentMessageStateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GDSentMessageStateButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setButtonState(String buttonState) {
        myButtonState = buttonState;
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (myButtonState == null) {
            return  super.onCreateDrawableState(extraSpace);
        }
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        switch (myButtonState.toUpperCase()) {
            case "P":
                mergeDrawableStates(drawableState, STATE_PENDING);
                break;
            case "E":
                mergeDrawableStates(drawableState, STATE_ERROR);
                break;
            case "S":
                mergeDrawableStates(drawableState, STATE_SENT);
                break;
            case "D":
                mergeDrawableStates(drawableState, STATE_DELIVERED);
                break;
            case "R":
                mergeDrawableStates(drawableState, STATE_SEEN);
                break;
        }
        return drawableState;
    }
}
