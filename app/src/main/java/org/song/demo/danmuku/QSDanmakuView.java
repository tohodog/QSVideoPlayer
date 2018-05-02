package org.song.demo.danmuku;

import android.content.Context;
import android.util.AttributeSet;

import java.io.InputStream;
import java.util.HashMap;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;

import static master.flame.danmaku.danmaku.model.IDanmakus.ST_BY_TIME;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2018/4/28
 * 描述
 */

public class QSDanmakuView extends master.flame.danmaku.ui.widget.DanmakuView {


    public Danmakus result;
    public BaseDanmaku item = null;
    protected DanmakuContext mContext;

    public QSDanmakuView(Context context) {
        super(context);
        initView();
    }

    public QSDanmakuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public QSDanmakuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {

        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mContext = DanmakuContext.create();
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
//                .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
//        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair)
                .setDanmakuMargin(40);
        result = new Danmakus(ST_BY_TIME, false, mContext.getBaseComparator());

    }


    private BaseDanmakuParser createParser(InputStream stream) {


    }
}
