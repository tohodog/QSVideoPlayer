package org.song.videoplayer.floatwindow;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Created by song on 2018/1/9.
 * Contact github.com/tohodog
 * 浮窗容器
 * 拦截触摸移动事件，实现浮窗移动
 */

public class FloatMoveView extends FrameLayout {


    private int touchSlop;

    public FloatMoveView(Context context) {
        this(context, null);
    }

    public FloatMoveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatMoveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private boolean isBeingDragged;//是否触发拖曳
    private float mInitialMotionY, mInitialMotionX;

    @Override//判断是否截取事件进行刷新
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialMotionY = ev.getY();
                mInitialMotionX = ev.getX();
                isBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE:
                final float y = ev.getY();
                final float x = ev.getX();
                isBeingDragged = Math.abs(mInitialMotionY - y) > touchSlop ||
                        Math.abs(mInitialMotionX - x) > touchSlop;
                if (isBeingDragged) {
                    mInitialMotionY = ev.getRawY();
                    mInitialMotionX = ev.getRawX();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
        }
        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isBeingDragged)
            return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                final float y = event.getRawY();
                final float x = event.getRawX();
                moveListener.move((int) (x - mInitialMotionX), (int) (y - mInitialMotionY));
                //mInitialMotionY = y;
                //mInitialMotionX = x;

                //Log.e("move",
                //        x + "-" + y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                moveListener.end();

        }
        return true;
    }

    public void setRount(int r) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setOutlineProvider(new TextureVideoViewOutlineProvider(r));
            this.setClipToOutline(true);
        }
//        GradientDrawable gradientDrawable = new GradientDrawable();
//        gradientDrawable.setCornerRadius(r);
//        setBackgroundDrawable(gradientDrawable);
    }

    public interface MoveListener {
        void move(int x, int y);

        void end();
    }

    public void setMoveListener(MoveListener moveListener) {
        this.moveListener = moveListener;
    }

    private MoveListener moveListener;

}
