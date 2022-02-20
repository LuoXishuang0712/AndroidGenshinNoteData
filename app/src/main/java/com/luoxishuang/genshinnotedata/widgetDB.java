package com.luoxishuang.genshinnotedata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class widgetDB extends SQLiteOpenHelper {

    private static String DBFileName = "genshinNote.db";
    private static Integer version = 1;
    public static String tableName = "widgetDB";
    private static String createCommand = "create table " + tableName + " ( " +
            "id integer primary key, widgetID integer, charID integer, is_full integer);";

    public widgetDB(@Nullable Context context){
        super(context, DBFileName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL(createCommand);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
