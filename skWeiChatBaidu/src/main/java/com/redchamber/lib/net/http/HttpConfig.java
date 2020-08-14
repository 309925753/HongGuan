package com.redchamber.lib.net.http;

public class HttpConfig {

    public static String BASE_URL = "http://192.168.0.130:8093/";

    public static boolean isDebug = true;
    //网络缓存地址
    public static String URL_CACHE;
    public static String HttpLogTAG = "red_chamber_http";
    //缓存最大的内存,默认为10M
    public static long MAX_MEMORY_SIZE = 10 * 1024 * 1024;

    //SharePreference的配置文件名
    public static String USER_CONFIG = "red_chamber_preference";

    //链接超时时间
    public static final int connectTimeout = 300;
    //读取超时时间
    public static final int readTimeout = 300;
    //写超时时间
    public static final int writeTimeout = 300;

}
