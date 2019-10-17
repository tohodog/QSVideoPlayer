package org.song.videoplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by song on 2017/2/13.
 * Contact github.com/tohodog
 * UI界面由子类决定
 * edit on 2017/4/8.
 * 分离出QSVideoView,本类作为辅助类.
 * <p>
 * *1.提供播放器常见的ui的控制逻辑(播放按钮 进度条 时间等)
 * *2.提供手势支持
 * <p>
 * 减轻开发者工作量
 * 子类提供的xml按规定提供控件的id 本类即可完成该控件的逻辑
 */

public abstract class QSVideoViewHelp extends QSVideoView implements HandleTouchEvent.GestureEvent, SeekBar.OnSeekBarChangeListener {

    public static final int EVENT_CONTROL_VIEW = 1001;//控制view显示隐藏事件 extra[0]：0显示 1隐藏
    public static final int EVENT_SEEKBAR_START = 1002;//进度条拖动事件开始
    public static final int EVENT_SEEKBAR_TOUCHING = 1003;//拖动中 extra[0]进度 extra[1]总进度
    public static final int EVENT_SEEKBAR_END = 1004;//进度条拖动事件结束
    public static final int EVENT_CLICK_VIEW = 1005;//点击事件

    public int controlUIHideTime = 2500;//控制UI隐藏时间
    public boolean isDoneShowControlUI = false;//初始化完成/视频播放完成后,是否显示控制UI
    public boolean isWindowGesture = false;//是否非全屏下也可以手势调节进度

    protected ViewGroup controlContainer;//控制ui容器
    //提供辅助的控件
    protected TextView titleTextView;//标题
    protected TextView definitionTextView;//清晰度
    protected ImageView startButton, startButton2;//播放按钮
    protected SeekBar seekBar;//拖动条
    protected TextView currentTimeTextView, totalTimeTextView;//播放时间/视频长度
    protected ImageView fullscreenButton;//全屏按钮
    protected ProgressBar progressBar;//第二进度条
    protected View backView, floatCloseView, floatBackView;//返回
    protected final int progressMax = 1000;

    protected boolean isShowControlView;
    protected Handler mHandler;
    private HandleTouchEvent handleTouchEvent;
    private MyOnClickListener myOnClickListener;

    private List<QSVideo> qsVideos;
    private QSVideo nowPlayVideo;

    public QSVideoViewHelp(Context context) {
        this(context, null);
    }

    public QSVideoViewHelp(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QSVideoViewHelp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHelpView(context);
    }

    public void setUp(String url, String title) {
        setUp(QSVideo.Build(url).title(title).build());
    }

    public void setUp(QSVideo... qsVideos) {
        setUp(Arrays.asList(qsVideos));
    }

    public void setUp(List<QSVideo> qsVideos) {
        this.qsVideos = qsVideos;
        switchVideo(0);
    }

    //切视频
    protected void switchVideo(int index) {
        if (qsVideos != null && qsVideos.size() > index) {
            if (qsVideos.indexOf(nowPlayVideo) == index)
                return;

            int position = getPosition();
            boolean isPlaying = isPlaying();
            boolean checkReady = checkReady();

            nowPlayVideo = qsVideos.get(index);
            super.setUp(nowPlayVideo.url(), nowPlayVideo.headers(), nowPlayVideo.option());
            if (titleTextView != null) titleTextView.setText(nowPlayVideo.title());
            //开启清晰度
            if (definitionTextView != null) {
                if (nowPlayVideo.definition() != null) {
                    definitionTextView.setVisibility(VISIBLE);
                    definitionTextView.setText(nowPlayVideo.definition());
                } else {
                    definitionTextView.setVisibility(GONE);
                }
            }

            if (checkReady) {
                seekTo(position);
                if (isPlaying) play();
                else prePlay();
            }
        }
    }

    public int getNowPlayIndex() {
        return qsVideos == null ? -1 : qsVideos.indexOf(nowPlayVideo);
    }

    protected void initHelpView(Context context) {
        myOnClickListener = new MyOnClickListener();
        mHandler = new Handler(Looper.getMainLooper());
        handleTouchEvent = new HandleTouchEvent(this);
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        controlContainer = (ViewGroup) View.inflate(context, getLayoutId(), null);
        videoView.addView(controlContainer, new LayoutParams(-1, -1));
        videoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouchEvent.handleEvent(v, event);
            }
        });

        titleTextView = (TextView) findViewById(R.id.help_title);
        startButton = (ImageView) findViewById(R.id.help_start);
        startButton2 = (ImageView) findViewById(R.id.help_start2);
        fullscreenButton = (ImageView) findViewById(R.id.help_fullscreen);
        seekBar = (SeekBar) findViewById(R.id.help_seekbar);
        progressBar = (ProgressBar) findViewById(R.id.help_progress);
        currentTimeTextView = (TextView) findViewById(R.id.help_current);
        totalTimeTextView = (TextView) findViewById(R.id.help_total);
        backView = findViewById(R.id.help_back);
        floatCloseView = findViewById(R.id.help_float_close);
        floatBackView = findViewById(R.id.help_float_goback);
        definitionTextView = findViewById(R.id.help_definition);
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(this);
            seekBar.setMax(progressMax);
        }
        if (progressBar != null)
            progressBar.setMax(progressMax);
        setClick(videoView, startButton, startButton2, fullscreenButton, backView, floatCloseView, floatBackView, definitionTextView);

    }

    //-----------ui监听start-----------------
    private void setClick(View... vs) {
        for (View v : vs) {
            if (v != null)
                v.setOnClickListener(myOnClickListener);
        }
    }

    private class MyOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            int i = view.getId();

            //播放按钮
            if (i == R.id.help_start || i == R.id.help_start2) {
                clickPlay();
            }
            //全屏按钮
            if (i == R.id.help_fullscreen) {
                clickFull();
            }
            //退出按钮
            if (i == R.id.help_back) {
                if (currentMode != MODE_WINDOW_NORMAL)
                    quitWindowFullscreen();
                else
                    Util.scanForActivity(getContext()).finish();
            }
            //退出悬浮窗按钮
            if (i == R.id.help_float_close) {
                if (isSystemFloatMode())
                    release();
                else
                    pause();
                quitWindowFloat();

            }
            //退出悬浮窗按钮
            if (i == R.id.help_float_goback) {
                if (isSystemFloatMode()) {
                    try {
                        Intent intent = new Intent(getContext(), Util.scanForActivity(getContext()).getClass());
                        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        getContext().getApplicationContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                quitWindowFloat();
            }
            //点击空白处
            if (view == videoView) {
                if (currentState == STATE_NORMAL || currentState == STATE_ERROR)
                    clickPlay();
                else if (currentState == STATE_PLAYING ||
                        currentState == STATE_PAUSE ||
                        currentState == STATE_AUTO_COMPLETE) {
                    isShowControlView = !isShowControlView;
                    setUIWithStateAndMode(currentState, currentMode);
                }
            }

            //清晰度按钮
            if (i == R.id.help_definition) {
                popDefinition(definitionTextView, qsVideos, getNowPlayIndex());
            }

            //点击事件
            handlePlayListener.onEvent(EVENT_CLICK_VIEW, i);
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
        if (getDuration() > 1) {
            int time = seekBar.getProgress() * (getDuration() / progressMax);
            if (currentTimeTextView != null)
                currentTimeTextView.setText(Util.stringForTime(time));
        }
        handlePlayListener.onEvent(EVENT_SEEKBAR_TOUCHING, progress, progressMax);
        //Log.i(TAG, "onProgressChanged " + Util.stringForTime(time));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
        cancelDismissControlViewTimer();
        handlePlayListener.onEvent(EVENT_SEEKBAR_START);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (getDuration() > 1) {
            int time = seekBar.getProgress() * (getDuration() / progressMax);
            seekTo(time);
        }
        startProgressTimer();
        if (currentState == STATE_PLAYING)
            startDismissControlViewTimer(1314);
        handlePlayListener.onEvent(EVENT_SEEKBAR_END);
    }
    //-----------ui监听end-----------------


    //-----------设置UI数据start-----------------
    @Override//覆盖父类监听播放器状态//todo 父类这个方法后面考虑改成final,子类从监听里获取状态,事件
    protected void setUIWithStateAndMode(final int status, final int mode) {
        cancelDismissControlViewTimer();
        cancelProgressTimer();
        switch (status) {
            case STATE_NORMAL:
            case STATE_PREPARING:
                resetProgressAndTime();
                onBuffering(false);
                isShowControlView = false;
                break;
            case STATE_PLAYING:
                startDismissControlViewTimer();
                startProgressTimer();
                if (currentState == STATE_PREPARING)//加载完成显示下控制UI
                    if (isDoneShowControlUI) isShowControlView = true;
                break;
            case STATE_PAUSE:
                startProgressTimer();
                if (currentState == STATE_PREPARING)//加载完成显示下控制UI
                    isShowControlView = true;
                break;
            case STATE_ERROR:
                onBuffering(false);
                isShowControlView = false;
                break;
            case STATE_AUTO_COMPLETE:
                if (isDoneShowControlUI) isShowControlView = true;
                setCompleProgressAndTime();
                onBuffering(false);
                break;
        }
        changeUiWithStateAndMode(status, mode);
        if ((status == STATE_PLAYING || status == STATE_PAUSE || status == STATE_AUTO_COMPLETE)
                & !isShowControlView)
            dismissControlView(status, mode);
        //调用父类...
        super.setUIWithStateAndMode(status, mode);
        //状态改变监听回调永远放在最后
        handlePlayListener.onEvent(EVENT_CONTROL_VIEW, isShowControlView ? 0 : 1);
    }

    //缓冲进度
    @Override
    protected void setBufferProgress(float bufferProgress) {
        if (seekBar != null)
            seekBar.setSecondaryProgress((int) (bufferProgress * progressMax));
        if (progressBar != null)
            progressBar.setSecondaryProgress((int) (bufferProgress * progressMax));
    }

    //设置进度和时间
    protected void setProgressAndText() {
        int position = getPosition();
        int duration = getDuration();
        if (position < 0)
            position = 0;
        if (duration <= 0)
            duration = 1;
        int progress = (int) (((long) position * progressMax) / duration);
        if (progress < 0 || progress > progressMax)//防止溢出
            progress = progressMax;
        setProgressBar(progress, seekBar, progressBar);
        if (currentTimeTextView != null)
            currentTimeTextView.setText(Util.stringForTime(position));
        if (totalTimeTextView != null)
            if (duration > 1)
                totalTimeTextView.setText(Util.stringForTime(duration));
            else
                totalTimeTextView.setText(R.string.online);
    }

    //初始化进度和时间
    protected void resetProgressAndTime() {
        setProgressBar(0, seekBar, progressBar);
        if (currentTimeTextView != null)
            currentTimeTextView.setText(Util.stringForTime(0));
        if (totalTimeTextView != null)
            totalTimeTextView.setText(Util.stringForTime(0));
    }

    //播放完成进度和时间
    protected void setCompleProgressAndTime() {
        setProgressBar(progressMax, seekBar, progressBar);
        if (currentTimeTextView != null)
            currentTimeTextView.setText(Util.stringForTime(getDuration()));
    }

    private void setProgressBar(int pro, ProgressBar... ps) {
        for (ProgressBar p : ps)
            if (p != null)
                p.setProgress(pro);

    }
    //-----------设置数据end-----------------


    //-----------定时任务更新进度start-----------------
    protected void startProgressTimer() {
        cancelProgressTimer();
        mHandler.post(updateProgress);
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


    //-----------定时任务隐藏控制栏start-----------------
    protected void startDismissControlViewTimer() {
        startDismissControlViewTimer(controlUIHideTime);
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
            isShowControlView = false;
            dismissControlView(currentState, currentMode);
            handlePlayListener.onEvent(EVENT_CONTROL_VIEW, isShowControlView ? 0 : 1);
        }
    };
    //-----------定时任务隐藏控制栏end-----------------


    /**
     * =========================================
     * -------子类需要实现的重要的方法------------
     * ========================================
     */
    protected abstract int getLayoutId();//id见ids.xml

    protected abstract void changeUiWithStateAndMode(int status, int mode);//根据状态设置ui显示/隐藏

    protected abstract void dismissControlView(int status, int mode);//播放时定时隐藏的控制ui

    protected abstract void onBuffering(boolean isBuffering);//缓冲


    //==============================================================================================
    //-------------------------------------以下为手势逻辑--------------------------------------------
    //==============================================================================================
    @Override
    public void onGestureBegin(int type) {
        if (!isWindowGesture & currentMode != MODE_WINDOW_FULLSCREEN)
            return;

        //进度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_FULL_X & checkReady())
            tempPosition = getPosition();
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
            int duration = getDuration();
            if (duration <= 1)
                return;
            //拖动开始慢 后面快
            int delta = (int) (level * Math.abs(level) * duration);
            if (delta < -tempPosition)
                delta = -tempPosition;
            if (delta > duration - tempPosition)
                delta = duration - tempPosition;
            if (showProgressDialog(delta, tempPosition, duration)) {
                //pause();
            }
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
            doubleClick();

        if (!isWindowGesture & currentMode != MODE_WINDOW_FULLSCREEN)
            return;
        //进度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_FULL_X & checkReady()) {
            int duration = getDuration();
            if (duration <= 0)
                return;
            if (!dismissProgressDialog()) return;

            int delta = (int) (level * Math.abs(level) * duration);
            tempPosition += delta;
            if (tempPosition > duration)
                tempPosition = duration;
            if (tempPosition < 0)
                tempPosition = 0;
            seekTo(tempPosition);
            tempPosition = 0;
            //play();
        }
        //亮度
        if (type == HandleTouchEvent.GestureEvent.TOUCH_LEFT_Y) {
            dismissBrightnessDialog();
        }
        //音量
        if (type == HandleTouchEvent.GestureEvent.TOUCH_RIGHT_Y) {
            dismissVolumeDialog();
        }

        if (currentMode == MODE_WINDOW_FULLSCREEN) {
            Util.showNavigationBar(getContext(), false);
        }
    }

    //-----------各种手势ui实现start-----------
    //子类写了实现 就返回true
    protected abstract boolean showWifiDialog();//要弹出非wifi提示框覆盖return true即可

    protected abstract void doubleClick();

    protected abstract void popDefinition(View view, List<QSVideo> qsVideos, int index);//弹出清晰度

    protected abstract boolean showProgressDialog(int delay, int position, int duration);

    protected abstract boolean dismissProgressDialog();

    protected abstract boolean showVolumeDialog(int nowVolume, int maxVolume);

    protected abstract boolean dismissVolumeDialog();

    protected abstract boolean showBrightnessDialog(int nowBrightness, int maxBrightness);

    protected abstract boolean dismissBrightnessDialog();
    //-----------各种调节弹窗end-----------

}
