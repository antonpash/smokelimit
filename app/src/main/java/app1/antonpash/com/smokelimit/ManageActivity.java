package app1.antonpash.com.smokelimit;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ManageActivity extends AppCompatActivity implements View.OnLongClickListener {

    public static final double HOURS = 16;
    private static final String FORMAT = "%02d:%02d:%02d";

    TextView txtCurLimit, txtTimeLeft;
    SharedPreferences preferences;
    int limit;
    SharedPreferences.Editor editor;
    long timestamp;
    boolean isStepRunning;
    int curLimit;
    AsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        init();
    }

    @Override
    protected void onResume() {


        super.onResume();
    }

    private void init() {
        initUI();

        isStepRunning = false;

        preferences = getSharedPreferences("LIMIT", MODE_PRIVATE);
        editor = preferences.edit();

        limit = preferences.getInt("limit", 0);

        curLimit = preferences.getInt("curLimit", -1);

        if (curLimit == -1) {
            curLimit = limit;
        }

        txtCurLimit.setText(String.valueOf(curLimit));

        continueTimer();
    }

    private void continueTimer() {
        timestamp = preferences.getLong("timestamp", 0);

        if (timestamp != 0) {
            task = new TimeLeftTask().execute();
        }
    }

    private void initUI() {
        txtCurLimit = (TextView) findViewById(R.id.txt_current_limit);
        txtTimeLeft = (TextView) findViewById(R.id.txt_time_left);

        txtCurLimit.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {

        if (!isStepRunning) {
            txtCurLimit.setText(String.valueOf(--curLimit));
            editor.putInt("curLimit", curLimit);

            timestamp = System.currentTimeMillis() + (long) ((HOURS / limit) * 60 * 60 * 1000);

            editor.putLong("timestamp", timestamp);
            editor.apply();

            task = new TimeLeftTask().execute();
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

    class TimeLeftTask extends AsyncTask<Void, Long, Void> {

        long step = timestamp - System.currentTimeMillis();
        TextView txtTimeLeftLocal = txtTimeLeft;

        public void setTxtTimeLeftLocal(TextView txtTimeLeftLocal) {
            this.txtTimeLeftLocal = txtTimeLeftLocal;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isStepRunning = true;
        }

        @Override
        protected Void doInBackground(Void... params) {

            while (step > 1000 && !isCancelled()) {
                step -= 1000;
                publishProgress(step);
                Log.d("AnPa", "continue");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);

            txtTimeLeftLocal.setText(getTime(values[0]));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isStepRunning = false;

            editor.putLong("timestamp", 0);
            editor.apply();

        }
    }

}
