package org.song.demo.io;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by song on 2017/1/19.
 * edit 2018-4-3 完善
 * 写入读取文件流辅助类
 */

public class WriteHelp {

    public static WriteHelp sink(OutputStream os) {
        return new WriteHelp(os);
    }

    public static WriteHelp sink(File f) throws FileNotFoundException {
        return sink(new FileOutputStream(f)).allCount(f.length());
    }

    public static WriteHelp sink(String path) throws FileNotFoundException {
        return sink(new File(path));
    }

    private WriteHelp(OutputStream os) {
        this.os = os;
        bos = new BufferedOutputStream(os, buffSize);
    }


    private OutputStream os;
    private BufferedOutputStream bos;
    private IProgress hp;
    private long writeCount, allCount = -1;
    private int buffSize = 1024 * 64;//缓存小了 大文件读写影响大
    private boolean isNoClose;//写入后不关闭流,可以连续写入

    public WriteHelp listener(IProgress hp) {
        this.hp = hp;
        return this;
    }

    public WriteHelp allCount(long allCount) {
        this.allCount = allCount;
        return this;
    }

    //
    public WriteHelp writeString(String s, String charset) throws IOException {
        return writeBytes(s.getBytes(charset));
    }

    public WriteHelp writeString(String s) throws IOException {
        return writeBytes(s.getBytes());
    }

    public WriteHelp writeBytes(byte[] bytes) throws IOException {
        int offset = 0, all = bytes.length, buf = 4 * 1024;
        allCount = all;
        while (offset < all) {
            int len = Math.min(buf, all - offset);
            write(bytes, offset, len);
            offset += len;
        }
        close();
        return this;
    }


    //资源来自文件路径
    public WriteHelp writeByFile(String file) throws IOException {
        return writeByFile(new File(file));
    }

    //资源来自文件
    public WriteHelp writeByFile(File file) throws IOException {
        allCount = file.length();
        return writeByStream(new FileInputStream(file));
    }

    //资源来自流
    public WriteHelp writeByStream(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is, buffSize);
        byte[] buf = new byte[4 * 1024];
        int len;
        while ((len = bis.read(buf)) > 0) {
            write(buf, 0, len);
        }
        is.close();
        bis.close();
        close();
        return this;
    }

    private void write(byte[] bytes, int offset, int count) throws IOException {
        bos.write(bytes, offset, count);
        writeCount += count;
        if (hp != null)
            hp.onProgress(writeCount, allCount);
    }

    public void close() throws IOException {
        if (isNoClose)
            return;
        bos.flush();
        bos.close();
        os.close();
    }


    public WriteHelp noClose() {
        isNoClose = true;
        return this;
    }
}
