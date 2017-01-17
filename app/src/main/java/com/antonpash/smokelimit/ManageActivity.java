package com.antonpash.smokelimit;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.antonpash.smokelimit.services.ExtendedTimeService;
import com.antonpash.smokelimit.services.TimeLeftService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ManageActivity extends AppCompatActivity implements View.OnLongClickListener {

    public static final double HOURS = 0.025;
    private static final String FORMAT = "%02d:%02d:%02d";
    private static final String EXT_FORMAT = "+%02d:%02d:%02d";
    private static final long TIME_FOR_SLEEP = 5;

    TextView txtCurLimit, txtTimeLeft, txtExTime;
    SharedPreferences preferences;
    int limit;
    SharedPreferences.Editor editor;
    long timestamp;
    boolean isStepRunning;
    int curLimit;
    FrameLayout progressBar;
    Intent extTimeServiceIntent;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "TIME_LEFT_PROGRESS":
                    isStepRunning = true;

                    txtTimeLeft.setText(getTime(intent.getLongExtra("step", 0), FORMAT));

                    break;
                case "TIME_LEFT_POST":
                    isStepRunning = false;

                    startExtTimeService();
                    break;
            }

            progressBar.setVisibility(View.GONE);
        }
    }, extTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "EXT_TIME_PROGRESS":
                    txtExTime.setVisibility(View.VISIBLE);
                    txtExTime.setText(getTime(intent.getLongExtra("step", 0), EXT_FORMAT));

                    break;
            }
        }
    };

    private void startExtTimeService() {

        extTimeServiceIntent = new Intent(ManageActivity.this, ExtendedTimeService.class);
        startService(extTimeServiceIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        MobileAds.initialize(this, "ca-app-pub-8866352609118104~8627345677");

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        init();

    }

    @Override
    protected void onResume() {

        checkLastVisited();

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void init() {
        initUI();

        IntentFilter filter = new IntentFilter();
        filter.addAction("TIME_LEFT_PROGRESS");
        filter.addAction("TIME_LEFT_POST");
        filter.addAction("TIME_LEFT_PRE");
        registerReceiver(receiver, filter);

        filter = new IntentFilter();
        filter.addAction("EXT_TIME_PROGRESS");
        registerReceiver(extTimeReceiver, filter);

        isStepRunning = false;

        preferences = getSharedPreferences("LIMIT", MODE_PRIVATE);
        editor = preferences.edit();

        limit = preferences.getInt("limit", 0);

        curLimit = preferences.getInt("curLimit", -1);

        if (curLimit == -1) {
            curLimit = limit;
        }

        txtCurLimit.setText(String.valueOf(curLimit));

        timestamp = preferences.getLong("timestamp", 0);

        if (!isServiceRunning(TimeLeftService.class)) {
            if (timestamp > System.currentTimeMillis()) {
                Intent intent = new Intent(this, TimeLeftService.class);
                startService(intent);
            } else if (timestamp != 0) {
                startExtTimeService();
            }

            progressBar.setVisibility(View.GONE);
        }
    }

    private void checkLastVisited() {
        long lastVisited = preferences.getLong("lastVisited", 0);

        if (lastVisited != 0) {
            if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastVisited) >= TIME_FOR_SLEEP) {
                curLimit = limit;
                editor.putInt("curLimit", curLimit);
                txtCurLimit.setText(String.valueOf(curLimit));
            }
        }
        editor.putLong("lastVisited", System.currentTimeMillis());
        editor.apply();
    }

    private void initUI() {
        txtCurLimit = (TextView) findViewById(R.id.txt_current_limit);
        txtTimeLeft = (TextView) findViewById(R.id.txt_time_left);
        txtExTime = (TextView) findViewById(R.id.txt_extended_time);
        progressBar = (FrameLayout) findViewById(R.id.progress);

        txtCurLimit.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {

        if (!isStepRunning) {
            if (curLimit > -100) {
                if(extTimeServiceIntent != null){
                    stopService(extTimeServiceIntent);
                    txtExTime.setVisibility(View.GONE);
                }

                txtCurLimit.setText(String.valueOf(--curLimit));
                editor.putInt("curLimit", curLimit);

                timestamp = System.currentTimeMillis() + (long) ((HOURS / limit) * 60 * 60 * 1000);

                editor.putLong("timestamp", timestamp);
                editor.apply();

                Intent intent = new Intent(this, TimeLeftService.class);
                startService(intent);
            } else {
                Toast.makeText(this, getString(R.string.manage_activity_limit_is_reached), Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    private String getTime(long timestamp, String format) {

        return String.format(Locale.getDefault(), format,
                TimeUnit.MILLISECONDS.toHours(timestamp),
                TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(timestamp)),
                TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(timestamp)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
