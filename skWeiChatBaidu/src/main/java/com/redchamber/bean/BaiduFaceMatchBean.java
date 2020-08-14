package com.redchamber.bean;

public class BaiduFaceMatchBean {
    //cached = 0;
    //    "error_code" = 0;
    //    "error_msg" = SUCCESS;
    //    "log_id" = 3500194152018;
    //    result =     {
    //        "face_list" =         (
    //                        {
    //                "face_token" = e0bcb12553382461128c9c656f25f941;
    //            },
    //                        {
    //                "face_token" = 3e6587734cd2b483c82d09f1de39f9ab;
    //            }
    //        );
    //        score = "96.70288085999999";
    //    };
    //    timestamp = 1591867997;

    public int error_code;
    public String error_msg;
    public Result result;

    public static class Result{
        public double score;
    }

}
