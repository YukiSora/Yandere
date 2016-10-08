package moe.yukisora.yandere;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.io.File;

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
        File tempDirectory = new File(directory, "temp");
        if (!directory.exists())
            directory.mkdir();
        if (directory.exists() && !tempDirectory.exists())
            tempDirectory.mkdir();
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

        //move to MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LoadingActivity.this.startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                LoadingActivity.this.finish();
            }
        }, 1000);
    }
}
