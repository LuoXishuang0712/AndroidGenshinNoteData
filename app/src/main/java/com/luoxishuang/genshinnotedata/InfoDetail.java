package com.luoxishuang.genshinnotedata;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoDetail extends AppCompatActivity {
    userDBHandler udbh = MainActivity.udbh;
    dataDBHandler ddbh = MainActivity.ddbh;

    public ConnectivityManager mConnectivity ;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_detail);

        mConnectivity = (ConnectivityManager)getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);

        Intent intent = getIntent();
        String ID = intent.getStringExtra("id");
        if(ID == null){
            Toast.makeText(
                    InfoDetail.this,
                    "获取对应ID失败！",
                    Toast.LENGTH_LONG
            ).show();
            finish();
        }
        Map<String,String> retData = udbh.getID(ID);

//        Log.d("dataDBRet",ddbh.getDB().toString());

//        Log.d("InfoDetail",retData.get("cookies"));
        requestOB reOB = new requestOB() {
            @Override
            public void onDataChanged(Object data) throws JSONException {
                JSONObject ret = (JSONObject) data ;
                if(ret.getInt("retcode") != 0){
                    Looper.prepare();
                    Toast.makeText(
                            InfoDetail.this,
                            String.format(
                                    "获取游戏内数据失败！请检查是否开启便笺功能。\nmsg: %s",
                                    genshinData.chinese_decode(ret.getString("message")
                                    )),
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                    Looper.loop();
                }
                ret = ret.getJSONObject("data");
                String retFilename = userFile.saveUserData(getApplicationContext(), ID, ret);
                if(retFilename!=null){
                    ddbh.updateData(Integer.parseInt(ID), retFilename);
                }
                // draw info_list
                drawUI(ret, true, ID);
            }
        };

        // 此处判断是否有网络并进行分支，有网络则开启子线程进行请求，否则读取缓存

        if( mConnectivity.getActiveNetworkInfo() != null ){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        genshinData.getUserData(retData.get("cookies"), retData.get("game_uid"), retData.get("region"), reOB);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
        else{
            JSONObject cache = userFile.readUserData(getApplicationContext(), ID);
            if(cache == null){
                Toast.makeText(
                        InfoDetail.this,
                        "无法获取缓存，请连接网络重试",
                        Toast.LENGTH_LONG
                ).show();
            }
            else{
                Toast.makeText(
                        InfoDetail.this,
                        "当前数据为缓存数据，时间为系统预计时间",
                        Toast.LENGTH_LONG
                ).show();
                drawUI(cache, false, ID);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawUI(JSONObject data, boolean isSubThread, String ID){
        ListView info_list = findViewById(R.id.info_list);
        String[] info_from = new String[]{"mainTitle","subTitle","data"};
        int[] info_to = new int[]{R.id.main_text,R.id.sub_text,R.id.info_data};
        List<Map<String, String>> info_mList = new ArrayList<>();
        SimpleAdapter info_sa = new SimpleAdapter(InfoDetail.this, info_mList, R.layout.info_list_cell, info_from, info_to);

        ListView exp_list = findViewById(R.id.expeditions_list);
        String[] exp_from = new String[]{"img","text","char"};
        int[] exp_to = new int[]{R.id.exp_img,R.id.exp_main_text,R.id.exp_name};
        List<Map<String ,String>> exp_mList = new ArrayList<>();
        SimpleAdapter exp_sa = new SimpleAdapter(InfoDetail.this, exp_mList, R.layout.expeditions_list_cell, exp_from, exp_to);
        exp_sa.setViewBinder(new netImageViewAdapter());

        if(isSubThread){
            String retPath = userFile.saveUserData(getApplicationContext(), ID, data);
//            Log.d("InfoDetail", "Saved path:" + retPath);
        }

        JSONObject finalRet = data;
        if(isSubThread){
            runOnUiThread(() -> {
                info_list.setAdapter(info_sa);
                exp_list.setAdapter(exp_sa);
                try {
                    infoListAdd(finalRet, info_mList);
                    expListAdd(finalRet.getJSONArray("expeditions"), exp_mList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
        else{
            info_list.setAdapter(info_sa);
            exp_list.setAdapter(exp_sa);
            Integer adjust = Math.toIntExact((new Date().getTime() - (Long) ddbh.getCharID(ID).get("lastUpdate")) / 1000);
            try {
                infoListAdd(finalRet, info_mList, adjust);
                expListAdd(finalRet.getJSONArray("expeditions"), exp_mList, adjust);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String sec2str(int sec){
        String ans = new String();
        ans = String.format("%d秒",sec%60) + ans;
        sec /= 60;
        if(sec<=0){
            return ans;
        }
        ans = String.format("%d分",sec%60) + ans;
        sec /= 60;
        if(sec<=0){
            return ans;
        }
        ans = String.format("%d时",sec%24) + ans;
        sec /= 24;
        if(sec<=0){
            return ans;
        }
        ans = String.format("%d日",sec) + ans;
        return ans;
    }

    private String nameSplit(String url){
        String[] tmp = url.split("_");
        return tmp[tmp.length - 1].split("\\.")[0];
    }

    private void infoListAdd(JSONObject data, List<Map<String,String>> list) throws JSONException{
        infoListAdd(data,list,0);
    }

    private Integer calcRest(Integer max, Integer past, Integer time, Integer adjust){
        Double pre = ((double) (max - past)) / time;
        past += (int) Math.round(pre * adjust);
        return past;
    }

    private void bindPress(Integer presentResin, Integer restTime){
        ListView info_list = findViewById(R.id.info_list);
        info_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){ //press resin data
                    Intent calcResin = new Intent();
                    calcResin.setClass(getApplicationContext(), calcResin.class);
                    calcResin.putExtra("present", presentResin);
                    calcResin.putExtra("rest", restTime);
                    startActivity(calcResin);
                }
            }
        });
    }

    private void infoListAdd(JSONObject data, List<Map<String,String>> list, Integer adjust) throws JSONException {
        // todo : 没有家园信息时 Exception Handle
        // todo : 无家园信息的用户信息收集
        Map<String,String> resin = new HashMap<>();
        Map<String,String> homeCoin = new HashMap<>();
        Map<String,String> task = new HashMap<>();
        Map<String,String> discount = new HashMap<>();
        //{"mainTitle","subTitle","data"}
        resin.put("mainTitle","原粹树脂");
        resin.put("subTitle",
                data.getInt("resin_recovery_time") - adjust <= 0?
                "原粹树脂已全部恢复。":
                "预计"+sec2str(data.getInt("resin_recovery_time") - adjust)+"后完全恢复"
        );
        resin.put("data",String.format("%d/%d",calcRest(data.getInt("max_resin"),data.getInt("current_resin"),data.getInt("resin_recovery_time"),adjust),data.getInt("max_resin")));
        list.add(resin);
        homeCoin.put("mainTitle","洞天宝钱");
        homeCoin.put("subTitle",
                data.getInt("home_coin_recovery_time") - adjust <= 0?
                        "洞天宝钱已全部恢复。":
                        "预计"+sec2str(data.getInt("home_coin_recovery_time") - adjust)+"后完全恢复"
        );
        homeCoin.put("data",String.format("%d/%d",calcRest(data.getInt("max_home_coin"),data.getInt("current_home_coin"),data.getInt("home_coin_recovery_time"), adjust),data.getInt("max_home_coin")));
        list.add(homeCoin);
        task.put("mainTitle","每日委托");
        task.put("subTitle",
                data.getInt("finished_task_num") == data.getInt("total_task_num")?
                        (data.getBoolean("is_extra_task_reward_received")?
                            "安逸的氛围，喜欢。":
                            "每日委任奖励还未领取。")
                        :
                        "劳逸结合是不错，但也别放松过头。"
        );
        task.put("data",String.format("%d/%d",data.getInt("total_task_num")-data.getInt("finished_task_num"),data.getInt("total_task_num")));
        list.add(task);
        discount.put("mainTitle","周本减半");
        discount.put("subTitle",
                data.getInt("remain_resin_discount_num") == data.getInt("resin_discount_num_limit")?
                        "若陀想你了！":
                        "本周周本减半次数已用完。"
        );
        discount.put("data",String.format("%d/%d",data.getInt("remain_resin_discount_num"),data.getInt("resin_discount_num_limit")));
        bindPress(calcRest(data.getInt("max_resin"),data.getInt("current_resin"),data.getInt("resin_recovery_time"),adjust),data.getInt("resin_recovery_time") - adjust);
        list.add(discount);
    }

    private void expListAdd(JSONArray data, List<Map<String,String>> list) throws JSONException{
        expListAdd(data, list, 0);
    }

    private void expListAdd(JSONArray data, List<Map<String,String>> list, Integer adjust) throws JSONException {
        int finish_count = 0;
        int all_count = 0;
        for(int i=0;i<data.length();i++){
            Map<String,String> tmp = new HashMap<>();
            JSONObject tData = data.getJSONObject(i);
            // {"img","text","char"}
            tmp.put("img",tData.getString("avatar_side_icon"));
            tmp.put("text", (tData.getInt("remained_time") - adjust <= 0) ?
                    "探索已完成。":
                    "预计"+sec2str(tData.getInt("remained_time") - adjust)+"后完成");
            if((tData.getInt("remained_time") - adjust <= 0)){
                finish_count ++;
            }
            tmp.put("char",nameSplit(tData.getString("avatar_side_icon")));
            all_count ++;
            list.add(tmp);
        }
        int finalFinish_count = finish_count;
        int finalAll_count = all_count;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView count = findViewById(R.id.expeditions_limit);
                count.setText(String.format("(%d/%d)", finalFinish_count, finalAll_count));
            }
        });
    }
}
