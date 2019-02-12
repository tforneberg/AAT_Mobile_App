package de.tforneberg.aatapp.logic.util;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.tforneberg.aatapp.model.Settings;

/**
 * A task for downloading a settings file from a HTTP URL string on the background. It creates
 * a Settings object directly from the HttpURLConnections InputStream.
 * It needs to be initialized with a listener. When finished, it notifies the listener by calling
 * settingsDownloadDone(Settings settings).
 */
public class DownloadSettingsTask extends AsyncTask<String, Void, Settings> {

    public interface Listener { void settingsDownloadDone(Settings settings); }
    private Listener listener;

    public DownloadSettingsTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Settings settings) {
        super.onPostExecute(settings);
        listener.settingsDownloadDone(settings);
    }

    @Override
    protected Settings doInBackground(String... urls) {
        Settings settings = null;

        URL url;
        try {
            url = new URL(urls[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        try {
            //open http connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                //open input stream from http connection
                InputStream inputStream = conn.getInputStream();

                //write Settings object from input stream
                ObjectInputStream ois = new ObjectInputStream(inputStream);
                settings = (Settings) ois.readObject();
                
                ois.close();
                inputStream.close();
                conn.disconnect();
            }
        } catch (Exception e)  {
            e.printStackTrace();
            return null;
        }

        return settings;
    }
}
