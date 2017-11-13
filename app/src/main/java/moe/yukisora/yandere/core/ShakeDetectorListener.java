package moe.yukisora.yandere.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.squareup.seismic.ShakeDetector;

import java.util.List;

import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.activities.ImageActivity;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShakeDetectorListener implements ShakeDetector.Listener {
    private Call<List<ImageData>> call;
    private Context context;
    private ShakeDetectorCallback callback;
    private YandereService service;
    private boolean isLoading;

    public ShakeDetectorListener(Context context, ShakeDetectorCallback callback) {
        this.context = context;
        this.callback = callback;

        service = ServiceGenerator.generate(YandereService.class);
    }

    @Override
    public void hearShake() {
        if (!isLoading) {
            isLoading = true;
            callback.onStart();
            enqueueService(service);
        }
    }

    public void stopLoading() {
        call.cancel();
        isLoading = false;
    }

    @SuppressWarnings("ConstantConditions")
    private void enqueueService(final YandereService service) {
        call = service.getRandom();
        call.enqueue(new Callback<List<ImageData>>() {
            @Override
            public void onResponse(@NonNull Call<List<ImageData>> call, @NonNull Response<List<ImageData>> response) {
                if (response.isSuccessful() && response.body() != null && isLoading) {
                    ImageData imageData = response.body().get(0);
                    if (!YandereApplication.isSafe() || imageData.rating.equalsIgnoreCase("s")) {
                        isLoading = false;
                        Intent intent = new Intent(context, ImageActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("imageData", imageData);
                        bundle.putInt("type", ImageActivity.RANDOM);
                        intent.putExtras(bundle);

                        callback.onFinish();

                        context.startActivity(intent);
                    }
                    else {
                        enqueueService(service);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ImageData>> call, @NonNull Throwable throwable) {
                enqueueService(service);
            }
        });
    }

    public interface ShakeDetectorCallback {
        void onStart();
        void onFinish();
    }
}
