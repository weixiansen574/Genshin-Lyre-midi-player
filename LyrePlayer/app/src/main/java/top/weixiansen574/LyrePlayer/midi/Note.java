package top.weixiansen574.LyrePlayer.midi;

import java.io.Serializable;

public class Note implements Comparable<Note>, Serializable {
    private final long tick;
    private final byte value;
    private final boolean isNoteOn;

    public Note(long tick, byte value, boolean isNoteOn) {
        this.tick = tick;
        this.value = value;
        this.isNoteOn = isNoteOn;
    }

    @Override
    public String toString() {
        return "Note{" +
                "tick=" + tick +
                ", value=" + value +
                ", isNoteOn=" + isNoteOn +
                '}';
    }

    public long getTick() {
        return tick;
    }

    public byte getValue() {
        return value;
    }

    public boolean isNoteOn() {
        return isNoteOn;
    }

    @Override
    public int compareTo(Note note) {
        return (int) (this.tick - note.getTick());
    }
}
