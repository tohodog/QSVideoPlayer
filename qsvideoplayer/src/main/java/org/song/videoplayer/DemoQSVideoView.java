package org.song.videoplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by song on 2017/4/9.
 * Contact github.com/tohodog
 * diy自己的播放器,不需要写播放逻辑,只需设置各个状态下view的显示状态即可
 */
public class DemoQSVideoView extends QSVideoViewHelp {

    protected ImageView coverImageView;//封面
    protected ViewGroup bottomContainer;//底部栏
    protected ViewGroup topContainer;//顶部栏
    protected ViewGroup loadingContainer;//初始化
    protected ViewGroup errorContainer;//出错了显示的 重试
    protected ViewGroup bufferingContainer;//缓冲

    protected List<View> changeViews;//根据状态隐藏显示的view集合

    public DemoQSVideoView(Context context) {
        this(context, null);
    }

    public DemoQSVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public DemoQSVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        setUIWithStateAndMode(STATE_NORMAL, currentMode);
    }

    //提交xml 需要父类替你完成某个控件的逻辑 控件的id按照values/ids.xml去定义 如播放按钮id定义为(android:id="@id/help_start") @id 不是 @+id
    @Override
    protected int getLayoutId() {
        return R.layout.video_view;
    }

    protected void initView() {
        topContainer = (ViewGroup) findViewById(R.id.layout_top);
        bottomContainer = (ViewGroup) findViewById(R.id.layout_bottom);

        bufferingContainer = (ViewGroup) findViewById(R.id.buffering_container);
        loadingContainer = (ViewGroup) findViewById(R.id.loading_container);
        errorContainer = (ViewGroup) findViewById(R.id.error_container);

        coverImageView = (ImageView) findViewById(R.id.cover);

        changeViews = new ArrayList<>();

        //会根据播放器状态而改变的view加进去
        changeViews.add(topContainer);
        changeViews.add(bottomContainer);
        changeViews.add(loadingContainer);
        changeViews.add(errorContainer);
        changeViews.add(coverImageView);
        changeViews.add(startButton);
        changeViews.add(progressBar);
    }

    //根据播放状态设置ui显示/隐藏,包括控制UI
    @Override
    protected void changeUiWithStateAndMode(int status, int mode) {
        switch (status) {
            case STATE_NORMAL:
                showChangeViews(coverImageView, startButton);//普通状态显示封面和播放按钮
                break;
            case STATE_PREPARING:
                showChangeViews(loadingContainer);//初始化状态显示loading
                break;
            case STATE_PLAYING:
            case STATE_PAUSE:
            case STATE_AUTO_COMPLETE://播放中显示 播放按钮  [底部] [顶部]
                showChangeViews(startButton,
                        mode >= MODE_WINDOW_FLOAT_SYS ? null : bottomContainer,//浮窗不显示底部
                        mode == MODE_WINDOW_FULLSCREEN ? topContainer : null);//全屏显示顶部
                break;
            case STATE_ERROR://出错显示errorContainer
                showChangeViews(errorContainer);
                break;
        }

        //播放,全屏按钮
        startButton.setImageResource(status == STATE_PLAYING ?
                R.drawable.jc_click_pause_selector : R.drawable.jc_click_play_selector);
        fullscreenButton.setImageResource(mode == MODE_WINDOW_FULLSCREEN ?
                R.drawable.jc_shrink : R.drawable.jc_enlarge);

        //浮窗显示两个按钮
        floatCloseView.setVisibility(mode >= MODE_WINDOW_FLOAT_SYS ? View.VISIBLE : View.INVISIBLE);
        floatBackView.setVisibility(mode >= MODE_WINDOW_FLOAT_SYS ? View.VISIBLE : View.INVISIBLE);
    }

    //自动隐藏控制UI
    @Override
    protected void dismissControlView(int status, int mode) {
        bottomContainer.setVisibility(View.INVISIBLE);
        topContainer.setVisibility(View.INVISIBLE);
        //显示mini进度条
        progressBar.setVisibility(View.VISIBLE);
        //播放完成不隐藏播放按钮
        if (status != STATE_AUTO_COMPLETE)
            startButton.setVisibility(View.INVISIBLE);
        //浮窗隐藏角落按钮
        if (mode >= MODE_WINDOW_FLOAT_SYS)
            floatCloseView.setVisibility(View.INVISIBLE);
        if (mode >= MODE_WINDOW_FLOAT_SYS)
            floatBackView.setVisibility(View.INVISIBLE);
    }

    //缓冲
    @Override
    protected void onBuffering(boolean isBuffering) {
        bufferingContainer.setVisibility(isBuffering ? VISIBLE : INVISIBLE);
    }

    //根据播放器状态要显示的view
    protected void showChangeViews(View... views) {
        for (View v : changeViews)
            if (v != null)
                v.setVisibility(INVISIBLE);
        for (View v : views)
            if (v != null)
                v.setVisibility(VISIBLE);
    }

    public ImageView getCoverImageView() {
        return coverImageView;
    }

    //至此diy播放器逻辑就完成了 非常简单百来行代码 只是设置view的显示和隐藏,真的不用写逻辑

    //==============================================================================================
    //--------------------- 以下为辅助功能,不需要不用写实现--------------------------------------------
    //==============================================================================================

    public boolean isShowWifiDialog = true;

    //移动网络提示框
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

    //双击执行什么
    @Override
    protected void doubleClick() {
//        clickFull();
        clickPlay();
    }

    //弹出清晰度选择
    @Override
    protected void popDefinition(View view, List<QSVideo> qsVideos, int index) {
        VideoPopWindow videoPopWindow = new VideoPopWindow(getContext(), qsVideos, index);
        videoPopWindow.setOnItemListener(new VideoPopWindow.OnItemListener() {
            @Override
            public void OnClick(int position) {
                switchVideo(position);
            }
        });
        videoPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                startDismissControlViewTimer(1314);
            }
        });
        videoPopWindow.showTop(view);
        cancelDismissControlViewTimer();
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//            PopupMenu mPopupMenu = new PopupMenu(getContext(), view);
//            for (int i = 0; i < qsVideos.size(); i++) {
//                QSVideo q = qsVideos.get(i);
//                mPopupMenu.getMenu().add(i, i, i, q.resolution());
//            }
//            mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    switchVideo(item.getItemId());
//                    return true;
//                }
//            });
//            mPopupMenu.show();
//        }

    }

    protected PopupWindow mProgressDialog;
    protected ProgressBar mDialogProgressBar;
    protected TextView tv_current;
    protected TextView tv_duration;
    protected TextView tv_delta;
    protected ImageView mDialogIcon;

    //调节进度框
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

        tv_delta.setText(String.format("%s%ss",
                (delta > 0 ? "+" : ""),
                delta / 1000));
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

    //调节音量框
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
            mVolumeDialog.showAtLocation(this, Gravity.TOP, 0, Util.dp2px(getContext(), currentMode == MODE_WINDOW_NORMAL ? 25 : 50));

        mDialogVolumeTextView.setText(String.valueOf(nowVolume));
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

    //调节亮度框
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
            mBrightnessDialog.showAtLocation(this, Gravity.TOP, 0, Util.dp2px(getContext(), currentMode == MODE_WINDOW_NORMAL ? 25 : 50));

        mDialogBrightnessTextView.setText(String.valueOf(brightnessPercent));
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

    private PopupWindow getPopupWindow(View popupView) {
        PopupWindow mPopupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
        mPopupWindow.setAnimationStyle(R.style.jc_popup_toast_anim);
        return mPopupWindow;
    }

}
