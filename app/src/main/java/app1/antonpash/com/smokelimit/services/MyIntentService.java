package app1.antonpash.com.smokelimit.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import app1.antonpash.com.smokelimit.ManageActivity;
import app1.antonpash.com.smokelimit.R;
import app1.antonpash.com.smokelimit.tasks.TimeLeftTask;


public class MyIntentService extends Service implements TimeLeftTask.TimeLeftTaskListener {

    long timestamp;
    AsyncTask task;
    private Intent broadcastIntent;

    @Override
    public void onCreate() {
        Log.d("anpa", "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("anpa", "onStartCommand");

        timestamp = intent.getLongExtra("timestamp", 10000);
        task = new TimeLeftTask(timestamp, this).execute();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("anpa", "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("anpa", "taskRemoved");

        task.cancel(true);

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

    @Override
    public void timeLeftProgressCallback(long step) {
        broadcastIntent = new Intent("TIME_LEFT_PROGRESS");
        broadcastIntent.putExtra("step", step);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void timeLeftPostCallback() {
        broadcastIntent = new Intent("TIME_LEFT_POST");
        sendBroadcast(broadcastIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyIntentService.this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("You were very patient and now you can smoke!");
        builder.setAutoCancel(true);
        builder.setSound(RingtoneManager.getActualDefaultRingtoneUri(MyIntentService.this, RingtoneManager.TYPE_NOTIFICATION));
        builder.setVibrate(new long[]{500, 500, 500});
        builder.setLights(Color.RED, 3000, 3000);
        builder.setCategory(NotificationCompat.CATEGORY_ALARM);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(MyIntentService.this, ManageActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyIntentService.this);
        stackBuilder.addParentStack(ManageActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());

        stopSelf();

    }

    @Override
    public void timeLeftPreCallback() {
        broadcastIntent = new Intent("TIME_LEFT_PRE");
        sendBroadcast(broadcastIntent);
    }

}
