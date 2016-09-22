package moe.yukisora.yandere;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImageCache<K, V> extends LruCache<K, V> {

    public ImageCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(K key, V value) {
        return ((Bitmap)value).getByteCount();
    }

    @Override
    protected V create(K key) {
        return (V)ImageManager.downloadImage(((ImageData)key).preview_url);
    }
}
