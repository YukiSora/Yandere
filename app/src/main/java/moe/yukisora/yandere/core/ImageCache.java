package moe.yukisora.yandere.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.core.DownloadImageThreadPool;
import moe.yukisora.yandere.modles.ImageData;

public class ImageCache extends LruCache<Integer, Bitmap> {
    private ImageData currentImageData;

    public ImageCache(int maxSize) {
        super(maxSize);
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
        DownloadImageThreadPool.getInstance().addRequest(currentImageData);

        //java.lang.IllegalStateException: Fragment not attached to Activity
        try {
            return BitmapFactory.decodeResource(currentImageData.fragment.getResources(), R.drawable.placeholder_small);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
