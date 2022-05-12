package top.weixiansen574.LyrePlayer.util;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;

import okhttp3.OkHttpClient;

public class HttpUtil {
    static OkHttpClient okHttpClient = new OkHttpClient();
    public static OkHttpClient getClient(){
        return okHttpClient;
    }
}
