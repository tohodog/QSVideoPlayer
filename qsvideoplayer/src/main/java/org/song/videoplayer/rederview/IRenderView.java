package org.song.videoplayer.rederview;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import org.song.videoplayer.media.IMediaControl;

/**
 * 接口化渲染view
 */
public interface IRenderView {
    int AR_ASPECT_FIT_PARENT = 0; // without clip
    int AR_ASPECT_FILL_PARENT = 1; // may clip
    int AR_ASPECT_WRAP_CONTENT = 2;
    int AR_MATCH_PARENT = 3;
    int AR_16_9_FIT_PARENT = 4;
    int AR_4_3_FIT_PARENT = 5;

    //boolean shouldWaitForResize();

    View get();

    void setVideoSize(int videoWidth, int videoHeight);


    //void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen);

    void setVideoRotation(int degree);

    //void setAspectRatio(int aspectRatio);

    void addRenderCallback(IRenderCallback callback);

    void removeRenderCallback();

    SurfaceHolder getSurfaceHolder();

    Surface openSurface();

    void bindMedia(IMediaControl iMediaControl);

    interface IRenderCallback {
        void onSurfaceCreated(IRenderView holder, int width, int height);

        void onSurfaceChanged(IRenderView holder, int format, int width, int height);

        void onSurfaceDestroyed(IRenderView holder);
    }
}
