package org.song.videoplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;

import org.song.videoplayer.media.AndroidMedia;
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
    private int media_mode = 0;

    private ConfigManage(Context context) {
        preferences = context.getSharedPreferences("cfg_qsvideo",
                Context.MODE_PRIVATE);
        media_mode = preferences.getInt("media_mode", 0);
    }


    public IRenderView getIRenderView(Context context) {
        if (Build.VERSION.SDK_INT >= 14)
            return new TextureRenderView(context);
        else
            return new SufaceRenderView(context);
    }


    //后期扩展其他解码器 exo ijk... exo api需大于16
    public IMediaControl getIMediaControl(IMediaCallback iMediaCallback, int MEDIA_MODE) {
        if (iMediaCallback instanceof QSVideoView)
            addVideoView((QSVideoView) iMediaCallback);
        if (MEDIA_MODE == 1)
            return newInstance("org.song.videoplayer.media.IjkMedia", iMediaCallback);
        if (MEDIA_MODE == 2 & Build.VERSION.SDK_INT >= 16)
            return newInstance("org.song.videoplayer.media.ExoMedia", iMediaCallback);
        if (MEDIA_MODE == 3 & Build.VERSION.SDK_INT >= 16)
            return newInstance("org.song.videoplayer.media.IjkExoMedia", iMediaCallback);
        return new AndroidMedia(iMediaCallback);
    }

    private IMediaControl newInstance(String className, IMediaCallback iMediaCallback) {
        IMediaControl i = Util.newInstance(className, iMediaCallback);
        if (i == null)
            i = new AndroidMedia(iMediaCallback);
        return i;
    }

    //后期扩展其他解码器 exo ijk... exo api需大于16
    public IMediaControl getIMediaControl(IMediaCallback iMediaCallback) {
        return getIMediaControl(iMediaCallback, media_mode);
    }

    private void addVideoView(QSVideoView q) {
        WeakReference<QSVideoView> w = new WeakReference<>(q);
        videos.add(w);
        Iterator<WeakReference<QSVideoView>> iterList = videos.iterator();//List接口实现了Iterable接口
        while (iterList.hasNext()) {
            WeakReference<QSVideoView> ww = iterList.next();
            if (ww.get() == null)
                iterList.remove();
        }
    }

    public static void releaseAll() {
        for (WeakReference<QSVideoView> w : videos) {
            QSVideoView q = w.get();
            if (q != null)
                q.release();
        }
        videos.clear();
    }

    public static void releaseOther(QSVideoView qs) {
        for (WeakReference<QSVideoView> w : videos) {
            QSVideoView q = w.get();
            if (q != null & q != qs)
                q.release();
        }
    }

    public void setMedia_mode(int media_mode) {
        this.media_mode = media_mode;
        preferences.edit().putInt("media_mode", media_mode).commit();
    }

    public int getMedia_mode() {
        return media_mode;
    }
}
