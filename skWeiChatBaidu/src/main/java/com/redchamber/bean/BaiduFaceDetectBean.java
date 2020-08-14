package com.redchamber.bean;

import java.util.List;

public class BaiduFaceDetectBean {

    public int error_code;
    public String error_msg;
    public Result result;

    public static class Result {

        public int face_num;
        public List<Face> face_list;

        public static class Face {

            public Gender gender;

            public static class Gender {
                public String type;
                public double probability;
            }

        }
    }

}
