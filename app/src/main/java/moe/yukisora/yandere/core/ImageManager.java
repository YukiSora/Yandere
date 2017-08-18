package moe.yukisora.yandere.core;

import android.app.Fragment;
import android.os.Handler;

import java.util.List;

import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.fragments.PostFragment;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageManager {
    private static ImageManager imageManager;
    private Handler handler;
    private boolean isDownloading;

    private ImageManager() {
        handler = new Handler();
    }

    public static ImageManager getInstance() {
        if (imageManager == null)
            imageManager = new ImageManager();

        return imageManager;
    }

    public void loadImage(Fragment fragment, Call<List<ImageData>> call) {
        if (!isDownloading)
            downloadImageData((PostFragment)fragment, call);
    }

    private void downloadImageData(final PostFragment fragment, Call<List<ImageData>> call) {
        isDownloading = true;

        call.enqueue(new Callback<List<ImageData>>() {
            @Override
            public void onResponse(Call<List<ImageData>> call, Response<List<ImageData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final int positionStart = fragment.getImageDatas().size();
                    int positionEnd = positionStart;

                    for (ImageData imageData : response.body()) {
                        // calculate ImageView height manually
                        imageData.layout_height = Math.round((YandereApplication.getScreenWidth() / 2 - (8 + 6 + 10) * (YandereApplication.getDpi() / 160f)) * imageData.actual_preview_height / imageData.actual_preview_width);

                        if (!YandereApplication.isSafe() || imageData.rating.equalsIgnoreCase("s")) {
                            imageData.list_id = positionEnd++;
                            fragment.getImageDatas().add(imageData);
                        }
                    }
                    final int count = positionEnd - positionStart;

                    handler.post(new Runnable() {
                        public void run() {
                            fragment.getAdapter().notifyItemRangeInserted(positionStart, count);
                        }
                    });
                    isDownloading = false;
                }
            }

            @Override
            public void onFailure(Call<List<ImageData>> call, Throwable t) {
            }
        });
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }
}
