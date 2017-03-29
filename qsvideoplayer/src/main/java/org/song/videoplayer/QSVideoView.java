package org.song.videoplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.song.videoplayer.media.IMediaControl;

/**
 * Created by song on 2017/2/13.
 * ui设计基于jcplayer
 */
public class QSVideoView extends BaseVideoView {

    public boolean isShowWifiDialog = true;//是否显示非wifi提示

    protected ImageView backButton;
    protected ProgressBar bottomProgressBar;
    protected TextView titleTextView;
    protected ImageView coverImageView;
    protected ImageView tinyBackImageView;

    public QSVideoView(Context context) {
        super(context);
    }

    public QSVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.video_view;
    }

    public ImageView getCoverImageView() {
        return coverImageView;
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        bottomProgressBar = (ProgressBar) findViewById(R.id.bottom_progressbar);
        titleTextView = (TextView) findViewById(R.id.title);
        backButton = (ImageView) findViewById(R.id.back);
        coverImageView = (ImageView) findViewById(R.id.cover);
        tinyBackImageView = (ImageView) findViewById(R.id.back_tiny);

        bottomProgressBar.setMax(progressMax);
        backButton.setOnClickListener(this);
        tinyBackImageView.setOnClickListener(this);

        setUIWithStateAndMode(STATE_NORMAL, currentMode);
    }

    @Override
    public void setUp(String url, Object... objects) {
        super.setUp(url, objects);
        if (objects != null)
            titleTextView.setText(String.valueOf(objects[0]));
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.back)
            onBackPressed();
    }

    //---底部进度条 覆盖父类设置数据
    @Override
    protected void setProgressAndText() {
        super.setProgressAndText();
        bottomProgressBar.setProgress(progressBar.getProgress());
    }

    @Override
    protected void setBufferProgress(int bufferProgress) {
        super.setBufferProgress(bufferProgress);
        bottomProgressBar.setSecondaryProgress(progressBar.getSecondaryProgress());
    }

    @Override
    protected void resetProgressAndTime() {
        super.resetProgressAndTime();
        bottomProgressBar.setProgress(0);
        bottomProgressBar.setSecondaryProgress(0);
    }

    @Override
    protected void setCompleProgressAndTime() {
        super.setCompleProgressAndTime();
        bottomProgressBar.setProgress(progressMax);
    }
    //------底部进度条end


    @Override//缓冲
    protected void onBuffering(boolean isBuffering) {
        bufferingContainer.setVisibility(isBuffering ? VISIBLE : INVISIBLE);
    }

    //隐藏控制ui
    @Override
    protected void dismissControlView() {
        bottomContainer.setVisibility(View.INVISIBLE);
        topContainer.setVisibility(View.INVISIBLE);
        bottomProgressBar.setVisibility(View.VISIBLE);
        if (currentState != STATE_AUTO_COMPLETE)
            startButton.setVisibility(View.INVISIBLE);
    }


    //--------------根据各种状态设置ui是否显示(控制view也显示)------------------
    @Override
    protected void changeUiWithStateAndMode(final int status, final int mode) {
        //立即隐藏控制ui标记 初始化好播放立即隐藏控制ui / 切换全屏如果原来是隐藏的也立即隐藏
        boolean flag = (status == STATE_PLAYING & currentState == STATE_PREPARING) |
                (mode != currentMode & bottomContainer.getVisibility() != VISIBLE);
        switch (status) {
            case STATE_NORMAL:
                changeUiToNormal(mode);
                break;
            case STATE_PREPARING:
                changeUiToPreparingShow(mode);
                break;
            case STATE_PLAYING:
                changeUiToPlayingShow(mode);
                startDismissControlViewTimer();
                break;
            case STATE_PAUSE:
                changeUiToPauseShow(mode);
                break;
            case STATE_ERROR:
                changeUiToError(mode);
                break;
            case STATE_AUTO_COMPLETE:
                changeUiToCompleteShow(mode);
                break;
        }
        updateViewImage(status, mode);
        if (flag) dismissControlView();
    }

    protected void changeUiToNormal(int currentMode) {
        bufferingContainer.setVisibility(INVISIBLE);
        switch (currentMode) {
            case MODE_WINDOW_NORMAL:
                setAllControlsVisible(4, 4, 0, 0, 4, 4);
                break;
            case MODE_WINDOW_FULLSCREEN:
                setAllControlsVisible(0, 4, 0, 0, 4, 4);
                break;
            case MODE_WINDOW_TINY:
                break;
        }
    }

    protected void changeUiToPreparingShow(int currentMode) {
        bufferingContainer.setVisibility(INVISIBLE);
        switch (currentMode) {
            case MODE_WINDOW_NORMAL:
                setAllControlsVisible(4, 4, 4, 4, 4, 0);

                break;
            case MODE_WINDOW_FULLSCREEN:
                setAllControlsVisible(4, 4, 4, 4, 4, 0);
                break;
            case MODE_WINDOW_TINY:
                break;
        }
    }

    protected void changeUiToPlayingShow(int currentMode) {
        switch (currentMode) {
            case MODE_WINDOW_NORMAL:
                setAllControlsVisible(4, 0, 0, 4, 4, 4);
                break;
            case MODE_WINDOW_FULLSCREEN:
                setAllControlsVisible(0, 0, 0, 4, 4, 4);
                break;
            case MODE_WINDOW_TINY:
                break;
        }

    }

    protected void changeUiToPauseShow(int currentMode) {
        switch (currentMode) {
            case MODE_WINDOW_NORMAL:
                setAllControlsVisible(4, 0, 0, 4, 4, 4);
                break;
            case MODE_WINDOW_FULLSCREEN:
                setAllControlsVisible(0, 0, 0, 4, 4, 4);
                break;
            case MODE_WINDOW_TINY:
                break;
        }

    }


    protected void changeUiToCompleteShow(int currentMode) {
        bufferingContainer.setVisibility(INVISIBLE);
        switch (currentMode) {
            case MODE_WINDOW_NORMAL:
                setAllControlsVisible(4, 0, 0, 4, 4, 4);
                break;
            case MODE_WINDOW_FULLSCREEN:
                setAllControlsVisible(0, 0, 0, 4, 4, 4);
                break;
            case MODE_WINDOW_TINY:
                break;
        }

    }

    protected void changeUiToError(int currentMode) {
        bufferingContainer.setVisibility(INVISIBLE);
        switch (currentMode) {
            case MODE_WINDOW_NORMAL:
                setAllControlsVisible(4, 4, 0, 4, 4, 4);
                break;
            case MODE_WINDOW_FULLSCREEN:
                setAllControlsVisible(4, 4, 0, 4, 4, 4);
                break;
            case MODE_WINDOW_TINY:
                break;
        }

    }

    /**
     * 0 VISIBLE  4 INVISIBLE 8 GONE
     * 参数分别为
     * 0顶部 1底部 2按钮 3封面 4小进度条 5初始化界面
     */
    protected void setAllControlsVisible(Integer... arr) {
        topContainer.setVisibility(arr[0]);
        bottomContainer.setVisibility(arr[1]);
        startButton.setVisibility(arr[2]);
        coverImageView.setVisibility(arr[3]);
        bottomProgressBar.setVisibility(arr[4]);
        loadingContainer.setVisibility(arr[5]);
        //errorContainer.setVisibility(arr[6]);
    }
    //--------------根据各种状态设置ui是否显示 end------------------


    protected void updateViewImage(int status, int mode) {
        if (status == STATE_ERROR) {
            startButton.setImageResource(R.drawable.jc_click_error_selector);
        } else if (status == STATE_PLAYING) {
            startButton.setImageResource(R.drawable.jc_click_pause_selector);
        } else {
            startButton.setImageResource(R.drawable.jc_click_play_selector);
        }

        if (mode == MODE_WINDOW_NORMAL)
            fullscreenButton.setImageResource(R.drawable.jc_enlarge);
        else
            fullscreenButton.setImageResource(R.drawable.jc_shrink);

    }


    //-----------------弹窗---------------
    @Override
    protected boolean showWifiDialog() {
        if (!isShowWifiDialog)
            return false;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                prepareMediaPlayer();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
        return true;
    }

    protected PopupWindow mProgressDialog;
    protected ProgressBar mDialogProgressBar;
    protected TextView tv_current;
    protected TextView tv_duration;
    protected TextView tv_delta;
    protected ImageView mDialogIcon;

    @Override
    protected boolean showProgressDialog(int delta, int position, int duration) {
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_dialog_progress, null);
            mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
            tv_current = ((TextView) localView.findViewById(R.id.tv_current));
            tv_duration = ((TextView) localView.findViewById(R.id.tv_duration));
            tv_delta = ((TextView) localView.findViewById(R.id.tv_delta));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mProgressDialog = getPopupWindow(localView);

        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.showAtLocation(this, Gravity.CENTER, 0, 0);
        }

        tv_delta.setText(
                (delta > 0 ? "+" : "") +
                        delta / 1000 + "秒");
        tv_current.setText(Util.stringForTime(position + delta) + "/");
        tv_duration.setText(Util.stringForTime(duration));
        mDialogProgressBar.setProgress((position + delta) * 100 / duration);
        if (delta > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.jc_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.jc_backward_icon);
        }
        return true;
    }

    @Override
    protected boolean dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        return true;
    }


    protected PopupWindow mVolumeDialog;
    protected ProgressBar mDialogVolumeProgressBar;
    protected TextView mDialogVolumeTextView;
    protected ImageView mDialogVolumeImageView;


    @Override
    protected boolean showVolumeDialog(int nowVolume, int maxVolume) {

        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_dialog_volume, null);
            mDialogVolumeImageView = ((ImageView) localView.findViewById(R.id.volume_image_tip));
            mDialogVolumeTextView = ((TextView) localView.findViewById(R.id.tv_volume));
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            mDialogVolumeProgressBar.setMax(maxVolume);
            mVolumeDialog = getPopupWindow(localView);
        }
        if (!mVolumeDialog.isShowing())
            mVolumeDialog.showAtLocation(this, Gravity.TOP, 0, Util.dp2px(getContext(), 50));

        mDialogVolumeTextView.setText(nowVolume + "");
        mDialogVolumeProgressBar.setProgress(nowVolume);
        return true;
    }

    @Override
    protected boolean dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
        return true;
    }

    protected PopupWindow mBrightnessDialog;
    protected ProgressBar mDialogBrightnessProgressBar;
    protected TextView mDialogBrightnessTextView;

    @Override
    protected boolean showBrightnessDialog(int brightnessPercent, int max) {
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_dialog_brightness, null);
            mDialogBrightnessTextView = ((TextView) localView.findViewById(R.id.tv_brightness));
            mDialogBrightnessProgressBar = ((ProgressBar) localView.findViewById(R.id.brightness_progressbar));
            mDialogBrightnessProgressBar.setMax(max);
            //mBrightnessDialog = getDialog(Gravity.TOP, 0, Util.dp2px(getContext(), 50));
            //mBrightnessDialog.setContentView(localView);

            mBrightnessDialog = getPopupWindow(localView);
        }
        if (!mBrightnessDialog.isShowing())
            mBrightnessDialog.showAtLocation(this, Gravity.TOP, 0, Util.dp2px(getContext(), 50));

        mDialogBrightnessTextView.setText(brightnessPercent + "");
        mDialogBrightnessProgressBar.setProgress(brightnessPercent);
        return true;
    }

    @Override
    protected boolean dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
        }
        return true;
    }

//
//    private Dialog getDialog(int graviaty, int marginX, int marginY) {
//        Dialog dialog = new Dialog(getContext(), R.style.jc_style_dialog_progress);
//        dialog.getWindow().addFlags(Window.FEATURE_ACTION_BAR);
//        dialog.getWindow().addFlags(32);
//        dialog.getWindow().addFlags(16);
//        dialog.getWindow().setLayout(-2, -2);
//        WindowManager.LayoutParams localLayoutParams = dialog.getWindow().getAttributes();
//        localLayoutParams.gravity = graviaty;
//        if (marginX > 0)
//            localLayoutParams.x = marginX;
//        if (marginY > 0)
//            localLayoutParams.y = marginY;
//        dialog.getWindow().setAttributes(localLayoutParams);
//
//        return dialog;
//    }

    private PopupWindow getPopupWindow(View popupView) {
        PopupWindow mPopupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
        mPopupWindow.setAnimationStyle(R.style.jc_popup_toast_anim);
        return mPopupWindow;
    }

}
