package org.song.videoplayer;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

public class VideoPopWindow extends PopupWindow implements View.OnClickListener {

    int h;

    public VideoPopWindow(Context context, List<QSVideo> qsVideos, int index) {
        ViewGroup popview = (ViewGroup) LayoutInflater.from(context).inflate(
                R.layout.pop_definition, new FrameLayout(context), false);
        float density = context.getResources().getDisplayMetrics().density;
        int padding = (int) (density * 12);
        for (int i = 0; i < qsVideos.size(); i++) {
            QSVideo qsVideo = qsVideos.get(i);
            TextView textView = new TextView(context);
            textView.setId(i);
            textView.setPadding(padding, padding / 2, padding, padding / 2);
            textView.setText(qsVideo.resolution());
            textView.setTextSize(14);
            textView.setOnClickListener(this);
            textView.setTextColor(index == i ? context.getResources().getColor(R.color.colorMain) : 0xffffffff);
            popview.addView(textView);
        }
        int mode = View.MeasureSpec.AT_MOST;
        //手动调用计算宽高
        popview.measure(View.MeasureSpec.makeMeasureSpec(1080, mode),
                View.MeasureSpec.makeMeasureSpec(1920, mode));
        h = popview.getMeasuredHeight();
        //设置视图
        setContentView(popview);
        setWidth(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);//设置宽
        setHeight(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);//设置高
        setFocusable(true);
        setOutsideTouchable(true);
        // 刷新状态
        update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        setBackgroundDrawable(dw);
    }

    public void showTop(View view) {
        showAsDropDown(view, 0, -h - view.getMeasuredHeight());
    }

    @Override
    public void onClick(View v) {
        itemListener.OnClick(v.getId());
        dismiss();
    }

    private OnItemListener itemListener;

    public void setOnItemListener(OnItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public interface OnItemListener {
        void OnClick(int position);
    }


}