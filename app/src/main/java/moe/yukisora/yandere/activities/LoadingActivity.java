package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.core.ServiceGenerator;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.TagData;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class LoadingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // initialize variable
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // create folder
        File directory = new File(Environment.getExternalStorageDirectory(), "Yandere");
        if (!directory.exists()) {
            directory.mkdir();
        }
        YandereApplication.setDirectory(directory);

        // search manager
        YandereApplication.setSearchManager((SearchManager)getSystemService(Context.SEARCH_SERVICE));

        // safe option
        YandereApplication.setSafe(preferences.getBoolean("isSafe", true));

        // display attribute
        YandereApplication.setDpi(metrics.densityDpi);
        YandereApplication.setScreenWidth(metrics.widthPixels);

        // max memory
        YandereApplication.setMaxMemory(1024 * 1024 * ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass());

        // placeholder image size
        YandereApplication.setSmallPlaceholderSize(BitmapFactory.decodeResource(getResources(), R.drawable.loading).getWidth());

        // http client
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        Request requestWithUserAgent = originalRequest.newBuilder()
                                .header("User-Agent", "Mozilla/5.0")
                                .build();
                        return chain.proceed(requestWithUserAgent);
                    }
                })
                .build();
        YandereApplication.setOkHttpClient(okHttpClient);

        // set picasso
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException ignored) {
        }

        // pre download and move to MainActivity
        downloadTags();
    }

    private void downloadTags() {
        YandereService service = ServiceGenerator.generate(YandereService.class);

        Call<TagData> call = service.getTags();
        call.enqueue(new Callback<TagData>() {
            @Override
            public void onResponse(Call<TagData> call, retrofit2.Response<TagData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //ArrayList<String> tags = new ArrayList<>();
                    HashMap<String, Integer> tags = new HashMap<>();

                    int lastDigit = -1;
                    for (String tag : response.body().data.replaceAll("\\s", "").split("`")) {
                        if (!(tag.length() == 1 && tag.charAt(0) >= '0' && tag.charAt(0) <= '9')) {
                            if (lastDigit != -1)
                                tags.put(tag, lastDigit);
                            lastDigit = -1;
                        }
                        else {
                            lastDigit = tag.charAt(0) - '0';
                        }
                    }

                    YandereApplication.setTags(tags);

                    LoadingActivity.this.startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                    LoadingActivity.this.finish();
                }
            }

            @Override
            public void onFailure(Call<TagData> call, Throwable t) {
            }
        });
    }
}
