package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.os.Bundle;

import java.util.List;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.core.ServiceGenerator;
import moe.yukisora.yandere.fragments.PostFragment;
import moe.yukisora.yandere.interfaces.GetCallGenerator;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;

public class SearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final String query = getIntent().getStringExtra(SearchManager.QUERY);

        // create fragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        PostFragment fragment = PostFragment.newInstance(true);
        fragment.setGenerator(new GetCallGenerator() {
            private String tags = query;

            @Override
            public Call<List<ImageData>> getCall(int page) {
                YandereService service = ServiceGenerator.generate(YandereService.class);

                return service.getPosts(page, tags);
            }
        });
        fragmentTransaction.add(R.id.searchFragment, fragment);
        fragmentTransaction.commit();
    }
}
