package moe.yukisora.yandere.interfaces;

import java.util.List;

import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YandereService {
    @GET("post.json?limit=20")
    Call<List<ImageData>> getPosts(@Query("page") int page, @Query("tag") String tag);
}
