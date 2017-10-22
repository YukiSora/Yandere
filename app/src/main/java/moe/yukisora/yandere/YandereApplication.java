package moe.yukisora.yandere;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.io.File;
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
            tags = new HashMap<>();
            String s = in.nextLine();

            int lastDigit = -1;
            for (String tag : s.replaceAll("\\s", "").split("`")) {
                if (!(tag.length() == 1 && tag.charAt(0) >= '0' && tag.charAt(0) <= '9')) {
                    if (lastDigit != -1)
                        tags.put(tag, lastDigit);
                    lastDigit = -1;
                }
                else {
                    lastDigit = tag.charAt(0) - '0';
                }
            }
        }
    }
}
