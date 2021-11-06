package top.weixiansen574.LyrePlayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button openMidiFile;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.openMidiFile = this.findViewById(R.id.openMidiFile);
        this.openMidiFile.setOnClickListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.about:
                startActivity(new Intent(this,AboutActivity.class));
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
                Toast.makeText(this, "\u6743\u9650\u7533\u8bf7\u5931\u8d25\uff0c\u60a8\u53ef\u4ee5\u53bb\u8bbe\u7f6e\u81ea\u884c\u6253\u5f00\uff01", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClick(View view) {
        //判断是拥有存储权限，否则申请
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("\u8bf7\u6388\u4e88\u5b58\u50a8\u6743\u9650").setMessage("\u5426\u5219\u65e0\u6cd5\u6253\u5f00midi\u6587\u4ef6\uff01\n\u4e0d\u5efa\u8bae\u201c\u4ec5\u5728\u4f7f\u7528\u4e2d\u5141\u8bb8\u201d").setPositiveButton("\u786e\u5b9a", new DialogInterface.OnClickListener(){

                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
                }
            }).show();
        } else {
        startActivity(new Intent(this,SelectFile.class));
        }
    }



}
