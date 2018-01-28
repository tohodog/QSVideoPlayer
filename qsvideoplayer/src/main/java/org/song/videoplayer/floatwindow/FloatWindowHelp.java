package org.song.videoplayer.floatwindow;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.reflect.Method;


/**
 * Created by song on 2018/1/15.
 * 视频进出浮窗辅助类
 */

public class FloatWindowHelp implements FloatMoveView.MoveListener {

    private WindowManage windowManage;
    private FloatMoveView floatMoveView;
    private FloatParams floatParams, newFloatParams;
    private Context context;
    private int type;

    public FloatWindowHelp(Context context) {
        this.context = context;
        windowManage = new WindowManage(context);
        type = WindowManager.LayoutParams.TYPE_PHONE;
    }

    public boolean enterWindowFloat(View view, FloatParams floatParams) {
        if (!checkPermission()) {
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
        windowManage.addWindowView(floatMoveView, windowManage.creatParams(type, floatParams));

        return true;
    }

    public void quieWindowFloat() {
        if (floatMoveView != null) {
            windowManage.removeWindowView(floatMoveView);
            floatMoveView.removeAllViews();
            floatMoveView = null;
        }
    }

    @Override
    public void move(int x, int y) {
        if (floatMoveView != null && floatParams.canMove) {
            newFloatParams.x = floatParams.x + x;
            newFloatParams.y = floatParams.y + y;
            windowManage.updateWindowView(floatMoveView, windowManage.creatParams(type, newFloatParams));
        }
    }

    @Override
    public void end() {
        if (floatMoveView != null)
            floatParams = newFloatParams.clone();
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

