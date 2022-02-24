package com.luoxishuang.genshinnotedata;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class widgetUpdateMethod {

    widgetDBHandler wdbh = MainActivity.wdbh;
    userDBHandler udbh = MainActivity.udbh;
    dataDBHandler ddbh = MainActivity.ddbh;

    Context context;

    public ConnectivityManager mConnectivity ;

    public final void update(Context context){
        this.context = context;
        mConnectivity = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        List<Map<String,Integer>> ret = wdbh.getDB();
        for(Map<String,Integer> item : ret){
            Log.d("widgetUpdate", "widgetID = " + item.get("widgetID"));
            if(item.get("charID") == 0){
                handleEmptyWidget(item);
            }
            else{
                handleWidget(item);
            }
        }
    }

    private void handleEmptyWidget(Map<String,Integer> widgetInfo){
        RemoteViews rv;
        int nickname;
        if(widgetInfo.get("is_full") == 0){
            rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            nickname = R.id.widget_info_nickname;
        }
        else{
            rv = new RemoteViews(context.getPackageName(), R.layout.widget_full);
            nickname = R.id.widget_full_info_nickname;
        }
        rv.setTextViewText(nickname, "单击以绑定角色信息");
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        awm.updateAppWidget(widgetInfo.get("widgetID"), rv);
    }

    private void handleWidget(Map<String,Integer> widgetInfo){
        final Integer ID = (Integer) widgetInfo.get("charID");
        Map<String,String> retData = udbh.getID(String.valueOf(ID));
        requestOB reOB = new requestOB() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChanged(Object data) throws JSONException {
                JSONObject ret = (JSONObject) data ;
                if(ret.getInt("retcode") != 0){
                    Log.e("widgetUpdate", ret.toString());
                }
                else{
                    ret = ret.getJSONObject("data");
                    String retFilename = userFile.saveUserData(context, String.valueOf(ID), ret);
                    if(retFilename!=null){
                        ddbh.updateData(ID, retFilename);
                    }
                    // draw info_list
                    drawWidget(ret, true, widgetInfo);
                }
            }
        };
        if(mConnectivity.getActiveNetworkInfo() != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean ret = false;
                        int count = 0;
                        do{
                            ret = genshinData.getUserData(retData.get("cookies"), retData.get("game_uid"), retData.get("region"), reOB);
                            count++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }while(!ret && count < 5);
                        if(!ret){
                            Looper.prepare();
                            Toast.makeText(
                                    context,
                                    "获取游戏内数据失败！请检查是否开启便笺功能。",
                                    Toast.LENGTH_LONG
                            ).show();
                            Looper.loop();
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
        else{
            JSONObject cache = userFile.readUserData(context, String.valueOf(ID));
            if(cache == null){
                Toast.makeText(
                        context ,
                        "无法获取缓存，请连接网络重试",
                        Toast.LENGTH_LONG
                ).show();
            }
            else{
                Toast.makeText(
                        context,
                        "当前数据为缓存数据，时间为系统预计时间",
                        Toast.LENGTH_LONG
                ).show();
                drawWidget(cache, false, widgetInfo);
            }
        }
    }

    public void drawWidget(JSONObject data, boolean isSubThread, Map<String, Integer> widgetInfo){
        RemoteViews rv;
        int[] componentList;
        Map<String, Object> userData = ddbh.getCharID(String.valueOf(widgetInfo.get("charID")));
        int adjust = 0;
        if(!isSubThread){
            adjust = (int) ((new Date().getTime()) - ((Long) userData.get("lastUpdate")));
        }
        if(widgetInfo.get("is_full") == 0){
            rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            componentList = new int[]{R.id.widget_info_nickname, R.id.widget_info_uid, R.id.resin_text, R.id.resin_data, R.id.resin_alert,
                    R.id.task_text, R.id.task_data, R.id.task_alert};
        }
        else{
            rv = new RemoteViews(context.getPackageName(), R.layout.widget_full);
            componentList = new int[]{R.id.widget_full_info_nickname, R.id.widget_full_info_uid, R.id.resin_full_text, R.id.resin_full_data, R.id.resin_full_alert,
                    R.id.task_full_text, R.id.task_full_data, R.id.task_full_alert, R.id.homecoin_full_text, R.id.homecoin_full_data, R.id.homecoin_full_alert,
                    R.id.discount_full_text, R.id.discount_full_data, R.id.discount_full_alert, R.id.widget_full_exp};
        }
        try {
            rv.setTextViewText(componentList[0], (String) userData.get("nickname"));
            rv.setTextViewText(componentList[1], (String) userData.get("uid"));
//            rv.setTextViewText(componentList[2], );   // resin text can be ignored
            rv.setTextViewText(componentList[3], String.format("%d/%d",calcRest(data.getInt("max_resin"),data.getInt("current_resin"),data.getInt("resin_recovery_time"),adjust),data.getInt("max_resin")));
            rv.setTextViewText(componentList[4], data.getInt("resin_recovery_time") - adjust <= 0 ? "原粹树脂已满" : sec2str(data.getInt("resin_recovery_time") - adjust));
//            rv.setTextViewText(componentList[5], );  // task text can be ignored
            rv.setTextViewText(componentList[6], String.format("%d/%d",data.getInt("total_task_num")-data.getInt("finished_task_num"),data.getInt("total_task_num")));
            rv.setTextViewText(componentList[7], data.getInt("finished_task_num") == data.getInt("total_task_num") ? (data.getBoolean("is_extra_task_reward_received") ? "安逸的氛围，喜欢。" : "每日委任奖励还未领取。") : "劳逸结合是不错，但也别放松过头。");
            if(widgetInfo.get("is_full") != 0){
//                rv.setTextViewText(componentList[8], );  // homeCoin text
                rv.setTextViewText(componentList[9], String.format("%d/%d",calcRest(data.getInt("max_home_coin"),data.getInt("current_home_coin"),data.getInt("home_coin_recovery_time"), adjust),data.getInt("max_home_coin")));
                rv.setTextViewText(componentList[10], data.getInt("home_coin_recovery_time") - adjust <= 0 ? "洞天宝钱已全部恢复。" : sec2str(data.getInt("home_coin_recovery_time") - adjust));
//                rv.setTextViewText(componentList[11], );  // discount text
                rv.setTextViewText(componentList[12], String.format("%d/%d",data.getInt("remain_resin_discount_num"),data.getInt("resin_discount_num_limit")));
                rv.setTextViewText(componentList[13], data.getInt("remain_resin_discount_num") == data.getInt("resin_discount_num_limit") ? "若陀想你了！" : "本周周本减半次数已用完。");
                JSONArray exp_data = data.getJSONArray("expeditions");
                int all = 0; int finished = 0;
                for(int i=0; i<exp_data.length(); i++){
                    if("Finished".equals(exp_data.getJSONObject(i).getString("status"))){
                        ++finished;
                    }
                    ++all;
                }
                rv.setTextViewText(componentList[14], String.format("派遣探索 (%d/%d)", finished, all));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        awm.updateAppWidget(widgetInfo.get("widgetID"), rv);
    }

    private Integer calcRest(Integer max, Integer past, Integer time, Integer adjust){
        Double pre = ((double) (max - past)) / time;
        past += (int) Math.round(pre * adjust);
        return past;
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

}
