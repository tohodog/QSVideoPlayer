package org.song.demo.io;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class FileUtil {

    //////////////////SD卡////////////////////

    public static final String CHARSET = "utf-8";

    /**
     * 检测SD卡是否存在
     */
    public static boolean checkSDCardExists() {
        return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable());
    }

    /**
     * 获取外部存储上私有数据的路径
     */
    public static String getDiskCacheDir(Context context) {
        String cachePath;
        if (checkSDCardExists()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /////////////////文件名/////////////////////

    /**
     * 取带后缀名的文件名
     */
    public static String getNameWithSuffixFromPath(String path) {
        String fileName;
        if (TextUtils.isEmpty(path)) return null;
        int index = path.lastIndexOf("/");
        if (index > -1) {
            fileName = path.substring(index + 1);
        } else {
            fileName = path; //不包含路径
        }
        return fileName;
    }

    /**
     * 取文件名不带后缀名
     */
    public static String getNameNoSuffixFromPath(String path) {
        String fileName = null;
        if (TextUtils.isEmpty(path)) return fileName;
        int index = path.lastIndexOf("/");
        int lastIndex = path.lastIndexOf(".");
        if (index > -1 && lastIndex > index) {
            fileName = path.substring(index + 1, lastIndex);
        } else {
            fileName = path; //不包含路径
        }
        return fileName;
    }

    /**
     * 获取全路径中的文件拓展名
     *
     * @param file 文件
     * @return 文件拓展名
     */
    public static String getFileExtension(File file) {
        if (file == null) return null;
        return getFileExtension(file.getPath());
    }

    /**
     * 获取全路径中的文件拓展名
     *
     * @param filePath 文件路径
     * @return 文件拓展名
     */
    public static String getFileExtension(String filePath) {
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }


    /////////////////文件读写/////////////////////

    /**
     * 检查文件是否存在
     */
    public static boolean isFileExists(String filePath) {
        return isFileExists(new File(filePath));
    }

    /**
     * 检查文件是否存在
     */
    public static boolean isFileExists(File f) {
        boolean status;
        if (checkSDCardExists()) {
            status = (f != null && f.exists() && f.canRead() && (f.isDirectory() || (f.isFile() && f.length() > 0)));
        } else {
            status = false;
        }
        return status;
    }

    /**
     * 创建新的目录及文件
     *
     * @param folderPath 目录:/xx/xxx
     * @param fileName   文件名: xxx
     * @return File
     */
    public static File createFile(String folderPath, String fileName) {
        if (!checkSDCardExists())
            return null;
        File f = getFile(folderPath, fileName);
        try {
            if (f != null && !f.exists())
                f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return f;
    }

    /**
     * 创建新的目录,不创建文件
     *
     * @param folderPath 目录:/xx/xxx
     * @param fileName   文件名: xxx
     * @return f
     */
    public static File getFile(String folderPath, String fileName) {
        if (!checkSDCardExists())
            return null;
        File destDir = createDirectory(folderPath);
        return new File(destDir, fileName);
    }

    /**
     * 新建目录
     *
     * @param directoryName /xxx/xxx...
     * @return f
     */
    public static File createDirectory(String directoryName) {
        File newPath = null;
        if (checkSDCardExists()) {
            newPath = new File(directoryName);
            if (!newPath.exists())
                newPath.mkdirs();
        }
        return newPath;
    }

    /**
     * 复制Assets下的文件
     */
    public static void copyFromAssets(String fileName, String outFileName, Context context) {
        try {
            ReadHelp.source(context.getAssets().open(fileName)).readToFile(outFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 文件拷贝
     */
    public static boolean fileCopy(String from, String to) {
        boolean result = false;
        try {
            ReadHelp.source(from).readToFile(to);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取文件
     */
    public static String readFile(File f) {
        try {
            return ReadHelp.source(f).readString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取文件
     */
    public static String readFile(String f) {
        return readFile(new File(f));
    }

    /**
     * 读Assets下的文件
     */
    public static String readAssets(String fileName, Context context) {
        try {
            return ReadHelp.source(context.getAssets().open(fileName)).readString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 追加方式写入字符串文件
     *
     * @return 布尔值
     */
    public static boolean writerRAFFile(File f, String s) {
        if (f == null)
            return false;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "rw");
            raf.seek(f.length());
            raf.write(s.getBytes(CHARSET));

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (raf != null)
                    raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean writerBytes(String f, byte[] data) {
        try {
            WriteHelp.sink(f).writeBytes(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 写入字符串文件
     *
     * @return 布尔值
     */
    public static boolean writerString(String f, String s) {
        try {
            WriteHelp.sink(f).writeString(s);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 删除目录所有文件
     */
    public static void deleteAllFile(String filePath) {
        deleteAllFile(createDirectory(filePath));
    }

    /**
     * 删除目录所有文件
     */
    public static void deleteAllFile(final File FFF) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (FFF != null) {
                    dele(FFF);
                }
            }

            public void dele(File files) {
                for (File file : files.listFiles())
                    if (file.isDirectory()) {
                        dele(file);
                        file.delete();// 所有子目录删除
                    } else
                        file.delete();
                // files.delete();// 包括父目录也删除
            }

        }).start();
    }

    /**
     * 删除文件
     */
    public static void deleteFile(String filePath) {
        new File(filePath).delete();
    }

    /**
     * 重命名
     */
    public boolean renameFile(String oldname, String newname) {
        File oldfile = new File(oldname);
        File newfile = new File(newname);
        if (oldname.equals(newname) || newfile.exists() || !oldfile.exists())
            return false;
        return oldfile.renameTo(newfile);
    }


    /**
     * 解压缩一个文件
     *
     * @param zipFile    压缩文件
     * @param folderPath 文件解压到指定目标路径
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static void upZipFile(String zipFile, String folderPath) throws ZipException, IOException {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            InputStream in = zf.getInputStream(entry);
            String str = folderPath + File.separator + entry.getName();
            str = new String(str.getBytes("8859_1"), "utf-8");
            File desFile = new File(str);
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                desFile.createNewFile();
            }
            WriteHelp.sink(desFile).writeByStream(in);
        }
    }


    // 递归
    // 取得文件夹大小
    public static long getFileSizes(File f) {

        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }

        return size;


    }

    public static String FormetFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0KB";
        } else if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }


}
