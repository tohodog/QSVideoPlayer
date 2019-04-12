package org.song.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.song.demo.listvideo.CallBack;
import org.song.demo.listvideo.ListCalculator;
import org.song.demo.listvideo.ListViewGetter;
import org.song.videoplayer.DemoQSVideoView;
import org.song.videoplayer.IVideoPlayer;
import org.song.videoplayer.PlayListener;

import java.util.ArrayList;
import java.util.List;


public class ListVideoActivity extends AppCompatActivity implements CallBack {

    ListView listView;
    List<String> data = new ArrayList<>();
    ListCalculator calculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video);
        listView = (ListView) findViewById(R.id.recyclerview);
        //listView.setLayoutManager(new LinearLayoutManager(this));
        for (int i = 0; i < 100; i++)
            data.add("这是一个标题" + i + ",http://videos.kpie.com.cn/videos/20170526/037DCE54-EECE-4520-AA92-E4002B1F29B0.mp4");


        calculator = new ListCalculator(new ListViewGetter(listView), this);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.newState = scrollState;
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    calculator.onScrolled(300);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                calculator.onScrolling(newState);

            }

            int newState = 0;

        });

        listView.setAdapter(new Adapter(data));

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
    }

    @Override
    public void deactivate(View currentView, int position) {
        final DemoQSVideoView demoQSVideoView = (DemoQSVideoView) currentView.findViewById(R.id.qs);
        if (demoQSVideoView != null)
            demoQSVideoView.releaseInThread();
        Log.d("deactivate", "" + position);
    }

    @Override
    public void onBackPressed() {
        if (demoQSVideoView != null && demoQSVideoView.onBackPressed())
            return;
        super.onBackPressed();
    }


    class Adapter extends BaseAdapter {

        List<String> data;

        Adapter(List<String> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder h = null;
            if (convertView == null)
                h = new Holder(View.inflate(ListVideoActivity.this, R.layout.item_video, null));
            else
                h = (Holder) convertView.getTag();
            h.position = position;

            h.bindData(getItem(position));


            return h.itemView;
        }
    }


    class Holder {
        int position;
        DemoQSVideoView qsVideoView;
        View itemView;

        Holder(View itemView) {
            this.itemView = itemView;
            itemView.setTag(this);
            qsVideoView = (DemoQSVideoView) itemView.findViewById(R.id.qs);
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
                        calculator.setCurrentActiveItem(position);
                    }
                }
            });
            qsVideoView.isShowWifiDialog = false;
        }

        public void bindData(String s) {
            String[] arr = s.split(",");
            if (arr[1].equals(qsVideoView.getUrl())) return;
            qsVideoView.setUp(arr[1], arr[0]);
            qsVideoView.getCoverImageView().setImageResource(R.mipmap.cover1);
            FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(-1, (int) (((int) (Math.random() * 600) + 100) * getResources().getDisplayMetrics().density));
            //qsVideoView.setLayoutParams(l);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (demoQSVideoView != null)
            demoQSVideoView.release();
    }
}
