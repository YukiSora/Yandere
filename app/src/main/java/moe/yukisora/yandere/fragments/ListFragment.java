package moe.yukisora.yandere.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.activities.SearchActivity;
import moe.yukisora.yandere.adapters.ListRecyclerViewAdapter;
import moe.yukisora.yandere.core.TagFilter;
import moe.yukisora.yandere.interfaces.CallGenerator;
import moe.yukisora.yandere.modles.ImageData;
import moe.yukisora.yandere.modles.TagData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFragment extends Fragment {
    public static final int NONE = 0;
    public static final int LOAD = 1;
    public static final int SEARCH = 2;
    private static final int START_REFRESH = 0;
    private static final int START_LOAD = 1;
    private ArrayList<ImageData> imageDatas;
    private Call<List<ImageData>> call;
    private FloatingSearchView floatingSearchView;
    private CallGenerator generator;
    private Handler handler;
    private RecyclerView recyclerView;
    private ListRecyclerViewAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private TagFilter tagFilter;
    private TwinklingRefreshLayout refreshLayout;
    private boolean isLoadable;
    private boolean isLoading;
    private boolean isSearchable;
    private int page;

    public static ListFragment newInstance(int type) {
        Bundle args = new Bundle();
        ListFragment fragment = new ListFragment();
        args.putInt("type", type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        int type = getArguments().getInt("type");
        isLoadable = (type & LOAD) == LOAD;
        isSearchable = (type & SEARCH) == SEARCH;

        if (isSearchable) {
            tagFilter = new TagFilter(getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        initPage();
        initView(view);
        refreshLayout.startRefresh();

        return view;
    }

    private void initPage() {
        imageDatas = new ArrayList<>();
        page = 1;
        isLoading = false;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void initView(View view) {
        floatingSearchView = view.findViewById(R.id.floatingSearchView);
        final LinearLayout floatingSearchViewBackground = view.findViewById(R.id.floatingSearchViewBackground);
        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.refreshLayout);

        // floating search view
        if (!isSearchable) {
            floatingSearchView.setVisibility(View.GONE);
            floatingSearchViewBackground.setVisibility(View.GONE);
        }
        floatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                floatingSearchView.showProgress();
                tagFilter.getSuggestions(newQuery.replace(" ", "_"), new TagFilter.OnFindSuggestionsListener() {
                    @Override
                    public void onResults(ArrayList<TagData> results) {
                        floatingSearchView.swapSuggestions(results);
                        floatingSearchView.hideProgress();
                    }
                });
            }
        });
        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                onSearchAction(searchSuggestion.getBody());
            }

            @Override
            public void onSearchAction(String currentQuery) {
                if (!currentQuery.equals("")) {
                    String query = currentQuery.replace(" ", "_");
                    tagFilter.addHistory(query);

                    Intent intent = new Intent(ListFragment.this.getActivity(), SearchActivity.class);
                    intent.putExtra("query", query);

                    ListFragment.this.startActivity(intent);
                }
            }
        });
        floatingSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            private String query;

            @Override
            public void onFocus() {
                if (query == null || query.equals("")) {
                    floatingSearchView.swapSuggestions(tagFilter.getHistory());
                }
                else {
                    floatingSearchView.setSearchText(query);
                }
            }

            @Override
            public void onFocusCleared() {
                query = floatingSearchView.getQuery();
                floatingSearchView.clearQuery();
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

                if (tag.isHistory) {
                    leftIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_history));
                    leftIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.historyIconColor));
                }
            }
        });

        // recycler view
        recyclerView.setHasFixedSize(true);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ListRecyclerViewAdapter(this);
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
        refreshLayout.setBottomHeight(40);
        refreshLayout.setMaxBottomHeight(240);
        refreshLayout.setOverScrollRefreshShow(false);
        refreshLayout.setAutoLoadMore(isLoadable);
        refreshLayout.setEnableLoadmore(isLoadable);
        refreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                if (call != null) {
                    call.cancel();
                }
                initPage();
                loadImage(START_REFRESH);
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                loadImage(START_LOAD);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public void loadImage(final int type) {
        call = generator.generateCall(page);
        if (!isLoading) {
            isLoading = true;
            call.enqueue(new Callback<List<ImageData>>() {
                @Override
                public void onResponse(@NonNull Call<List<ImageData>> call, @NonNull Response<List<ImageData>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final int positionStart = imageDatas.size();
                        int positionEnd = positionStart;

                        for (ImageData imageData : response.body()) {
                            // calculate ImageView height
                            imageData.layout_height = Math.round(YandereApplication.getSmallImageLayoutWidth() * imageData.actual_preview_height / imageData.actual_preview_width);

                            if (!YandereApplication.isSafe() || imageData.rating.equalsIgnoreCase("s")) {
                                imageData.list_id = positionEnd++;
                                imageDatas.add(imageData);
                            }
                        }
                        final int count = positionEnd - positionStart;

                        handler.post(new Runnable() {
                            public void run() {
                                adapter.notifyItemRangeInserted(positionStart, count);
                                if (type == START_REFRESH) {
                                    refreshLayout.finishRefreshing();
                                }
                                else if (type == START_LOAD) {
                                    refreshLayout.finishLoadmore();
                                }
                            }
                        });
                        page++;
                        isLoading = false;
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ImageData>> call, @NonNull Throwable throwable) {
                    handler.post(new Runnable() {
                        public void run() {
                            if (type == START_REFRESH) {
                                refreshLayout.finishRefreshing();
                                Toast.makeText(getActivity(), "Refresh failed", Toast.LENGTH_SHORT).show();
                            }
                            else if (type == START_LOAD) {
                                refreshLayout.finishLoadmore();
                                Toast.makeText(getActivity(), "Load failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }

    public ArrayList<ImageData> getImageDatas() {
        return imageDatas;
    }

    public void setGenerator(CallGenerator generator) {
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
