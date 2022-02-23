package com.luoxishuang.genshinnotedata;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import kotlinx.coroutines.Job;

public class widgetUpdateService extends JobService {
    widgetDBHandler wdbh = MainActivity.wdbh;
    String CHANNEL_ID;

    @Override
    public boolean onStartJob(JobParameters params){
        super.onCreate();
        CHANNEL_ID  = getString(R.string.channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.channel_title))
                .setContentText(getString(R.string.channel_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setChannelId(CHANNEL_ID)
                .setOngoing(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(CHANNEL_ID, 1, builder.build());
        new widgetUpdateMethod().update(getApplicationContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params){
//        new widgetUpdateMethod().update(getApplicationContext());
        super.onDestroy();
        NotificationManagerCompat.from(this).cancelAll();
        return false;
    }

    public static void invoke(Context context){
        int JobId = 1;
        JobScheduler jobScheduler = (JobScheduler) context.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName jobService = new ComponentName(context.getPackageName(), widgetUpdateService.class.getName());
        JobInfo jobInfo = new JobInfo.Builder(JobId, jobService)
                .setPeriodic(16 * 60 * 1000)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresCharging(false)
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(10), JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        if(jobScheduler != null){
            jobScheduler.schedule(jobInfo);
        }
    }

//    public static void updateNotification(){
//
//    }
}
