package moe.yukisora.yandere.interfaces;

import java.util.List;

import moe.yukisora.yandere.modles.ImageData;
import moe.yukisora.yandere.modles.TagsData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YandereService {
    @GET("post.json?limit=20")
    Call<List<ImageData>> getPosts(@Query("page") int page, @Query("tags") String tags);

    @GET("post.json?limit=1&tags=order:random")
    Call<List<ImageData>> getRandom();

    @GET("post/popular_recent.json?limit=20")
    Call<List<ImageData>> getPopulars(@Query("page") int page, @Query("period") String period);

    @GET("tag/summary.json")
    Call<TagsData<String>> getTags();
}
