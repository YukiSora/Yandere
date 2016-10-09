package moe.yukisora.yandere;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
        imageData = (ImageData)getIntent().getSerializableExtra("imageData");
        imageView = (ImageView)findViewById(R.id.fullSizeImageView);
        FlowLayout flowLayout = (FlowLayout)findViewById(R.id.flowLayout);

        //RelativeLayout
        findViewById(R.id.fullSizeImageLayout).getLayoutParams().height = Math.round((MainActivity.getScreenWidth() - (16 + 6 + 10) * (MainActivity.getDpi() / 160f)) * imageData.sample_height / imageData.sample_width);
        //image view
        imageView.setImageResource(R.drawable.placeholder_large);
        imageView.getLayoutParams().width = 200;


        //image data
        String imageIdStr = String.format("yande.re Id: %d", imageData.id);
        ((TextView)findViewById(R.id.imageId)).setText(imageIdStr);
        String imageSizeStr = String.format("Image Size: %d x %d", imageData.width, imageData.height);
        ((TextView)findViewById(R.id.imageSize)).setText(imageSizeStr);
        String fileSizeStr = String.format("File Size: %.2fkb", imageData.file_size / 1024f);
        ((TextView)findViewById(R.id.fileSize)).setText(fileSizeStr);

        //tag
        for (String tag : imageData.tags.split(" ")) {
            TextView textView = new TextView(this);

            textView.setText(tag.replace("_", " "));

            //layout
            FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 5, 10, 5);
            textView.setLayoutParams(layoutParams);
            textView.setBackgroundResource(R.drawable.tag_selector);

            //click
            textView.setClickable(true);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("poi", ((TextView)v).getText().toString());
                }
            });

            flowLayout.addView(textView);
        }

        //download button
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
                    imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
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
            if (bitmap != null) {
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
            else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Image download failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
