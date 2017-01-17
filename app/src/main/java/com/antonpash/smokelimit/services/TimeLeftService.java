package com.antonpash.smokelimit.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.antonpash.smokelimit.ManageActivity;
import com.antonpash.smokelimit.R;


public class TimeLeftService extends Service {

    long timestamp;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    CountDownTimer timer;
    private Intent broadcastIntent;

    @Override
    public void onCreate() {
        Log.d("anpa", "onCreate");

        preferences = getSharedPreferences("LIMIT", MODE_PRIVATE);
        editor = preferences.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("anpa", "onStartCommand");

        timestamp = preferences.getLong("timestamp", 0);

        timer = new CountDownTimer(timestamp - System.currentTimeMillis(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("anpa", String.valueOf(millisUntilFinished));
                broadcastIntent = new Intent("TIME_LEFT_PROGRESS");
                broadcastIntent.putExtra("step", millisUntilFinished);
                sendBroadcast(broadcastIntent);
            }

            @Override
            public void onFinish() {
                timeLeftPostCallback();
            }
        }.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("anpa", "onDestroy");

        if (preferences.getLong("timestamp", 0) > System.currentTimeMillis()) {
            stopSelf();
            timer.cancel();
            restart();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("anpa", "taskRemoved");

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            restart();
        }
    }

    private void restart() {
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.putExtra("timestamp", timestamp);
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);

    }

    public void timeLeftPostCallback() {

        broadcastIntent = new Intent("TIME_LEFT_POST");
        sendBroadcast(broadcastIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(TimeLeftService.this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.main_notification));
        builder.setAutoCancel(true);
        builder.setSound(RingtoneManager.getActualDefaultRingtoneUri(TimeLeftService.this, RingtoneManager.TYPE_NOTIFICATION));
        builder.setVibrate(new long[]{500, 500, 500});
        builder.setLights(Color.RED, 3000, 3000);
        builder.setCategory(NotificationCompat.CATEGORY_ALARM);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(TimeLeftService.this, ManageActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(TimeLeftService.this);
        stackBuilder.addParentStack(ManageActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());


        stopSelf();

    }
}
