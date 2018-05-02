package org.song.videoplayer.rederview;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Contact github.com/tohodog
 * 根据缩放模式设置渲染view的大小
 */
public final class MeasureHelper {
    private WeakReference<View> mWeakView;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoSarNum;
    private int mVideoSarDen;

    private int mVideoRotationDegree;

    private int mMeasuredWidth;
    private int mMeasuredHeight;

    private int mCurrentAspectRatio = IRenderView.AR_ASPECT_FIT_PARENT;

    public MeasureHelper(View view) {
        mWeakView = new WeakReference<View>(view);
    }

    public View getView() {
        if (mWeakView == null)
            return null;
        return mWeakView.get();
    }

    public void setVideoSize(int videoWidth, int videoHeight) {
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
    }

    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        mVideoSarNum = videoSarNum;
        mVideoSarDen = videoSarDen;
    }

    public void setVideoRotation(int videoRotationDegree) {
        mVideoRotationDegree = videoRotationDegree;
    }

    /**
     * 根据模式计算视频显示的大小
     */
    public void doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) {
            int tempSpec = widthMeasureSpec;
            widthMeasureSpec = heightMeasureSpec;
            heightMeasureSpec = tempSpec;
        }

        int width = View.getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = View.getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mCurrentAspectRatio == IRenderView.AR_MATCH_PARENT) {
            width = widthMeasureSpec;
            height = heightMeasureSpec;
        } else if (mVideoWidth > 0 && mVideoHeight > 0) {
            int widthSpecSize = width;
            int heightSpecSize = height;

            //Log.i("viewsize", "容器控件大小 = " + widthSpecSize + "," + heightSpecSize);

            float displayAspectRatio;//计算不同模式的视频比例
            switch (mCurrentAspectRatio) {
                case IRenderView.AR_16_9_FIT_PARENT:
                    displayAspectRatio = 16.0f / 9.0f;
                    if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270)
                        displayAspectRatio = 1.0f / displayAspectRatio;
                    break;
                case IRenderView.AR_4_3_FIT_PARENT:
                    displayAspectRatio = 4.0f / 3.0f;
                    if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270)
                        displayAspectRatio = 1.0f / displayAspectRatio;
                    break;
                case IRenderView.AR_ASPECT_FIT_PARENT:
                case IRenderView.AR_ASPECT_FILL_PARENT:
                case IRenderView.AR_ASPECT_WRAP_CONTENT:
                default:
                    displayAspectRatio = (float) mVideoWidth / (float) mVideoHeight;
                    if (mVideoSarNum > 0 && mVideoSarDen > 0)
                        displayAspectRatio = displayAspectRatio * mVideoSarNum / mVideoSarDen;
                    break;
            }
            //容器view比例
            float specAspectRatio = (float) widthSpecSize / (float) heightSpecSize;
            //容器比例和视频比例大小 (确定是哪一边抵触边缘用,true表示视频比容器胖 false表示表示比较瘦
            boolean shouldBeWider = displayAspectRatio > specAspectRatio;
            //计算出宽高
            switch (mCurrentAspectRatio) {
                case IRenderView.AR_ASPECT_FILL_PARENT:
                    if (shouldBeWider) {
                        // not high enough, fix height
                        height = heightSpecSize;
                        width = (int) (height * displayAspectRatio);
                    } else {
                        // not wide enough, fix width
                        width = widthSpecSize;
                        height = (int) (width / displayAspectRatio);
                    }
                    break;
                case IRenderView.AR_ASPECT_WRAP_CONTENT:
                    if (shouldBeWider) {
                        // too wide, fix width
                        width = Math.min(mVideoWidth, widthSpecSize);
                        height = (int) (width / displayAspectRatio);
                    } else {
                        // too high, fix height
                        height = Math.min(mVideoHeight, heightSpecSize);
                        width = (int) (height * displayAspectRatio);
                    }

                    break;
                case IRenderView.AR_ASPECT_FIT_PARENT:
                case IRenderView.AR_16_9_FIT_PARENT:
                case IRenderView.AR_4_3_FIT_PARENT:
                default:
                    if (shouldBeWider) {
                        // too wide, fix width
                        width = widthSpecSize;
                        height = (int) (width / displayAspectRatio);
                    } else {
                        // too high, fix height
                        height = heightSpecSize;
                        width = (int) (height * displayAspectRatio);
                    }
                    break;


            }
        }

        mMeasuredWidth = width;
        mMeasuredHeight = height;
        //Log.i("viewsize", "视频大小 = " + mVideoWidth + "," + mVideoHeight
        //        + "\n改变后view大小 = " + width + "," + height);

    }

    public int getMeasuredWidth() {
        return mMeasuredWidth;
    }

    public int getMeasuredHeight() {
        return mMeasuredHeight;
    }

    public void setAspectRatio(int aspectRatio) {
        mCurrentAspectRatio = aspectRatio;
    }

}
