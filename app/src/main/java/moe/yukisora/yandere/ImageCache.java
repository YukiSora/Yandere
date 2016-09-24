package moe.yukisora.yandere;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
        new DownloadImageTask((ImageData)key).execute();

        return (V)BitmapFactory.decodeResource(((ImageData)key).fragment.getResources(), R.drawable.placeholder_small);
    }

    private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private ImageData imageData;

        DownloadImageTask(ImageData imageData) {
            this.imageData = imageData;
        }

        protected Bitmap doInBackground(Void... args) {
            return ImageManager.downloadImage(imageData.preview_url);
        }

        @SuppressWarnings("unchecked")
        protected void onPostExecute(Bitmap bitmap) {
            imageCache.put(imageData, bitmap);
            handler.post(new Runnable() {
                public void run() {
                    ((PostFragment)imageData.fragment).getAdapter().notifyItemChanged(imageData.list_id);
                }
            });
        }
    }
}
