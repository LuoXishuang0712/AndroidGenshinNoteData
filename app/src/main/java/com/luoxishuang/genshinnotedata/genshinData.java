package com.luoxishuang.genshinnotedata;

import org.json.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class genshinData {
    static String infoUrl = "https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=hk4e_cn";
    static String dataUrl = "https://api-takumi.mihoyo.com/game_record/app/genshin/api/dailyNote?role_id=%s&server=%s";
    static String salt = "salt=xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs&t=%d&r=%d&b=&q=%s";

    public static String get_md5(String str) {
        byte[] raw_data = null;
        try{
            raw_data = MessageDigest.getInstance("md5").digest(
                    str.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("No such algorithm");
        }
        return new BigInteger(1, raw_data).toString(16);
    }

    public static void getRequest(String url, String cookies, requestOB retOB){
        long _time = System.currentTimeMillis() / 1000;
        long _rand = (int)((Math.random() + 1) * 100000);
        String _check = get_md5(String.format(salt, _time, _rand, url.split("\\?")[1]));
        String _ds = String.format("%d,%d,%s", _time, _rand, _check);

        Map<String,String> header = new HashMap<String,String>();
        header.put("Cookie", cookies);
        header.put("DS", _ds);
        header.put("x-rpc-app_version", "2.20.1");
        header.put("x-rpc-client_type", "5");

        try {
            request.doGet(url, header, retOB);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String chinese_decode(String chn){
        return new String(chn.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    public static void getUserInfo(String cookie, requestOB retOB) {
        getRequest(infoUrl, cookie, retOB);
    }

    public static JSONObject splitUserInfo(JSONObject rawData, int char_num) throws JSONException {
        return rawData.getJSONObject("data").getJSONArray("list").getJSONObject(char_num);
    }

    public static void getUserData(String cookie, String game_uid, String region, requestOB retOB) throws JSONException {
        getRequest(
                String.format(dataUrl, game_uid, region),
                cookie,
                retOB
        );
    }

    public static void validCookie(String cookie, requestOB retOB) throws JSONException {
        getRequest(infoUrl, cookie, retOB);
    }
}
