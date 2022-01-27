// Decompiled by DJ v3.12.12.98 Copyright 2014 Atanas Neshkov  Date: 2021/10/26 18:33:21
// Home Page:  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3)
// Source File Name:   adjustAndStart.java
//谢谢dj java 反编译拯救此代码
package top.weixiansen574.LyrePlayer;

import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Track;
import top.weixiansen574.LyrePlayer.midi.Note;

public class AdjustAndStartActivity extends AppCompatActivity implements View.OnClickListener {
    Spinner spinner3;
    Spinner spinner4;
    Spinner spinner5;
    Spinner spinner6;
    RadioGroup blackKeySettings;
    float speed = -1;
    int tansposition = 11;
    int currentTansposition;
    SharedPreferences midi_info;
    SharedPreferences keyCoordinates;
    SharedPreferences music_speed_list;
    Uri midiUri;
    String midiName;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            midiUri = data.getData();
            String path = midiUri.getPath();
            midiName = path.substring(path.lastIndexOf("/") + 1);
            Toast.makeText(this, "" + getString(R.string.filename) + midiName, Toast.LENGTH_LONG).show();
            if (!midiName.endsWith(".mid")){
                //检测文件名
                new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(getString(R.string.file_mame_waning)).setMessage(getString(R.string.file_name_warning_message) + midiName + getString(R.string.file_name_warning_message1)).setPositiveButton(R.string.show_help, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(AdjustAndStartActivity.this,FileHelp.class));
                    }
                }).setNeutralButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
            }
        } catch (RuntimeException e){
            //如果没有选择到文件
            e.printStackTrace();
            new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.nmyxzwj).setMessage(R.string.nmyxzwj_msg).setPositiveButton(getString(R.string.show_help), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    startActivity(new Intent(AdjustAndStartActivity.this,FileHelp.class));
                }
            }).setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).setCancelable(false).show();

        }


    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent0 = getIntent();
        if(!intent0.getBooleanExtra("open_file",false)) {
            //intent调用系统文件管理器，选择midi文件
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/midi");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
        }else{
            //被intent启动，直接从URI里获取input stream
            midiUri = intent0.getData();
            String path = midiUri.getEncodedPath();
            midiName = path.substring(path.lastIndexOf("/") + 1);
            midiName = Uri.decode(midiName);
            System.out.println(fileHead4Byte());
            //判断是否是midi文件
            if(!fileHead4Byte().equals("4d 54 68 64 ")){
                new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.stxzfmidiwj).setMessage(R.string.stxzfmidiwj_msg).setPositiveButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setCancelable(false).show();
            }
            Toast.makeText(this,getString(R.string.filename) + midiName,Toast.LENGTH_SHORT).show();
        }
        this.setContentView(R.layout.activity_adjust_and_start);
        midi_info = getSharedPreferences("midi_info",Context.MODE_PRIVATE);
        music_speed_list = getSharedPreferences("music_speed_list",Context.MODE_PRIVATE);
        keyCoordinates = getSharedPreferences("key_coordinates",Context.MODE_PRIVATE);

        TextView textView = (TextView) findViewById(R.id.textView);
        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);
        final TextView tanspositionText = findViewById(R.id.tansposition_text);

        spinner3 = (Spinner)this.findViewById(R.id.spinner3);
        spinner4 = (Spinner)this.findViewById(R.id.spinner4);
        spinner5 = (Spinner)this.findViewById(R.id.spinner5);
        spinner6 = (Spinner)this.findViewById(R.id.spinner6);
        this.setSpinner(spinner3, R.array.spinner3_array, 0);
        this.setSpinner(spinner4, R.array.spinner4_array, 1);
        this.setSpinner(spinner5, R.array.spinner5_array, 2);
        this.setSpinner(spinner6, R.array.spinner6_array, 1);
        blackKeySettings = (RadioGroup) findViewById(R.id.black_key_settings);
        final EditText x1 = findViewById(R.id.x1);
        final EditText x2 = findViewById(R.id.x2);
        final EditText x3 = findViewById(R.id.x3);
        final EditText x4 = findViewById(R.id.x4);
        final EditText x5 = findViewById(R.id.x5);
        final EditText x6 = findViewById(R.id.x6);
        final EditText x7 = findViewById(R.id.x7);
        final EditText y1 = findViewById(R.id.y1);
        final EditText y2 = findViewById(R.id.y2);
        final EditText y3 = findViewById(R.id.y3);

        if(keyCoordinates.contains("x1")){
            x1.setText(keyCoordinates.getInt("x1",0) + "");
            x2.setText(keyCoordinates.getInt("x2",0) + "");
            x3.setText(keyCoordinates.getInt("x3",0) + "");
            x4.setText(keyCoordinates.getInt("x4",0) + "");
            x5.setText(keyCoordinates.getInt("x5",0) + "");
            x6.setText(keyCoordinates.getInt("x6",0) + "");
            x7.setText(keyCoordinates.getInt("x7",0) + "");
            y1.setText(keyCoordinates.getInt("y1",0) + "");
            y2.setText(keyCoordinates.getInt("y2",0) + "");
            y3.setText(keyCoordinates.getInt("y3",0) + "");
        }


        Button startSettings = findViewById(R.id.launch_setting);
        startSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getPackageManager().getLaunchIntentForPackage("com.android.settings")));
            }
        });
        //保存坐标
        Button saveCoordinates = findViewById(R.id.save_coordinates);
        saveCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    keyCoordinates.edit().putInt("x1", Integer.parseInt(x1.getText().toString())).
                            putInt("x2", Integer.parseInt(x2.getText().toString())).
                            putInt("x3", Integer.parseInt(x3.getText().toString())).
                            putInt("x4", Integer.parseInt(x4.getText().toString())).
                            putInt("x5", Integer.parseInt(x5.getText().toString())).
                            putInt("x6", Integer.parseInt(x6.getText().toString())).
                            putInt("x7", Integer.parseInt(x7.getText().toString())).
                            putInt("y1", Integer.parseInt(y1.getText().toString())).
                            putInt("y2", Integer.parseInt(y2.getText().toString())).
                            putInt("y3", Integer.parseInt(y3.getText().toString())).commit();
                    Toast.makeText(AdjustAndStartActivity.this, R.string.saved_successfully,Toast.LENGTH_LONG).show();
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.save_failed).setMessage(R.string.save_failed_msg).setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                }
            }
        });
        Button analyze = findViewById(R.id.analyze);
        //自助移调
        analyze.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final ProgressDialog analyzing = new ProgressDialog(AdjustAndStartActivity.this).show(AdjustAndStartActivity.this, getString(R.string.analyzing), getString(R.string.analyzing_msg), false, true);
                analyzing.show();
                final String[] tanspositionString = {"+11","+10","+9","+8","+7", "+6", "+5", "+4", "+3", "+2", "+1", "0", "-1", "-2", "-3", "-4", "-5", "-6", "-7","-8","-9","-10","-11"};

                final Handler hand = new Handler(Looper.myLooper()) {
                    public void handleMessage(Message msg) {
                        analyzing.dismiss();
                        if (msg.what == 1) {
                            new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.please_select_an_offset).setSingleChoiceItems(tanspositionString, tansposition , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    currentTansposition = i;
                                }

                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    tansposition = currentTansposition;
                                    tanspositionText.setText(getString(R.string.note_transposition) + ((11 - tansposition) > 0 ? "+":"") + (11 - tansposition));
                                }
                            }).show();
                        }else if(msg.what == 0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.file_data_is_corrupted).setMessage(getString(R.string.file_data_is_corrupted_msg) + fileHead4Byte() + "正常为：4d 54 68 64").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    });
                                    alertDialog.setCancelable(false);
                                    alertDialog.show();
                                }
                            });
                        }
                    }
                };

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Sequence midi = MidiSystem.getSequence(getContentResolver().openInputStream(midiUri));
                            ArrayList<Note> noteList = new ArrayList<>();
                            Track[] midiTracks = midi.getTracks();
                            for (int t = 0; t < midiTracks.length; t++) {
                                for (int i = 0; i < midiTracks[t].size(); ++i) {
                                    MidiEvent midiEvent = midiTracks[t].get(i);
                                    byte[] midiMessage = midiEvent.getMessage().getMessage();
                                    if (midiMessage.length == 3) {
                                        if (midiMessage[0] >= -112 && midiMessage[0] <= -97) {
                                            if (midiMessage[2] != 0) {
                                                //类型：音符按下
                                                noteList.add(new Note(midiEvent.getTick(), midiMessage[1], true));
                                            } else {
                                                //类型：音符松开
                                                noteList.add(new Note(midiEvent.getTick(), midiMessage[1], false));
                                            }
                                        } else if (midiMessage[0] >= -128 && midiMessage[0] <= -113) {
                                           //类型：音符松开
                                            noteList.add(new Note(midiEvent.getTick(), midiMessage[1], false));
                                        }
                                    }
                                    Collections.sort(noteList);
                                }
                            }
                            //分析黑键数
                            for (int i = 0; i < tanspositionString.length; i++) {
                                int blackKeyQuantity = 0;
                                for(Note note:noteList){
                                    if(Note.isBlackKey(note.getNote() + (11 - i))){
                                        blackKeyQuantity++;
                                    }
                                }
                                tanspositionString[i] += ("  " + getString(R.string.black_quantity) + (blackKeyQuantity / 2));
                            }
                            hand.sendEmptyMessage(1);

                        } catch (final InvalidMidiDataException e) {
                            e.printStackTrace();
                            final String s = e.toString();
                            if (e.getMessage() != "Invalid header") {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.error_reading_MIDI).setMessage(s).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finish();
                                            }
                                        });
                                        alertDialog.setCancelable(false);
                                        alertDialog.show();
                                    }
                                });
                            }else {
                                hand.sendEmptyMessage(0);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            final String s = e.toString();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.exception_details).setMessage(s).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    });
                                    alertDialog.setCancelable(false);
                                    alertDialog.show();
                                }
                            });
                        }
                    }
                }).start();

            }
        });
        //保存到列表
        Button save_to_list = findViewById(R.id.save_to_list);
        save_to_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(AdjustAndStartActivity.this, R.layout.edit_text, null);
                final EditText text = view.findViewById(R.id.edit_text);
                text.setText(midiName.replace(".mid",""));
                new AlertDialog.Builder(AdjustAndStartActivity.this)
                        .setTitle("请输入你要保存的名字")
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 获取输入框的内容
                                processAndSave(text.getText().toString());
                               // Toast.makeText(AdjustAndStartActivity.this, text.getText().toString() + "  已保存到播放列表", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

    }

    public void setSpinner(Spinner spinner, int textArrayResId, int selection) {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, textArrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selection);


    }

    //处理midi&打开悬浮窗
    @Override
    public void onClick(View view) {
        if(!keyCoordinates.contains("x1")){
            new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.Error_coordinates_are_empty).setMessage(R.string.Error_coordinates_are_empty_msg).setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
    }else{
        //判断是否有悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this).setTitle(R.string.floating_window_permission_is_required).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
            if (!ClickService.isStart()) {
                new AlertDialog.Builder(this).setTitle(R.string.Accessibility_required).setMessage(R.string.Accessibility_required_msg).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            AdjustAndStartActivity.this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        } catch (Exception e) {
                            AdjustAndStartActivity.this.startActivity(new Intent(Settings.ACTION_SETTINGS));
                            e.printStackTrace();
                        }
                    }
                }).show();
            } else {
                midi_info.edit().putBoolean("isMusicList",false).commit();
                processAndSave("lyreNotes");
            }


        }
    }
    }
    public String fileHead4Byte(){
        String _4byte = "";
        try {
            InputStream inputStream = getContentResolver().openInputStream(midiUri);
            for (int i = 0; i < 4; i++) {
                _4byte += (Integer.toHexString(inputStream.read() & 0xff) + " ");
            }
            return _4byte;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public void processAndSave(final String saveName){
        final ProgressDialog loading = new ProgressDialog(AdjustAndStartActivity.this).show(AdjustAndStartActivity.this, getString(R.string.Processing_MIDI_data), getString(R.string.Processing_MIDI_data_msg), false, true);
        loading.show();
        final Handler hand = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                loading.dismiss();
                if (msg.what == 1) {
                    Intent intent = new Intent(AdjustAndStartActivity.this, FloatingButtonService.class);
                    startService(intent);
                } else if (msg.what == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.file_data_is_corrupted).setMessage(getString(R.string.file_data_is_corrupted_msg) + fileHead4Byte() + "正常为：4d 54 68 64").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        }
                    });
                }
            }
        };
        //根据设定值处理midi数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    StringBuffer text = new StringBuffer();
                    StringBuffer text2 = new StringBuffer();
                    Sequence midi;
                    ArrayList<Note> noteList = new ArrayList<>();
                    midi = MidiSystem.getSequence(getContentResolver().openInputStream(midiUri));
                    Track[] midiTracks = midi.getTracks();
                    final String midiTracksLength = midiTracks.length + "";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AdjustAndStartActivity.this, (getString(R.string.read_midi_success_and_track_quantity) + midiTracksLength), Toast.LENGTH_SHORT).show();
                        }
                    });
                    for (int t = 0; t < midiTracks.length; t++) {
                        for (int i = 0; i < midiTracks[t].size(); ++i) {
                            byte[] midiMessage;
                            MidiEvent midiEvent = midiTracks[t].get(i);
                            text.append("tick:" + midiEvent.getTick() + " data:");
                            for (byte b : midiMessage = midiEvent.getMessage().getMessage()) {
                                text.append(Integer.toHexString(b & 0xFF) + " ");
                            }
                            if (midiMessage.length == 3) {
                                if (midiMessage[0] >= -112 && midiMessage[0] <= -97) {
                                    //这行判断代码迫于无奈，测试读取midi文件时发现某个midi文件全部只有音符按下没有松开，得重新判断
                                    if (midiMessage[2] != 0) {
                                        text.append("类型：音符按下");
                                        noteList.add(new Note(midiEvent.getTick(), midiMessage[1], true));
                                    } else {
                                        text.append("类型：音符松开");
                                        noteList.add(new Note(midiEvent.getTick(), midiMessage[1], false));
                                    }

                                } else if (midiMessage[0] >= -128 && midiMessage[0] <= -113) {
                                    text.append("类型：音符松开");
                                    noteList.add(new Note(midiEvent.getTick(), midiMessage[1], false));
                                }
                            }
                            //当遇到BPM事件时，设置整首歌的BPM，但不支持动态改变，故仅读一次BPM值,然后再此序列的时序分辨率。
                            if (speed == -1 && midiMessage.length == 6 && midiMessage[0] == -1 && midiMessage[1] == 81 && midiMessage[2] == 3) {
                                speed = (500 / (float) midi.getResolution()) * (120 / (60000000 / (float) (((midiMessage[3] & 0xFF) * 256 * 256) + ((midiMessage[4] & 0xFF) * 256) + (midiMessage[5] & 0xFF))));
                                //判断是想要直接打开悬浮窗还是保存到悬浮列表里（速度）
                                if(saveName.equals("lyreNotes")) {
                                    midi_info.edit().putFloat("speed", speed).putString("midi_name",midiName).commit();
                                }else{
                                    music_speed_list.edit().putFloat(saveName,speed).commit();
                                }
                            }
                            text.append("\n");
                        }
                        text.append("==============\n");
                    }
                    //合并音轨并排序音符
                    Collections.sort(noteList);
                    //遍历音符列表,并按照调好的配置写入lyreNotes（原神琴音符[21~1]）

                    ArrayList<Note> lyreNotes = new ArrayList<>();
                    for (Note note : noteList) {
                        if (note.type()) {
                            text2.append(note.toString() + "\n");
                            //88键 ======> 36键 ========> 21键
                            int note_36 = to36Key(note.getNote() + (11 - tansposition));
                            int[] lyreKeys = toLyreKey(note_36);
                            if (lyreKeys != null) {
                                for (int lyreKey : lyreKeys) {
                                    lyreNotes.add(new Note(note.getTick(), (byte) lyreKey, note.type()));
                                }
                            }
                        }
                    }
                    System.out.println(text);
                    //将处理好的音符存盘,如果没有要求保存到列悬浮表里就放缓存目录
                    File lyreNotesFile = null;
                    if(saveName.equals("lyreNotes")) {
                        lyreNotesFile = getCacheDir();
                        lyreNotesFile = new File(lyreNotesFile, "lyreNotes");
                        midi_info.edit().putString("midi_name",midiName).commit();
                    }else{
                        lyreNotesFile = getFilesDir();
                        lyreNotesFile = new File(lyreNotesFile, "music_list");
                        if(!lyreNotesFile.exists()){
                            lyreNotesFile.mkdir();
                        }
                        lyreNotesFile = new File(lyreNotesFile,saveName);
                        midi_info.edit().putString("midi_name",saveName).commit();
                    }
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(lyreNotesFile));
                    oos.writeObject(lyreNotes);
                    oos.flush();
                    oos.close();
                    if(saveName.equals("lyreNotes")) {
                        hand.sendEmptyMessage(1);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle("文件未找到").setMessage("返回URI类型不规范！请勿使用第三方（包括厂商系统的）文件管理器选择文件！错误的URI：").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    final String s = e.toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.exception_details).setMessage(s).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        }
                    });
                } catch (final InvalidMidiDataException e) {
                    e.printStackTrace();
                    final String s = e.toString();
                    if (e.getMessage() != "Invalid header") {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.error_reading_MIDI).setMessage(s).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                                alertDialog.setCancelable(false);
                                alertDialog.show();
                            }
                        });
                    }else {
                        hand.sendEmptyMessage(0);
                    }

                }
            }

        }).start();
    }
    //按照钢琴按键对应原神键位的范围的设定，将钢琴88个音符压缩成12*3=36个音符，但还包含半音
    public int to36Key(int noteNum){
        noteNum -= 47;
        if (noteNum <= -24) {
            return 1;
        } else if (noteNum >= -23 && noteNum <= -12) {
            return noteNum + 24;
        } else if (noteNum >= -11 && noteNum <= 0) {
            return noteNum + 12;
        } else if (noteNum >= 1 && noteNum <= 12) {
            if (spinner3.getSelectedItemPosition() == 0) {
                return noteNum;
            } else {
                return noteNum + 12;
            }
        } else if (noteNum >= 13 && noteNum <= 24) {
            if (spinner4.getSelectedItemPosition() == 0) {
                return noteNum - 12;
            } else if (spinner4.getSelectedItemPosition() == 1) {
                return noteNum;
            } else {
                return noteNum + 12;
            }
        } else if (noteNum >= 25 && noteNum <= 36) {
            if (spinner5.getSelectedItemPosition() == 0) {
                return noteNum - 24;
            } else if (spinner5.getSelectedItemPosition() == 1) {
                return noteNum - 12;
            } else {
                return noteNum;
            }
        } else if (noteNum >= 37 && noteNum <= 48) {
            if (spinner6.getSelectedItemPosition() == 0) {
                return noteNum - 24;
            } else {
                return noteNum - 12;
            }
        } else if (noteNum >= 49 && noteNum <= 60) {
            return noteNum - 24;
        }
        return -1;
    }
    //根据黑键的设定，36键进一步压缩成21键，21键即原神琴的键数
    public int[] toLyreKey(int noteNum){
        int magnification = 0;
        int noteNum_12 = 1;
        if (noteNum <= 12){ magnification = 0;noteNum_12 = noteNum;}
        else if(noteNum >= 13 && noteNum <= 24){magnification = 1;noteNum_12 = noteNum - 12;}
        else if(noteNum >= 25 && noteNum <= 36){magnification = 2;noteNum_12 = noteNum - 24;}

        if     (noteNum_12 == 1){return new int[]{1 + (7 * magnification)};}
        else if(noteNum_12 == 2){return blackKey(1,magnification);}
        else if(noteNum_12 == 3){return new int[]{2 + (7 * magnification)};}
        else if(noteNum_12 == 4){return blackKey(2,magnification);}
        else if(noteNum_12 == 5){return new int[]{3 + (7 * magnification)};}
        else if(noteNum_12 == 6){return new int[]{4 + (7 * magnification)};}
        else if(noteNum_12 == 7){return blackKey(4,magnification);}
        else if(noteNum_12 == 8){return new int[]{5 + (7 * magnification)};}
        else if(noteNum_12 == 9){return blackKey(5,magnification);}
        else if(noteNum_12 ==10){return new int[]{6 + (7 * magnification)};}
        else if(noteNum_12 ==11){return blackKey(6,magnification);}
        else if(noteNum_12 ==12){return new int[]{7 + (7 * magnification)};}
        return null;
    }
    //按照黑键的设定，按照黑键左边的白键为索引
    private int[] blackKey(int noteNum_7,int magnification){
        switch (blackKeySettings.getCheckedRadioButtonId()){
            case R.id.cb1:
                return new int[]{noteNum_7 + (7 * magnification),(noteNum_7 + 1) + (7 * magnification)};
            case R.id.cb2:
                return new int[]{noteNum_7 + (7 * magnification)};
            case R.id.cb3:
                return new int[]{(noteNum_7 + 1) + (7 * magnification)};
            case R.id.cb4:
                return null;
        }
        return null;
    }


    }

