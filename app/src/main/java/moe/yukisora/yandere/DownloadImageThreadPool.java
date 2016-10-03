package moe.yukisora.yandere;

import android.graphics.Bitmap;
import android.os.Handler;

import java.util.ArrayList;

public class DownloadImageThreadPool {
    private final static int MAX_CURRENT_REQUEST = 20;
    private static DownloadImageThreadPool downloadImageThreadPool;
    private ArrayList<ImageData> requests;
    private Handler handler;
    private ImageCache imageCache;
    private boolean active;
    private int currentRequest;

    private DownloadImageThreadPool() {
        requests = new ArrayList<>();
        handler = new Handler();
        active = true;
    }

    public static DownloadImageThreadPool getInstance() {
        if (downloadImageThreadPool == null)
            downloadImageThreadPool = new DownloadImageThreadPool();

        return downloadImageThreadPool;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setImageCache(ImageCache imageCache) {
        this.imageCache = imageCache;
    }

    public boolean isEmpty() {
        return currentRequest == 0;
    }

    public void addRequest(ImageData imageData) {
        if (active) {
            synchronized (this) {
                requests.add(0, imageData);
            }
            startRequest();
        }
    }

    private void startRequest() {
        ImageData imageData = null;
        synchronized (this) {
            if (requests.size() > 0 && currentRequest < MAX_CURRENT_REQUEST) {
                imageData = requests.get(0);
                requests.remove(0);
                currentRequest++;
            }
        }
        if (imageData != null)
            new DownloadImageTask(imageData).start();
    }

    private void finishRequest() {
        synchronized (this) {
            currentRequest--;
        }
        startRequest();
    }

    private class DownloadImageTask extends Thread {
        private ImageData imageData;

        DownloadImageTask(ImageData imageData) {
            this.imageData = imageData;
        }

        @Override
        public void run() {
            Bitmap bitmap = ImageManager.downloadImage(imageData.preview_url);
            if (bitmap != null) {
                imageCache.put(imageData.id, bitmap);
                if (active)
                    handler.post(new Runnable() {
                        public void run() {
                            ((PostFragment)imageData.fragment).getAdapter().notifyItemChanged(imageData.list_id);
                        }
                    });
            }
            finishRequest();
        }
    }
}
