package moe.yukisora.yandere;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private Activity activity;
    private ArrayList<ImageData> images;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        images = new ArrayList<>();

        initRecyclerView();
        ImageManager.getInstance().loadImage(activity);
    }

    private void initRecyclerView() {
        adapter = new RecyclerViewAdapter(activity);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener() {
            @Override
            public void onBottom() {
                ImageManager.getInstance().loadImage(activity);
            }
        });
    }

    public ArrayList<ImageData> getImages() {
        return images;
    }

    public RecyclerViewAdapter getAdapter() {
        return adapter;
    }
}
