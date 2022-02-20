package com.luoxishuang.genshinnotedata;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class widgetUpdateService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Timer timer;
    private TimerTask task = new TimerTask() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            Log.d("widgetService", "scheduled task running");
            new widgetUpdateMethod().update(getApplicationContext());
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();
        timer = new Timer();
        timer.schedule(task, 0, 600000);  //10mins
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        new widgetUpdateMethod().update(getApplicationContext());
        return super.onStartCommand(intent, flags, startID);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy(){
        new widgetUpdateMethod().update(getApplicationContext());
        super.onDestroy();
        timer.cancel();
    }
}
