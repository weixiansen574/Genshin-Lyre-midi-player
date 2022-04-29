package top.weixiansen574.LyrePlayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FloatList extends AppCompatActivity {
    ListView music_list;
    SharedPreferences music_speed_list;
    SharedPreferences midi_info;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //工具栏返回上一级按钮
        if (item.getItemId() == 16908332){
            finish();
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        music_speed_list = getSharedPreferences("music_speed_list", Context.MODE_PRIVATE);
        midi_info = getSharedPreferences("midi_info",Context.MODE_PRIVATE);
        setContentView(R.layout.activity_float_list);
        music_list = findViewById(R.id.music_list_main);
        final String musicNames[];
        final File[] musicFiles = new File(getFilesDir(), "music_list").listFiles();
        Arrays.sort(musicFiles);
        musicNames = new String[musicFiles.length];
        for (int i = 0; i < musicFiles.length; i++) {
            musicNames[i] = musicFiles[i].getName();
        }
        ArrayList<String> musicNamesList = new ArrayList<String>(Arrays.asList(musicNames));
        MusicListAdapter adapter = new MusicListAdapter(FloatList.this,R.layout.list_item,musicNamesList);
        music_list.setAdapter(adapter);
        music_list.setOnItemClickListener(new openMusic(getApplicationContext(),musicNames,FloatList.this));
    }

    public void delete(final String name){
        new AlertDialog.Builder(this).setTitle(R.string.qdyscm).setMessage(name).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int i) {
                File[] musicFiles = new File(getFilesDir(), "music_list").listFiles();
                for(File musicFile : musicFiles){
                    if(musicFile.getName().equals(name)){
                        //删除文件并清除音乐相关速度信息
                        musicFile.delete();
                        music_speed_list.edit().remove(name).commit();
                        musicFiles = new File(getFilesDir(), "music_list").listFiles();
                        final String musicNames[] = new String[musicFiles.length];
                        for (int j = 0; j < musicFiles.length; j++) {
                            musicNames[j] = musicFiles[j].getName();
                        }
                        ArrayList<String> musicNamesList = new ArrayList<String>(Arrays.asList(musicNames));
                        MusicListAdapter adapter = new MusicListAdapter(FloatList.this,R.layout.list_item,musicNamesList);
                        music_list.setAdapter(adapter);
                        music_list.setOnItemClickListener(new openMusic(getApplicationContext(),musicNames,FloatList.this));
                    }
                }
            }
        }).show();
    }
    public void startFloatingWindow (final String name){
        new AlertDialog.Builder(this).setTitle(R.string.querengequ).setMessage(name).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //判断是否有悬浮窗权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(FloatList.this)) {
                    new AlertDialog.Builder(FloatList.this).setTitle(R.string.floating_window_permission_is_required).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 100);
                        }
                    }).show();
                } else {
                    //已经有权限，可以直接显示悬浮窗
                    //判断是否有无障碍权限
                    if (!ClickService.isStart()) {
                        new AlertDialog.Builder(FloatList.this).setTitle(R.string.Accessibility_required).setMessage(R.string.Accessibility_required_msg).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    FloatList.this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                                } catch (Exception e) {
                                    FloatList.this.startActivity(new Intent(Settings.ACTION_SETTINGS));
                                    e.printStackTrace();
                                }
                            }
                        }).show();
                    } else {
                        midi_info.edit().putBoolean("isMusicList",true).commit();
                        midi_info.edit().putString("midi_name",name).commit();
                        Intent intent = new Intent(FloatList.this, FloatingButtonService.class);
                        startService(intent);
                    }
                }
            }
        }).show();
    }
}
class deleteMusic implements View.OnClickListener{
    FloatList activity;
    String name;

    public deleteMusic(FloatList activity, String name) {
        this.activity = activity;
        this.name = name;
    }

    @Override
    public void onClick(View v) {
        System.out.println(name);
        activity.delete(name);
    }
}
class openMusic implements AdapterView.OnItemClickListener{
    Context context;
    String[] musicNames;
    FloatList floatList;

    public openMusic(Context context, String[] musicNames, FloatList floatList) {
        this.context = context;
        this.musicNames = musicNames;
        this.floatList = floatList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        floatList.startFloatingWindow(musicNames[position]);
    }
}