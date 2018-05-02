package org.song.videoplayer.media;

import android.content.Context;
import android.media.AudioManager;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by song on 2017/2/22.
 * Contact github.com/tohodog
 * ijk解码器父类
 */

public abstract class IjkBaseMedia extends BaseMedia implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnInfoListener {


    public IMediaPlayer mediaPlayer;

    public IjkBaseMedia(IMediaCallback iMediaCallback) {
        super(iMediaCallback);
    }

    @Override
    public void doPrepar(final Context context, final String url, final Map<String, String> headers) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        try {
            release();
            mediaPlayer = getMedia(context, url, headers);
            mediaPlayer.setOnPreparedListener(IjkBaseMedia.this);
            mediaPlayer.setOnVideoSizeChangedListener(IjkBaseMedia.this);
            mediaPlayer.setOnCompletionListener(IjkBaseMedia.this);
            mediaPlayer.setOnErrorListener(IjkBaseMedia.this);
            mediaPlayer.setOnInfoListener(IjkBaseMedia.this);
            mediaPlayer.setOnBufferingUpdateListener(IjkBaseMedia.this);

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            onError(mediaPlayer, MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_UNKNOWN);
        }
        //}  }).start();


    }

    abstract IMediaPlayer getMedia(Context context, String url, Map<String, String> headers) throws Exception;

    @Override
    public void setSurface(Surface surface) {
        try {
            if (mediaPlayer != null)
                mediaPlayer.setSurface(surface);
            this.surface = surface;
        } catch (Exception e) {
            e.printStackTrace();
            iMediaCallback.onError(this, MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_UNKNOWN);
        }
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        try {
            if (mediaPlayer != null)
                mediaPlayer.setDisplay(surfaceHolder);
            if (surfaceHolder != null)
                this.surface = surfaceHolder.getSurface();
        } catch (Exception e) {
            e.printStackTrace();
            iMediaCallback.onError(this, MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_UNKNOWN);
        }
    }

    @Override
    public void doPlay() {
        if (!isPrepar)
            return;
        mediaPlayer.start();
    }

    @Override
    public void doPause() {
        if (!isPrepar)
            return;
        mediaPlayer.pause();
    }

    @Override
    public void seekTo(int duration) {
        if (!isPrepar)
            return;
        mediaPlayer.seekTo(duration);
    }

    @Override
    public int getCurrentPosition() {
        if (!isPrepar)
            return 0;
        try {
            return (int) mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getDuration() {
        if (!isPrepar)
            return 0;
        try {
            return (int) mediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getVideoHeight() {
        if (!isPrepar)
            return 0;
        return mediaPlayer.getVideoHeight();
    }

    @Override
    public int getVideowidth() {
        if (!isPrepar)
            return 0;
        return mediaPlayer.getVideoWidth();
    }

    @Override
    public boolean isPlaying() {
        if (!isPrepar)
            return false;
        return mediaPlayer.isPlaying();
    }

    @Override
    public boolean setVolume(float leftVol, float rightVol) {
        if (leftVol < 0 | rightVol < 0 | leftVol > 1 | rightVol > 1)
            return false;
        if (isPrepar)
            mediaPlayer.setVolume(leftVol, rightVol);
        return this instanceof IjkMedia;
    }

    @Override
    public void release() {

        isPrepar = false;
        this.surface = null;
        if (mediaPlayer != null) {
//            final IMediaPlayer temp = mediaPlayer;
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    if (temp != null) {
//                        temp.release();
//                    }
//                }
//            }).start();
            mediaPlayer.release();
        }
        mediaPlayer = null;

    }

    /////////////以下MediaPlayer回调//////////////

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        isPrepar = true;
        iMediaCallback.onPrepared(this);
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
        iMediaCallback.onBufferingUpdate(this, i / 100.0f);
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        iMediaCallback.onCompletion(this);
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        iMediaCallback.onError(this, what, extra);
        isPrepar = false;
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        iMediaCallback.onInfo(this, what, extra);
        return false;
    }


    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        iMediaCallback.onSeekComplete(this);
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int i2, int i3) {
        iMediaCallback.onVideoSizeChanged(this, width, height);
    }
}