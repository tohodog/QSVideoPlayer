package org.song.videoplayer.media;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by song on 2017/2/22.
 * Contact github.com/tohodog
 * 哔哩哔哩播放器
 */

public class IjkMedia extends IjkBaseMedia {

    public IjkMedia(IMediaCallback iMediaCallback) {
        super(iMediaCallback);
    }

    @Override
    IMediaPlayer getMedia(Context context, String url, Map<String, String> headers) throws Exception {
        IjkMediaPlayer mediaPlayer = new IjkMediaPlayer();

        if (url.startsWith(ContentResolver.SCHEME_CONTENT) || url.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE))
            mediaPlayer.setDataSource(context, Uri.parse(url), headers);
        else
            mediaPlayer.setDataSource(url, headers);


//        if (mSettings.getUsingMediaCodec()) {
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//            if (mSettings.getUsingMediaCodecAutoRotate()) {
//                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
//            } else {
//                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
//            }
//        } else {
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
//        }
//
//        if (mSettings.getUsingOpenSLES()) {
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
//        } else {
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
//        }
//
//        String pixelFormat = mSettings.getPixelFormat();
//        if (TextUtils.isEmpty(pixelFormat)) {
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
//        } else {
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixelFormat);
//        }
//        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
//        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
//
//        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//
//        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        return mediaPlayer;
    }
}
