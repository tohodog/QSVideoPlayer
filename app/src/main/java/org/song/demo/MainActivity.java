package org.song.demo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.song.videoplayer.IVideoPlayer;
import org.song.videoplayer.PlayListener;
import org.song.videoplayer.DemoQSVideoView;
import org.song.videoplayer.floatwindow.FloatParams;
import org.song.videoplayer.media.AndroidMedia;
import org.song.videoplayer.media.BaseMedia;
import org.song.videoplayer.media.ExoMedia;
import org.song.videoplayer.media.IjkExoMedia;
import org.song.videoplayer.media.IjkMedia;

public class MainActivity extends AppCompatActivity {

    DemoQSVideoView demoVideoView;

    String mp4 = "http://videos.kpie.com.cn/videos/20170526/037DCE54-EECE-4520-AA92-E4002B1F29B0.mp4";
    String m3u8 = "http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8";

    String url;
    Class decodeMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_url).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                changeUrl();
                return true;
            }
        });
        demoVideoView = (DemoQSVideoView) findViewById(R.id.qs);
        demoVideoView.getCoverImageView().setImageResource(R.mipmap.cover);
        //进入全屏的模式 0横屏 1竖屏 2传感器自动横竖屏 3根据视频比例自动确定横竖屏      -1什么都不做
        demoVideoView.enterFullMode = 3;
        demoVideoView.setPlayListener(new PlayListener() {
            @Override
            public void onStatus(int status) {//播放状态
                if (status == IVideoPlayer.STATE_AUTO_COMPLETE)
                    demoVideoView.quitWindowFullscreen();//播放完成退出全屏
            }

            @Override//全屏/普通
            public void onMode(int mode) {

            }

            @Override
            public void onEvent(int what, Integer... extra) {

            }

        });
        play(mp4, AndroidMedia.class);

    }


    private void play(String url, Class<? extends BaseMedia> decodeMedia) {
        Log.e("=====url:", url);
        demoVideoView.release();
        demoVideoView.setDecodeMedia(decodeMedia);
        demoVideoView.setUp(url, "这是一一一一一一一一一个标题");
        //qsVideoView.seekTo(12300);
        demoVideoView.setMute(mute);
        demoVideoView.play();
        this.url = url;
        this.decodeMedia = decodeMedia;
    }


    public void 系统硬解(View v) {
        play(url, AndroidMedia.class);
        setTitle("系统硬解");

    }

    public void ijk_ffmepg解码(View v) {
        play(url, IjkMedia.class);
        setTitle("ijk_ffmepg解码");

    }

    public void exo解码(View v) {
        play(url, ExoMedia.class);
        setTitle("exo解码");

    }

    public void ijk_exo解码(View v) {
        play(url, IjkExoMedia.class);
        setTitle("ijk_exo解码");

    }

    public void 网络视频(View v) {
        play(mp4, decodeMedia);

    }

    public void 视频列表(View v) {
        startActivity(new Intent(this, ListVideoActivity.class));
    }


    public void m3u8直播(View v) {
        play(m3u8, decodeMedia);
    }

    String[] arr = {"适应", "填充", "原尺寸", "拉伸", "16:9", "4:3"};
    int mode;
    boolean mute;

    public void 缩放模式(View v) {
        demoVideoView.setAspectRatio(++mode > 5 ? mode = 0 : mode);
        ((Button) v).setText(arr[mode]);
    }

    public void 静音(View v) {
        demoVideoView.setMute(mute = !mute);
        ((Button) v).setText(mute ? "静音 ON" : "静音 OFF");
    }

    public void 系统浮窗(View v) {
        if (demoVideoView.getCurrentMode() == IVideoPlayer.MODE_WINDOW_FLOAT_ACT)
            return;
        enterFloat(true);
        ((Button) v).setText(demoVideoView.isWindowFloatMode() ? "退出浮窗" : "系统浮窗");
    }

    public void 界面内浮窗(View v) {
        if (demoVideoView.getCurrentMode() == IVideoPlayer.MODE_WINDOW_FLOAT_SYS)
            return;
        enterFloat(false);
        ((Button) v).setText(demoVideoView.isWindowFloatMode() ? "退出浮窗" : "界面内浮窗");
    }

    private void enterFloat(boolean isSystemFloat) {
        FloatParams floatParams = new FloatParams();
        floatParams.x = 0;
        floatParams.y = 0;
        floatParams.w = 540;
        floatParams.h = 270;
        floatParams.round = 30;
        floatParams.fade = 0.8f;
        floatParams.canMove = true;
        floatParams.systemFloat = isSystemFloat;

        if (demoVideoView.isWindowFloatMode())
            demoVideoView.quitWindowFloat();
        else {
            if (!demoVideoView.enterWindowFloat(floatParams)) {
                Toast.makeText(this, "没有浮窗权限", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }
        }
    }


    public void 销毁(View v) {
        demoVideoView.release();
    }


    //返回键
    @Override
    public void onBackPressed() {
        //全屏和系统浮窗不finish
        if (demoVideoView.onBackPressed()) {
            if (demoVideoView.isSystemFloatMode())
                //系统浮窗返回上一界面
                moveTaskToBack(true);
            return;
        }
        super.onBackPressed();
    }

    //=======================以下生命周期控制=======================

    @Override
    public void onResume() {
        super.onResume();
        if (flag)
            demoVideoView.play();
        handler.removeCallbacks(runnable);
        if (position > 0) {
            demoVideoView.seekTo(position);
            position = 0;
        }
    }

    boolean flag;//记录退出时播放状态 回来的时候继续播放
    int position;//记录销毁时的进度 回来继续盖进度播放

    @Override
    public void onPause() {
        super.onPause();
        if (demoVideoView.isSystemFloatMode())
            return;
        //暂停
        flag = demoVideoView.isPlaying();
        demoVideoView.pause();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (demoVideoView.isSystemFloatMode())
            return;
        //不马上销毁 延时10秒
        handler.postDelayed(runnable, 1000 * 10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();//销毁
        if (demoVideoView.isSystemFloatMode())
            demoVideoView.quitWindowFloat();
        demoVideoView.release();
    }


    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (demoVideoView.getCurrentState() != IVideoPlayer.STATE_AUTO_COMPLETE)
                position = demoVideoView.getPosition();
            demoVideoView.release();
        }
    };


    public void changeUrl() {
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this).setView(editText).setTitle("网络视频地址").setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mp4 = editText.getText().toString();
                play(mp4, decodeMedia);
            }
        }).setPositiveButton("本地视频", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 1);
            }
        }).create().show();
    }

    public void setTitle(CharSequence title) {
        ((TextView) findViewById(R.id.tv_title)).setText(title);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 & resultCode == RESULT_OK) {
            mp4 = data.getData().toString();
            Toast.makeText(this, mp4, Toast.LENGTH_LONG).show();
            play(mp4, decodeMedia);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("text", mp4));
        }
    }
}
