package moe.yukisora.yandere;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PostFragment extends Fragment {
    private ArrayList<ImageData> imageDatas;
    private Fragment fragment;
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
        imageDatas = new ArrayList<>();
        fragment = this;
        url = getArguments().getString("url");
        isScrolled = getArguments().getBoolean("isScrolled");
        page = 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        initRecyclerView(view);
        ImageManager.getInstance().setDownloading(false);
        ImageManager.getInstance().loadImage(fragment, url + page++);

        return view;
    }

    private void initRecyclerView(View view) {
        adapter = new RecyclerViewAdapter(fragment);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        if (isScrolled)
            recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener() {
                @Override
                public void onBottom() {
                    ImageManager.getInstance().loadImage(fragment, url + page++);
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
