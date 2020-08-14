package com.sk.weichat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

public class GetFileSizeUtil {

    /**
     * 获取某个文件或者文件夹的大小
     *
     * @param file
     * @return
     */
    public static long getFileSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        long size = 0;
        if (file.isFile()) {// 是个文件
            size = calculateSize(file);
        } else {
            File flist[] = file.listFiles();
            if (flist == null) {
                return size;
            }

            for (File subFile : flist) {
                size += getFileSize(subFile);
            }
        }
        return size;
    }

    /**
     * 计算一个文件的大小
     *
     * @param file
     * @return
     */
    private static final long calculateSize(File file) {
        long s = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            s = fis.available();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return s;
    }

    /**
     * 转换文件大小单位(b/kb/mb/gb)
     *
     * @param fileS
     * @return
     */
    public static String formatFileSize(long fileS) {// 转换文件大小
        if (fileS <= 0) {
            return "0.00K";
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
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

    /**
     * 获取某个文件夹下文件的数量
     *
     * @param file
     * @return
     */
    public static int getFolderSubFilesNumber(File file) {
        if (file == null || !file.exists() || !file.isDirectory()) {
            return 0;
        }
        int number = 0;
        File flist[] = file.listFiles();

        if (flist == null) {
            return number;
        }
        for (File subFile : flist) {
            if (subFile.isFile()) {
                number++;
            } else {
                number += getFolderSubFilesNumber(subFile);
            }
        }
        return number;
    }
}
