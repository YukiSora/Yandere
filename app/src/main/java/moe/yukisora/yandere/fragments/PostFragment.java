package moe.yukisora.yandere.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.footer.BallPulseView;
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout;

import java.util.ArrayList;
import java.util.List;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.adapters.RecyclerViewAdapter;
import moe.yukisora.yandere.core.ImageManager;
import moe.yukisora.yandere.interfaces.GetCallGenerator;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;

public class PostFragment extends Fragment {
    private RecyclerView recyclerView;

    private ArrayList<ImageData> imageDatas;
    private Call<List<ImageData>> call;
    private RecyclerViewAdapter adapter;
    private TwinklingRefreshLayout refreshLayout;
    private GetCallGenerator generator;
    private boolean isScrollable;
    private int page;

    public static PostFragment newInstance(boolean isScrollable) {
        Bundle args = new Bundle();
        PostFragment fragment = new PostFragment();
        args.putBoolean("isScrollable", isScrollable);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isScrollable = getArguments().getBoolean("isScrollable");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        initFragment();
        initRecyclerView(view);
        loadImage();
        refreshLayout.startRefresh();

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

        // RefreshLayout
        refreshLayout = view.findViewById(R.id.refreshLayout);
        BezierLayout headerView = new BezierLayout(getActivity());
        headerView.setRippleColor(ContextCompat.getColor(getActivity(), R.color.loadingRippleColor));
        headerView.setWaveColor(ContextCompat.getColor(getActivity(), R.color.loadingWaveColor));
        refreshLayout.setHeaderView(headerView);
        refreshLayout.setHeaderHeight(80);
        refreshLayout.setMaxHeadHeight(120);
        BallPulseView footerView = new BallPulseView(getActivity());
        footerView.setNormalColor(ContextCompat.getColor(getActivity(), R.color.normalColor));
        footerView.setAnimatingColor(ContextCompat.getColor(getActivity(), R.color.animating));
        refreshLayout.setBottomView(footerView);
        refreshLayout.setBottomHeight(80);
        refreshLayout.setMaxBottomHeight(120);
        refreshLayout.setOverScrollRefreshShow(false);
        refreshLayout.setAutoLoadMore(isScrollable);
        refreshLayout.setEnableLoadmore(isScrollable);
        refreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                call.cancel();
                initFragment();
                adapter.notifyDataSetChanged();
                loadImage();
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                loadImage();
            }
        });
    }

    public void loadImage() {
        call = generator.getCall(page);
        ImageManager.getInstance().loadImage(this, call, page == 1);
        page++;
    }

    public ArrayList<ImageData> getImageDatas() {
        return imageDatas;
    }

    public RecyclerViewAdapter getAdapter() {
        return adapter;
    }

    public TwinklingRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    public void setGenerator(GetCallGenerator generator) {
        this.generator = generator;
    }

    public void goToTop() {
        recyclerView.smoothScrollToPosition(0);
    }
}
