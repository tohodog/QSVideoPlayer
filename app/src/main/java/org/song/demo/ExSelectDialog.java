package org.song.demo;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文件浏览器
 */
@SuppressLint("SdCardPath")
public class ExSelectDialog extends ListActivity {

    public static String KEY_TITILE = "explorer_title";
    public static String KEY_PATH = "explorer_path";
    public static String KEY_RESULT = "explorer_result";


    private List<Map<String, Object>> mData;
    private String mDir = "/sdcard";
    private String title = "选择文件";

    // 缓存功能
    private Map<String, Object> cache = new HashMap<String, Object>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        Bundle bl = intent.getExtras();
        if (bl != null) {
            title = bl.getString(KEY_TITILE);
            mDir = bl.getString(KEY_PATH);
        }

        setTitle(title);
        mData = getData();
        MyAdapter adapter = new MyAdapter(this);
        setListAdapter(adapter);
        getListView().setOnScrollListener(listener);

        // 固定对话框大小
        LayoutParams p = getWindow().getAttributes();
        p.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
        p.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        getWindow().setAttributes(p);

    }

    private List<Map<String, Object>> getData() {

        File f = new File(mDir);
        setTitle(title + " ../" + f.getName());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) cache
                .get(mDir);
        if (list != null)
            return list;
        list = new ArrayList<>();
        Map<String, Object> map = null;

        File[] files = f.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {

                String info = "文件夹";
                String size = "";
                String name = files[i].getName();
                String path = files[i].getPath();
                int imgID = R.mipmap.ex_folder;

                if (files[i].isDirectory()) {
                    imgID = R.mipmap.ex_folder;
                    File[] afiles = files[i].listFiles();
                    if (afiles == null || afiles.length == 0)
                        size = "空";
                    else
                        size = afiles.length + "个子文件";
                } else {
                    String extension = getFileFormat(name);
                    // extension.concat(string)
                    if (Pattern.compile("(jpg)|(jpeg)|(png)|(bmp)|(gif)")
                            .matcher(extension).find()) {
                        imgID = R.mipmap.ex_photo;
                        info = "图片";
                    } else if (Pattern
                            .compile(
                                    "(mp3)|(aac)|(flac)|(ape)|(m4a)|(wav)|(amr)")
                            .matcher(extension).find()) {
                        imgID = R.mipmap.ex_music;
                        info = "音频";
                    } else if (Pattern
                            .compile(
                                    "(avi)|(mp4)|(flv)|(rmvb)|(rm)|(m4v)|(mkv)|(3gp)|(ogg)")
                            .matcher(extension).find()) {
                        imgID = R.mipmap.ex_video;
                        info = "视频";
                    } else if (Pattern.compile("(txt)|(doc)|(docx)|(pdf)")
                            .matcher(extension).find()) {
                        imgID = R.mipmap.ex_doc;
                        info = "文档";
                    } else {
                        imgID = R.mipmap.ex_file;
                        info = extension;
                    }

                    size = bytes2kb(files[i].length());
                }

                map = new HashMap<String, Object>();
                map.put("title", name);
                map.put("path", path);
                map.put("info", info);
                map.put("size", size);
                map.put("img", imgID);
                list.add(map);
            }
        }

        Collections.sort(list, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
                return arg0.get("title").toString().compareTo(arg1.get("title").toString());
            }
        });


        if (!mDir.equals("/")) {
            map = new HashMap<>();
            map.put("title", "Back to ../");
            map.put("path", f.getParent());
            map.put("img", R.mipmap.ex_previous_folder);
            map.put("info", "返回上一级");
            map.put("size", f.getParent());
            list.add(0, map);
        }

        cache.put(mDir, list);
        return list;
    }

    // 点击
    @Override
    protected void onListItemClick(ListView l, View view, int position, long id) {
        int imgid = (int) mData.get(position).get("img");
        if (imgid == R.mipmap.ex_folder
                || imgid == R.mipmap.ex_previous_folder) {
            String path = (String) mData.get(position).get("path");
            if (TextUtils.isEmpty(path))
                return;
            // 保存当前count top;
            cache.put(mDir + "count", itemCount);
            View v = ExSelectDialog.this.getListView().getChildAt(0);
            cache.put(mDir + "top", v == null ? 0 : v.getTop());
            // 更换数据
            mDir = path;
            mData = getData();
            MyAdapter adapter = new MyAdapter(this);
            setListAdapter(adapter);
            // 恢复
            Object o = cache.get(mDir + "count");
            if (o != null)
                ExSelectDialog.this.getListView().setSelectionFromTop((int) o,
                        (int) cache.get(mDir + "top"));
        } else {
            finishWithResult((String) mData.get(position).get("path"));
        }
    }


    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return mData.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_exlistview, null);
                holder.img = (ImageView) convertView.findViewById(R.id.ex_img);
                holder.title = (TextView) convertView.findViewById(R.id.ex_title);
                holder.info = (TextView) convertView.findViewById(R.id.ex_info);
                holder.size = (TextView) convertView.findViewById(R.id.ex_size);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.img.setBackgroundResource((Integer) mData.get(position).get("img"));
            holder.title.setText((String) mData.get(position).get("title"));
            holder.info.setText((String) mData.get(position).get("info"));
            holder.size.setText((String) mData.get(position).get("size"));
            return convertView;
        }

        private final class ViewHolder {
            ImageView img;
            TextView title;
            TextView info;
            TextView size;
        }
    }

    private int itemCount;

    private OnScrollListener listener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // TODO Auto-generated method stub
            System.out.println("Y" + scrollState);

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            System.out.println("F" + firstVisibleItem);
            itemCount = firstVisibleItem;
        }
    };

    private void finishWithResult(String path) {
        Intent intent = new Intent();
        intent.putExtra(KEY_RESULT, path);
        setResult(RESULT_OK, intent);
        finish();
    }

    public String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        float returnValue = 0;

        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1024) {
            BigDecimal megabyte = new BigDecimal(1024 * 1024);
            returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                    .floatValue();

            if (returnValue > 1024) {
                BigDecimal Gbyte = new BigDecimal(1024 * 1024 * 1024);
                returnValue = filesize.divide(Gbyte, 2, BigDecimal.ROUND_UP)
                        .floatValue();
                return (returnValue + "GB");
            }
            return (returnValue + "MB");
        }

        if (returnValue > 1)
            return (returnValue + "KB");

        return (bytes + "B");

    }

    /**
     * 获取文件扩展名
     */
    public String getFileFormat(String fileName) {
        if (TextUtils.isEmpty(fileName))
            return "";
        int point = fileName.lastIndexOf('.');
        if (point < 0)
            return "文件";
        return fileName.substring(point + 1);
    }
}
