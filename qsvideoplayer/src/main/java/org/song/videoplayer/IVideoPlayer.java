package org.song.videoplayer;

import android.graphics.Bitmap;

import org.song.videoplayer.floatwindow.FloatParams;
import org.song.videoplayer.media.BaseMedia;

/**
 * Created by song on 2017/2/10.
 * Contact github.com/tohodog
 * 视频播放器
 */
public interface IVideoPlayer {

    //播放器当前状态
    int STATE_NORMAL = 0;//未播放
    int STATE_PREPARING = 1;//初始化中
    int STATE_PLAYING = 2;//播放中
    //去掉这个状态 缓冲只是显示个进度 不影响其他状态
    //int STATE_PLAYING_BUFFERING_START = 3;//缓冲
    int STATE_PAUSE = 4;//暂停中
    int STATE_AUTO_COMPLETE = 5;//播放完成
    int STATE_ERROR = 6;//播放出错

    //播放器容器模式
    int MODE_WINDOW_NORMAL = 100;//普通模式
    int MODE_WINDOW_FULLSCREEN = 101;//全屏模式
    int MODE_WINDOW_FLOAT_SYS = 102;//系统悬浮窗口模式 需要权限
    int MODE_WINDOW_FLOAT_ACT = 103;//界面内悬浮窗口模式

    //播放事件
    int EVENT_PREPARE_START = 10;//初始化开始
    int EVENT_PREPARE_END = 11;//初始化完成
    int EVENT_PLAY = 12;//播放事件
    int EVENT_PAUSE = 13;//暂停事件
    int EVENT_BUFFERING_START = 14;//缓冲
    int EVENT_BUFFERING_END = 15;//缓冲结束
    int EVENT_ERROR = 16;//出错
    int EVENT_VIDEOSIZECHANGE = 17;//视频长宽大小
    int EVENT_COMPLETION = 18;//播放完成
    int EVENT_BUFFERING_UPDATE = 19;//缓冲进度
    int EVENT_SEEK_COMPLETION = 20;//调节进度完成
    int EVENT_SEEK_TO = 21;//调节进度

    int EVENT_RELEASE = 88;//销毁事件

    /**
     * 设置视频参数
     *
     * @param url     视频地址
     * @param objects [0] 视频标题
     *                [1] Map<String, String> headers
     */
    void setUp(String url, Object... objects);

    void play();//播放

    void pause();//暂停

    void seekTo(int duration);//进度调节

    void setPlayListener(PlayListener playListener);//播放监听 参数含义参照本类

    void addPlayListener(PlayListener playListener);//多播放监听

    void removePlayListener(PlayListener playListener);//移除播放监听

    void setAspectRatio(int aspectRatio);//设置视频比例 参数见IRenderView

    void setDecodeMedia(Class<? extends BaseMedia> claxx);//设置解码模块

    boolean onBackPressed();//返回键退出全屏

    boolean isPlaying();//是否播放中

    void enterWindowFullscreen();//全屏

    void quitWindowFullscreen();//退出全屏

    boolean enterWindowFloat(FloatParams floatParams);//浮窗 false没权限

    void quitWindowFloat();//退出浮窗

    boolean setMute(boolean isMute);//是否静音 false不支持

    void release();//销毁

    Bitmap getCurrentFrame();//截图

    int getPosition();//获取播放进度

    int getDuration();//获取视频时长

    int getVideoWidth();//获取视频宽

    int getVideoHeight();//获取视频长

    int getCurrentMode();//获得播放器当前的模式(全屏,普通...)

    int getCurrentState();//获得播放器当前的状态(播放,暂停,完成...)

}
