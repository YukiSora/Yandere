package moe.yukisora.yandere;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class MainActivity extends Activity {
    private static final int NUM_ITEMS = 4;
    private static int maxMemory;

    public static int getMaxMemory() {
        return maxMemory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize variable
        maxMemory = 1024 * 1024 * ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        //configure ViewPager
        final ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public int getCount() {
                return NUM_ITEMS;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return PostFragment.newInstance("https://yande.re/post.json?page=");
                    case 1:
                        return PostFragment.newInstance("https://yande.re/post.json?tags=order:random&page=");
                    case 2:
                        return RandomFragment.newInstance();
                    case 3:
                        return RandomFragment.newInstance();
                    default:
                        return null;
                }
            }
        });

        //configure Button
        findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
            }
        });

        findViewById(R.id.random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
            }
        });

        findViewById(R.id.popular).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(2);
            }
        });

        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(3);
            }
        });
    }
}
