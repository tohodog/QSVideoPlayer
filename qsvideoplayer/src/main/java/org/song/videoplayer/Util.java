package org.song.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by song on 2017/2/13.
 */

public class Util {
    //单位毫秒
    public static String stringForTime(int timeMs) {
        if (timeMs <= 0) {
            return "00:00";
        }
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    //保持常亮
    public static void KEEP_SCREEN_ON(Context context) {
        scanForActivity(context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //取消常亮
    public static void KEEP_SCREEN_OFF(Context context) {
        scanForActivity(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    //全屏
    public static boolean SET_FULL(Context context) {
        Window w = scanForActivity(context).getWindow();
        boolean b = (w.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return b;

    }

    //取消全屏
    public static void CLEAR_FULL(Context context) {
        scanForActivity(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    //横屏
    public static void SET_LANDSCAPE(Context context) {
        scanForActivity(context).setRequestedOrientation
                (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    //竖屏屏
    public static void SET_PORTRAIT(Context context) {
        scanForActivity(context).setRequestedOrientation
                (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //重力感应
    public static void SET_SENSOR(Context context) {
        scanForActivity(context).setRequestedOrientation
                (ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public static int dp2px(Context context, float value) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }


    public static Activity scanForActivity(Context context) {
        if (context instanceof Activity) {
            Activity a = (Activity) context;
            if (a.getParent() != null)
                return a.getParent();
            else
                return a;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        throw new IllegalStateException("context得不到activity");
    }
}
