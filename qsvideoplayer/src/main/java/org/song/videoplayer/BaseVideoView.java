package org.song.videoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.song.videoplayer.media.IMediaCallback;
import org.song.videoplayer.media.IMediaControl;
import org.song.videoplayer.rederview.IRenderView;
import org.song.videoplayer.rederview.SufaceRenderView;

/**
 * Created by song on 2017/2/13.
 * 和UI有关操作子类实现(除了设置进度 时间
 * UI界面由子类决定
 */
public abstract class BaseVideoView extends FrameLayout implements IVideoPlayer, IMediaCallback, View.OnClickListener, SeekBar.OnSeekBarChangeListener, HandleTouchEvent.GestureEvent {

    public static final String TAG = "QSVideoPlayer";
    public boolean isWindowGesture = false;//是否非全屏下也可以手势调节进度
    public int enterFullMode = 0;//进入全屏的模式 0横屏 1传感器自动横竖屏

    final int progressMax = 1000;

    protected int currentState = STATE_NORMAL;
    protected int currentMode = MODE_WINDOW_NORMAL;

    protected String url;
    //必须提供
    protected ImageView startButton;//播放按钮
    protected SeekBar progressBar;//进度条
    protected TextView currentTimeTextView, totalTimeTextView;//播放时间
    protected ViewGroup renderViewContainer;//suface容器
    protected ViewGroup bottomContainer;//底部 拿来判断是不是显示控制view用
    //子类可不提供
    protected ImageView startButton2;//播放按钮2
    //protected SeekBar progressBar2;//进度条2
    protected ImageView fullscreenButton;//全屏按钮
    protected ViewGroup topContainer;//顶部
    protected ViewGroup bufferingContainer;//缓冲
    protected ViewGroup loadingContainer;//初始化
    protected ViewGroup errorContainer;//出错了显示的 重试

    protected Handler mHandler;
    protected PlayListener playListener;
    private HandleTouchEvent handleTouchEvent;

    private View videoView;
    //protected boolean controlViewIsShow;//控制view是否显示


    public BaseVideoView(Context context) {
        this(context, null);
    }

    public BaseVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void init(Context context) {
        iMediaControl = ConfigManage.getInstance(getContext()).getIMediaControl(this);
        videoView = View.inflate(context, getLayoutId(), null);
        addView(videoView, new LayoutParams(-1, -1));
        //子类必须提供的控件 id已确定
        startButton = (ImageView) findViewById(R.id.start);
        progressBar = (SeekBar) findViewById(R.id.progress);
        currentTimeTextView = (TextView) findViewById(R.id.current);
        totalTimeTextView = (TextView) findViewById(R.id.total);
        bottomContainer = (ViewGroup) findViewById(R.id.layout_bottom);
        renderViewContainer = (ViewGroup) findViewById(R.id.surface_container);
        //可不提供
        fullscreenButton = (ImageView) findViewById(R.id.fullscreen);
        topContainer = (ViewGroup) findViewById(R.id.layout_top);
        bufferingContainer = (ViewGroup) findViewById(R.id.buffering_container);
        loadingContainer = (ViewGroup) findViewById(R.id.loading_container);
        errorContainer = (ViewGroup) findViewById(R.id.error_container);
        startButton2 = (ImageView) findViewById(R.id.start2);

        if (fullscreenButton != null)
            fullscreenButton.setOnClickListener(this);
        if (startButton2 != null)
            startButton2.setOnClickListener(this);
        startButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);
        progressBar.setMax(progressMax);
        bottomContainer.setOnClickListener(this);
        videoView.setOnClickListener(this);
        videoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouchEvent.handleEvent(event, BaseVideoView.this);
            }
        });

        mHandler = new Handler(Looper.getMainLooper());
        handleTouchEvent = new HandleTouchEvent(this);
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);


    }

    //-----------给外部调用的start----------
    @Override
    public void setUp(String url, Object... objects) {
        this.url = url;
        release();
        setUIWithStateAndMode(STATE_NORMAL, currentMode);
    }

    @Override
    public void play() {
        if (currentState != STATE_PLAYING)
            clickPlay();
    }

    @Override
    public void seekTo(int duration) {
        if (checkReady()) {
            if (duration >= 0) {
                seek(duration);
            }
        } else
            seekToInAdvance = duration;
    }

    @Override
    public void pause() {
        if (currentState == STATE_PLAYING)
            clickPlay();
    }

    @Override
    public void release() {
        iMediaControl.release();
        removeRenderView();
        setUIWithStateAndMode(STATE_NORMAL, currentMode);
        intiParams();
    }

    public void setPlayListener(PlayListener playListener) {
        this.playListener = playListener;
    }

    @Override
    public boolean onBackPressed() {
        if (currentMode != MODE_WINDOW_NORMAL) {
            quitWindowFullscreen();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        return iMediaControl.isPlaying();
    }


    //-----------给外部调用的end----------

    //视频长度/进度
    protected int duration = -1, position;

    public int getPosition() {
        return position;
    }

    public int getDuration() {
        return duration;
    }

    //初始化一些变量
    protected void intiParams() {
        duration = -1;
        position = 0;
    }

    private IRenderView iRenderView;

    //-----------各种流程逻辑-----------------
    //一开始点击准备播放--初始化
    protected void prepareMediaPlayer() {
        Log.d(TAG, "prepareMediaPlayer [" + this.hashCode() + "] ");
        removeRenderView();
        iMediaControl.doPrepar(getContext(), url, null);
        addRenderView();
        setUIWithStateAndMode(STATE_PREPARING, currentMode);
    }

    protected void addRenderView() {
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
        LayoutParams layoutParams =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        renderViewContainer.addView(iRenderView.get(), layoutParams);
    }

    protected void removeRenderView() {
        renderViewContainer.removeAllViews();
        if (iRenderView != null) {
            iRenderView.removeRenderCallback();
            iRenderView = null;
        }
    }

    //根据播放状态设置
    protected void setUIWithStateAndMode(final int status, final int mode) {
        Log.e("setUIWithStateAndMode", "status:" + status + " mode:" + mode);

        final int temp_status = this.currentState;
        final int temp_mode = this.currentMode;
        switch (status) {
            case STATE_NORMAL:
            case STATE_PREPARING:
                cancelProgressTimer();
                resetProgressAndTime();
                break;
            case STATE_PLAYING:
            case STATE_PAUSE:
                startProgressTimer();
                break;
            case STATE_ERROR:
                cancelProgressTimer();
                break;
            case STATE_AUTO_COMPLETE:
                cancelProgressTimer();
                setCompleProgressAndTime();
                break;
        }
        if (status == STATE_PLAYING)
            Util.KEEP_SCREEN_ON(getContext());
        else
            Util.KEEP_SCREEN_OFF(getContext());

        cancelDismissControlViewTimer();
        changeUiWithStateAndMode(status, mode);

        this.currentState = status;
        this.currentMode = mode;

        if (playListener != null) {
            if (temp_status != status)
                playListener.onStatus(status);
            if (temp_mode != mode)
                playListener.onMode(mode);
        }
    }

    private long tempLong;
    private boolean full_flag;//标记状态栏状态

    //全屏
    public void enterWindowFullscreen() {
        if (currentMode != MODE_WINDOW_FULLSCREEN & checkEnterOrFullOK()) {
            full_flag = Util.SET_FULL(getContext());
            if (enterFullMode == 1)
                Util.SET_SENSOR(getContext());
            else
                Util.SET_LANDSCAPE(getContext());

            ViewGroup vp = (ViewGroup) videoView.getParent();
            if (vp != null)
                vp.removeView(videoView);
            ViewGroup decorView = (ViewGroup) (Util.scanForActivity(getContext())).getWindow().getDecorView();
            //.findViewById(Window.ID_ANDROID_CONTENT);
            decorView.addView(videoView, new LayoutParams(-1, -1));
            setUIWithStateAndMode(currentState, MODE_WINDOW_FULLSCREEN);
        }
    }

    //退出全屏
    public void quitWindowFullscreen() {
        if (currentMode != MODE_WINDOW_NORMAL & checkEnterOrFullOK()) {
            if (!full_flag)
                Util.CLEAR_FULL(getContext());
            Util.SET_PORTRAIT(getContext());

            ViewGroup vp = (ViewGroup) videoView.getParent();
            if (vp != null)
                vp.removeView(videoView);
            addView(videoView, new LayoutParams(-1, -1));
            setUIWithStateAndMode(currentState, MODE_WINDOW_NORMAL);
        }
    }

    //防止频繁切换全屏
    private boolean checkEnterOrFullOK() {
        long now = System.currentTimeMillis();
        long d = now - tempLong;
        if (d > 1000)
            tempLong = now;
        return d > 1000;
    }

    protected void setProgressAndText() {
        int position = iMediaControl.getCurrentPosition();
        int duration = iMediaControl.getDuration();
        int progress = position * progressMax / (duration <= 0 ? 1 : duration);
        if (progress < 0)
            progress = 0;
        progressBar.setProgress(progress);
        currentTimeTextView.setText(Util.stringForTime(position));
        if (duration > 1)
            totalTimeTextView.setText(Util.stringForTime(duration));
        else {
            totalTimeTextView.setText("直播");
            duration = -1;
        }
        this.position = position;
        this.duration = duration;
    }

    protected void setBufferProgress(int bufferProgress) {
        progressBar.setSecondaryProgress(bufferProgress);
    }

    protected void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(Util.stringForTime(0));
        totalTimeTextView.setText(Util.stringForTime(0));
    }

    protected void setCompleProgressAndTime() {
        position = duration;
        progressBar.setProgress(progressMax);
        currentTimeTextView.setText(totalTimeTextView.getText());
    }

    //-----------各种流程逻辑end-----------------

    private IMediaControl iMediaControl;

    public void setiMediaControl(int i) {
        this.iMediaControl = ConfigManage.getInstance(getContext()).getIMediaControl(this, i);
    }

    private int seekToInAdvance;

    //-----------解码器回调start-----------------
    @Override
    public void onPrepared(IMediaControl iMediaControl) {
        Log.e("MediaCallBack", "onPrepared");
        setProgressAndText();
        if (seekToInAdvance > 0) {
            if (duration > 0)
                iMediaControl.seekTo(seekToInAdvance);
            seekToInAdvance = 0;
        }
        iMediaControl.doPlay();
        setUIWithStateAndMode(STATE_PLAYING, currentMode);
        //prepared(iMediaControl);
        if (playListener != null)
            playListener.onEvent(EVENT_PREPARED, 0);
    }

    //public abstract void prepared(IMediaControl iMediaControl);

    @Override
    public void onCompletion(IMediaControl iMediaControl) {
        Log.e("MediaCallBack", "onCompletion");
        setUIWithStateAndMode(STATE_AUTO_COMPLETE, currentMode);
        if (playListener != null)
            playListener.onEvent(EVENT_COMPLETION);
    }

    @Override
    public void onSeekComplete(IMediaControl iMediaControl) {
        Log.e("MediaCallBack", "onSeekComplete");

    }


    @Override
    public void onInfo(IMediaControl iMediaControl, int what, int extra) {
        Log.e("MediaCallBack", "onInfo" + " what" + what + " extra" + extra);
        if (what == IMediaControl.MEDIA_INFO_BUFFERING_START) {
            onBuffering(true);
            if (playListener != null)
                playListener.onEvent(EVENT_BUFFERING_START);
        }

        if (what == IMediaControl.MEDIA_INFO_BUFFERING_END) {
            onBuffering(false);
            if (playListener != null)
                playListener.onEvent(EVENT_BUFFERING_END);
        }
    }

    protected abstract void onBuffering(boolean isBuffering);

    @Override
    public void onVideoSizeChanged(IMediaControl iMediaControl, int width, int height) {
        Log.e("MediaCallBack", "onVideoSizeChanged" + " width:" + width + " height:" + height);
        iRenderView.setVideoSize(width, height);
        if (playListener != null)
            playListener.onEvent(EVENT_VIDEOSIZECHANGE, width, height);
    }

    @Override
    public void onError(IMediaControl iMediaControl, int what, int extra) {
        Log.e("MediaCallBack", "onError" + "what:" + what + " extra:" + extra);
        //if (what == 38 | extra == -38 | extra == -19)
        //    return;
        Toast.makeText(getContext(), "error: " + what + "," + extra, Toast.LENGTH_SHORT).show();
        iMediaControl.release();
        seekToInAdvance = position;//记录错误时进度
        setUIWithStateAndMode(STATE_ERROR, currentMode);
        if (playListener != null)
            playListener.onEvent(EVENT_ERROR, what, extra);
    }

    @Override
    public void onBufferingUpdate(IMediaControl iMediaControl, float percent) {
        Log.e("MediaCallBack", "onBufferingUpdate" + percent);
        setBufferProgress((int) (percent * progressMax));
        if (playListener != null)
            playListener.onEvent(EVENT_BUFFERING_UPDATA, (int) (percent * 100));
    }
    //-----------解码器回调end-----------------

    //-----------各种UI监听start-----------------
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start || i == R.id.start2) {
            clickPlay();
        }
        if (i == R.id.fullscreen) {
            clickFull();
        }
        if (v == videoView) {
            if (currentState == STATE_NORMAL || currentState == STATE_ERROR)
                clickPlay();
            else {
                if (bottomContainer.getVisibility() == VISIBLE)
                    dismissControlView();
                else
                    setUIWithStateAndMode(currentState, currentMode);
            }
        }
    }

    //点击时根据不同状态做出不同的反应
    protected void clickPlay() {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentState == STATE_NORMAL) {
            if (!url.startsWith("file") && !Util.isWifiConnected(getContext())) {
                if (showWifiDialog())
                    return;
            }
            prepareMediaPlayer();
        } else if (currentState == STATE_PLAYING) {
            iMediaControl.doPause();
            setUIWithStateAndMode(STATE_PAUSE, currentMode);
        } else if (currentState == STATE_PAUSE) {
            iMediaControl.doPlay();
            setUIWithStateAndMode(STATE_PLAYING, currentMode);
        } else if (currentState == STATE_AUTO_COMPLETE || currentState == STATE_ERROR) {
            prepareMediaPlayer();
        }
    }

    protected void clickFull() {
        if (currentMode == MODE_WINDOW_FULLSCREEN) {
            quitWindowFullscreen();
        } else {
            enterWindowFullscreen();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int time = seekBar.getProgress() * iMediaControl.getDuration() / progressMax;
        currentTimeTextView.setText(Util.stringForTime(time));
        //Log.i(TAG, "onProgressChanged " + Util.stringForTime(time));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
        cancelDismissControlViewTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int time = seekBar.getProgress() * iMediaControl.getDuration() / progressMax;
        seek(time);
        startProgressTimer();
        if (currentState == STATE_PLAYING)
            startDismissControlViewTimer(1314);
    }

    protected void seek(int time) {
        if (currentState == STATE_PLAYING ||
                currentState == STATE_PAUSE)
            if (duration > 0)
                iMediaControl.seekTo(time);
        if (currentState == STATE_AUTO_COMPLETE) {
            //seekToInAdvance = time;//播放完成 拖动进度条重新播放
            //prepareMediaPlayer();
            if (duration > 0)
                iMediaControl.seekTo(time);
            iMediaControl.doPlay();
            setUIWithStateAndMode(STATE_PLAYING, currentMode);
        }
    }
    //-----------各种UI监听end-----------------


    //-----------定时任务更新进度start-----------------
    protected void startProgressTimer() {
        cancelProgressTimer();
        mHandler.postDelayed(updateProgress, 500);
    }

    protected void cancelProgressTimer() {
        mHandler.removeCallbacks(updateProgress);
    }

    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(updateProgress, 500);
            setProgressAndText();
        }
    };
    //-----------定时任务更新进度end-----------------


    //-----------定时任务隐藏控制栏end-----------------

    protected void startDismissControlViewTimer() {
        startDismissControlViewTimer(2500);
    }

    protected void startDismissControlViewTimer(int delayed) {
        cancelDismissControlViewTimer();
        mHandler.postDelayed(dismissControlViewTimerRunnable, delayed);
    }

    protected void cancelDismissControlViewTimer() {
        mHandler.removeCallbacks(dismissControlViewTimerRunnable);
    }

    private Runnable dismissControlViewTimerRunnable = new Runnable() {
        @Override
        public void run() {
            dismissControlView();
        }
    };

    protected abstract void dismissControlView();

    //-----------定时任务隐藏控制栏end-----------------


    //-----------手势调节弹窗start-----------
    @Override
    public void onGestureBegin(int type) {
        if (!isWindowGesture & currentMode != MODE_WINDOW_FULLSCREEN)
            return;

        //进度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_FULL_X & checkReady())
            tempPosition = position;
        //亮度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_LEFT_Y) {
            tempBrightness = (int) (Util.scanForActivity(getContext()).getWindow().getAttributes().screenBrightness * 255);
            if (tempBrightness < 0)
                try {//系统亮度 不能activity取
                    tempBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                    tempBrightness = 0;
                }
        }
        //音量
        if (type == HandleTouchEvent.GestureEvent.TOUCH_RIGHT_Y)
            tempVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    }

    //调节前的值 亮度退出全屏应原样
    private int tempPosition;
    private int tempBrightness;
    private int tempVolume;
    protected AudioManager audioManager;

    @Override
    public void onGestureChange(int type, float level) {
        if (!isWindowGesture & currentMode != MODE_WINDOW_FULLSCREEN)
            return;
        //进度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_FULL_X & checkReady()) {
            if (duration <= 0)
                return;
            int delta = (int) (level * duration);
            if (delta < -tempPosition)
                delta = -tempPosition;
            if (delta > duration - tempPosition)
                delta = duration - tempPosition;
            showProgressDialog(delta, tempPosition, duration);
        }
        //亮度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_LEFT_Y) {
            WindowManager.LayoutParams params = Util.scanForActivity(getContext()).getWindow().getAttributes();
            int delta = (int) (level * 255);
            int nowBrightness = tempBrightness + delta;
            if (nowBrightness < 0)
                nowBrightness = 0;
            if (nowBrightness > 255)
                nowBrightness = 255;
            float b = nowBrightness / 255.0f;
            if (showBrightnessDialog((int) (b * 100), 100)) {
                params.screenBrightness = b;
                Util.scanForActivity(getContext()).getWindow().setAttributes(params);
            }
        }
        //音量
        if (type == HandleTouchEvent.GestureEvent.TOUCH_RIGHT_Y) {
            int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int deltaV = (int) (max * level);
            int nowVolume = tempVolume + deltaV;
            if (nowVolume < 0)
                nowVolume = 0;
            if (nowVolume > max)
                nowVolume = max;
            if (showVolumeDialog(nowVolume, max))
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nowVolume, 0);
        }
    }

    @Override
    public void onGestureEnd(int type, float level) {
        //双击
        if (type == HandleTouchEvent.GestureEvent.TOUCH_DOUBLE_C)
            clickFull();

        if (!isWindowGesture & currentMode != MODE_WINDOW_FULLSCREEN)
            return;
        //进度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_FULL_X & checkReady()) {
            if (duration <= 0)
                return;
            if (!dismissProgressDialog()) return;

            int delta = (int) (level * duration);
            tempPosition += delta;
            if (tempPosition > duration)
                tempPosition = duration;
            if (tempPosition < 0)
                tempPosition = 0;
            seekTo(tempPosition);
            tempPosition = 0;

        }
        //亮度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_LEFT_Y) {
            dismissBrightnessDialog();
        }
        //音量
        if (type == HandleTouchEvent.GestureEvent.TOUCH_RIGHT_Y) {
            dismissVolumeDialog();
        }


    }

    //子类写了实现 就返回true
    protected abstract boolean showWifiDialog();//要弹出非wifi提示框覆盖return true即可

    protected abstract boolean showProgressDialog(int delay, int position, int duration);

    protected abstract boolean dismissProgressDialog();

    protected abstract boolean showVolumeDialog(int nowVolume, int maxVolume);

    protected abstract boolean dismissVolumeDialog();

    protected abstract boolean showBrightnessDialog(int nowBrightness, int maxBrightness);

    protected abstract boolean dismissBrightnessDialog();
    //-----------各种调节弹窗end-----------


    /**
     * 提供的布局至少需要包含6个控件 见44行
     * id见ids.xml
     */
    protected abstract int getLayoutId();

    protected abstract void changeUiWithStateAndMode(int status, int mode);

    protected boolean checkReady() {
        return currentState != STATE_NORMAL
                & currentState != STATE_PREPARING
                & currentState != STATE_ERROR;

    }

    public int getCurrentMode() {
        return currentMode;
    }

    public int getCurrentState() {
        return currentState;
    }
}
