package moe.yukisora.yandere.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.core.ServiceGenerator;
import moe.yukisora.yandere.interfaces.CallGenerator;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;

public class RankFragment extends Fragment {
    private static final int ITEM_COUNT = 3;
    private ListFragment[] listFragments;
    private TabLayout tabLayout;

    public static RankFragment newInstance() {
        Bundle args = new Bundle();
        RankFragment fragment = new RankFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        listFragments = new ListFragment[3];

        final ViewPager viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        // view pager
        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public int getCount() {
                return ITEM_COUNT;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        listFragments[0] = ListFragment.newInstance(ListFragment.NONE);
                        listFragments[0].setGenerator(new CallGenerator() {
                            @Override
                            public Call<List<ImageData>> generateCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPopulars(page, "1d");
                            }
                        });
                        return listFragments[0];
                    case 1:
                        listFragments[1] = ListFragment.newInstance(ListFragment.NONE);
                        listFragments[1].setGenerator(new CallGenerator() {
                            @Override
                            public Call<List<ImageData>> generateCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPopulars(page, "1w");
                            }
                        });
                        return listFragments[1];
                    case 2:
                        listFragments[2] = ListFragment.newInstance(ListFragment.NONE);
                        listFragments[2].setGenerator(new CallGenerator() {
                            @Override
                            public Call<List<ImageData>> generateCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPopulars(page, "1m");
                            }
                        });
                        return listFragments[2];
                    default:
                        return null;
                }
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.daily);
                    case 1:
                        return getString(R.string.weekly);
                    case 2:
                        return getString(R.string.monthly);
                    default:
                        return super.getPageTitle(position);
                }
            }
        });
        viewPager.setOffscreenPageLimit(ITEM_COUNT);

        // tab layout
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    public void onTabReSelected() {
        int position = tabLayout.getSelectedTabPosition();
        if (listFragments[position].isAtTop()) {
            listFragments[position].refresh();
        }
        else {
            listFragments[position].goToTop();
        }
    }
}
