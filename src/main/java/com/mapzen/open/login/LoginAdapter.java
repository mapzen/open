package com.mapzen.open.login;

import com.mapzen.open.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class LoginAdapter extends PagerAdapter {
    public static final int[] LAYOUTS = {
            R.layout.login_page_2,
            R.layout.login_page_3,
            R.layout.login_page_4 };

    public static final int PAGE_2 = 0;
    public static final int PAGE_3 = 1;
    public static final int PAGE_4 = 2;

    private Context context;

    public LoginAdapter(Context context) {
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View view = View.inflate(context, LAYOUTS[position], null);
        container.addView(view);

        if (position == PAGE_3) {
            view.findViewById(R.id.learn_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://mapzen.com"));
                    context.startActivity(intent);
                }
            });
        }

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return LAYOUTS.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
