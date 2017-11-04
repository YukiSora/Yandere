package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.nex3z.flowlayout.FlowLayout;
import com.robertlevonyan.views.chip.Chip;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.modles.ImageData;

public class ImageActivity extends Activity {
    private Button downloadButton;
    private DownloadManager downloadManager;
    private Handler handler;
    private ImageData imageData;
    private ImageView imageView;
    private LinearLayout photoLayout;
    private PhotoView photoView;
    private ScheduledFuture scheduledFuture;
    private SmoothProgressBar smoothProgressBar;
    private String filename;
    private boolean isDownloading;
    private boolean isShowUpdateTags;
    private long requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageData = (ImageData)getIntent().getSerializableExtra("imageData");
        filename = String.format("yandere_%s.%s", imageData.id, imageData.file_ext);

        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        handler = new Handler();

        initView();
    }

    @Override
    public void onBackPressed() {
        if (photoLayout.getVisibility() == View.VISIBLE) {
            photoLayout.setVisibility(View.INVISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }

    private void initView() {
        downloadButton = findViewById(R.id.downloadButton);
        FlowLayout flowLayout = findViewById(R.id.flowLayout);
        imageView = findViewById(R.id.imageView);
        photoLayout = findViewById(R.id.photoLayout);
        photoView = findViewById(R.id.photoView);
        smoothProgressBar = findViewById(R.id.smoothProgressBar);

        // image layout
        RelativeLayout imageLayout = findViewById(R.id.imageLayout);
        imageLayout.getLayoutParams().height = Math.round(YandereApplication.getLargeImageLayoutWidth() * imageData.sample_height / imageData.sample_width);

        // image view
        loadPreviewImage(imageData);

        // photo view
        photoView.setScaleLevels(1f, 2f, 5f);

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
                isShowUpdateTags = true;
            }
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ImageActivity.this, SearchActivity.class);
                    intent.putExtra("query", ((Chip)view).getChipText().replace(" ", "_"));

                    startActivity(intent);
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
                    downloadButton.startAnimation(AnimationUtils.loadAnimation(ImageActivity.this, R.anim.anim_shake));
                }
            }
        });
    }

    private void loadPreviewImage(final ImageData imageData) {
        Picasso.with(this)
                .load(imageData.preview_url)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(@NonNull Palette palette) {
                                int vibrant = palette.getVibrantColor(ContextCompat.getColor(ImageActivity.this, R.color.progressBarColor));
                                smoothProgressBar.setSmoothProgressDrawableColor(vibrant);
                            }
                        });
                        loadImage(imageData);
                    }

                    @Override
                    public void onError() {
                        loadPreviewImage(imageData);
                    }
                });
    }

    private void loadImage(final ImageData imageData) {
        Picasso.with(this)
                .load(imageData.sample_url)
                .placeholder(imageView.getDrawable())
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                smoothProgressBar.progressiveStop();
                                smoothProgressBar.setVisibility(View.GONE);

                                if (isShowUpdateTags) {
                                    Toast.makeText(ImageActivity.this, "Tags may be able to update.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 500);

                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                photoView.setImageDrawable(imageView.getDrawable());
                                photoView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        photoLayout.setVisibility(View.INVISIBLE);
                                    }
                                });
                                photoLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        loadImage(imageData);
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
