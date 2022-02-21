package com.luoxishuang.genshinnotedata;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class userFile {
    public static String saveUserData(Context context, String id, JSONObject userData){
        // return the stored filename.
        String path = context.getFilesDir().getPath();
        path += "/" + id + ".uDat";
//        Log.d("WriteFile","saveUserData() path:" + path);
        try{
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(genshinData.chinese_decode(userData.toString()).getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return path;
    }

    public static JSONObject readUserData(Context context, String id){
        FileInputStream fis;
        String path = context.getFilesDir().getPath();
        path += "/" + id + ".uDat";
        try{
            fis = new FileInputStream(path);
        } catch (FileNotFoundException ignored) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        JSONObject retJson ;
        try {
            retJson = new JSONObject(reader.readLine());
            fis.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
        return retJson;
    }

    public static String getFileName(String url){
        String[] tmp = url.split("/");
        return tmp[tmp.length - 1];
    }

    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static Bitmap avatarCache(Context context, String url){
        String path = context.getCacheDir().getPath();
        String fileName = getFileName(url);
        if(fileIsExists(path + "/" + fileName)){
            try {
                FileInputStream fis = new FileInputStream(path + "/" + fileName);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                fis.close();
                return bitmap;
            } catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }
        else{
            return null;
        }
    }

    public static boolean saveAvatar(Context context, Bitmap bitmap, String url){
        String path = context.getCacheDir().getPath();
        String fileName = getFileName(url);
        try{
            OutputStream os = new FileOutputStream(path + "/" + fileName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
