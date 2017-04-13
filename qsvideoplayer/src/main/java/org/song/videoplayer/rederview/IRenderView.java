package org.song.videoplayer.rederview;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import org.song.videoplayer.media.IMediaControl;

/**
 * 接口化渲染view
 */
public interface IRenderView {
    int AR_ASPECT_FIT_PARENT = 0; // 根据视频大小缩放 填充屏幕 *默认
    int AR_ASPECT_FILL_PARENT = 1; // 根据视频大小缩放 完全填充屏幕
    int AR_ASPECT_WRAP_CONTENT = 2;//原画
    int AR_MATCH_PARENT = 3;//强制拉伸全屏 会变形
    int AR_16_9_FIT_PARENT = 4;//强制16:9 填充屏幕
    int AR_4_3_FIT_PARENT = 5;//强制4:3 填充屏幕

    //boolean shouldWaitForResize();

    View get();

    void setVideoSize(int videoWidth, int videoHeight);


    //void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen);

    void setVideoRotation(int degree);

    void setAspectRatio(int aspectRatio);

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
