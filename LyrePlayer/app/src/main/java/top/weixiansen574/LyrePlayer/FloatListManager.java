package top.weixiansen574.LyrePlayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import top.weixiansen574.LyrePlayer.midi.Note;

public class FloatListManager {
    private SQLiteDatabase database;
    public FloatListManager(Context context){
        this.database = context.openOrCreateDatabase("float_music_list",Context.MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS musics (name TEXT Not null Primary key,note_list BLOB Not null) ");
    }

    public boolean insertMusic (String name, ArrayList<Note> lyreNotes){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(lyreNotes);
            ContentValues cv = new ContentValues(3);
            cv.put("name",name);
            cv.put("note_list",baos.toByteArray());
            database.insert("musics",null,cv);
            oos.flush();
            oos.close();
            baos.flush();
            baos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String[] getMusicNames(){
        Cursor cursor = database.rawQuery("select * from musics",null);
        String[] musicNames = new String[cursor.getCount()];
        int j = 0;
        while(cursor.moveToNext()){
            musicNames[j] = cursor.getString(cursor.getColumnIndex("name"));
            j++;
        }
        return musicNames;
    }

    public ArrayList<Note> getLyreNotes(String musicName){
        Cursor cursor = database.rawQuery("select note_list from musics where name=?",new String[]{musicName});
        if (cursor.moveToNext()){
            byte[] data = cursor.getBlob(cursor.getColumnIndex("note_list"));
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                ArrayList<Note> lyreNotes = (ArrayList<Note>) ois.readObject();
                return lyreNotes;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }else {
            return null;
        }
    }

    public int deleteMusic (String musicName){
        return database.delete("musics","name=?",new String[]{musicName});
    }
}
