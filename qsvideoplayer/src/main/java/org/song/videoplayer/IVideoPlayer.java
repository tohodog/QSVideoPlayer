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
    int EVENT_ERROR = 13;//出错
    int EVENT_VIDEOSIZECHANGE = 14;//视频长宽大小
    int EVENT_COMPLETION = 15;//播放完成
    int EVENT_BUFFERING_UPDATA = 16;//缓冲进度


    void setUp(String url, Object... objects);//设置视频地址

    void play();//播放

    void pause();//暂停

    void seekTo(int duration);//进度调节

    void setPlayListener(PlayListener playListener);//播放监听 参数含义参照上面

    void setAspectRatio(int aspectRatio);//设置视频比例

    void setiMediaControl(int i);//设置解码模块

    boolean onBackPressed();//返回键退出全屏

    boolean isPlaying();//是否播放中

    int getPosition();//获取播放进度

    int getDuration();//获取视频时长

    int getCurrentMode();//获得播放器当前的模式(全屏,普通...)

    int getCurrentState();//获得播放器当前的状态(播放,暂停,完成...)

    void enterWindowFullscreen();//全屏

    void quitWindowFullscreen();//退出全屏

    void release();//销毁

}
