package moe.yukisora.yandere;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;

public class MainActivity extends Activity {
    private static final int NUM_ITEMS = 4;
    private static File directory;
    private static boolean isSafe;
    private static int dpi;
    private static int maxMemory;
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
        //Memory
        maxMemory = 1024 * 1024 * ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        //Preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isSafe = preferences.getBoolean("isSafe", true);

        directory = new File(Environment.getExternalStorageDirectory(), "Yandere");
        if (!directory.exists())
            if (!directory.mkdir())
                directory = null;

        //configure ViewPager
        final ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
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
                        return SettingFragment.newInstance();
                    default:
                        return null;
                }
            }
        });

        final ImageButton postBtn = ((ImageButton)findViewById(R.id.post));
        final ImageButton randomBtn = ((ImageButton)findViewById(R.id.random));
        final ImageButton popularBtn = ((ImageButton)findViewById(R.id.popular));
        final ImageButton settingBtn = ((ImageButton)findViewById(R.id.setting));

        //configure Button
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
                postBtn.setImageResource(R.drawable.list_focused);
                randomBtn.setImageResource(R.drawable.random);
                popularBtn.setImageResource(R.drawable.rank);
                settingBtn.setImageResource(R.drawable.setting);
            }
        });

        randomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
                postBtn.setImageResource(R.drawable.list);
                randomBtn.setImageResource(R.drawable.random_focused);
                popularBtn.setImageResource(R.drawable.rank);
                settingBtn.setImageResource(R.drawable.setting);
            }
        });

        popularBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(2);
                postBtn.setImageResource(R.drawable.list);
                randomBtn.setImageResource(R.drawable.random);
                popularBtn.setImageResource(R.drawable.rank_focused);
                settingBtn.setImageResource(R.drawable.setting);
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(3);
                postBtn.setImageResource(R.drawable.list);
                randomBtn.setImageResource(R.drawable.random);
                popularBtn.setImageResource(R.drawable.rank);
                settingBtn.setImageResource(R.drawable.setting_focused);
            }
        });
    }
}
