package org.song.demo.danmaku;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import org.song.demo.R;
import org.song.videoplayer.IVideoPlayer;
import org.song.videoplayer.PlayListener;
import org.song.videoplayer.QSVideoView;


import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.ui.widget.DanmakuView;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2018/5/8
 * 弹幕关联播放器以及控制
 */

public class DanmakuControl implements PlayListener {

    public static DanmakuControl bind(QSVideoView qsVideoView, final QSDanmakuParser parser, DanmakuContext danmakuContext) {
        return new DanmakuControl(qsVideoView, parser, danmakuContext);
    }


    private DanmakuView danmakuView;
    private QSDanmakuParser parser;
    private QSVideoView qsVideoView;
    private DanmakuContext danmakuContext;
    private Context context;
    private boolean isShow = true;


    //需要播放前调用!!
    private DanmakuControl(QSVideoView qsVideoView, final QSDanmakuParser parser, DanmakuContext danmakuContext) {
        this.parser = parser;
        this.qsVideoView = qsVideoView;
        this.context = qsVideoView.getContext();
        this.danmakuContext = danmakuContext;
        parser.setTextSize(context.getResources().getDisplayMetrics().density);

        danmakuView = new DanmakuView(context);
        ViewGroup videoview = qsVideoView.findViewById(R.id.qs_videoview);
        videoview.addView(danmakuView, 1, new ViewGroup.LayoutParams(-1, -1));

        //danmakuView.showFPS(true);
        danmakuView.enableDanmakuDrawingCache(true);

        danmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
            @Override
            public void updateTimer(DanmakuTimer timer) {
            }

            @Override
            public void drawingFinished() {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
            }

            @Override
            public void prepared() {
                Log.e("danmakuView", "prepared");
                if (DanmakuControl.this.qsVideoView.isPlaying()) {
                    danmakuView.start(DanmakuControl.this.qsVideoView.getPosition());
                    danmakuView.resume();
                    Log.e("danmakuView", "prepared " + DanmakuControl.this.qsVideoView.getPosition());
                }
            }
        });

        qsVideoView.addPlayListener(this);

    }

    public boolean isShow() {
        return isShow;
    }

    public void hide() {
        danmakuView.hide();
        isShow = false;
    }

    public void show() {
        danmakuView.show();
        isShow = true;
    }

    public void add(BaseDanmaku item) {
        danmakuView.addDanmaku(item);
    }

    @Override
    public void onStatus(int status) {
    }


    @Override
    public void onMode(int mode) {
        if (qsVideoView.isWindowFloatMode()) {
            danmakuView.hide();
        } else if (isShow)
            danmakuView.show();
    }


    @Override
    public void onEvent(int what, Integer... extra) {
        if (what != IVideoPlayer.EVENT_PREPARE_START && !danmakuView.isPrepared())
            return;
        switch (what) {
            case IVideoPlayer.EVENT_PREPARE_START:
                danmakuView.prepare(parser, danmakuContext);
                danmakuView.enableDanmakuDrawingCache(true);
                break;
            case IVideoPlayer.EVENT_PREPARE_END:
                if (danmakuView.isPrepared())
                    danmakuView.start(0);
                break;
            case IVideoPlayer.EVENT_PLAY:
                handler.removeCallbacks(run);
                danmakuView.resume();
                Log.e("danmakuView", "EVENT_PLAY");
                break;
            case IVideoPlayer.EVENT_PAUSE:
                danmakuView.pause();
                Log.e("danmakuView", "EVENT_PAUSE");
                break;
            case IVideoPlayer.EVENT_ERROR:
                danmakuView.clear();
                break;
            case IVideoPlayer.EVENT_COMPLETION:
                danmakuView.pause();
                break;
            case IVideoPlayer.EVENT_RELEASE:
                danmakuView.release();
                Log.e("danmakuView", "EVENT_RELEASE");
                break;

            case IVideoPlayer.EVENT_BUFFERING_START:
                danmakuView.pause();
                Log.e("danmakuView", "EVENT_BUFFERING_START");
                break;
            case IVideoPlayer.EVENT_SEEK_TO:
                break;
            case IVideoPlayer.EVENT_SEEK_COMPLETION:
            case IVideoPlayer.EVENT_BUFFERING_END:
                danmakuView.seekTo(Long.valueOf(extra[0]));//seek后会播放
                if (!qsVideoView.isPlaying())//danmakuView.pause();seek后马上调用pause 没有用
                    rundelayed(run, 168);
                Log.e("danmakuView", "EVENT_SEEK_COMPLETION");
                break;
        }
    }

    private Handler handler = new Handler();
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            danmakuView.pause();
        }
    };

    private void rundelayed(Runnable run, int delayed) {
        this.run = run;
        handler.postDelayed(run, delayed);
    }
}
