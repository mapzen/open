package com.mapzen.support;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.mapzen.R;

public class DummyActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy);
    }
}
