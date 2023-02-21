package top.weixiansen574.LyrePlayer.midi;

import java.util.ArrayList;

public class FloatMusicBean {
    public String name;
    public int type;
    public ArrayList<Note> noteList;

    public FloatMusicBean(String name, int type, ArrayList<Note> noteList) {
        this.name = name;
        this.type = type;
        this.noteList = noteList;
    }

    @Override
    public String toString() {
        return "FloatMusicBean{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", noteList=" + noteList +
                '}';
    }
}
