package moe.yukisora.yandere;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

        String imageIdStr = String.format("yande.re Id: %d", imageData.id);
        ((TextView)findViewById(R.id.imageId)).setText(imageIdStr);
        String imageSizeStr = String.format("Image Size: %d x %d", imageData.width, imageData.height);
        ((TextView)findViewById(R.id.imageSize)).setText(imageSizeStr);
        String fileSizeStr = String.format("File Size: %.2fkb", imageData.file_size / 1024f);
        ((TextView)findViewById(R.id.fileSize)).setText(fileSizeStr);

        File file = new File(MainActivity.getDirectory(), "yandere_" + imageData.id + "." + imageData.file_ext);
        if (file.exists()) {
            ((ImageView)findViewById(R.id.downloadImage)).setImageResource(R.drawable.done);
            findViewById(R.id.download).setEnabled(false);
        }
        else {
            findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SaveImageTask().start();
                }
            });
        }

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

    private class SaveImageTask extends Thread {
        @Override
        public void run() {
            File file = new File(MainActivity.getDirectory(), "yandere_" + imageData.id + "." + imageData.file_ext);
            Bitmap bitmap = ImageManager.downloadImage(imageData.file_url);
            try (FileOutputStream out = new FileOutputStream(file)) {
                if (imageData.file_ext.equalsIgnoreCase("png"))
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                else if (imageData.file_ext.equalsIgnoreCase("jpg"))
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (IOException ignore) {
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    ((ImageView)findViewById(R.id.downloadImage)).setImageResource(R.drawable.done);
                    findViewById(R.id.download).setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Image is downloaded.", Toast.LENGTH_SHORT).show();
                }
            });

            //display in photo gallery
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(file)));
        }
    }
}
