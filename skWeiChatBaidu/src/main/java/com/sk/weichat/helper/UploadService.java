package com.sk.weichat.helper;

import android.text.TextUtils;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class UploadService {

    /* 表单的一些固定字段 */
    private static final String END = "\r\n";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String BOUNDARY = "------WebKitFormBoundarywL3jvc3wm30NCvQt"; // 数据分隔线

    /**
     * @param 只发送普通数据   ,调用此方法
     * @param uploadUrl 对应的Php 页面
     * @param params    需要发送的相关数据 包括调用的方法
     * @param filePath  图片或文件手机上的地址 如:sdcard/photo/123.jpg
     * @param pic       图片名称
     * @return Json
     */
    public String uploadFile(String uploadUrl, Map<String, String> params, List<String> filePathList) {
        if (filePathList == null || filePathList.size() <= 0) {
            return null;
        }
        String result = "";// 返回信息

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        InputStream is = null;
        try {
            System.setProperty("http.keepAlive", "false");
            URL url = new URL(uploadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);   // 允许输入
            // conn.setDoOutput(true);//允许输出
            conn.setUseCaches(false); //不使用Cache
            conn.setConnectTimeout(50000);// 50秒钟连接超时
            conn.setReadTimeout(50000);   // 50秒钟读数据超时
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);

            StringBuilder sb = new StringBuilder();
            // 上传的表单参数部分，格式请参考文章
            for (Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append(END);
                sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                sb.append(entry.getValue());
                sb.append(END);
            }

            dos = new DataOutputStream(conn.getOutputStream());
            dos.write(sb.toString().getBytes());

            for (int i = 0; i < filePathList.size(); i++) {
                String filePath = filePathList.get(i);
                if (TextUtils.isEmpty(filePath)) {
                    continue;
                }
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);// 获得图片或文件名称
                String fileType = getFileType(fileName);
                StringBuilder sbsb = new StringBuilder();
                sbsb.append("--");
                sbsb.append(BOUNDARY);
                sbsb.append(END);
                sbsb.append("Content-Disposition: form-data; name=\"" + "file" + i + "\"; filename=\"" + fileName + "\"" + "\r\n" + "Content-Type: "
                        + fileType + "\r\n\r\n");
                dos.write(sbsb.toString().getBytes());
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024]; // 8k
                int count = 0;
                while ((count = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, count);
                }
                dos.writeBytes(END);
                fis.close();

                dos.flush();
            }
            dos.writeBytes("--" + BOUNDARY + "--" + END);

            // 获取服务器响应
            is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = isr.read()) != -1) {
                b.append((char) ch);
            }
            result = b.toString();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                if (dos != null) {
                    dos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String uploadSingleBitmap(String uploadUrl, Map<String, String> params, byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        String result = "";// 返回信息

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        InputStream is = null;
        try {
            System.setProperty("http.keepAlive", "false");
            URL url = new URL(uploadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);   // 允许输入
            // conn.setDoOutput(true);//允许输出
            conn.setUseCaches(false);// 不使用Cache
            conn.setConnectTimeout(50000);// 20秒钟连接超时
            conn.setReadTimeout(50000);   // 30秒钟读数据超时
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);

            StringBuilder sb = new StringBuilder();
            // 上传的表单参数部分，格式请参考文章
            for (Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append(END);
                sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                sb.append(entry.getValue());
                sb.append(END);
            }

            dos = new DataOutputStream(conn.getOutputStream());
            dos.write(sb.toString().getBytes());

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";
            String fileType = "image/jpg";
            StringBuilder sbsb = new StringBuilder();
            sbsb.append("--");
            sbsb.append(BOUNDARY);
            sbsb.append(END);
            sbsb.append("Content-Disposition: form-data; name=\"" + "file1" + "\"; filename=\"" + fileName + "\"" + "\r\n" + "Content-Type: "
                    + fileType + "\r\n\r\n");
            dos.write(sbsb.toString().getBytes());
            dos.write(data);
            dos.writeBytes(END);

            dos.writeBytes("--" + BOUNDARY + "--" + END);
            // 获取服务器响应
            is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = isr.read()) != -1) {
                b.append((char) ch);
            }
            result = b.toString();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                if (dos != null) {
                    dos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static final String getFileType(String fileName) {
        if (fileName.endsWith(".png") || fileName.endsWith(".PNG")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".JPEG") || fileName.endsWith(".JPG")) {
            return "image/jpg";
        } else if (fileName.endsWith(".bmp") || fileName.endsWith(".BMP")) {
            return "image/bmp";
        } else {
            return "application/octet-stream";
        }
    }
}
