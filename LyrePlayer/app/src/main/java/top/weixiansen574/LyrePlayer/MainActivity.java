package top.weixiansen574.LyrePlayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    Button selectFromServer;
    SharedPreferences server;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.openMidiFile = this.findViewById(R.id.openMidiFile);
        this.openMidiFile.setOnClickListener(this);
        server = getSharedPreferences("server", Context.MODE_PRIVATE);
        openFloatList = findViewById(R.id.open_float_list);
        openFloatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FloatList.class));
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
                        .setTitle("???????????????")
                        .setMessage("??????????????????????????????????????????")
                        .setView(view)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                server.edit().putString("address", editText.getText().toString()).commit();
                            }
                        })
                        .setNegativeButton("??????", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton("??????",null).show();
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
        //???????????????????????????QQ?????????????????????????????????????????????URI????????????activity
        if (intent0.getAction() == "android.intent.action.VIEW") {
            Uri uri = intent0.getData();
            Intent intent = new Intent(this, AdjustAndStartActivity.class);
            intent.putExtra("open_file", true);
            intent.setData(uri);
            startActivity(intent);
        }
        //????????????music_list?????????????????????????????????????????????????????????????????????????????????
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
        //??????????????????????????????????????????
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
