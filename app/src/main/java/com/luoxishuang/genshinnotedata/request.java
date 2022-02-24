package com.luoxishuang.genshinnotedata;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class request {
    public static boolean doGet(String url, Map<String, String> header, requestOB retOB) {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        JSONObject response = null;
        requestOBAB OBAB = new requestOBAB() { };
        boolean request_status = false;
        OBAB.addObserver(retOB);
        get.addHeader("Content-type", "application/json; charset=utf-8");
        if(header != null) {
            Iterator<String> MapIt = header.keySet().iterator();
            while (MapIt.hasNext()) {
                String key = MapIt.next();
                get.addHeader(key, header.get(key));
            }
        }
        get.setHeader("Accept", "application/json");

        try {
            HttpResponse res = client.execute(get);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = res.getEntity();
                String result = EntityUtils.toString(entity);
                response = new JSONObject(result);
                if(response.getInt("retcode") == 0){
                    request_status = true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        OBAB.setData(response);
        return request_status;
    }
}
