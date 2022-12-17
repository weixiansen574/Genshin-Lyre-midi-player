package top.weixiansen574.LyrePlayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MusicListAdapter extends ArrayAdapter {
    private int resourceId;
    FloatListActivity context;
    public MusicListAdapter(Context context, int resource,List<String> objects) {
        super(context, resource, objects);
        this.context = (FloatListActivity) context;
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String name = (String) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView musicName = view.findViewById(R.id.music_name);
        Button delete = view.findViewById(R.id.delate);
        delete.setOnClickListener(new deleteMusic(context,name));
        musicName.setText(name);
        return view;
    }

}
