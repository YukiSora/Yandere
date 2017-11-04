package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.List;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.core.ServiceGenerator;
import moe.yukisora.yandere.fragments.ListFragment;
import moe.yukisora.yandere.fragments.SettingFragment;
import moe.yukisora.yandere.interfaces.GetCallGenerator;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;

public class MainActivity extends Activity {
    private static final int NUM_ITEMS = 4;
    private ListFragment[] listFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listFragments = new ListFragment[3];
        final ViewPager viewPager = findViewById(R.id.viewPager);
        final BottomBar bottomBar = findViewById(R.id.bottomBar);

        // view pager
        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public int getCount() {
                return NUM_ITEMS;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        listFragments[0] = ListFragment.newInstance(ListFragment.LOAD | ListFragment.SEARCH);
                        listFragments[0].setGenerator(new GetCallGenerator() {
                            @Override
                            public Call<List<ImageData>> getCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPosts(page, null);
                            }
                        });
                        return listFragments[0];
                    case 1:
                        listFragments[1] = ListFragment.newInstance(ListFragment.LOAD | ListFragment.SEARCH);
                        listFragments[1].setGenerator(new GetCallGenerator() {
                            private String tags = "order:random";

                            @Override
                            public Call<List<ImageData>> getCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPosts(page, tags);
                            }
                        });
                        return listFragments[1];
                    case 2:
                        listFragments[2] = ListFragment.newInstance(ListFragment.NONE);
                        listFragments[2].setGenerator(new GetCallGenerator() {
                            @Override
                            public Call<List<ImageData>> getCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPopulars(page);
                            }
                        });
                        return listFragments[2];
                    case 3:
                        return SettingFragment.newInstance();
                    default:
                        return null;
                }
            }
        });

        // bottom bar
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.list_item:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.random_item:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.rank_item:
                        viewPager.setCurrentItem(2);
                        break;
                    case R.id.setting_item:
                        viewPager.setCurrentItem(3);
                        break;
                }
            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.list_item:
                        if (listFragments[0].isAtTop()) {
                            listFragments[0].refresh();
                        }
                        else {
                            listFragments[0].goToTop();
                        }
                        break;
                    case R.id.random_item:
                        if (listFragments[1].isAtTop()) {
                            listFragments[1].refresh();
                        }
                        else {
                            listFragments[1].goToTop();
                        }
                        break;
                    case R.id.rank_item:
                        if (listFragments[2].isAtTop()) {
                            listFragments[2].refresh();
                        }
                        else {
                            listFragments[2].goToTop();
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        View view = getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null && inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        super.onPause();
    }
}
