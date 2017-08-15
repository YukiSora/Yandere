package moe.yukisora.yandere.core;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.fragments.PostFragment;
import moe.yukisora.yandere.modles.ImageData;

public class ImageManager {
    private static ImageCache imageCache;
    private static ImageManager imageManager;
    private Handler handler;
    private boolean isDownloading;

    private ImageManager() {
        handler = new Handler();
        imageCache = new ImageCache(YandereApplication.getMaxMemory() / 2);
        DownloadImageThreadPool.getInstance().setImageCache(imageCache);
    }

    public static ImageManager getInstance() {
        if (imageManager == null)
            imageManager = new ImageManager();

        return imageManager;
    }

    public static ImageCache getImageCache() {
        return imageCache;
    }

    public static Bitmap downloadImage(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (IOException ignored) {
        }

        return null;
    }

    public void loadImage(Fragment fragment, String url) {
        if (!isDownloading)
            new DownloadImageData(fragment, url).start();
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    private class DownloadImageData extends Thread {
        private PostFragment fragment;
        private String url;

        DownloadImageData(Fragment fragment, String url) {
            this.fragment = (PostFragment)fragment;
            this.url = url;
        }

        private String getJSON() {
            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                try (Scanner in = new Scanner(connection.getInputStream())) {
                    return in.useDelimiter("\\A").next();
                }
            } catch (IOException ignored) {
            }

            return null;
        }

        private void parseJSON(String str) {
            try {
                JSONArray jsonArray = new JSONArray(str);
                final int positionStart = fragment.getImageDatas().size();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    final ImageData imageData = new ImageData();
                    imageData.id = jsonObject.getInt("id");
                    imageData.list_id = fragment.getImageDatas().size();
                    imageData.tags = jsonObject.getString("tags");
                    imageData.file_size = jsonObject.getInt("file_size");
                    imageData.file_ext = jsonObject.getString("file_ext");
                    imageData.file_url = jsonObject.getString("file_url");
                    imageData.preview_url = jsonObject.getString("preview_url");
                    imageData.actual_preview_width = jsonObject.getInt("actual_preview_width");
                    imageData.actual_preview_height = jsonObject.getInt("actual_preview_height");
                    imageData.sample_url = jsonObject.getString("sample_url");
                    imageData.sample_width = jsonObject.getInt("sample_width");
                    imageData.sample_height = jsonObject.getInt("sample_height");
                    imageData.rating = jsonObject.getString("rating");
                    imageData.width = jsonObject.getInt("width");
                    imageData.height = jsonObject.getInt("height");
                    // calculate ImageView height manually
                    imageData.layout_height = Math.round((YandereApplication.getScreenWidth() / 2 - (8 + 6 + 10) * (YandereApplication.getDpi() / 160f)) * imageData.actual_preview_height / imageData.actual_preview_width);
                    imageData.fragment = fragment;

                    if (!YandereApplication.isSafe() || imageData.rating.equalsIgnoreCase("s")) {
                        fragment.getImageDatas().add(imageData);
                    }
                }
                final int itemCount = fragment.getImageDatas().size() - positionStart;

                handler.post(new Runnable() {
                    public void run() {
                        fragment.getAdapter().notifyItemRangeInserted(positionStart, itemCount);
                    }
                });
            } catch (JSONException ignored) {
            }
        }

        @Override
        public void run() {
            isDownloading = true;
            String json = getJSON();
            if (json != null)
                parseJSON(json);
            isDownloading = false;
        }
    }
}
