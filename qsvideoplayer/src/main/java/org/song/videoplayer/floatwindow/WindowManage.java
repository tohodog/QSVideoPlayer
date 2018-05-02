package org.song.videoplayer.floatwindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Created by song on 2018/1/15.
 * Contact github.com/tohodog
 * 浮窗管理
 */

public class WindowManage {


    public WindowManage(Context context) {
        getWindowManager(context);
    }

    public int w, h;

    /**
     * 添加悬浮窗
     */
    public void addWindowView(View view, WindowManager.LayoutParams windowParams) {
        WindowManager windowManager = getWindowManager(view.getContext());
        view.setLayoutParams(windowParams);
        if (windowManager != null)
            windowManager.addView(view, windowParams);
    }

    /**
     * 移除悬浮窗
     */
    public void removeWindowView(View view) {
        WindowManager windowManager = getWindowManager(view.getContext());
        if (windowManager != null)
            windowManager.removeView(view);
    }

    /**
     * 更新悬浮窗
     */
    public void updateWindowView(View view, WindowManager.LayoutParams windowParams) {
        WindowManager windowManager = getWindowManager(view.getContext());
        if (windowManager != null)
            windowManager.updateViewLayout(view, windowParams);
    }


    public WindowManager.LayoutParams creatParams(int type, FloatParams floatParams) {
        int ww = (w - floatParams.w) / 2;
        int hh = (h - floatParams.h) / 2;
        if (Math.abs(floatParams.x) > ww)
            floatParams.x = floatParams.x > 0 ? ww : -ww;
        if (Math.abs(floatParams.y) > hh)
            floatParams.y = floatParams.y > 0 ? hh : -hh;

        LayoutParams smallWindowParams = new LayoutParams();
        smallWindowParams.type = type;
        smallWindowParams.format = PixelFormat.RGBA_8888;
        smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        //smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        smallWindowParams.width = floatParams.w;
        smallWindowParams.height = floatParams.h;
        smallWindowParams.x = floatParams.x;
        smallWindowParams.y = floatParams.y;
        return smallWindowParams;
    }

    private WindowManager windowManager;

    private WindowManager getWindowManager(Context context) {
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            //Point p = new Point();
        }
        if (windowManager != null) {
            h = windowManager.getDefaultDisplay().getHeight();
            w = windowManager.getDefaultDisplay().getWidth();
        }
        return windowManager;
    }


}
