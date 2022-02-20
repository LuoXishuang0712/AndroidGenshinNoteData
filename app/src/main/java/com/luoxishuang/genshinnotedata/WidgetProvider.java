package com.luoxishuang.genshinnotedata;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Map;

public class WidgetProvider extends AppWidgetProvider {
    String broadcastString = "com.luoxishuang.genshinnotedata.WIDGET";
    private widgetDBHandler wdbh = MainActivity.wdbh;

    public static int serviceCount = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Intent intent = new Intent(context, WidgetProvider.class);
        Log.d("Widget","onUpdate update " + Arrays.toString(appWidgetIds));

        if(wdbh.getWidgetID(appWidgetIds[0]).isEmpty()){
            wdbh.insertWidget(appWidgetIds[0]);
            intent.setAction(broadcastString + "." + appWidgetIds[0]);
        }
        else{
            intent.setAction(broadcastString);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        remoteViews.setOnClickPendingIntent(R.id.widget_main, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        setService(context);

        new widgetUpdateMethod().update(context);

        super.onUpdate(context,appWidgetManager,appWidgetIds);

    }

    private String getActivity(String fullName){
        String[] tmp = fullName.split("\\.");
        String ans = new String();
        int i=0;
        for(i=0;i<tmp.length-2;i++){
            ans += tmp[i] + ".";
        }
        ans += tmp[i];
        return ans;
    }

    private Integer getActivityID(String fullName){
        String[] tmp = fullName.split("\\.");
        return Integer.parseInt(tmp[tmp.length - 1]);
    }

    private void initListener(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent intent = new Intent(context, WidgetProvider.class);
        for(int appWidgetId : appWidgetIds){
            if(!wdbh.getWidgetID(appWidgetId).isEmpty()){
                intent.setAction(broadcastString + "." + appWidgetId);
            }
            else{
                intent.setAction(broadcastString);
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_main, pendingIntent);
        }
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        Log.d("Widget", "onReceive activity: " + intent.getAction());
        initListener(context);
        if(broadcastString.equals(getActivity(intent.getAction()))){
            Integer actID = getActivityID(intent.getAction());
//            Log.d("Widget", "onReceive private intent source id : "+actID);
            if(!wdbh.getWidgetNull(actID).isEmpty()){
                Intent selector = new Intent();
                selector.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                selector.setClass(context.getApplicationContext(), widgetAddChar.class);
                selector.putExtra("widgetID", actID);
                context.startActivity(selector);
            }
            else{
                Integer charID;
                try{
                    charID = wdbh.getWidgetID(actID).get("charID");
                } catch (Exception e){
                    e.printStackTrace();
                    return;
                }
                Intent InfoDetail = new Intent(context.getApplicationContext(), InfoDetail.class);
                InfoDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                InfoDetail.putExtra("id",String.valueOf(charID));
                context.startActivity(InfoDetail);
            }
        }
        else{
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds){
        Log.d("Widget","onDeleted delete " + Arrays.toString(appWidgetIds));
        if(!wdbh.getWidgetID(appWidgetIds[0]).isEmpty()){
            wdbh.deleteWidget(appWidgetIds[0]);
        }
        unsetService(context);
        super.onDeleted(context,appWidgetIds);
    }

    @Override
    public void onEnabled(Context context){
    }

    @Override
    public void onDisabled(Context context){
    }

    public static void setService(Context context){
        if(serviceCount++ == 0){
            Intent service = new Intent(context, widgetUpdateService.class);
            context.startService(service);
        }
        Log.d("setService", "now serviceCount = "+ serviceCount);
    }

    public static void unsetService(Context context){
        if(--serviceCount == 0){
            Intent service = new Intent(context, widgetUpdateService.class);
            context.stopService(service);
        }
        Log.d("unsetService", "now serviceCount = "+ serviceCount);
    }
}
