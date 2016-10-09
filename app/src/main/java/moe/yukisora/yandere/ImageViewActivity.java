package moe.yukisora.yandere;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ImageViewActivity extends Activity {
    private Button downloadButton;
    private DownloadManager downloadManager;
    private Handler handler;
    private ImageData imageData;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        downloadButton = (Button)findViewById(R.id.download);
        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
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


            //color
            textView.setTextColor(Color.parseColor(getResources().getStringArray(R.array.tagColor)[MainActivity.getTags().get(tag)]));
            textView.setBackgroundResource(R.drawable.tag_selector);

            //click
            textView.setClickable(true);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ImageViewActivity.this, SearchActivity.class);
                    intent.putExtra(SearchManager.QUERY, ((TextView)v).getText().toString().replace(" ", "_"));
                    ImageViewActivity.this.startActivity(intent);
                }
            });

            flowLayout.addView(textView);
        }

        //download button
        //why have sdcard? shouldn't be
        File file = new File("/sdcard" + MainActivity.getDirectory(), "yandere_" + imageData.id + "." + imageData.file_ext);
        if (file.exists()) {
            ((ImageView)findViewById(R.id.downloadImage)).setImageResource(R.drawable.done);
            downloadButton.setEnabled(false);
        }
        else {
            downloadButton.setOnClickListener(new View.OnClickListener() {
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
            String directory = MainActivity.getDirectory().toString();
            String filename = "yandere_" + imageData.id + "." + imageData.file_ext;

            //start a request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageData.file_url));
            request.setDestinationInExternalPublicDir(directory, filename);
            long id = downloadManager.enqueue(request);

            //processing
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
            boolean successful = true;
            while (true) {
                try (Cursor cursor = downloadManager.query(query)) {
                    cursor.moveToFirst();

                    //download status
                    int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        break;
                    }
                    if (status == DownloadManager.STATUS_FAILED) {
                        successful = false;
                        break;
                    }

                    //download process
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    double progress = Math.abs(100.0 * bytes_downloaded / bytes_total);
                    final String buttonTextStr = String.format("%.2f%%", progress);
                    handler.postAtFrontOfQueue(new Runnable() {
                        @Override
                        public void run() {
                            downloadButton.setText(buttonTextStr);
                        }
                    });
                }
            }

            if (successful) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView)findViewById(R.id.downloadImage)).setImageResource(R.drawable.done);
                        downloadButton.setText(R.string.download);
                        downloadButton.setEnabled(false);
                        Toast.makeText(getApplicationContext(), "Image is downloaded.", Toast.LENGTH_SHORT).show();
                    }
                });

                //display in photo gallery
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(new File(directory, filename))));
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
