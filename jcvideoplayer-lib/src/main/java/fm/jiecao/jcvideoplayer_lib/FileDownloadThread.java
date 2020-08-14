package fm.jiecao.jcvideoplayer_lib;

/**
 * Created by dty on 2015/11/8.
 * 单个下载线程
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloadThread extends Thread {
    private static final int BUFFER_SIZE = 1024;
    private URL url;
    private File file;
    private int startPosition;
    private int endPosition;
    private int curPosition;
    //用于标识当前线程是否下载完成
    private boolean finished = false;
    private int downloadSize = 0;

    public FileDownloadThread(URL url, File file, int startPosition, int endPosition) {
        this.url = url;
        this.file = file;
        this.startPosition = startPosition;
        this.curPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public void run() {
        BufferedInputStream bis = null;
        RandomAccessFile fos = null;
        byte[] buf = new byte[BUFFER_SIZE];
        URLConnection con = null;
        try {
            con = url.openConnection();
            con.setAllowUserInteraction(true);
            //设置当前线程下载的起点，终点
            con.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
            //使用java中的RandomAccessFile 对文件进行随机读写操作
            fos = new RandomAccessFile(file, "rw");
            //设置开始写文件的位置
            fos.seek(startPosition);
            bis = new BufferedInputStream(con.getInputStream());
            //开始循环以流的形式读写文件
            while (curPosition < endPosition) {
                int len = bis.read(buf, 0, BUFFER_SIZE);
                if (len == -1) {
                    break;
                }
                fos.write(buf, 0, len);
                curPosition = curPosition + len;
                if (curPosition > endPosition) {
                    downloadSize += len - (curPosition - endPosition) + 1;
                } else {
                    downloadSize += len;
                }
            }
            //下载完成设为true
            this.finished = true;
            bis.close();
            fos.close();
        } catch (IOException e) {
            Log.d(getName() + " Error:", e.getMessage());
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public int getDownloadSize() {
        return downloadSize;
    }
}