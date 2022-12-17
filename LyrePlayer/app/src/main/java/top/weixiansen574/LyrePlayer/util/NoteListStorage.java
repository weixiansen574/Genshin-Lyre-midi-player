package top.weixiansen574.LyrePlayer.util;

import java.util.ArrayList;
import java.util.HashMap;

import top.weixiansen574.LyrePlayer.midi.Note;

public class NoteListStorage {
    private static HashMap<Long, ArrayList<Note>> noteListMap = new HashMap<>();

    public static long putNoteList(ArrayList<Note> noteList) {
        long key = System.currentTimeMillis();
        noteListMap.put(key, noteList);
        return key;
    }

    public static  ArrayList<Note> getNoteList(long key){
        return noteListMap.get(key);
    }
}
