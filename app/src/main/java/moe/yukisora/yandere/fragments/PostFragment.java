package moe.yukisora.yandere.fragments;

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

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.adapters.RecyclerViewAdapter;
import moe.yukisora.yandere.core.ImageManager;
import moe.yukisora.yandere.interfaces.RecyclerViewOnScrollListener;
import moe.yukisora.yandere.interfaces.GetCallGenerator;
import moe.yukisora.yandere.modles.ImageData;

public class PostFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<ImageData> imageDatas;
    private Handler handler;
    private RecyclerViewAdapter adapter;
    private GetCallGenerator generator;
    private boolean isScrolled;
    private int page;

    public static PostFragment newInstance(GetCallGenerator generator, boolean isScrolled) {
        Bundle args = new Bundle();
        PostFragment fragment = new PostFragment();
        args.putSerializable("generator", generator);
        args.putBoolean("isScrolled", isScrolled);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        generator = (GetCallGenerator)getArguments().getSerializable("generator");
        isScrolled = getArguments().getBoolean("isScrolled");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        initFragment();
        initRecyclerView(view);
        ImageManager.getInstance().loadImage(this, generator.getCall(page++));

        return view;
    }

    private void initFragment() {
        imageDatas = new ArrayList<>();
        page = 1;
        ImageManager.getInstance().setDownloading(false);
    }

    private void initRecyclerView(View view) {
        // RecyclerView
        adapter = new RecyclerViewAdapter(this);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        // if not only one page, do load more
        if (isScrolled)
            recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener() {
                @Override
                public void onBottom() {
                    ImageManager.getInstance().loadImage(PostFragment.this, generator.getCall(page++));
                }
            });

        // SwipeRefreshLayout
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // wait downloading thread
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initFragment();
                        adapter.notifyDataSetChanged();
                        ImageManager.getInstance().loadImage(PostFragment.this, generator.getCall(page++));
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

    public void goToTop() {
        recyclerView.smoothScrollToPosition(0);
    }
}
