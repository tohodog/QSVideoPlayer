package org.song.videoplayer;

/**
 * Created by song on 2017/2/10.
 * 视频播放器
 */

public interface IVideoPlayer {

    int STATE_NORMAL = 0;//未播放
    int STATE_PREPARING = 1;//初始化中
    int STATE_PLAYING = 2;//播放中

    //去掉这个状态 缓冲只是显示个进度 不影响其他状态
    //int STATE_PLAYING_BUFFERING_START = 3;//缓冲

    int STATE_PAUSE = 5;//暂停中
    int STATE_AUTO_COMPLETE = 6;//播放完成
    int STATE_ERROR = 7;//播放出错

    int MODE_WINDOW_NORMAL = 100;//普通模式
    int MODE_WINDOW_FULLSCREEN = 101;//全屏模式
    int MODE_WINDOW_TINY = 102;//小窗口模式

    int EVENT_BUFFERING_START = 10;//缓冲
    int EVENT_BUFFERING_END = 11;//缓冲结束
    int EVENT_PREPARED = 12;//初始化完成
    int EVENT_ERROR = 13;//
    int EVENT_VIDEOSIZECHANGE = 14;//
    int EVENT_COMPLETION = 15;//
    int EVENT_BUFFERING_UPDATA = 16;//


    void setUp(String url, Object... objects);

    void play();

    void pause();

    void seekTo(int duration);

    void setPlayListener(PlayListener playListener);

    boolean onBackPressed();

    boolean isPlaying();

    int getPosition();

    int getDuration();

    void release();

}
