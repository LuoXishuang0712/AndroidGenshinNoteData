package com.luoxishuang.genshinnotedata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dataDBHandler {

    private SQLiteOpenHelper helper;
    private Context context;
    public dataDBHandler(Context context){
        this.context = context;
    }
    private String tableName = dataDB.tableName;

    public void init(){
        helper = new dataDB(context);
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

    @SuppressLint({"DefaultLocale", "Range"})
    public void insertInfo(Integer charID, String uid, String nickname, Integer level, String region){
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("insert into " + tableName +
                        " (charID, uid, nickname, level, regionName) " +
                        " values " +
                        " (?,?,?,?,?) ;",
                new Object[]{charID, uid, nickname, level, region});
        db.close();
    }

    public void updateInfoI(Integer charID, String nickname, Integer level, String region){
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("update " + tableName +
                " set nickname=? , level=? , regionName=? " +
                " where charID=? ;",
                new Object[]{nickname, level, region, charID});
        db.close();
    }

    public void updateInfo(Integer charID, String uid, String nickname, Integer level, String region){
        if(getCharID(String.valueOf(charID)).isEmpty()){
            insertInfo(charID, uid, nickname, level, region);
        }
        else{
            updateInfoI(charID, nickname, level, region);
        }
    }

    public void updateData(Integer charID, String filepath){
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("update " + tableName +
                        " set filePath=? , lastUpdate=? " +
                        " where charID=? ;",
                new Object[]{filepath, new Date().getTime(), charID});
        db.close();
    }

    public void clean(){
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from "+ tableName +" ;");
        db.close();
    }

    public void deleteRecord(String id){
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from "+ tableName +" where id=? ;",new Object[]{id});
        db.close();
    }

    public void deleteCharIDRecord(String charID){
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from "+ tableName +" where charID=? ;",new Object[]{charID});
        db.close();
    }

    @SuppressLint("Range")
    public Map<String, Object> getID(String id){
        Map<String,Object> ans = new HashMap<>() ;
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor ret = db.query(tableName, new String[]{"*"}, String.format("id=%s", id), null, null, null, null);
//        ret.moveToFirst();
        if(ret != null) {
            if(ret.moveToNext()) {
                ans.put("id", ret.getInt(ret.getColumnIndex("id")));
                ans.put("charID", ret.getInt(ret.getColumnIndex("charID")));
                ans.put("uid", ret.getString(ret.getColumnIndex("uid")));
                ans.put("filePath", ret.getString(ret.getColumnIndex("filePath")));
                ans.put("lastUpdate", ret.getLong(ret.getColumnIndex("lastUpdate")));
                ans.put("nickname", ret.getString(ret.getColumnIndex("nickname")));
                ans.put("level", ret.getInt(ret.getColumnIndex("level")));
                ans.put("regionName", ret.getString(ret.getColumnIndex("regionName")));
            }
        }
        db.close();
        return ans;
    }

    @SuppressLint("Range")
    public Map<String, Object> getCharID(String id){
        Map<String,Object> ans = new HashMap<>() ;
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor ret = db.query(tableName, new String[]{"*"}, String.format("charID=%s", id), null, null, null, null);
//        ret.moveToFirst();
        if(ret != null) {
            if(ret.moveToNext()) {
                ans.put("id", ret.getInt(ret.getColumnIndex("id")));
                ans.put("charID", ret.getInt(ret.getColumnIndex("charID")));
                ans.put("uid", ret.getString(ret.getColumnIndex("uid")));
                ans.put("filePath", ret.getString(ret.getColumnIndex("filePath")));
                ans.put("lastUpdate", ret.getLong(ret.getColumnIndex("lastUpdate")));
                ans.put("nickname", ret.getString(ret.getColumnIndex("nickname")));
                ans.put("level", ret.getInt(ret.getColumnIndex("level")));
                ans.put("regionName", ret.getString(ret.getColumnIndex("regionName")));
            }
        }
        db.close();
        return ans;
    }

    @SuppressLint("Range")
    public List<Map<String,Object>> getDB(){
        List<Map<String,Object>> ans = new ArrayList<>() ;
        helper = new dataDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor ret = db.query(tableName, new String[]{"*"}, null, null, null, null, null);
//        ret.moveToFirst();
        if(ret != null) {
            while (ret.moveToNext()) {
                Map<String, Object> tmp = new HashMap<>();
                tmp.put("id", ret.getInt(ret.getColumnIndex("id")));
                tmp.put("charID", ret.getInt(ret.getColumnIndex("charID")));
                tmp.put("uid", ret.getString(ret.getColumnIndex("uid")));
                tmp.put("filePath", ret.getString(ret.getColumnIndex("filePath")));
                tmp.put("lastUpdate", ret.getLong(ret.getColumnIndex("lastUpdate")));
                tmp.put("nickname", ret.getString(ret.getColumnIndex("nickname")));
                tmp.put("level", ret.getInt(ret.getColumnIndex("level")));
                tmp.put("regionName", ret.getString(ret.getColumnIndex("regionName")));
                ans.add(tmp);
            }
        }
        db.close();
        return ans;
    }
}
