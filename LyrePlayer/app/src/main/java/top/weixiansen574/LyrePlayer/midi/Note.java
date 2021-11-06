package top.weixiansen574.LyrePlayer.midi;

import java.io.Serializable;

public class Note implements Comparable<Note>, Serializable {
    private final long tick;
    private final byte note;
    private final boolean type;

    public Note(long tick, byte note, boolean type) {
        this.tick = tick;
        this.note = note;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Note{" +
                "tick=" + tick +
                ", note=" + note +
                ", type=" + type +
                '}';
    }

    public long getTick() {
        return tick;
    }

    public byte getNote() {
        return note;
    }

    public boolean type() {
        return type;
    }

    public static boolean isBlackKey(int note){
        int[] blackKey = {22,25,27,30,32,34,37,39,42,44,46,49,51,54,56,58,58,61,63,66,68,70,73,75,78,80,82,85,87,90,92,94,97,99,102,104,106};
        for(int keyNum:blackKey){
            if (note == keyNum){
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Note note) {
        return (int) (this.tick - note.getTick());
    }
}
