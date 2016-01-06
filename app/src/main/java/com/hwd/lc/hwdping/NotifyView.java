package com.hwd.lc.hwdping;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * **************************************************************
 * <p/>
 * **************************************************************
 * Authors:huweidong on 2016/1/5 19:19
 * Email：huwwds@gmail.com
 */
public class NotifyView {
    private TextView mTv;

    public NotifyView(Context context) {
        WindowManager mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.gravity = Gravity.TOP + Gravity.START;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mTv = new TextView(context);
        mTv.setBackgroundColor(0xff00cc00);
        mTv.setTextColor(0xffff5555);
        mWm.addView(mTv, mParams);
    }

    public void show(String msg) {
        mTv.setText(msg);
    }
}
