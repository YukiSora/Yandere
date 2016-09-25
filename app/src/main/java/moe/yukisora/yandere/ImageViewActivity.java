package moe.yukisora.yandere;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageViewActivity extends Activity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Intent intent = this.getIntent();
        ImageData imageData = (ImageData)intent.getSerializableExtra("imageData");
        imageView = (ImageView)findViewById(R.id.fullSizeImageView);
        imageView.setImageResource(R.drawable.placeholder_large);

        String imageSizeStr = String.format("Image Size: %d x %d", imageData.width, imageData.height);
        ((TextView)findViewById(R.id.imageSize)).setText(imageSizeStr);
        String fileSizeStr = String.format("File Size: %.2fkb", imageData.file_size / 1024f);
        ((TextView)findViewById(R.id.fileSize)).setText(fileSizeStr);

        new DownloadImageTask().execute(imageData.sample_url);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            return ImageManager.downloadImage(urls[0]);
        }

        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
