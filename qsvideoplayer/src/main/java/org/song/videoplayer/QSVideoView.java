package org.song.videoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.song.videoplayer.cache.CacheManager;
import org.song.videoplayer.floatwindow.FloatParams;
import org.song.videoplayer.floatwindow.FloatWindowHelp;
import org.song.videoplayer.media.BaseMedia;
import org.song.videoplayer.media.IMediaCallback;
import org.song.videoplayer.media.IMediaControl;
import org.song.videoplayer.media.IjkMedia;
import org.song.videoplayer.rederview.IRenderView;
import org.song.videoplayer.rederview.SufaceRenderView;

import java.util.List;
import java.util.Map;

/**
 * Created by song on 2017/2/13.
 * edit on 2017/4/8.
 * 轻松视频播放器,提供完整控制功能,没有控制ui
 * Github: https://github.com/tohodog/QSVideoPlayer
 */

public class QSVideoView extends FrameLayout implements IVideoPlayer, IMediaCallback {

    public static final String TAG = "QSVideoView";
    /**
     * 进入全屏的模式 0横屏 1竖屏 2传感器自动横竖屏 3根据视频比例自动确定横竖屏      -1什么都不做
     */
    public int enterFullMode = 3;

    private IMediaControl iMediaControl;
    protected HandlePlayListener handlePlayListener;

    protected FrameLayout videoView;
    private FrameLayout renderViewContainer;//suface容器
    private IRenderView iRenderView;
    protected FloatWindowHelp floatWindowHelp;

    protected String url;
    protected Map<String, String> headers;
    protected Object option;
    public int urlMode;//0网络 -1本地 1直播流

    protected int currentState = STATE_NORMAL;
    protected int currentMode = MODE_WINDOW_NORMAL;
    protected int seekToInAdvance;//初始化视频完成跳进度
    protected boolean noPlayInAdvance;//初始化视频完成 是否播放
    protected int aspectRatio;
    protected boolean isMute, openCache;
    protected float rate;


    public QSVideoView(Context context) {
        this(context, null);
    }

    public QSVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QSVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        handlePlayListener = new HandlePlayListener();
        iMediaControl = ConfigManage.getInstance(getContext()).getMediaControl(this, IjkMedia.class);
        floatWindowHelp = new FloatWindowHelp(context);
        videoView = new FrameLayout(context);
        videoView.setId(R.id.qs_videoview);
        renderViewContainer = new FrameLayout(context);
        renderViewContainer.setBackgroundColor(Color.BLACK);
        videoView.addView(renderViewContainer, new LayoutParams(-1, -1));
        addView(videoView, new LayoutParams(-1, -1));
    }


    //-----------给外部调用的start----------
    @Override
    public void setUp(String url, Map<String, String> headers, Object option) {
        if (STATE_NORMAL != currentState)
            release();
        this.url = url;
        this.urlMode = Util.PaserUrl(url);
        this.headers = headers;
        this.option = option;
        setStateAndMode(STATE_NORMAL, currentMode);
    }

    public void setUp(String url, Map<String, String> headers, List<IjkMedia.Option> optionList) {
        setUp(url, headers, (Object) optionList);
    }

    public void setUp(String url) {
        setUp(url, null, (Object) null);
    }

    public void setUp(String url, Map<String, String> headers) {
        setUp(url, headers, (Object) null);
    }


    @Override
    public void play() {
        if (currentState != STATE_PLAYING) {
            clickPlay();
        }
    }

    @Override
    public void prePlay() {
        noPlayInAdvance = true;
        play();
    }

    @Override
    public void seekTo(int duration) {
        if (checkReady()) {
            if (duration >= 0) {
                seek(duration);
            }
        } else {
            seekToInAdvance = duration;
        }
    }

    @Override
    public void pause() {
        if (currentState == STATE_PLAYING) {
            clickPlay();
        }
    }


    @Override
    public void setPlayListener(final PlayListener playListener) {
        handlePlayListener.setListener(playListener);
        post(new Runnable() {
            @Override
            public void run() {
                playListener.onMode(currentMode);
                playListener.onStatus(currentState);
            }
        });
    }

    @Override
    public void addPlayListener(final PlayListener playListener) {
        handlePlayListener.addListener(playListener);
        post(new Runnable() {
            @Override
            public void run() {
                playListener.onMode(currentMode);
                playListener.onStatus(currentState);
            }
        });
    }

    @Override
    public void removePlayListener(PlayListener playListener) {
        handlePlayListener.removeListener(playListener);
    }


    @Override
    public boolean onBackPressed() {
        if (currentMode == MODE_WINDOW_FULLSCREEN) {
            quitWindowFullscreen();
            return true;
        } else if (currentMode == MODE_WINDOW_FLOAT_SYS) {
            //Util.scanForActivity(getContext()).moveTaskToBack(true);
            return true;
        }
        return false;
    }


    @Override
    public void setAspectRatio(int aspectRatio) {
        if (iRenderView != null)
            iRenderView.setAspectRatio(aspectRatio);
        this.aspectRatio = aspectRatio;
    }

    @Override
    public void setDecodeMedia(Class<? extends BaseMedia> claxx) {
        this.iMediaControl = ConfigManage.getInstance(getContext()).getMediaControl(this, claxx);
    }

    @Override
    public void openCache() {
        openCache = true;
    }

    @Override
    public boolean isPlaying() {
        return iMediaControl.isPlaying();
    }

    @Override
    public int getPosition() {
        return iMediaControl.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return iMediaControl.getDuration();
    }

    @Override
    public int getCurrentMode() {
        return currentMode;
    }

    @Override
    public int getCurrentState() {
        return currentState;
    }

    @Override
    public Bitmap getCurrentFrame() {
        return iRenderView == null ? null : iRenderView.getCurrentFrame();
    }

    @Override
    public void release() {
        iMediaControl.release();
        removeRenderView();
        setStateAndMode(STATE_NORMAL, currentMode);
        initParams();
        handlePlayListener.onEvent(EVENT_RELEASE);
    }

    //异步销毁，解决list视频销毁卡顿问题
    public void releaseInThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                iMediaControl.release();
            }
        }).start();
        removeRenderView();
        initParams();
        setStateAndMode(STATE_NORMAL, currentMode);
        handlePlayListener.onEvent(EVENT_RELEASE);
    }


    private long tempLong;
    private boolean full_flag;//标记状态栏状态
    private boolean orientation_flag;//标记横竖屏状态

    //全屏
    @Override
    public void enterWindowFullscreen() {
        if (currentMode == MODE_WINDOW_NORMAL & checkSpaceOK()) {
            Util.hideBottomUIMenu(getContext());

            int mode = enterFullMode;
            if (mode == 3) {
                mode = height > width ? 1 : 0;
            }

            full_flag = Util.SET_FULL(getContext());
            orientation_flag = Util.isScreenOriatationPortrait(getContext());

            if (mode == 0) {
                Util.SET_LANDSCAPE(getContext());
            } else if (mode == 1) {
                Util.SET_PORTRAIT(getContext());
            } else if (mode == 2) {
                Util.SET_SENSOR(getContext());
            }

            ViewGroup vp = (ViewGroup) videoView.getParent();
            if (vp != null) vp.removeView(videoView);
            ViewGroup decorView = (ViewGroup) (Util.scanForActivity(getContext())).getWindow().getDecorView();
            //.findViewById(Window.ID_ANDROID_CONTENT);
            decorView.addView(videoView, new LayoutParams(-1, -1));
            setStateAndMode(currentState, MODE_WINDOW_FULLSCREEN);
        }
    }

    //退出全屏
    @Override
    public void quitWindowFullscreen() {
        if (currentMode == MODE_WINDOW_FULLSCREEN & checkSpaceOK()) {
            Util.showBottomUIMenu(getContext());

            if (full_flag) {
                Util.SET_FULL(getContext());
            } else {
                Util.CLEAR_FULL(getContext());
            }
            if (orientation_flag) {
                Util.SET_PORTRAIT(getContext());
            } else {
                Util.SET_LANDSCAPE(getContext());
            }


            ViewGroup vp = (ViewGroup) videoView.getParent();
            if (vp != null) vp.removeView(videoView);
            addView(videoView, new LayoutParams(-1, -1));
            setStateAndMode(currentState, MODE_WINDOW_NORMAL);
        }
    }


    //浮窗
    @Override
    public boolean enterWindowFloat(FloatParams floatParams) {
        boolean b = true;
        if (currentMode == MODE_WINDOW_NORMAL && floatParams != null) {
            ViewGroup vp = (ViewGroup) videoView.getParent();
            if (vp != null) vp.removeView(videoView);
            b = floatWindowHelp.enterWindowFloat(videoView, floatParams);
            if (b) {
                setStateAndMode(currentState, floatParams.systemFloat ? MODE_WINDOW_FLOAT_SYS : MODE_WINDOW_FLOAT_ACT);
            } else {
                addView(videoView, new LayoutParams(-1, -1));
            }
        }
        return b;
    }

    //退出浮窗
    @Override
    public void quitWindowFloat() {
        if (isWindowFloatMode()) {
            ViewGroup vp = (ViewGroup) videoView.getParent();
            if (vp != null) vp.removeView(videoView);
            addView(videoView, new LayoutParams(-1, -1));
            floatWindowHelp.quieWindowFloat();
            setStateAndMode(currentState, MODE_WINDOW_NORMAL);
        }
    }

    //静音
    @Override
    public boolean setMute(boolean isMute) {
        this.isMute = isMute;
        int v = isMute ? 0 : 1;
        return iMediaControl.setVolume(v, v);
    }


    @Override
    public boolean setSpeed(float rate) {
        this.rate = rate;
        return iMediaControl.setSpeed(rate);
    }

    //防止频繁切换全屏
    private boolean checkSpaceOK() {
        long now = System.currentTimeMillis();
        long d = now - tempLong;
        if (d > 888)
            tempLong = now;
        return d > 888;
    }

    //-----------给外部调用的end----------


    //-----------解码器回调start-----------------
    @Override
    public void onPrepared(IMediaControl iMediaControl) {
        Log.e(TAG, "onPrepared");
        setMute(isMute);
        setSpeed(rate);
        if (!noPlayInAdvance) {
            iMediaControl.doPlay();
            setStateAndMode(STATE_PLAYING, currentMode);
        } else {
            noPlayInAdvance = false;
            setStateAndMode(STATE_PAUSE, currentMode);
        }

        handlePlayListener.onEvent(EVENT_PREPARE_END);
        handlePlayListener.onEvent(EVENT_PLAY, 1);

        if (seekToInAdvance > 0) {
            iMediaControl.seekTo(seekToInAdvance);
            handlePlayListener.onEvent(EVENT_SEEK_TO, seekToInAdvance);
            seekToInAdvance = 0;
        }
    }


    @Override
    public void onCompletion(IMediaControl iMediaControl) {
        Log.e(TAG, "onCompletion");
        setStateAndMode(STATE_AUTO_COMPLETE, currentMode);
        handlePlayListener.onEvent(EVENT_COMPLETION);
    }

    @Override
    public void onSeekComplete(IMediaControl iMediaControl) {
        Log.e(TAG, "onSeekComplete");
        handlePlayListener.onEvent(EVENT_SEEK_COMPLETION, getPosition());
    }

    @Override
    public void onInfo(IMediaControl iMediaControl, int what, int extra) {
        Log.e(TAG, "onInfo" + " what" + what + " extra" + extra);
        if ((what == 804 | what == 805) & extra == -1004) {
            onError(iMediaControl, what, extra);//8.0断流走的onInfo
        }

        if (what == IMediaControl.MEDIA_INFO_BUFFERING_START) {
            onBuffering(true);
            handlePlayListener.onEvent(EVENT_BUFFERING_START, getPosition());
        }

        if (what == IMediaControl.MEDIA_INFO_BUFFERING_END) {
            onBuffering(false);
            handlePlayListener.onEvent(EVENT_BUFFERING_END, getPosition());
        }
    }

    private int width, height;

    @Override
    public void onVideoSizeChanged(IMediaControl iMediaControl, int width, int height) {
        Log.e(TAG, "onVideoSizeChanged" + " width:" + width + " height:" + height);
        iRenderView.setVideoSize(width, height);
        this.width = width;
        this.height = height;
        handlePlayListener.onEvent(EVENT_VIDEOSIZECHANGE, width, height);
    }

    @Override
    public void onError(IMediaControl iMediaControl, int what, int extra) {
        Log.e(TAG, "onError" + "what:" + what + " extra:" + extra);
        //if (what == 38 | extra == -38 | extra == -19)
        //    return;
        Toast.makeText(getContext(), "error: " + what + "," + extra, Toast.LENGTH_SHORT).show();
        seekToInAdvance = getPosition();//记录错误时进度
        iMediaControl.release();
        setStateAndMode(STATE_ERROR, currentMode);
        handlePlayListener.onEvent(EVENT_ERROR, what, extra);
    }

    @Override
    public void onBufferingUpdate(IMediaControl iMediaControl, float bufferProgress) {
        //Log.e(TAG, "onBufferingUpdate" + bufferProgress);
        setBufferProgress(bufferProgress);
        handlePlayListener.onEvent(EVENT_BUFFERING_UPDATE, (int) (bufferProgress * 100));
    }

    //给子类覆盖
    protected void onBuffering(boolean isBuffering) {
    }

    //给子类覆盖 0~progressMax
    protected void setBufferProgress(float bufferProgress) {
    }

    //-----------解码器回调end-----------------


    //-----------各种流程逻辑start-----------------

    //初始化一些变量
    protected void initParams() {
        this.width = 0;
        this.height = 0;
    }

    //设置播放状态
    private void setStateAndMode(final int status, final int mode) {
        if (Looper.getMainLooper() == Looper.myLooper())
            setUIWithStateAndMode(status, mode);
        else
            post(new Runnable() {
                @Override
                public void run() {
                    setUIWithStateAndMode(status, mode);
                }
            });
    }

    protected void setUIWithStateAndMode(final int status, final int mode) {
        Log.e(TAG, "status:" + status + " mode:" + mode);
        if (status == STATE_PLAYING)
            Util.KEEP_SCREEN_ON(getContext());
        else
            Util.KEEP_SCREEN_OFF(getContext());

        final int temp_status = this.currentState;
        final int temp_mode = this.currentMode;
        this.currentState = status;
        this.currentMode = mode;
        if (temp_status != status)
            handlePlayListener.onStatus(status);
        if (temp_mode != mode)
            handlePlayListener.onMode(mode);

    }

    //点击时根据不同状态做出不同的反应
    protected void clickPlay() {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentState == STATE_NORMAL) {
            if (urlMode >= 0 && !Util.isWifiConnected(getContext())) {
                if (showWifiDialog())
                    return;
            }
            prepareMediaPlayer();
        } else if (currentState == STATE_PLAYING) {
            iMediaControl.doPause();
            setStateAndMode(STATE_PAUSE, currentMode);
            handlePlayListener.onEvent(EVENT_PAUSE);
        } else if (currentState == STATE_PAUSE) {
            iMediaControl.doPlay();
            setStateAndMode(STATE_PLAYING, currentMode);
            handlePlayListener.onEvent(EVENT_PLAY);
        } else if (currentState == STATE_AUTO_COMPLETE || currentState == STATE_ERROR) {
            prepareMediaPlayer();
        }
    }

    protected boolean showWifiDialog() {
        return false;
    }

    //一开始点击准备播放--初始化
    protected void prepareMediaPlayer() {
        Log.e(TAG, "prepareMediaPlayer [" + this.hashCode() + "] ");
        removeRenderView();
        String url = this.url;
        if (openCache && urlMode == 0) {
            url = CacheManager.buildCahchUrl(getContext(), url, headers);
        }
        boolean res = iMediaControl.doPrepar(getContext(), url, headers, option);
        if (res) {
            addRenderView();
            setStateAndMode(STATE_PREPARING, currentMode);
            handlePlayListener.onEvent(EVENT_PREPARE_START);
        }
    }

    private void addRenderView() {
        iRenderView = ConfigManage.getInstance(getContext()).getIRenderView(getContext());
        iRenderView.addRenderCallback(new IRenderView.IRenderCallback() {
            @Override
            public void onSurfaceCreated(IRenderView holder, int width, int height) {
                holder.bindMedia(iMediaControl);
            }

            @Override
            public void onSurfaceChanged(IRenderView holder, int format, int width, int height) {

            }

            @Override
            public void onSurfaceDestroyed(IRenderView holder) {
                if (holder instanceof SufaceRenderView) {
                    iMediaControl.setDisplay(null);
                }
            }
        });
        iRenderView.setAspectRatio(aspectRatio);
        LayoutParams layoutParams = new LayoutParams(-1, -1, Gravity.CENTER);
        renderViewContainer.addView(iRenderView.get(), layoutParams);
    }

    private void removeRenderView() {
        renderViewContainer.removeAllViews();
        if (iRenderView != null) {
            iRenderView.removeRenderCallback();
            iRenderView = null;
        }
    }


    private void seek(int time) {
        if (currentState == STATE_PLAYING ||
                currentState == STATE_PAUSE)
            iMediaControl.seekTo(time);
        if (currentState == STATE_AUTO_COMPLETE) {
            //seekToInAdvance = time;//播放完成 拖动进度条重新播放
            //prepareMediaPlayer();
            iMediaControl.seekTo(time);
            iMediaControl.doPlay();
            setStateAndMode(STATE_PLAYING, currentMode);
            handlePlayListener.onEvent(EVENT_PLAY);
        }
        handlePlayListener.onEvent(EVENT_SEEK_TO, time);
    }


    protected boolean checkReady() {
        return currentState != STATE_NORMAL
                & currentState != STATE_PREPARING
                & currentState != STATE_ERROR;
    }

    public String getUrl() {
        return url;
    }

    public int getVideoWidth() {
        return width;
    }

    public int getVideoHeight() {
        return height;
    }


    public FloatParams getFloatParams() {
        return floatWindowHelp.getFloatParams();
    }

    ////是否系统浮窗模式
    public boolean isSystemFloatMode() {
        return currentMode == MODE_WINDOW_FLOAT_SYS;
    }

    //是否浮窗模式
    public boolean isWindowFloatMode() {
        return currentMode == MODE_WINDOW_FLOAT_SYS || currentMode == MODE_WINDOW_FLOAT_ACT;
    }

}
