package data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import model.Music;
import model.MusicConstant;

/**
 * Created by vutha_000 on 7/27/2016.
 */
public class    MusicDatabaseHandler extends SQLiteOpenHelper {

    private final ArrayList<Music> musicArrayList = new ArrayList<>();


    public MusicDatabaseHandler(Context context) {
        super(context, MusicConstant.DATABASE_NAME, null, MusicConstant.DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MusicConstant.TABLE_NAME);
        //Make new db
        onCreate(sqLiteDatabase);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_TABLE_QUERY = "CREATE TABLE " + MusicConstant.TABLE_NAME + "(" +
                MusicConstant.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                MusicConstant.TITLE_NAME + " TEXT, " +
                MusicConstant.ARTIST_NAME + " TEXT, " +
                MusicConstant.DISPLAY_NAME + " TEXT, " +
                MusicConstant.DURATION_NAME + " TEXT, " +
                MusicConstant.PATH_NAME + " TEXT, " +
                MusicConstant.ALBUM_NAME + " TEXT);";

        sqLiteDatabase.execSQL(CREATE_TABLE_QUERY);
    }

    /*
    * Add new music to database
    */

    public void addMusic(Music newMusic) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(MusicConstant.TITLE_NAME, newMusic.getMUSIC_TITLE());
        values.put(MusicConstant.ALBUM_NAME, newMusic.getMUSIC_ALBUM());
        values.put(MusicConstant.ARTIST_NAME, newMusic.getMUSIC_ARTIST());
        values.put(MusicConstant.DISPLAY_NAME, newMusic.getMUSIC_DISPLAY_NAME());
        values.put(MusicConstant.DURATION_NAME, newMusic.getMUSIC_DURATION());
        values.put(MusicConstant.PATH_NAME, newMusic.getMUSIC_PATH());

        db.insert(MusicConstant.TABLE_NAME, null, values);
        db.close();

    }

    /*
    *  get all music list
    * */

    public ArrayList<Music> getMusicArrayList() {

        SQLiteDatabase db = getReadableDatabase();
        //String SELECT_QUERY = "SELECT * FROM " + MusicConstant.TABLE_NAME;

        Cursor musicCursor = db.query(MusicConstant.TABLE_NAME,
                new String[]{MusicConstant.KEY_ID, MusicConstant.ARTIST_NAME, MusicConstant.ALBUM_NAME,
                        MusicConstant.DURATION_NAME, MusicConstant.DISPLAY_NAME, MusicConstant.TITLE_NAME, MusicConstant.PATH_NAME},
                null, null, null, null, MusicConstant.TITLE_NAME + " ASC");

        if (musicCursor.moveToFirst()) {

            do {

                Music tmpMusic = new Music();

                tmpMusic.setMUSIC_ALBUM(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.ALBUM_NAME)));
                tmpMusic.setMUSIC_ARTIST(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.ARTIST_NAME)));
                tmpMusic.setMUSIC_DISPLAY_NAME(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.DISPLAY_NAME)));
                tmpMusic.setMUSIC_DURATION(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.DURATION_NAME)));
                tmpMusic.setMUSIC_PATH(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.PATH_NAME)));
                tmpMusic.setMUSIC_TITLE(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.TITLE_NAME)));
                tmpMusic.setMUSIC_ID(musicCursor.getInt(musicCursor.getColumnIndex(MusicConstant.KEY_ID)));

                musicArrayList.add(tmpMusic);
            } while (musicCursor.moveToNext());

        }

        musicCursor.close();
        db.close();

        return musicArrayList;
    }

    //delete music

    public void deletePath(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MusicConstant.TABLE_NAME, MusicConstant.KEY_ID + " = ? ",
                new String[]{String.valueOf(id)});
        db.close();
    }

    //search music
    public ArrayList<Music> searchMusic(String name) {

        //name = "How you";
        ArrayList<Music> searchList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        /*
        * Query is weird
        * */


        Cursor musicCursor = db.query(MusicConstant.TABLE_NAME,
                new String[]{MusicConstant.KEY_ID, MusicConstant.ARTIST_NAME, MusicConstant.ALBUM_NAME,
                MusicConstant.DURATION_NAME, MusicConstant.DISPLAY_NAME, MusicConstant.TITLE_NAME, MusicConstant.PATH_NAME},
                MusicConstant.DISPLAY_NAME + " LIKE " + "'%" + name + "%'", null, null, null, MusicConstant.TITLE_NAME + " ASC");



        if (musicCursor.moveToFirst()) {

            do {

                Music tmpMusic = new Music();

                tmpMusic.setMUSIC_ALBUM(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.ALBUM_NAME)));
                tmpMusic.setMUSIC_ARTIST(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.ARTIST_NAME)));
                tmpMusic.setMUSIC_DISPLAY_NAME(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.DISPLAY_NAME)));
                tmpMusic.setMUSIC_DURATION(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.DURATION_NAME)));
                tmpMusic.setMUSIC_PATH(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.PATH_NAME)));
                tmpMusic.setMUSIC_TITLE(musicCursor.getString(musicCursor.getColumnIndex(MusicConstant.TITLE_NAME)));
                tmpMusic.setMUSIC_ID(musicCursor.getInt(musicCursor.getColumnIndex(MusicConstant.KEY_ID)));

                searchList.add(tmpMusic);
            } while (musicCursor.moveToNext());

        }

        musicCursor.close();
        db.close();
        return searchList;
    }

}
