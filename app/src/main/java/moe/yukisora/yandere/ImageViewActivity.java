package moe.yukisora.yandere;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageViewActivity extends Activity {
    private ImageData imageData;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Intent intent = this.getIntent();
        imageData = (ImageData)intent.getSerializableExtra("imageData");
        imageView = (ImageView)findViewById(R.id.fullSizeImageView);
        imageView.setImageResource(R.mipmap.ic_launcher);

        new DownloadImageTask().execute(imageData.file_url);
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
