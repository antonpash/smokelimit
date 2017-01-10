package app1.antonpash.com.smokelimit.tasks;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TimeLeftTask extends AsyncTask<Void, Long, Void> {

    private long step;
    private TimeLeftTaskListener listener;

    public TimeLeftTask(long timestamp, TimeLeftTaskListener listener) {
        this.step = timestamp - System.currentTimeMillis();
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        listener.timeLeftPreCallback();
    }

    @Override
    protected Void doInBackground(Void... params) {

        while (step > 1000 && !isCancelled()) {
            step -= 1000;
            publishProgress(step);

            Log.d("AnPa", String.valueOf(step));
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

        listener.timeLeftProgressCallback(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        listener.timeLeftPostCallback();
    }

    public interface TimeLeftTaskListener {
        void timeLeftProgressCallback(long step);

        void timeLeftPostCallback();

        void timeLeftPreCallback();
    }

}
