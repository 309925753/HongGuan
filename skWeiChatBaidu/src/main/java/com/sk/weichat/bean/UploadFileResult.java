package com.sk.weichat.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.List;

/**
 * NEED
 */
public class UploadFileResult extends Result {
    private int failure;
    private int success;
    private int total;
    private Data data;

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private List<Sources> audios;
        private List<Sources> videos;
        private List<Sources> images;
        private List<Sources> others;
        private List<Sources> files;

        public List<Sources> getFiles() {
            return files;
        }

        public void setFiles(List<Sources> files) {
            this.files = files;
        }

        public List<Sources> getAudios() {
            return audios;
        }

        public void setAudios(List<Sources> audios) {
            this.audios = audios;
        }

        public List<Sources> getVideos() {
            return videos;
        }

        public void setVideos(List<Sources> videos) {
            this.videos = videos;
        }

        public List<Sources> getImages() {
            return images;
        }

        public void setImages(List<Sources> images) {
            this.images = images;
        }

        public List<Sources> getOthers() {
            return others;
        }

        public void setOthers(List<Sources> others) {
            this.others = others;
        }
    }

    public static class Sources {
        @JSONField(name = "oFileName")
        private String originalFileName;

        @JSONField(name = "oUrl")
        private String originalUrl;

        @JSONField(name = "tUrl")
        private String thumbnailUrl;

        private int status;

        private long length;// 语音视频文件的时长，返回数据后，自己在添加上去的

        private long size;   // 语音视频文件的大小，返回数据后，自己在添加上去的

        public String getOriginalFileName() {
            return originalFileName;
        }

        public void setOriginalFileName(String originalFileName) {
            this.originalFileName = originalFileName;
        }

        public String getOriginalUrl() {
            return originalUrl;
        }

        public void setOriginalUrl(String originalUrl) {
            this.originalUrl = originalUrl;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }

    // 图片上传的Filter
    public static PropertyPreFilter sImagesFilter = new PropertyPreFilter() {
        @Override
        public boolean apply(JSONSerializer arg0, Object arg1, String arg2) {
            if (arg2.equals("oUrl") || arg2.equals("tUrl")) {
                return true;
            }
            return false;
        }
    };

    // 语音视频上传的Filter
    public static PropertyPreFilter sAudioVideosFilter = new PropertyPreFilter() {
        @Override
        public boolean apply(JSONSerializer arg0, Object arg1, String arg2) {
            if (arg2.equals("oUrl") || arg2.equals("length") || arg2.equals("size")) {
                return true;
            }
            return false;
        }
    };
}
