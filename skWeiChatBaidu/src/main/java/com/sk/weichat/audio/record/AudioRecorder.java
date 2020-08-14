package com.sk.weichat.audio.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("ALL")
public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    protected String pcmFileName;
    private int audioInput = MediaRecorder.AudioSource.MIC;
    private int audioSampleRate = 8000;
    private int audioChannel = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncode = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = 0;
    private AudioRecord audioRecord;
    private Status status = Status.STATUS_NO_READY;
    private Timer timer;

    private TimerTask timerTask;
    private int currentPosition = 0;
    private CallBack callBack;
    private int lastVolumn = 0;
    private AudioEncoder encoder;

    public AudioRecorder(CallBack callBack, AudioEncoder encoder) {
        pcmFileName = AudioFileUtils.getPcmFileAbsolutePath(System.currentTimeMillis() + "");
        this.encoder = encoder;

//        encoder.init(audioSampleRate, 16, 1);
        File file = new File(pcmFileName);
        if (file.exists()) {
            file.delete();
        }
        status = Status.STATUS_READY;
        this.callBack = callBack;
    }


    public void setAudioInput(int audioInput) {
        this.audioInput = audioInput;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public void setAudioChannel(int audioChannel) {
        this.audioChannel = audioChannel;
    }


    public void setEncoder(AudioEncoder encoder) {
        this.encoder = encoder;
    }

    private void startTimer() {
        if (timer == null)
            timer = new Timer();
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                currentPosition++;
                if (callBack != null && status == Status.STATUS_START) {
                    callBack.recordProgress(currentPosition);
                    callBack.volumn(lastVolumn);
                }

            }
        };
        timer.schedule(timerTask, 0, 100);
        Log.i(TAG, "startTimer: " + timer + ", " + timerTask);
    }

    private void stopTimer() {
        Log.i(TAG, "stopTimer: " + timer + ", " + timerTask);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void startRecord() {

        bufferSizeInBytes = AudioRecord.getMinBufferSize(audioSampleRate,
                audioChannel, audioEncode);
        audioRecord = new AudioRecord(audioInput, audioSampleRate, audioChannel, audioEncode, bufferSizeInBytes);
        if (status == Status.STATUS_NO_READY) {
            throw new IllegalStateException("not init");
        }
        if (status == Status.STATUS_START) {
            throw new IllegalStateException("is recording ");
        }
        Log.d("AudioRecorder", "===startRecord===" + audioRecord.getState());
        audioRecord.startRecording();

        // status状态要马上更改，否则对于瞬间开启关闭的情况无法正确判断状态，导致没有关闭，
        status = Status.STATUS_START;
        new Thread(new Runnable() {
            @Override
            public void run() {
                recordToFile();
            }
        }).start();
        startTimer();
    }

    public void stop() {
        if (status != Status.STATUS_START) {
            throw new IllegalStateException("not recording");
        } else {
            stopRecorder();
            makeDestFile();
            status = Status.STATUS_READY;
        }
    }

    private void makeDestFile() {
        if (encoder == null)
            return;
        encoder.init(audioSampleRate, audioSampleRate * 16 * audioRecord.getChannelCount(), audioRecord.getChannelCount());
        encoder.encode(pcmFileName);
        releaseRecorder();
    }

    public void release() {
        Log.d("AudioRecorder", "===release===");
        Status oldStatus = status;
        stopRecorder();
        releaseRecorder();
        status = Status.STATUS_READY;
        if (oldStatus == Status.STATUS_START) {
            // 正在录音过程调用释放release方法而不是stop方法的话，表示这次录音被放弃了，删除录音文件，
            // 其他情况，可能只是例行回收资源，并不需要把正常录音完成的文件删除，
            clearFiles();
        }
    }

    private void releaseRecorder() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void stopRecorder() {
        stopTimer();
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * todo release 方法会将该路径下所有音频文件都删掉，可以考虑指定删除或者release不删
     */
    public void clearFiles() {
        if (encoder != null) {
            File file = new File(encoder.getDestFile());
            if (file.exists())
                file.delete();
        }
        File pcmfile = new File(pcmFileName);
        if (pcmfile.exists())
            pcmfile.delete();
    }

    private void recordToFile() {
        byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            fos = new FileOutputStream(pcmFileName, true);
        } catch (FileNotFoundException e) {
            Log.e("AudioRecorder", e.getMessage());
        }
        while (status == Status.STATUS_START && audioRecord != null) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
                try {
                    if (readsize > 0 && readsize <= audiodata.length) {
                        ShortBuffer shorts = ByteBuffer.wrap(audiodata, 0, readsize)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .asShortBuffer();

                        int sum = 0;
                        for (int i = 0; i < shorts.limit(); i++) {
                            // abs(sin(x)), 3* abs(sin(x)),(3* abs(sin(x))-0.75)/9+0.75
                            final int rate = 3; // 倍数，
                            final short s = shorts.get(i);
                            int as = Math.abs(s);
                            double ds = 1.0 * as / 0x8000;
                            double qs = ds * rate;
                            if (qs > 0.75) {
                                // 超过0.75的部分收缩到0.75-1的范围，
                                qs = (qs - 0.75) / ((rate - 1) * 4 + 1) + 0.75;
                            }
                            int an = (int) (qs * 0x8000);
                            int n;
                            if (s >= 0) {
                                n = an;
                            } else {
                                n = -an;
                            }
                            shorts.put((short) n);
                            sum += an;
                        }

                        if (readsize > 0) {
                            int raw = sum / shorts.limit();
                            lastVolumn = raw;
                            Log.i(TAG, "writeDataTOFile: volumn -- " + raw + " / lastvolumn -- " + lastVolumn);
                        }

                        fos.write(audiodata, 0, readsize);
                    }
                } catch (IOException e) {
                    Log.e("AudioRecorder", e.getMessage());
                }
            }
        }
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            Log.e("AudioRecorder", e.getMessage());
        }
    }

    public int getCurrentPosition() {
        return currentPosition;
    }


    public Status getStatus() {
        return status;
    }


    public String getVoiceFilePath() {
        return encoder == null ? pcmFileName : encoder.getDestFile();
    }

    public enum Status {
        STATUS_NO_READY,
        STATUS_READY,
        STATUS_START,
        STATUS_PAUSE,
        STATUS_STOP
    }

    public interface CallBack {
        public void recordProgress(int progress);

        public void volumn(int volumn);
    }

}