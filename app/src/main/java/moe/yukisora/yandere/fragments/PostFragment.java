package moe.yukisora.yandere.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.footer.BallPulseView;
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout;

import java.util.ArrayList;
import java.util.List;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.activities.SearchActivity;
import moe.yukisora.yandere.adapters.RecyclerViewAdapter;
import moe.yukisora.yandere.core.ImageManager;
import moe.yukisora.yandere.core.TagFilter;
import moe.yukisora.yandere.interfaces.GetCallGenerator;
import moe.yukisora.yandere.modles.ImageData;
import moe.yukisora.yandere.modles.TagData;
import retrofit2.Call;

public class PostFragment extends Fragment {
    private ArrayList<ImageData> imageDatas;
    private Call<List<ImageData>> call;
    private FloatingSearchView floatingSearchView;
    private GetCallGenerator generator;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private TagFilter tagFilter;
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
        tagFilter = new TagFilter(getActivity());
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
        floatingSearchView = view.findViewById(R.id.floatingSearchView);
        final LinearLayout floatingSearchViewBackground = view.findViewById(R.id.floatingSearchViewBackground);
        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.refreshLayout);

        // floating search view
        floatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                floatingSearchView.showProgress();
                tagFilter.filter(newQuery.replace(" ", "_"), new TagFilter.OnFindSuggestionsListener() {
                    @Override
                    public void onResults(Object results) {
                        floatingSearchView.swapSuggestions((ArrayList<TagData>)results);
                        floatingSearchView.hideProgress();
                    }
                });
            }
        });
        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                Intent intent = new Intent(PostFragment.this.getActivity(), SearchActivity.class);
                intent.putExtra("query", searchSuggestion.getBody());

                PostFragment.this.startActivity(intent);
            }

            @Override
            public void onSearchAction(String currentQuery) {
                Intent intent = new Intent(PostFragment.this.getActivity(), SearchActivity.class);
                intent.putExtra("query", currentQuery);

                PostFragment.this.startActivity(intent);
            }
        });
        floatingSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            private String query;

            @Override
            public void onFocus() {
                floatingSearchView.setSearchText(query);
            }

            @Override
            public void onFocusCleared() {
                query = floatingSearchView.getQuery();
                floatingSearchView.setSearchText("");
            }
        });
        floatingSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                TagData tag = (TagData)item;

                textView.setText(tag.tag);
                textView.setTextColor(tag.color);
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                textView.setMarqueeRepeatLimit(-1);
                textView.setSelected(true);
            }
        });

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
                    floatingSearchViewBackground.animate()
                            .translationY(0)
                            .alpha(1)
                            .setDuration(500);
                }
                else if (dy < 0 && floatingSearchView.getTag().equals("show")) {
                    int y = -floatingSearchView.findViewById(R.id.search_query_section).getHeight();
                    floatingSearchView.animate()
                            .translationY(y)
                            .alpha(0)
                            .setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);

                                    floatingSearchView.setTag("hide");
                                }
                            });
                    floatingSearchViewBackground.animate()
                            .translationY(y)
                            .alpha(0)
                            .setDuration(500);
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
