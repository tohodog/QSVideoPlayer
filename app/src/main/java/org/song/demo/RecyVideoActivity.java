package org.song.demo;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.song.demo.listvideo.CallBack;
import org.song.demo.listvideo.ListCalculator;
import org.song.demo.listvideo.RecyclerViewGetter;
import org.song.videoplayer.DemoQSVideoView;
import org.song.videoplayer.IVideoPlayer;
import org.song.videoplayer.PlayListener;
import org.song.videoplayer.media.IjkMedia;

import java.util.ArrayList;
import java.util.List;


public class RecyVideoActivity extends AppCompatActivity implements CallBack {

    RecyclerView recyclerView;
    List<String> data = new ArrayList<>();
    ListCalculator calculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recy_video);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        for (int i = 0; i < 100; i++)
            data.add("这是一个标题" + i + ",http://videos.kpie.com.cn/videos/20170526/037DCE54-EECE-4520-AA92-E4002B1F29B0.mp4");

        recyclerView.setAdapter(new Adapter(data));


        calculator = new ListCalculator(new RecyclerViewGetter((LinearLayoutManager) recyclerView.getLayoutManager(), recyclerView), this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int newState = 0;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                this.newState = newState;
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    calculator.onScrolled(300);
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                calculator.onScrolling(newState);

            }
        });
        //new LinearSnapHelper().attachToRecyclerView(recyclerView);
    }

    DemoQSVideoView demoQSVideoView;

    @Override
    public void activeOnScrolled(View newActiveView, int position) {
        demoQSVideoView = (DemoQSVideoView) newActiveView.findViewById(R.id.qs);
        if (demoQSVideoView != null)
            demoQSVideoView.play();
        Log.d("activeOnScrolled", "" + position);
    }

    @Override
    public void activeOnScrolling(View newActiveView, int position) {
        Log.d("activeOnScrolled", "" + position);
//        ObjectAnimator animator = ObjectAnimator.ofFloat(newActiveView, "alpha", 0.3f, 1);
//        animator.setDuration(300);
//        animator.start();
    }

    @Override
    public void deactivate(View currentView, int position) {
        final DemoQSVideoView demoQSVideoView = (DemoQSVideoView) currentView.findViewById(R.id.qs);
        if (demoQSVideoView != null)
            demoQSVideoView.releaseInThread();
        Log.d("deactivate", "" + position);
//        ObjectAnimator animator = ObjectAnimator.ofFloat(currentView, "alpha", 1, 0.3f);
//        animator.setDuration(300);
//        animator.start();
    }

    @Override
    public void onBackPressed() {
        if (demoQSVideoView != null && demoQSVideoView.onBackPressed())
            return;
        super.onBackPressed();
    }

    class Adapter extends RecyclerView.Adapter<Holder> {

        List<String> data;

        Adapter(List<String> data) {
            this.data = data;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(View.inflate(RecyVideoActivity.this, R.layout.item_video, null));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.bindData(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }


    class Holder extends RecyclerView.ViewHolder {

        DemoQSVideoView qsVideoView;

        Holder(View itemView) {
            super(itemView);

            qsVideoView = (DemoQSVideoView) itemView.findViewById(R.id.qs);
            qsVideoView.setDecodeMedia(IjkMedia.class);
            qsVideoView.setPlayListener(new PlayListener() {
                @Override
                public void onStatus(int status) {

                }

                @Override
                public void onMode(int mode) {

                }

                @Override
                public void onEvent(int what, Integer... extra) {
                    if (what == IVideoPlayer.EVENT_PREPARE_START) {
                        //ConfigManage.releaseOther(qsVideoView);
                        calculator.setCurrentActiveItem(getLayoutPosition());
                    }
                }
            });
            qsVideoView.isShowWifiDialog = false;
        }

        public void bindData(String s) {
            String[] arr = s.split(",");
            qsVideoView.setUp(arr[1], arr[0]);
            qsVideoView.getCoverImageView().setImageResource(R.mipmap.cover);
            FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(-1, (getResources().getDisplayMetrics().widthPixels * 3 / 4));
            //qsVideoView.setLayoutParams(l);
            //itemView.setAlpha(0.3f);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (demoQSVideoView != null)
            demoQSVideoView.release();
    }
}
