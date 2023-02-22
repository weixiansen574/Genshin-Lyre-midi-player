package top.weixiansen574.LyrePlayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import top.weixiansen574.LyrePlayer.midi.FloatMusicBean;
import top.weixiansen574.LyrePlayer.midi.Note;

public class FloatListManager extends SQLiteOpenHelper {
    public static final int VERSION = 2;
    public static final String DBNAME = "float_music_list";

    public static final int MI_TYPE_LYRE = 1;
    public static final int MI_TYPE_OLD_LYRE = 2;

    Context context;
    public FloatListManager(Context context){
        super(context,DBNAME,null,VERSION);
        SQLiteDatabase db = context.openOrCreateDatabase(DBNAME,Context.MODE_PRIVATE,null);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //version0: db.execSQL("CREATE TABLE IF NOT EXISTS musics (name TEXT Not null Primary key,note_list BLOB Not null) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS musics (name TEXT Not null Primary key,type INTEGER Not null,note_list BLOB Not null) ");
        //收拾烂摊子，之前没用SQLiteOpenHelper，会直接调用onCreate，而不是调用onUpgrade
        if (db.getVersion() == 0){
            onUpgrade(db,0,VERSION);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("oldVersion:" + oldVersion + " newVersion:" + newVersion);
        switch (oldVersion){
            case 0 :
                Cursor cursor = db.rawQuery("select * from musics", null);
                System.out.println(cursor.getCount());
                db.execSQL("drop table musics");
                db.execSQL("CREATE TABLE IF NOT EXISTS musics (name TEXT Not null Primary key,type INTEGER Not null,note_list BLOB Not null) ");
                while (cursor.moveToNext()){
                    ContentValues values = new ContentValues();
                    values.put("name",cursor.getString(cursor.getColumnIndex("name")));
                    values.put("type",FloatListManager.MI_TYPE_LYRE);
                    values.put("note_list",cursor.getBlob(cursor.getColumnIndex("note_list")));
                    db.insert("musics",null,values);
                }
                break;
        }
    }

    public boolean insertMusic (String name,int type, ArrayList<Note> lyreNotes){
        SQLiteDatabase database = getWritableDatabase();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(lyreNotes);
            ContentValues cv = new ContentValues(3);
            cv.put("name",name);
            cv.put("type",type);
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

    public ArrayList<String> getMusicNames(){
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from musics",null);
        ArrayList<String> musicNames = new ArrayList<>(cursor.getCount());
        while(cursor.moveToNext()){
            musicNames.add(cursor.getString(cursor.getColumnIndex("name")));
        }
        return musicNames;
    }

    public FloatMusicBean getFloatMusicBean(String musicName){
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("select type,note_list from musics where name=?",new String[]{musicName});
        if (cursor.moveToNext()){
            byte[] data = cursor.getBlob(cursor.getColumnIndex("note_list"));
            int type = cursor.getInt(cursor.getColumnIndex("type"));
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                ArrayList<Note> lyreNotes = (ArrayList<Note>) ois.readObject();
                return new FloatMusicBean(musicName,type,lyreNotes);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }else {
            return null;
        }
    }

    public int deleteMusic (String musicName){
        SQLiteDatabase database = getWritableDatabase();
        return database.delete("musics","name=?",new String[]{musicName});
    }
}
