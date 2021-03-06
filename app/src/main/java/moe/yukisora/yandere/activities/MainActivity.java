package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;
import com.squareup.seismic.ShakeDetector;

import java.util.List;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.core.ServiceGenerator;
import moe.yukisora.yandere.core.ShakeDetectorListener;
import moe.yukisora.yandere.fragments.ListFragment;
import moe.yukisora.yandere.fragments.RankFragment;
import moe.yukisora.yandere.fragments.SettingFragment;
import moe.yukisora.yandere.interfaces.CallGenerator;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;

public class MainActivity extends Activity {
    private static final int ITEM_COUNT = 3;
    private ListFragment listFragment;
    private RankFragment rankFragment;
    private RelativeLayout progressBar;
    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;
    private ShakeDetectorListener shakeDetectorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        shakeDetectorListener = new ShakeDetectorListener(this, new ShakeDetectorListener.ShakeDetectorCallback() {
            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
            }
        });
        shakeDetector = new ShakeDetector(shakeDetectorListener);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        shakeDetector.start(sensorManager);
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

    @Override
    protected void onStop() {
        shakeDetector.stop();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
            shakeDetectorListener.stopLoading();
        }
        else {
            super.onBackPressed();
        }
    }

    private void initView() {
        final ViewPager viewPager = findViewById(R.id.viewPager);
        final BottomBar bottomBar = findViewById(R.id.bottomBar);
        progressBar = findViewById(R.id.progressBar);

        // view pager
        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public int getCount() {
                return ITEM_COUNT;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        listFragment = ListFragment.newInstance(ListFragment.LOAD | ListFragment.SEARCH);
                        listFragment.setGenerator(new CallGenerator() {
                            @Override
                            public Call<List<ImageData>> generateCall(int page) {
                                YandereService service = ServiceGenerator.generate(YandereService.class);

                                return service.getPosts(page, null);
                            }
                        });
                        return listFragment;
                    case 1:
                        rankFragment = RankFragment.newInstance();
                        return rankFragment;
                    case 2:
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
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setOffscreenPageLimit(ITEM_COUNT);

        // bottom bar
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.list_item:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.rank_item:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.setting_item:
                        viewPager.setCurrentItem(2);
                        break;
                }
            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.list_item:
                        if (listFragment.isAtTop()) {
                            listFragment.refresh();
                        }
                        else {
                            listFragment.goToTop();
                        }
                        break;
                    case R.id.rank_item:
                        rankFragment.onTabReSelected();
                        break;
                }
            }
        });

        // progress bar
        progressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }
}
