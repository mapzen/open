package com.mapzen.open.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class IntentReceiver extends BroadcastReceiver {
    private IntentFilter filter;

    public IntentReceiver(String action) {
        this.filter = new IntentFilter();
        filter.addAction(action);
    }

    public IntentFilter getIntentFilter() {
        return filter;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
    }
}
