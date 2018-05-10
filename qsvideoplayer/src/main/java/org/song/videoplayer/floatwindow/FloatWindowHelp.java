package org.song.videoplayer.floatwindow;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.song.videoplayer.Util;

import java.lang.reflect.Method;


/**
 * Created by song on 2018/1/15.
 * Contact github.com/tohodog
 * 视频进出浮窗辅助类
 */

public class FloatWindowHelp implements FloatMoveView.MoveListener {

    private WindowManage windowManage;
    private ViewGroup decorView;
    private FloatMoveView floatMoveView;
    private FloatParams floatParams, newFloatParams;
    private Context context;
    private int type;

    public FloatWindowHelp(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= 26)  //8.0新特性
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            type = WindowManager.LayoutParams.TYPE_PHONE;

        decorView = (ViewGroup) (Util.scanForActivity(context)).getWindow().getDecorView();
    }

    public boolean enterWindowFloat(View view, final FloatParams floatParams) {
        if (floatParams.systemFloat && !checkPermission()) {
            return false;
        }
        this.floatParams = floatParams;
        newFloatParams = floatParams.clone();
        floatMoveView = new FloatMoveView(context);
        floatMoveView.setMoveListener(this);
        floatMoveView.setRount(floatParams.round);
        if (Build.VERSION.SDK_INT >= 11)
            floatMoveView.setAlpha(floatParams.fade);

        floatMoveView.addView(view, new FrameLayout.LayoutParams(-1, -1));

        if (floatParams.systemFloat)
            getWindowManage().addWindowView(floatMoveView, getWindowManage().creatParams(type, floatParams));
        else {
            FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(floatParams.w, floatParams.h);
            l.leftMargin = (decorView.getMeasuredWidth() - floatParams.w) / 2 + floatParams.x;
            l.topMargin = (decorView.getMeasuredHeight() - floatParams.h) / 2 + floatParams.y;
            decorView.addView(floatMoveView, l);
        }

        return true;
    }

    public void quieWindowFloat() {
        if (floatMoveView != null) {
            if (floatParams.systemFloat)
                getWindowManage().removeWindowView(floatMoveView);
            else {
                ViewGroup vp = (ViewGroup) floatMoveView.getParent();
                if (vp != null)
                    vp.removeView(floatMoveView);
            }
            floatMoveView.removeAllViews();
            floatMoveView = null;

            newFloatParams = null;
            windowManage = null;
        }
    }

    @Override
    public void move(int x, int y) {
        if (floatMoveView != null && floatParams.canMove) {
            newFloatParams.x = floatParams.x + x;
            newFloatParams.y = floatParams.y + y;
            if (floatParams.systemFloat)
                getWindowManage().updateWindowView(floatMoveView, getWindowManage().creatParams(type, newFloatParams));
            else {
                ViewGroup.MarginLayoutParams l = (ViewGroup.MarginLayoutParams) floatMoveView.getLayoutParams();
                l.leftMargin = (decorView.getMeasuredWidth() - floatParams.w) / 2 + newFloatParams.x;
                l.topMargin = (decorView.getMeasuredHeight() - floatParams.h) / 2 + newFloatParams.y;
                floatMoveView.setLayoutParams(l);
            }
        }
    }

    @Override
    public void end() {
        if (floatMoveView != null)
            floatParams = newFloatParams.clone();
    }

    public FloatParams getFloatParams() {
        return floatParams;
    }


    private WindowManage getWindowManage() {
        if (windowManage == null)
            windowManage = new WindowManage(context);
        return windowManage;
    }


    //检查浮窗权限
    public boolean checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //AppOpsManager添加于API 19
            return checkOps();
        } else {
            //4.4以下一般都可以直接添加悬浮窗
            return true;
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean checkOps() {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = (Integer) method.invoke(object, arrayOfObject1);
            //4.4至6.0之间的非国产手机，例如samsung，sony一般都可以直接添加悬浮窗
            boolean b = m == AppOpsManager.MODE_ALLOWED;//|| !RomUtils.isDomesticSpecialRom();
            if (!b)
                type = WindowManager.LayoutParams.TYPE_TOAST;
        } catch (Exception ignore) {
        }
        return true;
    }

}

