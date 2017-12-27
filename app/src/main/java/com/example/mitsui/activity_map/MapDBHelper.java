package com.example.mitsui.activity_map;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by matiyu on 2017/12/27.
 */

public class MapDBHelper extends SQLiteOpenHelper{
    private static final String DB_NAME ="hinanjo.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "hinanjo";
    public static final String _ID = "id";
    public static final String NAME = "name";
    public static final String Lat = "lat";
    public static final String Lng = "lng";


    public MapDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable =
                "CREATE TABLE " + TABLE_NAME + " ( " + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT, " +
                Lat + " DOUBLE, " +
                Lng + " DOUBLE " + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
