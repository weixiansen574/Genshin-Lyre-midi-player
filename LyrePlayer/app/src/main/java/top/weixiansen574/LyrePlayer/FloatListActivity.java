package top.weixiansen574.LyrePlayer;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FloatListActivity extends AppCompatActivity {
    RecyclerView music_list;
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
        ArrayList<String> musicNamesList = floatListManager.getMusicNames();
        MusicListAdapter adapter = new MusicListAdapter(FloatListActivity.this,musicNamesList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        music_list.setLayoutManager(linearLayoutManager);
        music_list.setAdapter(adapter);
    }
}
