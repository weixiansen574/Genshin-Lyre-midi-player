package top.weixiansen574.LyrePlayer.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import top.weixiansen574.LyrePlayer.AdjustAndStartActivity;
import top.weixiansen574.LyrePlayer.R;
import top.weixiansen574.LyrePlayer.SelecFromServerActivity;
import top.weixiansen574.LyrePlayer.util.HttpUtil;

public class MusicListAdapterForOnline extends RecyclerView.Adapter {
    public Context context;
    List<JSONObject> objects;
    boolean hasFootView;
    public int maxCount;

    public MusicListAdapterForOnline(Context context, boolean hasFootView, List<JSONObject> objects) {
        this.context = context;
        this.objects = objects;
        this.hasFootView = hasFootView;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == objects.size() && hasFootView){
            return 1;
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1){
            View footView = View.inflate(context,R.layout.foot_view,null);
            return new FooterViewHolder(footView);
        } else {
            View itemView = View.inflate(context,R.layout.list_item_online_music,null);
            return new MyViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            View itemView = holder.itemView;
            JSONObject jsonObject;
            jsonObject = objects.get(position);
            TextView name = itemView.findViewById(R.id.online_music_name);
            TextView uploadBy = itemView.findViewById(R.id.online_music_upload_by);
            name.setText(jsonObject.getString("name"));
            uploadBy.setText(jsonObject.getString("upload_by"));
        } else if (holder instanceof FooterViewHolder && maxCount == objects.size()){
            View footView = holder.itemView;
            TextView textView = footView.findViewById(R.id.tv_foot_view);
            textView.setText("翻到底了！");
        }
    }


    @Override
    public int getItemCount() {
        int count = objects.size();
        if (hasFootView){
            count++;
        }
        return count;
    }

    public void addData(int postiton,JSONObject jsonObject) {
        objects.add(postiton,jsonObject);
        notifyItemInserted(postiton);
    }

    public void addData(List<JSONObject> latestSongsList) {
        objects.addAll(latestSongsList);
        notifyItemInserted(getItemCount());
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ImageView imageView = itemView.findViewById(R.id.item_menu);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View dialogView = View.inflate(context,R.layout.online_music_info_dialog,null);
                    final JSONObject jsonObject = objects.get(getLayoutPosition());
                    TextView tv_name = dialogView.findViewById(R.id.music_info_name);
                    TextView tv_upload_by = dialogView.findViewById(R.id.music_info_upload_by);
                    TextView tv_duration = dialogView.findViewById(R.id.music_info_duration);
                    TextView tv_file_size = dialogView.findViewById(R.id.music_info_file_size);
                    TextView tv_MD5 = dialogView.findViewById(R.id.music_info_MD5);
                    tv_name.setText(jsonObject.getString("name"));
                    tv_upload_by.setText(jsonObject.getString("upload_by"));
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

                    tv_duration.setText(sdf.format(Integer.parseInt(jsonObject.getString("duration"))));
                    tv_file_size.setText(String.format("%.2f",Double.parseDouble(jsonObject.getString("file_size"))/1024) + "KB");
                    tv_MD5.setText(jsonObject.getString("hash"));
                    final AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("音乐详情")
                            .setView(dialogView)
                            .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNegativeButton("删除音乐",null).show();
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View dialogView = View.inflate(context, R.layout.edit_text, null);
                            final EditText et_deletePassword = dialogView.findViewById(R.id.edit_text);
                            AlertDialog deleteDialog = new AlertDialog.Builder(context)
                                    .setTitle("请输入删除密码：")
                                    .setView(dialogView)
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            RequestBody requestBody = new FormBody.Builder()
                                                    .add("hash",jsonObject.getString("hash"))
                                                    .add("delete_password",et_deletePassword.getText().toString())
                                                    .build();
                                            Request request = new Request.Builder()
                                                    .url("http://" + SelecFromServerActivity.SERVER_ADDRESS + "/delete")
                                                    .post(requestBody)
                                                    .build();
                                            OkHttpClient okHttpClient = HttpUtil.getClient();
                                            Call call = okHttpClient.newCall(request);
                                            final Handler handler = new Handler(Looper.myLooper()){
                                                @Override
                                                public void handleMessage(@NonNull Message msg) {
                                                    switch (msg.what) {
                                                        case 1:
                                                            Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                                                            alertDialog.dismiss();
                                                            objects.remove(getLayoutPosition());
                                                            notifyItemRemoved(getLayoutPosition());
                                                            break;
                                                        case 2:
                                                            Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }
                                                }
                                            };
                                            call.enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    SelecFromServerActivity activity = (SelecFromServerActivity)context;
                                                    activity.networkError(e);
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    JSONObject jsonObject1 = JSON.parseObject(response.body().string());
                                                    Message message = new Message();
                                                    message.what = (jsonObject1.getBoolean("succeed") ? 1 : 2);
                                                    message.obj = jsonObject1.getString("message");
                                                    handler.sendMessage(message);
                                                }
                                            });

                                        }
                                    }).show();
                        }
                    });
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("确认下载")
                            .setMessage("歌名：" + objects.get(getLayoutPosition()).getString("name"))
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, final int which) {
                                    //判断存储权限
                                    if (ContextCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle(R.string.qsyccqx).setMessage("需要存储权限以下载文件！\n" + context.getString(R.string.qsyccqx_msg_2)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ActivityCompat.requestPermissions((Activity) context, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
                                            }
                                        }).show();
                                    } else {
                                        final ProgressDialog progressDialog = new ProgressDialog(context).show(context, "下载中……", "", false, false);

                                    final Handler handler = new Handler(Looper.myLooper()) {
                                        @Override
                                        public void handleMessage(@NonNull final Message msg) {
                                            final String path = (String) msg.obj;
                                            progressDialog.dismiss();
                                            AlertDialog dialog = new AlertDialog.Builder(context)
                                                    .setTitle("下载完成")
                                                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            //将文件存入URI，然后打开activity进行配置
                                                            Uri uri = Uri.fromFile(new File(path));
                                                            Intent intent = new Intent(context, AdjustAndStartActivity.class);
                                                            intent.putExtra("open_file", true);
                                                            intent.setData(uri);
                                                            context.startActivity(intent);
                                                        }
                                                    })
                                                    .setNegativeButton("完成", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Toast.makeText(context, "文件已保存至" + path, Toast.LENGTH_LONG).show();
                                                        }
                                                    }).show();
                                        }
                                    };

                                    OkHttpClient okHttpClient = HttpUtil.getClient();
                                    Request request = new Request.Builder().url("http://" + SelecFromServerActivity.SERVER_ADDRESS + "/download?hash=" + objects.get(getLayoutPosition()).getString("hash")).build();
                                    okHttpClient.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            SelecFromServerActivity activity = (SelecFromServerActivity) context;
                                            activity.networkError(e);
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            InputStream inputStream = response.body().byteStream();
                                            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/midi/");
                                            if (!file.exists()) {
                                                file.mkdirs();
                                            }
                                            String path = Environment.getExternalStorageDirectory().getPath() + "/Download/midi/" + Uri.decode(response.header("Content-Disposition").replace("attachment; filename=", "")) + ".mid";
                                            FileOutputStream fos = new FileOutputStream(path);
                                            int len = 0;
                                            byte[] buffer = new byte[4096];
                                            while ((len = inputStream.read(buffer)) != -1) {
                                                fos.write(buffer, 0, len);
                                            }
                                            fos.flush();
                                            fos.close();
                                            inputStream.close();
                                            final Message message = new Message();
                                            message.obj = path;
                                            handler.sendMessage(message);
                                        }
                                    });
                                }
                            }
                            }).show();
                    }

            });

        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
