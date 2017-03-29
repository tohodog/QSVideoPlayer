package org.song.videoplayer.rederview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;

import org.song.videoplayer.media.IMediaControl;

/**
 * 大于4.0用这个绘制view
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TextureRenderView extends TextureView implements TextureView.SurfaceTextureListener, IRenderView {

    protected static final String TAG = "TextureRenderView";

    int videoWidth;
    int videoHeight;

    private IRenderCallback callback;

    public TextureRenderView(Context context) {
        super(context);
        init();
    }

    public TextureRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
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
            requestLayout();
        }
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return null;
    }

    @Override
    public void setVideoRotation(int degree) {
        if (degree != getRotation()) {
            super.setRotation(degree);
            requestLayout();
        }
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
    public Surface openSurface() {
        if (surface != null)
            return new Surface(surface);
        return null;
    }

    @Override
    public void bindMedia(IMediaControl iMediaControl) {
        iMediaControl.setSurface(openSurface());
    }

    private SurfaceTexture surface;

    @TargetApi(16)
    @Override//改变大小时会重建Surface
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable " + width + "-" + height);
        if (this.surface == null || Build.VERSION.SDK_INT < 16) {
            this.surface = surface;
            if (callback != null)
                callback.onSurfaceCreated(this, width, height);
        } else {
            //当api大于16 不用重新set解码器的suface 把原来的更新进view就行
            setSurfaceTexture(this.surface);
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged " + width + "-" + height);
        if (callback != null)
            callback.onSurfaceChanged(this, 0, width, height);
    }

    @Override//返回值待研究...
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        if (callback != null)
            callback.onSurfaceDestroyed(this);
        return this.surface == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //Log.i(TAG, "onSurfaceTextureUpdated");
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure " + " [" + this.hashCode() + "] ");
        int viewRotation = (int) getRotation();

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
