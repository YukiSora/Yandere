package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import java.util.List;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.core.ServiceGenerator;
import moe.yukisora.yandere.fragments.ListFragment;
import moe.yukisora.yandere.interfaces.CallGenerator;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;

public class SearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final String query = getIntent().getStringExtra("query");

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        ListFragment fragment = ListFragment.newInstance(ListFragment.LOAD);
        fragment.setGenerator(new CallGenerator() {
            private String tags = query;

            @Override
            public Call<List<ImageData>> generateCall(int page) {
                YandereService service = ServiceGenerator.generate(YandereService.class);

                return service.getPosts(page, tags);
            }
        });
        fragmentTransaction.add(R.id.searchFragment, fragment);
        fragmentTransaction.commit();
    }
}
