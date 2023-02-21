package top.weixiansen574.LyrePlayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import top.weixiansen574.LyrePlayer.midi.FloatMusicBean;
import top.weixiansen574.LyrePlayer.util.AccessibilityUtil;
import top.weixiansen574.LyrePlayer.util.NoteListStorage;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MyViewHolder> {
    Context context;
    FloatListManager floatListManager;
    List<String> musicNames;

    public MusicListAdapter(Context context, List<String> musicNames) {
        this.context = context;
        this.musicNames = musicNames;
        floatListManager = new FloatListManager(context);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(context, R.layout.list_item, null);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FloatMusicBean musicBean = floatListManager.getFloatMusicBean(musicNames.get(position));
        if (musicBean.type == FloatListManager.MI_TYPE_LYRE) {
            holder.img_icon.setImageDrawable(context.getDrawable(R.drawable.lrye_round_icon));
        } else if (musicBean.type == FloatListManager.MI_TYPE_OLD_LYRE) {
            holder.img_icon.setImageDrawable(context.getDrawable(R.drawable.old_lyre_round_icon));
        }
        holder.txv_name.setText(musicBean.name);
        Activity activity = (Activity) context;
        holder.itemView.setOnClickListener(v -> {
            final AlertDialog dialog_start = new AlertDialog.Builder(context).setTitle(R.string.querengequ).setMessage(musicBean.name).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setPositiveButton(R.string.ok, null).show();
            dialog_start.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //判断是否有悬浮窗权限
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        new AlertDialog.Builder(context).setTitle(R.string.floating_window_permission_is_required).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                activity.startActivityForResult(intent, 100);
                            }
                        }).show();
                    } else {
                        //已经有权限，可以直接显示悬浮窗
                        //判断是否有无障碍权限
                        if (AccessibilityUtil.checkPermission(context)) {
                            Intent intent = new Intent(context, FloatingButtonService.class);
                            intent.putExtra("name", musicBean.name);
                            intent.putExtra("noteListKey", NoteListStorage.putNoteList(floatListManager.getFloatMusicBean(musicBean.name).noteList));
                            activity.startService(intent);
                            dialog_start.dismiss();
                        }
                    }
                }
            });
        });
        holder.btn_delete.setOnClickListener((View.OnClickListener) v -> {
            new AlertDialog.Builder(context).setTitle(R.string.qdyscm).setMessage(musicBean.name).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    //删除文件并清除音乐相关速度信息
                    floatListManager.deleteMusic(musicBean.name);
                    musicNames.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            }).show();
        });
    }

    @Override
    public int getItemCount() {
        return musicNames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView img_icon;
        TextView txv_name;
        Button btn_delete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            img_icon = itemView.findViewById(R.id.img_music_icon);
            txv_name = itemView.findViewById(R.id.music_name);
            btn_delete = itemView.findViewById(R.id.delate);
        }
    }
}
