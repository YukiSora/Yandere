package moe.yukisora.yandere;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

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
