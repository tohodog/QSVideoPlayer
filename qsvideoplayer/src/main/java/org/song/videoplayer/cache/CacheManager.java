package org.song.videoplayer.cache;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

import java.util.Map;

/**
 * Created by song on 2018/9/3
 * Contact with github.com/tohodog
 * 描述
 */

public class CacheManager {

    public static String buildCahchUrl(Context context, String rawUrl, Map<String, String> headers) {
        HttpProxyCacheServer proxy = Proxy.getProxy(context, headers);
        return proxy.getProxyUrl(rawUrl);
    }

}
