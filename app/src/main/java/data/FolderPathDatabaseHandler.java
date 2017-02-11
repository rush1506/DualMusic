package data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import model.FolderPath;
import model.FolderPathConstant;

/**
 * Created by vutha_000 on 7/25/2016.
 */
public class FolderPathDatabaseHandler extends SQLiteOpenHelper {

    private final ArrayList<FolderPath> folderPathsLists = new ArrayList<>();

    public FolderPathDatabaseHandler(Context context) {
        super(context, FolderPathConstant.DATABASE_NAME, null, FolderPathConstant.DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FolderPathConstant.TABLE_NAME);

        //Make new db
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String CREATE_TABLE = "CREATE TABLE " + FolderPathConstant.TABLE_NAME + "("
                + FolderPathConstant.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + FolderPathConstant.PATH_NAME + " TEXT);";

        sqLiteDatabase.execSQL(CREATE_TABLE);

    }


    //Add path to table
    public void addPath(FolderPath path) {
        SQLiteDatabase pathdb = getWritableDatabase();

        ContentValues data = new ContentValues();
        data.put(FolderPathConstant.PATH_NAME, path.getmPath());

        pathdb.insert(FolderPathConstant.TABLE_NAME, null, data);
        pathdb.close();

    }

    //get all paths
    public ArrayList<FolderPath> getPaths() {
        String SELECT_QUERY = "SELECT * FROM " + FolderPathConstant.TABLE_NAME;

        SQLiteDatabase pathdb = getReadableDatabase();
        Cursor pathCursor = pathdb.query(FolderPathConstant.TABLE_NAME,
                new String[]{FolderPathConstant.KEY_ID, FolderPathConstant.PATH_NAME},
                null, null, null, null, FolderPathConstant.KEY_ID + " DESC");

        if (pathCursor.moveToFirst()) {

            do {
                FolderPath folderPath = new FolderPath();

                folderPath.setmPath(pathCursor.getString(pathCursor.getColumnIndex(FolderPathConstant.PATH_NAME)));
                folderPath.setmID(pathCursor.getInt(pathCursor.getColumnIndex(FolderPathConstant.KEY_ID)));

                folderPathsLists.add(folderPath);


            } while (pathCursor.moveToNext());

        }
        pathCursor.close();
        pathdb.close();

        return folderPathsLists;
    }

    //delete path

    public void deletePath(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FolderPathConstant.TABLE_NAME, FolderPathConstant.KEY_ID + " = ? ",
                new String[]{String.valueOf(id)});
        db.close();
    }

    //change path

    public void changePath(int id, String newPath) {

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues updatedValues = new ContentValues();

            updatedValues.put(FolderPathConstant.PATH_NAME, newPath);

            db.update(FolderPathConstant.TABLE_NAME, updatedValues, FolderPathConstant.KEY_ID + "= " + String.valueOf(id), null);

            db.close();
        } catch (Exception e) {
            Log.v("Error! ", "Can't save");
            return;
        }

    }


}
