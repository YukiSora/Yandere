package moe.yukisora.yandere;

import android.app.Fragment;
import android.graphics.Bitmap;

import java.io.Serializable;

public class ImageData implements Serializable {
    public int id;
    public int list_id;
    public String tags;
    public int file_size;
    public String file_ext;
    public String file_url;
    public String preview_url;
    public int actual_preview_width;
    public int actual_preview_height;
    public String rating;
    public int width;
    public int height;
    transient public Fragment fragment;

    public Bitmap getBitmap() {
        return ImageManager.getImageCache().get(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImageData && id == ((ImageData)obj).id;
    }
}
