package org.song.videoplayer.rederview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
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
 * Contact github.com/tohodog
 * 大于4.0用这个绘制view
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TextureRenderView extends TextureView implements TextureView.SurfaceTextureListener, IRenderView {

    protected static final String TAG = "TextureRenderView";

    private MeasureHelper mMeasureHelper;
    private int videoWidth;
    private int videoHeight;

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
        mMeasureHelper = new MeasureHelper(this);
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
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setAspectRatio(int aspectRatio) {
        mMeasureHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return null;
    }

    @Override
    public void setVideoRotation(int degree) {
        if (degree != getRotation()) {
            super.setRotation(degree);
            mMeasureHelper.setVideoRotation(degree);
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

    @Override
    public Bitmap getCurrentFrame() {
        return getBitmap();
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
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }


}
