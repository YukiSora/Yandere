package moe.yukisora.yandere;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PostFragment extends Fragment {
    private ArrayList<ImageData> imageDatas;
    private Handler handler;
    private RecyclerViewAdapter adapter;
    private String url;
    private boolean isScrolled;
    private int page;

    public static PostFragment newInstance(String url, boolean isScrolled) {
        Bundle args = new Bundle();
        PostFragment fragment = new PostFragment();
        args.putString("url", url);
        args.putBoolean("isScrolled", isScrolled);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        url = getArguments().getString("url");
        isScrolled = getArguments().getBoolean("isScrolled");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        initFragment();
        initRecyclerView(view);
        ImageManager.getInstance().loadImage(this, url + page++);

        return view;
    }

    private void initFragment() {
        imageDatas = new ArrayList<>();
        page = 1;
        ImageManager.getInstance().setDownloading(false);
        DownloadImageThreadPool.getInstance().setActive(true);
    }

    private void initRecyclerView(View view) {
        //RecyclerView
        adapter = new RecyclerViewAdapter(this);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        //if not only one page, do load more
        if (isScrolled)
            recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener() {
                @Override
                public void onBottom() {
                    ImageManager.getInstance().loadImage(PostFragment.this, url + page++);
                }
            });

        //SwipeRefreshLayout
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DownloadImageThreadPool.getInstance().setActive(false);
                //wait downloading thread
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        while (!DownloadImageThreadPool.getInstance().isEmpty())
                            ;
                        initFragment();
                        adapter.notifyDataSetChanged();
                        ImageManager.getInstance().loadImage(PostFragment.this, url + page++);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.argb(90, 102, 204, 255));
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
    }

    public ArrayList<ImageData> getImageDatas() {
        return imageDatas;
    }

    public RecyclerViewAdapter getAdapter() {
        return adapter;
    }
}
