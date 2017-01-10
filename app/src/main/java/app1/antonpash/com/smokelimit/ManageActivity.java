package app1.antonpash.com.smokelimit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app1.antonpash.com.smokelimit.services.MyIntentService;

public class ManageActivity extends AppCompatActivity implements View.OnLongClickListener {

    public static final double HOURS = 16;
    private static final String FORMAT = "%02d:%02d:%02d";
    private static final long TIME_FOR_SLEEP = 5;

    TextView txtCurLimit, txtTimeLeft;
    SharedPreferences preferences;
    int limit;
    SharedPreferences.Editor editor;
    long timestamp;
    boolean isStepRunning;
    int curLimit;
    FrameLayout progressBar;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "TIME_LEFT_PROGRESS":
                    isStepRunning = true;
                    txtTimeLeft.setText(getTime(intent.getLongExtra("step", 0)));
                    break;
                case "TIME_LEFT_POST":
                    isStepRunning = false;

                    editor.putLong("timestamp", 0);
                    editor.apply();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

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

        isStepRunning = false;
        progressBar.setVisibility(View.GONE);

        preferences = getSharedPreferences("LIMIT", MODE_PRIVATE);
        editor = preferences.edit();

        limit = preferences.getInt("limit", 0);

        curLimit = preferences.getInt("curLimit", -1);

        if (curLimit == -1) {
            curLimit = limit;
        }

        txtCurLimit.setText(String.valueOf(curLimit));
    }

    private void checkLastVisited() {
        long lastVisited = preferences.getLong("lastVisited", 0);

        if (lastVisited != 0) {
            if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastVisited) >= TIME_FOR_SLEEP) {
                curLimit = limit;
                txtCurLimit.setText(String.valueOf(curLimit));
            }
        }
        editor.putLong("lastVisited", System.currentTimeMillis());
        editor.apply();
    }

    private void initUI() {
        txtCurLimit = (TextView) findViewById(R.id.txt_current_limit);
        txtTimeLeft = (TextView) findViewById(R.id.txt_time_left);
        progressBar = (FrameLayout) findViewById(R.id.progress);

        txtCurLimit.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {

        if (!isStepRunning) {
            if (curLimit > -100) {
                txtCurLimit.setText(String.valueOf(--curLimit));
                editor.putInt("curLimit", curLimit);

                timestamp = System.currentTimeMillis() + (long) ((HOURS / limit) * 60 * 60 * 1000);

                editor.putLong("timestamp", timestamp);
                editor.apply();

                Intent intent = new Intent(this, MyIntentService.class);
                intent.putExtra("timestamp", timestamp);
                startService(intent);
            } else {
                Toast.makeText(this, getString(R.string.manage_activity_limit_is_reached), Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    private String getTime(long timestamp) {

        return String.format(Locale.getDefault(), FORMAT,
                TimeUnit.MILLISECONDS.toHours(timestamp),
                TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(timestamp)),
                TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(timestamp)));
    }

}
