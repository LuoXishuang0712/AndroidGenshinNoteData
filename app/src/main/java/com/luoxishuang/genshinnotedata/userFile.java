package com.luoxishuang.genshinnotedata;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
        return retJson;
    }
}
