package org.song.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import org.song.videoplayer.IVideoPlayer;
import org.song.videoplayer.PlayListener;
import org.song.videoplayer.QSVideoView;

public class MainActivity extends AppCompatActivity {

    QSVideoView qsVideoView;

    String mp4 = "http://sz-kpie-videos.oss-cn-shenzhen.aliyuncs.com/videos/20161110/11.mp4";

    String m3u8 = "http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8";

    String url;
    int media;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        qsVideoView = (QSVideoView) findViewById(R.id.qs);
        qsVideoView.getCoverImageView().setImageResource(R.mipmap.cover);
        qsVideoView.setPlayListener(new PlayListener() {
            @Override
            public void onStatus(int status) {//播放状态
                if (status == IVideoPlayer.STATE_AUTO_COMPLETE)
                    qsVideoView.quitWindowFullscreen();//播放完成退出全屏
            }

            @Override//全屏/普通
            public void onMode(int mode) {

            }

            @Override
            public void onEvent(int what, Integer... extra) {

            }

        });
        play(mp4, 0);
    }


    private void play(String url, int media) {
        qsVideoView.release();
        qsVideoView.setiMediaControl(media);
        qsVideoView.setUp(url, "这是一一一一一一一一一个标题");
        //qsVideoView.seekTo(12300);
        qsVideoView.play();
        this.url = url;
        this.media = media;

        //qsVideoView.enterFullMode =1;
    }


    public void 系统硬解(View v) {
        play(url, 0);
        setTitle("系统硬解");

    }

    public void ijk_ffmepg解码(View v) {
        play(url, 1);
        setTitle("ijk_ffmepg解码");

    }

    public void exo解码(View v) {
        play(url, 2);
        setTitle("exo解码");

    }

    public void ijk_exo解码(View v) {
        play(url, 3);
        setTitle("ijk_exo解码");

    }

    public void 网络视频(View v) {
        play(mp4, media);

    }

    public void m3u8直播(View v) {
        play(m3u8, media);
    }

    public void 销毁(View v) {
        qsVideoView.release();
    }


    @Override
    public void onBackPressed() {
        if (qsVideoView.onBackPressed())
            return;
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (flag)
            qsVideoView.play();
        handler.removeCallbacks(runnable);
        if (position > 0) {
            qsVideoView.seekTo(position);
            position = 0;
        }

    }

    boolean flag;//记录退出时播放状态 回来的时候继续播放
    int position;//记录销毁时的进度 回来继续盖进度播放

    @Override
    public void onPause() {
        super.onPause();//暂停
        flag = qsVideoView.isPlaying();
        qsVideoView.pause();
    }


    @Override
    public void onStop() {
        super.onStop();//不马上销毁 延时10秒
        handler.postDelayed(runnable, 1000 * 10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();//销毁
        qsVideoView.release();
    }


    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (qsVideoView.getCurrentState() != IVideoPlayer.STATE_AUTO_COMPLETE)
                position = qsVideoView.getPosition();
            qsVideoView.release();
        }
    };

}
