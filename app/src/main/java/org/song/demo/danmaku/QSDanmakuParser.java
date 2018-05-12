package org.song.demo.danmaku;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.util.DanmakuUtils;

import static master.flame.danmaku.danmaku.model.IDanmakus.ST_BY_TIME;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2018/5/8
 * 直接解析弹幕
 * 不用load方法加载
 */

public class QSDanmakuParser extends BaseDanmakuParser {

    public static int textShadowColor = Color.WHITE;

    public QSDanmakuParser(String json) {
        this.json = json;
    }

    @Override
    protected IDanmakus parse() {
        if (result == null) result = preParse(mContext);
        return result;
    }


    private Danmakus result;
    private BaseDanmaku item;
    private String json;//解析好释放
    public int index = 0;


    //可以考虑异步预先加载解析好 再去初始化视频
    public synchronized Danmakus preParse(DanmakuContext mContext) {
        this.mContext = mContext;
        result = new Danmakus(ST_BY_TIME, false, mContext.getBaseComparator());
        long l = System.currentTimeMillis();
        int len = 0;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("list");
            len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String p = object.getString("p");
                String t = object.getString("t");
                String[] values = p.split(",");
                long time = (long) (parseFloat(values[0]) * 1000); // 出现时间
                int type = parseInteger(values[1]); // 弹幕类型
                float textSize = parseFloat(values[2]); // 字体大小
                int color = (int) ((0xff000000 | parseLong(values[3])) & 0xffffffff); // 颜色

                item = buildDanmaku(t, type, time, textSize, color);
                Object lock = result.obtainSynchronizer();
                synchronized (lock) {
                    result.addItem(item);
                }
            }
            json = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("QSDanmakuParser", len + "条弹幕,解析弹幕时间:" + (System.currentTimeMillis() - l));
        return result;
    }

    public BaseDanmaku buildDanmaku(String text, int type, long time, float size, int color) {
        item = mContext.mDanmakuFactory.createDanmaku(type, mContext);
        item.setTime(time);
        item.setTimer(getTimer());

        DanmakuUtils.fillText(item, text);//支持换行
        item.textSize = size * density;
        item.textColor = color;
        item.textShadowColor = textShadowColor;
        item.flags = mContext.mGlobalFlagValues;
        item.index = index++;
        return item;
    }

    private float density;

    public void setTextSize(float density) {
        this.density = density;
    }

    private float parseFloat(String floatStr) {
        try {
            return Float.parseFloat(floatStr);
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    private int parseInteger(String intStr) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseLong(String longStr) {
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
