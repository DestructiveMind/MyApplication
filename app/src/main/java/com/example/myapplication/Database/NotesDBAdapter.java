package com.example.myapplication.Database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class NotesDBAdapter {
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_ID = "_id";
    public static final String TAG = NotesDBAdapter.class.getSimpleName();
    private DBHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "notes.db";
    private static final String TABLE_NAME = "notes";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
            + "title text not null, body text not null);";

    private final Context mContext;


public class DBHelper extends SQLiteOpenHelper {


    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.d(TAG, "onCreate() database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
        Log.d(TAG, "onUpgrade() database");
        }
    }

    public NotesDBAdapter(Context context) {
    mContext = context;
    }
    public NotesDBAdapter open() throws SQLException{
    mDbHelper = new DBHelper(mContext);
    mDb = mDbHelper.getWritableDatabase();
    return this;
    }
}
