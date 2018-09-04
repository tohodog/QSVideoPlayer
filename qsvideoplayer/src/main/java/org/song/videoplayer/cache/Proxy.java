package org.song.videoplayer.cache;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.headers.HeaderInjector;

import java.util.Map;

/**
 * Created by song on 2018/9/3
 * Contact with github.com/tohodog
 * 描述
 */

public class Proxy {

    private static HttpProxyCacheServer.Builder builder;

    synchronized static HttpProxyCacheServer getProxy(Context context, final Map<String, String> headers) {
        if (builder == null)
            builder = new HttpProxyCacheServer.Builder(context);
        if (headers != null)
            builder.headerInjector(new HeaderInjector() {
                @Override
                public Map<String, String> addHeaders(String url) {
                    return headers;
                }
            });
        return builder.build();
    }


    public static void setConfig(HttpProxyCacheServer.Builder builder) {
        Proxy.builder = builder;
    }
}
