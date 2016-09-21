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
        try {
            URL url = new URL(((ImageData)key).preview_url);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            return (V)BitmapFactory.decodeStream(connection.getInputStream());
        } catch (IOException ignored) {
        }

        return null;
    }
}
