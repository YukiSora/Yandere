package moe.yukisora.yandere;

import android.app.Application;
import android.app.SearchManager;

import java.io.File;
import java.util.HashMap;

import okhttp3.OkHttpClient;

public class YandereApplication extends Application {
    private static OkHttpClient okHttpClient;
    private static File directory;
    private static HashMap<String, Integer> tags;
    private static SearchManager searchManager;
    private static boolean isSafe;
    private static int dpi;
    private static int maxMemory;
    private static int screenWidth;
    private static int smallPlaceholderSize;

    public static File getDirectory() {
        return directory;
    }

    public static void setDirectory(File directory) {
        YandereApplication.directory = directory;
    }

    public static HashMap<String, Integer> getTags() {
        return tags;
    }

    public static void setTags(HashMap<String, Integer> tags) {
        YandereApplication.tags = tags;
    }
    public static SearchManager getSearchManager() {
        return searchManager;
    }

    public static void setSearchManager(SearchManager searchManager) {
        YandereApplication.searchManager = searchManager;
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

    public static void setDpi(int dpi) {
        YandereApplication.dpi = dpi;
    }
    public static int getMaxMemory() {
        return maxMemory;
    }

    public static void setMaxMemory(int maxMemory) {
        YandereApplication.maxMemory = maxMemory;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static void setScreenWidth(int screenWidth) {
        YandereApplication.screenWidth = screenWidth;
    }

    public static int getSmallPlaceholderSize() {
        return smallPlaceholderSize;
    }

    public static void setSmallPlaceholderSize(int smallPlaceholderSize) {
        YandereApplication.smallPlaceholderSize = smallPlaceholderSize;
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static void setOkHttpClient(OkHttpClient okHttpClient) {
        YandereApplication.okHttpClient = okHttpClient;
    }
}
