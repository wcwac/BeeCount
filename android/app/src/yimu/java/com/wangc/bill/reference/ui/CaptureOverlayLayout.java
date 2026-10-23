package com.wangc.bill.reference.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

/** AutoAddLayout.dispatchKeyEvent closes on a released Back key only after the IME is hidden. */
public final class CaptureOverlayLayout extends RelativeLayout {
    private Runnable backAction;
    private boolean keyboardVisible;
    public CaptureOverlayLayout(Context context, AttributeSet attributes) { super(context, attributes); }
    public void setBackAction(Runnable action) { backAction = action; }
    public void setKeyboardVisible(boolean value) { keyboardVisible = value; }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP
            && !keyboardVisible && backAction != null) backAction.run();
        return super.dispatchKeyEvent(event);
    }
}
