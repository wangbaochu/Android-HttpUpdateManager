package com.open.utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created on 2015/4/20.
 */
public class FileUtils {

    public static enum Size {
        B(1), KB(1000), MB(1000000), GB(1000000000);

        long time;

        private Size(long time) {
            this.time = time;
        }

        public double convert(long byt) {
            BigDecimal bd = new BigDecimal(byt / time);
            return bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        public static String format(long byt) {
            Size b;
            if (byt < KB.time) {
                b = B;
            } else if (byt < MB.time) {
                b = KB;
            } else if (byt < GB.time) {
                b = MB;
            } else {
                b = GB;
            }
            return String.valueOf(b.convert(byt)) + b.name();
        }
    }


    public static long getFileSize(String filePath){
        File file = new File(filePath);
        return (file.exists()) ? file.length() : 0;
    }

    private static boolean _nativeInitialized = false;

    /**
     * 获取文件、文件夹大小
     *
     * @param f
     * @return
     */
    public static long getFolderOrFileSize(String f) {
        if (TextUtils.isEmpty(f)) {
            return 0L;
        }

        if (_nativeInitialized) {
            return getFileSizeNative(f);
        }
        return getFolderOrFileSize(new File(f));
    }

    // get file or folder size.
    public static native long getFileSizeNative(String file);

    // copy from CUtils.java, Utils.java
    private static final String CPU_ARCHITECTURE_KEY = "ro.product.cpu.abi";
    public static String getSystemProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(clazz, key, ""));
        } catch (Exception e) {
        }

        return value;
    }
    // 该判断是根据成淼提供的方法判断的
    public static boolean isCpuX86() {
        // try {
        // String cpuArch = System.getProperty("os.arch");
        // if (TextUtils.isEmpty(cpuArch)) {
        // Log.e("jabe", "get system property os.arch null or empty.");
        // return false;
        // }
        // if (cpuArch.contains("i386") || cpuArch.contains("i686")) {
        // return true;
        // } else
        // return false;
        // } catch (Exception e) {
        // return false;
        // }
        if (getSystemProperty(CPU_ARCHITECTURE_KEY, "arm").contains("x86")) {
            return true;
        }
        return false;
    }

    static {
        try {
            // loads a native library
            if ( isCpuX86() )
                System.loadLibrary("cleanerutilsx86");
            else
                System.loadLibrary("cleanerutils");
            Log.i("FileUtils", "using native filesize hash");
            _nativeInitialized = true;
        } catch (Exception e) {
            Log.e("FileUtils", "using java filesize hash: " + e.toString());
            _nativeInitialized = false;
        } catch (Error e) {
            // 主要是为了防止UnsatisfiedLinkError崩溃
            Log.e("FileUtils", "using java filesize hash: " + e.toString());
            _nativeInitialized = false;
        }
    }

    /**
     * 获取文件、文件夹大小
     *
     * @param f
     * @return
     */
    public static long getFolderOrFileSize(File f) {
        if (f == null) {
            return 0L;
        }
        long size = 0L;
        try {
            if (f.exists()) {
                if (f.isDirectory()) {
                    size = /*4096 + */getFolderSize(f);
                } else if (f.isFile()) {
                    size = f.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 获取文件夹大小,递归操作
     *
     * @param f
     * @return
     */
    private static long getFolderSize(File f) {

        long size = 0;
        try {
            java.io.File[] fileList = f.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;
    }

    /**
     * 删除文件,保留文件夹
     * @param file
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    deleteFile(subFile);
                }
            }
        }
        return file.delete();
    }

    /**
     * 获取路径里的文件名
     * @param path
     * @param extension 是否包含扩展名（如有）
     * @return
     */
    public static String getFilename(String path, boolean extension) {
        if (null == path)
            return "";
        int slash = path.lastIndexOf('/');
        if (slash < 0) slash = 0;
        int end = path.length();
        if (!extension) {   // 不含扩展名
            int dot = path.lastIndexOf('.');
            if (dot > slash)
                end = dot;
        }
        return path.substring(slash + 1, end);
    }

    public static String getFilename(String path) {
        return getFilename(path, true);
    }

    public static String getFileExtension(String path) {
        if (null == path)
            return "";
        int slash = path.lastIndexOf('/');
        if (slash < 0) slash = 0;
        int dot = path.lastIndexOf('.');
        if (dot > slash) {
            return path.substring(dot);
        }
        return "";
    }

    // check if path string is a subpath of parent
    public static boolean isSubPath(String parent, String path) {
        if (null == parent || null == path)
            return false;
        if (path.length() <= parent.length())
            return false;
        if (path.charAt(parent.length()) != '/')
            return false;
        return path.startsWith(parent);
    }

    public static String getFileNameFromUrl(String url) {
        // 名字不能只用这个
        // 通过 ‘？’ 和 ‘/’ 判断文件名
        String extName = "";
        String filename;
        int index = url.lastIndexOf('?');
        if (index > 1)
        {
            extName = url.substring(url.lastIndexOf('.') + 1, index);
        } else
        {
            extName = url.substring(url.lastIndexOf('.') + 1);
        }
        filename = hashKeyForDisk(url) + "." + extName;
        return filename;
        /*
         * int index = url.lastIndexOf('?'); String filename; if (index > 1) {
         * filename = url.substring(url.lastIndexOf('/') + 1, index); } else {
         * filename = url.substring(url.lastIndexOf('/') + 1); }
         * 
         * if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
         * filename = UUID.randomUUID() + ".apk";// 默认取一个文件名 } return filename;
         */
    }

    /**
     * 一个散列方法,改变一个字符串(如URL)到一个散列适合使用作为一个磁盘文件名。
     */
    private static String hashKeyForDisk(String key) {
        String cacheKey;
        try
        {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes(Charset.forName("UTF-8")));
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e)
        {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
        {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1)
            {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 解压缩zip文件，耗时操作，建议放入异步线程
     * @return how many files the ZIP file includs.
     * */
    public static int unzip(String targetPath, String zipFilePath) {
        int fileNumbers = 0;
        try {
            int BUFFER = 2048;
            String fileName = zipFilePath;
            String filePath = targetPath;
            ZipFile zipFile = new ZipFile(fileName);
            Enumeration<?> emu = zipFile.entries();

            while (emu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) emu.nextElement();
                if(entry.getName().contains("../"))
                    continue;
                if (entry.isDirectory()) {
                    new File(filePath + entry.getName()).mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(filePath + entry.getName());
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);

                int count;
                byte data[] = new byte[BUFFER];
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    bos.write(data, 0, count);
                }
                bos.flush();
                bos.close();
                bis.close();
                fileNumbers++;
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileNumbers;
    }

    /**
     * copy db file from assets folder to databases folder
     */
    public static void copyAssertDatabases(Context context, String name) {

        //if file is not exist, copy it.
        File file = context.getDatabasePath(name);

        File dir = file.getParentFile();
        if(!dir.exists()) {
            dir.mkdir();
        }

        if(!file.exists()) {
            try {
                InputStream ins = context.getAssets().open(name);
                FileOutputStream ous = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;

                while((len = ins.read(buffer)) > 0) {
                    ous.write(buffer, 0, len);
                }

                ous.flush();
                ous.close();
                ins.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    *  copy src file to dst file
    */
    public static boolean copyFile(String srcFile, String dstFile) {
        boolean result = false;

        if(!isFileExist(dstFile)) {
            try {
                InputStream ins = new FileInputStream(srcFile);
                FileOutputStream ous = new FileOutputStream(dstFile);
                byte[] buffer = new byte[1024];
                int len;

                while((len = ins.read(buffer)) > 0) {
                    ous.write(buffer, 0, len);
                }

                ous.flush();
                ous.close();
                ins.close();

                result = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
