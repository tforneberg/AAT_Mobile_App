package de.tforneberg.aatapp.ui.util;

import android.os.AsyncTask;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

/**
 * Updates the given progressBar without blocking the UI thread.
 */
public class UpdateProgressBarTask extends AsyncTask<Void, Integer, Void> {

    private WeakReference<ProgressBar> progressBar;

    public UpdateProgressBarTask(ProgressBar bar) {
        super();
        this.progressBar = new WeakReference<>(bar);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int progress = progressBar.get().getProgress();
        long oldTime = System.currentTimeMillis();
        while (progress < progressBar.get().getMax()) {
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e) { e.printStackTrace(); }
            long newTime = System.currentTimeMillis();
            long diff = newTime - oldTime;
            oldTime = newTime;
            progress = progress + (int) diff;
            this.onProgressUpdate((int) diff);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... ints) {
        super.onProgressUpdate(ints);
        progressBar.get().incrementProgressBy(ints[0]);
    }
}