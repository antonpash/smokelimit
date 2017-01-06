package app1.antonpash.com.smokelimit;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ManageActivity extends AppCompatActivity implements View.OnLongClickListener {

    TextView txtCurLimit, txtTimeLeft;
    SharedPreferences preferences;
    String limit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        txtCurLimit = (TextView) findViewById(R.id.txt_current_limit);
        txtTimeLeft = (TextView) findViewById(R.id.txt_time_left);

        txtCurLimit.setOnLongClickListener(this);

        preferences = getSharedPreferences("LIMIT", MODE_PRIVATE);
        limit = preferences.getString("limit", null);

        String curLimit = preferences.getString("curLimit", null);

        if(curLimit != null){
            txtCurLimit.setText(curLimit);
        } else {
            txtCurLimit.setText(limit);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("curLimit", limit);
            editor.apply();
        }

        new TimeLeftTask().execute();
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(this, "Hi!", Toast.LENGTH_SHORT).show();
        return false;
    }

    class TimeLeftTask extends AsyncTask<Void, Long, Void>{

        long step = (14 / Long.parseLong(limit)) * 60 * 60 * 1000;

        @Override
        protected Void doInBackground(Void... params) {

            while(step > 0){
                step -= 1000;
                publishProgress(step);

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
            txtTimeLeft.setText(String.valueOf(values[0]));
        }
    }
}
