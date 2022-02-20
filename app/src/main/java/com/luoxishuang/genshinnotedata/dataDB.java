package com.luoxishuang.genshinnotedata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class dataDB extends SQLiteOpenHelper {

    private static String DBFileName = "genshinNote.db";
    private static int version = 1;
    public static String tableName = "dataDB";
    private static String createCommand = "create table "+ tableName +" (" +
            "id integer primary key, charID integer, filePath text, uid text, " +
            "lastUpdate integer, nickname text, level integer, regionName text);";

    public dataDB(@Nullable Context context) {
        super(context, DBFileName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createCommand);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
