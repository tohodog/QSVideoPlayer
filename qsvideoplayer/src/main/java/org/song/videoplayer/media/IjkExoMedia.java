package org.song.videoplayer.media;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.util.Map;

import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by song on 2017/2/22.
 * Contact github.com/tohodog
 */

public class IjkExoMedia extends IjkBaseMedia {

    public IjkExoMedia(IMediaCallback iMediaCallback) {
        super(iMediaCallback);
    }

    @Override
    IMediaPlayer getMedia(Context context, String url, Map<String, String> headers, Object... objects) throws Exception {
        IjkExoMediaPlayer mediaPlayer = new IjkExoMediaPlayer(context);

        if (url.startsWith(ContentResolver.SCHEME_FILE)) {
            mediaPlayer.setDataSource(url.replace("file:/", ""));
        } else {
            mediaPlayer.setDataSource(context, Uri.parse(url), headers);
        }

        mainThreadHandler.postDelayed(runnable, 500);
        return mediaPlayer;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isPrepar)
                onBufferingUpdate(mediaPlayer, ((IjkExoMediaPlayer) mediaPlayer).getBufferedPercentage());
            mainThreadHandler.postDelayed(runnable, 1000);
        }
    };


    @Override
    public boolean setSpeed(float rate) {
        return false;
    }

    @Override
    public void release() {
        super.release();
        mainThreadHandler.removeCallbacks(runnable);
    }


}
