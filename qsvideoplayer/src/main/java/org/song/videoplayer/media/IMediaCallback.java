package org.song.videoplayer.media;

/**
 * Created by song on 2017/2/10.
 * Contact github.com/tohodog
 * 解码器回调
 */

public interface IMediaCallback {


    void onPrepared(IMediaControl iMediaControl);//准备完毕

    void onCompletion(IMediaControl iMediaControl);//播放完毕

    void onSeekComplete(IMediaControl iMediaControl);//拖动进度条完毕

    void onInfo(IMediaControl iMediaControl, int what, int extra);//播放事件 [缓冲 缓冲完毕

    void onVideoSizeChanged(IMediaControl iMediaControl, int width, int height);//视频尺寸变化

    void onError(IMediaControl iMediaControl, int what, int extra);//播放出错

    void onBufferingUpdate(IMediaControl iMediaControl, final float percent);//缓冲进度 0~1
}
