package com.mapzen.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchViewAdapter extends FragmentStatePagerAdapter {
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

    @Override
    public int getItemPosition(Object object) {
        // TODO remove this yuck stuggested at
        // http://stackoverflow.com/questions/13695649/refresh-images-on-fragmentstatepageradapter-on-resuming-activity
        // This defeats the whole purpose of using a state adapter ;)
        return POSITION_NONE;
    }
}
