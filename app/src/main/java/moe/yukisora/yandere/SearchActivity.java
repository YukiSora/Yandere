package moe.yukisora.yandere;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

public class SearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();

        String query = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            query = intent.getStringExtra(SearchManager.QUERY);

        //create fragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        PostFragment fragment = PostFragment.newInstance("https://yande.re/post.json?limit=20&tags=" + query + "&page=", true);
        fragmentTransaction.add(R.id.searchFragment, fragment);
        fragmentTransaction.commit();
    }
}
