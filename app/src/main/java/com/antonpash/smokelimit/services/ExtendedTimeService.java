package com.antonpash.smokelimit.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class ExtendedTimeService extends Service {
    long timestamp;
    public static final String LOG = "EX_TIME";
    public static final int DONE = -1;
    boolean isStopped = false;
    Intent broadcastIntent;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("anpa", LOG + " " + String.valueOf(System.currentTimeMillis() - timestamp));

            if(!isStopped){
                sendMessageDelayed(obtainMessage(DONE), 1000);

                broadcastIntent = new Intent("EXT_TIME_PROGRESS");
                broadcastIntent.putExtra("step", System.currentTimeMillis() - timestamp);
                sendBroadcast(broadcastIntent);
            }
        }
    };
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        Log.d("anpa", LOG + " onCreate");

        preferences = getSharedPreferences("LIMIT", MODE_PRIVATE);

        timestamp = preferences.getLong("timestamp", 0);

        handler.sendMessage(handler.obtainMessage(DONE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("anpa", LOG + " onStartCommand");

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("anpa", LOG + " onDestroy");

        isStopped = true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
