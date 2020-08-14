package com.sk.weichat.audio.record;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by michael_xu on 2017/8/1.
 */

@SuppressWarnings("ALL")
public class AmrEncoder extends AudioEncoder {

    private static byte[] header = new byte[]{'#', '!', 'A', 'M', 'R', '\n'};

    @Override
    public void init(int SAMPLE_RATE, int BIT_RATE, int CHANNEL_COUNT) {
        super.init(SAMPLE_RATE, BIT_RATE, CHANNEL_COUNT);
        destinationFile = AudioFileUtils.getAmrFileAbsolutePath(System.currentTimeMillis() + "");
    }

    @Override
    public void encode(String sourceFile) {
        try {
            MediaCodec encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AMR_NB);
            MediaFormat format = new MediaFormat();
            format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AMR_NB);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL_COUNT);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);


            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            File pcmFile = new File(sourceFile);
            FileInputStream fis = new FileInputStream(pcmFile);
            File armFIle = new File(destinationFile);
            FileOutputStream fos = new FileOutputStream(armFIle);
            fos.write(header);
            encoder.start();

            ByteBuffer[] codecInputBuffers = encoder.getInputBuffers();
            ByteBuffer[] codecOutputBuffers = encoder.getOutputBuffers();
            byte[] tempBuffer = new byte[88200];
            boolean hasMoreData = true;
            MediaCodec.BufferInfo outBuffInfo = new MediaCodec.BufferInfo();
            double presentationTimeUs = 0;
            int totalBytesRead = 0;
            do {
                int inputBufIndex = 0;
                while (inputBufIndex != -1 && hasMoreData) {
                    inputBufIndex = encoder.dequeueInputBuffer(0);

                    if (inputBufIndex >= 0) {
                        ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                        dstBuf.clear();


                        int bytesRead = fis.read(tempBuffer, 0, dstBuf.limit());
                        if (bytesRead == -1) { // -1 implies EOS
                            hasMoreData = false;
                            encoder.queueInputBuffer(inputBufIndex, 0, 0, (long) presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            totalBytesRead += bytesRead;
                            dstBuf.put(tempBuffer, 0, bytesRead);
                            encoder.queueInputBuffer(inputBufIndex, 0, bytesRead, (long) presentationTimeUs, 0);
                            presentationTimeUs = 1000000l * (totalBytesRead / 2) / SAMPLE_RATE;
                        }
                    }
                }
                // Drain audio
                int outputBufIndex = 0;
                while (outputBufIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    outputBufIndex = encoder.dequeueOutputBuffer(outBuffInfo, 0);
                    if (outputBufIndex >= 0) {
                        ByteBuffer encodedData = codecOutputBuffers[outputBufIndex];
                        encodedData.position(outBuffInfo.offset);
                        encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
                        byte[] outData = new byte[outBuffInfo.size];
                        encodedData.get(outData, 0, outBuffInfo.size);
                        fos.write(outData, 0, outBuffInfo.size);
                        encoder.releaseOutputBuffer(outputBufIndex, false);
                    }
                }
            } while (outBuffInfo.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM);


            if (fis != null) {
                fis.close();
                fis = null;
            }
            if (fos != null) {
                fos.flush();
                fos.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
