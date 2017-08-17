package moe.yukisora.yandere.modles;

import java.io.Serializable;

public class ImageData implements Serializable {
    public int id;
    public int list_id;
    public String tags;
    public String author;
    public String source;
    public int file_size;
    public String file_ext;
    public String file_url;
    public String preview_url;
    public int actual_preview_width;
    public int actual_preview_height;
    public String sample_url;
    public int sample_width;
    public int sample_height;
    public String rating;
    public int width;
    public int height;
    public int layout_height;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImageData && id == ((ImageData)obj).id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
