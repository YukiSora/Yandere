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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import moe.yukisora.yandere.modles.TagsData;

public class YandereApplication extends Application {
    public static final String TAGS_FILENAME = "tags.json";

    private static File directory;
    private static TagsData<HashMap<String, Integer>> tagsData;
    private static boolean isSafe;
    private static int dpi;
    private static int screenWidth;
    private static int smallPlaceholderSize;

    public static File getDirectory() {
        return directory;
    }

    public static HashMap<String, Integer> getTags() {
        return tagsData.data;
    }

    public static void setTagsData(TagsData<HashMap<String, Integer>> tagsData) {
        YandereApplication.tagsData = tagsData;
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

    public static int getTagsVersion() {
        return tagsData.version;
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

        // init tags
        tagsData = new TagsData<>();
        File file = new File(getFilesDir(), TAGS_FILENAME);
        try (Reader in = file.exists() ? new FileReader(file) : new InputStreamReader(getResources().openRawResource(R.raw.tags))) {
            tagsData = new Gson().fromJson(in, new TypeToken<TagsData<HashMap<String, Integer>>>() {}.getType());
        } catch (IOException ignore) {
        }
    }
}
