package fm.jiecao.jcvideoplayer_lib;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by dty on 2015/11/8.
 */
public class downloadTask extends Thread {
    String urlStr, threadNo, fileName;
    private int blockSize, downloadSizeMore;
    private int threadNum = 5;
    private int mDownloadedSize = 0;
    private int fileSize = 0;

    public downloadTask(String urlStr, int threadNum, String fileName) {
        this.urlStr = urlStr;
        this.threadNum = threadNum;
        this.fileName = fileName;
    }

    /**
     * @修改人：TanX
     * @时间： 2016/4/19 17:22
     * @参数：
     * @说明： 这里会出现资源站没有该资源，而下载却一直在下载的情况，所以加个时间做超时
     * 如果超过10秒下载的量依然没变，则超时
     **/
    @Override
    public void run() {
        FileDownloadThread[] fds = new FileDownloadThread[threadNum];
        int overTime = 10000;//10秒超时
        int downloadSize = 0;//初始下载为0
        long downloadTime = System.currentTimeMillis();//起始下载时间
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            //获取下载文件的总大小
            fileSize = conn.getContentLength();
            //计算每个线程要下载的数据量
            blockSize = fileSize / threadNum;
            // 解决整除后百分比计算误差
            downloadSizeMore = (fileSize % threadNum);
            File file = new File(fileName);
            for (int i = 0; i < threadNum; i++) {
                //启动线程，分别下载自己需要下载的部分
                FileDownloadThread fdt = new FileDownloadThread(url, file,
                        i * blockSize, (i + 1) * blockSize - 1);
                fdt.setName("Thread" + i);
                fdt.start();
                fds[i] = fdt;
            }
            boolean finished = false;
            while (!finished) {
                if (System.currentTimeMillis() - downloadTime >= overTime) {
                    //到了超时的时限
                    if (mDownloadedSize <= downloadSize) {
                        //过了10秒，但是并没下载到东西，超时
                        throw new Exception();
                    }
                    //下载到了东西
                    downloadTime = System.currentTimeMillis();//重置时间
                    downloadSize = mDownloadedSize;//设置为当前的下载量
                }
                // 先把整除的余数搞定
                mDownloadedSize = downloadSizeMore;
                finished = true;
                for (int i = 0; i < fds.length; i++) {
                    mDownloadedSize += fds[i].getDownloadSize();
                    if (!fds[i].isFinished()) {
                        finished = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}