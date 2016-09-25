package moe.yukisora.yandere;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.LruCache;

public class ImageCache<K, V> extends LruCache<K, V> {
    private Handler handler;
    private ImageCache imageCache;

    public ImageCache(int maxSize) {
        super(maxSize);

        handler = new Handler();
        imageCache = this;
    }

    @Override
    protected int sizeOf(K key, V value) {
        return ((Bitmap)value).getByteCount();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected V create(K key) {
        new DownloadImageTask((ImageData)key).start();

        //java.lang.IllegalStateException: Fragment not attached to Activity
        try {
            return (V)BitmapFactory.decodeResource(((ImageData)key).fragment.getResources(), R.drawable.placeholder_small);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Override
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
        if (newValue == null)
            ((ImageData)key).isPlaceholder = true;
    }

    private class DownloadImageTask extends Thread {
        private ImageData imageData;

        DownloadImageTask(ImageData imageData) {
            this.imageData = imageData;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            Bitmap bitmap = ImageManager.downloadImage(imageData.preview_url);
            if (bitmap != null) {
                imageCache.put(imageData, bitmap);
                imageData.isPlaceholder = false;
                handler.post(new Runnable() {
                    public void run() {
                        ((PostFragment)imageData.fragment).getAdapter().notifyItemChanged(imageData.list_id);
                    }
                });
            }
        }
    }
}
