package moe.yukisora.yandere;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static int maxMemory;
    private Activity activity;
    private ArrayList<ImageData> imageDatas;
    private RecyclerViewAdapter adapter;

    public static int getMaxMemory() {
        return maxMemory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        imageDatas = new ArrayList<>();
        maxMemory = 1024 * 1024 * ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

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

    public ArrayList<ImageData> getImageDatas() {
        return imageDatas;
    }

    public RecyclerViewAdapter getAdapter() {
        return adapter;
    }
}
