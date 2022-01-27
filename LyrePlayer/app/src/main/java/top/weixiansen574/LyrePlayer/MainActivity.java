package top.weixiansen574.LyrePlayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button openMidiFile;
    Button openFloatList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.openMidiFile = this.findViewById(R.id.openMidiFile);
        this.openMidiFile.setOnClickListener(this);

        openFloatList = findViewById(R.id.open_float_list);
        openFloatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FloatList.class));
            }
        });
        Intent intent0 = getIntent();
        //当使用文件管理器、QQ等第三方应用打开文件时，直接将URI传给下一activity
        if (intent0.getAction() == "android.intent.action.VIEW") {
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
}
