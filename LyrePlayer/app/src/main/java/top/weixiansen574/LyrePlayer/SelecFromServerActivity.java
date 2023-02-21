package top.weixiansen574.LyrePlayer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import top.weixiansen574.LyrePlayer.adapter.MusicListAdapterForOnline;
import top.weixiansen574.LyrePlayer.midi.MidiProcessor;
import top.weixiansen574.LyrePlayer.midi.Note;
import top.weixiansen574.LyrePlayer.util.HttpUtil;

public class SelecFromServerActivity extends AppCompatActivity {
    RecyclerView music_list;
    Uri fileUri;
    String fileName;
    SharedPreferences uploadInfo;
    SwipeRefreshLayout swipe;
    LinearLayoutManager linearLayoutManager;
    public static String SERVER_ADDRESS;
    MusicListAdapterForOnline adapter;
    int lastVisibleItemPosition;
    int totalPages;
    int currentPage;
    int maxCount;
    boolean hasFootView = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                search();
                break;
            //返回上一级
            case 16908332:
                finish();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selec_from_server);
        FloatingActionButton fab = findViewById(R.id.upload);
        music_list = findViewById(R.id.online_music_library);
        uploadInfo = getSharedPreferences("upload_info", Context.MODE_PRIVATE);
        swipe = findViewById(R.id.swipe);
        SERVER_ADDRESS = getSharedPreferences("server", Context.MODE_PRIVATE).getString("address", "lyre-player.weixiansen574.top:1180");
        linearLayoutManager = new LinearLayoutManager(SelecFromServerActivity.this);
        music_list.setLayoutManager(linearLayoutManager);
        music_list.addItemDecoration(new DividerItemDecoration(SelecFromServerActivity.this, DividerItemDecoration.VERTICAL));
        music_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == linearLayoutManager.getItemCount() - 1) {
                        //当滑动到底部时
                        if (currentPage < totalPages && hasFootView){
                            currentPage++;
                            getLatestSongsList(currentPage);
                        } else if (currentPage >= totalPages && hasFootView){

                        }
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent调用系统文件管理器，选择midi文件
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/midi");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setLatestSongsList();
                swipe.setRefreshing(false);
            }
        });
        setLatestSongsList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            fileUri = data.getData();
            String path = fileUri.getPath();
            fileName = path.substring(path.lastIndexOf("/") + 1);
            ArrayList<Note> noteList = MidiProcessor.toNoteList(getContentResolver().openInputStream(fileUri));
            long midiDuration = 0;
            for (int i = noteList.size() - 1; i > 0; i--) {
                if (noteList.get(i).isNoteOn()){
                    midiDuration = noteList.get(i).getTick();
                    break;
                }
            }
            System.out.println(midiDuration);
                    //sequence.getMicrosecondLength()/1000;

            View view = View.inflate(SelecFromServerActivity.this, R.layout.upload_dialog, null);
            final EditText upload_name = view.findViewById(R.id.upload_name);
            final EditText upload_by = view.findViewById(R.id.upload_by);
            final EditText delete_passowrd = view.findViewById(R.id.delete_password);
            final TextView current_file = view.findViewById(R.id.current_file);
            final InputStream inputStream = getContentResolver().openInputStream(fileUri);
            final long fileSize = inputStream.available();
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            current_file.setText("当前文件：" + fileName +"\n文件大小：" + fileSize/1024 + "KB\n歌曲时长：" + sdf.format(new Date(midiDuration)));
            //读取上一次输入的上传者名称以及删除密码
            upload_by.setText(uploadInfo.getString("upload_by", ""));
            delete_passowrd.setText(uploadInfo.getString("delete_password", ""));
            upload_name.setText(fileName.replace(".mid", ""));

            final AlertDialog alertDialog = new AlertDialog.Builder(SelecFromServerActivity.this)
                    .setTitle("填写上传信息")
                    .setView(view)
                    .setPositiveButton("上传", null)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            final long finalMidiDuration = midiDuration;
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //上传文件
                    uploadInfo.edit()
                            .putString("upload_by", upload_by.getText().toString())
                            .putString("delete_password", delete_passowrd.getText().toString())
                            .commit();
                    if (upload_name.getText().length() == 0) {
                        upload_name.setError(getString(R.string.mingchengbunengweikong));
                    } else if (upload_name.getText().length() > 100) {
                        upload_name.setError(getString(R.string.mcbndy100gzf));
                    } else {
                        byte[] fileData = null;
                        try {
                            fileData = new byte[inputStream.available()];
                            inputStream.read(fileData);
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (fileData.length <= 1048576) {
                            RequestBody requestBody = RequestBody.create(MediaType.parse("audio/midi"), fileData);
                            MultipartBody multipartBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("upload_by", upload_by.getText().toString())
                                    .addFormDataPart("delete_password", delete_passowrd.getText().toString())
                                    .addFormDataPart("hash", MD5.getMD5Three(fileData))
                                    .addFormDataPart("file_size",fileSize+"")
                                    .addFormDataPart("duration", finalMidiDuration +"")
                                    .addFormDataPart("file", upload_name.getText().toString(), requestBody)
                                    .build();
                            OkHttpClient okHttpClient = HttpUtil.getClient();
                            Request request = new Request.Builder()
                                    .post(multipartBody)
                                    .url("http://" + SERVER_ADDRESS + "/upload")
                                    .build();
                            alertDialog.dismiss();
                            final ProgressDialog progressDialog = new ProgressDialog(SelecFromServerActivity.this).show(SelecFromServerActivity.this, getString(R.string.shangchuanzhong), "", false, false);
                            okHttpClient.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    progressDialog.dismiss();
                                    networkError(e);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.code() == 200) {
                                        JSONObject jsonObject = JSON.parseObject(response.body().string());
                                        Message message = new Message();
                                        message.what = 3;
                                        Map<String, String> log = new HashMap<>();
                                        log.put("title", jsonObject.getBoolean("succeed") ? "上传成功" : "上传失败");
                                        log.put("message", jsonObject.getString("message"));
                                        message.obj = log;
                                        progressDialog.dismiss();
                                        handler.sendMessage(message);
                                    } else {
                                        progressDialog.dismiss();
                                        networkError(response.code());
                                    }
                                }
                            });
                        } else {
                            new AlertDialog.Builder(SelecFromServerActivity.this).setTitle(R.string.wenjianguoda).setMessage(R.string.zgksc1mbwj).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }
                    }
                }
            });
        } catch (RuntimeException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(SelecFromServerActivity.this, getString(R.string.dqwjsydyc) + e.getMessage() + getString(R.string.qxzzcnbfdwj), Toast.LENGTH_LONG).show();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    private void setLatestSongsList() {
        currentPage = 1;
        hasFootView = true;
        final ProgressDialog progressDialog = new ProgressDialog(SelecFromServerActivity.this).show(SelecFromServerActivity.this, getString(R.string.zzhqzxyy), "", false, false);
        HttpUtil.GETJson("http://" + SERVER_ADDRESS + "/latest_songs", this, handler, new HttpUtil.Receive_WithDismiss() {
            @Override
            public void doDismiss(Exception e) {
                progressDialog.dismiss();
            }

            @Override
            public void success(JSONObject jsonObject) {
                progressDialog.dismiss();
                totalPages = jsonObject.getInteger("total_pages");
                maxCount = jsonObject.getInteger("count");
                JSONArray jsonArray = jsonObject.getJSONArray("midis");
                List<JSONObject> musicList = new ArrayList<>(jsonArray.size());
                for (int i = 0; i < jsonArray.size(); i++) {
                    musicList.add(jsonArray.getJSONObject(i));
                }
                adapter = new MusicListAdapterForOnline(SelecFromServerActivity.this, hasFootView, musicList);
                adapter.maxCount = maxCount;
                music_list.setAdapter(adapter);
            }
        });


    }

    private void getLatestSongsList(int page) {
        HttpUtil.GETJson("http://" + SERVER_ADDRESS + "/latest_songs?page=" + page, this, handler, new HttpUtil.Receive() {
            @Override
            public void success(JSONObject jsonObject) {
                JSONArray jsonArray = jsonObject.getJSONArray("midis");
                List<JSONObject> musicList = new ArrayList<>(jsonArray.size());
                for (int i = 0; i < jsonArray.size(); i++) {
                    musicList.add(jsonArray.getJSONObject(i));
                }
                adapter.addData(musicList);
            }
        });
    }

    private void search() {
        hasFootView = false;
        View view = View.inflate(SelecFromServerActivity.this, R.layout.edit_text, null);
        final EditText text = view.findViewById(R.id.edit_text);
        new AlertDialog.Builder(SelecFromServerActivity.this)
                .setTitle(R.string.search)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = new ProgressDialog(SelecFromServerActivity.this).show(SelecFromServerActivity.this, "搜索中……", "", false, false);
                        HttpUtil.GETJson("http://" + SERVER_ADDRESS + "/search?name=" + text.getText(), SelecFromServerActivity.this, handler, new HttpUtil.Receive_WithDismiss() {
                            @Override
                            public void doDismiss(Exception e) {
                                progressDialog.dismiss();
                            }

                            @Override
                            public void success(JSONObject jsonObject) {
                                JSONArray jsonArray = jsonObject.getJSONArray("results");
                                List<JSONObject> musicList = new ArrayList<>(jsonArray.size());
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    musicList.add(jsonArray.getJSONObject(i));
                                }
                                progressDialog.dismiss();
                                adapter = new MusicListAdapterForOnline(SelecFromServerActivity.this, hasFootView, musicList);
                                adapter.maxCount = maxCount;
                                music_list.setAdapter(adapter);
                                Toast.makeText(SelecFromServerActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void networkError(IOException e) {
        Message message = new Message();
        message.what = 4;
        message.obj = e.getMessage();
        handler.sendMessage(message);
    }

    public void networkError(int code) {
        Message message = new Message();
        message.what = 4;
        switch (code) {
            case 500:
                message.obj = "HTTP状态：500 - 内部服务器错误";
                break;
            case 404:
                message.obj = "HTTP状态：404 - 404 Not fount";
                break;
        }
        handler.sendMessage(message);
    }
    public Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull final Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(SelecFromServerActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    List musicList = (List) msg.obj;
                    adapter = new MusicListAdapterForOnline(SelecFromServerActivity.this, hasFootView, musicList);
                    adapter.maxCount = maxCount;
                    music_list.setAdapter(adapter);
                    break;
                case 3:
                    Map<String, String> log = (Map<String, String>) msg.obj;
                    final AlertDialog alertDialog = new AlertDialog.Builder(SelecFromServerActivity.this)
                            .setTitle(log.get("title"))
                            .setMessage(log.get("message"))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    break;
                case 4:
                    new AlertDialog.Builder(SelecFromServerActivity.this)
                            .setTitle(R.string.wfljzfwq)
                            .setMessage((String) msg.obj)
                            .setPositiveButton(R.string.ckbz, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(SelecFromServerActivity.this,NetworkHelpActivity.class));
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    break;
                case 5:
                    adapter.addData((List<JSONObject>) msg.obj);
                    break;
                case 6:

            }
        }
    };


}