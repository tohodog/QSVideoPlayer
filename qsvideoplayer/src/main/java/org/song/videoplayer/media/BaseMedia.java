package org.song.videoplayer.media;

import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

/**
 * Created by song on 2017/2/10.
 * Contact github.com/tohodog
 * 解码器父类
 */

public abstract class BaseMedia implements IMediaControl {

    protected IMediaCallback iMediaCallback;
    protected Surface surface;
    protected boolean isPrepar;
    protected Handler mainThreadHandler;

    public Surface getSurface() {
        return surface;
    }

    public BaseMedia(IMediaCallback iMediaCallback) {
        if (iMediaCallback == null)
            throw new IllegalArgumentException();
        this.iMediaCallback = iMediaCallback;
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }
}
