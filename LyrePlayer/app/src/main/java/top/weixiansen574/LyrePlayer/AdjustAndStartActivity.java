
package top.weixiansen574.LyrePlayer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;

import top.weixiansen574.LyrePlayer.enums.InvalidKeySetting;
import top.weixiansen574.LyrePlayer.enums.MusicInstrumentType;
import top.weixiansen574.LyrePlayer.midi.MidiProcessor;
import top.weixiansen574.LyrePlayer.midi.Note;
import top.weixiansen574.LyrePlayer.util.AccessibilityUtil;
import top.weixiansen574.LyrePlayer.util.NoteListStorage;

public class AdjustAndStartActivity extends AppCompatActivity implements View.OnClickListener {
    Spinner spinner1;
    Spinner spinner2;
    Spinner spinner3;
    Spinner spinner4;
    Spinner spinner5;
    Spinner spinner6;
    Spinner spinner7;
    RadioGroup blackKeySettings;
    RadioGroup rg_musicInstrumentType;
    float speed = -1;
    int transposition = 11;
    int currentTransposition = -1;
    SharedPreferences midi_info;
    SharedPreferences keyCoordinates;
    SharedPreferences music_speed_list;
    static Uri midiUri;
    static String midiName;
    FloatListManager floatListManager;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //工具栏返回上一级按钮
        if (item.getItemId() == 16908332) {
            finish();
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            midiUri = data.getData();
            String path = midiUri.getPath();
            midiName = path.substring(path.lastIndexOf("/") + 1);
            Toast.makeText(this, "" + getString(R.string.filename) + midiName, Toast.LENGTH_SHORT).show();
            if (!midiName.endsWith(".mid")) {
                //检测文件名
                new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(getString(R.string.file_mame_waning)).setMessage(getString(R.string.file_name_warning_message) + midiName + getString(R.string.file_name_warning_message1)).setPositiveButton(R.string.show_help, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(AdjustAndStartActivity.this, FileHelp.class));
                    }
                }).setNeutralButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
            }
        } catch (RuntimeException e) {
            //如果没有选择到文件
            e.printStackTrace();
            new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.nmyxzwj).setMessage(R.string.nmyxzwj_msg).setPositiveButton(getString(R.string.show_help), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    startActivity(new Intent(AdjustAndStartActivity.this, FileHelp.class));
                }
            }).setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).setCancelable(false).show();

        }
        System.out.println("result uri:" + midiUri);


    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断是否是旋转屏幕
        if (savedInstanceState == null) {
            Intent intent0 = getIntent();
            if (!intent0.getBooleanExtra("open_file", false)) {
                //intent调用系统文件管理器，选择midi文件
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/midi");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            } else {
                //被intent启动，直接从URI里获取input stream
                midiUri = intent0.getData();
                String path = midiUri.getEncodedPath();
                midiName = path.substring(path.lastIndexOf("/") + 1);
                midiName = Uri.decode(midiName);
                System.out.println(fileHead4Byte());
                //判断是否是midi文件
                if (!fileHead4Byte().equals("4d 54 68 64 ")) {
                    new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.stxzfmidiwj).setMessage(R.string.stxzfmidiwj_msg).setPositiveButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).setCancelable(false).show();
                }
                Toast.makeText(this, getString(R.string.filename) + midiName, Toast.LENGTH_SHORT).show();
            }
        }
        this.setContentView(R.layout.activity_adjust_and_start_new);
        midi_info = getSharedPreferences("midi_info", Context.MODE_PRIVATE);
        music_speed_list = getSharedPreferences("music_speed_list", Context.MODE_PRIVATE);
        keyCoordinates = getSharedPreferences("key_coordinates", Context.MODE_PRIVATE);
        floatListManager = new FloatListManager(AdjustAndStartActivity.this);

        TextView textView = (TextView) findViewById(R.id.textView);
        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);
        final TextView tanspositionText = findViewById(R.id.tansposition_text);

        spinner1 = (Spinner) this.findViewById(R.id.spinner1);
        spinner2 = (Spinner) this.findViewById(R.id.spinner2);
        spinner3 = (Spinner) this.findViewById(R.id.spinner3);
        spinner4 = (Spinner) this.findViewById(R.id.spinner4);
        spinner5 = (Spinner) this.findViewById(R.id.spinner5);
        spinner6 = (Spinner) this.findViewById(R.id.spinner6);
        spinner7 = (Spinner) this.findViewById(R.id.spinner7);
        this.setSpinner(spinner1, R.array.spinner1_array, 0);
        this.setSpinner(spinner2, R.array.spinner2_array, 0);
        this.setSpinner(spinner3, R.array.spinner3_array, 0);
        this.setSpinner(spinner4, R.array.spinner4_array, 1);
        this.setSpinner(spinner5, R.array.spinner5_array, 2);
        this.setSpinner(spinner6, R.array.spinner6_array, 1);
        this.setSpinner(spinner7, R.array.spinner7_array, 0);
        blackKeySettings = (RadioGroup) findViewById(R.id.black_key_settings);
        rg_musicInstrumentType = findViewById(R.id.music_instrument_type);
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

        if (keyCoordinates.contains("x1")) {
            x1.setText(keyCoordinates.getInt("x1", 0) + "");
            x2.setText(keyCoordinates.getInt("x2", 0) + "");
            x3.setText(keyCoordinates.getInt("x3", 0) + "");
            x4.setText(keyCoordinates.getInt("x4", 0) + "");
            x5.setText(keyCoordinates.getInt("x5", 0) + "");
            x6.setText(keyCoordinates.getInt("x6", 0) + "");
            x7.setText(keyCoordinates.getInt("x7", 0) + "");
            y1.setText(keyCoordinates.getInt("y1", 0) + "");
            y2.setText(keyCoordinates.getInt("y2", 0) + "");
            y3.setText(keyCoordinates.getInt("y3", 0) + "");
        } else {
            //自动填写坐标，根据屏幕分辨率
            try {
                InputStream inputStream = getResources().getAssets().open("ResolutionCoordinateMapping.json");
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer, 0, inputStream.available());
                String JSONString = new String(buffer);
                JSONObject coordinateMappingJSON = JSON.parseObject(JSONString);
                String resolution = getResolution();
                if (coordinateMappingJSON.containsKey(resolution)) {
                    Toast.makeText(this, "已根据您的屏幕分辨率：" + resolution + "自动填写坐标！（需自行保存）", Toast.LENGTH_LONG).show();
                    JSONObject coordinatesJSON = coordinateMappingJSON.getJSONObject(resolution);
                    x1.setText(coordinatesJSON.getString("x1"));
                    x2.setText(coordinatesJSON.getString("x2"));
                    x3.setText(coordinatesJSON.getString("x3"));
                    x4.setText(coordinatesJSON.getString("x4"));
                    x5.setText(coordinatesJSON.getString("x5"));
                    x6.setText(coordinatesJSON.getString("x6"));
                    x7.setText(coordinatesJSON.getString("x7"));
                    y1.setText(coordinatesJSON.getString("y1"));
                    y2.setText(coordinatesJSON.getString("y2"));
                    y3.setText(coordinatesJSON.getString("y3"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    Toast.makeText(AdjustAndStartActivity.this, R.string.saved_successfully, Toast.LENGTH_LONG).show();
                } catch (NumberFormatException e) {
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
                final String[] tanspositionString = {"+11", "+10", "+9", "+8", "+7", "+6", "+5", "+4", "+3", "+2", "+1", "0", "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10", "-11"};

                final Handler hand = new Handler(Looper.myLooper()) {
                    public void handleMessage(Message msg) {
                        analyzing.dismiss();
                        if (msg.what == 1) {
                            new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.please_select_an_offset).setSingleChoiceItems(tanspositionString, transposition, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    currentTransposition = i;
                                }

                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (currentTransposition == -1) {
                                        transposition = 11;
                                    } else {
                                        transposition = currentTransposition;
                                    }
                                    tanspositionText.setText(getString(R.string.note_transposition) + ((11 - transposition) > 0 ? "+" : "") + (11 - transposition));
                                }
                            }).show();
                        } else if (msg.what == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.file_data_is_corrupted).setMessage(getString(R.string.tshbsyxdmidiwj) + fileHead4Byte() + "正常为：4d 54 68 64").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
                Thread analyzeBlackKeyThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Note> noteArrayList = MidiProcessor.processorToNoteListAndHandleExceptions(AdjustAndStartActivity.this, getContentResolver(), midiUri);
                        if (noteArrayList != null) {
                            for (int i = 0; i < tanspositionString.length; i++) {
                                int blackKeyQuantity = 0;
                                if (getMusicInstrumentType() == MusicInstrumentType.lyre) {
                                    blackKeyQuantity = MidiProcessor.analyzeBlackKeyQuantity(noteArrayList, (11 - i));
                                } else if (getMusicInstrumentType() == MusicInstrumentType.oldLyre) {
                                    blackKeyQuantity = MidiProcessor.analyzeInvalidKeyQuantityForOldLyre(noteArrayList, (11 - i), getSpinnerSettings());
                                }
                                tanspositionString[i] += ("  " + getString(R.string.black_quantity) + (blackKeyQuantity / 2));
                            }
                            hand.sendEmptyMessage(1);
                        }
                    }
                });
                analyzeBlackKeyThread.setName("analyzeBlackKey");
                analyzeBlackKeyThread.start();
            }

        });
        analyze.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new Thread(() -> {
                    try {
                        MidiProcessor.toNoteList(getContentResolver().openInputStream(midiUri));
                    } catch (InvalidMidiDataException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                return false;
            }
        });
        //保存到列表
        Button save_to_list = findViewById(R.id.save_to_list);
        save_to_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(AdjustAndStartActivity.this, R.layout.edit_text, null);
                final EditText text = view.findViewById(R.id.edit_text);
                text.setText(midiName.replace(".mid", ""));
                new AlertDialog.Builder(AdjustAndStartActivity.this)
                        .setTitle("请输入你要保存的名字")
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 获取输入框的内容
                                Thread processMidiThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayList<Note> noteList = null;
                                        if (getMusicInstrumentType() == MusicInstrumentType.lyre) {
                                            noteList = MidiProcessor.toLyreNoteList(MidiProcessor.processorToNoteListAndHandleExceptions(AdjustAndStartActivity.this, getContentResolver(), midiUri), transposition, getSpinnerSettings(), getInvalidKeySettings());
                                        } else if (getMusicInstrumentType() == MusicInstrumentType.oldLyre){
                                            noteList = MidiProcessor.toOldLyreNoteList(MidiProcessor.processorToNoteListAndHandleExceptions(AdjustAndStartActivity.this, getContentResolver(), midiUri), transposition, getSpinnerSettings(), getInvalidKeySettings());
                                        }
                                        if (floatListManager.insertMusic(text.getText().toString(),getMusicInstrumentType() == MusicInstrumentType.lyre ? FloatListManager.MI_TYPE_LYRE : FloatListManager.MI_TYPE_OLD_LYRE, noteList)) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(AdjustAndStartActivity.this, text.getText().toString() + "  已保存到播放列表", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(AdjustAndStartActivity.this, text.getText().toString() + "  保存失败", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                });
                                processMidiThread.setName("processMidi");
                                processMidiThread.start();
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
        if (!keyCoordinates.contains("x1")) {
            new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(R.string.Error_coordinates_are_empty).setMessage(R.string.Error_coordinates_are_empty_msg).setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        } else {
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
                if (AccessibilityUtil.checkPermission(AdjustAndStartActivity.this)) {
                    midi_info.edit().putBoolean("isMusicList", false).commit();
                    Thread processMidiThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Note> noteList = null;
                            if (getMusicInstrumentType() == MusicInstrumentType.lyre) {
                                noteList = MidiProcessor.toLyreNoteList(MidiProcessor.processorToNoteListAndHandleExceptions(AdjustAndStartActivity.this, getContentResolver(), midiUri), transposition, getSpinnerSettings(), getInvalidKeySettings());
                            } else if (getMusicInstrumentType() == MusicInstrumentType.oldLyre){
                                noteList = MidiProcessor.toOldLyreNoteList(MidiProcessor.processorToNoteListAndHandleExceptions(AdjustAndStartActivity.this, getContentResolver(), midiUri), transposition, getSpinnerSettings(), getInvalidKeySettings());
                            }
                            long noteListId = NoteListStorage.putNoteList(noteList);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(AdjustAndStartActivity.this, FloatingButtonService.class);
                                    intent.putExtra("name", midiName);
                                    intent.putExtra("noteListKey", noteListId);
                                    startService(intent);
                                }
                            });
                        }
                    });
                    processMidiThread.setName("processMidi");
                    processMidiThread.start();
                    //processAndSave("lyreNotes");
                }
            }
        }
    }

    public String fileHead4Byte() {
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

    public int[] getSpinnerSettings() {
        int[] settings = new int[7];//{1,1,1,2,3,3,3};
        if (spinner1.getSelectedItemPosition() == 0) {
            settings[0] = 1;
        } else if (spinner1.getSelectedItemPosition() == 1) {
            settings[0] = -1;
        }
        if (spinner2.getSelectedItemPosition() == 0) {
            settings[1] = 1;
        } else if (spinner2.getSelectedItemPosition() == 1) {
            settings[1] = -1;
        }
        if (spinner3.getSelectedItemPosition() == 0) {
            settings[2] = 1;
        } else if (spinner3.getSelectedItemPosition() == 1) {
            settings[2] = 2;
        }

        if (spinner4.getSelectedItemPosition() == 0) {
            settings[3] = 1;
        } else if (spinner4.getSelectedItemPosition() == 1) {
            settings[3] = 2;
        } else if (spinner4.getSelectedItemPosition() == 2) {
            settings[3] = 3;
        }

        if (spinner5.getSelectedItemPosition() == 0) {
            settings[4] = 1;
        } else if (spinner5.getSelectedItemPosition() == 1) {
            settings[4] = 2;
        } else if (spinner5.getSelectedItemPosition() == 2) {
            settings[4] = 3;
        }

        if (spinner6.getSelectedItemPosition() == 0) {
            settings[5] = 2;
        } else if (spinner6.getSelectedItemPosition() == 1) {
            settings[5] = 3;
        } else if (spinner6.getSelectedItemPosition() == 2) {
            settings[5] = -1;
        }
        if (spinner7.getSelectedItemPosition() == 0) {
            settings[6] = 1;
        } else if (spinner7.getSelectedItemPosition() == 1) {
            settings[6] = -1;
        }
        return settings;
    }

    private InvalidKeySetting getInvalidKeySettings() {
        switch (blackKeySettings.getCheckedRadioButtonId()) {
            case R.id.cb1:
                return InvalidKeySetting.leftAndRight;
            case R.id.cb2:
                return InvalidKeySetting.left;
            case R.id.cb3:
                return InvalidKeySetting.right;
            case R.id.cb4:
                return InvalidKeySetting.no;
        }
        return InvalidKeySetting.no;
    }

    private MusicInstrumentType getMusicInstrumentType() {
        switch (rg_musicInstrumentType.getCheckedRadioButtonId()) {
            case R.id.cb_lyre:
                return MusicInstrumentType.lyre;
            case R.id.cb_old_lrye:
                return MusicInstrumentType.oldLyre;
        }
        return MusicInstrumentType.lyre;
    }


    private void dialog(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdjustAndStartActivity.this).setTitle(title).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alertDialog.show();
            }
        });
    }

    public String getResolution() {
        WindowManager windowManager = getWindow().getWindowManager();
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        int width = point.x;
        int height = point.y;
        return height + "*" + width;
    }

}

