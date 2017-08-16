package moe.yukisora.yandere.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.fragments.PostFragment;
import moe.yukisora.yandere.fragments.SearchFragment;
import moe.yukisora.yandere.fragments.SettingFragment;

public class MainActivity extends Activity {
    private static final int NUM_ITEMS = 5;
    private static final String POST_URL = "https://yande.re/post.json/?limit=20&page=";
    private static final String RANDOM_URL = "https://yande.re/post.json/?limit=20&tags=order:random&page=";
    private static final String POPULAR_URL = "https://yande.re/post/popular_recent.json/?page=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // configure ViewPager
        final ViewPager viewPager = findViewById(R.id.viewPager);
        final ImageButton imageButtonPost = findViewById(R.id.post);
        final ImageButton imageButtonRandom = findViewById(R.id.random);
        final ImageButton imageButtonPopular = findViewById(R.id.popular);
        final ImageButton imageButtonSetting = findViewById(R.id.setting);
        final ImageButton imageButtonSearch = findViewById(R.id.search);

        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public int getCount() {
                return NUM_ITEMS;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return PostFragment.newInstance(POST_URL, true);
                    case 1:
                        return PostFragment.newInstance(RANDOM_URL, true);
                    case 2:
                        return PostFragment.newInstance(POPULAR_URL, false);
                    case 3:
                        return SearchFragment.newInstance();
                    case 4:
                        return SettingFragment.newInstance();
                    default:
                        return null;
                }
            }
        });
        viewPager.setOffscreenPageLimit(5);

        // dynamic change button image
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int lastPosition;
            private ImageButton[] imageButtons = new ImageButton[]{
                    imageButtonPost,
                    imageButtonRandom,
                    imageButtonPopular,
                    imageButtonSearch,
                    imageButtonSetting
            };
            private int[] normalImages = new int[]{
                    R.drawable.list,
                    R.drawable.random,
                    R.drawable.rank,
                    R.drawable.search,
                    R.drawable.setting
            };
            private int[] focusedImages = new int[]{
                    R.drawable.list_focused,
                    R.drawable.random_focused,
                    R.drawable.rank_focused,
                    R.drawable.search_focused,
                    R.drawable.setting_focused
            };

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                imageButtons[lastPosition].setImageResource(normalImages[lastPosition]);
                imageButtons[position].setImageResource(focusedImages[position]);
                lastPosition = position;

                // close soft keyboard
                InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(findViewById(R.id.searchView).getWindowToken(), 0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // configure Button
        imageButtonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
            }
        });

        imageButtonRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
            }
        });

        imageButtonPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(2);
            }
        });

        imageButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(3);
            }
        });

        imageButtonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(4);
            }
        });
    }
}
