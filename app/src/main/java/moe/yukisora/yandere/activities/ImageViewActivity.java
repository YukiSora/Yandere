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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nex3z.flowlayout.FlowLayout;
import com.robertlevonyan.views.chip.Chip;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.modles.ImageData;

public class ImageViewActivity extends Activity {
    private Button downloadButton;
    private DownloadManager downloadManager;
    private Handler handler;
    private ImageView imageView;
    private ScheduledFuture scheduledFuture;
    private String filename;
    private boolean isDownloading;
    private long requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        final ImageData imageData = (ImageData)getIntent().getSerializableExtra("imageData");
        filename = String.format("yandere_%s.%s", imageData.id, imageData.file_ext);

        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        handler = new Handler();

        downloadButton = findViewById(R.id.downloadButton);
        FlowLayout flowLayout = findViewById(R.id.flowLayout);
        imageView = findViewById(R.id.imageView);

        // image layout
        RelativeLayout imageLayout = findViewById(R.id.imageLayout);
        imageLayout.getLayoutParams().height = Math.round(YandereApplication.getLargeImageLayoutWidth() * imageData.sample_height / imageData.sample_width);

        // image view
        imageView.getLayoutParams().width = YandereApplication.getSmallPlaceholderSize();
        loadImage(imageData);

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

        // tags
        for (String tag : imageData.tags.split(" ")) {
            Chip chip = new Chip(this);
            chip.setChipText(tag.replace("_", " "));
            chip.setStrokeSize(1);
            if (YandereApplication.getTags().containsKey(tag)) {
                chip.setTextColor(Color.parseColor(getResources().getStringArray(R.array.tagColor)[YandereApplication.getTags().get(tag)]));
                chip.setStrokeColor(Color.parseColor(getResources().getStringArray(R.array.tagColor)[YandereApplication.getTags().get(tag)]));
            }
            else {
                Toast.makeText(this, "Tags may be able to update.", Toast.LENGTH_SHORT).show();
            }
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ImageViewActivity.this, SearchActivity.class);

                    intent.putExtra(SearchManager.QUERY, ((Chip)view).getChipText().replace(" ", "_"));
                    ImageViewActivity.this.startActivity(intent);
                }
            });

            flowLayout.addView(chip);
        }

        // download button
        File file = new File(YandereApplication.getDirectory(), filename);
        if (file.exists()) {
            downloadButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
            downloadButton.setEnabled(false);
        }
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDownloading) {
                    isDownloading = true;

                    // start a request
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageData.file_url));
                    request.setDestinationInExternalPublicDir(YandereApplication.APPLICATION_FOLDER, filename);
                    requestId = downloadManager.enqueue(request);

                    // download progress schedule
                    ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
                    scheduledFuture = scheduleTaskExecutor.scheduleAtFixedRate(new SaveImageRunnable(), 100, 100, TimeUnit.MILLISECONDS);
                }
                else {
                    downloadButton.startAnimation(AnimationUtils.loadAnimation(ImageViewActivity.this, R.anim.shake_anim));
                }
            }
        });
    }

    private void loadImage(final ImageData imageData) {
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

    private class SaveImageRunnable implements Runnable {
        @Override
        public void run() {
            try (Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(requestId))) {
                cursor.moveToFirst();
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
                            downloadButton.setText(R.string.download);
                            downloadButton.setEnabled(false);
                            Toast.makeText(getApplicationContext(), "Image download successful.", Toast.LENGTH_SHORT).show();

                            // display in photo gallery
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(new File(YandereApplication.getDirectory(), filename))));

                            isDownloading = false;
                        }
                    });
                    scheduledFuture.cancel(false);
                }
                else if (status == DownloadManager.STATUS_FAILED) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Image download failed.", Toast.LENGTH_SHORT).show();

                            isDownloading = false;
                        }
                    });
                }
                else {
                    int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    final double progress = Math.abs(100.0 * bytesDownloaded / bytesTotal);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadButton.setText(String.format("%.2f%%", progress));
                        }
                    });
                }
            }
        }
    }
}
