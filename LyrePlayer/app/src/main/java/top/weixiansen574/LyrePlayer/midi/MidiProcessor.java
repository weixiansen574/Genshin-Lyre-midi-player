package top.weixiansen574.LyrePlayer.midi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.NotIsMidiFileException;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import top.weixiansen574.LyrePlayer.R;
import top.weixiansen574.LyrePlayer.enums.BlackKeySetting;

public class MidiProcessor {

    public static ArrayList<Note> toNoteList(InputStream inputStream) throws IOException, NotIsMidiFileException {
        MidiFile midi = new MidiFile(inputStream);
        List<MidiEvent> midiEventList = new ArrayList<>();
        ArrayList<Note> noteList = new ArrayList<>();
        List<MidiTrack> midiTracks = midi.getTracks();

        //提取midi所有midi事件
        for (int t = 0; t < midi.getTrackCount(); t++) {
            midiEventList.addAll(midiTracks.get(t).getEvents());
        }

        //按照tick排序midiEvent(合并音轨)
        Collections.sort(midiEventList, new Comparator<MidiEvent>() {
            @Override
            public int compare(MidiEvent o1, MidiEvent o2) {
                return (int) (o1.getTick() - o2.getTick());
            }
        });

        //
        long lastTick = 0;
        long lastTick1 = 0;
        long currentTick = 0;
        float speed = 1;
        for (MidiEvent midiEvent : midiEventList) {
            // System.out.println(midiEvent);
            //音符打开事件
            if (midiEvent instanceof NoteOn) {
                NoteOn noteOn = (NoteOn) midiEvent;
                currentTick = (long) (((midiEvent.getTick() - lastTick) * speed) + lastTick1);
                noteList.add(new Note(currentTick, (byte) noteOn.getNoteValue(), true));
                lastTick = midiEvent.getTick();
                lastTick1 = currentTick;
            }
            //音符关闭事件
            if (midiEvent instanceof NoteOff) {
                NoteOff noteOff = (NoteOff) midiEvent;
                currentTick = (long) (((midiEvent.getTick() - lastTick) * speed) + lastTick1);
                noteList.add(new Note((long) (midiEvent.getTick() * speed), (byte) noteOff.getNoteValue(), false));
                lastTick = midiEvent.getTick();
                lastTick1 = currentTick;
            }

            //读取到BPM事件时调整当前速度（动态改变，旧版不支持）
            if (midiEvent instanceof Tempo) {
                Tempo tempo = (Tempo) midiEvent;
                speed = (500 / (float) midi.getResolution()) * (120 / tempo.getBpm());
            }

        }

        //for (Note note : noteList){
        //     System.out.println(note);
        // }

        //合并音轨并排序音符
        //Collections.sort(noteList);
        //遍历音符列表,并按照调好的配置写入lyreNotes（原神琴音符[21~1]）
        inputStream.close();
        return noteList;
    }

    public static ArrayList<Note> toLyreNoteList (ArrayList<Note> noteList, int transposition, int[] sp, BlackKeySetting blackKeySetting){
        ArrayList<Note> lyreNotes = new ArrayList<>();
        for (Note note : noteList) {
            if (note.isNoteOn()) {
                //88键 ======> 36键 ========> 21键
                int note_36 = to36Key(note.getValue() + (11 - transposition), sp);
                if (note_36 != -1){
                    int[] lyreKeys = toLyreKey(note_36,blackKeySetting);
                    if (lyreKeys != null) {
                        for (int lyreKey : lyreKeys) {
                            lyreNotes.add(new Note(note.getTick(), (byte) lyreKey, note.isNoteOn()));
                        }
                    }
                }
            }
        }
        return lyreNotes;
    }

    public static ArrayList<Note> toOldLyreList(ArrayList<Note> noteList, int transposition, int[] sp, BlackKeySetting blackKeySetting){
        ArrayList<Note> lyreNotes = new ArrayList<>();
        for (Note note : noteList) {
            if (note.isNoteOn()) {
                //88键 ======> 36键 ========> 21键
                int note_36 = to36Key(note.getValue() + (11 - transposition), sp);
                if (note_36 != -1){
                    int[] lyreKeys = toOldLyreKey(note_36,blackKeySetting);
                    if (lyreKeys != null) {
                        for (int lyreKey : lyreKeys) {
                            lyreNotes.add(new Note(note.getTick(), (byte) lyreKey, note.isNoteOn()));
                        }
                    }
                }
            }
        }
        return lyreNotes;
    }

    public static int analyzeBlackKeyQuantity(ArrayList<Note> notes,int transposition){
        int[] blackKey = {22,25,27,30,32,34,37,39,42,44,46,49,51,54,56,58,58,61,63,66,68,70,73,75,78,80,82,85,87,90,92,94,97,99,102,104,106};
        int blackKeyQuantity = 0;
        for (Note note : notes) {
            if (note.isNoteOn()) {
                for (int keyNum : blackKey) {
                    if (note.getValue() + transposition == keyNum) {
                        blackKeyQuantity++;
                    }
                }
            }
        }
        return blackKeyQuantity;
    }

    public static int analyzeInvalidKeyQuantityForOldLyre(ArrayList<Note> notes, int transposition, int[] sp){
        int[] blackKey = {2,5,7,9,12,14,17,19,21,24,27,29,31,34,36};
        int blackKeyQuantity = 0;
        for (Note note : notes) {
            if (note.isNoteOn()) {
                for (int keyNum : blackKey) {
                    if (to36Key(note.getValue() + transposition,sp) == keyNum) {
                        blackKeyQuantity++;
                    }
                }
            }
        }
        return blackKeyQuantity;
    }

    public static ArrayList<Note> processorToNoteListAndHandleExceptions(Activity activity, ContentResolver contentResolver, Uri uri){
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            return toNoteList(inputStream);
        } catch (final IOException e) {
            //其他IO异常
            dialog(activity,activity.getString(R.string.exception_details),e.toString());
        } catch (NotIsMidiFileException e) {
            //打开的midi损坏或不是midi文件
            dialog(activity,activity.getString(R.string.file_data_is_corrupted),activity.getString(R.string.tshbsyxdmidiwj));
        }
        return null;
    }

    private static void dialog(final Activity activity, final String title, final String message){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity).setTitle(title).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.finish();
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        });
    }

    //按照钢琴按键对应原神键位的范围的设定，将钢琴88个音符压缩成12*3=36个音符，但还包含黑键
    private static int to36Key(int noteValve, int[] sp) {
        //钢琴有88键，选择7组 7x12=84
        noteValve -= 24;
        int grepNumber = noteValve / 12;
        if (sp[grepNumber] != -1) {
            return noteValve - (grepNumber * 12) + ((sp[grepNumber] - 1) * 12) + 1;
        } else {
            return -1;
        }
    }


    //根据黑键的设定，36键进一步压缩成21键，21键即原神琴的键数
    private static int[] toLyreKey(int noteValue, BlackKeySetting blackKeySetting) {
        int magnification = 0;
        int noteNum_12 = 1;
        if (noteValue <= 12) {
            noteNum_12 = noteValue;
        } else if (noteValue <= 24) {
            magnification = 1;
            noteNum_12 = noteValue - 12;
        } else if (noteValue <= 36) {
            magnification = 2;
            noteNum_12 = noteValue - 24;
        }

        if (noteNum_12 == 1) {
            return new int[]{1 + (7 * magnification)};
        } else if (noteNum_12 == 2) {
            return blackKey(1, magnification,blackKeySetting);
        } else if (noteNum_12 == 3) {
            return new int[]{2 + (7 * magnification)};
        } else if (noteNum_12 == 4) {
            return blackKey(2, magnification,blackKeySetting);
        } else if (noteNum_12 == 5) {
            return new int[]{3 + (7 * magnification)};
        } else if (noteNum_12 == 6) {
            return new int[]{4 + (7 * magnification)};
        } else if (noteNum_12 == 7) {
            return blackKey(4, magnification,blackKeySetting);
        } else if (noteNum_12 == 8) {
            return new int[]{5 + (7 * magnification)};
        } else if (noteNum_12 == 9) {
            return blackKey(5, magnification,blackKeySetting);
        } else if (noteNum_12 == 10) {
            return new int[]{6 + (7 * magnification)};
        } else if (noteNum_12 == 11) {
            return blackKey(6, magnification,blackKeySetting);
        } else if (noteNum_12 == 12) {
            return new int[]{7 + (7 * magnification)};
        }
        return null;
    }

    //古老的琴，有黑键部分黑键但也缺失各种键，准备适配
    private static int[] toOldLyreKey(int noteValue, BlackKeySetting blackKeySetting){
        switch (noteValue){
            /* □ */ case 1  : return new int[]{1};
            /* ■ */ case 2  : return blackKey(1, 1,blackKeySetting);
            /* □ */ case 3  : return new int[]{2};
            /* ■ */ case 4  : return new int[]{3};
            /* □ */ case 5  : return blackKey(3, 1,blackKeySetting);
            /* □ */ case 6  : return new int[]{4};
            /* ■ */ case 7  : return blackKey(4, 1,blackKeySetting);
            /* □ */ case 8  : return new int[]{5};
            /* ■ */ case 9  : return blackKey(5, 1,blackKeySetting);
            /* □ */ case 10 : return new int[]{6};
            /* ■ */ case 11 : return new int[]{7};
            /* □ */ case 12 : return blackKey(7, 1,blackKeySetting);

            /* □ */ case 13 : return new int[]{8};
            /* ■ */ case 14 : return blackKey(1, 2,blackKeySetting);
            /* □ */ case 15 : return new int[]{9};
            /* ■ */ case 16 : return new int[]{10};
            /* □ */ case 17 : return blackKey(3, 2,blackKeySetting);
            /* □ */ case 18 : return new int[]{11};
            /* ■ */ case 19 : return blackKey(4, 2,blackKeySetting);
            /* □ */ case 20 : return new int[]{12};
            /* ■ */ case 21 : return blackKey(5, 2,blackKeySetting);
            /* □ */ case 22 : return new int[]{13};
            /* ■ */ case 23 : return new int[]{14};
            /* □ */ case 24 : return blackKey(7, 2,blackKeySetting);

            /* □ */ case 25 : return new int[]{15};
            /* ■ */ case 26 : return new int[]{16};
            /* □ */ case 27 : return blackKey(2, 3,blackKeySetting);
            /* ■ */ case 28 : return new int[]{17};
            /* □ */ case 29 : return blackKey(3, 3,blackKeySetting);
            /* □ */ case 30 : return new int[]{18};
            /* ■ */ case 31 : return blackKey(4, 3,blackKeySetting);
            /* □ */ case 32 : return new int[]{19};
            /* ■ */ case 33 : return new int[]{20};
            /* □ */ case 34 : return blackKey(6, 3,blackKeySetting);
            /* ■ */ case 35 : return new int[]{21};
            /* □ */ case 36 :
                if (blackKeySetting == BlackKeySetting.no || blackKeySetting == BlackKeySetting.right){
                    return null;
                } else {
                    return new int[]{7};
                }
            default:return null;
        }
    }
    //按照黑键的设定，按照黑键左边的白键为索引
    private static int[] blackKey(int noteNum_7, int magnification, BlackKeySetting setting) {
        switch (setting) {
            case leftAndRight:
                return new int[]{noteNum_7 + (7 * magnification), (noteNum_7 + 1) + (7 * magnification)};
            case left:
                return new int[]{noteNum_7 + (7 * magnification)};
            case right:
                return new int[]{(noteNum_7 + 1) + (7 * magnification)};
            case no:
                return null;
        }
        return null;
    }
}
