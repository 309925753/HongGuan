package com.sk.weichat.audio.record;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

@SuppressWarnings("ALL")
public class AudioFileUtils {

    private static String rootPath = null;
    private static String folder = "audiorecord";
    private final static String AUDIO_PCM_BASEPATH = "/" + folder + "/pcm/";
    private final static String AUDIO_WAV_BASEPATH = "/" + folder + "/wav/";
    private final static String AUDIO_AMR_BASEPATH = "/" + folder + "/amr/";

    private static File resolve(File folder, String fileName) {
        return new File(folder, fileName);
    }

    private static File getFolder() {
        if (!TextUtils.isEmpty(rootPath)) {
            File file = new File(rootPath);
            if (file.isDirectory() && file.canWrite()) {
                return file;
            }
        }
        return Environment.getExternalStorageDirectory();
    }

    public static void setFolder(String folder) {
        AudioFileUtils.folder = folder;
    }

    public static String getPcmFileAbsolutePath(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            throw new NullPointerException("fileName isEmpty");
        }
        String mAudioRawPath = "";
        if (!fileName.endsWith(".pcm")) {
            fileName = fileName + ".pcm";
        }
        File file = resolve(getFolder(), AUDIO_PCM_BASEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        mAudioRawPath = resolve(file, fileName).getAbsolutePath();

        return mAudioRawPath;
    }


    public static String getWavFileAbsolutePath(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("fileName can't be null");
        }

        String mAudioWavPath = "";
        if (!fileName.endsWith(".wav")) {
            fileName = fileName + ".wav";
        }
        File file = resolve(getFolder(), AUDIO_WAV_BASEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        mAudioWavPath = resolve(file, fileName).getAbsolutePath();
        return mAudioWavPath;
    }

    public static String getAmrFileAbsolutePath(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("fileName can't be null");
        }

        String mAudioWavPath = "";
        if (!fileName.endsWith(".amr")) {
            fileName = fileName + ".amr";
        }
        File file = resolve(getFolder(), AUDIO_AMR_BASEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        mAudioWavPath = resolve(file, fileName).getAbsolutePath();
        return mAudioWavPath;
    }

}

