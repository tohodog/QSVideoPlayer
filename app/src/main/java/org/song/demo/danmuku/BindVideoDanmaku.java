package org.song.demo.danmuku;

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
 * 描述
 */

public class BindVideoDanmaku implements PlayListener {

    private DanmakuView danmakuView;
    private QSDanmakuParser parser;
    private QSVideoView qsVideoView;
    private DanmakuContext danmakuContext;

    public DanmakuView bind(QSVideoView qsVideoView, final QSDanmakuParser parser, DanmakuContext mContext) {
        this.parser = parser;
        this.qsVideoView = qsVideoView;
        danmakuContext = mContext;

        qsVideoView.addPlayListener(this);
        danmakuView = new DanmakuView(qsVideoView.getContext());
        ViewGroup videoview = qsVideoView.findViewById(R.id.qs_videoview);
        videoview.addView(danmakuView, 1, new ViewGroup.LayoutParams(-1, -1));

        danmakuView.showFPS(true);
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
                if (BindVideoDanmaku.this.qsVideoView.isPlaying())
                    danmakuView.start(BindVideoDanmaku.this.qsVideoView.getPosition());
            }
        });

        return danmakuView;
    }

    public void hide() {
        danmakuView.hide();
    }

    public void show() {
        danmakuView.show();
    }

    @Override
    public void onStatus(int status) {
    }

    @Override
    public void onMode(int mode) {

    }


    @Override
    public void onEvent(int what, Integer... extra) {
        switch (what) {
            case IVideoPlayer.EVENT_PREPARE_START:
                danmakuView.prepare(parser, danmakuContext);
                break;
            case IVideoPlayer.EVENT_PREPARE_END:
                //if (danmakuView.isPrepared())
                    danmakuView.start(0);
            case IVideoPlayer.EVENT_PLAY:
                danmakuView.resume();
                break;
            case IVideoPlayer.EVENT_PAUSE:
                danmakuView.pause();
                break;
            case IVideoPlayer.EVENT_ERROR:
                danmakuView.clear();
                break;
            case IVideoPlayer.EVENT_COMPLETION:
                danmakuView.pause();
                break;
            case IVideoPlayer.EVENT_RELEASE:
                break;
            case IVideoPlayer.EVENT_SEEK_TO:
            case IVideoPlayer.EVENT_BUFFERING_START:
                danmakuView.pause();
                break;
            case IVideoPlayer.EVENT_BUFFERING_END:
            case IVideoPlayer.EVENT_SEEK_COMPLETION:
                danmakuView.resume();
                danmakuView.seekTo(Long.valueOf(extra[0]));
                break;
        }
    }
}
