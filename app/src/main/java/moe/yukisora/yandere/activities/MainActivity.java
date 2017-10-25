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
                if (viewPager.getCurrentItem() == 0) {
                    postFragments[0].goToTop();
                }
                else {
                    viewPager.setCurrentItem(0);
                }
            }
        });

        imageButtonRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager.getCurrentItem() == 1) {
                    postFragments[1].goToTop();
                }
                else {
                    viewPager.setCurrentItem(1);
                }
            }
        });

        imageButtonPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager.getCurrentItem() == 2) {
                    postFragments[2].goToTop();
                }
                else {
                    viewPager.setCurrentItem(2);
                }
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
