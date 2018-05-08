package org.song.demo.io;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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
 * 读取文件流辅助类
 */

public class ReadHelp {


    public static ReadHelp source(InputStream is) {
        return new ReadHelp(is);
    }

    public static ReadHelp source(File f) throws FileNotFoundException {
        return source(new FileInputStream(f)).allCount(f.length());
    }

    public static ReadHelp source(String path) throws FileNotFoundException {
        return source(new File(path));
    }

    private ReadHelp(InputStream is) {
        this.is = is;
        bis = new BufferedInputStream(is, buffSize);
    }


    private InputStream is;
    private BufferedInputStream bis;
    private IProgress hp;
    private long readCount, allCount = -1;
    private int buffSize = 1024 * 64;//缓存小了 大文件读写影响大

    public ReadHelp listener(IProgress hp) {
        this.hp = hp;
        return this;
    }

    public ReadHelp allCount(long allCount) {
        this.allCount = allCount;
        return this;
    }


    //读取成字节数组
    public byte[] readBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        readToStream(baos, false);
        return baos.toByteArray();
    }

    //读取成字符串
    public String readString(String charset) throws IOException {
        return new String(readBytes(), charset);
    }

    //读取成字符串
    public String readString() throws IOException {
        return new String(readBytes());
    }

    //读取后写入文件路径
    public void readToFile(String file) throws IOException {
        readToFile(new File(file));
    }

    //读取后写入文件
    public void readToFile(File file) throws IOException {
        readToStream(new FileOutputStream(file), true);
    }

    //读取后写入流 isClose是否关闭输出流
    public void readToStream(OutputStream os, boolean isClose) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(os, buffSize);
        byte[] buf = new byte[4 * 1024];
        int len;
        while ((len = read(buf)) > 0) {
            bos.write(buf, 0, len);
        }
        bos.flush();
        if (isClose) {
            bos.close();
            os.close();
        }
        close();
    }


    private int read(byte[] bytes) throws IOException {
        int len = bis.read(bytes);
        if (len > 0)
            readCount += len;
        if (hp != null)
            hp.onProgress(readCount, allCount);
        return len;
    }

    public void close() throws IOException {
        bis.close();
        is.close();
    }
//
//    //线性链表
//    private class Segment {
//        byte[] data;
//        Segment next;
//        Segment prev;
//        int len;
//
//        Segment(int size) {
//            this.data = new byte[size];
//        }
//
//        //链表弹出一个
//        public Segment pop() {
//            Segment result = next != this ? next : null;
//            if (prev != null)
//                prev.next = next;
//            if (next != null)
//                next.prev = prev;
//            next = null;
//            prev = null;
//            return result;
//        }
//
//        //链表插入一个
//        public Segment push(Segment segment) {
//            segment.prev = this;
//            segment.next = next;
//            if (next != null)
//                next.prev = segment;
//            next = segment;
//            return segment;
//        }
//
//    }
}
