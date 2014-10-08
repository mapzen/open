package com.mapzen.open.fragment;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.activity.BaseActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import retrofit.RetrofitError;

import static com.mapzen.open.activity.BaseActivity.COM_MAPZEN_UPDATE_VIEW;

public abstract class BaseFragment extends Fragment implements BaseActivity.ViewUpdater {
    protected BaseActivity act;
    protected MapFragment mapFragment;
    protected MapzenApplication app;
    protected ViewUpdatesReceiver viewUpdatesReceiver;

    public void setAct(BaseActivity act) {
        this.act = act;
        this.app = (MapzenApplication) act.getApplication();
    }

    public void inject() {
        app.inject(this);
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    protected void onServerError(int status) {
        if (status == 207) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyError(R.string.no_route_found);
                }

            });
        } else {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyError(R.string.generic_server_error);
                }
            });
        }
        act.hideLoadingIndicator();
        Log.e(MapzenApplication.LOG_TAG, "request: error: " + String.valueOf(status));
    }

    private void notifyError(int messageRes) {
        if (act == null) {
            return;
        }
        Toast.makeText(act, act.getString(messageRes), Toast.LENGTH_LONG).show();
    }

    protected void onServerError(RetrofitError error) {
        Toast.makeText(act, act.getString(R.string.generic_server_error), Toast.LENGTH_LONG).show();
        act.hideLoadingIndicator();
        Log.e(MapzenApplication.LOG_TAG, "request: error: " + error.toString());
    }

    protected void registerViewUpdater() {
        viewUpdatesReceiver = new ViewUpdatesReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(COM_MAPZEN_UPDATE_VIEW);
        act.registerReceiver(viewUpdatesReceiver, filter);
    }

    protected void unregisterViewUpdater() {
        act.unregisterReceiver(viewUpdatesReceiver);
    }

    @Override
    public void onViewUpdate() {
    }

    public class ViewUpdatesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onViewUpdate();
        }
    }
}
