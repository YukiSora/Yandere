package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import moe.yukisora.yandere.R;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

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

        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException ignored) {
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
