package moe.yukisora.yandere.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arlib.floatingsearchview.FloatingSearchView;
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
    private ArrayList<ImageData> imageDatas;
    private Call<List<ImageData>> call;
    private GetCallGenerator generator;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private TwinklingRefreshLayout refreshLayout;
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
        final FloatingSearchView floatingSearchView = view.findViewById(R.id.floatingSearchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.refreshLayout);

        // recycler view
        adapter = new RecyclerViewAdapter(this);
        recyclerView.setHasFixedSize(true);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && floatingSearchView.getTag().equals("hide")) {
                    floatingSearchView.animate()
                            .translationY(0)
                            .alpha(1)
                            .setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);

                                    floatingSearchView.setTag("show");
                                }
                            });
                }
                else if (dy < 0 && floatingSearchView.getTag().equals("show")) {
                    floatingSearchView.animate()
                            .translationY(-floatingSearchView.findViewById(R.id.search_query_section).getHeight())
                            .alpha(0)
                            .setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);

                                    floatingSearchView.setTag("hide");
                                }
                            });
                }
            }
        });

        // refresh layout
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

    public boolean isAtTop() {
        int[] position = new int[2];
        layoutManager.findFirstCompletelyVisibleItemPositions(position);

        return position[0] == 0;
    }

    public void refresh() {
        refreshLayout.startRefresh();
    }
}
