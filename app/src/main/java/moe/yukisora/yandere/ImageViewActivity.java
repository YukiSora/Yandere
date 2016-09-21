package moe.yukisora.yandere;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

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

        new DownloadImage().execute(imageData.file_url);
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent","Mozilla/5.0");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                return BitmapFactory.decodeStream(connection.getInputStream());
            } catch (IOException e) {
            }

            return null;
        }

        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
