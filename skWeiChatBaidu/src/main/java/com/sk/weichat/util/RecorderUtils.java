package com.sk.weichat.util;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderUtils {
    public static final int HIDE_DIALOG = 1;// 隐藏对话框，必须小于100， 因为外面有大于100就隐藏对话框的设定，
    public static final int MUSIC_SUCCESS = 1001; // 合并音乐
    public static final int MUSIC_FAILURE = 1002;
    public static final int COMPRESS_SUCCESS = 1003;// 压缩
    public static final int COMPRESS_FAILURE = 1004;
    public static final int SPEED_SUCCESS = 1005; // 变速
    public static final int SPEED_FAILURE = 1006;
    public static final int ACTIVATE_BTN = 1007; // 激活按钮
    public static final int MERGE_FAILURE = 1008; // 视屏合并失败
    public static final int VOLUME_SUCCESS = 1009; // 音量
    public static final int VOLUME_FAILURE = 1010;
    public static final int THUMB_SUCCESS = 1011;
    public static final int THUMB_FAILURE = 1012;

    public static final int COVER_SUCCESS = 1013; // 获取封面成功
    public static final int COVER_FAILURE = 1014;// 获取封面失败
    public static final int COVER_AUTOOPEN = 1015;// 自动打开
    public static final int COVER_LOADING = 1016;// 获取封面中

    public static final int WATERMARK_SUCCESS = 1017;// 添加水印成功
    public static final int WATERMARK_FAILURE = 1018;// 添加水印失败

    public static boolean delVideoFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static String getRecorderPath() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return null; // 内存卡不存在
        }

        File sdDir = Environment.getExternalStorageDirectory();
        File eis = new File(sdDir.toString() + "/RecordVideo/");
        if (!eis.exists()) {
            eis.mkdir();
        }
        return sdDir.toString() + "/RecordVideo/";
    }

    public static String getThumb(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        String fileName = name.substring(name.lastIndexOf("/") + 1);
        String temp[] = fileName.split("\\.");
        String out = getRecorderPath() + temp[0];
        return out;
    }

    /**
     * 根据视频名称获取缩略图名称
     *
     * @param name
     * @return
     */
    public static String getThumbPath(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        String fileName = name.substring(name.lastIndexOf("/") + 1);
        String temp[] = fileName.split("\\.");
        String out = getRecorderPath() + temp[0] + "_thumb%03d.jpg";
        return out;
    }

    public static String getThumbPath(String name, int index) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        String fileName = name.substring(name.lastIndexOf("/") + 1);
        String temp[] = fileName.split("\\.");
        // String out = getRecorderPath() + temp[0] + "_thumb%03d.jpg";
        String out = getRecorderPath() + temp[0] + "_thumb00" + index + ".jpg";
        return out;
    }

    public static String getVideoFileByTime() {
        String filePath = getRecorderPath() + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()) + ".mp4";
        return filePath;
    }

    public static String getImagePathByTime() {
        String filePath = getRecorderPath() + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        return filePath;
    }

    // 视频变速编辑文件名
    public static String changeFileNameBySpeed(String inPath, float speed) {
        String str = "";
        if (speed == 0.5f) {
            str = "zm";
        } else if (speed == 0.75f) {
            str = "m";
        } else if (speed == 1.25f) {
            str = "k";
        } else {
            str = "zk";
        }
        return changeFileName(inPath, str);
    }

    // 拼接文件名 如可以 xuan.mp3 -> xuan2.mp3
    public static String changeFileName(String name, String append) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        String fileName = name.substring(name.lastIndexOf("/") + 1);
        String temp[] = fileName.split("\\.");
        String out = getRecorderPath() + temp[0] + append + "." + temp[1];
        return out;
    }

    /**
     * ffmpeg 修改视频bg 音量
     *
     * @param inPaht
     * @param outPath
     * @param volume
     * @return
     */
    public static String[] ffmpegVolumeCmd(String inPaht, String outPath, int volume) {
        String[] commands = new String[]{
                "ffmpeg", "-i", inPaht, "-vcodec", "copy", "-af", "volume=" + volume + "dB", "-y", outPath};
        return commands;
    }

    /** 参数说明：
     -threads： 执行线程数，传入1 单线程压缩
     -i：input路径，传入视频文件的路径
     -c:v： 编码格式，一般都是指定libx264
     -crf：编码质量，取值范围是0-51，默认值为23，数字越小输出视频的质量越高。这里的30是我们经过测试得到的经验值
     -preset：转码速度，ultrafast，superfast，veryfast，faster，fast，medium，slow，slower，veryslow和placebo。
     ultrafast 编码速度最快，但压缩率低，生成的文件更大，placebo则正好相反。x264所取的默认值为medium。需要说明的是，preset主要是影响编码的速度，
     并不会很大的影响编码出来的结果的质量。
     -acodec：音频编码，!!! 使用libmp3lame会导致ios解码失败听不到声音 !!!, 改用aac,
     arg.thumbVideoPath：最后传入的是视频压缩后保存的路径
     -y：输出时覆盖输出目录已存在的同名文件（如果不加此参数，就不会覆盖）
     */
    /**
     * superfast  44s  9.6m->1.8m
     * ultrafast  19s  9.9m->640k
     */
    public static String[] ffmpegComprerssCmd(String inPaht, String outPath) {
        String[] commands = new String[]{
                "ffmpeg", "-i", inPaht, "-c:v", "libx264", "-crf", "30",
                "-preset", "superfast",
                "-y", "-acodec", "aac", outPath};
        return commands;
    }

    /**
     * 使用ffmpeg命令行给视频添加水印
     *
     * @param srcFile    源文件
     * @param waterMark  水印文件路径
     * @param targetFile 目标文件
     * @return 添加水印后的文件
     */
    public static String[] ffmpegAddWaterMark(String srcFile, String waterMark, String targetFile) {
        String waterMarkCmd = "ffmpeg -i %s -i %s -filter_complex overlay=0:0 %s";
        waterMarkCmd = String.format(waterMarkCmd, srcFile, waterMark, targetFile);
        return waterMarkCmd.split(" ");//以空格分割为字符串数组
    }

    /**
     * 将整个视频 变成 count 张图片
     *
     * @param inPaht   视频地址可以是网络和本地
     * @param outPath  输出图片的位置 格式需要是 xxx%03d形式
     * @param duration 视频长度 秒
     * @param count    张数
     * @return
     */
    public static String ffmpegFindThumbMultipleCmd(String inPaht, String outPath, float duration, int count) {
        float rate = count / duration;
        String str = "-y -i " + inPaht + " -r " + rate + " -q:v 2 -f image2 -preset superfast " + outPath;
        return str;
    }

    /**
     * 在视频中 获取第d秒的多张帧图片
     *
     * @param inPaht   视频地址可以是网络和本地
     * @param outPath  输出图片的位置 格式需要是 xxx%03d形式
     * @param duration 在第几秒获取，秒数越大，需要的时间也越大 单位是秒 必须要小于视频长度，否则会报错
     * @return
     */
    public static String ffmpegFindThumbCmd(String inPaht, String outPath, float duration, int count) {
        String str = "-i " + inPaht + " -y -f image2 -ss " + duration + " -vframes " + count + " " + outPath;
        return str;
    }

    public static String saveThumbByStrote(String inPaht) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(inPaht);
        Bitmap bitmap = retriever.getFrameAtTime(50, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

        if (bitmap != null) {
            String fileName = inPaht.substring(inPaht.lastIndexOf("/") + 1);
            String temp[] = fileName.split("\\.");
            String out = getRecorderPath() + temp[0] + "_thumb.jpg";
            if (BitmapUtil.saveBitmapToSDCard(bitmap, out)) {
                return out;
            }
        }
        return "";
    }
}
