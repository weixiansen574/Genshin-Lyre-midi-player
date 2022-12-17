package top.weixiansen574.LyrePlayer;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;

import top.weixiansen574.LyrePlayer.util.ShellUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button openMidiFile;
    Button openFloatList;
    Button selectFromServer;
    SharedPreferences server;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.openMidiFile = this.findViewById(R.id.openMidiFile);
        this.openMidiFile.setOnClickListener(this);
        test();
        server = getSharedPreferences("server", Context.MODE_PRIVATE);
        openFloatList = findViewById(R.id.open_float_list);
        openFloatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FloatListActivity.class));
            }
        });
        selectFromServer = findViewById(R.id.select_from_server);
        selectFromServer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View view = View.inflate(MainActivity.this, R.layout.edit_text, null);
                final EditText editText = view.findViewById(R.id.edit_text);
                editText.setText(server.getString("address", "lyre-player.weixiansen574.top:1180"));
                AlertDialog setServerDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("服务器地址")
                        .setMessage("仅开发者调试用，请不要乱改！")
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                server.edit().putString("address", editText.getText().toString()).commit();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton("默认",null).show();
                setServerDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setText("lyre-player.weixiansen574.top:1180");
                    }
                });
                return false;
            }
        });
        selectFromServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SelecFromServerActivity.class));
            }
        });
        Intent intent0 = getIntent();
        //当使用文件管理器、QQ等第三方应用打开文件时，直接将URI传给下一activity
        if (intent0.getAction().equals("android.intent.action.VIEW")) {
            Uri uri = intent0.getData();
            Intent intent = new Intent(this, AdjustAndStartActivity.class);
            intent.putExtra("open_file", true);
            intent.setData(uri);
            startActivity(intent);
        }
        //如果没有music_list文件夹就创建一个，免得没有创建文件夹导致打开列表时崩溃
        File lyreNotesFile = getFilesDir();
        lyreNotesFile = new File(lyreNotesFile, "music_list");
        if (!lyreNotesFile.exists()) {
            lyreNotesFile.mkdir();
        }
    }

    private void test() {


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            default:
                break;
        }

        return true;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == 0) return;
                Toast.makeText(this, getString(R.string.qxsqsb), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClick(View view) {
        //判断是拥有存储权限，否则申请
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(R.string.qsyccqx).setMessage(getString(R.string.qsyccqx_msg_1) + getString(R.string.qsyccqx_msg_2)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
                }
            }).show();
        } else {
            startActivity(new Intent(this, AdjustAndStartActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        if (isAccessibilitySettingsOn(this,"top.weixiansen574.LyrePlayer.ClickService")){
            new AlertDialog.Builder(this).setTitle("温馨提示：").setMessage("请先关闭无障碍再退出程序，不然下次打开会导致无障碍功能出故障，导致启用了但是等于没有的问题（安卓系统背锅，万年不修此bug！）")
                    .setPositiveButton("去关闭", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            } catch (Exception e) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("取消",null).setNeutralButton("使用root快速关闭", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(ShellUtils.execCommand("settings put secure enabled_accessibility_services \"\"",true).result == 0){
                        Toast.makeText(MainActivity.this,"无障碍已关闭，正在安全退出程序",Toast.LENGTH_LONG).show();
                        exit();
                    }else {
                        Toast.makeText(MainActivity.this,"你的手机没有root权限或拒绝了授权",Toast.LENGTH_LONG).show();
                    }
                }
            }).show();
        }else {
            exit();
        }
    }

    public static boolean isAccessibilitySettingsOn(Context context,String className){
        if (context == null){
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices =
                activityManager.getRunningServices(100);// 获取正在运行的服务列表
        if (runningServices.size()<0){
            return false;
        }
        for (int i=0;i<runningServices.size();i++){
            ComponentName service = runningServices.get(i).service;
            System.out.println(service.getClassName());
            if (service.getClassName().equals(className)){
                return true;
            }
        }
        return false;
    }

    public void exit(){
        finish();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }).start();
    }

}
