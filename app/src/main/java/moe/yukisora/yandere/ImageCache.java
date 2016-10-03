package moe.yukisora.yandere;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.LruCache;

public class ImageCache extends LruCache<Integer, Bitmap> {
    private Handler handler;
    private ImageCache imageCache;
    private ImageData currentImageData;

    public ImageCache(int maxSize) {
        super(maxSize);

        handler = new Handler();
        imageCache = this;
    }

    public Bitmap getByImageData(ImageData imageData) {
        //should be single thread
        currentImageData = imageData;

        return get(imageData.id);
    }

    @Override
    protected int sizeOf(Integer key, Bitmap value) {
        return value.getByteCount();
    }

    @Override
    protected Bitmap create(Integer key) {
        //create a thread to download image, return a placeholder image first
        new DownloadImageTask(currentImageData).start();

        //java.lang.IllegalStateException: Fragment not attached to Activity
        try {
            return BitmapFactory.decodeResource(currentImageData.fragment.getResources(), R.drawable.placeholder_small);
        } catch (IllegalStateException e) {
            return null;
        }
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
                handler.post(new Runnable() {
                    public void run() {
                        ((PostFragment)imageData.fragment).getAdapter().notifyItemChanged(imageData.list_id);
                    }
                });
            }
        }
    }
}
