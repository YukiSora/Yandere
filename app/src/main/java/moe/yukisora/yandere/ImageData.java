package moe.yukisora.yandere;

import android.graphics.Bitmap;

public class ImageData {
    public int id;
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
    public Bitmap bitmap;

    @Override
    public boolean equals(Object obj) {
        return id == ((ImageData)obj).id;
    }
}

