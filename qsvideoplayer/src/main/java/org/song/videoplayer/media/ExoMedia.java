package org.song.videoplayer.media;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.util.Map;

//import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

/**
 * Created by song on 2017/2/22.
 * exo-2.0.4
 */

public class ExoMedia extends BaseMedia implements ExoPlayer.EventListener, SimpleExoPlayer.VideoListener {

    private final String USER_AGENT = "(￢_￢)";

    private SimpleExoPlayer simpleExoPlayer;
    private int currentVideoWidth = 0;
    private int currentVideoHeight = 0;

    public ExoMedia(IMediaCallback iMediaCallback) {
        super(iMediaCallback);
    }

    private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    @Override
    public void doPrepar(Context context, String url, Map<String, String> headers) {
        release();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(mainThreadHandler, videoTrackSelectionFactory);
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, new DefaultLoadControl(),
                null, false);
        simpleExoPlayer.setPlayWhenReady(true);
        MediaSource mediaSource = buildMediaSource(context, Uri.parse(url));
        simpleExoPlayer.addListener(ExoMedia.this);
        simpleExoPlayer.setVideoListener(ExoMedia.this);
        simpleExoPlayer.prepare(mediaSource, true, true);

    }

    private MediaSource buildMediaSource(Context context, Uri uri) {
        int type = getUrlType(uri.toString());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, new DefaultDataSourceFactory(context, null,
                        new DefaultHttpDataSourceFactory(USER_AGENT, null)),
                        new DefaultSsChunkSource.Factory(new DefaultDataSourceFactory(context, BANDWIDTH_METER,
                                new DefaultHttpDataSourceFactory(USER_AGENT, BANDWIDTH_METER))), mainThreadHandler, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, new DefaultDataSourceFactory(context, null,
                        new DefaultHttpDataSourceFactory(USER_AGENT, null)),
                        new DefaultDashChunkSource.Factory(new DefaultDataSourceFactory(context, BANDWIDTH_METER,
                                new DefaultHttpDataSourceFactory(USER_AGENT, BANDWIDTH_METER))), mainThreadHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, new DefaultDataSourceFactory(context, BANDWIDTH_METER,
                        new DefaultHttpDataSourceFactory(USER_AGENT, BANDWIDTH_METER)), mainThreadHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, new DefaultDataSourceFactory(context, BANDWIDTH_METER,
                        new DefaultHttpDataSourceFactory(USER_AGENT, BANDWIDTH_METER)), new DefaultExtractorsFactory(),
                        mainThreadHandler, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private static int getUrlType(String url) {
        if (url.contains(".mpd")) {
            return C.TYPE_DASH;
        } else if (url.contains(".ism") || url.contains(".isml")) {
            return C.TYPE_SS;
        } else if (url.contains(".m3u8")) {
            return C.TYPE_HLS;
        } else {
            return C.TYPE_OTHER;
        }
    }

    @Override
    public void setSurface(Surface surface) {
        try {
            if (simpleExoPlayer != null)
                simpleExoPlayer.setVideoSurface(surface);
            this.surface = surface;
        } catch (Exception e) {
            e.printStackTrace();
            iMediaCallback.onError(this, 10010, 10010);
        }
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        try {
            if (simpleExoPlayer != null)
                simpleExoPlayer.setVideoSurfaceHolder(surfaceHolder);
            if (surfaceHolder != null)
                this.surface = surfaceHolder.getSurface();
        } catch (Exception e) {
            e.printStackTrace();
            iMediaCallback.onError(this, 10010, 10010);
        }
    }

    @Override
    public void doPlay() {
        if (!isPrepar)
            return;
        simpleExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void doPause() {
        if (!isPrepar)
            return;
        simpleExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void seekTo(int du) {
        if (!isPrepar)
            return;
        simpleExoPlayer.seekTo(du);
    }

    @Override
    public int getCurrentPosition() {
        if (!isPrepar)
            return 0;
        return (int) simpleExoPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (!isPrepar)
            return -1;
        return (int) simpleExoPlayer.getDuration();
    }

    @Override
    public int getVideoHeight() {
        if (!isPrepar)
            return 0;
        return currentVideoHeight;//simpleExoPlayer.getVideoFormat().height;
    }

    @Override
    public int getVideowidth() {
        if (!isPrepar)
            return 0;
        return currentVideoWidth;//simpleExoPlayer.getVideoFormat().width;
    }

    @Override
    public boolean isPlaying() {
        return isPrepar && simpleExoPlayer.getPlayWhenReady();
    }

    @Override
    public boolean setVolume(float leftVol, float rightVol) {
        if (leftVol < 0 | rightVol < 0 | leftVol > 1 | rightVol > 1)
            return false;
        if (isPrepar)
            simpleExoPlayer.setVolume((leftVol + rightVol) / 2);
        return true;
    }

    @Override
    public void release() {
        isPrepar = false;
        if (simpleExoPlayer != null) {
            simpleExoPlayer.release();
        }
        mainThreadHandler.removeCallbacks(runnable);

        simpleExoPlayer = null;
        surface = null;
        currentVideoWidth = 0;
        currentVideoHeight = 0;
    }

    /////////////  media 回调   ///////////////////

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isPrepar)
                iMediaCallback.onBufferingUpdate(ExoMedia.this, simpleExoPlayer.getBufferedPercentage() / 100.f);
            mainThreadHandler.postDelayed(runnable, 1000);
        }
    };


    @Override
    public void onPlayerStateChanged(boolean b, int i) {
        Log.e("ExoMedia", "onPlayerStateChanged " + b + i);

        if (i == ExoPlayer.STATE_READY) {
            if (!isPrepar) {
                isPrepar = true;
                iMediaCallback.onPrepared(this);//第一次初始化
            }
            //缓冲好了
            iMediaCallback.onInfo(this, MEDIA_INFO_BUFFERING_END, MEDIA_INFO_BUFFERING_END);
            mainThreadHandler.removeCallbacks(runnable);
            mainThreadHandler.post(runnable);
        }
        //播放完毕
        if (i == ExoPlayer.STATE_ENDED) {
            mainThreadHandler.removeCallbacks(runnable);
            iMediaCallback.onCompletion(this);
        }
        if (i == ExoPlayer.STATE_BUFFERING)
            iMediaCallback.onInfo(this, MEDIA_INFO_BUFFERING_START, MEDIA_INFO_BUFFERING_START);

    }


    @Override
    public void onPlayerError(ExoPlaybackException e) {
        iMediaCallback.onError(this, MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_UNKNOWN);
    }


    @Override
    public void onVideoSizeChanged(int width, int height, int i2, float v) {
        currentVideoWidth = width;
        currentVideoHeight = height;
        iMediaCallback.onVideoSizeChanged(this, width, height);
    }

    //加载缓冲调用 用来更新缓冲进度..
    @Override
    public void onLoadingChanged(boolean b) {
        iMediaCallback.onBufferingUpdate(ExoMedia.this, simpleExoPlayer.getBufferedPercentage() / 100.f);
        Log.e("ExoMedia", "onLoadingChanged " + b + simpleExoPlayer.getBufferedPercentage());
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object o) {
        Log.e("ExoMedia", "onTimelineChanged");

    }

//    @Override
//    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//        Log.e("ExoMedia", "onTracksChanged");
//    }

    @Override
    public void onPositionDiscontinuity() {
        Log.e("ExoMedia", "onPositionDiscontinuity");

    }

    @Override
    public void onRenderedFirstFrame() {
        Log.e("ExoMedia", "onRenderedFirstFrame");

    }

    @Override
    public void onVideoTracksDisabled() {

    }
}
