package com.luoxishuang.genshinnotedata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class userDB extends SQLiteOpenHelper {

    private static String DBFileName = "genshinNote.db";
    private static int version = 1;
    public static String tableName = "userDB";
    private static String createCommand = "create table "+ tableName +" (" +
            "id integer primary key, cookies varchar(256), char_cnt integer, " +
            "game_uid varchar(16), region varchar(16))";

    public userDB(@Nullable Context context) {
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
