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

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.modles.ImageData;

public class ImageViewActivity extends Activity {
    private Button downloadButton;
    private DownloadManager downloadManager;
    private Handler handler;
    private ImageData imageData;
    private ImageView imageView;
    private boolean isDownloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        downloadButton = findViewById(R.id.downloadButton);
        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        handler = new Handler();
        imageData = (ImageData)getIntent().getSerializableExtra("imageData");
        imageView = findViewById(R.id.imageView);
        FlowLayout flowLayout = findViewById(R.id.flowLayout);

        // image layout
        RelativeLayout imageLayout = findViewById(R.id.imageLayout);
        imageLayout.getLayoutParams().height = Math.round((YandereApplication.getScreenWidth() - (16 + 6 + 10) * (YandereApplication.getDpi() / 160f)) * imageData.sample_height / imageData.sample_width);

        // image view
        imageView.getLayoutParams().width = YandereApplication.getSmallPlaceholderSize() / (int)(YandereApplication.getDpi() / 160f);
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
            chip.setPadding(10, 0, 10, 0);
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
        File file = new File(YandereApplication.getDirectory(), String.format("yandere_%s.%s", imageData.id, imageData.file_ext));
        if (file.exists()) {
            downloadButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
            downloadButton.setEnabled(false);
        }
        else {
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isDownloading) {
                        isDownloading = true;
                        new SaveImageTask().start();
                    }
                    else {
                        downloadButton.startAnimation(AnimationUtils.loadAnimation(ImageViewActivity.this, R.anim.shake_anim));
                    }
                }
            });
        }
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
                        downloadButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
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
