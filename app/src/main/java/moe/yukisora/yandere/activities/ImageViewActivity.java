package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.modles.ImageData;
import moe.yukisora.yandere.ui.FlowLayout;

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

        downloadButton = findViewById(R.id.download);
        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        handler = new Handler();
        imageData = (ImageData)getIntent().getSerializableExtra("imageData");
        imageView = findViewById(R.id.fullSizeImageView);
        FlowLayout flowLayout = findViewById(R.id.flowLayout);

        // RelativeLayout
        findViewById(R.id.fullSizeImageLayout).getLayoutParams().height = Math.round((YandereApplication.getScreenWidth() - (16 + 6 + 10) * (YandereApplication.getDpi() / 160f)) * imageData.sample_height / imageData.sample_width);

        // image data
        String imageIdStr = String.format("yande.re Id: %d", imageData.id);
        ((TextView)findViewById(R.id.imageId)).setText(imageIdStr);
        String authorStr = String.format("Author: %s", imageData.author);
        ((TextView)findViewById(R.id.author)).setText(authorStr);
        String imageSizeStr = String.format("Image Size: %d x %d", imageData.width, imageData.height);
        ((TextView)findViewById(R.id.imageSize)).setText(imageSizeStr);
        String fileSizeStr = String.format("File Size: %.2fkb", imageData.file_size / 1024f);
        ((TextView)findViewById(R.id.fileSize)).setText(fileSizeStr);
        String sourceStr = String.format("Source: %s", imageData.source);
        ((TextView)findViewById(R.id.source)).setText(sourceStr);

        // tag
        for (String tag : imageData.tags.split(" ")) {
            TextView textView = new TextView(this);

            textView.setText(tag.replace("_", " "));

            // layout
            FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 5, 10, 5);
            textView.setLayoutParams(layoutParams);


            // color
            textView.setTextColor(Color.parseColor(getResources().getStringArray(R.array.tagColor)[YandereApplication.getTags().get(tag)]));
            textView.setBackgroundResource(R.drawable.tag_selector);

            // click
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

        // download button
        File file = new File(YandereApplication.getDirectory(), String.format("yandere_%s.%s", imageData.id, imageData.file_ext));
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

        loadImage(imageData);
    }

    private void loadImage(final ImageData imageData) {
        imageView.getLayoutParams().width = YandereApplication.getSmallPlaceholderSize() / (int)(YandereApplication.getDpi() / 160f);

        Picasso.with(this)
                .load(imageData.sample_url)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.reload)
                .noFade()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                        imageView.setOnClickListener(null);
                    }

                    @Override
                    public void onError() {
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadImage(imageData);
                            }
                        });
                    }
                });
    }

    private class SaveImageTask extends Thread {
        @Override
        public void run() {
            String filename = String.format("yandere_%s.%s", imageData.id, imageData.file_ext);

            // start a request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageData.file_url));
            request.setDestinationInExternalPublicDir(YandereApplication.APPLICATION_FOLDER, filename);
            long id = downloadManager.enqueue(request);

            // processing
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
            boolean successful = true;
            while (true) {
                try (Cursor cursor = downloadManager.query(query)) {
                    cursor.moveToFirst();

                    // download status
                    int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        break;
                    }
                    if (status == DownloadManager.STATUS_FAILED) {
                        successful = false;
                        break;
                    }

                    // download process
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

                // display in photo gallery
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(new File(YandereApplication.getDirectory(), filename))));
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
