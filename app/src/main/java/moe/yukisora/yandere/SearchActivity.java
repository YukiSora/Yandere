package moe.yukisora.yandere;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.os.Bundle;

public class SearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        String query = getIntent().getStringExtra(SearchManager.QUERY);

        //create fragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        PostFragment fragment = PostFragment.newInstance("https://yande.re/post.json?limit=20&tags=" + query + "&page=", true);
        fragmentTransaction.add(R.id.searchFragment, fragment);
        fragmentTransaction.commit();
    }
}
