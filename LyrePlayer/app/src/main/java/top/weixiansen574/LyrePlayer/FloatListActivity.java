package top.weixiansen574.LyrePlayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

import top.weixiansen574.LyrePlayer.util.AccessibilityUtil;
import top.weixiansen574.LyrePlayer.util.NoteListStorage;

public class FloatListActivity extends AppCompatActivity {
    ListView music_list;
    FloatListManager floatListManager;
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
        floatListManager = new FloatListManager(FloatListActivity.this);
        setContentView(R.layout.activity_float_list);
        music_list = findViewById(R.id.music_list_main);
        String musicNames[] = floatListManager.getMusicNames();
        ArrayList<String> musicNamesList = new ArrayList<String>(Arrays.asList(musicNames));
        MusicListAdapter adapter = new MusicListAdapter(FloatListActivity.this,R.layout.list_item,musicNamesList);
        music_list.setAdapter(adapter);
        music_list.setOnItemClickListener(new openMusic(getApplicationContext(),musicNames, FloatListActivity.this));
    }

    public void delete(final String name){
        new AlertDialog.Builder(this).setTitle(R.string.qdyscm).setMessage(name).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int i) {
                        //删除文件并清除音乐相关速度信息
                        floatListManager.deleteMusic(name);
                        final String musicNames[] = floatListManager.getMusicNames();
                        ArrayList<String> musicNamesList = new ArrayList<String>(Arrays.asList(musicNames));
                        MusicListAdapter adapter = new MusicListAdapter(FloatListActivity.this,R.layout.list_item,musicNamesList);
                        music_list.setAdapter(adapter);
                        music_list.setOnItemClickListener(new openMusic(getApplicationContext(),musicNames, FloatListActivity.this));
            }
        }).show();
    }
    public void startFloatingWindow (final String name){
        final AlertDialog dialog_start = new AlertDialog.Builder(this).setTitle(R.string.querengequ).setMessage(name).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton(R.string.ok, null).show();
        dialog_start.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否有悬浮窗权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(FloatListActivity.this)) {
                    new AlertDialog.Builder(FloatListActivity.this).setTitle(R.string.floating_window_permission_is_required).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
                    if (AccessibilityUtil.checkPermission(FloatListActivity.this)) {
                        Intent intent = new Intent(FloatListActivity.this, FloatingButtonService.class);
                        intent.putExtra("name",name);
                        intent.putExtra("noteListKey", NoteListStorage.putNoteList(floatListManager.getLyreNotes(name)));
                        startService(intent);
                        dialog_start.dismiss();
                    }
                }
            }
        });
    }
}
class deleteMusic implements View.OnClickListener{
    FloatListActivity activity;
    String name;

    public deleteMusic(FloatListActivity activity, String name) {
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
    FloatListActivity floatListActivity;

    public openMusic(Context context, String[] musicNames, FloatListActivity floatListActivity) {
        this.context = context;
        this.musicNames = musicNames;
        this.floatListActivity = floatListActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        floatListActivity.startFloatingWindow(musicNames[position]);
    }
}