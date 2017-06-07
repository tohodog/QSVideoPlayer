package org.song.demo;

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
import org.song.videoplayer.ConfigManage;
import org.song.videoplayer.DemoQSVideoView;
import org.song.videoplayer.IVideoPlayer;
import org.song.videoplayer.PlayListener;

import java.util.ArrayList;
import java.util.List;


public class ListVideoActivity extends AppCompatActivity implements CallBack {

    RecyclerView recyclerView;
    List<String> data = new ArrayList<>();
    ListCalculator calculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video);
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
    }

    DemoQSVideoView demoQSVideoView;

    @Override
    public void setActive(View newActiveView, int setActive) {
        demoQSVideoView = (DemoQSVideoView) newActiveView.findViewById(R.id.qs);
        demoQSVideoView.play();
        Log.d("2333setActive", "" + setActive);
    }

    @Override
    public void deactivate(View currentView, int position) {
        DemoQSVideoView demoQSVideoView = (DemoQSVideoView) currentView.findViewById(R.id.qs);
        demoQSVideoView.release();
        Log.d("2333deactivate", "" + position);

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
            return new Holder(View.inflate(ListVideoActivity.this, R.layout.item_video, null));
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

        DemoQSVideoView demoQSVideoView;

        Holder(View itemView) {
            super(itemView);

            demoQSVideoView = (DemoQSVideoView) itemView.findViewById(R.id.qs);
            demoQSVideoView.setPlayListener(new PlayListener() {
                @Override
                public void onStatus(int status) {

                }

                @Override
                public void onMode(int mode) {

                }

                @Override
                public void onEvent(int what, Integer... extra) {
                    if (what == IVideoPlayer.EVENT_PREPARE_START) {
                        //ConfigManage.releaseOther(demoQSVideoView);
                        calculator.setCurrentActiveItem(getLayoutPosition());
                    }
                }
            });
        }

        public void bindData(String s) {
            String[] arr = s.split(",");
            demoQSVideoView.setUp(arr[1], arr[0]);
            demoQSVideoView.getCoverImageView().setImageResource(R.mipmap.ic_launcher);
            FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(-1, (int) (((int) (Math.random() * 600) + 100) * getResources().getDisplayMetrics().density));
            demoQSVideoView.setLayoutParams(l);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        ConfigManage.releaseAll();
    }
}
