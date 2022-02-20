package com.luoxishuang.genshinnotedata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class userDBHandler {

    private SQLiteOpenHelper helper;
    private Context context;
    public userDBHandler(Context context){
        this.context = context;
    }
    private String tableName = userDB.tableName;

    public void init(){
        helper = new userDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        try{
            db.query(tableName,new String[]{"*"}, null, null, null, null, null);
        } catch (SQLiteException e) {
            db.close();
            db = helper.getWritableDatabase();
            helper.onCreate(db);
        }
        db.close();
    }

    @SuppressLint("DefaultLocale")
    public void insert(String cookies, String char_cnt, String game_uid, String region){
        helper = new userDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(
                "insert into " + tableName + " " +
                        "(cookies, char_cnt, game_uid, region) "+
                        "values " +
                        "(?, ?, ?, ?);",
                new Object[]{cookies, char_cnt, game_uid, region}
        );
        db.close();
    }

    @SuppressLint("Range")
    public Integer insertRetId(String cookies, String char_cnt, String game_uid, String region){
        insert(cookies, char_cnt, game_uid, region);
        helper = new userDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                tableName,
                new String[]{"id"},
                String.format("game_uid='%s' and region='%s'", game_uid, region),
                null, null, null, null);
        Integer ret = -1;
        if(cursor != null){
            cursor.moveToNext();
            ret = cursor.getInt(cursor.getColumnIndex("id"));
        }
        db.close();
        return ret;
    }

    public void clean(){
        helper = new userDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from "+ tableName +";");
        db.close();
    }

    public void deleteRecord(String id){
        helper = new userDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from "+ tableName +" where id=?;",new Object[]{id});
        db.close();
    }

    @SuppressLint("Range")
    public Map<String, String> getID(String id){
        Map<String,String> ans = new HashMap<>() ;
        helper = new userDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor ret = db.query(tableName, new String[]{"*"}, String.format("id=%s", id), null, null, null, null);
//        ret.moveToFirst();
        if(ret != null) {
            if(ret.moveToNext()) {
                ans.put("id", ret.getString(ret.getColumnIndex("id")));
                ans.put("cookies", ret.getString(ret.getColumnIndex("cookies")));
                ans.put("char_cnt", ret.getString(ret.getColumnIndex("char_cnt")));
                ans.put("game_uid", ret.getString(ret.getColumnIndex("game_uid")));
                ans.put("region", ret.getString(ret.getColumnIndex("region")));
            }
        }
        db.close();
        return ans;
    }

    @SuppressLint("Range")
    public List<Map<String,String>> getDB(){
        List<Map<String,String>> ans = new ArrayList<>() ;
        helper = new userDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor ret = db.query(tableName, new String[]{"*"}, null, null, null, null, null);
//        ret.moveToFirst();
        if(ret != null) {
            while (ret.moveToNext()) {
                Map<String, String> tmp = new HashMap<>();
                tmp.put("id", ret.getString(ret.getColumnIndex("id")));
                tmp.put("cookies", ret.getString(ret.getColumnIndex("cookies")));
                tmp.put("char_cnt", ret.getString(ret.getColumnIndex("char_cnt")));
                tmp.put("game_uid", ret.getString(ret.getColumnIndex("game_uid")));
                tmp.put("region", ret.getString(ret.getColumnIndex("region")));
                ans.add(tmp);
            }
        }
        db.close();
        return ans;
    }
}
