package moe.yukisora.yandere;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import moe.yukisora.yandere.modles.TagsData;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YandereApplication extends Application {
    public static final String APPLICATION_FOLDER= "Yandere";
    public static final String TAGS_FILENAME = "tags.json";
    public static final String HARMONY_FLAG_FILENAME = "yandere_386449.png";

    private static File directory;
    private static TagsData<HashMap<String, Integer>> tagsData;
    private static boolean enableRating;
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

    public static boolean isEnableRating() {
        return enableRating;
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Gson gson = new Gson();

        // init application directory
        directory = new File(Environment.getExternalStorageDirectory(), APPLICATION_FOLDER);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // init variables
        enableRating = new File(directory, HARMONY_FLAG_FILENAME).exists();

        isSafe = preferences.getBoolean("isSafe", true);

        dpi = displayMetrics.densityDpi;
        screenWidth = displayMetrics.widthPixels;

        smallPlaceholderSize = BitmapFactory.decodeResource(getResources(), R.drawable.loading).getWidth();

        // init tags
        tagsData = new TagsData<>();
        File file = new File(getFilesDir(), TAGS_FILENAME);
        try (Reader in = file.exists() ? new FileReader(file) : new InputStreamReader(getResources().openRawResource(R.raw.tags))) {
            tagsData = gson.fromJson(in, new TypeToken<TagsData<HashMap<String, Integer>>>() {}.getType());
        } catch (IOException ignore) {
        }

        // init picasso
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Response response = chain.proceed(request);
                        return response.newBuilder()
                                .header("Cache-Control", "max-age=604800")
                                .removeHeader("Pragma")
                                .build();
                    }
                })
                .cache(new Cache(getCacheDir(), 32 * 1024 * 1024))
                .build();

        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException ignored) {
        }
    }
}
