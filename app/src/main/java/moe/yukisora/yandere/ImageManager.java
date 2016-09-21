package moe.yukisora.yandere;

import android.app.Activity;
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
    private static ImageManager imageManager;
    private int page;
    private boolean isDownloading;
    private ImageCache<ImageData, Bitmap> imageCache;
    private Handler handler;

    private ImageManager() {
        page = 1;
        imageCache = new ImageCache<>(MainActivity.getMaxMemory() / 2);
        handler = new Handler();
    }

    public static ImageManager getInstance() {
        if (imageManager == null)
            imageManager = new ImageManager();

        return imageManager;
    }

    public void loadImage(Activity activity) {
        if (!isDownloading)
            new DownloadImageData(activity).start();
    }

    private class DownloadImageData extends Thread {
        private MainActivity activity;

        DownloadImageData(Activity activity) {
            this.activity = (MainActivity)activity;
        }

        private String getJSON() {
            try {
                URL url = new URL("https://yande.re/post.json?page=" + page++);
                URLConnection connection = url.openConnection();
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
                    imageData.bitmap = imageCache.get(imageData);

                    activity.getImages().add(imageData);
                    handler.post(new Runnable() {
                        public void run() {
                            activity.getAdapter().notifyItemInserted(activity.getImages().size() - 1);
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
