package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.List;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.core.ServiceGenerator;
import moe.yukisora.yandere.fragments.PostFragment;
import moe.yukisora.yandere.fragments.SearchFragment;
import moe.yukisora.yandere.fragments.SettingFragment;
import moe.yukisora.yandere.interfaces.GetCallGenerator;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;

public class MainActivity extends Activity {
    private static final int NUM_ITEMS = 5;
    private PostFragment[] postFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postFragments = new PostFragment[3];
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
                        postFragments[0] = PostFragment.newInstance(true);
                        postFragments[0].setGenerator(new GetCallGenerator() {
                            @Override
                            public Call<List<ImageData>> getCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPosts(page, null);
                            }
                        });
                        return postFragments[0];
                    case 1:
                        postFragments[1] = PostFragment.newInstance(true);
                        postFragments[1].setGenerator(new GetCallGenerator() {
                            private String tags = "order:random";

                            @Override
                            public Call<List<ImageData>> getCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPosts(page, tags);
                            }
                        });
                        return postFragments[1];
                    case 2:
                        postFragments[2] = PostFragment.newInstance(false);
                        postFragments[2].setGenerator(new GetCallGenerator() {
                            @Override
                            public Call<List<ImageData>> getCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPopulars(page);
                            }
                        });
                        return postFragments[2];
                    case 3:
                        return SearchFragment.newInstance();
                    case 4:
                        return SettingFragment.newInstance();
                    default:
                        return null;
                }
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                bottomBar.setDefaultTabPosition(position);

                // close soft keyboard
//                InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (manager != null) {
//                    manager.hideSoftInputFromWindow(findViewById(R.id.searchView).getWindowToken(), 0);
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
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
                    case R.id.search_item:
                        viewPager.setCurrentItem(3);
                        break;
                    case R.id.setting_item:
                        viewPager.setCurrentItem(4);
                        break;
                }
            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.list_item:
                        postFragments[0].goToTop();
                        break;
                    case R.id.random_item:
                        postFragments[1].goToTop();
                        break;
                    case R.id.rank_item:
                        postFragments[2].goToTop();
                }
            }
        });
    }
}
