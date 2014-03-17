package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.entity.Feature;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.util.ApiHelper.getRouteUrlForCar;

public class ItemFragment extends BaseFragment {
    private Feature feature;

    @InjectView(R.id.title)
    TextView title;

    @InjectView(R.id.address)
    TextView address;

    @InjectView(R.id.start)
    TextView startButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.search_item, container, false);
        ButterKnife.inject(this, view);
        initTitle();
        initAddress();
        return view;
    }

    private void initTitle() {
        title.setText(feature.getProperty(Feature.NAME));
    }

    private void initAddress() {
        address.setText(String.format(Locale.getDefault(), "%s, %s",
                feature.getProperty(Feature.ADMIN1_NAME),
                feature.getProperty(Feature.ADMIN1_ABBR)));
    }

    @OnClick(R.id.start)
    public void start() {
        act.hideActionBar();
        act.showProgressDialog();
        mapFragment.clearMarkers();
        mapFragment.updateMap();
        app.enqueueApiRequest(getRouteRequest());
    }

    private JsonObjectRequest getRouteRequest() {
        final RouteFragment routeFragment = RouteFragment.newInstance(act, feature);

        final String url = getRouteUrlForCar(getMapController().getZoomLevel(),
                mapFragment.getUserLocationPoint(), routeFragment.getDestinationPoint());

        final Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (routeFragment.setRoute(response)) {
                    act.getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .add(R.id.routes_container, routeFragment, RouteFragment.TAG)
                            .commit();
                } else {
                    Toast.makeText(act,
                            act.getString(R.string.no_route_found), Toast.LENGTH_LONG).show();
                    act.showActionBar();
                }
                act.dismissProgressDialog();
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                onServerError(volleyError);
            }
        };

        return new JsonObjectRequest(url, null, successListener, errorListener);
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }
}
