package top.weixiansen574.LyrePlayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class SelectFile extends Activity {
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/midi");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        this.uri = data.getData();
        Intent aas = new Intent(this,AdjustAndStartActivity.class);
        String path = uri.getPath();

        aas.putExtra("path",path.replace("/document/primary:","/storage/emulated/0/"));
        startActivity(aas);
        finish();
    }
}