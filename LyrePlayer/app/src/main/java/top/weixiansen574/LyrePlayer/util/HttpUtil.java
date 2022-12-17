package top.weixiansen574.LyrePlayer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.weixiansen574.LyrePlayer.NetworkHelpActivity;
import top.weixiansen574.LyrePlayer.R;

public class HttpUtil {
    static OkHttpClient okHttpClient = new OkHttpClient();

    public static OkHttpClient getClient() {
        return okHttpClient;
    }

    public static void GETJson(String url, final Context context, final Handler handler, final Receive receive) {
        final Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if(receive instanceof Receive_WithDismiss){
                    Receive_WithDismiss receive_withDismiss = (Receive_WithDismiss)receive;
                    receive_withDismiss.doDismiss(e);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.wfljzfwq)
                                .setMessage(e.getMessage())
                                .setPositiveButton(R.string.ckbz, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        context.startActivity(new Intent(context, NetworkHelpActivity.class));
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final JSONObject jsonObject = JSON.parseObject(response.body().string());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        receive.success(jsonObject);
                    }
                });
            }
        });
    }
    public interface Receive {
        void success(JSONObject jsonObject);
    }
    public interface Receive_WithDismiss extends Receive {
        void doDismiss(Exception e);
    }
}
