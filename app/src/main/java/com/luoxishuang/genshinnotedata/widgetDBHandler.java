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

public class widgetDBHandler {

    private SQLiteOpenHelper helper;
    private Context context;
    private String tableName = widgetDB.tableName;

    public widgetDBHandler(Context context){
        this.context = context;
    }

    public void init(){
        helper = new widgetDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        try{
            db.query(tableName, new String[]{"*"}, null, null, null, null, null);
        } catch (SQLiteException e) {
            db.close();
            db = helper.getWritableDatabase();
            helper.onCreate(db);
        }
        db.close();
    }

    public void insertWidget(Integer widgetID){
        insertWidget(widgetID, 0);
    }

    public void insertWidgetFull(Integer widgetID){
        insertWidget(widgetID, 1);
    }

    public void insertWidget(Integer widgetID, Integer isFull){
        helper = new widgetDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("insert into " + tableName +
                " (widgetID, is_full) " +
                " values " +
                " (?,?) ;",
                new Object[]{widgetID, isFull});
        db.close();
    }

    public void updateWidget(Integer widgetID, Integer charID){
        helper = new widgetDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("update "+ tableName +
                " set charID=? " +
                " where widgetID=? ; ",
                new Object[]{charID, widgetID});
        db.close();
    }

    public void deleteWidget(Integer widgetID){
        helper = new widgetDB(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from "+ tableName +" where widgetID=? ;",new Object[]{widgetID});
        db.close();
    }

    @SuppressLint("Range")
    public Map<String,Integer> getWidgetID(Integer widgetID){
        Map<String,Integer> ans = new HashMap<>();
        helper = new widgetDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor ret = db.query(tableName, new String[]{"*"}, String.format("widgetID=%d", widgetID), null,null,null,null);
        if(ret != null && ret.moveToNext()){
            ans.put("id", ret.getInt(ret.getColumnIndex("id")));
            ans.put("widgetID", ret.getInt(ret.getColumnIndex("widgetID")));
            ans.put("charID", ret.getInt(ret.getColumnIndex("charID")));
            ans.put("is_full", ret.getInt(ret.getColumnIndex("is_full")));
        }
        db.close();
        return ans;
    }

    @SuppressLint("Range")
    public Map<String,Integer> getCharID(Integer widgetID){
        Map<String,Integer> ans = new HashMap<>();
        helper = new widgetDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor ret = db.query(tableName, new String[]{"*"}, String.format("charID=%d", widgetID), null,null,null,null);
        if(ret != null && ret.moveToNext()){
            ans.put("id", ret.getInt(ret.getColumnIndex("id")));
            ans.put("widgetID", ret.getInt(ret.getColumnIndex("widgetID")));
            ans.put("charID", ret.getInt(ret.getColumnIndex("charID")));
            ans.put("is_full", ret.getInt(ret.getColumnIndex("is_full")));
        }
        db.close();
        return ans;
    }

    @SuppressLint("Range")
    public Map<String,Integer> getWidgetNull(Integer widgetID){
        Map<String,Integer> ans = new HashMap<>();
        helper = new widgetDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor ret = db.query(tableName, new String[]{"*"}, String.format("widgetID=%d and charID is null", widgetID), null,null,null,null);
        if(ret != null && ret.moveToNext()){
            ans.put("id", ret.getInt(ret.getColumnIndex("id")));
            ans.put("widgetID", ret.getInt(ret.getColumnIndex("widgetID")));
            ans.put("charID", ret.getInt(ret.getColumnIndex("charID")));
            ans.put("is_full", ret.getInt(ret.getColumnIndex("is_full")));
        }
        db.close();
        return ans;
    }

    @SuppressLint("Range")
    public List<Map<String,Integer>> getDB(){
        return getDB(0);
    }

    @SuppressLint("Range")
    public List<Map<String,Integer>> getDB(Integer is_bind){
        List<Map<String,Integer>> ans = new ArrayList<>();
        helper = new widgetDB(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = null;
        if(is_bind != 0){
            selection = "charID is not null";
        }
        Cursor ret = db.query(tableName, new String[]{"*"}, selection, null,null,null,null);
        if(ret != null){
            while(ret.moveToNext()){
                Map<String,Integer> tmp = new HashMap<>();
                tmp.put("id", ret.getInt(ret.getColumnIndex("id")));
                tmp.put("widgetID", ret.getInt(ret.getColumnIndex("widgetID")));
                tmp.put("charID", ret.getInt(ret.getColumnIndex("charID")));
                tmp.put("is_full", ret.getInt(ret.getColumnIndex("is_full")));
                ans.add(tmp);
            }
        }
        db.close();
        return ans;
    }
}
