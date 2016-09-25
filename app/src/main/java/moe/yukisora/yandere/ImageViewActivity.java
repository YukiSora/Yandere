package moe.yukisora.yandere;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageViewActivity extends Activity {
    private Handler handler;
    private ImageData imageData;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        handler = new Handler();

        Intent intent = this.getIntent();
        imageData = (ImageData)intent.getSerializableExtra("imageData");
        imageView = (ImageView)findViewById(R.id.fullSizeImageView);
        imageView.setImageResource(R.drawable.placeholder_large);

        String imageSizeStr = String.format("Image Size: %d x %d", imageData.width, imageData.height);
        ((TextView)findViewById(R.id.imageSize)).setText(imageSizeStr);
        String fileSizeStr = String.format("File Size: %.2fkb", imageData.file_size / 1024f);
        ((TextView)findViewById(R.id.fileSize)).setText(fileSizeStr);

        new DownloadImageTask().start();
    }

    private class DownloadImageTask extends Thread {
        @Override
        public void run() {
            final Bitmap bitmap = ImageManager.downloadImage(imageData.sample_url);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    }
}
