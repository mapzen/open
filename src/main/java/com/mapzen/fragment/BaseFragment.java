package com.mapzen.fragment;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import org.oscim.map.Map;

import retrofit.RetrofitError;

public abstract class BaseFragment extends Fragment {
    protected Map map;
    protected BaseActivity act;
    protected MapFragment mapFragment;
    protected MapzenApplication app;

    public void setAct(BaseActivity act) {
        this.act = act;
        this.app = (MapzenApplication) act.getApplication();
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    protected void onServerError(int status) {
        if (status == 207) {
            Toast.makeText(act, act.getString(R.string.no_route_found), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(act, act.getString(R.string.generic_server_error),
                    Toast.LENGTH_LONG).show();
        }
        act.dismissProgressDialog();
        Log.e(MapzenApplication.LOG_TAG, "request: error: " + String.valueOf(status));
    }

    protected void onServerError(RetrofitError error) {
        Toast.makeText(act, act.getString(R.string.generic_server_error), Toast.LENGTH_LONG).show();
        act.dismissProgressDialog();
        Log.e(MapzenApplication.LOG_TAG, "request: error: " + error.toString());
    }
}
