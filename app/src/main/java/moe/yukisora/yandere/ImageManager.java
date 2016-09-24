package moe.yukisora.yandere;

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

public class ImageManager {
    private  static ImageCache<ImageData, Bitmap> imageCache;
    private static ImageManager imageManager;
    private Handler handler;
    private boolean isDownloading;

    private ImageManager() {
        handler = new Handler();
        imageCache = new ImageCache<>(MainActivity.getMaxMemory() / 2);
    }

    public static ImageManager getInstance() {
        if (imageManager == null)
            imageManager = new ImageManager();

        return imageManager;
    }

    public static ImageCache<ImageData, Bitmap> getImageCache() {
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
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ImageData imageData = new ImageData();
                    imageData.id = jsonObject.getInt("id");
                    imageData.tags = jsonObject.getString("tags");
                    imageData.file_size = jsonObject.getInt("file_size");
                    imageData.file_ext = jsonObject.getString("file_ext");
                    imageData.file_url = jsonObject.getString("file_url");
                    imageData.preview_url = jsonObject.getString("preview_url");
                    imageData.actual_preview_width = jsonObject.getInt("actual_preview_width");
                    imageData.actual_preview_height = jsonObject.getInt("actual_preview_height");
                    imageData.rating = jsonObject.getString("rating");
                    imageData.width = jsonObject.getInt("width");
                    imageData.height = jsonObject.getInt("height");
                    imageCache.get(imageData);

                    fragment.getImageDatas().add(imageData);
                    handler.post(new Runnable() {
                        public void run() {
                            fragment.getAdapter().notifyItemInserted(fragment.getImageDatas().size() - 1);
                        }
                    });
                }
            } catch (JSONException ignored) {
            }
        }

        @Override
        public void run() {
            isDownloading = true;
            parseJSON(getJSON());
            isDownloading = false;
        }
    }
}
