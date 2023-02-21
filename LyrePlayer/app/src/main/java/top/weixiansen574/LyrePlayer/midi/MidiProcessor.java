package top.weixiansen574.LyrePlayer.midi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import top.weixiansen574.LyrePlayer.R;
import top.weixiansen574.LyrePlayer.enums.InvalidKeySetting;

public class MidiProcessor {

    public static ArrayList<Note> toNoteList(InputStream inputStream) throws InvalidMidiDataException, IOException {
        //移植jdk17里的javax.sound.midi库，为保持兼容，阉割了midiDrive、Sequencer等,只保证能正常读取文件
        Sequence sequence = MidiSystem.getSequence(inputStream);
        Track[] tracks = sequence.getTracks();
        ArrayList<javax.sound.midi.MidiEvent> midiEvents = new ArrayList<>();
        for (Track track : tracks) {
            for (int i = 0; i < track.size(); i++) {
                midiEvents.add(track.get(i));
            }
        }
        //按照tick排序midiEvent(合并音轨)
        Collections.sort(midiEvents, (o1, o2) -> (int) (o1.getTick() - o2.getTick()));

        long lastTick = 0;
        long lastTick1 = 0;
        long currentTick = 0;
        float speed = 1;
        ArrayList<Note> noteList = new ArrayList<>();
        for (javax.sound.midi.MidiEvent midiEvent : midiEvents) {
            MidiMessage midiMessage = midiEvent.getMessage();
            System.out.println(midiEvent.getTick() + " " + Arrays.toString(midiMessage.getMessage()));
            if (midiMessage.getStatus() == ShortMessage.NOTE_ON) {
                System.out.print(" note_on");
            } else if (midiMessage.getStatus() == ShortMessage.NOTE_OFF) {
                System.out.print(" note_off");
            } else if (midiMessage instanceof MetaMessage && ((MetaMessage) midiMessage).getType() == 0x51) {
                byte[] midiMessageData = midiMessage.getMessage();
                speed = ((500 / (float) sequence.getResolution()) * (120 / (60000000 / (float) (((midiMessageData[3] & 0xFF) * 256 * 256) + ((midiMessageData[4] & 0xFF) * 256) + (midiMessageData[5] & 0xFF)))));
                System.out.println("speed:" + speed);
            }
        }
        for (javax.sound.midi.MidiEvent midiEvent : midiEvents) {
            MidiMessage midiMessage = midiEvent.getMessage();
            byte[] midiMessageData = midiMessage.getMessage();
            //读取midi事件 参考 https://www.bilibili.com/read/cv21175352
            //音符打开事件
            if (midiMessageData[0] >= -112 && midiMessageData[0] <= -97) {
                //读取某些midi文件时发现某个midi文件全部只有音符按下没有松开,但不可能没有松开，继续判断音符按压力度是否为零来确定是否为音符松开
                currentTick = (long) (((midiEvent.getTick() - lastTick) * speed) + lastTick1);
                //noteList.add(new Note(currentTick, (byte) noteOn.getNoteValue(), true));
                if (midiMessageData[2] != 0) {
                    noteList.add(new Note(currentTick, midiMessageData[1], true));
                } else {
                    noteList.add(new Note(currentTick, midiMessageData[1], false));
                }
                lastTick = midiEvent.getTick();
                lastTick1 = currentTick;
                //音符关闭事件
            } else if (midiMessageData[0] >= -128 && midiMessageData[0] <= -113) {
                currentTick = (long) (((midiEvent.getTick() - lastTick) * speed) + lastTick1);
                noteList.add(new Note(currentTick, midiMessageData[1], false));
                lastTick = midiEvent.getTick();
                lastTick1 = currentTick;
                //MIDI Set Tempo meta message 参阅 https://www.recordingblogs.com/wiki/midi-set-tempo-meta-message
            } else if (midiMessage instanceof MetaMessage && ((MetaMessage) midiMessage).getType() == 0x51) {
                speed = ((500 / (float) sequence.getResolution()) * (120 / (60000000 / (float) (((midiMessageData[3] & 0xFF) * 256 * 256) + ((midiMessageData[4] & 0xFF) * 256) + (midiMessageData[5] & 0xFF)))));
            }
        }
        inputStream.close();
        return noteList;
    }

    public static ArrayList<Note> toLyreNoteList(ArrayList<Note> noteList, int transposition, int[] sp, InvalidKeySetting blackKeySetting) {
        ArrayList<Note> lyreNotes = new ArrayList<>();
        for (Note note : noteList) {
            if (note.isNoteOn()) {
                //88键 ======> 36键 ========> 21键
                int note_36 = to36Key(note.getValue() + (11 - transposition), sp);
                if (note_36 != -1) {
                    int[] lyreKeys = toLyreKey(note_36, blackKeySetting);
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

    public static ArrayList<Note> toOldLyreNoteList(ArrayList<Note> noteList, int transposition, int[] sp, InvalidKeySetting invalidKeySetting) {
        ArrayList<Note> lyreNotes = new ArrayList<>();
        for (Note note : noteList) {
            if (note.isNoteOn()) {
                //88键 ======> 36键 ========> 21键
                int note_36 = to36Key(note.getValue() + (11 - transposition), sp);
                if (note_36 != -1) {
                    int[] lyreKeys = toOldLyreKey(note_36, invalidKeySetting);
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

    public static int analyzeBlackKeyQuantity(ArrayList<Note> notes, int transposition) {
        int[] blackKey = {22, 25, 27, 30, 32, 34, 37, 39, 42, 44, 46, 49, 51, 54, 56, 58, 58, 61, 63, 66, 68, 70, 73, 75, 78, 80, 82, 85, 87, 90, 92, 94, 97, 99, 102, 104, 106};
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

    public static int analyzeInvalidKeyQuantityForOldLyre(ArrayList<Note> notes, int transposition, int[] sp) {
        int[] blackKey = {2, 5, 7, 9, 12, 14, 17, 19, 21, 24, 27, 29, 31, 34, 36};
        int blackKeyQuantity = 0;
        for (Note note : notes) {
            if (note.isNoteOn()) {
                for (int keyVal : blackKey) {
                    if (to36Key(note.getValue() + transposition, sp) == keyVal) {
                        blackKeyQuantity++;
                    }
                }
            }
        }
        return blackKeyQuantity;
    }

    public static ArrayList<Note> processorToNoteListAndHandleExceptions(Activity activity, ContentResolver contentResolver, Uri uri) {
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            return toNoteList(inputStream);
        } catch (final IOException e) {
            //其他IO异常
            dialog(activity, activity.getString(R.string.exception_details), e.toString());
        } catch (InvalidMidiDataException e) {
            //打开的midi损坏或不是midi文件
            dialog(activity, activity.getString(R.string.file_data_is_corrupted), activity.getString(R.string.tshbsyxdmidiwj));
        }
        return null;
    }

    private static void dialog(final Activity activity, final String title, final String message) {
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
        if (noteValve <= 1) {
            return 1;
        } else if (noteValve >=84){
            return 84;
        }else if (sp[grepNumber] != -1) {
            return noteValve - (grepNumber * 12) + ((sp[grepNumber] - 1) * 12) + 1;
        } else {
            return -1;
        }
    }


    //根据黑键的设定，36键进一步压缩成21键，21键即原神琴的键数
    private static int[] toLyreKey(int noteValue, InvalidKeySetting blackKeySetting) {
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
            return invalidKey(1, magnification, blackKeySetting);
        } else if (noteNum_12 == 3) {
            return new int[]{2 + (7 * magnification)};
        } else if (noteNum_12 == 4) {
            return invalidKey(2, magnification, blackKeySetting);
        } else if (noteNum_12 == 5) {
            return new int[]{3 + (7 * magnification)};
        } else if (noteNum_12 == 6) {
            return new int[]{4 + (7 * magnification)};
        } else if (noteNum_12 == 7) {
            return invalidKey(4, magnification, blackKeySetting);
        } else if (noteNum_12 == 8) {
            return new int[]{5 + (7 * magnification)};
        } else if (noteNum_12 == 9) {
            return invalidKey(5, magnification, blackKeySetting);
        } else if (noteNum_12 == 10) {
            return new int[]{6 + (7 * magnification)};
        } else if (noteNum_12 == 11) {
            return invalidKey(6, magnification, blackKeySetting);
        } else if (noteNum_12 == 12) {
            return new int[]{7 + (7 * magnification)};
        }
        return null;
    }

    //古老的琴，有黑键部分黑键但也缺失各种键，准备适配
    private static int[] toOldLyreKey(int noteValue, InvalidKeySetting InvalidKeySetting) {
        switch (noteValue) {
            /* □ */
            case 1:
                return new int[]{1};
            /* ■ */
            case 2:
                return invalidKey(1, 0, InvalidKeySetting);
            /* □ */
            case 3:
                return new int[]{2};
            /* ■ */
            case 4:
                return new int[]{3};
            /* □ */
            case 5:
                return invalidKey(3, 0, InvalidKeySetting);
            /* □ */
            case 6:
                return new int[]{4};
            /* ■ */
            case 7:
                return invalidKey(4, 0, InvalidKeySetting);
            /* □ */
            case 8:
                return new int[]{5};
            /* ■ */
            case 9:
                return invalidKey(5, 0, InvalidKeySetting);
            /* □ */
            case 10:
                return new int[]{6};
            /* ■ */
            case 11:
                return new int[]{7};
            /* □ */
            case 12:
                return invalidKey(7, 0, InvalidKeySetting);

            /* □ */
            case 13:
                return new int[]{8};
            /* ■ */
            case 14:
                return invalidKey(1, 1, InvalidKeySetting);
            /* □ */
            case 15:
                return new int[]{9};
            /* ■ */
            case 16:
                return new int[]{10};
            /* □ */
            case 17:
                return invalidKey(3, 1, InvalidKeySetting);
            /* □ */
            case 18:
                return new int[]{11};
            /* ■ */
            case 19:
                return invalidKey(4, 1, InvalidKeySetting);
            /* □ */
            case 20:
                return new int[]{12};
            /* ■ */
            case 21:
                return invalidKey(5, 1, InvalidKeySetting);
            /* □ */
            case 22:
                return new int[]{13};
            /* ■ */
            case 23:
                return new int[]{14};
            /* □ */
            case 24:
                return invalidKey(7, 1, InvalidKeySetting);

            /* □ */
            case 25:
                return new int[]{15};
            /* ■ */
            case 26:
                return new int[]{16};
            /* □ */
            case 27:
                return invalidKey(2, 2, InvalidKeySetting);
            /* ■ */
            case 28:
                return new int[]{17};
            /* □ */
            case 29:
                return invalidKey(3, 2, InvalidKeySetting);
            /* □ */
            case 30:
                return new int[]{18};
            /* ■ */
            case 31:
                return invalidKey(4, 2, InvalidKeySetting);
            /* □ */
            case 32:
                return new int[]{19};
            /* ■ */
            case 33:
                return new int[]{20};
            /* □ */
            case 34:
                return invalidKey(6, 2, InvalidKeySetting);
            /* ■ */
            case 35:
                return new int[]{21};
            /* □ */
            case 36:
                return invalidKey(7,2,InvalidKeySetting);
            default:
                return null;
        }
    }

    //按照黑键的设定，按照黑键左边的白键为索引
    private static int[] invalidKey(int noteNum_7, int magnification, InvalidKeySetting setting) {
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
