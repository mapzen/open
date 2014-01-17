package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;

public class ListResultsFragment extends ListFragment {
    private FrameLayout wrapper;
    private BaseActivity act;

    public void setAct(BaseActivity act) {
        this.act = act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_results_list,
                container, false);
        return view;
    }

    public void attachToContainer(int container) {
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(container, this, "full results")
                .commit();
        wrapper = (FrameLayout) act.findViewById(R.id.full_list);
        wrapper.setVisibility(View.VISIBLE);
        act.blurSearchMenu();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        wrapper.setVisibility(View.GONE);
    }
}
