package com.mapzen;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchViewAdapter extends FragmentPagerAdapter {
    private Context context;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    public SearchViewAdapter(Context act, FragmentManager fm) {
        super(fm);
        this.context = act;
    }

    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
        notifyDataSetChanged();
    }

    public void clearFragments() {
        fragments.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }
}
