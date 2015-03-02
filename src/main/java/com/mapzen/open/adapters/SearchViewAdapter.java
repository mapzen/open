package com.mapzen.open.adapters;

import com.mapzen.open.fragment.ItemFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class SearchViewAdapter extends FragmentPagerAdapter {
    private List<ItemFragment> fragments;

    public SearchViewAdapter(FragmentManager fm, List<ItemFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return fragments.indexOf(object);
    }
}
