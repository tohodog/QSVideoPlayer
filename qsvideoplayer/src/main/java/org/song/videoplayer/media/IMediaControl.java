package org.song.videoplayer.media;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Map;

/**
 * Created by song on 2017/2/10.
 * Contact github.com/tohodog
 * 解码器控制
 */

public interface IMediaControl {

    int MEDIA_INFO_UNKNOWN = 1;
    int MEDIA_INFO_STARTED_AS_NEXT = 2;
    int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    int MEDIA_INFO_BUFFERING_START = 701;
    int MEDIA_INFO_BUFFERING_END = 702;
    int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    int MEDIA_INFO_BAD_INTERLEAVING = 800;
    int MEDIA_INFO_NOT_SEEKABLE = 801;
    int MEDIA_INFO_METADATA_UPDATE = 802;
    int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;
    int MEDIA_INFO_AUDIO_RENDERING_START = 10002;
    int MEDIA_ERROR_UNKNOWN = 1;
    int MEDIA_ERROR_SERVER_DIED = 100;
    int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    int MEDIA_ERROR_IO = -1004;
    int MEDIA_ERROR_MALFORMED = -1007;
    int MEDIA_ERROR_UNSUPPORTED = -1010;
    int MEDIA_ERROR_TIMED_OUT = -110;

    void doPrepar(Context context, String url, Map<String, String> headers);//准备播放

    void setSurface(Surface surface);//api14以上用这个 可以无缝全屏切换

    void setDisplay(SurfaceHolder surfaceHolder);//api14一下用这个 全屏切换会顿一下

    void doPlay();//播放

    void doPause();//暂停

    void seekTo(int du);//调整播放进度

    int getCurrentPosition();//当前播放进度

    int getDuration();//视频长度

    int getVideoHeight();

    int getVideowidth();

    boolean isPlaying();

    boolean setVolume(float leftVol,float rightVol);

    void release();//销毁

}
