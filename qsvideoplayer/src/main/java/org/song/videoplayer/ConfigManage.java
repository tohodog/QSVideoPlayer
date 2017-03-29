package org.song.videoplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.song.videoplayer.media.AndroidMedia;
import org.song.videoplayer.media.ExoMedia;
import org.song.videoplayer.media.IjkExoMedia;
import org.song.videoplayer.media.IjkMedia;
import org.song.videoplayer.media.IMediaCallback;
import org.song.videoplayer.media.IMediaControl;
import org.song.videoplayer.rederview.IRenderView;
import org.song.videoplayer.rederview.SufaceRenderView;
import org.song.videoplayer.rederview.TextureRenderView;

/**
 * Created by song on 2017/2/10.
 */

public class ConfigManage {

    private static ConfigManage instance;

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
        if (MEDIA_MODE == 1)
            return new IjkMedia(iMediaCallback);
        if (MEDIA_MODE == 2 & Build.VERSION.SDK_INT >= 16)
            return new ExoMedia(iMediaCallback);
        if (MEDIA_MODE == 3 & Build.VERSION.SDK_INT >= 16)
            return new IjkExoMedia(iMediaCallback);
        return new AndroidMedia(iMediaCallback);
    }

    //后期扩展其他解码器 exo ijk... exo api需大于16
    public IMediaControl getIMediaControl(IMediaCallback iMediaCallback) {
        return getIMediaControl(iMediaCallback, media_mode);
    }

    public void setMedia_mode(int media_mode) {
        this.media_mode = media_mode;
        preferences.edit().putInt("media_mode", media_mode).commit();
    }

    public int getMedia_mode() {
        return media_mode;
    }
}
