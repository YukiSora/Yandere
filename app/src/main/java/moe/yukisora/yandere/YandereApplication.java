package moe.yukisora.yandere;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Scanner;

public class YandereApplication extends Application {
    private static File directory;
    private static HashMap<String, Integer> tags;
    private static boolean isSafe;
    private static int dpi;
    private static int screenWidth;
    private static int smallPlaceholderSize;

    public static File getDirectory() {
        return directory;
    }

    public static HashMap<String, Integer> getTags() {
        return tags;
    }

    public static boolean isSafe() {
        return isSafe;
    }

    public static void setSafe(boolean isSafe) {
        YandereApplication.isSafe = isSafe;
    }

    public static int getDpi() {
        return dpi;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getSmallPlaceholderSize() {
        return smallPlaceholderSize;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        directory = new File(Environment.getExternalStorageDirectory(), "Yandere");
        if (!directory.exists()) {
            directory.mkdir();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isSafe = preferences.getBoolean("isSafe", true);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        dpi = displayMetrics.densityDpi;
        screenWidth = displayMetrics.widthPixels;

        smallPlaceholderSize = BitmapFactory.decodeResource(getResources(), R.drawable.loading).getWidth();

        try (Scanner in = new Scanner(getResources().openRawResource(R.raw.tags))) {
            Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
            tags = new Gson().fromJson(in.useDelimiter("\\A").next(), type);
        }
    }
}
