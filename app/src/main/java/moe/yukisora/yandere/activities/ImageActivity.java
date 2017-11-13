package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.MotionEvent;
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
import com.squareup.seismic.ShakeDetector;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.core.DownloadRequestManager;
import moe.yukisora.yandere.core.ShakeDetectorListener;
import moe.yukisora.yandere.modles.ImageData;

public class ImageActivity extends Activity {
    public static final int NONE = 0;
    public static final int RANDOM = 1;
    private Button downloadButton;
    private DownloadManager downloadManager;
    private Handler handler;
    private ImageData imageData;
    private ImageView imageView;
    private LinearLayout photoLayout;
    private PhotoView photoView;
    private RelativeLayout progressBar;
    private ScheduledExecutorService scheduleTaskExecutor;
    private ScheduledFuture scheduledFuture;
    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;
    private ShakeDetectorListener shakeDetectorListener;
    private SmoothProgressBar smoothProgressBar;
    private String filename;
    private boolean isDownloading;
    private boolean isRandomizable;
    private boolean isShowUpdateTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        int type = getIntent().getIntExtra("type", NONE);
        isRandomizable = (type & RANDOM) == RANDOM;
        imageData = (ImageData)getIntent().getSerializableExtra("imageData");
        filename = String.format("yandere_%s.%s", imageData.id, imageData.file_ext);

        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        handler = new Handler();
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        shakeDetectorListener = new ShakeDetectorListener(this, new ShakeDetectorListener.ShakeDetectorCallback() {
            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                finish();
            }
        });
        shakeDetector = new ShakeDetector(shakeDetectorListener);
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isRandomizable) {
            shakeDetector.start(sensorManager);
        }
    }

    @Override
    protected void onStop() {
        shakeDetector.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
            shakeDetectorListener.stopLoading();
        }
        else if (photoLayout.getVisibility() == View.VISIBLE) {
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
        progressBar = findViewById(R.id.progressBar);
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
        long requestId = DownloadRequestManager.getInstance().get(imageData.id);
        File file = new File(YandereApplication.getExternalDirectory(), filename);
        if (requestId != 0) {
            isDownloading = true;
            scheduledFuture = scheduleTaskExecutor.scheduleAtFixedRate(new SaveImageRunnable(requestId), 0, 100, TimeUnit.MILLISECONDS);
        }
        else if (file.exists()) {
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
                    request.allowScanningByMediaScanner();
                    request.setTitle(filename);
                    long requestId = downloadManager.enqueue(request);
                    DownloadRequestManager.getInstance().put(imageData.id, requestId);

                    scheduledFuture = scheduleTaskExecutor.scheduleAtFixedRate(new SaveImageRunnable(requestId), 100, 100, TimeUnit.MILLISECONDS);
                }
                else {
                    downloadButton.startAnimation(AnimationUtils.loadAnimation(ImageActivity.this, R.anim.anim_shake));
                }
            }
        });

        // progress bar
        progressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
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

    @SuppressWarnings("ConstantConditions")
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

                        photoView.setImageDrawable(imageView.getDrawable().getConstantState().newDrawable().mutate());
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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
        private long requestId;

        SaveImageRunnable(long requestId) {
            this.requestId = requestId;
        }

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
                            isDownloading = false;
                            DownloadRequestManager.getInstance().delete(requestId);
                        }
                    });
                    scheduledFuture.cancel(false);
                }
                else if (status == DownloadManager.STATUS_FAILED) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadButton.setText(R.string.download);
                            isDownloading = false;
                            DownloadRequestManager.getInstance().delete(requestId);
                        }
                    });
                    scheduledFuture.cancel(false);
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
