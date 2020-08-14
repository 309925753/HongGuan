package com.sk.im.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.audio.RecordStateListener;
import com.sk.weichat.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.sk.im.audio
 * @作者:王阳
 * @创建时间: 2015年10月15日 下午4:54:45
 * @描述: 类功能描述：</b> MP3实时录制功能,可暂停,注意因踩用Native开发,不能混淆
 * 配置,编译so查看:http://blog.csdn.net/cboy017/article/details/8455629
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容:
 */
public class MP3Recorder {
    private static final int MSG_REC_STARTEING = 0;// 开始中
    private static final int MSG_REC_STARTED = 1;// 开始录音
    private static final int MSG_REC_STOPPED = 2;// 结束录音
    private static final int MSG_REC_PAUSE = 3;// 暂停录音
    private static final int MSG_REC_RESTORE = 4;// 继续录音
    private static final int MSG_REC_ERROR = 5;// 继续录音

    private static final int ERROR_GET_MIN_BUFFERSIZE = -1;// 缓冲区出错,采样率手机不支持
    private static final int ERROR_CREATE_FILE = -2;// 创建文件的时候出错
    private static final int ERROR_REC_START = -3;// 初始化录音的时候出错
    private static final int ERROR_AUDIO_RECORD = -4;// 录音
    private static final int ERROR_AUDIO_ENCODE = -5;// 编码时挂了
    private static final int ERROR_WRITE_FILE = -6;// 写文件时挂了
    private static final int ERROR_CLOSE_FILE = -7;// 没法关闭文件流

    private static final int DEFAULR_SAMPLE_RATE = 8000;
    private static final int GUARANTEED_SAMPLE_RATE = 44100;

    private static MP3Recorder instance;
    private Handler mHandler;
    private ReentrantLock mReentrantLock;
    private int mMaxValue;
    private RecordStateListener mListener;
    private Timer mTimer;
    private long mStartTime;

    private boolean isRecording = false;
    private boolean isPause = false;
    private boolean isCancel = false;

    private MP3Recorder() {
        mHandler = new RecordStatusHandler(Looper.getMainLooper());
        mReentrantLock = new ReentrantLock();
        // mMaxValue//从SP中读取最大音量
    }

    public static MP3Recorder getInstance() {
        if (instance == null) {
            synchronized (MP3Recorder.class) {
                if (instance == null) {
                    instance = new MP3Recorder();
                }
            }
        }
        return instance;
    }

    public void setRecordStateListener(RecordStateListener listener) {
        this.mListener = listener;
    }

    private void fileDelete(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (file.exists()) {
                file.deleteOnExit();
            }
        }
    }

    private class RecordStatusHandler extends Handler {
        public RecordStatusHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REC_STARTEING:
                    if (mListener != null) {
                        mListener.onRecordStarting();
                    }
                    break;
                case MSG_REC_STARTED:// 开始录音
                    if (mListener != null) {
                        mListener.onRecordStart();
                    }
                    break;
                case MSG_REC_STOPPED:// 结束录音
                    String filePath = (String) msg.obj;
                    if (isCancel) {
                        if (mListener != null) {
                            mListener.onRecordCancel();
                        }
                        fileDelete(filePath);
                    } else {
                        long delay = System.currentTimeMillis() - mStartTime;
                        if (delay <= 500) {
                            if (mListener != null) {
                                mListener.onRecordTooShoot();
                            }
                            fileDelete(filePath);
                        } else {
                            if (mListener != null) {
                                mListener.onRecordFinish(filePath);
                            }
                        }
                    }
                    stopTimer();
                    break;
                case MSG_REC_PAUSE:// 暂停录音
                    break;
                case MSG_REC_RESTORE:// 继续录音
                    break;
                case MSG_REC_ERROR:// 录音出错
                    if (mListener != null) {
                        mListener.onRecordError();
                    }
                    stopTimer();
                    break;
            }
        }
    }

    private void sendErrorMessage(int errorCode) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_REC_ERROR, errorCode, 0));
    }

    /**
     * 开片
     */
    public void start() {
        if (isRecording) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                Log.d("roamer", "开始录音");
                mReentrantLock.lock();

                isRecording = true; // 录音状态
                isPause = false; // 录音状态
                isCancel = false;

                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

                mHandler.sendEmptyMessage(MSG_REC_STARTEING);

                // 根据定义好的几个配置，来获取合适的缓冲大小
                int sampleRate = DEFAULR_SAMPLE_RATE;
                int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if (minBufferSize < 0) {
                    sampleRate = GUARANTEED_SAMPLE_RATE;
                    minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                }

                if (minBufferSize < 0) {
                    sendErrorMessage(ERROR_GET_MIN_BUFFERSIZE);
                    mReentrantLock.unlock();
                    return;
                }

                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);

                // 5秒的缓冲
                short[] buffer = new short[sampleRate * (16 / 8) * 1 * 5];
                byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

                final String filePath = FileUtil.getRandomAudioFilePath();
                if (filePath == null) {
                    sendErrorMessage(ERROR_CREATE_FILE);
                    mReentrantLock.unlock();
                    return;
                }
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(new File(filePath));
                } catch (FileNotFoundException e) {
                    sendErrorMessage(ERROR_CREATE_FILE);
                    mReentrantLock.unlock();
                    return;
                }

                MP3Recorder.init(sampleRate, 1, sampleRate, 16);

                try {
                    try {
                        audioRecord.startRecording(); // 开启录音获取音频数据
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        sendErrorMessage(ERROR_REC_START);
                        return;
                    }

                    try {
                        // 开始录音
                        mHandler.sendEmptyMessage(MSG_REC_STARTED);
                        mStartTime = System.currentTimeMillis();
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (mListener != null) {
                                    int seconds = (int) ((System.currentTimeMillis() - mStartTime) / 1000);
                                    notifyRecordTimeChange(seconds);
                                }
                            }
                        }, 0, 1000);

                        int readSize = 0;
                        boolean pause = false;
                        while (isRecording) {
                            /*--暂停--*/
                            if (isPause) {
                                if (!pause) {
                                    mHandler.sendEmptyMessage(MSG_REC_PAUSE);
                                    pause = true;
                                }
                                continue;
                            }
                            if (pause) {
                                mHandler.sendEmptyMessage(MSG_REC_RESTORE);
                                pause = false;
                            }
                            /*--End--*/
                            /*--实时录音写数据--*/
                            readSize = audioRecord.read(buffer, 0, minBufferSize);

                            // ////////////音量获取///////////////////////////////

                            int v = 0;
                            // 将 buffer 内容取出，进行平方和运算
                            for (int i = 0; i < readSize; i++) {
                                // 这里没有做运算的优化，为了更加清晰的展示代码
                                v += buffer[i] * buffer[i];
                            }

                            // double dB = 10*Math.log10(v/(double)readSize);
                            int value = (int) (Math.abs((int) (v / (float) readSize) / 10000) >> 1);

                            // 音量级别 0- 15
                            int level = 0;
                            if (value <= 0) {
                                level = 0;
                            } else if (value >= mMaxValue) {
                                level = 15;
                            } else {
                                level = (int) (value / (float) mMaxValue * 15);
                            }

                            if (mListener != null) {
                                notifyRecordVolumeChange(level);
                            }

                            if (value > mMaxValue) {
                                mMaxValue = value;
                            }
                            // ///////////////////////////////////////////
                            if (readSize < 0) {
                                sendErrorMessage(ERROR_AUDIO_RECORD);
                                break;
                            } else if (readSize == 0) {
                                ;
                            } else {
                                int encResult = MP3Recorder.encode(buffer, buffer, readSize, mp3buffer);
                                if (encResult < 0) {
                                    sendErrorMessage(ERROR_AUDIO_ENCODE);
                                    break;
                                }
                                if (encResult != 0) {
                                    try {
                                        output.write(mp3buffer, 0, encResult);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        sendErrorMessage(ERROR_WRITE_FILE);
                                        break;
                                    }
                                }
                            }
                            /*--End--*/
                        }
                        /*--录音完--*/
                        int flushResult = MP3Recorder.flush(mp3buffer);
                        if (flushResult < 0) {
                            sendErrorMessage(ERROR_AUDIO_ENCODE);
                        }
                        if (flushResult != 0) {
                            try {
                                output.write(mp3buffer, 0, flushResult);
                            } catch (IOException e) {
                                e.printStackTrace();
                                sendErrorMessage(ERROR_WRITE_FILE);
                            }
                        }
                        try {
                            output.close();
                        } catch (IOException e) {
                            sendErrorMessage(ERROR_CLOSE_FILE);
                            e.printStackTrace();
                        }
                        /*--End--*/
                    } finally {
                        audioRecord.stop();
                        audioRecord.release();
                    }
                } finally {
                    MP3Recorder.close();
                    isRecording = false;
                    mReentrantLock.unlock();
                }
                Message message = mHandler.obtainMessage(MSG_REC_STOPPED);
                message.obj = filePath;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
    }

    public void stop() {
        isRecording = false;
    }

    // 暂不支持
    private void pause() {
        isPause = true;
    }

    // 暂不支持
    private void restore() {
        isPause = false;
    }

    public void cancel() {
        isRecording = false;
        isCancel = true;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPause() {
        if (!isRecording) {
            return false;
        }
        return isPause;
    }

    private void notifyRecordTimeChange(final int seconds) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onRecordTimeChange(seconds);
            }
        });
    }

    private void notifyRecordVolumeChange(final int v) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onRecordVolumeChange(v);
            }
        });
    }

    /*--以下为Native部分--*/
    static {
        System.loadLibrary("mp3lame");
    }

    /**
     * 初始化录制参数
     */
    public static void init(int inSamplerate, int outChannel, int outSamplerate, int outBitrate) {
        init(inSamplerate, outChannel, outSamplerate, outBitrate, 7);
    }

    /**
     * 初始化录制参数 quality:0=很好很慢 9=很差很快
     */
    public native static void init(int inSamplerate, int outChannel, int outSamplerate, int outBitrate, int quality);

    /**
     * 音频数据编码(PCM左进,PCM右进,MP3输出)
     */
    public native static int encode(short[] buffer_l, short[] buffer_r, int samples, byte[] mp3buf);

    /**
     * flush
     */
    public native static int flush(byte[] mp3buf);

    /**
     * 结束编码
     */
    public native static void close();
}
