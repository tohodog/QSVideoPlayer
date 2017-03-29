package org.song.videoplayer.rederview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.song.videoplayer.media.IMediaControl;

/**
 * 小于4.0用这个绘制view
 * ps:切换全屏效果不好 会停顿
 */
public class SufaceRenderView extends SurfaceView implements SurfaceHolder.Callback, IRenderView {

    protected static final String TAG = "SufaceRenderView";

    int videoWidth;
    int videoHeight;

    private IRenderCallback callback;

    public SufaceRenderView(Context context) {
        super(context);
        init();
    }

    public SufaceRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }


    @Override
    public View get() {
        return this;
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth >= 0 && videoHeight >= 0) {
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            if (holder != null)
                holder.setFixedSize(videoWidth, videoHeight);

            requestLayout();
        }
    }


    @Override
    public void setVideoRotation(int degree) {
//        if (degree != getRotation()) {
//            super.setRotation(degree);
//            requestLayout();
//        }
    }

    @Override
    public void addRenderCallback(IRenderCallback callback) {
        this.callback = callback;
    }

    @Override
    public void removeRenderCallback() {
        this.callback = null;
    }


    @Override
    public SurfaceHolder getSurfaceHolder() {
        return holder;
    }

    @Override
    public Surface openSurface() {
        if (holder != null)
            return holder.getSurface();
        return null;
    }

    @Override
    public void bindMedia(IMediaControl iMediaControl) {
        //iMediaControl.setDisplay(null);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        iMediaControl.setDisplay(getSurfaceHolder());
    }

    private SurfaceHolder holder;


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        this.holder = holder;
        if (callback != null)
            callback.onSurfaceCreated(this, 0, 0);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged " + width + "-" + height);
        if (callback != null)
            callback.onSurfaceChanged(this, 0, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        if (callback != null)
            callback.onSurfaceDestroyed(this);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure " + " [" + this.hashCode() + "] ");
        int viewRotation = 0;//(int) getRotation();

        Log.i(TAG, "videoWidth = " + videoWidth + ", " + "videoHeight = " + videoHeight);
        Log.i(TAG, "viewRotation = " + viewRotation);

        // 如果判断成立，则说明显示的TextureView和本身的位置是有90度的旋转的，所以需要交换宽高参数。
        if (viewRotation == 90 || viewRotation == 270) {
            int tempMeasureSpec = widthMeasureSpec;
            widthMeasureSpec = heightMeasureSpec;
            heightMeasureSpec = tempMeasureSpec;
        }

        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);
        if (videoWidth > 0 && videoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            Log.i(TAG, "widthMeasureSpec  [" + MeasureSpec.toString(widthMeasureSpec) + "]");
            Log.i(TAG, "heightMeasureSpec [" + MeasureSpec.toString(heightMeasureSpec) + "]");

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight;
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * videoHeight / videoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * videoWidth / videoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth;
                height = videoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }


}
