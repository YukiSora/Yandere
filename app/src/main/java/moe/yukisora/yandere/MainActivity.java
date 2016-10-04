package moe.yukisora.yandere;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;

public class MainActivity extends Activity {
    private static final int NUM_ITEMS = 5;
    private static File directory;
    private static boolean isSafe;
    private static int dpi;
    private static int maxMemory;
    private static int smallPlaceholderSize;
    private static int screenWidth;

    public static File getDirectory() {
        return directory;
    }

    public static boolean isSafe() {
        return isSafe;
    }

    public static void setSafe(boolean isSafe) {
        MainActivity.isSafe = isSafe;
    }

    public static int getDpi() {
        return dpi;
    }

    public static int getMaxMemory() {
        return maxMemory;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getSmallPlaceholderSize() {
        return smallPlaceholderSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize variable
        //DisplayMetrics
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dpi = metrics.densityDpi;
        screenWidth = metrics.widthPixels;
        //placeholder image size
        smallPlaceholderSize = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_small).getWidth();
        //Memory
        maxMemory = 1024 * 1024 * ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        //Preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isSafe = preferences.getBoolean("isSafe", true);

        //create folder
        directory = new File(Environment.getExternalStorageDirectory(), "Yandere");
        if (!directory.exists())
            if (!directory.mkdir())
                directory = null;

        //configure ViewPager
        final ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
        final ImageButton postBtn = ((ImageButton)findViewById(R.id.post));
        final ImageButton randomBtn = ((ImageButton)findViewById(R.id.random));
        final ImageButton popularBtn = ((ImageButton)findViewById(R.id.popular));
        final ImageButton settingBtn = ((ImageButton)findViewById(R.id.setting));
        final ImageButton searchBtn = ((ImageButton)findViewById(R.id.search));

        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public int getCount() {
                return NUM_ITEMS;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return PostFragment.newInstance("https://yande.re/post.json?limit=20&page=", true);
                    case 1:
                        return PostFragment.newInstance("https://yande.re/post.json?limit=20&tags=order:random&page=", true);
                    case 2:
                        return PostFragment.newInstance("https://yande.re/post/popular_recent.json?page=", false);
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

        //dynamic change button image
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int lastPosition;
            private ImageButton[] imageButtons = new ImageButton[]{postBtn, randomBtn, popularBtn, searchBtn, settingBtn};
            private int[] normalResourceId = new int[]{R.drawable.list, R.drawable.random, R.drawable.rank, R.drawable.search, R.drawable.setting};
            private int[] focusedResourceId = new int[]{R.drawable.list_focused, R.drawable.random_focused, R.drawable.rank_focused, R.drawable.search_focused, R.drawable.setting_focused};

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                imageButtons[lastPosition].setImageResource(normalResourceId[lastPosition]);
                imageButtons[position].setImageResource(focusedResourceId[position]);
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //configure Button
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
            }
        });

        randomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
            }
        });

        popularBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(2);
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(3);
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(4);
            }
        });
    }
}
