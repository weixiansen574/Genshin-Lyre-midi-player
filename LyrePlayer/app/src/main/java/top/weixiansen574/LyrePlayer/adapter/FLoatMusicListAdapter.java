package top.weixiansen574.LyrePlayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import top.weixiansen574.LyrePlayer.FloatListManager;
import top.weixiansen574.LyrePlayer.R;
import top.weixiansen574.LyrePlayer.midi.FloatMusicBean;

public class FLoatMusicListAdapter extends RecyclerView.Adapter<FLoatMusicListAdapter.MyViewHolder> {
    Context context;
    List<String> musicNames;
    OnItemClickListener listener;
    FloatListManager floatListManager;

    public FLoatMusicListAdapter(Context context, List<String> musicNames) {
        this.context = context;
        this.musicNames = musicNames;
        floatListManager = new FloatListManager(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context.getApplicationContext(),R.layout.item_float_window_music,null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int finalPosition = position;
        FloatMusicBean musicBean = floatListManager.getFloatMusicBean(musicNames.get(position));
        if (musicBean.type == FloatListManager.MI_TYPE_LYRE){
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.lrye_round_icon));
        } else if (musicBean.type == FloatListManager.MI_TYPE_OLD_LYRE){
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.old_lyre_round_icon));
        }
        holder.textView.setText(musicBean.name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v,finalPosition);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    @Override
    public int getItemCount() {
        return musicNames.size();
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, int position);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        ImageView imageView;
        TextView textView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.imageView = itemView.findViewById(R.id.img_music_icon);
            this.textView = itemView.findViewById(R.id.music_name);
        }
    }
}
