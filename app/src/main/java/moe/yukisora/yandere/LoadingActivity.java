package moe.yukisora.yandere;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Scanner;

public class LoadingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        //initialize variable
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //create folder
        File directory = new File(Environment.getExternalStorageDirectory(), "Yandere");
        if (!directory.exists())
            directory.mkdir();
        MainActivity.setDirectory(directory);

        //SearchManager
        MainActivity.setSearchManager((SearchManager)getSystemService(Context.SEARCH_SERVICE));

        //safe option
        MainActivity.setSafe(preferences.getBoolean("isSafe", true));

        //display attribute
        MainActivity.setDpi(metrics.densityDpi);
        MainActivity.setScreenWidth(metrics.widthPixels);

        //max memory
        MainActivity.setMaxMemory(1024 * 1024 * ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass());

        //placeholder image size
        MainActivity.setSmallPlaceholderSize(BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_small).getWidth());

        //pre download and move to MainActivity
        new DownloadTagsTask().execute();
    }


    private class DownloadTagsTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... args) {
            try {
                URLConnection connection = new URL("https://yande.re/tag/summary.json").openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                String str;
                try (Scanner in = new Scanner(connection.getInputStream())) {
                    str = in.useDelimiter("\\A").next();
                }

                //ArrayList<String> tags = new ArrayList<>();
                HashMap<String, Integer> tags = new HashMap<>();

                int lastDigit = -1;
                for (String tag : new JSONObject(str).getString("data").replaceAll("\\s", "").split("`")) {
                    if (!(tag.length() == 1 && tag.charAt(0) >= '0' && tag.charAt(0) <= '9')) {
                        if (lastDigit != -1)
                            tags.put(tag, lastDigit);
                        lastDigit = -1;
                    }
                    else {
                        lastDigit = tag.charAt(0) - '0';
                    }
                }

                MainActivity.setTags(tags);
            } catch (IOException | JSONException ignored) {
            }

            return null;
        }

        protected void onPostExecute(Void args) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoadingActivity.this.startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                    LoadingActivity.this.finish();
                }
            }, 1000);
        }
    }
}
