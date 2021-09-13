package org.song.videoplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.song.videoplayer.media.AndroidMedia;
import org.song.videoplayer.media.BaseMedia;
import org.song.videoplayer.media.IMediaCallback;
import org.song.videoplayer.media.IMediaControl;
import org.song.videoplayer.rederview.IRenderView;
import org.song.videoplayer.rederview.SufaceRenderView;
import org.song.videoplayer.rederview.TextureRenderView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by song on 2017/2/10.
 * Contact github.com/tohodog
 * 管理 统筹
 */

public class ConfigManage {

    private static ConfigManage instance;

    private static List<WeakReference<QSVideoView>> videos = new ArrayList<>();

    public static ConfigManage getInstance(Context context) {
        if (instance == null)
            instance = new ConfigManage(context);
        return instance;
    }

    private SharedPreferences preferences;
    private String decodeClassName = "";
    private int renderViewType;//1=SufaceRenderView, 2=TextureRenderView

    private ConfigManage(Context context) {
        preferences = context.getSharedPreferences("cfg_qsvideo",
                Context.MODE_PRIVATE);
        decodeClassName = preferences.getString("decodeClassName", AndroidMedia.class.getName());

        int def = Build.VERSION.SDK_INT >= 14 ? 2 : 1;
        renderViewType = preferences.getInt("renderViewType", def);
    }


    IRenderView getIRenderView(Context context, int type) {
        if (type == 0) type = renderViewType;
        if (type == 1) return new SufaceRenderView(context);
        if (type == 2 && Build.VERSION.SDK_INT >= 14) return new TextureRenderView(context);
        Log.e(QSVideoView.TAG, "renderViewType设置有误,默认使用SufaceRenderView");
        return new SufaceRenderView(context);
    }


    IMediaControl newMediaControl(IMediaCallback iMediaCallback, Class<? extends BaseMedia> className) {
        IMediaControl i = Util.newInstance(className.getName(), iMediaCallback);
        if (i == null) {
            Log.e(QSVideoView.TAG, "newInstance error: " + iMediaCallback);
            i = new AndroidMedia(iMediaCallback);
        }
        return i;
    }

    void addVideoView(QSVideoView q) {
        WeakReference<QSVideoView> w = new WeakReference<>(q);
        videos.add(w);
        Iterator<WeakReference<QSVideoView>> iterList = videos.iterator();//List接口实现了Iterable接口
        while (iterList.hasNext()) {
            WeakReference<QSVideoView> ww = iterList.next();
            if (ww.get() == null) iterList.remove();
        }
    }

    public static void releaseAll() {
        for (WeakReference<QSVideoView> w : videos) {
            QSVideoView q = w.get();
            if (q != null) q.release();
        }
        videos.clear();
    }

    public static void releaseOther(QSVideoView qs) {
        for (WeakReference<QSVideoView> w : videos) {
            QSVideoView q = w.get();
            if (q != null & q != qs) q.release();
        }
    }

    public void setDecodeMediaClass(String decodeClassName) {
        this.decodeClassName = decodeClassName;
        preferences.edit().putString("decodeClassName", decodeClassName).apply();
    }

    public String getDecodeMediaClass() {
        return decodeClassName;
    }

    public void setRenderViewType(int renderViewType) {
        this.renderViewType = renderViewType;
        preferences.edit().putInt("renderViewType", renderViewType).apply();
    }

    public int getRenderViewType() {
        return renderViewType;
    }
}
